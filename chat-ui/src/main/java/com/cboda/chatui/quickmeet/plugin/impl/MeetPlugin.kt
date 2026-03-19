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
    private var waitingStateHeight = 0
    private var waitingStateFl: FrameLayout? = null
    private var waitingStateIv: ImageView? = null
    private var durationTv: TextView? = null
    private var touchHandle: ViewTouchHandle? = null
    private var coverAvatarIv: ImageView? = null
    private var mIvIconMic: ImageView? = null
    private var mTvMeetEnd: TextView? = null
    private var mVoiceWaveView: MeetVoiceWaveBroadView? = null
    private var mLlNameView: View? = null
    private var mTvUserName: TextView? = null

    private var viewModel: QuickMeetViewBroadModel? = null
    private var groupBean: GlobalChatGroupBean? = null
    private var groupId: String? = null
    private var meetChannelBean: QuickDetailMeetBean?=null


    val pluginMoveData: BusLiveBroadData<String> by lazy(LazyThreadSafetyMode.NONE) {
        LiveDataBroadBroadBus.getInstance().with("call_move_event")
    }

    init {
        callingStateHeight = activity.resources.getDimensionPixelSize(com.cboda.baselibrary.R.dimen.dp_144)
        waitingStateHeight = activity.resources.getDimensionPixelSize(com.cboda.baselibrary.R.dimen.dp_96)
        viewWidth = waitingStateHeight
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
        waitingStateFl = mView?.findViewById(R.id.chat_call_zoom_answer_state_fl)
        waitingStateIv = mView?.findViewById(R.id.chat_call_zoom_answer_state_iv)
        durationTv = mView?.findViewById(R.id.chat_voice_call_duration_tv)
        coverAvatarIv = mView?.findViewById(R.id.chat_call_zoom_avatar_iv)
        mTvMeetEnd = mView?.findViewById(R.id.mTvMeetEnd)
        mVoiceWaveView = mView?.findViewById(R.id.mVoiceWaveView)
        mIvIconMic = mView?.findViewById(R.id.mIvIconMic)
        mLlNameView = mView?.findViewById(R.id.mLlNameView)
        mTvUserName = mView?.findViewById(R.id.mTvUserName)
        mIvIconMic?.isSelected = false
        showUI()

        touchHandle = ViewTouchHandle(mView!!, activity, this)
        if (isCalling()) {
            touchHandle?.setupViewSize(viewWidth, callingStateHeight, false)
        } else {
            touchHandle?.setupViewSize(viewWidth, viewWidth, false)
        }

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
                    meetChannelBean =it.list!![0]
                    meetChannelBean?.token =it.token
                }
            }
        }

    }

     fun joinMyMeetRoom(isJoinInvoke:()-> Unit ={}) {
         goPermissionAudioMeet() {
             val userMId = MMKVBroadUtils.getUserMId()
             meetChannelBean?.let {
                 isJoinInvoke.invoke()
                 val direct = if (userMId == it.idNo.toString()) {
                     CallDirect.CALLER
                 } else {
                     CallDirect.ANSWER
                 }
                 MeetCallManager.getInstance().callMeetAction(direct, it.channelId ?: "", it.token?:"", it)
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
        if (MeetCallManager.getInstance().isCallDirect()) {
            mTvMeetEnd?.let {
                it.isVisible = !it.isVisible
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
        mLlNameView?.click {
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