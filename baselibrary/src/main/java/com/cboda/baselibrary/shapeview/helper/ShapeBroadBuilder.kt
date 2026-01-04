package com.cboda.baselibrary.shapeview.helper

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.os.Build
import android.view.View
import androidx.core.view.ViewCompat
import com.cboda.baselibrary.shapeview.data.AttributeSetBroadData


enum class ShapeBroadType(val value: Int) {
    RECTANGLE(0),
    OVAL(1),
    LINE(2),
    RING(3);

    companion object {
        @JvmStatic
        fun fromValue(value: Int): ShapeBroadType {
            return values().find { it.value == value } ?: RECTANGLE
        }
    }
}

enum class ShapeGradientBroadAngle(val value: Int) {
    LEFT_RIGHT(0),
    BL_TR(45),
    BOTTOM_TOP(90),
    BR_TL(135),
    RIGHT_LEFT(180),
    TR_BL(225),
    TOP_BOTTOM(270),
    TL_BR(315);

    companion object {
        @JvmStatic
        fun fromValue(value: Int): ShapeGradientBroadAngle {
            return values().find { it.value == value } ?: LEFT_RIGHT
        }
    }
}

enum class ShapeGradientBroadType(val value: Int) {
    LINEAR(0),
    RADIAL(1),
    SWEEP(2);

    companion object {
        @JvmStatic
        fun fromValue(value: Int): ShapeGradientBroadType {
            return values().find { it.value == value } ?: LINEAR
        }
    }
}

class ShapeBroadBuilder {

    /**
     * android:shape=["rectangle" | "oval" | "line" | "ring"]
     */
    private var shapeType: ShapeBroadType = ShapeBroadType.RECTANGLE

    /**
     * <solid android:color="color"></solid>
     */
    private var solidColor = -1

    /**
     * <stroke android:width="integer" android:color="color" android:dashWidth="integer" android:dashGap="integer"></stroke>
     */
    private var strokeWidth = -1
    private var strokeColor = -1
    private var strokeDashWidth = 0.0f
    private var strokeDashGap = 0.0f

    /**
     * <corners android:radius="integer" android:topLeftRadius="integer" android:topRightRadius="integer" android:bottomLeftRadius="integer" android:bottomRightRadius="integer"></corners>
     */
    private var cornersRadius = 0.0f
    private var cornersTopLeftRadius = 0.0f
    private var cornersTopRightRadius = 0.0f
    private var cornersBottomLeftRadius = 0.0f
    private var cornersBottomRightRadius = 0.0f

    /**
     * <gradient android:angle="45"/>
     */
    private var shapeGradientAngle: ShapeGradientBroadAngle = ShapeGradientBroadAngle.LEFT_RIGHT
    private var gradientCenterX: Float = 0f
    private var gradientCenterY: Float = 0f
    private var gradientGradientRadius: Int = 0

    private var gradientStartColor = -1
    private var gradientCenterColor = -1
    private var gradientEndColor = -1

    /**
     * <gradient android:type=["radial" | "linear" | "sweep"] />
     */
    private var gradientType: ShapeGradientBroadType = ShapeGradientBroadType.LINEAR

    /**
     * <gradient android:useLevel="false" />
     */
    private var gradientUseLevel: Boolean = false

    /**
     * <size android:width="integer" android:height="integer"></size>
     */
    private var sizeWidth = -1
    private var sizeHeight = -1

    /**
     * <selector xmlns:android="http://schemas.android.com/apk/res/android" android:constantSize=["true" |></selector> "false"]
     * android:dither=["true" | "false"]
     * android:variablePadding=["true" | "false"] >
     * <item android:drawable="@[package:]drawable/drawable_resource" android:state_pressed=["true" |></item> "false"]
     * android:state_focused=["true" | "false"]
     * android:state_hovered=["true" | "false"]
     * android:state_selected=["true" | "false"]
     * android:state_checkable=["true" | "false"]
     * android:state_checked=["true" | "false"]
     * android:state_enabled=["true" | "false"]
     * android:state_activated=["true" | "false"]
     * android:state_window_focused=["true" | "false"] />
     *
     */
    private var selectorPressedColor: Int = 0
    private var selectorDisableColor: Int = 0
    private var selectorNormalColor: Int = 0

    private var useSelector: Boolean = false

    fun setShapeType(shapeType: ShapeBroadType): ShapeBroadBuilder {
        this.shapeType = shapeType
        return this
    }

    fun setShapeSolidColor(color: Int): ShapeBroadBuilder {
        this.solidColor = color
        return this
    }

    fun setShapeCornersRadius(radius: Float): ShapeBroadBuilder {
        this.cornersRadius = radius
        return this
    }

    fun setShapeCornersTopLeftRadius(radius: Float): ShapeBroadBuilder {
        this.cornersTopLeftRadius = radius
        return this
    }

    fun setShapeCornersTopRightRadius(radius: Float): ShapeBroadBuilder {
        this.cornersTopRightRadius = radius
        return this
    }

    fun setShapeCornersBottomRightRadius(radius: Float): ShapeBroadBuilder {
        this.cornersBottomRightRadius = radius
        return this
    }

    fun setShapeCornersBottomLeftRadius(radius: Float): ShapeBroadBuilder {
        this.cornersBottomLeftRadius = radius
        return this
    }

    fun setShapeStrokeWidth(strokeWidth: Int): ShapeBroadBuilder {
        this.strokeWidth = strokeWidth
        return this
    }

    fun setShapeStrokeColor(strokeColor: Int): ShapeBroadBuilder {
        this.strokeColor = strokeColor
        return this
    }

    fun setShapeStrokeDashWidth(strokeDashWidth: Float): ShapeBroadBuilder {
        this.strokeDashWidth = strokeDashWidth
        return this
    }

    fun setShapeStrokeDashGap(strokeDashGap: Float): ShapeBroadBuilder {
        this.strokeDashGap = strokeDashGap
        return this
    }

    fun setShapeUseSelector(useSelector: Boolean): ShapeBroadBuilder {
        this.useSelector = useSelector
        return this
    }

    fun setShapeSelectorPressedColor(color: Int): ShapeBroadBuilder {
        this.selectorPressedColor = color
        return this
    }

    fun setShapeSelectorNormalColor(color: Int): ShapeBroadBuilder {
        this.selectorNormalColor = color
        return this
    }

    fun setShapeSelectorDisableColor(color: Int): ShapeBroadBuilder {
        this.selectorDisableColor = color
        return this
    }

    fun setShapeSizeWidth(sizeWidth: Int): ShapeBroadBuilder {
        this.sizeWidth = sizeWidth
        return this
    }

    fun setShapeSizeHeight(sizeHeight: Int): ShapeBroadBuilder {
        this.sizeHeight = sizeHeight
        return this
    }

    fun setShapeGradientAngle(shapeGradientAngle: ShapeGradientBroadAngle): ShapeBroadBuilder {
        this.shapeGradientAngle = shapeGradientAngle
        return this
    }

    fun setShapeGradientCenterX(gradientCenterX: Float): ShapeBroadBuilder {
        this.gradientCenterX = gradientCenterX
        return this
    }

    fun setShapeGradientCenterY(gradientCenterY: Float): ShapeBroadBuilder {
        this.gradientCenterY = gradientCenterY
        return this
    }

    fun setShapeGradientGradientRadius(gradientGradientRadius: Int): ShapeBroadBuilder {
        this.gradientGradientRadius = gradientGradientRadius
        return this
    }

    fun setShapeGradientStartColor(gradientStartColor: Int): ShapeBroadBuilder {
        this.gradientStartColor = gradientStartColor
        return this
    }

    fun setShapeGradientCenterColor(gradientCenterColor: Int): ShapeBroadBuilder {
        this.gradientCenterColor = gradientCenterColor
        return this
    }

    fun setShapeGradientEndColor(gradientEndColor: Int): ShapeBroadBuilder {
        this.gradientEndColor = gradientEndColor
        return this
    }

    fun setShapeGradientType(gradientType: ShapeGradientBroadType): ShapeBroadBuilder {
        this.gradientType = gradientType
        return this
    }

    fun setShapeGradientUseLevel(gradientUseLevel: Boolean): ShapeBroadBuilder {
        this.gradientUseLevel = gradientUseLevel
        return this
    }


    fun init(targetView: View, attributeSetData: AttributeSetBroadData) {
        setShapeType(ShapeBroadType.fromValue(attributeSetData.shapeType))
        setShapeCornersRadius(attributeSetData.cornersRadius)
        setShapeCornersTopLeftRadius(attributeSetData.cornersTopLeftRadius)
        setShapeCornersTopRightRadius(attributeSetData.cornersTopRightRadius)
        setShapeCornersBottomRightRadius(attributeSetData.cornersBottomRightRadius)
        setShapeCornersBottomLeftRadius(attributeSetData.cornersBottomLeftRadius)
        setShapeSolidColor(attributeSetData.solidColor)
        setShapeStrokeColor(attributeSetData.strokeColor)
        setShapeStrokeWidth(attributeSetData.strokeWidth)
        setShapeStrokeDashWidth(attributeSetData.strokeDashWidth)
        setShapeStrokeDashGap(attributeSetData.strokeDashGap)
        setShapeUseSelector(attributeSetData.useSelector)
        setShapeSelectorNormalColor(attributeSetData.selectorNormalColor)
        setShapeSelectorPressedColor(attributeSetData.selectorPressedColor)
        setShapeSelectorDisableColor(attributeSetData.selectorDisableColor)
        setShapeSizeWidth(attributeSetData.sizeWidth)
        setShapeSizeHeight(attributeSetData.sizeHeight)
        setShapeGradientType(ShapeGradientBroadType.fromValue(attributeSetData.gradientType))
        setShapeGradientAngle(ShapeGradientBroadAngle.fromValue(attributeSetData.gradientAngle))
        setShapeGradientGradientRadius(attributeSetData.gradientGradientRadius)
        setShapeGradientUseLevel(attributeSetData.gradientUseLevel)
        setShapeGradientCenterX(attributeSetData.gradientCenterX)
        setShapeGradientCenterY(attributeSetData.gradientCenterY)
        setShapeGradientStartColor(attributeSetData.gradientStartColor)
        setShapeGradientCenterColor(attributeSetData.gradientCenterColor)
        setShapeGradientEndColor(attributeSetData.gradientEndColor)
        into(targetView)
    }

    private fun setShapeType(gradientDrawable: GradientDrawable) {
        when (shapeType) {
            ShapeBroadType.RECTANGLE -> gradientDrawable.shape = GradientDrawable.RECTANGLE
            ShapeBroadType.OVAL -> gradientDrawable.shape = GradientDrawable.OVAL
            ShapeBroadType.LINE -> gradientDrawable.shape = GradientDrawable.LINE
            ShapeBroadType.RING -> gradientDrawable.shape = GradientDrawable.RING
        }
    }


    private fun setSize(gradientDrawable: GradientDrawable) {
        if (sizeWidth > 0 || sizeHeight > 0) {
            gradientDrawable.setSize(sizeWidth, sizeHeight)
        }
    }

    /**
     * 设置边框  宽度  颜色  虚线  间隙
     */
    private fun setBorder(gradientDrawable: GradientDrawable) {
        if (strokeWidth >= 0) {
            gradientDrawable.setStroke(strokeWidth, strokeColor, strokeDashWidth, strokeDashGap)
        }
    }

    /**
     * 只有类型是矩形的时候设置圆角半径才有效
     */
    private fun setRadius(gradientDrawable: GradientDrawable) {
        if (shapeType == ShapeBroadType.RECTANGLE) {
            if (cornersRadius != 0f) {
                gradientDrawable.cornerRadius = cornersRadius
            } else {
                if (cornersTopLeftRadius != 0f || cornersTopRightRadius != 0f || cornersBottomRightRadius != 0f || cornersBottomLeftRadius != 0f) {
                    //1、2两个参数表示左上角，3、4表示右上角，5、6表示右下角，7、8表示左下角
                    gradientDrawable.cornerRadii = floatArrayOf(
                        cornersTopLeftRadius,
                        cornersTopLeftRadius,
                        cornersTopRightRadius,
                        cornersTopRightRadius,
                        cornersBottomRightRadius,
                        cornersBottomRightRadius,
                        cornersBottomLeftRadius,
                        cornersBottomLeftRadius
                    )
                }
            }
        }
    }


    private fun setSolidColor(gradientDrawable: GradientDrawable) {
        if (gradientStartColor == -1 && gradientEndColor == -1) {
            gradientDrawable.setColor(solidColor)
        }
    }


    /**
     * 设置Selector的不同状态的颜色
     *
     * @param state 按钮状态
     */
    private fun setSelectorColor(gradientDrawable: GradientDrawable, state: Int) {
        if (useSelector && state != 0) {
            when (state) {
                android.R.attr.state_pressed -> gradientDrawable.setColor(selectorPressedColor)
                -android.R.attr.state_enabled -> gradientDrawable.setColor(selectorDisableColor)
                android.R.attr.state_enabled -> gradientDrawable.setColor(selectorNormalColor)
            }
        }
    }


    /**
     * 设置背景颜色
     * 如果设定的有Orientation 就默认为是渐变色的Button，否则就是纯色的Button
     */
    private fun setGradient(gradientDrawable: GradientDrawable) {
        if (gradientStartColor != -1 || gradientEndColor != -1) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                if (gradientCenterColor == -1) {
                    gradientDrawable.colors = intArrayOf(gradientStartColor, gradientEndColor)
                } else {
                    gradientDrawable.colors =
                        intArrayOf(gradientStartColor, gradientCenterColor, gradientEndColor)
                }
                when (gradientType) {
                    ShapeGradientBroadType.LINEAR -> {
                        gradientDrawable.gradientType = GradientDrawable.LINEAR_GRADIENT
                        gradientDrawable.orientation =
                            getGradientOrientationByAngle(shapeGradientAngle)
                    }
                    ShapeGradientBroadType.RADIAL -> {
                        gradientDrawable.gradientType = GradientDrawable.RADIAL_GRADIENT
                        gradientDrawable.gradientRadius = gradientGradientRadius.toFloat()
                    }
                    ShapeGradientBroadType.SWEEP -> gradientDrawable.gradientType =
                        GradientDrawable.SWEEP_GRADIENT
                }
                if (gradientCenterX != 0f || gradientCenterY != 0f) {
                    gradientDrawable.setGradientCenter(gradientCenterX, gradientCenterY)
                }
                gradientDrawable.useLevel = gradientUseLevel
            }
        }
    }


    /**
     * 设置颜色渐变类型
     *
     * @param shapeGradientAngle gradientAngle
     * @return Orientation
     */
    private fun getGradientOrientationByAngle(shapeGradientAngle: ShapeGradientBroadAngle): GradientDrawable.Orientation {
        return when (shapeGradientAngle) {
            ShapeGradientBroadAngle.LEFT_RIGHT -> GradientDrawable.Orientation.LEFT_RIGHT
            ShapeGradientBroadAngle.BL_TR -> GradientDrawable.Orientation.BL_TR
            ShapeGradientBroadAngle.BOTTOM_TOP -> GradientDrawable.Orientation.BOTTOM_TOP
            ShapeGradientBroadAngle.BR_TL -> GradientDrawable.Orientation.BR_TL
            ShapeGradientBroadAngle.RIGHT_LEFT -> GradientDrawable.Orientation.RIGHT_LEFT
            ShapeGradientBroadAngle.TR_BL -> GradientDrawable.Orientation.TR_BL
            ShapeGradientBroadAngle.TOP_BOTTOM -> GradientDrawable.Orientation.TOP_BOTTOM
            ShapeGradientBroadAngle.TL_BR -> GradientDrawable.Orientation.TL_BR
        }
    }

    /**
     * 获取设置之后的Selector
     * 注意该处的顺序，只要有一个状态与之相配，背景就会被换掉 所以不要把大范围放在前面了，
     * 如果sd.addState(new[]{},normal)放在第一个的话，就没有什么效果了
     * @return stateListDrawable
     */
    private val selectorDrawable: StateListDrawable
        get() {
            val stateListDrawable = StateListDrawable()
            stateListDrawable.addState(
                intArrayOf(
                    android.R.attr.state_pressed,
                    android.R.attr.state_enabled
                ), getDrawable(android.R.attr.state_pressed)
            )
            stateListDrawable.addState(
                intArrayOf(-android.R.attr.state_enabled),
                getDrawable(-android.R.attr.state_enabled)
            )
            stateListDrawable.addState(intArrayOf(), getDrawable(android.R.attr.state_enabled))
            return stateListDrawable
        }

    private fun getDrawable(state: Int): GradientDrawable {
        val gradientDrawable = GradientDrawable()
        setShapeType(gradientDrawable)
        setGradient(gradientDrawable)
        setSolidColor(gradientDrawable)
        setBorder(gradientDrawable)
        setRadius(gradientDrawable)
        setSize(gradientDrawable)
        setSelectorColor(gradientDrawable, state)
        return gradientDrawable
    }

    fun into(view: View?) {
        if (view == null) return
        ViewCompat.setBackground(view, if (useSelector) selectorDrawable else getDrawable(0))
    }

    /**
     * 单位转换工具类
     *
     * @param context  上下文对象
     * @param dipValue 值
     * @return 返回值
     */
    private fun dip2px(context: Context, dipValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dipValue * scale + 0.5f).toInt()
    }
}