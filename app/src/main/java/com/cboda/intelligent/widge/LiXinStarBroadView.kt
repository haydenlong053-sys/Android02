package com.cboda.intelligent.widge

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.LinearLayoutCompat
import com.cboda.intelligent.R

/**
 * 简述： <功能描述>
 * Created by ysh on 2024/4/17.
 */
class LiXinStarBroadView : LinearLayoutCompat {
    var starCount = 0
    var totalCount = 5
    private var clickStar = false
    private var normalStar = 0
    private var pressStar = 0
    var star = 5
    private var map = HashMap<Int, AppCompatImageView>()
    private lateinit var llStarLayout:
            LinearLayoutCompat

    private fun initViews(view: View) {
        llStarLayout = view.findViewById(R.id.llStarLayout)
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
        val view = View.inflate(context, R.layout.item_star_broad_layout, null)
        addView(view)
        initViews(view)
    }

    private fun initAttributes(att: AttributeSet?) {
        val attributes = context?.obtainStyledAttributes(att, R.styleable.LiXinStarView)
        starCount= attributes?.getInt(R.styleable.LiXinStarView_starCount, 5) ?: 5
        totalCount  = attributes?.getInt(R.styleable.LiXinStarView_totalCount, 5) ?: 5
        clickStar = attributes?.getBoolean(R.styleable.LiXinStarView_clickStar, false) ?: false
        normalStar = attributes?.getResourceId(R.styleable.LiXinStarView_normalStar, 0) ?: 0
        pressStar = attributes?.getResourceId(R.styleable.LiXinStarView_pressStar, 0) ?: 0
        attributes?.recycle()
        addStar()
    }

    fun addStar() {
        llStarLayout?.removeAllViews()
        for (i in 0 until totalCount) {
            val innerView = View.inflate(context, R.layout.item_star_btn_broad_layout, null)
            val start = innerView.findViewById<AppCompatImageView>(R.id.start)
            if (i < starCount)
                start.setImageResource(pressStar)
            else
                start.setImageResource(normalStar)
            start.tag = i + 1
            map[i] = start
            llStarLayout.addView(innerView)
            start.setOnClickListener {
                if (clickStar && (start.tag is Int)) {
                    updateStar(start.tag as Int)
                    star = start.tag as Int
                }
            }
        }
    }

    fun updateStar(starLevel: Int) {
        for (i in 0 until starCount) {
            map[i]?.setImageResource(if (i < starLevel) pressStar else normalStar)
        }
    }
}