package com.lxkj.aiim.widge

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.LinearLayoutCompat
import com.lxkj.aiim.R
import com.lxkj.aiim.utils.PixelUtils

/**
 * 简述：底部导航
 * Created by ysh on 2023/3/17.
 */
class LiXinBottomNavigation : LinearLayoutCompat {

    private var mClickPosition = THIRD_INDEX
    var itemClickListener: OnItemClickListener? = null
    private lateinit var fiveIndex: LiXinBottomItemNavigation
    private lateinit var fouthIndex: LiXinBottomItemNavigation
    private lateinit var firstIndex: LiXinBottomItemNavigation
    private lateinit var secondIndex: LiXinBottomItemNavigation
    private lateinit var thirdIndex:
            LiXinBottomItemNavigation

    private fun initViews(view: View) {
        thirdIndex = view.findViewById(R.id.thirdIndex)
        secondIndex = view.findViewById(R.id.secondIndex)
        firstIndex = view.findViewById(R.id.firstIndex)
        fouthIndex = view.findViewById(R.id.fouthIndex)
        fiveIndex = view.findViewById(R.id.fiveIndex)
    }

    companion object {
        const val FIRST_INDEX = 0
        const val SEC_INDEX = 1
        const val THIRD_INDEX = 2
        const val FOUTH_INDEX = 3
        const val FIVE_INDEX = 4
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context, attrs,
        defStyleAttr
    )

    init {
        val screenWidth = PixelUtils.getScreenWidth(context)
        val view = View.inflate(context, R.layout.bottom_navigation_layout, null)
        addView(view)
        initViews(view)
        firstIndex?.press()
        thirdIndex?.setOnClickListener {
            if (mClickPosition != FIRST_INDEX) {
                mClickPosition = FIRST_INDEX
                clickIndex()
            }

        }

        secondIndex?.setOnClickListener {
            if (mClickPosition != SEC_INDEX) {
                mClickPosition = SEC_INDEX
                clickIndex()
            }

        }

        firstIndex?.setOnClickListener {
            if (mClickPosition != THIRD_INDEX) {
                mClickPosition = THIRD_INDEX
                clickIndex()
            }

        }

        fouthIndex?.setOnClickListener {
            if (mClickPosition != FOUTH_INDEX) {
                mClickPosition = FOUTH_INDEX
                clickIndex()
            }

        }

        fiveIndex?.setOnClickListener {
            if (mClickPosition != FIVE_INDEX) {
                mClickPosition = FIVE_INDEX
                clickIndex()
            }

        }


        firstIndex?.layoutParams = LayoutParams(screenWidth / 5, LayoutParams.WRAP_CONTENT)
        secondIndex?.layoutParams = LayoutParams(screenWidth / 5, LayoutParams.WRAP_CONTENT)
        thirdIndex?.layoutParams = LayoutParams(screenWidth / 5, LayoutParams.WRAP_CONTENT)
        fouthIndex?.layoutParams = LayoutParams(screenWidth / 5, LayoutParams.WRAP_CONTENT)
        fiveIndex?.layoutParams = LayoutParams(screenWidth / 5, LayoutParams.WRAP_CONTENT)
    }

    private fun clickIndex() {
        firstIndex?.normal()
        secondIndex?.normal()
        thirdIndex?.normal()
        fouthIndex?.normal()
        fiveIndex?.normal()
        when (mClickPosition) {
            FIRST_INDEX -> {
                thirdIndex?.press()
            }
            SEC_INDEX -> {
                secondIndex?.press()
            }
            THIRD_INDEX -> {
                firstIndex?.press()
            }
            FOUTH_INDEX -> {
                fouthIndex?.press()
            }
            FIVE_INDEX -> {
                fiveIndex?.press()
            }
        }

        itemClickListener?.onItemClick(mClickPosition)
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }
    fun setNum (num:String){
        fouthIndex?.number(num)
    }
}