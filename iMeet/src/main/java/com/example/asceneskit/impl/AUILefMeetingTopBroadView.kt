package com.example.asceneskit.impl

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.asceneskit.databinding.AuiRoomLeftBroadLayoutBinding
import io.agora.auikit.ui.roomInfo.IAUIRoomInfoBroadView
import io.agora.auikit.ui.roomInfo.listener.AUIRoomInfoActionBroadListener

class AUILefMeetingTopBroadView: ConstraintLayout, IAUIRoomInfoBroadView {
    private val aRoomViewBinding = AuiRoomLeftBroadLayoutBinding.inflate(LayoutInflater.from(context))
    private var aUpperLeftListener: AUIRoomInfoActionBroadListener? = null
    private var aContext: Context? = null

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        this.aContext = context
        addView(aRoomViewBinding.root)
        initListener()
    }

    private  fun initListener(){
        aRoomViewBinding.backIcon.setOnClickListener {
            aUpperLeftListener?.onBackClickListener(it)
        }

    }

    override fun setVoiceTitle(title: String) {
        aRoomViewBinding.tvRoomTitle.text = title

    }

    override fun setRoomInfoActionListener(listener: AUIRoomInfoActionBroadListener?) {
        this.aUpperLeftListener=listener
    }
}