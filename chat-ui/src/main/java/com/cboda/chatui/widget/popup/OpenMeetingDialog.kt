package com.cboda.chatui.widget.popup

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import com.cboda.chatui.R
import com.cboda.chatui.bridge.ScreenUtil
import com.cboda.chatui.databinding.DialogMeetingPublishBinding
import razerdp.basepopup.BasePopupWindow

class OpenMeetingDialog(context: Context,title:String="",openMic: Boolean=true,openMute: Boolean=false,
    openClick: (openMic: Boolean, openMute: Boolean,meetingTitle: String) -> Unit?): BasePopupWindow(context) {

    private var openMic: Boolean = openMic
    private var openMute: Boolean = openMute
    private var openClick: (openMic: Boolean, openMute: Boolean,meetingTitle: String) -> Unit? = openClick

    private var meetingTitle: String = title
    private var  binding: DialogMeetingPublishBinding = DialogMeetingPublishBinding.inflate(LayoutInflater.from(context))

    init {
        contentView = binding.root
        setWidth(ScreenUtil.getScreenWidth(context)-ScreenUtil.dp2px(64f,context.resources.displayMetrics).toInt())
        setPopupGravity(Gravity.CENTER)

    }

    override fun onViewCreated(contentView: View) {
        super.onViewCreated(contentView)
        if (meetingTitle.isNotEmpty()){
            binding.etMeetingTitle.setText(meetingTitle)
        }
        switchState(binding.ivSwitchMic, openMic)
        switchState(binding.ivSwitchMute, openMute)

        binding.ivSwitchMic.setOnClickListener {
            openMic = !openMic
            switchState(binding.ivSwitchMic, openMic)
        }

        binding.ivSwitchMute.setOnClickListener {
            openMute = !openMute
            switchState(binding.ivSwitchMute, openMute)
        }

        binding.btnOpen.setOnClickListener {
            openClick?.invoke(openMic, openMute,binding.etMeetingTitle.text.trim().toString())
            dismiss()
        }



    }

    private  fun switchState(switchView: ImageView, open: Boolean) {
         switchView.setImageResource(if (open) R.mipmap.ic_switch_kai else R.mipmap.ic_switch_guan)
    }

}