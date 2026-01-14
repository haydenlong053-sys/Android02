package com.cboda.agora.ui.ui.chatBottomBar.impl

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.AdapterView
import androidx.appcompat.widget.LinearLayoutCompat
import com.cboda.agora.ui.R
import com.cboda.agora.ui.ui.chatBottomBar.AUIChatBottomBarIcon
import com.cboda.agora.ui.databinding.AuiEmojiGridviewBroadLayoutBinding
import com.cboda.agora.ui.ui.chatBottomBar.listener.AUIExpressionClickBroadListener
import com.cboda.agora.ui.utils.DeviceBroadTools

class AUIEmojiBroadView : LinearLayoutCompat {
    private var aContext: Context? = null
    private val mColumns = 7
    private val mRoomViewBinding =
        AuiEmojiGridviewBroadLayoutBinding.inflate(LayoutInflater.from(context))
    private var gridAdapter: AUIEmojiGridBroadAdapter? = null
    private var listener: AUIExpressionClickBroadListener? = null
    private var baseLayout: Int = 0

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        this.aContext = context
        addView(mRoomViewBinding.root)

        val themeTa =
            context.obtainStyledAttributes(attrs, R.styleable.AUIEmojiBroadView, defStyleAttr, 0)
        val appearanceId =
            themeTa.getResourceId(R.styleable.AUIEmojiBroadView_aui_emojiView_appearance, 0)
        themeTa.recycle()

        initView(appearanceId)
        initListener()
    }

    fun initView(appearanceId: Int) {
        val typedArray = context.obtainStyledAttributes(appearanceId, R.styleable.AUIEmojiBroadView)
        baseLayout = typedArray.getResourceId(
            R.styleable.AUIEmojiBroadView_aui_emoji_view_background,
            R.color.voice_white_100
        )
        mRoomViewBinding.baseLayout.setBackgroundResource(baseLayout)

        mRoomViewBinding.gridview.let {
            it.verticalSpacing = DeviceBroadTools.dp2px(context, 20F)
            it.numColumns = mColumns
            it.verticalSpacing = 40
            gridAdapter = AUIEmojiGridBroadAdapter(
                context,
                1,
                mutableListOf(*AUIDefaultEmojiBroadData.getData())
            )
            it.adapter = gridAdapter
        }
    }

    private fun initListener() {
        mRoomViewBinding.ivEmojiDelete.setOnClickListener {
            listener?.onDeleteImageClicked()
        }
        mRoomViewBinding.gridview.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                val emojiIcon: AUIChatBottomBarIcon? = gridAdapter?.getItem(position)
                listener?.onExpressionClicked(emojiIcon)
            }
    }

    fun setExpressionListener(listener: AUIExpressionClickBroadListener) {
        this.listener = listener
    }

}