package com.lxkj.aiim.widge

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.isVisible
import com.cboda.baselibrary.shapeview.shape.ShapeTextBroadView
import com.cboda.intelligent.R

/**
 * 简述：底部导航item
 * Created by ysh on 2023/3/17.
 */
class LiXinBottomItemNavigation : LinearLayoutCompat {
    private var normalIcon = 0
    private var pressIcon = 0
    private var normalColor = 0
    private var pressColor = 0
    private var itemTitle = ""
    private var showIcon = true
    private var showText = true
    private lateinit var tvTitle: TextView
    private lateinit var tvRedNum: ShapeTextBroadView
    private lateinit var icon: AppCompatImageView

    private fun initViews(view: View) {
        icon = view.findViewById(R.id.icon)
        tvRedNum = view.findViewById(R.id.tvRedNum)
        tvTitle = view.findViewById(R.id.tvTitle)
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initAttributes(attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context, attrs,
        defStyleAttr
    ) {
        initAttributes(attrs)
    }

    init {
        val view = View.inflate(context, R.layout.bottom_item_navigation_layout, null)
        addView(view)
        initViews(view)
    }

    private fun initAttributes(att: AttributeSet?) {
        val attributes = context?.obtainStyledAttributes(att, R.styleable.LiXinBottomItemNavigation)
        showIcon =
            attributes?.getBoolean(R.styleable.LiXinBottomItemNavigation_showIcon, true) ?: true
        showText =
            attributes?.getBoolean(R.styleable.LiXinBottomItemNavigation_showText, true) ?: true
        itemTitle = attributes?.getString(R.styleable.LiXinBottomItemNavigation_itemTitle) ?: ""
        normalIcon =
            attributes?.getResourceId(R.styleable.LiXinBottomItemNavigation_normalIcon, 0) ?: 0
        pressIcon =
            attributes?.getResourceId(R.styleable.LiXinBottomItemNavigation_pressIcon, 0) ?: 0

        normalColor =
            attributes?.getColor(
                R.styleable.LiXinBottomItemNavigation_normalColor,
                Color.parseColor("#333333")
            ) ?: Color.parseColor("#333333")
        pressColor =
            attributes?.getColor(
                R.styleable.LiXinBottomItemNavigation_pressColor,
                Color.parseColor("#333333")
            ) ?: Color.parseColor("#333333")

        if (showIcon) {
            icon?.setImageResource(normalIcon)
        }

        if (showText) {
            tvTitle?.setTextColor(normalColor)
            tvTitle?.text = itemTitle
        }

        attributes?.recycle()
    }

    fun press() {
        if (showIcon) {
            icon?.setImageResource(pressIcon)
        }
        if (showText) {
            tvTitle?.setTextColor(pressColor)
        }

    }

    fun normal() {
        if (showIcon) {
            icon?.setImageResource(normalIcon)
        }

        if (showText) {
            tvTitle?.setTextColor(normalColor)
        }
    }

    fun number(number:String = "") {
        tvRedNum?.isVisible = number.isNotEmpty()
        tvRedNum?.text = number?:""
    }

}