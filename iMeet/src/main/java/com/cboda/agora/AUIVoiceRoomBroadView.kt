package com.cboda.agora

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.fragment.app.FragmentActivity
import com.cboda.agora.ui.chatBottomBar.listener.AUISoftKeyboardHeightChangeBroadListener
import com.cboda.agora.ui.micseats.MicSeatBroadOption
import com.cboda.agora.ui.micseats.MicSeatBroadType
import com.cboda.agora.ui.ui.basic.AUIAlertBroadDialog
import com.cboda.agora.ui.ui.chatBottomBar.impl.AUIKeyboardStatusBroadWatcher
import com.cboda.agora.ui.ui.member.MemberBroadInfo
import com.cboda.agora.binder.AUIChatBottomBarBinder
import com.cboda.agora.binder.AUIChatListBinder
import com.cboda.agora.binder.AUIGiftBarrageBinder
import com.cboda.agora.binder.AUIInvitationBinder
import com.cboda.agora.binder.AUIMicSeatsBroadBinder
import com.cboda.agora.binder.AUIRoomInfoBinder
import com.cboda.agora.binder.AUIRoomMembersBinder
import com.cboda.agora.binder.IAUIBroadBindable
import com.cboda.agora.databinding.VoiceRoomBroadViewBinding
import com.cboda.agora.dialog.VoiceMoreItemBean
import com.cboda.agora.dialog.VoiceRoomMoreDialog
import com.cboda.agora.service.model.AUIRoomBroadContext
import com.cboda.agora.service.model.AUIRoomBroadInfo
import com.cboda.agora.service.service.IAUIIMManagerBroadService
import com.cboda.agora.service.service.IAUIMicSeatBroadService
import com.cboda.agora.service.service.imp.AUIIMManagerServiceBroadImpl
import com.cboda.agora.service.utils.AUIBroadLogger
import com.cboda.agora.service.utils.ThreadBroadManager

class AUIVoiceRoomBroadView : FrameLayout,
    AUIVoiceRoomBroadObserver,
    IAUIMicSeatBroadService.AUIMicSeatRespObserver {
        private  val TAG="AUIVoiceRoomView"
    private val mRoomViewBinding = VoiceRoomBroadViewBinding.inflate(LayoutInflater.from(context))
    private val mBinders = mutableListOf<IAUIBroadBindable>()
    private var mVoiceService: AUIVoiceRoomBroadService? = null
    private var listener: AUISoftKeyboardHeightChangeBroadListener? = null
    private var activity:FragmentActivity?= null
    private var moreDialog: VoiceRoomMoreDialog?=null
    private var micType: MicSeatBroadType?=null
    private var mOnClickShutDown: (() -> Unit)? = null
    private var mOnClickShare: (() -> Unit)? = null
    private var mOnClickMore: (() -> Unit)? = null
    private var mOnKickUser: ((userId:String) -> Unit)? = null

    private var mOnClickAllMic: ((openMic: Boolean) -> Unit)? = null
    private var mOnClickAllMute: ((allMute: Boolean) -> Unit)? = null
    private var mPIPEvent: (() -> Unit)? = null
    private var mOnRoomDestroyEvent: (() -> Unit)? = null
    private var imManagerService: IAUIIMManagerBroadService?=null
    private var invitationBinder: AUIInvitationBinder?=null
    private var auiGiftBarrageBinder : AUIGiftBarrageBinder?=null
    private val mDestroyDialog by lazy {
        AUIAlertBroadDialog(context).apply {
            setTitle("房间已销毁")
            setMessage("请返回房间列表 ")
            setPositiveButton("我知道了") {
                mOnRoomDestroyEvent?.invoke()
                dismiss()
            }
            setCancelable(false)
        }
    }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        addView(mRoomViewBinding.root)
    }

    fun bindService(service: AUIVoiceRoomBroadService) {
        Log.d(TAG,"bindService")
        mVoiceService = service
        imManagerService = service.imManagerService as AUIIMManagerServiceBroadImpl
        service.observableHelper.subscribeEvent(this)
        service.micSeatService.registerRespObserver(this)

        val roomInfo = service.roomInfo ?: throw RuntimeException("RoomInfo is null")
        setUpRoomStyle(roomInfo)
        viewBinderConnected(service, roomInfo)
        getSoftKeyboardHeight()
    }

    private fun viewBinderConnected(service:AUIVoiceRoomBroadService, roomInfo: AUIRoomBroadInfo){
        /** 获取礼物列表 初始化礼物Binder */
//        service.giftService.getGiftsFromService(object : AUIGiftListBroadCallback{
//            override fun onResult(error: AUIBroadException?, giftList: List<AUIGiftTabBroadEntity>) {
//                auiGiftBarrageBinder = AUIGiftBarrageBinder(
//                    activity,
//                    mRoomViewBinding.giftView,
//                    giftList,
//                    service.giftService,
//                    service.chatManager
//                )
//                auiGiftBarrageBinder?.let {
//                    it.bind()
//                    mBinders.add(it)
//                }
//            }
//        })


        service.chatManager.saveWelcomeMsg(context.getString(R.string.voice_room_welcome))

        val chatListBinder = AUIChatListBinder(
            roomInfo,
            mRoomViewBinding.chatListView,
            mRoomViewBinding.chatBottomBar,
            service.chatManager,
            service.imManagerService,
        )
        chatListBinder.let {
            it.bind()
            mBinders.add(it)
        }

        val roomInfoBinder = AUIRoomInfoBinder(
            mRoomViewBinding.leftView,
            roomInfo,
            listener = object : AUIRoomInfoBinder.AUIRoomInfoEvent {
                override fun onBackClickListener(view: View) {
                    AUIBroadLogger.logger().d("AUIVoiceRoomView", "AUIRoomInfoBinder onBackClick ...")
//                    mOnClickShutDown?.invoke()
                    mPIPEvent?.invoke()
                }
            })

        roomInfoBinder.let {
            it.bind()
            mBinders.add(it)
        }

        val roomMemberBinder = AUIRoomMembersBinder(
            context,
            mRoomViewBinding.rightView,
            service,
            listener = object : AUIRoomMembersBinder.AUIRoomMemberEvent {
                override fun onCloseClickListener(view: View) {
                    AUIBroadLogger.logger()
                        .d("AUIVoiceRoomView", "AUIRoomMembersBinder onCloseClick ...")
                    mOnClickShutDown?.invoke()
                }

                override fun onLocalUserKickedOut() {
                    super.onLocalUserKickedOut()
                    showKickedOutDialog()
                }

                override fun showRoomOperate() {
                    super.showRoomOperate()
                    mOnClickMore?.invoke()


                }

                override fun kickUser(member: MemberBroadInfo?) {
                    super.kickUser(member)
                    mOnKickUser?.invoke(member!!.userId)

                }
            }
        )

        roomMemberBinder.let {
            it.bind()
            mBinders.add(it)
        }

        val micSeatsBinder = AUIMicSeatsBroadBinder(
            context,
            when (micType) {
                MicSeatBroadType.EightTag -> {
                    mRoomViewBinding.micSeatsView
                }

                MicSeatBroadType.NineTag -> {
                    mRoomViewBinding.micSeatsHostView
                }

                else -> {
                    mRoomViewBinding.micSeatsCircleView
                }
            }, service
        )

        micSeatsBinder.let {
            it.bind()
            mBinders.add(it)
        }

        val chatBottomBarBinder = AUIChatBottomBarBinder(
            service,
            mRoomViewBinding.chatBottomBar,
            mRoomViewBinding.chatListView,
            when (micType) {
                MicSeatBroadType.EightTag -> {
                    mRoomViewBinding.micSeatsView
                }

                MicSeatBroadType.NineTag -> {
                    mRoomViewBinding.micSeatsHostView
                }

                else -> {
                    mRoomViewBinding.micSeatsCircleView
                }
            },
            object : AUIChatBottomBarBinder.AUIChatBottomBarEventDelegate {
                override fun onClickGift(view: View?) {
                    auiGiftBarrageBinder?.showBottomGiftDialog()
                }

                override fun onClickLike(view: View?) {
//                    mRoomViewBinding.likeView.addFavor()
                }

                override fun onClickMore(view: View?) {
//                    showMoreDialog()
                    if (isRoomOwner()) {
                        mRoomViewBinding.chatBottomBar.setShowMoreStatus(true, false)
                        // 显示申请列表
                        invitationBinder?.showApplyDialog()

                    }
                }

                override fun onClickMic(view: View?) {

                }
            })

        chatBottomBarBinder.let {
            Log.d(TAG,"chatBottomBarBinder  it.bind()")
            it.bind()
            mBinders.add(it)
            listener = it.getSoftKeyboardHeightChangeListener()
        }

        invitationBinder = AUIInvitationBinder(activity, service)
        invitationBinder?.let {
            it.bind()
            mBinders.add(it)
        }
    }


    fun setFragmentActivity(activity: FragmentActivity){
        this.activity = activity
    }

    fun getImManagerService()=imManagerService

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        AUIBroadLogger.logger().d("AUIVoiceRoomView", "onDetachedFromWindow")

        mVoiceService?.observableHelper?.unSubscribeEvent(this)
        mVoiceService?.micSeatService?.unRegisterRespObserver(this)

        mBinders.forEach {
            it.unBind()
        }
//        if(isRoomOwner()){
//            imManagerService?.userDestroyedChatroom()
//        }else {
//            imManagerService?.userQuitRoom()
//        }
    }
    fun  destoryRoom(){

                if(isRoomOwner()){
                    try {
                        imManagerService?.userDestroyedChatroom()
                    }catch (e: Exception){
                        Log.d("销毁房间失败","${e.message}",e)
                    }
        }else {
            imManagerService?.userQuitRoom()
        }

    }

    private fun isRoomOwner() = AUIRoomBroadContext.shared().isRoomOwner(mVoiceService?.roomInfo?.roomId)

    private fun getSoftKeyboardHeight(){
        ThreadBroadManager.getInstance().runOnMainThreadDelay({
            activity?.let {
                AUIKeyboardStatusBroadWatcher(
                    it, activity!!
                ) { isKeyboardShowed: Boolean, keyboardHeight: Int? ->
                    Log.e("apex", "isKeyboardShowed: $isKeyboardShowed $keyboardHeight")
                    listener?.onSoftKeyboardHeightChanged(isKeyboardShowed, keyboardHeight)
                }
            }
        },200)
    }


    @SuppressLint("UseCompatLoadingForDrawables")
    private fun showMoreDialog(){
        val bean = VoiceMoreItemBean()
        val list = mutableListOf<VoiceMoreItemBean>()
        if (isRoomOwner()){ // 房主显示申请列表
            bean.ItemIcon = context.getDrawable(R.drawable.voice_icon_more_hands)
            bean.ItemTitle = context.getString(R.string.aui_room_apply_list)
            list.add(bean)
        }
        moreDialog = VoiceRoomMoreDialog(context, list, object :
            VoiceRoomMoreDialog.GridViewItemClickListener {
            override fun onItemClickListener(position: Int) {
                if (position == 0) {
                    if (isRoomOwner()) {
                        // 显示申请列表
                        invitationBinder?.showApplyDialog()
                    }
                }
            }
        })
        if (isRoomOwner()){
            activity?.supportFragmentManager?.let { moreDialog?.show(it,"more_dialog") }
        }
    }

    override fun onShowInvited(index:Int) {
        // 显示邀请dialog
        invitationBinder?.showInvitationDialog(index)
    }

    override fun onRoomDestroy(roomId: String) {
        AUIBroadLogger.logger().d("AUIVoiceRoomView", "onRoomDestroy $roomId")
        if (roomId == mVoiceService?.roomInfo?.roomId){
            mDestroyDialog.show()
        }
    }


    private fun showKickedOutDialog() {
        AUIAlertBroadDialog(context).apply {
            setTitle(R.string.voice_room_kick_by_host)
            setCancelable(false)
            setPositiveButton(context.getString(R.string.voice_room_confirm)) {
                dismiss()
                mOnRoomDestroyEvent?.invoke()
            }
            show()
        }
    }

    fun setOnShutDownClick(action: () -> Unit) {
        mOnClickShutDown = action
    }

    fun setPIPEvent(action: () -> Unit) {
        mPIPEvent = action
    }

    fun setClickShare(action: () -> Unit) {
        mOnClickShare = action
    }

    fun setClickMore(action: () -> Unit){
        mOnClickMore = action
    }

    fun setKickUserClick(action:((userId:String) -> Unit)? = null){
        mOnKickUser = action
    }


    fun setClickAllMic(action: (openMic: Boolean) -> Unit){
        mOnClickAllMic = action
    }
    fun setClickAllMute(action: (allMute: Boolean) -> Unit){
        mOnClickAllMute = action
    }


    fun setMuteEditLayout(isOwner: Boolean, isMute: Boolean){
        mRoomViewBinding.chatBottomBar.showMuteLayout(isOwner,isMute)

    }




    fun setOnRoomDestroyEvent(action: () -> Unit){
        mOnRoomDestroyEvent = action
    }

    private fun setUpRoomStyle(roomInfo:AUIRoomBroadInfo){
        val seatStyle = roomInfo.micSeatStyle
        micType = MicSeatBroadType.fromString(seatStyle)
        when( micType ){
            MicSeatBroadType.OneTag ->{
                mRoomViewBinding.micSeatsView.visibility = GONE
                mRoomViewBinding.micSeatsHostView.visibility = GONE
                mRoomViewBinding.micSeatsCircleView.visibility = VISIBLE
                mRoomViewBinding.micSeatsCircleView.setOptions(
                    MicSeatBroadOption(
                        roomInfo.micSeatCount, MicSeatBroadType.OneTag
                    )
                )
            }
            MicSeatBroadType.SixTag ->{
                mRoomViewBinding.micSeatsView.visibility = GONE
                mRoomViewBinding.micSeatsHostView.visibility = GONE
                mRoomViewBinding.micSeatsCircleView.visibility = VISIBLE
                mRoomViewBinding.micSeatsCircleView.setOptions(
                    MicSeatBroadOption(
                    roomInfo.micSeatCount,MicSeatBroadType.SixTag,if (
                    roomInfo.micSeatCount == 3 || roomInfo.micSeatCount == 5) 90 else 0
                )
                )
            }
            MicSeatBroadType.EightTag ->{
                mRoomViewBinding.micSeatsView.visibility = VISIBLE
                mRoomViewBinding.micSeatsCircleView.visibility = GONE
                mRoomViewBinding.micSeatsHostView.visibility = GONE
            }
            MicSeatBroadType.NineTag ->{
                mRoomViewBinding.micSeatsView.visibility = GONE
                mRoomViewBinding.micSeatsCircleView.visibility = GONE
                mRoomViewBinding.micSeatsHostView.visibility = VISIBLE
            }
            else -> {}
        }
    }
}