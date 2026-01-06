package com.lxkj.aiim.widge

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import com.cboda.intelligent.R
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer


/**
 * 简述：短视频播放器
 */
class LiXinStandardGSYVideoPlayer : StandardGSYVideoPlayer {
    var callBack: PlayerDoubleUpCallBack? = null

    constructor(context: Context?) : super(context)
    constructor(context: Context?, fullFlag: Boolean) : super(context, fullFlag)
    constructor(context: Context?, attrs: AttributeSet) : super(context, attrs)

    override fun getLayoutId(): Int {
        return R.layout.video_layout_standard
    }



    override fun onClickUiToggle(e: MotionEvent?) {
//        if (currentState == CURRENT_STATE_PLAYING) {
//            onVideoPause()
//            callBack?.onClick(true)
//        } else {
//            onVideoResume(false)
//            callBack?.onClick(false)
//        }
    }

    override fun touchDoubleUp(e: MotionEvent?) {
        callBack?.touchDoubleUp()
    }

    /**
     * 切换后重置播放器相关属性
     */
    fun reset(){
        callBack = null
    }

    interface PlayerDoubleUpCallBack {
        fun touchDoubleUp()
        fun onClick(isPause:Boolean)
    }
}