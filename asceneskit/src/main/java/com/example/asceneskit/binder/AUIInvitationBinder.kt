package com.example.asceneskit.binder

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.example.asceneskit.AUIVoiceRoomService
import com.examole.asceneskit.R
import io.agora.auikit.model.AUIRoomContext
import io.agora.auikit.model.AUIUserInfo
import io.agora.auikit.model.AUIUserThumbnailInfo
import io.agora.auikit.service.IAUIInvitationService
import io.agora.auikit.service.IAUIMicSeatService
import io.agora.auikit.service.IAUIUserService
import io.agora.auikit.service.callback.AUIException
import io.agora.auikit.ui.action.AUIActionUserInfo
import io.agora.auikit.ui.action.AUIActionUserInfoList
import io.agora.auikit.ui.action.impI.AUIApplyDialog
import io.agora.auikit.ui.action.impI.AUIInvitationDialog
import io.agora.auikit.ui.action.listener.AUIApplyDialogEventListener
import io.agora.auikit.ui.action.listener.AUIInvitationDialogEventListener
import io.agora.auikit.ui.basic.AUIAlertDialog

class AUIInvitationBinder constructor(
    activity: FragmentActivity?,
    voiceService: AUIVoiceRoomService,
):
    IAUIBindable, IAUIInvitationService.AUIInvitationRespObserver,
    IAUIUserService.AUIUserRespObserver, IAUIMicSeatService.AUIMicSeatRespObserver {
    private var activity: FragmentActivity?
    private var mVoiceService: AUIVoiceRoomService
    private var invitationService:IAUIInvitationService
    private val applyList = mutableListOf<AUIUserInfo?>()
    private var applyDialog:AUIApplyDialog?=null
    private var invitationDialog:AUIInvitationDialog?=null

    private var mSeatMap = mutableMapOf<Int, String>()
    private var mMemberMap = mutableMapOf<String?, AUIUserInfo?>()
    private var currentMemberList:MutableList<AUIUserInfo?> = mutableListOf()
    private var userService:IAUIUserService
    private var applyUserList = ArrayList<AUIUserInfo?>()

    init {
        this.mVoiceService = voiceService
        this.invitationService = voiceService.invitationService
        this.activity = activity
        this.userService = voiceService.userService

        val roomOwner = voiceService.roomInfo?.owner
        val owner = AUIUserInfo()
        owner.userName = roomOwner?.userName.toString()
        owner.userId = roomOwner?.userId.toString()
        owner.userAvatar = roomOwner?.userAvatar.toString()
        mMemberMap[roomOwner?.userId] = owner
        mSeatMap[0] = owner.userId
    }

    override fun bind() {
        invitationService.registerRespObserver(this)
        mVoiceService.userService.registerRespObserver(this)
        mVoiceService.micSeatService.registerRespObserver(this)
    }

    override fun unBind() {
        invitationService.unRegisterRespObserver(this)
        mVoiceService.userService.unRegisterRespObserver(this)
        mVoiceService.micSeatService.unRegisterRespObserver(this)
    }

    // 显示申请列表
    fun showApplyDialog(){
        var dialog = applyDialog
        if (dialog == null){
            dialog = AUIApplyDialog()
            dialog.refreshApplyData(applyList.map { userInfo ->
                AUIActionUserInfo(
                    userInfo?.userId ?: "",
                    userInfo?.userName ?: "",
                    userInfo?.userAvatar ?: "",
                    userInfo?.micIndex ?: 0
                )
            })
            applyDialog = dialog
        }
        dialog.setApplyDialogListener(object : AUIApplyDialogEventListener {
            override fun onApplyItemClick(
                view: View,
                applyIndex: Int?,
                user: AUIActionUserInfo?,
                position: Int
            ) {
                if (user?.userId != null && applyIndex != null){


                    mVoiceService.invitationService.acceptApply(
                        user.userId,
                        applyIndex
                    ) {
                        if (it == null){
                            Log.d("apex","房主同意上麦申请 成功")
                            Toast.makeText(view.context, R.string.voice_room_apply_accept_success, Toast.LENGTH_SHORT).show()
                            
                            // 优化后的重复申请处理逻辑（可选的安全保障）
//                            handleDuplicateApplications(user.userId, applyIndex)
                        }else{
                            Toast.makeText(view.context, R.string.voice_room_apply_accept_failed, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        })
        activity?.supportFragmentManager?.let {
            applyDialog?.show(
                it, "AUIApplyDialog"
            )
        }
    }

    override fun onApplyListUpdate(userList: List<AUIUserInfo>?) {
        super.onApplyListUpdate(userList)
        applyUserList = ArrayList(userList ?: emptyList())
        applyList.clear()
        userList?.forEach { user ->
            val userInfo = mMemberMap[user.userId]
            userInfo?.micIndex = user.micIndex
            userInfo?.let {
                applyList.add(it)
            }
        }
        applyDialog?.refreshApplyData(applyList.map { userInfo ->
            AUIActionUserInfo(userInfo?.userId ?: "", userInfo?.userName ?: "", userInfo?.userAvatar ?: "", userInfo?.micIndex ?: 0)
        })
    }

    override fun onApplyAccepted(userId: String, seatIndex: Int) {
        super.onApplyAccepted(userId, seatIndex)
        if(userId == AUIRoomContext.shared().currentUserInfo.userId){
//            Toast.makeText(activity, R.string.voice_room_apply_accepted, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onApplyWillAccept(userId: String, seatIndex: Int): AUIException? {
        if (mSeatMap[seatIndex]?.isNotEmpty() == true) {
            return AUIException(AUIException.ERROR_CODE_SEAT_NOT_IDLE, "")
        }
        userService.getUserInfo(userId)?.let {
            mVoiceService.micSeatService.pickSeat(seatIndex, it) {}
        }
        return super.onApplyWillAccept(userId, seatIndex)
    }

    override fun onInviteWillAccept(userId: String, seatIndex: Int): AUIException? {
        if (mSeatMap[seatIndex]?.isNotEmpty() == true) {
            return AUIException(AUIException.ERROR_CODE_SEAT_NOT_IDLE, "")
        }
        userService.getUserInfo(userId)?.let {
            mVoiceService.micSeatService.pickSeat(seatIndex, it) {}
        }

        return super.onInviteWillAccept(userId, seatIndex)
    }


    //显示邀请列表
    fun showInvitationDialog(index:Int){
        val invitationInfo = AUIActionUserInfoList(currentMemberList.map { userInfo ->
            AUIActionUserInfo(userInfo?.userId ?: "", userInfo?.userName ?: "", userInfo?.userAvatar ?: "", userInfo?.micIndex ?: 0)
        })
        invitationInfo.invitedIndex = index
        Log.e("apex","onShowInvited $index $invitationInfo")
        invitationDialog = AUIInvitationDialog()
        invitationDialog?.apply {
            arguments = Bundle().apply {
                putSerializable(AUIInvitationDialog.KEY_ROOM_INVITED_BEAN, invitationInfo)
                putInt(AUIInvitationDialog.KEY_CURRENT_ITEM, 0)
            }
            setInvitationDialogListener(object : AUIInvitationDialogEventListener {
                override fun onInvitedItemClick(view: View, invitedIndex: Int, user: AUIActionUserInfo?) {
                    if (user != null){
                        mVoiceService.invitationService.sendInvitation(
                            user.userId,
                            invitedIndex
                        ) {
                            if (it == null) {
                                Toast.makeText(context, R.string.voice_room_invite_send_success, Toast.LENGTH_SHORT).show()
                                dismiss()
                            } else {
                                Toast.makeText(context, R.string.voice_room_invite_send_failed, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            })
        }
        activity?.supportFragmentManager?.let {
            invitationDialog?.show(
                it, "AUIInvitationDialog"
            )
        }
    }

    override fun onReceiveInvitation(userId: String, micIndex: Int) {
        if(userId != mVoiceService.micSeatService.roomContext.currentUserInfo.userId){
            return
        }
        // 收到上麦邀请
        activity?.let {
            AUIAlertDialog(it).apply {

                setTitle(
                    if (micIndex == -1){
                        context.getString(R.string.voice_room_receive_invitation_content).format(mVoiceService.roomInfo?.owner?.userName,)
                    }else{
                        context.getString(R.string.voice_room_receive_invitation_title).format(
                            mVoiceService.roomInfo?.owner?.userName,
                            micIndex+1
                        )
//                        context.getString(R.string.voice_room_receive_invitation_title,micIndex+1)
                    }
                )
                setMessage("上麦后可开启语音说话")
                setPositiveButton(context.getString(R.string.voice_room_confirm)) {
                    mVoiceService.invitationService.acceptInvitation(userId,micIndex
                    ) { e->
                        if (e == null){
                            Log.d("apex","同意上麦邀请成功")
                        }
                    }
                    dismiss()
                }
                setNegativeButton( context.getString(R.string.voice_room_reject)) {
                    mVoiceService.invitationService.rejectInvitation(userId){ e ->
                        if (e == null){
                            Log.d("apex","拒绝上麦邀请成功")
                        }
                    }
                    dismiss()
                }
                show()
            }
        }
    }


    /** IAUiUserService.AUiUserRespDelegate */
    override fun onRoomUserEnter(roomId: String, userInfo: AUIUserInfo) {
        Log.d("apex","onRoomUserEnter ${userInfo.userId}")
        val auiUserInfo = userService.getUserInfo(userInfo.userId)
        mMemberMap[userInfo.userId] = auiUserInfo
        filterCurrentMember()
    }

    override fun onRoomUserLeave(roomId: String, userInfo: AUIUserInfo) {
        mMemberMap.remove(userInfo.userId)
        filterCurrentMember()
    }

    override fun onRoomUserUpdate(roomId: String, userInfo: AUIUserInfo) {
        mMemberMap[userInfo.userId] = userInfo
        updateApplyUserInfo()
        filterCurrentMember()
    }

    override fun onRoomUserSnapshot(roomId: String, userList: MutableList<AUIUserInfo>?) {
        userList?.forEach { userInfo ->
            Log.e("apex","onRoomUserSnapshot ${userInfo.userName} - ${userInfo.userAvatar}")
            mMemberMap[userInfo.userId] = userInfo
        }
        filterCurrentMember()
    }

    /** IAUiMicSeatService.AUiMicSeatRespDelegate */
    override fun onAnchorEnterSeat(seatIndex: Int, userInfo: AUIUserThumbnailInfo) {
        mSeatMap[seatIndex] = userInfo.userId
        filterCurrentMember()
    }

    override fun onAnchorLeaveSeat(seatIndex: Int, userInfo: AUIUserThumbnailInfo) {
        if (mSeatMap[seatIndex].equals(userInfo.userId)) {
            mSeatMap.remove(seatIndex)
        }
        invitationService.cleanUserInfo(userInfo.userId){}
        filterCurrentMember()
    }

    private fun filterCurrentMember(){
        // 过滤已在麦位的用户
        currentMemberList.clear()
        mMemberMap.values.toList().forEach {  user ->
            // 在麦位数据和所有成员数据中 查找共有的uid
            val uid = mSeatMap.entries.find { it.value == user?.userId }?.value
            if (user?.userId != uid && user?.userId != mVoiceService.roomInfo?.owner?.userId){
                currentMemberList.add(user)
            }
        }
        invitationDialog?.refreshInvitationData(currentMemberList.map { userInfo ->
            AUIActionUserInfo(userInfo?.userId ?: "", userInfo?.userName ?: "", userInfo?.userAvatar ?: "", userInfo?.micIndex ?: 0)
        })
    }

    private fun updateApplyUserInfo(){
        applyList.clear()
        applyUserList.forEach { it1 ->
            val userInfo = mMemberMap[it1?.userId]
            userInfo?.micIndex = (it1?.micIndex)?:-1
            userInfo?.let {
                applyList.add(it)
            }
        }
        applyDialog?.refreshApplyData(applyList.map { userInfo ->
            AUIActionUserInfo(userInfo?.userId ?: "", userInfo?.userName ?: "", userInfo?.userAvatar ?: "", userInfo?.micIndex ?: 0)
        })
    }

    /**
     * 处理重复申请的优化方法
     * 当房主同意某个用户的上麦申请后，自动拒绝其他申请同一麦位的用户
     * 
     * @param acceptedUserId 已被同意的用户ID
     * @param seatIndex 麦位索引
     */
    private fun handleDuplicateApplications(acceptedUserId: String, seatIndex: Int) {
        // 查找申请同一麦位的其他用户
        val duplicateApplications = applyList.filter { user ->
            user != null && 
            user.userId != acceptedUserId && 
            user.micIndex == seatIndex
        }.mapNotNull { it?.userId }
        
        if (duplicateApplications.isEmpty()) {
            Log.d("DuplicateApply", "没有发现重复申请同一麦位的用户")
            return
        }
        
        val duplicateCount = duplicateApplications.size
        Log.d("DuplicateApply", "发现 $duplicateCount 个重复申请麦位 $seatIndex 的用户")
        
        // 安全检查：如果重复申请过多，记录警告并限制处理数量
        if (duplicateCount > 20) {
            Log.w("DuplicateApply", "重复申请数量过多($duplicateCount)，可能存在异常情况")
            // 可以选择只处理前20个，或者完全跳过处理
            // return
        }
        
        // 使用批量处理和限流机制
        batchRejectApplications(duplicateApplications, seatIndex)
    }
    
    /**
     * 批量拒绝申请，使用限流机制避免并发请求风暴
     * 
     * @param userIds 需要拒绝的用户ID列表
     * @param seatIndex 麦位索引
     */
    private fun batchRejectApplications(userIds: List<String>, seatIndex: Int) {
        var successCount = 0
        var failureCount = 0
        val totalCount = userIds.size
        
        // 限流处理：每次最多处理5个请求，间隔100ms
        val batchSize = 5
        val delayMs = 100L
        
        userIds.chunked(batchSize).forEachIndexed { batchIndex, batch ->
            // 延迟处理，避免并发请求过多
            Handler(Looper.getMainLooper()).postDelayed({
                batch.forEach { userId ->
                    mVoiceService.invitationService.rejectApply(userId) { error ->
                        if (error == null) {
                            successCount++
                            Log.d("DuplicateApply", "成功拒绝用户 $userId 的重复申请 ($successCount/$totalCount)")
                        } else {
                            failureCount++
                            Log.w("DuplicateApply", "拒绝用户 $userId 的重复申请失败: ${error.message} ($failureCount 失败)")
                        }
                        
                        // 当所有请求完成时，输出统计信息
                        if (successCount + failureCount == totalCount) {
                            Log.i("DuplicateApply", "批量拒绝完成 - 成功: $successCount, 失败: $failureCount, 总计: $totalCount")
                        }
                    }
                }
            }, batchIndex * delayMs)
        }
    }

}