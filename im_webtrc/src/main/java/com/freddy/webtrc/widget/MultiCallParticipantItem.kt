package com.freddy.webtrc.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.freddy.webtrc.R

/** 多人语音通话成员格子，对齐野火 {@code VoipCallBroadItem} 音频态。 */
class MultiCallParticipantItem @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    val portraitImageView: ImageView
    val statusTextView: TextView
    val userNameTv: TextView

    init {
        LayoutInflater.from(context).inflate(R.layout.item_multi_call_participant, this, true)
        portraitImageView = findViewById(R.id.portraitImageView)
        statusTextView = findViewById(R.id.statusTextView)
        userNameTv = findViewById(R.id.userNameTv)
    }
}
