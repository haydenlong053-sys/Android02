package com.cboda.agora.binder

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.Toast
import com.cboda.agora.ui.ui.basic.AUIAlertBroadDialog
import com.cboda.agora.ui.ui.basic.AUIBottomBroadDialog
import com.cboda.agora.ui.ui.member.listener.AUIRoomMembersActionBroadListener
import com.cboda.agora.ui.ui.member.MemberBroadInfo
import com.cboda.agora.AUIVoiceRoomBroadService
import com.cboda.agora.R
import com.cboda.agora.impl.AUIRightMeetingTopBroadView
import com.cboda.agora.impl.MeetingMemberListBroadView
import com.cboda.agora.service.model.AUIRoomBroadContext
import com.cboda.agora.service.model.AUIRoomBroadInfo
import com.cboda.agora.service.model.AUIUserBroadInfo
import com.cboda.agora.service.model.AUIUserThumbnailBroadInfo
import com.cboda.agora.service.service.IAUIMicSeatBroadService
import com.cboda.agora.service.service.IAUIUserBroadService

class AUIRoomMembersBinder constructor(
    context: Context,
    memberView: AUIRightMeetingTopBroadView,
    voiceService: AUIVoiceRoomBroadService,
    listener: AUIRoomMemberEvent?,
) : IAUIBroadBindable,
    IAUIUserBroadService.AUIUserRespObserver,
    AUIRoomMembersActionBroadListener,
    IAUIMicSeatBroadService.AUIMicSeatRespObserver {

    private var userService: IAUIUserBroadService?
    private var micSeats: IAUIMicSeatBroadService?
    private var memberView: AUIRightMeetingTopBroadView?
    private var roomInfo: AUIRoomBroadInfo?
    private var context: Context? = null
    private var roomContext: AUIRoomBroadContext
    private var mMemberMap = mutableMapOf<String?, AUIUserBroadInfo?>()
    private var mSeatMap = mutableMapOf<Int, String?>()
    private var isOwner: Boolean = false
    private var listener: AUIRoomMemberEvent?
    private var dialogMemberView: MeetingMemberListBroadView? = null


    init {
        this.userService = voiceService.userService
        this.micSeats = voiceService.micSeatService
        this.roomInfo = voiceService.roomInfo
        this.roomContext = AUIRoomBroadContext.shared()
        this.memberView = memberView
        this.context = context
        this.listener = listener
        val roomOwner = roomInfo?.owner
        isOwner = roomContext.currentUserInfo.userId == roomOwner?.userId

        val owner = AUIUserBroadInfo()
        owner.userName = roomOwner?.userName.toString()
        owner.userId = roomOwner?.userId.toString()
        owner.userAvatar = roomOwner?.userAvatar.toString()
        mMemberMap[roomOwner?.userId] = owner
        mSeatMap[0] = roomOwner?.userId
    }

    override fun bind() {
        micSeats?.registerRespObserver(this)
        userService?.registerRespObserver(this)
        memberView?.setMemberActionListener(this)
    }

    override fun unBind() {
        userService?.unRegisterRespObserver(this)
        micSeats?.unRegisterRespObserver(this)
        memberView?.setMemberActionListener(null)
    }

    override fun onMemberRankClickListener(view: View) {
        listener?.showRoomOperate()


    }

    override fun onMemberRightShutDownClickListener(view: View) {
        listener?.onCloseClickListener(view)
    }

    override fun onMemberRightUserMoreClickListener(view: View) {
        context?.let { showUserListDialog(it) }
    }

    override fun onKickOutUser(view: View, user: MemberBroadInfo?) {
        user?.let { listener?.kickUser(it) }
    }

    /**
     * 显示用户列表
     */
    private fun showUserListDialog(context: Context) {
        dialogMemberView = MeetingMemberListBroadView(context)
        dialogMemberView?.setMembers(mMemberMap.values.toList().map {
            MemberBroadInfo(it?.userId ?: "", it?.userName ?: "", it?.userAvatar ?: "")
        }, mSeatMap)
        dialogMemberView?.setIsOwner(isOwner, roomInfo?.owner?.userId)
        dialogMemberView?.setMemberActionListener(object : MeetingMemberListBroadView.ActionListener {
            override fun onKickClick(view: View, position: Int, user: MemberBroadInfo?) {
                try {
                    val userId = user?.userId ?: return
                    if (isOwner) {
                        // 踢人（非麦位，是在用户列表里踢人，此处应该是userService里的一个方法）
                        showKickSureDialog(context){
                            userService?.kickUser(userId){ error->
                                if (error == null) {
                                    mMemberMap.remove(userId)
                                    updateMemberView()
                                    Log.d("AUIRoomMembersBinder", "onKickClick suc")
                                    Toast.makeText(context, R.string.voice_room_kick_user_success, Toast.LENGTH_SHORT).show()
                                    onKickOutUser(view,user)
                                } else {
                                    Log.d("AUIRoomMembersBinder", "onKickClick fail ${error.message}")
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.d("AUIRoomMembersBinder", "Conversion exception")
                }
            }
        })

        dialogMemberView.let {
            AUIBottomBroadDialog(context).apply {
                setBackground(null)
                it?.let { it1 -> setCustomView(it1) }
                show()
            }
        }
    }

    private fun showKickSureDialog(context: Context, confirm: ()->Unit) {
        AUIAlertBroadDialog(context).apply {
            setTitle(R.string.voice_room_tip)
            setMessage(context.getString(R.string.voice_room_kick_sure_tip))
            setPositiveButton(context.getString(R.string.voice_room_confirm)){
                dismiss()
                confirm.invoke()
            }
            setTitleCloseButton{
                dismiss()
            }
            show()
        }
    }


    /** IAUiUserService.AUiUserRespDelegate */
    override fun onRoomUserEnter(roomId: String, userInfo: AUIUserBroadInfo) {
        val auiUserInfo = userService?.getUserInfo(userInfo.userId)
        mMemberMap[userInfo.userId] = auiUserInfo
        updateMemberView()
    }

    override fun onRoomUserLeave(roomId: String, userInfo: AUIUserBroadInfo) {
        mMemberMap.remove(userInfo.userId)
        updateMemberView()
    }

    override fun onRoomUserUpdate(roomId: String, userInfo: AUIUserBroadInfo) {
        mMemberMap[userInfo.userId] = userInfo
        updateMemberView()
    }

    override fun onRoomUserSnapshot(roomId: String, userList: MutableList<AUIUserBroadInfo>?) {
        userList?.forEach { userInfo ->
            mMemberMap[userInfo.userId] = userInfo
        }
        memberView?.setMemberData(mMemberMap.values.toList().map {
            MemberBroadInfo(it?.userId ?: "", it?.userName ?: "", it?.userAvatar ?: "")
        })
    }


    /** IAUiMicSeatService.AUiMicSeatRespDelegate */
    override fun onAnchorEnterSeat(seatIndex: Int, userInfo: AUIUserThumbnailBroadInfo) {
        Log.e("apex", "onAnchorEnterSeat $seatIndex ${userInfo.userName}")
        mSeatMap[seatIndex] = userInfo.userId
        updateMemberView()
    }

    override fun onAnchorLeaveSeat(seatIndex: Int, userInfo: AUIUserThumbnailBroadInfo) {
        if (mSeatMap[seatIndex].equals(userInfo.userId)) {
            mSeatMap.remove(seatIndex)
            updateMemberView()
        }
    }

    override fun onLocalUserKickedOut(roomId: String) {
        super.onLocalUserKickedOut(roomId)
        listener?.onLocalUserKickedOut()
    }

    private fun updateMemberView() {
        memberView?.setMemberData(mMemberMap.values.toList().map {
            MemberBroadInfo(it?.userId ?: "", it?.userName ?: "", it?.userAvatar ?: "")
        })
        dialogMemberView?.setMembers(mMemberMap.values.toList().map {
            MemberBroadInfo(it?.userId ?: "", it?.userName ?: "", it?.userAvatar ?: "")
        }, mSeatMap)
    }

    interface AUIRoomMemberEvent {
        fun onCloseClickListener(view: View) {}

        fun onLocalUserKickedOut(){}

        open  fun showRoomOperate(){}


        open  fun kickUser(member: MemberBroadInfo?){}
    }

}