package com.example.asceneskit.impl

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.asceneskit.databinding.AuiRoomLeftLayoutBinding
import io.agora.auikit.ui.roomInfo.IAUIRoomInfoView
import io.agora.auikit.ui.roomInfo.listener.AUIRoomInfoActionListener

class AUILefMeetingTopBroadView: ConstraintLayout, IAUIRoomInfoView {
    private val aRoomViewBinding = AuiRoomLeftLayoutBinding.inflate(LayoutInflater.from(context))
    private var aUpperLeftListener: AUIRoomInfoActionListener? = null
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

    override fun setRoomInfoActionListener(listener: AUIRoomInfoActionListener?) {
        this.aUpperLeftListener=listener
    }
}