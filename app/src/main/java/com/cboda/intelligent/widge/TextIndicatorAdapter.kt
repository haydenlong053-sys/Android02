package com.cboda.intelligent.widge

import com.cboda.intelligent.R
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
import androidx.annotation.LayoutRes
import com.cboda.baselibrary.utils.getAppColor
import com.cboda.baselibrary.view.magicindicator.buildins.UIBroadUtil
import com.cboda.baselibrary.view.magicindicator.buildins.commonnavigator.abs.CommonNavigatorBroadAdapter
import com.cboda.baselibrary.view.magicindicator.buildins.commonnavigator.abs.IPagerIndicator
import com.cboda.baselibrary.view.magicindicator.buildins.commonnavigator.abs.IPagerTitleView
import com.cboda.baselibrary.view.magicindicator.buildins.commonnavigator.indicators.LinePagerBroadIndicator
import com.cboda.baselibrary.view.magicindicator.buildins.commonnavigator.titles.CommonPagerTitleBroadView

class TextIndicatorAdapter : CommonNavigatorBroadAdapter {
    private val mTitles: IntArray?

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
    private var mLayout = 0
    private var mLineWidth = 0.0

//    private var magicItemWidth: Float = 0f

    constructor(
        context: Context,
        titles: IntArray?,
        @DimenRes fontSize: Int = com.cboda.baselibrary.R.dimen.dp_16,
        @LayoutRes layout: Int = R.layout.text_indicator_broad_tv,
        lineWidth: Double = 32.0
    ) {
        mTitles = titles
        mLayout = layout
        mLineWidth = lineWidth
        mSelectedColor = getAppColor(context, com.cboda.baselibrary.R.color.color_3B47DD)
        mNormalColor = getAppColor(context, com.cboda.baselibrary.R.color.black_65)
        mIndicatorColor = ArrayList()
        mIndicatorColor.add(getAppColor(context, com.cboda.baselibrary.R.color.color_3B47DD))
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
        val pagerTitleView = CommonPagerTitleBroadView(context)
        // load custom layout。@LayoutRes
        val itemLayout = LayoutInflater.from(context).inflate(
            mLayout,
            pagerTitleView, false
        )
        val titleText = itemLayout.findViewById<TextView>(R.id.textIndicatorTitleTv)
//        val shapeView = itemLayout.findViewById<View>(R.id.shapeView)
//        titleText.post {
        titleText.setText(mTitles!![index])
//        }
//        titleText.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize.toFloat())
        pagerTitleView.contentView = itemLayout
        pagerTitleView.onPagerTitleChangeListener = object :
            CommonPagerTitleBroadView.OnPagerTitleChangeListener {
            override fun onSelected(index: Int, totalCount: Int) {
                if (mSelectedColor != -1) {
                    titleText.setTextColor(mSelectedColor)
                } else {
                    titleText.setTextColor(getAppColor(context, R.color.white))
                }
//                titleText.setTypeface(Typeface.DEFAULT, Typeface.BOLD)
            }

            override fun onDeselected(index: Int, totalCount: Int) {
                if (mNormalColor != -1) {
                    titleText.setTextColor(mNormalColor)
                } else {
                    titleText.setTextColor(getAppColor(context, R.color.colorAccent))
                }
//                titleText.setTypeface(Typeface.DEFAULT, Typeface.NORMAL)
//                bottomIv.imageTintList = ColorStateList.valueOf(mNormalColor)
            }

            override fun onLeave(
                index: Int,
                totalCount: Int,
                leavePercent: Float,
                leftToRight: Boolean
            ) {
                if (isEnableScaleAnim) {
//                    titleText.pivotX = (if (leftToRight) 0 else titleText.measuredWidth).toFloat()
//                    titleText.pivotY = titleText.measuredHeight * 0.5f
//                    titleText.scaleX = 1.0f + (mMinScale - 1.0f) * leavePercent
//                    titleText.scaleY = 1.0f + (mMinScale - 1.0f) * leavePercent
//                    bottomIv.pivotX = (if (leftToRight) 0 else titleText.measuredWidth).toFloat()
//                    bottomIv.pivotY = titleText.measuredHeight * 0.5f
//                    bottomIv.scaleX = 1.0f + (mMinScale - 1.0f) * leavePercent
//                    bottomIv.scaleY = 1.0f + (mMinScale - 1.0f) * leavePercent
                    val interpolatedColor =
                        interpolateColor(mSelectedColor, mNormalColor, leavePercent)
                    titleText.setTextColor(interpolatedColor)
//                    bottomIv.setColorFilter(interpolatedColor, PorterDuff.Mode.SRC_IN)
                }
            }

            override fun onEnter(
                index: Int,
                totalCount: Int,
                enterPercent: Float,
                leftToRight: Boolean
            ) {
                if (isEnableScaleAnim) {
//                    titleText.pivotX = (if (leftToRight) titleText.measuredWidth else 0).toFloat()
//                    titleText.pivotY = titleText.measuredHeight * 0.5f
//                    titleText.scaleX = mMinScale + (1.0f - mMinScale) * enterPercent
//                    titleText.scaleY = mMinScale + (1.0f - mMinScale) * enterPercent
//                    bottomIv.pivotX = (if (leftToRight) titleText.measuredWidth else 0).toFloat()
//                    bottomIv.pivotY = titleText.measuredHeight * 0.5f
//                    bottomIv.scaleX = mMinScale + (1.0f - mMinScale) * enterPercent
//                    bottomIv.scaleY = mMinScale + (1.0f - mMinScale) * enterPercent
                    val interpolatedColor =
                        interpolateColor(mNormalColor, mSelectedColor, enterPercent)
                    titleText.setTextColor(interpolatedColor)
//                    bottomIv.setColorFilter(interpolatedColor, PorterDuff.Mode.SRC_IN)
                }
            }
        }
        pagerTitleView.setOnClickListener {
            indicatorItemOnTabClick(index)
        }
        return pagerTitleView
    }

    private fun interpolateColor(from: Int, to: Int, fraction: Float): Int {
        val fromA = Color.alpha(from)
        val fromR = Color.red(from)
        val fromG = Color.green(from)
        val fromB = Color.blue(from)

        val toA = Color.alpha(to)
        val toR = Color.red(to)
        val toG = Color.green(to)
        val toB = Color.blue(to)

        return Color.argb(
            fromA + ((toA - fromA) * fraction).toInt(),
            fromR + ((toR - fromR) * fraction).toInt(),
            fromG + ((toG - fromG) * fraction).toInt(),
            fromB + ((toB - fromB) * fraction).toInt()
        )
    }

    //LinePagerBroadIndicator 当标题被选中时，标题下方出现一条直线
    override fun getIndicator(context: Context): IPagerIndicator {
        val indicator = LinePagerBroadIndicator(context)
        indicator.mode = LinePagerBroadIndicator.MODE_EXACTLY
        indicator.lineHeight = UIBroadUtil.dip2px(context, 2.0).toFloat()
        indicator.lineWidth = UIBroadUtil.dip2px(context, mLineWidth).toFloat()
        indicator.roundRadius = UIBroadUtil.dip2px(context, 2.0).toFloat()
        indicator.startInterpolator = AccelerateInterpolator()
        indicator.endInterpolator = DecelerateInterpolator(2.0f)
        indicator.colors = mIndicatorColor
        return indicator
    }

    //为了与ViewPage进行连动，需要设置一个接口，通知使用者实现被点击事件的处理
//    fun setOnIndicatorTapClickListener(listener: OnIndicatorItemClickListener) {
//        mListener = listener
//    }
//
//    interface OnIndicatorItemClickListener {
//        fun onTabClick(index: Int)
//    }
}