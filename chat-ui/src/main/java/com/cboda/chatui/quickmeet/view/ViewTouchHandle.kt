package com.cboda.chatui.quickmeet.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.graphics.Point
import android.graphics.PointF
import android.os.Build
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewConfiguration
import androidx.core.view.GestureDetectorCompat
import androidx.fragment.app.FragmentActivity
import com.cboda.baselibrary.common.PlatformParams
import com.cboda.baselibrary.utils.eLogD
import com.cboda.chatui.R
import com.cboda.chatui.third.utils.UIBroadUtils

//view移动效果
class ViewTouchHandle constructor(
    private val mView: View,
    activity: FragmentActivity,
    private val mEvent: OnTouchHandleEvent?,
    var statusBarTrans: Boolean = true
) : OnTouchListener {
    private val mGestureDetectorCompat: GestureDetectorCompat
    private val minTouchVal: Int
    private var mDownX = 0f
    private var mDownY = 0f
    private val statusBarHeight: Int
    val screenWidth: Int
    val screenHeight: Int
    private var viewWidth = 0
    private var viewHeight = 0
    private val mNavigationBarHeight: Int
    private var disabledTouch = false
    private var enabledClick = false
    private var endMargin: Int
    private var mBottomMargin: Int
    var isAnimStart = false
        private set

    init {
        val context = mView.context
        statusBarHeight = UIBroadUtils.getStatusBarHeight(context)
        screenWidth = UIBroadUtils.getScreenWidth(context)
        mNavigationBarHeight = UIBroadUtils.getNavBarHeight(context)
        val point = Point()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            activity.display!!.getRealSize(point)
        } else {
            activity.windowManager.defaultDisplay.getRealSize(point)
        }
        setupViewSize()
        mGestureDetectorCompat = GestureDetectorCompat(context,
            object : SimpleOnGestureListener() {
                override fun onSingleTapUp(e: MotionEvent): Boolean {
                    mEvent?.onItemClick(e)
                    return true
                }
            })
        minTouchVal = ViewConfiguration.get(context).scaledTouchSlop / 2
        mView.setOnTouchListener(this)
        mBottomMargin = context.resources.getDimensionPixelSize(com.cboda.baselibrary.R.dimen.dp_80)
        endMargin = 0
        val bottomMargin = if (statusBarTrans) 0 else mBottomMargin
        screenHeight = point.y  - bottomMargin
        eLogD("call", "statusBarHeight==$statusBarHeight" +
                "; mNavigationBarHeight=$mNavigationBarHeight ； mBottomMargin =$mBottomMargin")

        eLogD("call", "init screenWidth=$screenWidth" +
                "; screenHeight=$screenHeight")
    }

    fun setupViewSize(width: Int, height: Int, needMeasure: Boolean = true) {
        if (needMeasure) {
            mView.measure(0, 0)
        }
        viewWidth = width
        viewHeight = height
        eLogD("call", "setupViewSize viewWidth=${viewWidth} ; viewHeight=${viewHeight}")
    }

    fun setupViewSize() {
        mView.measure(0, 0)
        viewWidth = mView.measuredWidth
        viewHeight = mView.measuredHeight
        eLogD("call", "setupViewSize viewWidth=${viewWidth} ; viewHeight=${viewHeight}")
    }

    fun setEndMargin(endMargin: Int) {
        this.endMargin = endMargin
    }

    fun setDisabledTouch(disableTouch: Boolean) {
        disabledTouch = disableTouch
    }
    fun setEnabledClick(enabledClick: Boolean) {
        this.enabledClick = enabledClick
    }

    fun setBottomMargin(bottomMargin: Int) {
        mBottomMargin = bottomMargin
    }

    private var diffMoveX = 0f
    private var diffMoveY = 0f

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View, event: MotionEvent): Boolean {
        if (enabledClick){
            mGestureDetectorCompat.onTouchEvent(event)
            return true
        }
        if (disabledTouch || isAnimStart) {
            return false
        }
        mGestureDetectorCompat.onTouchEvent(event)
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mDownX = event.x
                mDownY = event.y
                mView.parent.requestDisallowInterceptTouchEvent(true)
            }
            MotionEvent.ACTION_MOVE -> {
                if (!isAnimStart) {
                    diffMoveX = event.rawX - mDownX
                    diffMoveY = event.rawY - mDownY
                    if ((event.x - mDownX).toInt() == 0 && (event.y - mDownY).toInt() == 0) {
                        return true
                    }
                    updateLocation(diffMoveX, diffMoveY, true)
                }
            }
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                moveEnd(false)
                diffMoveX = 0f
                diffMoveY = 0f
            }
        }
        return true
    }

    fun moveEnd(exit: Boolean) {
        mView.parent.requestDisallowInterceptTouchEvent(false)
        if (diffMoveY > minTouchVal || diffMoveX > minTouchVal) {
            val halfW = (screenWidth - viewWidth) / 2
            eLogD("call","halfW==$halfW; diffMoveX=$diffMoveX")
            if (diffMoveX < halfW) {
                if (exit) {
                    mView.translationX = endMargin.toFloat()
                    updateViewLocation()
                } else {
                    startAnimation(endMargin.toFloat())
                }
            } else {
                if (exit) {
                    mView.translationX = (screenWidth - viewWidth - endMargin).toFloat()
                    updateViewLocation()
                } else {
                    startAnimation((screenWidth - viewWidth - endMargin).toFloat())
                }
            }
        }
    }

    fun startAnimation(x: Float) {
        val translationX = mView.translationX
        val duration = 300L
        if (translationX != x) {
            isAnimStart = true
            mView.animate().translationX(x)
                .setDuration(duration)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationCancel(animation: Animator) {
                        super.onAnimationCancel(animation)
                        isAnimStart = false
                        updateViewLocation()
                    }

                    override fun onAnimationEnd(animation: Animator) {
                        super.onAnimationEnd(animation)
                        isAnimStart = false
                        updateViewLocation()
                    }
                })
                .start()
        } else {
            eLogD("call","已在边界位置====>")
            isAnimStart = false
            updateViewLocation()
        }
    }

    private fun updateViewLocation() {
        mEvent?.updateLocation(mView.translationX, mView.translationY)
    }

    private fun checkBounds(transX: Float, transY: Float, move: Boolean): PointF {
        eLogD("call", "checkBounds transX=$transX ; transY=$transY")
        var x = transX
        var y = transY
        val pointF = PointF()
        val rightBounds = screenWidth - viewWidth - endMargin
        if (move) {
            if (x < endMargin) {
                x = endMargin.toFloat()
            } else if (x > rightBounds) {
                x = rightBounds.toFloat()
            }
        } else {
            val halfW = (screenWidth - viewWidth) / 2
            x = if (x > halfW) {
                (screenWidth - viewWidth - endMargin).toFloat()
            } else {
                endMargin.toFloat()
            }
        }
        //垂直方向边界
        var bottom = (screenHeight - mBottomMargin - viewHeight).toFloat()
        if (PlatformParams.isBottomNavBarEnabled) {
            bottom -= mNavigationBarHeight.toFloat()
        }
        val yVal = if (statusBarTrans) statusBarHeight else 0
        if (y < yVal) {
            y = yVal.toFloat()
        } else if (y > bottom) {
            y = bottom
        }
        pointF.x = x
        pointF.y = y
        return pointF
    }

    fun updateLocation(x: Float, y: Float, move: Boolean) {
        val pointF = checkBounds(x, y, move)
        eLogD("call", "updateLocation transX=${pointF.x} ; transY=${pointF.y}")
        val viewX = mView.translationX
        val viewY = mView.translationY
        if (viewX != pointF.x) {
            mView.translationX = pointF.x
        }
        if (viewY != pointF.y) {
            mView.translationY = pointF.y
        }
    }
}