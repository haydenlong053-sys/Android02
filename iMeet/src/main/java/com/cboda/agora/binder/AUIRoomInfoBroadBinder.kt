package com.cboda.agora.binder

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import com.cboda.agora.ui.roomInfo.listener.AUIRoomInfoActionBroadListener
import com.cboda.agora.ui.ui.roomInfo.IAUIRoomInfoBroadView
import com.cboda.agora.impl.AUILefMeetingTopBroadView
import com.cboda.agora.service.model.AUIRoomBroadInfo

class AUIRoomInfoBinder constructor(
    roomInfoView: IAUIRoomInfoBroadView,
    auiRoomInfo: AUIRoomBroadInfo,
    listener:AUIRoomInfoEvent?
):
    IAUIBroadBindable, AUIRoomInfoActionBroadListener {
    private var auiRoomInfoView: AUILefMeetingTopBroadView? = null
    private var roomInfoView: IAUIRoomInfoBroadView? = null
    private var mMainHandler:Handler? = null
    private var auiRoomInfo:AUIRoomBroadInfo? = null
    private var listener:AUIRoomInfoEvent?

    init {
        mMainHandler = Handler(Looper.getMainLooper())
        this.roomInfoView = roomInfoView
        this.auiRoomInfoView = roomInfoView as AUILefMeetingTopBroadView
        this.auiRoomInfo = auiRoomInfo
        this.listener = listener

        // 缺少房间信息server 设置房主头像、房间标题和子标题
        auiRoomInfo.let {
//            it.owner?.userAvatar?.let { it1 -> auiRoomInfoView?.setMemberAvatar(it1) }
//            it.roomName.let { it1 ->
                auiRoomInfoView?.setVoiceTitle("${it.roomName}")
//            }
//            it.roomId.let { it1 -> auiRoomInfoView?.setVoiceSubTitle("房间ID: $it1") }
        }
    }

    override fun bind() {
        auiRoomInfoView?.setRoomInfoActionListener(this)
    }

    override fun unBind() {
        mMainHandler?.removeCallbacksAndMessages(null)
        mMainHandler = null
        roomInfoView?.setRoomInfoActionListener(null)
    }

    private fun runOnUiThread(runnable: Runnable) {
        if (mMainHandler != null) {
            if (mMainHandler?.looper?.thread === Thread.currentThread()) {
                runnable.run()
            } else {
                mMainHandler?.post(runnable)
            }
        }
    }

    override fun onClickUpperLeftAvatar(view: View) {
        Log.d("AUIRoomInfoBinder","onClickUpperLeftAvatar")
    }

    override fun onLongClickUpperLeftAvatar(view: View): Boolean {
        Log.d("AUIRoomInfoBinder","onLongClickUpperLeftAvatar")
        return true
    }

    override fun onUpperLeftRightIconClickListener(view: View) {
        Log.d("AUIRoomInfoBinder","onUpperLeftRightIconClickListener")
    }

    override fun onBackClickListener(view: View) {
        listener?.onBackClickListener(view)
    }

    interface AUIRoomInfoEvent{
        fun onBackClickListener(view: View){}
    }

}