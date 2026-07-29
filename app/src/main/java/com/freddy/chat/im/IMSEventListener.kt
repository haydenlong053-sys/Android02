package com.freddy.chat.im

import android.content.Context
import android.net.ConnectivityManager
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import com.blankj.utilcode.util.ThreadUtils
import com.cboda.baselibrary.app.UtilBroadApplication
import com.cboda.baselibrary.utils.MMKVBroadUtils
import com.cboda.baselibrary.utils.eLogD
import com.cboda.baselibrary.utils.getAppContext
import com.cboda.baselibrary.utils.toLog
import com.cboda.chatui.quickmeet.MeetCallManager
import com.freddy.chat.im.othbusi.OthBusiSigHandlers
import com.freddy.im.bean.AppMessage
import com.freddy.im.call.CallSignalSender
import com.freddy.im.crypto.ImCryptoKeys
import com.freddy.im.db.ImLocalStore
import com.freddy.im.db.repository.ImMessageRepository.OnMessageUpdatedCallback
import com.freddy.im.http.model.im.SessionItem
import com.freddy.im.listener.OnEventListener
import com.freddy.im.message.ImGroupChangeManager
import com.freddy.im.message.ImGroupMemberChangeManager
import com.freddy.im.message.ImGroupTransferManager
import com.freddy.im.message.ImMessageManager
import com.freddy.im.signal.ImSignalProtos
import com.freddy.im.signal.ImSignalProtos.CountMsgReadSig
import com.freddy.im.signal.ImSignalProtos.GroupChangeSig
import com.freddy.im.signal.ImSignalProtos.MemberChangeSig
import com.freddy.im.signal.ImSignalProtos.MessageReadSig
import com.freddy.im.signal.ImSignalProtos.MessageRepealSig
import com.freddy.im.signal.ImSignalProtos.NewMsgSignal
import com.freddy.im.signal.ImSignalProtos.NewMsgSignal.MsgSig
import com.freddy.im.signal.ImSignalProtos.TransferGroupSig
import com.freddy.webtrc.activity.CallActivity
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import kotlin.collections.LinkedHashMap
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.concurrent.Volatile

/**
 * SeekWeb4 + imsignal 回调到应用层 [MessageProcessor]。
 */
class IMSEventListener(private val userId: String?, private val token: String?) : OnEventListener {
    private val tAG = "IMSEventListenerClass"

    private val RSA_PUBLIC_ASSET = "public_key.pem"
//    private val mainHandler = Handler(Looper.getMainLooper())

    @Volatile
    private var rsaPublicKeyPem: String? = null

    override fun onSignalResponse(res: ImSignalProtos.SignalResponse?) {
        if (res?.hasHeader() == false) {
            return
        }
        val cmd = res?.header?.cmd
        if (cmd == ImSignalProtos.Cmd.BIND_USER_RES_VALUE && res.header.code == 0) {
            //todo 第五步，连接成功刷新会话列表
            MessageProcessor.getInstance().notifyConversationList()
        }
    }

    override fun onSignalPush(push: ImSignalProtos.SignalPush?) {
        if (!push!!.hasHeader()) {
            return
        }
        eLogD(tAG, "收到服务端的onSignalPush信令响应cmd: " + push.header.cmd)

        if (push.header.cmd == ImSignalProtos.Cmd.NEW_MSGSIG_VALUE && push.hasBody()) {
            try {
                val nm = NewMsgSignal.parseFrom(push.body.toByteArray())
                // messageId 形如 uid1!uid2-1046：uid1!uid2 为会话锚点，1046 为序列号；同锚点只保留序列最大的 MsgSig
                val bestByConversationAnchor: MutableMap<String?, MsgSig?> = LinkedHashMap<String?, MsgSig?>()
                for (i in 0..<nm.signalsCount) {
                    val sig = nm.getSignals(i)
                    val fullMessageId = sig.getMessageId()
                    val anchor = ImMessageManager.extractConversationAnchor(fullMessageId)
                    if (anchor == null) {
                        eLogD(tAG, "跳过无法解析锚点的 messageId: $fullMessageId")
                        continue
                    }
                    val seq = ImMessageManager.extractTrailingSequence(fullMessageId)
                    if (seq == ImMessageManager.INVALID_SEQUENCE) {
                        eLogD(tAG, "跳过无有效序列号的 messageId: $fullMessageId")
                        continue
                    }
                    val existing = bestByConversationAnchor[anchor]
                    if (existing == null) {
                        bestByConversationAnchor[anchor] = sig
                        continue
                    }
                    val existingSeq = ImMessageManager.extractTrailingSequence(existing.getMessageId())
                    if (seq > existingSeq) {
                        bestByConversationAnchor[anchor] = sig
                    }
                }
                val deduped = bestByConversationAnchor.values
                for (s in deduped) {
                    eLogD(tAG, "NEW_MSGSIG 拉列表 messageId=" + s?.getMessageId() + " unread=" + s?.unreadCount)
                    s?.getMessageId()?.let {
                        ImMessageManager.getInstance().fetchNewMessageList(
                            it
                        ) { batch: MutableList<AppMessage?>? -> MessageProcessor.getInstance().receiveMsg(batch) }
                    }

                }
                // cmd=516 NEW_MSGSIG：新消息通知后刷新会话列表
//                MessageProcessor.getInstance().notifyConversationList()
            } catch (e: Exception) {
                e.toLog(tAG + "_解析 NewMsgSignal 失败")
            }
        }

        // 群变更：0x0101 / cmd=257 GroupChangeSig.groupList → 拉群详情 → 入库 → 更新群会话列表
        else if (push.header.cmd == ImSignalProtos.Cmd.GROUP_CHANGE_VALUE) {
            try {
                if (!push.hasBody()) {
                    eLogD(tAG, "GROUP_CHANGE(cmd=257) 无 body，直接刷新会话列表")
                    MessageProcessor.getInstance().notifyConversationList()
                    return
                }
                val sig = GroupChangeSig.parseFrom(push.body.toByteArray())
                val groupList = sig.groupListList.filterNotNull().filter { it.isNotEmpty() }
                eLogD(tAG, "GROUP_CHANGE(cmd=257) groupList size=${groupList.size}")
                if (groupList.isEmpty()) {
                    MessageProcessor.getInstance().notifyConversationList()
                    return
                }
                ImGroupChangeManager.getInstance().handleGroupChange(groupList) { sessions ->
                    if (!sessions.isNullOrEmpty()) {
                        MessageProcessor.getInstance().notifySessionsUpdated(sessions)
                    }
                    // 再拉全量会话列表，确保群会话页完整刷新
                    MessageProcessor.getInstance().notifyConversationList()
                }
            } catch (e: Exception) {
                eLogD(tAG, "解析 GroupChangeSig 失败", e)
                MessageProcessor.getInstance().notifyConversationList()
            }
        }


        // 群成员变更：0x0102 / cmd=258 MemberChangeSig → 拉成员 / 软删除 → 更新群会话列表
        else if (push.header.cmd == ImSignalProtos.Cmd.GROUP_MEMBER_CHANGE_VALUE && push.hasBody()) {
            try {
                val sig = MemberChangeSig.parseFrom(
                    push.body.toByteArray()
                )
                val groupId = sig.getGroupId()
                eLogD(
                    tAG, ("GROUP_MEMBER_CHANGE groupId=" + groupId
                            + " enters=" + sig.entersCount
                            + " leaves=" + sig.leavesCount
                            + " kickouts=" + sig.kickoutsCount)
                )
                ImGroupMemberChangeManager.getInstance().handleMemberChange(
                    groupId,
                    sig.entersList,
                    sig.leavesList,
                    sig.kickoutsList
                )
                if (TextUtils.isEmpty(groupId)) {
                    return
                }
                val myId = MMKVBroadUtils.getRealUserId()
                val selfRemoved = !myId.isNullOrEmpty() && (
                    sig.leavesList.any { it == myId } || sig.kickoutsList.any { it == myId }
                )
                if (selfRemoved) {
                    // 自己退群/被踢：移除本地会话并刷新列表
                    ImLocalStore.removeSessionCompletely(groupId) {
                        MessageProcessor.getInstance().notifySessionRemoved(groupId)
                    }
                } else {
                    // 同步该群会话到会话列表（对齐 0x0101 GROUP_CHANGE）
                    ImGroupChangeManager.getInstance().handleGroupChange(
                        listOf(groupId)
                    ) { sessions: MutableList<SessionItem?>? ->
                        MessageProcessor.getInstance().notifySessionsUpdated(sessions)
                    }
                }
            } catch (e: Exception) {
                eLogD(tAG, "解析 MemberChangeSig 失败", e)
            }
        }

        // 转让群主：0x0104 / cmd=260 TransferGroupSig → 回写 owerId/角色 → 刷新私群公群分类
        else if (push.header.cmd == ImSignalProtos.Cmd.TRANSFER_GROUP_VALUE && push.hasBody()) {
            try {
                val sig = TransferGroupSig.parseFrom(push.body.toByteArray())
                val groupId = sig.groupId
                val newOwner = sig.newOwer
                eLogD(tAG, "TRANSFER_GROUP(cmd=260) groupId=$groupId newOwer=$newOwner")
                ImGroupTransferManager.getInstance().handleTransfer(groupId, newOwner) { gid, sessions ->
//                    if (!sessions.isNullOrEmpty()) {
//                        MessageProcessor.getInstance().notifySessionsUpdated(sessions)
//                    }
//                    if (!gid.isNullOrEmpty()) {
//                        MessageProcessor.getInstance().notifyGroupMemberRolesUpdated(gid)
//                    }
                    MessageProcessor.getInstance().notifyConversationList()
                }
            } catch (e: Exception) {
                eLogD(tAG, "解析 TransferGroupSig 失败", e)
                MessageProcessor.getInstance().notifyConversationList()
            }
        }


        // 单聊 0x0207：对方已读，messageId 序列及之前己方消息展示已读
        else if (push.header.cmd == ImSignalProtos.Cmd.MSG_READ_VALUE && push.hasBody()) {
            try {
                val sig = MessageReadSig.parseFrom(
                    push.body.toByteArray()
                )
                val messageId = sig.getMessageId()
                eLogD(tAG, "MSG_READ messageId=$messageId")
                MessageProcessor.getInstance().notifyPeerMessageRead(messageId)
            } catch (e: Exception) {
                eLogD(tAG, "解析 MessageReadSig 失败", e)
            }
        }

        // 消息撤回：0x0202 MessageRepealSig
        else if (push.header.cmd == ImSignalProtos.Cmd.MSG_REPEAL_VALUE && push.hasBody()) {
            try {
                val sig = MessageRepealSig.parseFrom(
                    push.body.toByteArray()
                )
                val messageId = sig.getMessageId()
                eLogD(tAG, "MSG_REPEAL messageId=$messageId")
                if (TextUtils.isEmpty(messageId)) {
                    return
                }
                ImLocalStore.applyMessageRepeal(messageId, OnMessageUpdatedCallback { recalled: AppMessage? ->
                    if (recalled != null) {
                        MessageProcessor.getInstance().notifyMessageRepealed(messageId, recalled)
                        return@OnMessageUpdatedCallback
                    }
                    ImMessageManager.getInstance().fetchNewMessageList(
                        messageId
                    ) { batch: MutableList<AppMessage?>? -> MessageProcessor.getInstance().receiveMsg(batch) }
                })
            } catch (e: Exception) {
                eLogD(tAG, "解析 MessageRepealSig 失败", e)
            }
        }


        // 群聊 0x0210：已读人数变动
        else if (push.header.cmd == ImSignalProtos.Cmd.COUNT_MSG_READ_VALUE && push.hasBody()) {
            try {
                val sig = CountMsgReadSig.parseFrom(
                    push.body.toByteArray()
                )
                eLogD(
                    tAG, ("COUNT_MSG_READ messageId=" + sig.getMessageId()
                            + " readPersonCount=" + sig.readPersonCount)
                )
                MessageProcessor.getInstance().notifyGroupMessageReadCount(
                    sig.getMessageId(),
                    sig.readPersonCount
                )
            } catch (e: Exception) {
                eLogD(tAG, "解析 CountMsgReadSig 失败", e)
            }
        }
        // 收到语音/视频通话信令
        else if (push.header.cmd == ImSignalProtos.Cmd.CALL_SIGNAL_VALUE && push.hasBody()) {
            try {
                val sign = ImSignalProtos.CallSignal.parseFrom(push.body.toByteArray())
                eLogD(
                    tAG, ("callData_fromUserId:" + sign.fromUserId
                            + " ,fromUsername=" + sign.fromUsername
                            + " ,type=" + sign.type
                            + " ,roomName=" + sign.roomName
                            + " ,callId=" + sign.callId
                            + " ,mode=" + sign.mode
                            + " ,mediaType=" + sign.mediaType
                            + " ,fromUserIcon=" + sign.fromUserIcon)
                )
                handleCallSignal(sign)
            } catch (e: Exception) {
                e.toLog(tAG + "_解析 CallSignal 失败")
            }
        }

        // 0x0501 其他业务信令：OthBusiSig 按 code 分发（含会议弹幕 MEETING_GIFT=2001）
        else if (push.header.cmd == ImSignalProtos.Cmd.OTHER_BUSI_VALUE && push.hasBody()) {
            eLogD(tAG, "收到 OTHER_BUSI，bodySize=${push.body.size()}")
            OthBusiSigHandlers.get().dispatch(push.body.toByteArray())
        }
    }

    private fun handleCallSignal(sign: ImSignalProtos.CallSignal) {
        when (sign.type) {
            ImSignalProtos.CallSignalType.CALL_INVITE_VALUE -> {
                val selfId = MMKVBroadUtils.getRealUserId()
                if (!TextUtils.isEmpty(selfId) && TextUtils.equals(selfId, sign.fromUserId)) {
                    eLogD(tAG, "忽略自己发出的 CALL_INVITE")
                    return
                }
                val roomName = when {
                    sign.hasRoomName() && !TextUtils.isEmpty(sign.roomName) -> sign.roomName
                    !TextUtils.isEmpty(sign.callId) -> sign.callId
                    else -> ""
                }
                if (TextUtils.isEmpty(sign.fromUserId) || TextUtils.isEmpty(roomName)) {
                    eLogD(tAG, "CALL_INVITE 缺少 fromUserId/roomName，跳过")
                    return
                }
                val video = TextUtils.equals("video", sign.mediaType)
                val callId = if (!TextUtils.isEmpty(sign.callId)) sign.callId else roomName
                val groupId = if (sign.hasGroupId()) sign.groupId else ""
                val participantIds = linkedSetOf<String>()
                if (!TextUtils.isEmpty(sign.fromUserId)) {
                    participantIds.add(sign.fromUserId)
                }
                if (sign.toUserIdsCount > 0) {
                    sign.toUserIdsList.forEach { uid ->
                        if (!TextUtils.isEmpty(uid)) participantIds.add(uid)
                    }
                }
                if (sign.membersCount > 0) {
                    sign.membersList.forEach { uid ->
                        if (!TextUtils.isEmpty(uid)) participantIds.add(uid)
                    }
                }
                if (!TextUtils.isEmpty(selfId)) {
                    participantIds.remove(selfId)
                }
                val sameCall = CallActivity.isSameCall(callId) || CallActivity.isSameCall(roomName)
                // 语音/视频通话中，或会议中：回 CALL_SIGNAL(0x0502) / CALL_BUSY=5，不弹来电页
                val busyNow = (CallActivity.isInCall() && !sameCall)
                        || MeetCallManager.getInstance().isCallingState()
                if (busyNow) {
                    eLogD(tAG, "通话/会议中收到新 INVITE，回忙线 CALL_BUSY callId=$callId from=${sign.fromUserId}")
                    CallSignalSender.busy(callId, sign.fromUserId, roomName, video)
                    return
                }
                // type=1 由产品约定为「用户已接通」通知，此处不再回执 RINGING，避免主叫误判已接通
                eLogD(tAG, "CALL_INVITE groupId=$groupId participants=$participantIds")
                ThreadUtils.getMainHandler().post {
                    CallActivity.startIncoming(
                        getAppContext(),
                        sign.fromUserId,
                        roomName,
                        video,
                        participantIds = participantIds.toList(),
                        callId = sign.callId,
                        groupId = groupId
                    )
                }
            }
            ImSignalProtos.CallSignalType.CALL_RINGING_VALUE -> {
                // 产品约定：0x0502 type=1 表示某用户已接通，刷新通话页 UI
                val matchId = when {
                    !TextUtils.isEmpty(sign.callId) -> sign.callId
                    sign.hasRoomName() && !TextUtils.isEmpty(sign.roomName) -> sign.roomName
                    else -> ""
                }
                eLogD(tAG, "用户已接通 CALL_RINGING/type=1 matchId=$matchId from=${sign.fromUserId}")
                ThreadUtils.getMainHandler().post {
                    CallActivity.onRemoteUserConnected(matchId, sign.fromUserId)
                }
            }
            ImSignalProtos.CallSignalType.CALL_ACCEPT_VALUE -> {
                val matchId = when {
                    !TextUtils.isEmpty(sign.callId) -> sign.callId
                    sign.hasRoomName() && !TextUtils.isEmpty(sign.roomName) -> sign.roomName
                    else -> ""
                }
                eLogD(tAG, "对方接听 CALL_ACCEPT matchId=$matchId from=${sign.fromUserId}")
                ThreadUtils.getMainHandler().post {
                    CallActivity.onRemoteUserConnected(matchId, sign.fromUserId)
                }
            }
            ImSignalProtos.CallSignalType.CALL_MEMBER_JOINED_VALUE -> {
                val matchId = when {
                    !TextUtils.isEmpty(sign.callId) -> sign.callId
                    sign.hasRoomName() && !TextUtils.isEmpty(sign.roomName) -> sign.roomName
                    else -> ""
                }
                eLogD(tAG, "成员加入 CALL_MEMBER_JOINED matchId=$matchId from=${sign.fromUserId}")
                ThreadUtils.getMainHandler().post {
                    CallActivity.onRemoteUserConnected(matchId, sign.fromUserId)
                }
            }
            ImSignalProtos.CallSignalType.CALL_BUSY_VALUE -> {
                val matchId = when {
                    !TextUtils.isEmpty(sign.callId) -> sign.callId
                    sign.hasRoomName() && !TextUtils.isEmpty(sign.roomName) -> sign.roomName
                    else -> ""
                }
                eLogD(tAG, "对方忙线 CALL_BUSY matchId=$matchId from=${sign.fromUserId}")
                ThreadUtils.getMainHandler().post {
                    CallActivity.onRemoteParticipantLeft(matchId, sign.fromUserId, busy = true)
                }
            }
            ImSignalProtos.CallSignalType.CALL_CANCEL_VALUE -> {
                val matchId = when {
                    !TextUtils.isEmpty(sign.callId) -> sign.callId
                    sign.hasRoomName() && !TextUtils.isEmpty(sign.roomName) -> sign.roomName
                    else -> ""
                }
                eLogD(tAG, "主叫取消 CALL_CANCEL matchId=$matchId from=${sign.fromUserId}")
                ThreadUtils.getMainHandler().post {
                    CallActivity.finishIfRemoteCancelled(matchId, sign.fromUserId)
                }
            }
            ImSignalProtos.CallSignalType.CALL_HANGUP_VALUE -> {
                val matchId = when {
                    !TextUtils.isEmpty(sign.callId) -> sign.callId
                    sign.hasRoomName() && !TextUtils.isEmpty(sign.roomName) -> sign.roomName
                    else -> ""
                }
                eLogD(tAG, "对方挂断 CALL_HANGUP matchId=$matchId from=${sign.fromUserId}")
                ThreadUtils.getMainHandler().post {
                    // 群/多人：仅该成员离开；1v1 才整通结束
                    CallActivity.onRemoteParticipantLeft(matchId, sign.fromUserId)
                }
            }
            ImSignalProtos.CallSignalType.CALL_REJECT_VALUE -> {
                val matchId = when {
                    !TextUtils.isEmpty(sign.callId) -> sign.callId
                    sign.hasRoomName() && !TextUtils.isEmpty(sign.roomName) -> sign.roomName
                    else -> ""
                }
                eLogD(tAG, "对方拒绝 CALL_REJECT matchId=$matchId from=${sign.fromUserId}")
                ThreadUtils.getMainHandler().post {
                    CallActivity.onRemoteParticipantLeft(matchId, sign.fromUserId)
                }
            }
            ImSignalProtos.CallSignalType.CALL_MEMBER_LEFT_VALUE -> {
                val matchId = when {
                    !TextUtils.isEmpty(sign.callId) -> sign.callId
                    sign.hasRoomName() && !TextUtils.isEmpty(sign.roomName) -> sign.roomName
                    else -> ""
                }
                eLogD(tAG, "成员离开 CALL_MEMBER_LEFT matchId=$matchId from=${sign.fromUserId}")
                ThreadUtils.getMainHandler().post {
                    CallActivity.onRemoteParticipantLeft(matchId, sign.fromUserId)
                }
            }
            ImSignalProtos.CallSignalType.CALL_END_VALUE -> {
                val matchId = when {
                    !TextUtils.isEmpty(sign.callId) -> sign.callId
                    sign.hasRoomName() && !TextUtils.isEmpty(sign.roomName) -> sign.roomName
                    else -> ""
                }
                eLogD(tAG, "通话结束信令 CALL_END matchId=$matchId from=${sign.fromUserId}")
                ThreadUtils.getMainHandler().post {
                    CallActivity.finishIfRemoteEnded(matchId, sign.fromUserId)
                }
            }
            else -> {
                eLogD(tAG, "未处理的通话信令 type=${sign.type}")
            }
        }
    }

    override fun onKeyNegotiateResponse(transportCode: Short) {
        eLogD(tAG, "密钥协商传输层 code=$transportCode")
    }

    override fun isNetworkAvailable(): Boolean {
        val cm = UtilBroadApplication.cxt.getSystemService(Context.CONNECTIVITY_SERVICE)
        return if (cm is ConnectivityManager) {
            val info = cm.activeNetworkInfo
            info != null && info.isConnected
        } else false
    }

    override fun getReconnectInterval(): Int {
        return 0
    }

    override fun getConnectTimeout(): Int {
        return 0
    }

    override fun getForegroundHeartbeatInterval(): Int {
        return 0
    }

    override fun getBackgroundHeartbeatInterval(): Int {
        return 0
    }

    override fun getResendCount(): Int {
        return 0
    }

    override fun getResendInterval(): Int {
        return 0
    }

    override fun getUserId(): String? {
        return userId
    }

    override fun getBindToken(): String? {
        return token
    }

    override fun getRsaPublicKeyPem(): String? {
        if (rsaPublicKeyPem != null) {
            return rsaPublicKeyPem
        }
        synchronized(this) {
            if (rsaPublicKeyPem != null) {
                return rsaPublicKeyPem
            }
            var pem: String?
            try {
                UtilBroadApplication.cxt.assets.open(RSA_PUBLIC_ASSET).use { `in` ->
                    BufferedReader(InputStreamReader(`in`, StandardCharsets.UTF_8)).use { br ->
                        val sb = StringBuilder(512)
                        var line: String?
                        while ((br.readLine().also { line = it }) != null) {
                            sb.append(line).append('\n')
                        }
                        pem = sb.toString().trim { it <= ' ' }
                    }
                }
            } catch (e: Exception) {
                e.toLog(tAG + "_读取 assets/$RSA_PUBLIC_ASSET 失败，回退 im_lib demo 公钥")
                pem = ImCryptoKeys.demoPublicKeyPem()
            }
            if (pem?.isEmpty() == true) {
                pem = ImCryptoKeys.demoPublicKeyPem()
            }
            rsaPublicKeyPem = pem
            return rsaPublicKeyPem
        }
    }
}