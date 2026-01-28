package com.cboda.intelligent.widge

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.DimenRes
import androidx.appcompat.widget.AppCompatImageView
import com.cboda.baselibrary.utils.getAppColor
import com.cboda.baselibrary.view.magicindicator.buildins.UIBroadUtil
import com.cboda.baselibrary.view.magicindicator.buildins.commonnavigator.abs.CommonNavigatorBroadAdapter
import com.cboda.baselibrary.view.magicindicator.buildins.commonnavigator.abs.IPagerIndicator
import com.cboda.baselibrary.view.magicindicator.buildins.commonnavigator.abs.IPagerTitleView
import com.cboda.baselibrary.view.magicindicator.buildins.commonnavigator.indicators.LinePagerBroadIndicator

class BadgeIndicatorAdapter : CommonNavigatorBroadAdapter {
    private val mTitles: IntArray?
    private val badgeNums: IntArray?
//    private var mListener: OnIndicatorItemClickListener? = null

    @ColorInt
    private var mSelectedColor: Int

    @ColorInt
    private var mNormalColor: Int

    @ColorInt
    private var mIndicatorColor: MutableList<Int>
    private val mMinScale = 1.0f
    private val textSize: Int
    private var isEnableScaleAnim = true
//    private var magicItemWidth: Float = 0f

    constructor(
        context: Context,
        titles: IntArray?,
        mBadges: IntArray?,
        @DimenRes fontSize: Int = com.cboda.baselibrary.R.dimen.dp_16
    ) {
        mTitles = titles
        badgeNums = mBadges
        mSelectedColor = getAppColor(context, com.cboda.baselibrary.R.color.color_2563EB)
        mNormalColor = getAppColor(context, com.cboda.baselibrary.R.color.color_94A3B8)
        mIndicatorColor = ArrayList()
        mIndicatorColor.add(getAppColor(context, com.cboda.baselibrary.R.color.color_2563EB))
        textSize = context.resources.getDimensionPixelSize(fontSize)
    }


    fun setColor(
        @ColorInt selectedColor: Int, @ColorInt normalColor: Int,
        @ColorInt indicatorColor: MutableList<Int>
    ) {
        mSelectedColor = selectedColor
        mNormalColor = normalColor
        mIndicatorColor = indicatorColor
    }

    fun setColorIndicatorColor(indicatorColor: MutableList<Int>) {
        mIndicatorColor = indicatorColor
    }

    override fun getCount(): Int {
        return mTitles?.size ?: 0
    }

    override fun getTitleView(context: Context, index: Int): IPagerTitleView {
        val pagerTitleView = CustomBadgePagerTitleView(context)
        pagerTitleView.setText(mTitles!![index])
        pagerTitleView.setBadgeCount(badgeNums!![index])
        pagerTitleView.setOnClickListener {
            indicatorItemOnTabClick(index)
        }
        return pagerTitleView
    }

    //LinePagerBroadIndicator 当标题被选中时，标题下方出现一条直线
    override fun getIndicator(context: Context): IPagerIndicator {
        val indicator = LinePagerBroadIndicator(context)
        indicator.mode = LinePagerBroadIndicator.MODE_EXACTLY
        indicator.lineHeight = UIBroadUtil.dip2px(context, 2.0).toFloat()
        indicator.lineWidth = UIBroadUtil.dip2px(context, 15.0).toFloat()
        indicator.roundRadius = UIBroadUtil.dip2px(context, 2.0).toFloat()
        indicator.startInterpolator = AccelerateInterpolator()
        indicator.endInterpolator = DecelerateInterpolator(2.0f)
        indicator.colors = mIndicatorColor
        return indicator
    }

}