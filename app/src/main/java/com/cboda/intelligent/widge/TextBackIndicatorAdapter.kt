package com.cboda.intelligent.widge

import com.cboda.intelligent.R
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.DimenRes
import com.cboda.baselibrary.utils.getAppColor
import com.cboda.baselibrary.utils.setBackColor
import com.cboda.baselibrary.view.magicindicator.buildins.UIBroadUtil
import com.cboda.baselibrary.view.magicindicator.buildins.commonnavigator.abs.CommonNavigatorBroadAdapter
import com.cboda.baselibrary.view.magicindicator.buildins.commonnavigator.abs.IPagerIndicator
import com.cboda.baselibrary.view.magicindicator.buildins.commonnavigator.abs.IPagerTitleView
import com.cboda.baselibrary.view.magicindicator.buildins.commonnavigator.indicators.BezierPagerBroadIndicator
import com.cboda.baselibrary.view.magicindicator.buildins.commonnavigator.indicators.LinePager2Indicator
import com.cboda.baselibrary.view.magicindicator.buildins.commonnavigator.indicators.LinePagerBroadIndicator
import com.cboda.baselibrary.view.magicindicator.buildins.commonnavigator.indicators.WrapPager2Indicator
import com.cboda.baselibrary.view.magicindicator.buildins.commonnavigator.indicators.WrapPagerBroadIndicator
import com.cboda.baselibrary.view.magicindicator.buildins.commonnavigator.titles.CommonPagerTitleBroadView
import com.hjq.shape.layout.ShapeConstraintLayout

class TextBackIndicatorAdapter : CommonNavigatorBroadAdapter {
    private val mTitles: IntArray?

//    private var mListener: OnIndicatorItemClickListener? = null

    @ColorInt
    private var mSelectedColor: Int

    @ColorInt
    private var mNormalColor: Int


    @ColorInt
    private var mBackSelectedColor: Int

    @ColorInt
    private var mBackNormalColor: Int

    @ColorInt
    private var mIndicatorColor: MutableList<Int>
    private val mMinScale = 1.0f
    private val textSize: Int
    private var isEnableScaleAnim = true
//    private var magicItemWidth: Float = 0f

    constructor(
        context: Context,
        titles: IntArray?,
        @DimenRes fontSize: Int = com.cboda.baselibrary.R.dimen.dp_15
    ) {
        mTitles = titles
        mBackSelectedColor = com.cboda.baselibrary.R.color.white
        mBackNormalColor = com.cboda.baselibrary.R.color.transparent
        mSelectedColor = getAppColor(context, com.cboda.baselibrary.R.color.color_3B47DD)
        mNormalColor = getAppColor(context, com.cboda.baselibrary.R.color.color_white_85)
        mIndicatorColor = ArrayList()
        mIndicatorColor.add(getAppColor(context, com.cboda.baselibrary.R.color.white))
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
        // load custom layout
        val itemLayout = LayoutInflater.from(context).inflate(
            R.layout.text_back_indicator_broad_tv,
            pagerTitleView, false
        )
//        val backLayout = itemLayout.findViewById<ShapeConstraintLayout>(R.id.textBackIndicatorLayout)
        val titleText = itemLayout.findViewById<TextView>(R.id.textBackIndicatorTitleTv)
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
                // 选中背景由 WrapPagerBroadIndicator 随滑动绘制，此处保持透明
//                backLayout.setBackColor(com.cboda.baselibrary.R.color.transparent)
//                titleText.setTypeface(Typeface.DEFAULT, Typeface.BOLD)
            }

            override fun onDeselected(index: Int, totalCount: Int) {
                if (mNormalColor != -1) {
                    titleText.setTextColor(mNormalColor)
                } else {
                    titleText.setTextColor(getAppColor(context, R.color.colorAccent))
                }
//                backLayout.setBackColor(com.cboda.baselibrary.R.color.transparent)
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
//                    val interpolatedBackColor = interpolateColor(getAppColor(context, mBackSelectedColor), getAppColor(context, mBackNormalColor), leavePercent)
//                    backLayout.setBackColor2(interpolatedBackColor)
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

    // WrapPagerBroadIndicator：在标题下方绘制圆角块，随 ViewPager 滑动在相邻 Tab 间插值移动（与 text_back_indicator_broad_tv 的 dp_8 圆角一致）
    override fun getIndicator(context: Context): IPagerIndicator {
//        val indicator = WrapPager2Indicator(context)
//        val fillColor =
//            if (mIndicatorColor.isNotEmpty()) mIndicatorColor[0] else getAppColor(
//                context,
//                com.cboda.baselibrary.R.color.white
//            )
//        indicator.fillColor = fillColor
//        indicator.setRoundRadius(UIBroadUtil.dip2px(context, 8.0).toFloat())
//        indicator.setStartInterpolator(AccelerateInterpolator())
//        indicator.setEndInterpolator(DecelerateInterpolator(2.0f))


        val indicator = LinePager2Indicator(context)
        indicator.mode = LinePager2Indicator.MODE_FULL_CELL
        indicator.fillColor = mIndicatorColor[0]
        indicator.roundRadius = UIBroadUtil.dip2px(context, 8.0).toFloat()
        indicator.setStartInterpolator(AccelerateInterpolator())
        indicator.setEndInterpolator(DecelerateInterpolator(2.0f))
        return indicator
    }
}