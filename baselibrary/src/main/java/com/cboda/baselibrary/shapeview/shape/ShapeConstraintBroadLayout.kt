package com.cboda.baselibrary.shapeview.shape

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import com.cboda.baselibrary.shapeview.data.AttributeSetBroadData
import com.cboda.baselibrary.shapeview.helper.AttributeSetBroadHelper
import com.cboda.baselibrary.shapeview.helper.ShadowBroadHelper
import com.cboda.baselibrary.shapeview.helper.ShapeBroadBuilder


class ShapeConstraintBroadLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {
    var shapeBuilder: ShapeBroadBuilder? = null
    var shadowHelper: ShadowBroadHelper? = null

    var showShadow: Boolean = false

    var attributeSetData: AttributeSetBroadData = AttributeSetBroadData()

    init {
        attributeSetData = AttributeSetBroadHelper().loadFromAttributeSet(context, attrs)
        if (attributeSetData.showShadow) {
            shadowHelper = ShadowBroadHelper()
            shadowHelper?.init(this, attributeSetData)
        } else {
            shapeBuilder = ShapeBroadBuilder()
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
            shadowHelper = ShadowBroadHelper()
            shadowHelper?.init(this, attributeSetData)
        } else {
            shadowHelper = null
            shapeBuilder = ShapeBroadBuilder()
            shapeBuilder?.init(this, attributeSetData)
        }
        invalidate()
    }
}