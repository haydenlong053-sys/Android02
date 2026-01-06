package com.lxkj.aiim.widge

import android.content.Context
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.cboda.intelligent.R
import com.lxkj.aiim.lj.utils.AppUtil

class AvatarShadowView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var shadowColor: Int = Color.parseColor("#1E293B")
    private var shadowDx: Float = 0f
    private var shadowDy: Float = 4f
    private var shadowRadius: Float = 8f
    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val avatarSize = AppUtil.dp2px(context, 56) // 头像大小
    private val shadowExtra = AppUtil.dp2px(context, 8) // 阴影额外范围

    private val imageView = AppCompatImageView(context).apply {
        scaleType = ImageView.ScaleType.CENTER_CROP
        layoutParams = LayoutParams(avatarSize, avatarSize).apply {
            gravity = android.view.Gravity.CENTER
        }
        Glide.with(context)
            .load(R.mipmap.ic_head_defaut)
            .apply(RequestOptions.circleCropTransform())
            .placeholder(R.mipmap.ic_head_defaut)
            .into(this)
    }

    init {
        attrs?.let {
            val ta = context.obtainStyledAttributes(it, R.styleable.AvatarShadowView)
            shadowColor = ta.getColor(R.styleable.AvatarShadowView_shadowColor, shadowColor)
            shadowDx = ta.getDimension(R.styleable.AvatarShadowView_shadowDx, shadowDx)
            shadowDy = ta.getDimension(R.styleable.AvatarShadowView_shadowDy, shadowDy)
            shadowRadius = ta.getDimension(R.styleable.AvatarShadowView_shadowRadius, shadowRadius)
            ta.recycle()
        }

        shadowPaint.color = Color.TRANSPARENT
        shadowPaint.setShadowLayer(shadowRadius, shadowDx, shadowDy, shadowColor)

        setLayerType(LAYER_TYPE_SOFTWARE, null)
        addView(imageView)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // 整体大小 = 头像大小 + 阴影额外范围
        val size = avatarSize + shadowExtra
        setMeasuredDimension(size, size)
        super.onMeasure(
            MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY)
        )
    }

    override fun dispatchDraw(canvas: Canvas) {
        val radius = width / 2f
        // 阴影层
        canvas.drawCircle(radius, radius, avatarSize / 2f, shadowPaint)

        // 剪裁圆形区域（头像部分）
        val save = canvas.save()
        val path = Path()
        path.addCircle(radius, radius, avatarSize / 2f, Path.Direction.CW)
        canvas.clipPath(path)

        super.dispatchDraw(canvas)
        canvas.restoreToCount(save)
    }

    fun loadAvatar(url: String, placeholder: Int? = null) {
        Glide.with(context)
            .load(url)
            .apply(RequestOptions.circleCropTransform())
            .placeholder(placeholder ?: android.R.color.darker_gray)
            .into(imageView)
    }
}


