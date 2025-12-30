package com.lxkj.aiim.widge

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup

/**
 * 简述：流式布局
 */
class FlowLayout : ViewGroup {
    /**
     * 存储所有的View，按行记录
     */
    private val mAllViews = ArrayList<ArrayList<View>>()

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun generateLayoutParams(
        p: LayoutParams?
    ): LayoutParams {
        return MarginLayoutParams(p)
    }

    override fun generateLayoutParams(attrs: AttributeSet?): LayoutParams {
        return MarginLayoutParams(context, attrs)
    }

    override fun generateDefaultLayoutParams(): LayoutParams {
        return MarginLayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.MATCH_PARENT
        )
    }

    /**
     * 负责设置子控件的测量模式和大小 根据所有子控件设置自己的宽和高
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        // 获得它的父容器为它设置的测量模式和大小
        val sizeWidth = MeasureSpec.getSize(widthMeasureSpec)
        val sizeHeight = MeasureSpec.getSize(heightMeasureSpec)
        val modeWidth = MeasureSpec.getMode(widthMeasureSpec)
        val modeHeight = MeasureSpec.getMode(heightMeasureSpec)
        // 如果是warp_content情况下，记录宽和高
        var width = 0
        var height = 0

        /**
         * 记录每一行的宽度，width不断取最大宽度
         */
        var lineWidth = 0

        /**
         * 每一行的高度，累加至height
         */
        var lineHeight = 0
        val cCount = childCount

        // 遍历每个子元素
        for (i in 0 until cCount) {
            val child: View = getChildAt(i)
            // 测量每一个child的宽和高
            measureChild(child, widthMeasureSpec, heightMeasureSpec)
            // 得到child的lp
            val lp = child
                .layoutParams as MarginLayoutParams
            // 当前子空间实际占据的宽度
            val childWidth: Int = (child.measuredWidth + lp.leftMargin
                    + lp.rightMargin)
            // 当前子空间实际占据的高度
            val childHeight: Int = (child.measuredHeight + lp.topMargin
                    + lp.bottomMargin)
            /**
             * 如果加入当前child，则超出最大宽度，则的到目前最大宽度给width，类加height 然后开启新行
             */
            if (lineWidth + childWidth > sizeWidth) {
                width = lineWidth.coerceAtLeast(childWidth) // 取最大的
                lineWidth = childWidth // 重新开启新行，开始记录
                // 叠加当前高度，
                height += lineHeight
                // 开启记录下一行的高度
                lineHeight = childHeight
            } else  // 否则累加值lineWidth,lineHeight取最大高度
            {
                lineWidth += childWidth
                lineHeight = lineHeight.coerceAtLeast(childHeight)
            }
            // 如果是最后一个，则将当前记录的最大宽度和当前lineWidth做比较
            if (i == cCount - 1) {
                width = width.coerceAtLeast(lineWidth)
                height += lineHeight
            }
        }
        setMeasuredDimension(
            if (modeWidth == MeasureSpec.EXACTLY) sizeWidth else width,
            if (modeHeight == MeasureSpec.EXACTLY) sizeHeight else height
        )
    }

    /**
     * 记录每一行的最大高度
     */
    private val mLineHeight: MutableList<Int> = ArrayList()
    @SuppressLint("DrawAllocation")
    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        mAllViews.clear()
        mLineHeight.clear()
        val width = width
        var lineWidth = 0
        var lineHeight = 0
        // 存储每一行所有的childView
        var lineViews = ArrayList<View>()
        val cCount = childCount
        // 遍历所有的孩子
        for (i in 0 until cCount) {
            val child = getChildAt(i)
            val lp = child
                .layoutParams as MarginLayoutParams
            val childWidth = child.measuredWidth
            val childHeight = child.measuredHeight

            // 如果已经需要换行
            if (childWidth + lp.leftMargin + lp.rightMargin + lineWidth > width) {
                // 记录这一行所有的View以及最大高度
                mLineHeight.add(lineHeight)
                // 将当前行的childView保存，然后开启新的ArrayList保存下一行的childView
                mAllViews.add(lineViews)
                lineWidth = 0 // 重置行宽
                lineViews = ArrayList()
            }
            /**
             * 如果不需要换行，则累加
             */
            lineWidth += childWidth + lp.leftMargin + lp.rightMargin
            lineHeight = lineHeight.coerceAtLeast(
                childHeight + lp.topMargin
                        + lp.bottomMargin
            )
            lineViews.add(child)
        }
        // 记录最后一行
        mLineHeight.add(lineHeight)
        mAllViews.add(lineViews)
        var left = 0
        var top = 0
        // 得到总行数
        val lineNums = mAllViews.size

        for (i in 0 until lineNums) {
            // 每一行的所有的views
            lineViews = mAllViews[i]
            // 当前行的最大高度
            lineHeight = mLineHeight[i]

            // 遍历当前行所有的View
            for (j in lineViews.indices) {
                val child: View = lineViews[j]
                if (child.visibility == GONE) {
                    continue
                }
                val lp = child
                    .layoutParams as MarginLayoutParams

                //计算childView的left,top,right,bottom
                val lc = left + lp.leftMargin
                val tc = top + lp.topMargin
                val rc = lc + child.measuredWidth
                val bc = tc + child.measuredHeight

                child.layout(lc, tc, rc, bc)
                left += (child.measuredWidth + lp.rightMargin
                        + lp.leftMargin)
            }
            left = 0
            top += lineHeight
        }

    }
}