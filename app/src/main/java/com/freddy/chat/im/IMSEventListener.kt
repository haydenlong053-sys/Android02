package com.freddy.chat.im

import android.content.Context
import android.net.ConnectivityManager
import android.text.TextUtils
import com.cboda.baselibrary.app.UtilBroadApplication
import com.cboda.baselibrary.utils.eLogD
import com.cboda.baselibrary.utils.toLog
import com.freddy.chat.im.othbusi.OthBusiSigHandlers
import com.freddy.im.bean.AppMessage
import com.freddy.im.crypto.ImCryptoKeys
import com.freddy.im.db.ImLocalStore
import com.freddy.im.db.repository.ImMessageRepository.OnMessageUpdatedCallback
import com.freddy.im.http.model.im.SessionItem
import com.freddy.im.listener.OnEventListener
import com.freddy.im.message.ImGroupChangeManager
import com.freddy.im.message.ImGroupMemberChangeManager
import com.freddy.im.message.ImMessageManager
import com.freddy.im.signal.ImSignalProtos
import com.freddy.im.signal.ImSignalProtos.CountMsgReadSig
import com.freddy.im.signal.ImSignalProtos.GroupChangeSig
import com.freddy.im.signal.ImSignalProtos.MemberChangeSig
import com.freddy.im.signal.ImSignalProtos.MessageReadSig
import com.freddy.im.signal.ImSignalProtos.MessageRepealSig
import com.freddy.im.signal.ImSignalProtos.NewMsgSignal
import com.freddy.im.signal.ImSignalProtos.NewMsgSignal.MsgSig
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
    private val tAG = "IMSEventListener"

    private val RSA_PUBLIC_ASSET = "public_key.pem"


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
            } catch (e: Exception) {
                e.toLog(tAG + "_解析 NewMsgSignal 失败")
            }
        }

        // 群变更：0x0101 GroupChangeSig.groupList → 拉群详情 → 入库 → 通知 UI
        else if (push.header.cmd == ImSignalProtos.Cmd.GROUP_CHANGE_VALUE && push.hasBody()) {
            try {
                val sig = GroupChangeSig.parseFrom(
                    push.body.toByteArray()
                )
                val groupList: MutableList<String?> = sig.groupListList
                eLogD(tAG, "GROUP_CHANGE groupList size=" + (groupList.size))
                ImGroupChangeManager.getInstance().handleGroupChange(groupList) { sessions: MutableList<SessionItem?>? -> MessageProcessor.getInstance().notifySessionsUpdated(sessions) }
            } catch (e: java.lang.Exception) {
                eLogD(tAG, "解析 GroupChangeSig 失败", e)
            }
        }


        // 群成员变更：0x0102 MemberChangeSig → 拉成员 / 软删除 → 入库 → 通知消息列表 UI
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
            } catch (e: Exception) {
                eLogD(tAG, "解析 MemberChangeSig 失败", e)
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
        //语音通话
        else if (push.header.cmd == ImSignalProtos.Cmd.CALL_SIGNAL_VALUE && push.hasBody()) {
            val sign = ImSignalProtos.CallSignal.parseFrom(push.body.toByteArray())
            eLogD(
                tAG, ("callData_groupId:" + sign.groupId
                        + " ,type=" + sign.type
                        + " ,roomName=" + sign.roomName
                        + " ,callId=" + sign.callId
                        + " ,mode=" + sign.mode)
            )
        }

        // 0x0501 其他业务信令：OthBusiSig 按 code 分发
        else if (push.header.cmd == ImSignalProtos.Cmd.OTHER_BUSI_VALUE && push.hasBody()) {
            OthBusiSigHandlers.get().dispatch(push.body.toByteArray())
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