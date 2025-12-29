package io.agora.asceneskit.voice.binder

import android.util.Log
import android.view.View
import android.widget.Toast
import io.agora.asceneskit.R
import io.agora.asceneskit.voice.AUIVoiceRoomService
import io.agora.auikit.model.AUIMicSeatInfo
import io.agora.auikit.model.AUIRoomContext
import io.agora.auikit.model.AUIUserInfo
import io.agora.auikit.model.AUIUserThumbnailInfo
import io.agora.auikit.service.IAUIInvitationService
import io.agora.auikit.service.IAUIMicSeatService
import io.agora.auikit.service.IAUIUserService
import io.agora.auikit.ui.chatBottomBar.IAUIChatBottomBarView
import io.agora.auikit.ui.chatBottomBar.listener.AUIMenuItemClickListener
import io.agora.auikit.ui.chatBottomBar.listener.AUISoftKeyboardHeightChangeListener
import io.agora.auikit.ui.chatList.AUIChatInfo
import io.agora.auikit.ui.chatList.IAUIChatListView
import io.agora.auikit.ui.micseats.IMicSeatsView
import io.agora.auikit.utils.FastClickTools

class AUIChatBottomBarBinder constructor(
    private val voiceService: AUIVoiceRoomService,
    private val chatBottomBarView: IAUIChatBottomBarView,
    private val chatList: IAUIChatListView,
    private val micSeatsView: IMicSeatsView,
    private val event: AUIChatBottomBarEventDelegate?
) : IAUIBindable,
    AUIMenuItemClickListener,
    IAUIUserService.AUIUserRespObserver,
    IAUIMicSeatService.AUIMicSeatRespObserver,
    IAUIInvitationService.AUIInvitationRespObserver {

    private val roomInfo = voiceService.roomInfo ?: throw RuntimeException("RoomInfo is null!")
    private val userService = voiceService.userService
    private val micSeatsService = voiceService.micSeatService
    private val invitationService = voiceService.invitationService
    private val imManagerService = voiceService.imManagerService
    private val chatManager = voiceService.chatManager

    private var listener: AUISoftKeyboardHeightChangeListener? = null
    private var roomContext = AUIRoomContext.shared()
    private var mLocalMute = true
    private val mVolumeMap: HashMap<String, Int> = HashMap()
    private val isRoomOwner = roomContext.isRoomOwner(roomInfo.roomId)

    init {
        Log.e("AUIChatBottomBarBinder","init")
        val owner = roomContext?.getRoomOwner(roomInfo.roomId)
        owner?.let {
            mVolumeMap[it] = 0
        }

        // 检查当前用户是否在麦位上
        val localUserId = roomContext?.currentUserInfo?.userId ?: ""
        val currentUserSeatIndex = micSeatsService.getMicSeatIndex(localUserId)
        val isUserOnSeat = currentUserSeatIndex >= 0

        // 初始化mLocalMute状态：如果用户在麦位上，根据实际状态设置；否则默认为true
        if (isUserOnSeat) {
            val micSeatInfo = micSeatsService.getMicSeatInfo(currentUserSeatIndex)
            val userInfo = userService.getUserInfo(localUserId)
            // 综合考虑房主禁麦状态和用户自己的静音状态
            mLocalMute = (micSeatInfo?.muteAudio == true) || (userInfo?.muteAudio == 1)
            // 同步设置底部按钮状态
            chatBottomBarView.setEnableMic(mLocalMute)
        } else {
            // 用户不在麦位上，默认为静音状态
            mLocalMute = true
        }

        if (!isRoomOwner) {
            // 非房主用户：如果在麦位上则显示麦位操作按钮，否则隐藏
            chatBottomBarView.setShowMic(isUserOnSeat)
        }else{
            chatBottomBarView.setShowMoreStatus(isRoomOwner,false)
            chatBottomBarView.setShowMic(true)
        }

        // 如果用户在麦位上，更新音量映射
        if (isUserOnSeat) {
            mVolumeMap[localUserId] = currentUserSeatIndex
        }
    }

    override fun bind() {
        userService.registerRespObserver(this)
        micSeatsService.registerRespObserver(this)
        invitationService.registerRespObserver(this)

        chatBottomBarView.setMenuItemClickListener(this)
        chatBottomBarView.setSoftKeyListener()
    }

    override fun unBind() {
        userService.unRegisterRespObserver(this)
        micSeatsService.unRegisterRespObserver(this)
        invitationService.unRegisterRespObserver(this)

        chatBottomBarView.setMenuItemClickListener(null)
    }

    fun setMoreStatus(isOwner: Boolean, status: Boolean) {
        chatBottomBarView.setShowMoreStatus(isOwner, status)
    }

    override fun setSoftKeyBoardHeightChangedListener(listener: AUISoftKeyboardHeightChangeListener) {
        this.listener = listener
    }

    fun getSoftKeyboardHeightChangeListener(): AUISoftKeyboardHeightChangeListener? {
        return listener
    }

    override fun onChatExtendMenuItemClick(itemId: Int, view: View?) {
        when (itemId) {
            R.id.voice_extend_item_more -> {
                //自定义预留
                if (view?.let { FastClickTools.isFastClick(it) } == true) return
                chatBottomBarView.setShowMoreStatus(isRoomOwner, false)
                event?.onClickMore(view)
            }

            R.id.voice_extend_item_mic -> {
                //点击下方麦克风
                val newMuteState = !mLocalMute

                // 权限检查：如果用户想要取消静音，需要检查是否被房主静音
                if (!newMuteState && !isRoomOwner) {
                    // 用户想要取消静音，且不是房主，需要检查麦位状态
                    val localUserId = roomContext?.currentUserInfo?.userId ?: ""
                    val currentUserSeatIndex = micSeatsService.getMicSeatIndex(localUserId)

                    if (currentUserSeatIndex >= 0) {
                        // 用户在麦位上，检查麦位是否被房主静音
                        val micSeatInfo = micSeatsService.getMicSeatInfo(currentUserSeatIndex)
                        if (micSeatInfo?.muteAudio == true) {
                            // 麦位被房主静音，不允许用户通过底部按钮取消静音
                            Toast.makeText(
                                roomContext.requireCommonConfig().context,
                                "当前麦位被房主静音，无法取消静音",
                                Toast.LENGTH_SHORT
                            ).show()
                            return
                        }
                    }
                }

                mLocalMute = newMuteState
                userService.muteUserAudio(mLocalMute, null)
                event?.onClickMic(view)
            }

            R.id.voice_extend_item_gift -> {
                if (view?.let { FastClickTools.isFastClick(it) } == true) return
                //点击下方礼物按钮 弹出送礼菜单
                event?.onClickGift(view)
            }

            R.id.voice_extend_item_like -> {
                //点击下方点赞按钮
                event?.onClickLike(view)
            }
        }
    }

    override fun onSendMessage(content: String?) {
        content?: return
        imManagerService.sendMessage(
            roomInfo.roomId,
            content
        ) { _, error ->
            if(error == null){
                chatList.refreshSelectLast(chatManager.getMsgList().map {
                    AUIChatInfo(
                        it.user?.userId ?: "", it.user?.userName ?: "",
                        it.content, it.joined
                    )
                })
            }
        }
    }

    interface AUIChatBottomBarEventDelegate {
        fun onClickGift(view: View?) {}
        fun onClickLike(view: View?) {}
        fun onClickMore(view: View?) {}
        fun onClickMic(view: View?) {}
    }

    override fun onUserAudioMute(userId: String, mute: Boolean) {
        val localUserId = roomContext?.currentUserInfo?.userId ?: ""
        if (localUserId != userId) {
            return
        }
        var localUserSeat: AUIMicSeatInfo? = null
        for (i in 0..7) {
            val seatInfo = micSeatsService.getMicSeatInfo(i)
            if (seatInfo?.user != null && seatInfo.user?.userId == userId) {
                localUserSeat = seatInfo
                break
            }
        }
        if (localUserSeat != null) {
            // 移除错误的房主禁麦提示，因为 onUserAudioMute 在用户自己静音时也会被触发
            // 房主禁麦的提示应该在 onSeatAudioMute 回调中处理
            val userInfo = userService.getUserInfo(userId)
            val mMute = localUserSeat.muteAudio || (userInfo?.muteAudio == 1)
            mVolumeMap[userId]?.let { setLocalMute(it, mMute) }
        }
    }

    /** IAUiMicSeatService.AUiMicSeatRespDelegate */
    override fun onAnchorEnterSeat(seatIndex: Int, userInfo: AUIUserThumbnailInfo) {
        val localUserId = roomContext?.currentUserInfo?.userId ?: ""
        mVolumeMap[userInfo.userId] = seatIndex
        if (userInfo.userId == localUserId) { // 本地用户上麦
            chatBottomBarView.setShowMic(true)
            voiceService.setupLocalStreamOn(true)
            val micSeatInfo = micSeatsService.getMicSeatInfo(seatIndex)
            val mUserInfo = userService.getUserInfo(localUserId)
            val isMute = micSeatInfo?.muteAudio ?: false || (mUserInfo?.muteAudio == 1)
            setLocalMute(seatIndex, isMute)
        }
    }

    override fun onAnchorLeaveSeat(seatIndex: Int, userInfo: AUIUserThumbnailInfo) {
        val localUserId = roomContext?.currentUserInfo?.userId ?: ""
        mVolumeMap.remove(userInfo.userId)
        if (userInfo.userId == localUserId) { // 本地用户下麦
            chatBottomBarView.setShowMic(false)
            voiceService.setupLocalStreamOn(false)
        }
    }

    override fun onSeatAudioMute(seatIndex: Int, isMute: Boolean) {
        // 麦位被禁用麦克风
        // 远端用户：关闭对该麦位的音频流订阅
        // 本地用户：保险起见关闭本地用户的麦克风音量
        val micSeatInfo = micSeatsService.getMicSeatInfo(seatIndex)
        val seatUserId = micSeatInfo?.user?.userId
        if (seatUserId.isNullOrEmpty()) {
            return
        }
        val localUserId = roomContext?.currentUserInfo?.userId ?: ""
        if (seatUserId == localUserId) {
            // 只有当房主禁麦时才显示提示（isMute为true表示被禁麦）
            if (isMute) {
                Toast.makeText(
                    roomContext.requireCommonConfig().context,
                    roomContext.requireCommonConfig().context.getString(io.agora.asceneskit.R.string.voice_room_owner_mute_seat),
                    Toast.LENGTH_SHORT
                ).show()
            }
            userService.muteUserAudio(isMute, null)
        } else {
            voiceService.setupRemoteAudioMute(seatUserId, isMute)
        }
        micSeatsView.stopRippleAnimation(seatIndex)
    }


    private fun setLocalMute(seatIndex: Int, isMute: Boolean) {
        Log.d("local_mic", "update rtc mute state: $isMute")
        mLocalMute = isMute
        voiceService.setupLocalAudioMute(isMute)
        chatBottomBarView.setEnableMic(isMute)

        val seatView = micSeatsView.micSeatItemViewList[seatIndex]
        seatView?.let {
            seatView.setAudioMuteVisibility(if (isMute) View.VISIBLE else View.GONE)
        }
    }

    override fun onApplyListUpdate(userList: List<AUIUserInfo>?) {
        val firstOrNull = userList?.firstOrNull { it.isRead == 0 }
        val showStatus=if (firstOrNull!=null){
            true
        }else{
            false
        }
        chatBottomBarView.setShowMoreStatus(isRoomOwner, showStatus)
    }


}