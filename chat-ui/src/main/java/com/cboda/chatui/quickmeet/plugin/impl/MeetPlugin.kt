package com.cboda.chatui.quickmeet.plugin.impl

import android.annotation.SuppressLint
import android.graphics.Color
import android.text.TextUtils
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.cboda.baselibrary.bean.MeetUser
import com.cboda.baselibrary.toast.ToastBroadUtils
import com.cboda.baselibrary.utils.APP_TAG
import com.cboda.baselibrary.utils.BusLiveBroadData
import com.cboda.baselibrary.utils.LiveDataBroadBroadBus
import com.cboda.baselibrary.utils.MMKVBroadUtils
import com.cboda.baselibrary.utils.anyToInt
import com.cboda.baselibrary.utils.click
import com.cboda.baselibrary.utils.eLogD
import com.cboda.baselibrary.utils.loadImg
import com.cboda.baselibrary.utils.meetJoinMsgLiveData
import com.cboda.baselibrary.utils.meetSeatChangeMsgLiveData
import com.cboda.baselibrary.view.MeetVoiceWaveBroadView
import com.cboda.chatui.R
import com.cboda.chatui.quickmeet.CallEventCallback
import com.cboda.chatui.quickmeet.MeetCallManager
import com.cboda.chatui.quickmeet.bean.CallData
import com.cboda.chatui.quickmeet.bean.CallDirect
import com.cboda.chatui.quickmeet.bean.GroupMemberRole
import com.cboda.chatui.quickmeet.bean.QuickDetailMeetBean
import com.cboda.chatui.quickmeet.bean.QuickMeetInfoBean
import com.cboda.chatui.quickmeet.bean.SeatNumb
import com.cboda.chatui.quickmeet.dismissLoadingWfc
import com.cboda.chatui.quickmeet.goPermissionAudioMeet
import com.cboda.chatui.quickmeet.plugin.PluginLayout
import com.cboda.chatui.quickmeet.showDownMicDialog
import com.cboda.chatui.quickmeet.showLoadingWfc
import com.cboda.chatui.quickmeet.view.OnTouchHandleEvent
import com.cboda.chatui.quickmeet.view.ViewTouchHandle
import com.cboda.chatui.quickmeet.vm.QuickMeetViewBroadModel
import io.agora.rtc2.Constants.CONNECTION_CHANGED_INVALID_TOKEN
import io.agora.rtc2.Constants.CONNECTION_CHANGED_KEEP_ALIVE_TIMEOUT
import io.agora.rtc2.Constants.CONNECTION_STATE_FAILED
import io.agora.rtc2.Constants.CONNECTION_STATE_RECONNECTING
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.IRtcEngineEventHandler.AudioVolumeInfo
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlin.let

/**
 * 会议最小化显示界面
 */
class MeetPlugin(val activity: FragmentActivity) : PluginLayout(activity), CallEventCallback, OnTouchHandleEvent {
    private val intervalTime = 15000L
    private var callingStateHeight = 0
    private var viewWidth = 0
    private var marginTop = 0
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
    private var mTvCloseMic: TextView? = null
    private var mViewRootMeetMain: View? = null
    private var mViewRootMeetFour: View? = null
    private var mViewUiLine: View? = null
    private var mTvUserName: TextView? = null

    // 用于“点击悬浮窗外收起四座位”的全屏透明遮罩
    private var outsideTapMaskView: View? = null

    private var viewModel: QuickMeetViewBroadModel? = null
    private var groupId: String? = null
    private var meetChannelBean: QuickDetailMeetBean? = null

    /** 自研群聊本地群成员角色，加入会议时写入 CallData。 */
    var groupMemberRole: Int = GroupMemberRole.UNKNOWN

    //4个固定麦位的本地数据
    private val seatMicFourList = arrayListOf(
        MeetUser(seatNumbIndex = SeatNumb.MIC_MAIN.ordinal),
        MeetUser(seatNumbIndex = SeatNumb.MIC_SUB.ordinal),
        MeetUser(seatNumbIndex = SeatNumb.MIC_GUEST_ONE.ordinal),
        MeetUser(seatNumbIndex = SeatNumb.MIC_GUEST_TWO.ordinal)
    )

    //麦位上当前所有人员的麦克风禁言状态的集合
    private val tempSeatMutAudioMap = hashMapOf<Int, Boolean>()
    private val myMid by lazy {
        MMKVBroadUtils.getUserMId()
    }
    private val myUserId by lazy {
        MMKVBroadUtils.getRealUserId() ?: ""
    }
    private val openMicText by lazy {
        activity.resources.getString(com.cboda.baselibrary.R.string.msg_meeting_open_mic)
    }
    private val closeMicText by lazy {
        activity.resources.getString(com.cboda.baselibrary.R.string.msg_meeting_close_mic)
    }

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
        meetSeatChangeMsgLiveData.observe(activity) {
            if (groupId == it) {
                goRefreshMeetSeatInfo()
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
        mViewUiLine = mView?.findViewById(R.id.mViewUiLine)

        seatMainMicIv?.isSelected = false
        seatSubMicIv?.isSelected = false
        seatGuestOneMicIv?.isSelected = false
        seatGuestTwoMicIv?.isSelected = false
        mIvIconMic?.isSelected = false
        showUI()

        touchHandle = ViewTouchHandle(mView!!, activity, this)
        touchHandle?.setupViewSize(viewWidth, callingStateHeight, false)

        setupOutsideTapToCollapseFourSeats()

        marginTop = mContext?.resources?.getDimensionPixelSize(com.cboda.baselibrary.R.dimen.dp_100) ?: 0
        touchHandle?.let {
            val result = pluginMoveData.value
            if (!TextUtils.isEmpty(result)) {
                updateLayoutPosition(result!!, true)
            } else {
                it.updateLocation(
//                    it.screenWidth.toFloat(),
                    0f,
                    marginTop.toFloat(),
                    false
                )
            }
        }
    }

    var hasMeetInfoInvoke: (QuickMeetInfoBean) -> Unit = {}
    fun queryMeetInfo(groupIdNo: String, liveStatus: String = "active") {
        groupId = groupIdNo
        activity.lifecycleScope.launch {
            viewModel?.goQuickMeetInfo(groupIdNo, liveStatus)?.observe(activity) {
                if (it.list?.isNotEmpty() == true && it.token != null) {
                    hasMeetInfoInvoke.invoke(it)
                    meetChannelBean = it.list!![0]
                    meetChannelBean?.token = it.token
                }
            }
        }

    }

    fun joinMyMeetRoom(isJoinInvoke: () -> Unit = {}) {
        goPermissionAudioMeet() {
            meetChannelBean?.let {
                isJoinInvoke.invoke()
                val direct = if (myMid == it.idNo.toString()) {
                    CallDirect.CALLER
                } else {
                    CallDirect.ANSWER
                }
                MeetCallManager.getInstance().callMeetAction(
                    direct,
                    it.channelId ?: "",
                    it.token ?: "",
                    it,
                    groupMemberRole,
                )
            }
        }
    }

    //取消会议
    private fun cancelMeeting() {
        if (MeetCallManager.getInstance().isOwner()) {
            val callData = MeetCallManager.getInstance().mCallData
            val bean = callData.quickDetailMeetBean
            val imGroupId = bean?.groupIdNo?.takeIf { it.isNotEmpty() }
                ?: groupId?.takeIf { it.isNotEmpty() }
                ?: bean?.channelId?.takeIf { it.isNotEmpty() }
                ?: callData.channelId
            // 禁言恢复由 leaveAndRelease / 离会回调统一触发；此处只负责解散房间
            if (!imGroupId.isNullOrEmpty() && isCalling()) {
                MainScope().launch {
                    viewModel?.goDissolveQuickMeet(imGroupId)?.observeForever { }
                }
            }
        } else if (isCalling() && MeetCallManager.getInstance().isCallDirect()) {
            //会议中的发言方（主播端）退出频道
            MainScope().launch {
                viewModel?.goNetDownMic(myUserId, groupId)?.observeForever { }
            }
        } else if (isCalling() && MeetCallManager.getInstance().mCallData.useLiveKit) {
            // LiveKit 观众离会：记 leave 流水
            MainScope().launch {
                val meetingId = MeetCallManager.getInstance().mCallData.meetingId
                    .ifBlank { MeetCallManager.getInstance().mCallData.quickDetailMeetBean?.meetingId }
                val roomName = MeetCallManager.getInstance().mCallData.roomName
                    .ifBlank { MeetCallManager.getInstance().mCallData.quickDetailMeetBean?.roomName }
                com.freddy.im.http.ImMeetingHttpExample.requestLeave(meetingId, roomName, null, null)
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
        switchSeatUI()

    }

    private fun switchSeatUI() {
        mViewRootMeetMain?.let {
            it.isVisible = !it.isVisible
        }
        mViewRootMeetFour?.let {
            it.isVisible = !it.isVisible
        }
        syncOutsideTapMaskVisibility()
        mView?.post {
            touchHandle?.let {
                it.setupViewSize()
                val result = pluginMoveData.value
                if (!TextUtils.isEmpty(result)) {
                    updateLayoutPosition(result!!, true)
                } else {
                    it.updateLocation(
                        0f,
                        marginTop.toFloat(),
                        false
                    )
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
        MeetCallManager.getInstance().mCallData.quickDetailMeetBean?.let {
            mTvMeetEnd?.text = if (MeetCallManager.getInstance().isOwner()) {
                activity.getString(com.cboda.baselibrary.R.string.msg_meeting_end)
            } else {
                activity.getString(com.cboda.baselibrary.R.string.msg_meeting_exit)
            }
        }
        mTvMeetEnd?.click {
            if (isCalling()) {
                goOverMeeting()
            } else {
                //没收到声网销毁引擎回调的异常处理，手动关闭会议UI
                goOverMeeting()
                onMeetDestroy()
            }
        }
        mTvCloseMic?.click {
            if (MeetCallManager.getInstance().isCallDirect()) {
                seatMicFourList.find {
                    it.userId == myUserId
                }?.let { seatUser ->
                    goLocalMuteSeatMicStateChange(seatUser.seatNumbIndex)
                }

            }

        }

        seatMainContainer?.click { }
        seatSubContainer?.click {
            if (seatSubMicIv?.isVisible == true) {
                goDownMic(SeatNumb.MIC_SUB.ordinal)
            }

        }
        seatGuestOneContainer?.click {
            if (seatGuestOneMicIv?.isVisible == true) {
                goDownMic(SeatNumb.MIC_GUEST_ONE.ordinal)
            }

        }
        seatGuestTwoContainer?.click {
            if (seatGuestTwoMicIv?.isVisible == true) {
                goDownMic(SeatNumb.MIC_GUEST_TWO.ordinal)
            }

        }
        showMicBtnUI()
    }

    //群主或管理员点击下mic弹框
    private fun goDownMic(ordinal: Int) {
        if (MeetCallManager.getInstance().isGroupOwnerOrAdmin()) {
            showDownMicDialog(activity) { i, _ ->
                val applicantUserId = if (SeatNumb.MIC_SUB.ordinal == ordinal) {
                    seatMicFourList[1].userId
                } else if (SeatNumb.MIC_GUEST_ONE.ordinal == ordinal) {
                    seatMicFourList[2].userId
                } else if (SeatNumb.MIC_GUEST_TWO.ordinal == ordinal) {
                    seatMicFourList[3].userId
                } else {
                    ""
                }
                activity.lifecycleScope.launch {
                    viewModel?.goNetDownMic(applicantUserId, groupId)?.observe(activity) {
                        ToastBroadUtils.show(com.cboda.baselibrary.R.string.msg_meeting_off_mic_done)
                    }
                }

            }
        }
    }

    private fun showMicBtnUI() {
        if (MeetCallManager.getInstance().isCallDirect()) {
            mTvCloseMic?.isVisible = true
            mViewUiLine?.isVisible = true
        } else {
            mTvCloseMic?.isVisible = false
            mViewUiLine?.isVisible = false
        }
    }

    /**
     * 当 `mViewRootMeetFour` 处于展开状态时，点击悬浮窗外部收起四座位。
     * 逻辑参考 `onItemClick`：收起后会重算 `touchHandle` 尺寸并矫正位置边界。
     */
    @SuppressLint("ClickableViewAccessibility")
    private fun setupOutsideTapToCollapseFourSeats() {
        if (outsideTapMaskView == null) {
            outsideTapMaskView = View(activity).apply {
                setBackgroundColor(Color.TRANSPARENT)
                isClickable = true
                isFocusable = false
                visibility = View.GONE

                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )

                setOnTouchListener { _, event ->
                    if (mViewRootMeetFour?.isVisible != true) return@setOnTouchListener false

                    when (event.actionMasked) {
                        MotionEvent.ACTION_DOWN -> {
                            switchSeatUI()
                            return@setOnTouchListener true
                        }
                    }
                    // 遮罩已命中：把事件传到底层
                    false
                }
            }

            // 插入到 mView 下面，保证 mView 自己仍能收到触摸事件
            mContainer?.let { container ->
                val index = container.indexOfChild(mView!!)
                if (index >= 0) {
                    container.addView(outsideTapMaskView, index)
                } else {
                    container.addView(outsideTapMaskView)
                }
            }
        }

        syncOutsideTapMaskVisibility()
    }

    private fun syncOutsideTapMaskVisibility() {
        outsideTapMaskView?.visibility =
            if (mViewRootMeetFour?.isVisible == true) View.VISIBLE else View.GONE
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

    //本人麦位上的麦克风状态变更---取反
    private fun goLocalMuteSeatMicStateChange(index: Int) {
        fun toggleMic(micIv: ImageView?, updateMainIcon: Boolean) {
            micIv ?: return
            val muteLocalAudio = MeetCallManager.getInstance().muteLocalAudio(!micIv.isSelected)
            if (muteLocalAudio == 0) {
                micIv.isSelected = !micIv.isSelected
                if (updateMainIcon) {
                    mIvIconMic?.isSelected = !mIvIconMic!!.isSelected
                }
                tempSeatMutAudioMap.put(myMid.toInt(), micIv.isSelected)
                // 已禁麦 => 展示“打开麦克风”，未禁麦 => 展示“关闭麦克风”
                mTvCloseMic?.text = if (micIv.isSelected) openMicText else closeMicText
            }
        }

        when (index) {
            SeatNumb.MIC_MAIN.ordinal -> toggleMic(seatMainMicIv, updateMainIcon = true)
            SeatNumb.MIC_SUB.ordinal -> toggleMic(seatSubMicIv, updateMainIcon = false)
            SeatNumb.MIC_GUEST_ONE.ordinal -> toggleMic(seatGuestOneMicIv, updateMainIcon = false)
            SeatNumb.MIC_GUEST_TWO.ordinal -> toggleMic(seatGuestTwoMicIv, updateMainIcon = false)
        }
    }

    //麦位上的用户信息展示
    private fun showSeatAvatarNameUI(index: Int, icon: String?, name: String?) {
        if (index == SeatNumb.MIC_MAIN.ordinal) {
            //2个值都等于null即表示麦位无人
            if (icon == null && name == null) {
                seatMainAvatarIv?.setImageResource(R.drawable.meet_mic_site_bg)
                seatMainNameTv?.text = ""
                seatMainMicIv?.isVisible = false
            } else {
                coverAvatarIv?.loadImg(icon)
                mTvUserName?.text = name
                seatMainAvatarIv?.loadImg(icon)
                seatMainNameTv?.text = name
                seatMainMicIv?.isVisible = true
            }

        } else if (index == SeatNumb.MIC_SUB.ordinal) {
            if (icon == null && name == null) {
                seatSubAvatarIv?.setImageResource(R.drawable.meet_mic_site_bg)
                seatSubNameTv?.text = ""
                seatSubMicIv?.isVisible = false
            } else {
                seatSubAvatarIv?.loadImg(icon)
                seatSubNameTv?.text = name
                seatSubMicIv?.isVisible = true
            }

        } else if (index == SeatNumb.MIC_GUEST_ONE.ordinal) {
            if (icon == null && name == null) {
                seatGuestOneAvatarIv?.setImageResource(R.drawable.meet_mic_site_bg)
                seatGuestOneNameTv?.text = ""
                seatGuestOneMicIv?.isVisible = false
            } else {
                seatGuestOneAvatarIv?.loadImg(icon)
                seatGuestOneNameTv?.text = name
                seatGuestOneMicIv?.isVisible = true
            }

        } else if (index == SeatNumb.MIC_GUEST_TWO.ordinal) {
            if (icon == null && name == null) {
                seatGuestTwoAvatarIv?.setImageResource(R.drawable.meet_mic_site_bg)
                seatGuestTwoNameTv?.text = ""
                seatGuestTwoMicIv?.isVisible = false
            } else {
                seatGuestTwoAvatarIv?.loadImg(icon)
                seatGuestTwoNameTv?.text = name
                seatGuestTwoMicIv?.isVisible = true
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
            goOverMeeting()
        } else if (state == CONNECTION_STATE_FAILED) {
            // 含 BANNED_BY_SERVER（时间到）及其他失败：意外结束也要离会并恢复禁言
            goOverMeeting()
        } else if (state == CONNECTION_STATE_RECONNECTING && reason == CONNECTION_CHANGED_KEEP_ALIVE_TIMEOUT) {
            ToastBroadUtils.show("当前会议网络环境较差，正在重新连接")
        }

    }

    override fun onEngineDestroy(data: CallData) {
        eLogD(APP_TAG, "onEngineDestroy---mIsLayout==>$mIsLayout")
        onMeetDestroy()
    }

    private fun onMeetDestroy() {
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
        upAttendanceCheckIn()
        showMeetMiniLayoutView()
//        Toast.makeText(UtilBroadApplication.cxt.applicationContext, "Join channel success", Toast.LENGTH_SHORT).show()
    }

    //进入会议上报给服务器记录次数
    private fun upAttendanceCheckIn() {
        activity.lifecycleScope.launch {
            val channelId = MeetCallManager.getInstance().mCallData.channelId
            viewModel?.goAttendanceCheckIn(channelId)?.observe(activity) { bean ->
                eLogD(APP_TAG,"会议进入成功，上报服务器统计次数")
            }
        }
    }


    override fun onRoomAudioVolumeIndication(speakers: Array<AudioVolumeInfo>?, totalVolume: Int) {
        if (totalVolume > 0 && mVoiceWaveView?.isAnimating() == false) {
            mVoiceWaveView?.startLoopAnim()
        }
        updateFourSeatSpeakingState(speakers)
    }

    /**
     * 依据 Agora 回调中的 uid 与麦位用户 idno 对应关系，找出当前正在说话的麦位并更新波纹动画。
     * 比对规则：AudioVolumeInfo.uid == MeetUser.idno
     *
     */
    private fun updateFourSeatSpeakingState(speakers: Array<AudioVolumeInfo>?) {
        speakers?.forEach { speaker ->
//            eLogD(APP_TAG, "speakers.uid==>${speaker.uid.toString()}")
            if (speaker.volume <= 0) return@forEach
            val uidString = if (speaker.uid == 0) {
                myMid
            } else {
                speaker.uid.toString()
            }
            seatMicFourList.find { meetUser ->
                !meetUser.idNo.isNullOrEmpty() && meetUser.idNo == uidString
            }?.let {
//                eLogD(APP_TAG, "speakers.uid==>${it.idNo}----seatNumbIndex =${it.seatNumbIndex}")
                updateSeatWaveByIndex(it.seatNumbIndex)
            }
        }

    }

    private fun updateSeatWaveByIndex(seatIndex: Int) {
        if (SeatNumb.MIC_MAIN.ordinal == seatIndex && seatMainVoiceWaveView?.isAnimating() == false) {
            seatMainVoiceWaveView?.startLoopAnim()
        } else if (SeatNumb.MIC_SUB.ordinal == seatIndex && seatSubVoiceWaveView?.isAnimating() == false) {
            seatSubVoiceWaveView?.startLoopAnim()
        } else if (SeatNumb.MIC_GUEST_ONE.ordinal == seatIndex && seatGuestOneVoiceWaveView?.isAnimating() == false) {
            seatGuestOneVoiceWaveView?.startLoopAnim()
        } else if (SeatNumb.MIC_GUEST_TWO.ordinal == seatIndex && seatGuestTwoVoiceWaveView?.isAnimating() == false) {
            seatGuestTwoVoiceWaveView?.startLoopAnim()
        }
    }

    override fun onRtcRoleChanged(oldRole: Int, newRole: Int) {
        showMicBtnUI()
    }

    private var meetRefreshPersonTimer = 0L
    override fun onRtcStats(statsBean: IRtcEngineEventHandler.RtcStats?) {
//        eLogD(APP_TAG, "RtcStats=users=>${statsBean?.users}")

        if (System.currentTimeMillis() > meetRefreshPersonTimer) {
            meetRefreshPersonTimer = System.currentTimeMillis() + intervalTime
            activity.lifecycleScope.launch {
                viewModel?.queryMeetTotalPersonCount(groupId)?.observe(activity) {
                    MeetCallManager.getInstance().notifyMeetTotalPersonCount(groupId ?: "", anyToInt(it))
                }
            }
        }

    }

    override fun onRoomUserMuteAudio(uid: Int, muted: Boolean) {
        tempSeatMutAudioMap.put(uid, muted)
        refreshSeatMicState(uid, muted)
    }

    //刷新麦位上的麦克风状态
    private fun refreshSeatMicState(uid: Int, muted: Boolean) {
        seatMicFourList.find {
            it.idNo?.toIntOrNull() == uid
        }?.let {
            if (it.seatNumbIndex == SeatNumb.MIC_MAIN.ordinal) {
                mIvIconMic?.isSelected = muted
                seatMainMicIv?.isSelected = muted
            } else if (it.seatNumbIndex == SeatNumb.MIC_SUB.ordinal) {
                seatSubMicIv?.isSelected = muted
            } else if (it.seatNumbIndex == SeatNumb.MIC_GUEST_ONE.ordinal) {
                seatGuestOneMicIv?.isSelected = muted
            } else if (it.seatNumbIndex == SeatNumb.MIC_GUEST_TWO.ordinal) {
                seatGuestTwoMicIv?.isSelected = muted
            }
        }
    }

    //刷新同步4个固定麦位信息
    private fun goRefreshMeetSeatInfo(isRefreshMainMic: Boolean = false) {
        if (MeetCallManager.getInstance().isCallingState()) {
            activity.lifecycleScope.launch {
                viewModel?.goMeetMicSeatPersonList(groupId)?.observe(activity) {
                    val quickDetailMeetBean = MeetCallManager.getInstance().mCallData.quickDetailMeetBean
                    it.speakingList?.let { netList ->
                        //先将接口返回的数据排序，将主麦人放到集合首位
                        netList.find { netBean ->
                            quickDetailMeetBean?.userId == netBean.userId
                        }?.let { bean ->
                            val indexOf = netList.indexOf(bean)
                            if (indexOf != 0) {
                                netList.remove(bean)
                                netList.add(0, bean)
                            }
                        }
                        //同步接口数据到 4个固定麦位的集合上
                        seatMicFourList.forEachIndexed { index, seatMicFourUser ->
                            if (index < netList.size) {
                                seatMicFourUser.icon = netList[index].icon
                                seatMicFourUser.nickname = netList[index].nickname
                                seatMicFourUser.role = netList[index].role
                                seatMicFourUser.userId = netList[index].userId
                                seatMicFourUser.idNo = netList[index].idNo
                            } else {
                                seatMicFourUser.icon = null
                                seatMicFourUser.nickname = null
                                seatMicFourUser.role = null
                                seatMicFourUser.userId = null
                                seatMicFourUser.idNo = null
                            }
                        }


                    }

                    seatMicFourList.forEach { bean ->
                        if (bean.seatNumbIndex != SeatNumb.MIC_MAIN.ordinal) {
                            showSeatAvatarNameUI(bean.seatNumbIndex, bean.icon, bean.nickname)
                        } else if (isRefreshMainMic) {
                            showSeatAvatarNameUI(bean.seatNumbIndex, bean.icon, bean.nickname)
                        }

                    }
                    goRefreshMyselfSeatMic()
                    //同步麦位上的麦克风状态
                    tempSeatMutAudioMap.forEach { (key, value) ->
                        refreshSeatMicState(key, value)
                    }
                }
            }
        }

    }

    //同步自己的上下麦信息（过滤掉会议房主）
    private fun goRefreshMyselfSeatMic() {
        if (!MeetCallManager.getInstance().isOwner()) {
            seatMicFourList.find { meetUser ->
                meetUser.userId == myUserId
            }?.let {
                //上麦
                if (!MeetCallManager.getInstance().isCallDirect()) {
                    if (MeetCallManager.getInstance().mCallData.useLiveKit) {
                        // 文档：审批后必须重新 /join 换发麦 token
                        MainScope().launch {
                            rejoinLiveKitAfterApproved()
                        }
                    } else {
                        MeetCallManager.getInstance().setRtcClientRoleDirect(CallDirect.CALLER).let { successInt ->
                            if (successInt == 0 && it.idNo?.isNotEmpty() == true) {
                                tempSeatMutAudioMap.put(anyToInt(it.idNo!!), false)
                            }
                        }
                    }
                }

            } ?: kotlin.run {
                //下麦
                if (MeetCallManager.getInstance().isCallDirect()) {
                    MeetCallManager.getInstance().setRtcClientRoleDirect(CallDirect.ANSWER)
                }
            }
        }


    }

    private suspend fun rejoinLiveKitAfterApproved() {
        val callData = MeetCallManager.getInstance().mCallData
        val meetingId = callData.meetingId.ifBlank { callData.quickDetailMeetBean?.meetingId }
        val roomName = callData.roomName.ifBlank { callData.quickDetailMeetBean?.roomName }
        com.freddy.im.http.ImMeetingHttpExample.requestJoin(
            meetingId,
            roomName,
            null,
            { session ->
                MeetCallManager.getInstance().reconnectLiveKit(
                    session.url,
                    session.token,
                    session.isCanPublish,
                )
            },
            null,
        )
    }

    override fun onRtcUserJoined(uid: Int) {
        //有人加入频道
        eLogD(APP_TAG, "onRtcUserJoined uid = $uid; mIsLayout=$mIsLayout; isResume=$mIsResume")
//        ToastBroadUtils.show("User joined: " + uid)

    }

    override fun onRtcUserOffline(uid: Int, reason: Int) {
        //有人离开频道
        eLogD(APP_TAG, "onRtcUserOffline uid = $uid; mIsLayout=$mIsLayout; isResume=$mIsResume")
//        ToastBroadUtils.show("User offline: " + uid)

    }

    private fun removePanel() {
        clearFourSeatMicList()
        tempSeatMutAudioMap.clear()
        clearVoiceWaveAnimate()

        mView?.visibility = View.GONE
        outsideTapMaskView?.isVisible = false
        outsideTapMaskView?.let { mask ->
            mContainer?.removeView(mask)
        }
        outsideTapMaskView = null
        removeView()
        mIsLayout = false
    }

    private fun clearVoiceWaveAnimate() {
        if (mVoiceWaveView?.isAnimating() == true) {
            mVoiceWaveView?.stopLoopAnim()
        }

        if (seatMainVoiceWaveView?.isAnimating() == true) {
            seatMainVoiceWaveView?.stopLoopAnim()
        }
        if (seatSubVoiceWaveView?.isAnimating() == true) {
            seatSubVoiceWaveView?.stopLoopAnim()
        }
        if (seatGuestOneVoiceWaveView?.isAnimating() == true) {
            seatGuestOneVoiceWaveView?.stopLoopAnim()
        }
        if (seatGuestTwoVoiceWaveView?.isAnimating() == true) {
            seatGuestTwoVoiceWaveView?.stopLoopAnim()
        }

    }

    private fun clearFourSeatMicList() {
        seatMicFourList.forEach {
            it.icon = null
            it.nickname = null
            it.role = null
            it.userId = null
            it.idNo = null
        }
    }

    override fun onResume() {
        super.onResume()
        showMeetMiniLayoutView()

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

    private fun showMeetMiniLayoutView() {
        if (mIsLayout) {
            mView?.visibility = View.VISIBLE
        } else {
            if (MeetCallManager.getInstance().isCallingState()) {
                //当前处于会议通话状态
                setupPanel(activity)
                goRefreshMeetSeatInfo(true)
            }
//            setupPanel(activity)
        }
    }

    override fun onPermissionDenied() {

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