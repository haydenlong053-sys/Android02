package com.cboda.chatui.quickmeet.plugin.impl

import android.text.TextUtils
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.ThreadUtils.runOnUiThread
import com.cboda.baselibrary.bean.GlobalChatGroupBean
import com.cboda.baselibrary.toast.ToastBroadUtils
import com.cboda.baselibrary.utils.APP_TAG
import com.cboda.baselibrary.utils.BusLiveBroadData
import com.cboda.baselibrary.utils.LiveDataBroadBroadBus
import com.cboda.baselibrary.utils.MMKVBroadUtils
import com.cboda.baselibrary.utils.click
import com.cboda.baselibrary.utils.eLogD
import com.cboda.baselibrary.utils.loadImg
import com.cboda.baselibrary.utils.meetJoinMsgLiveData
import com.cboda.baselibrary.view.MeetVoiceWaveBroadView
import com.cboda.chatui.R
import com.cboda.chatui.quickmeet.CallEventCallback
import com.cboda.chatui.quickmeet.MeetCallManager
import com.cboda.chatui.quickmeet.bean.CallData
import com.cboda.chatui.quickmeet.bean.CallDirect
import com.cboda.chatui.quickmeet.bean.QuickDetailMeetBean
import com.cboda.chatui.quickmeet.bean.QuickMeetInfoBean
import com.cboda.chatui.quickmeet.dismissLoadingWfc
import com.cboda.chatui.quickmeet.goPermissionAudioMeet
import com.cboda.chatui.quickmeet.plugin.PluginLayout
import com.cboda.chatui.quickmeet.showLoadingWfc
import com.cboda.chatui.quickmeet.view.OnTouchHandleEvent
import com.cboda.chatui.quickmeet.view.ViewTouchHandle
import com.cboda.chatui.quickmeet.vm.QuickMeetViewBroadModel
import io.agora.rtc2.Constants.CONNECTION_CHANGED_BANNED_BY_SERVER
import io.agora.rtc2.Constants.CONNECTION_CHANGED_INVALID_TOKEN
import io.agora.rtc2.Constants.CONNECTION_STATE_FAILED
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.IRtcEngineEventHandler.AudioVolumeInfo
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

/**
 * 会议最小化显示界面
 */
class MeetPlugin(val activity: FragmentActivity) : PluginLayout(activity), CallEventCallback, OnTouchHandleEvent {
    private var callingStateHeight = 0
    private var viewWidth = 0
    private var touchHandle: ViewTouchHandle? = null

    private var seatMainContainer: View? = null
    private var seatMainAvatarIv: ImageView? = null
    private var seatMainMicIv: ImageView? = null
    private var seatMainVoiceWaveView: MeetVoiceWaveBroadView? = null
    private var seatMainTagTv: TextView? = null
    private var seatMainNameTv: TextView? = null

    private var seatSubContainer: View? = null
    private var seatSubAvatarIv: ImageView? = null
    private var seatSubMicIv: ImageView? = null
    private var seatSubVoiceWaveView: MeetVoiceWaveBroadView? = null
    private var seatSubTagTv: TextView? = null
    private var seatSubNameTv: TextView? = null

    private var seatGuestOneContainer: View? = null
    private var seatGuestOneAvatarIv: ImageView? = null
    private var seatGuestOneMicIv: ImageView? = null
    private var seatGuestOneVoiceWaveView: MeetVoiceWaveBroadView? = null
    private var seatGuestOneTagTv: TextView? = null
    private var seatGuestOneNameTv: TextView? = null

    private var seatGuestTwoContainer: View? = null
    private var seatGuestTwoAvatarIv: ImageView? = null
    private var seatGuestTwoMicIv: ImageView? = null
    private var seatGuestTwoVoiceWaveView: MeetVoiceWaveBroadView? = null
    private var seatGuestTwoTagTv: TextView? = null
    private var seatGuestTwoNameTv: TextView? = null

    private var coverAvatarIv: ImageView? = null
    private var mIvIconMic: ImageView? = null
    private var mTvMeetEnd: TextView? = null
    private var mVoiceWaveView: MeetVoiceWaveBroadView? = null
    private var mTvCloseMic: View? = null
    private var mViewRootMeetMain: View? = null
    private var mViewRootMeetFour: View? = null
    private var mTvUserName: TextView? = null

    private var viewModel: QuickMeetViewBroadModel? = null
    private var groupBean: GlobalChatGroupBean? = null
    private var groupId: String? = null
    private var meetChannelBean: QuickDetailMeetBean? = null


    val pluginMoveData: BusLiveBroadData<String> by lazy(LazyThreadSafetyMode.NONE) {
        LiveDataBroadBroadBus.getInstance().with("call_move_event")
    }

    init {
        callingStateHeight = activity.resources.getDimensionPixelSize(com.cboda.baselibrary.R.dimen.dp_96)
        viewWidth = activity.resources.getDimensionPixelSize(com.cboda.baselibrary.R.dimen.dp_88)
        MeetCallManager.getInstance().registerEngineEvent(this)
//        CallManager.getInstance().getCallTimerObserver().observe(lifecycleOwner) { time ->
//            time?.let {
//                if (!isVideoCall()) {
//                    if (mIsLayout) {
//                        if (isCalling()) {
//                            durationTv?.text = getCallingDuration(it)
//                        }
//                    }
//                }
//            }
//        }
//        eLogD(APP_TAG, "init--lifecycles_owner==${activity.lifecycle.currentState}")
        viewModel =
            ViewModelProvider(activity).get<QuickMeetViewBroadModel>(QuickMeetViewBroadModel::class.java)
        meetJoinMsgLiveData.observe(activity) {
            if (MeetCallManager.getInstance().isIdleState() && groupId == it) {
                queryMeetInfo(it)
            }
        }
    }

    private fun getCallingDuration(mills: Long): String {
//        return TimeUtil.secToTime((mills / 1000L).toInt())
        return "0"
    }

    private val moveObserver = Observer<String> { data ->
        data?.let {
            if (mIsLayout) {
                updateLayoutPosition(it, false)
            }
        }
    }


    override fun getContentLayout(): Int {
        return R.layout.meet_call_plugin_layout
    }

    override fun initView() {
        mView?.findViewById<FrameLayout>(R.id.chat_call_plugin_container_fl)
        mViewRootMeetMain = mView?.findViewById(R.id.mViewRootMeetMain)
        mViewRootMeetFour = mView?.findViewById(R.id.mViewRootMeetFour)

        seatMainContainer = mView?.findViewById(R.id.seat_main_container)
        seatMainAvatarIv = mView?.findViewById(R.id.seat_main_avatar_iv)
        seatMainMicIv = mView?.findViewById(R.id.seat_main_mic_iv)
        seatMainVoiceWaveView = mView?.findViewById(R.id.seat_main_voice_wave_view)
        seatMainTagTv = mView?.findViewById(R.id.seat_main_tag_tv)
        seatMainNameTv = mView?.findViewById(R.id.seat_main_name_tv)

        seatSubContainer = mView?.findViewById(R.id.seat_sub_container)
        seatSubAvatarIv = mView?.findViewById(R.id.seat_sub_avatar_iv)
        seatSubMicIv = mView?.findViewById(R.id.seat_sub_mic_iv)
        seatSubVoiceWaveView = mView?.findViewById(R.id.seat_sub_voice_wave_view)
        seatSubTagTv = mView?.findViewById(R.id.seat_sub_tag_tv)
        seatSubNameTv = mView?.findViewById(R.id.seat_sub_name_tv)

        seatGuestOneContainer = mView?.findViewById(R.id.seat_guest_one_container)
        seatGuestOneAvatarIv = mView?.findViewById(R.id.seat_guest_one_avatar_iv)
        seatGuestOneMicIv = mView?.findViewById(R.id.seat_guest_one_mic_iv)
        seatGuestOneVoiceWaveView = mView?.findViewById(R.id.seat_guest_one_voice_wave_view)
        seatGuestOneTagTv = mView?.findViewById(R.id.seat_guest_one_tag_tv)
        seatGuestOneNameTv = mView?.findViewById(R.id.seat_guest_one_name_tv)

        seatGuestTwoContainer = mView?.findViewById(R.id.seat_guest_two_container)
        seatGuestTwoAvatarIv = mView?.findViewById(R.id.seat_guest_two_avatar_iv)
        seatGuestTwoMicIv = mView?.findViewById(R.id.seat_guest_two_mic_iv)
        seatGuestTwoVoiceWaveView = mView?.findViewById(R.id.seat_guest_two_voice_wave_view)
        seatGuestTwoTagTv = mView?.findViewById(R.id.seat_guest_two_tag_tv)
        seatGuestTwoNameTv = mView?.findViewById(R.id.seat_guest_two_name_tv)

        coverAvatarIv = mView?.findViewById(R.id.chat_call_zoom_avatar_iv)
        mTvMeetEnd = mView?.findViewById(R.id.mTvMeetEnd)
        mVoiceWaveView = mView?.findViewById(R.id.mVoiceWaveView)
        mIvIconMic = mView?.findViewById(R.id.mIvIconMic)
        mTvCloseMic = mView?.findViewById(R.id.mTvCloseMic)
        mTvUserName = mView?.findViewById(R.id.mTvUserName)

        seatMainMicIv?.isSelected = false
        seatSubMicIv?.isSelected = false
        seatGuestOneMicIv?.isSelected = false
        seatGuestTwoMicIv?.isSelected = false
        mIvIconMic?.isSelected = false
        showUI()

        touchHandle = ViewTouchHandle(mView!!, activity, this)
        touchHandle?.setupViewSize(viewWidth, callingStateHeight, false)


        val margin = mContext?.resources?.getDimensionPixelSize(com.cboda.baselibrary.R.dimen.dp_358) ?: 0
        eLogD("call", "margin==$margin")
        touchHandle?.let {
            val result = pluginMoveData.value
            if (!TextUtils.isEmpty(result)) {
                updateLayoutPosition(result!!, true)
            } else {
                it.updateLocation(
                    it.screenWidth.toFloat(),
                    0f,
                    false
                )
            }
        }
    }

    var hasMeetInfoInvoke: (QuickDetailMeetBean) -> Unit = {}
    fun queryMeetInfo(groupIdNo: String, liveStatus: String = "active") {
        groupId = groupIdNo
        activity.lifecycleScope.launch {
            viewModel?.goQuickMeetInfo(groupIdNo, liveStatus)?.observe(activity) {
                if (it.list?.isNotEmpty() == true && it.token != null) {
                    hasMeetInfoInvoke.invoke(it.list!![0])
                    meetChannelBean = it.list!![0]
                    meetChannelBean?.token = it.token
                }
            }
        }

    }

    fun joinMyMeetRoom(isJoinInvoke: () -> Unit = {}) {
        goPermissionAudioMeet() {
            val userMId = MMKVBroadUtils.getUserMId()
            meetChannelBean?.let {
                isJoinInvoke.invoke()
                val direct = if (userMId == it.idNo.toString()) {
                    CallDirect.CALLER
                } else {
                    CallDirect.ANSWER
                }
                MeetCallManager.getInstance().callMeetAction(direct, it.channelId ?: "", it.token ?: "", it)
            }
        }
    }

    //取消会议
    private fun cancelMeeting() {
        if (MeetCallManager.getInstance().isCallDirect() && isCalling()) {
            MainScope().launch {
                val channelId = MeetCallManager.getInstance().mCallData.channelId
                viewModel?.goDissolveQuickMeet(channelId)?.observeForever { }
            }
        }
    }

    private fun updateLayoutPosition(result: String, force: Boolean = false) {
        if (result.contains(",") && (!mIsResume || force)) {
            val split = result.split(",")
            val x = split[0].toFloat()
            val y = split[1].toFloat()
            touchHandle?.updateLocation(
                x,
                y,
                false
            )
        }
    }

    override fun onItemClick(e: MotionEvent) {
        super.onItemClick(e)
        if (MeetCallManager.getInstance().isCallingState()) {
            mViewRootMeetMain?.let {
                it.isVisible = !it.isVisible
            }
            mViewRootMeetFour?.let {
                it.isVisible = !it.isVisible
            }
            mView?.post {
                touchHandle?.let {
                    it.setupViewSize()
                    val result = pluginMoveData.value
                    if (!TextUtils.isEmpty(result)) {
                        updateLayoutPosition(result!!, true)
                    } else {
                        it.updateLocation(
                            0f,
                            0f,
                            false
                        )
                    }
                }
            }

        }
    }

    override fun updateLocation(translationX: Float, translationY: Float) {
        super.updateLocation(translationX, translationY)
        if (mIsResume) {
            pluginMoveData.value = ("$translationX,$translationY")
        }
    }

    private fun showUI() {
        mTvMeetEnd?.click {
            goOverMeeting()

        }
        mTvCloseMic?.click {
            if (MeetCallManager.getInstance().isCallDirect()) {
                mIvIconMic?.let { mivView ->
                    val muteLocalAudio = MeetCallManager.getInstance().muteLocalAudio(!mivView.isSelected)
                    if (muteLocalAudio == 0) {
                        mivView.isSelected = !mivView.isSelected
                    }
                }
            }

        }
//        showOrHideCover()
    }

    private fun goOverMeeting() {
        cancelMeeting()
        MeetCallManager.getInstance().leaveAndRelease()
    }

    /**
     * 会议中
     * @return Boolean
     */
    private fun isCalling(): Boolean {
        return MeetCallManager.getInstance().isCallingState()
    }

    private fun showOrHideCover() {
        groupBean?.let {
            if (it.userList?.isNotEmpty() == true) {
                val quickDetailMeetBean = MeetCallManager.getInstance().mCallData.quickDetailMeetBean
                it.userList?.find { user ->
                    quickDetailMeetBean?.let { detailMeetBean ->
                        user.userId == detailMeetBean.userId
                    } ?: false

                }?.let { ownerUser ->
                    //会议发起人的信息
                    coverAvatarIv?.loadImg(ownerUser.icon)
                    mTvUserName?.text = ownerUser.name
                    seatMainAvatarIv?.loadImg(ownerUser.icon)
                    seatMainNameTv?.text = ownerUser.name
                }
            }
        }
    }

    override fun onEngineSetup() {
        showLoadingWfc()
    }

    override fun onRtcConnectionStateChanged(state: Int, reason: Int) {
        if (state == CONNECTION_STATE_FAILED && reason == CONNECTION_CHANGED_INVALID_TOKEN) {
            dismissLoadingWfc()
            ToastBroadUtils.show("会议无效的 Token")
        } else if (state == CONNECTION_STATE_FAILED && reason == CONNECTION_CHANGED_BANNED_BY_SERVER) {
            goOverMeeting()
//            cancelMeeting()
//            MeetCallManager.getInstance().leaveAndRelease()
        }

    }

    override fun onEngineDestroy(data: CallData) {
        eLogD(APP_TAG, "mIsLayout==>$mIsLayout")
        if (mIsLayout) {
            touchHandle?.let {
                it.setDisabledTouch(true)
                if (it.isAnimStart) {
                    it.moveEnd(true)
                }
            }
            removePanel()
        }
    }

    override fun onRtcJoinChannelSuccess() {
        super.onRtcJoinChannelSuccess()
        dismissLoadingWfc()
        showMeetMiniLayoutView()
//        Toast.makeText(UtilBroadApplication.cxt.applicationContext, "Join channel success", Toast.LENGTH_SHORT).show()

    }

    override fun onRoomAudioVolumeIndication(speakers: Array<AudioVolumeInfo>?, totalVolume: Int) {
        if (totalVolume > 0 && mVoiceWaveView?.isAnimating() == false) {
            mVoiceWaveView?.startLoopAnim()
        }
        if (totalVolume > 0 && seatMainVoiceWaveView?.isAnimating() == false) {
            seatMainVoiceWaveView?.startLoopAnim()
        }
    }

    override fun onRtcRoleChanged(oldRole: Int, newRole: Int) {

    }

    override fun onRtcStats(statsBean: IRtcEngineEventHandler.RtcStats?) {
        //先判断会议人数有没有变化后，再在主线程更新人数信息
//        runOnUiThread {
//        }
    }

    override fun onRoomUserMuteAudio(uid: Int, muted: Boolean) {
        mIvIconMic?.isSelected = muted
    }

    override fun onRtcUserJoined(uid: Int) {
        //有人加入频道
        eLogD("call", "onRtcUserJoined uid = $uid; mIsLayout=$mIsLayout; isResume=$mIsResume")
//        Toast.makeText(UtilBroadApplication.cxt.applicationContext, "User joined: " + uid, Toast.LENGTH_SHORT).show()

    }

    override fun onRtcUserOffline(uid: Int, reason: Int) {
        //有人离开频道
        eLogD("call", "onRtcUserOffline uid = $uid; mIsLayout=$mIsLayout; isResume=$mIsResume")
//        Toast.makeText(UtilBroadApplication.cxt.applicationContext, "User offline: " + uid, Toast.LENGTH_SHORT).show()
    }

    private fun removePanel() {
        mVoiceWaveView?.stopLoopAnim()
        seatMainVoiceWaveView?.stopLoopAnim()
        mView?.visibility = View.GONE
        removeView()
        mIsLayout = false
    }

    override fun onResume() {
        super.onResume()
        showMeetMiniLayoutView()

    }


    private fun showMeetMiniLayoutView() {
        if (mIsLayout) {
            mView?.visibility = View.VISIBLE
        } else {
            if (MeetCallManager.getInstance().isCallingState()) {
                //当前处于会议通话状态
                setupPanel(activity)
                queryGroupInfo()
            }
//            setupPanel(activity)
        }
    }

    private fun queryGroupInfo() {
        activity.lifecycleScope.launch {
            val channelId = MeetCallManager.getInstance().mCallData.channelId
            viewModel?.queryGroupInfo(channelId, failInvoke = { code, _ ->
                // 如果当前用户已不在群组内，则退出正在进行中的会议
                if (code == 200 && MeetCallManager.getInstance().isCallingState()) {
                    goOverMeeting()
                }
            })?.observe(activity) { bean ->
                groupBean = bean
                showOrHideCover()

            }
        }
    }

    override fun onPermissionDenied() {

    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
//        eLogD(APP_TAG, "onResume--lifecycles_owner==${activity.lifecycle.currentState}")
    }

    override fun onPause() {
        super.onPause()
//        eLogD(APP_TAG, "onPause--lifecycles_owner==${activity.lifecycle.currentState}")
        if (mIsLayout
            && MeetCallManager.getInstance().isCallingState()
        ) {
            mView?.visibility = View.INVISIBLE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        eLogD("onDestroy==========>")
        MeetCallManager.getInstance().unregisterEngineEvent(this)
        goOverMeeting()
        removePanel()
        touchHandle = null
    }
}