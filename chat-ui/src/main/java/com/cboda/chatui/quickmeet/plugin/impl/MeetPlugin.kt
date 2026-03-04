package com.cboda.chatui.quickmeet.plugin.impl

import android.graphics.drawable.AnimationDrawable
import android.text.TextUtils
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import com.blankj.utilcode.util.ThreadUtils.runOnUiThread
import com.cboda.baselibrary.app.UtilBroadApplication
import com.cboda.baselibrary.utils.BusLiveBroadData
import com.cboda.baselibrary.utils.LiveDataBroadBroadBus
import com.cboda.baselibrary.utils.click
import com.cboda.baselibrary.utils.eLogD
import com.cboda.chatui.R
import com.cboda.chatui.quickmeet.CallEventCallback
import com.cboda.chatui.quickmeet.CallManager
import com.cboda.chatui.quickmeet.bean.CallData
import com.cboda.chatui.quickmeet.plugin.PluginLayout
import com.cboda.chatui.quickmeet.view.OnTouchHandleEvent
import com.cboda.chatui.quickmeet.view.ViewTouchHandle

/**
* 会议最小化显示界面
*/
class MeetPlugin(val activity: FragmentActivity) : PluginLayout(activity), CallEventCallback, OnTouchHandleEvent{
    private var callingStateHeight = 0
    private var viewWidth = 0
    private var waitingStateHeight = 0
    private var waitingStateFl: FrameLayout? = null
    private var waitingStateIv: ImageView? = null
    private var durationTv: TextView? = null
    private var touchHandle: ViewTouchHandle? = null
    private var coverAvatarIv: ImageView? = null
    private var mTvMeetEnd: TextView? = null

    private var mIsRegister = false


    val pluginMoveData: BusLiveBroadData<String> by lazy(LazyThreadSafetyMode.NONE) {
        LiveDataBroadBroadBus.getInstance().with("call_move_event")
    }

    init {
        callingStateHeight = activity.resources.getDimensionPixelSize(com.cboda.baselibrary.R.dimen.dp_144)
        waitingStateHeight = activity.resources.getDimensionPixelSize(com.cboda.baselibrary.R.dimen.dp_96)
        viewWidth = waitingStateHeight
        CallManager.getInstance().registerEngineEvent(this)
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

    private fun registerListener() {
        if (!mIsRegister) {
            mIsRegister = true
        }
    }

    private fun unregisterListener() {
        if (mIsRegister) {
            mIsRegister = false
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
        registerListener()
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
        mTvMeetEnd?.let {
            it.isVisible =!it.isVisible
        }

    }

    override fun updateLocation(translationX: Float, translationY: Float) {
        super.updateLocation(translationX, translationY)
        if (mIsResume) {
            pluginMoveData.value = ("$translationX,$translationY")
        }
    }

    private fun showUI() {
//        val layoutParams = mView?.layoutParams
//        layoutParams?.let { lp ->
//            lp.height = if (isCalling() && isVideoCall()) callingStateHeight else waitingStateHeight
//        }
//        mView?.layoutParams = layoutParams

//        if (isCalling()) {
//            waitingStateIv?.setImageDrawable(null)
//            if (isVideoCall()) {
//                waitingStateFl?.visibility = View.GONE
//                showOrHideCover(true )
//            } else {
//                //语音通话， 获取通话时长
////                CallManager.getInstance().getCallTimerObserver().value?.let { time ->
////                    durationTv?.text = getCallingDuration(time)
////                }
//                waitingStateFl?.visibility = View.VISIBLE
//                durationTv?.visibility = View.VISIBLE
//            }
//        } else {
//            waitingStateFl?.visibility = View.VISIBLE
//        }
        mTvMeetEnd?.click {
            CallManager.getInstance().leaveAndRelease()
        }
        showOrHideCover(true )
    }

    /**
     * 会议中
     * @return Boolean
     */
    private fun isCalling(): Boolean {
        return CallManager.getInstance().isCallingState()
    }


    private fun showOrHideCover(show: Boolean, cameraChange: Boolean = false) {
        if (show) {
            coverAvatarIv?.visibility = View.VISIBLE
        } else {
            coverAvatarIv?.visibility = View.GONE
        }
    }


    override fun onEngineDestroy( data: CallData) {
        eLogD("zhanglong", "mIsLayout==>$mIsLayout")
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
        showMeetMiniLayoutView()
        Toast.makeText(UtilBroadApplication.cxt.applicationContext, "Join channel success", Toast.LENGTH_SHORT).show()

    }

    override fun onRtcUserJoined(uid: Int) {
        super.onRtcUserJoined(uid)
        //对方加入频道
        eLogD("call", "onRtcUserJoined uid = $uid; mIsLayout=$mIsLayout; isResume=$mIsResume")
        runOnUiThread(Runnable {
            // 获取 uid 后，设置远端视频视图
            // After obtaining the UID, set the remote video view.
            Toast.makeText(UtilBroadApplication.cxt.applicationContext, "User joined: " + uid, Toast.LENGTH_SHORT).show()
        })
        if (isCalling()) {
            if (mIsLayout) {
                showUI()
                if (mIsResume) {

                }
            }
        }
    }

    private fun removePanel() {
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
            if (!CallManager.getInstance().isIdleState()) {
                //当前处于通话状态
                setupPanel(activity)
            }
//            setupPanel(activity)
        }
    }

    override fun onPermissionDenied(){

    }


    override fun onPause() {
        super.onPause()
        if (mIsLayout
            && !CallManager.getInstance().isIdleState()
        ) {
            //暂停等待接听动画
            if (CallManager.getInstance().isWaitingState()) {
                val animationDrawable = waitingStateIv?.drawable as? AnimationDrawable
                animationDrawable?.stop()
            }
            mView?.visibility = View.INVISIBLE
        }
    }



    override fun onDestroy() {
        super.onDestroy()
        unregisterListener()
        eLogD("onDestroy==========>")
        CallManager.getInstance().unregisterEngineEvent(this)
        CallManager.getInstance().leaveAndRelease()
        removePanel()
        touchHandle = null
    }
}