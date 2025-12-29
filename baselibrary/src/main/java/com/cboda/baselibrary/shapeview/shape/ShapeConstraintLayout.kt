package com.cboda.baselibrary.shapeview.shape

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import com.cboda.baselibrary.shapeview.data.AttributeSetData
import com.cboda.baselibrary.shapeview.helper.AttributeSetHelper
import com.cboda.baselibrary.shapeview.helper.ShadowHelper
import com.cboda.baselibrary.shapeview.helper.ShapeBuilder


class ShapeConstraintLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {
    var shapeBuilder: ShapeBuilder? = null
    var shadowHelper: ShadowHelper? = null

    var showShadow: Boolean = false

    var attributeSetData: AttributeSetData = AttributeSetData()

    init {
        attributeSetData = AttributeSetHelper().loadFromAttributeSet(context, attrs)
        if (attributeSetData.showShadow) {
            shadowHelper = ShadowHelper()
            shadowHelper?.init(this, attributeSetData)
        } else {
            shapeBuilder = ShapeBuilder()
            shapeBuilder?.init(this, attributeSetData)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        shadowHelper?.onSizeChanged(w, h)
    }

    override fun dispatchDraw(canvas: Canvas) {
        shadowHelper?.drawBefore(canvas)
        super.dispatchDraw(canvas)
        shadowHelper?.drawOver(canvas)
    }

    fun setIsShowShadow(showShadow: Boolean) {
        this.showShadow = showShadow
        if (showShadow) {
            shapeBuilder = null
            shadowHelper = ShadowHelper()
            shadowHelper?.init(this, attributeSetData)
        } else {
            shadowHelper = null
            shapeBuilder = ShapeBuilder()
            shapeBuilder?.init(this, attributeSetData)
        }
        invalidate()
    }
}