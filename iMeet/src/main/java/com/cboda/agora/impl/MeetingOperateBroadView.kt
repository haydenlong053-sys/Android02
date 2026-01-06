package com.cboda.agora.impl

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import com.cboda.agora.R
import com.cboda.agora.databinding.AuiRoomOperateBroadLayoutBinding
import razerdp.basepopup.BasePopupWindow
import razerdp.util.animation.AnimationHelper
import razerdp.util.animation.Direction
import razerdp.util.animation.TranslationConfig

class MeetingOperateBroadView(context: Context,isOwner: Boolean,isMute: Boolean,isMic: Boolean): BasePopupWindow(context) {

    private  var listener: OnOperateListener?=null

    private var  binding: AuiRoomOperateBroadLayoutBinding = AuiRoomOperateBroadLayoutBinding.inflate(
        LayoutInflater.from(context))

    private  var isOpenMic=isMic
    private  var isAllMute=isMute

    private  var isOwner =isOwner

    init {
        contentView = binding.root
        popupGravity = Gravity.TOP


    }

    override fun onViewCreated(contentView: View) {
        super.onViewCreated(contentView)

        binding.llMic.visibility=if (isOwner) View.VISIBLE else View.GONE
        binding.llMute.visibility=if (isOwner) View.VISIBLE else View.GONE

        switchMicState(isOpenMic)
        switchMuteState(isAllMute)

        binding.llQuite.setOnClickListener {
            listener?.quiteRoom()
            dismiss()
        }

        binding.llShare.setOnClickListener {
            listener?.shareRoom()
            dismiss()
        }

        binding.llMute.setOnClickListener {
            isAllMute = !isAllMute
            setMuteSwitchImage(isAllMute)
            dismiss()
            listener?.makeAllMute(isAllMute)
        }

        binding.llMic.setOnClickListener {
            isOpenMic = !isOpenMic
            setMicSwitchImage(isOpenMic)
            dismiss()
            listener?.openAllMic(isOpenMic)
        }

    }

    override fun onCreateShowAnimation(): Animation? {
        return AnimationHelper.asAnimation().withTranslation(TranslationConfig().from(Direction.TOP))
            .toShow()

    }

    private  fun setMicSwitchImage(isSelect: Boolean){
          binding.ivMic?.setImageResource(if (!isSelect) R.drawable.ic_voice_operate_mic_on else R.drawable.ic_voice_operate_mic_off)
          binding.tvMic?.text=if (!isSelect)"全开麦" else "全禁麦"
    }
    private  fun setMuteSwitchImage(isSelect: Boolean){
        binding.ivMute?.setImageResource(if (isSelect) R.drawable.ic_voice_operate_mute_on else R.drawable.ic_voice_operate_mute_off)
        binding.tvMute?.text=if (isSelect)"全开言" else "全禁言"
    }

    fun switchMicState(isOpen: Boolean){
        this.isOpenMic=isOpen
        setMicSwitchImage(isOpenMic)

    }

    fun switchMuteState(isMute: Boolean){
        this.isAllMute =isMute
        setMuteSwitchImage(isAllMute)
    }



    fun setOperateListener(listener: OnOperateListener){
        this.listener = listener

    }








    interface  OnOperateListener{
        fun  quiteRoom( isOwner: Boolean?=false)

        fun   shareRoom()


       open fun  makeAllMute(isMute: Boolean){}

       open fun  openAllMic(isMic: Boolean){}

    }
}