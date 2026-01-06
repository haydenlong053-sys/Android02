package com.lxkj.aiim.widge

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.customview.widget.ViewDragHelper
import com.cboda.intelligent.R
import com.lxkj.aiim.utils.PixelBroadUtils

/**
 * 简述： <功能描述>
 * Created by ysh on 2023/7/11.
 */
class MoveView : ConstraintLayout {

    private var viewDragHelper: ViewDragHelper? = null
    private var floatBtnView: View? = null
    private var mLastX = -1f
    private var mLastY = -1f

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initViewDragHelper(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context, attrs,
        defStyleAttr
    ) {
        initViewDragHelper(context, attrs)
    }

    private fun initViewDragHelper(context: Context, attrs: AttributeSet) {
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.SlideView)
        attributes.recycle()
        viewDragHelper = ViewDragHelper.create(this, 1.0f, object : ViewDragHelper.Callback() {
            override fun tryCaptureView(child: View, pointerId: Int): Boolean {
                return child == floatBtnView
            }

            override fun clampViewPositionHorizontal(child: View, left: Int, dx: Int): Int {
                var slideLeft = left
                if (slideLeft > PixelBroadUtils.getScreenWidth(context) - child.measuredWidth - PixelBroadUtils.dip2px(
                        context,
                        10f
                    )
                ) {
                    slideLeft =
                        PixelBroadUtils.getScreenWidth(context) - child.measuredWidth - PixelBroadUtils.dip2px(
                            context,
                            10f
                        )
                } else if (slideLeft < PixelBroadUtils.dip2px(context, 10f)) {
                    slideLeft = PixelBroadUtils.dip2px(context, 10f)
                }
                return slideLeft
            }

            override fun getViewHorizontalDragRange(child: View): Int {
                return PixelBroadUtils.getScreenWidth(context) - child.measuredWidth - PixelBroadUtils.dip2px(
                    context,
                    10f
                )
            }

            override fun clampViewPositionVertical(child: View, top: Int, dy: Int): Int {
                var slideTop = top
                if (slideTop > PixelBroadUtils.getScreenHeight(context) - child.measuredHeight
                ) {
                    slideTop =
                        PixelBroadUtils.getScreenHeight(context) - child.measuredHeight
                } else if (slideTop < PixelBroadUtils.dip2px(context, 50f)) {
                    slideTop = PixelBroadUtils.dip2px(context, 50f)
                }
                return slideTop
            }

            override fun getViewVerticalDragRange(child: View): Int {
                return PixelBroadUtils.getScreenHeight(context) - child.measuredHeight
            }

            override fun onViewPositionChanged(
                changedView: View,
                left: Int,
                top: Int,
                dx: Int,
                dy: Int
            ) {
                super.onViewPositionChanged(changedView, left, top, dx, dy)
                if (changedView == floatBtnView) {
                    mLastX = changedView.x
                    mLastY = changedView.y
                }
            }

            override fun onViewReleased(releasedChild: View, xvel: Float, yvel: Float) {
            }


        })
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        for (i in 0 until childCount){
            val childView = getChildAt(i)
//            if (childView.id == R.id.roomPkLayout){
//                floatBtnView = getChildAt(i)
//                break
//            }
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return viewDragHelper?.shouldInterceptTouchEvent(ev) ?: false
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        viewDragHelper?.processTouchEvent(event)
        return true
    }

    override fun computeScroll() {
        super.computeScroll()
        if (viewDragHelper?.continueSettling(true) == true) {
            invalidate()
        } else {
            if (mLastX != -1f && mLastY != -1f){
                floatBtnView?.layout(mLastX.toInt(),mLastY.toInt(),(mLastX +floatBtnView?.measuredWidth!!).toInt(),(mLastY +floatBtnView?.measuredHeight!!).toInt() )
            }
        }
    }

    interface OnMoveListener {
        fun onMove()
    }
}