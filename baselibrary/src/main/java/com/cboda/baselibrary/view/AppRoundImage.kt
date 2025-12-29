package com.cboda.baselibrary.view


import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.util.TypedValue
import androidx.appcompat.widget.AppCompatImageView
import com.cboda.baselibrary.R
import kotlin.math.max
import kotlin.math.min

open class AppRoundImage constructor(
    context: Context,
    attrs: AttributeSet? = null
) : AppCompatImageView(context, attrs) {
    private var mBorderRadiusType = 0//0:全圆 1:圆角
    private var mRoundType = 0 //0四边圆，1，上边圆，2，下边圆,3:特殊圆
    private var mBorderWidth = 0 //描边宽度
    private var mBorderColor = 0 //描边颜色
    private var mBorderRadius = 0  //圆角弧度
    private val mBitmapPaint = Paint()
    private var paint: Paint? = null
    private var paintB: Paint? = null
    private var mRadius = 0
    private val mMatrix = Matrix()
    private var mBitmapShader: BitmapShader? = null
    private var mWidth = 0
    private var mRoundRect: RectF? = null
    private var mRoundRectB: RectF? = null
    private var mRoundRectC: RectF? = null

    //    private var mRoundRectD: RectF? = null
    private var path: Path? = null
    private var pathB: Path? = null
    private var scale = 1.0f
    private val defaultBorderWidth = 0
    private val defaultBorderColor = Color.WHITE
    private val defaultTypeCircle = 0
    private val defaultTypeRound = 1
    private val defaultRoundType = 0
    private val defaultBorderRadius = 5
    private val defaultStateInstance = "state_instance"
    private val defaultStateType = "state_type"
    private val defaultStateBorderRadius = "state_border_radius"
    private val defaultStateRoundType = "state_round_type"
    private val defaultStateBorderWidth = "state_border_width"
    private val defaultStateBorderColor = "state_border_color"
    private var mWidthRatio = 0
    private var mHeightRatio = 0

    init {
        mBitmapPaint.isAntiAlias = true
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.AppRoundedImageView)
        mBorderRadius = typedArray.getDimensionPixelSize(
            R.styleable.AppRoundedImageView_app_round_borderRadius,
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                defaultBorderRadius.toFloat(),
                resources.displayMetrics
            ).toInt()
        )
        mRoundType = typedArray.getInt(
            R.styleable.AppRoundedImageView_app_round_round_type,
            defaultRoundType
        )
        mBorderRadiusType =
            typedArray.getInt(
                R.styleable.AppRoundedImageView_app_round_borderRadius_type,
                defaultTypeCircle
            )
        mBorderWidth = typedArray.getDimensionPixelSize(
            R.styleable.AppRoundedImageView_app_round_border_width,
            defaultBorderWidth
        )
        mBorderColor =
            typedArray.getColor(
                R.styleable.AppRoundedImageView_app_round_border_color,
                defaultBorderColor
            )
        mWidthRatio =
            typedArray.getInt(R.styleable.AppRoundedImageView_app_round_border_width_ratio, 0)
        mHeightRatio =
            typedArray.getInt(R.styleable.AppRoundedImageView_app_round_border_height_ratio, 0)
        typedArray.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        if (mBorderRadiusType == defaultTypeCircle) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            mWidth = measuredWidth.coerceAtMost(measuredHeight)
            mRadius = mWidth / 2
            setMeasuredDimension(mWidth, mWidth)
        } else {
            when {
                mWidthRatio > 0 -> { // 高度已知，根据比例，设置宽度
                    val height = MeasureSpec.getSize(heightMeasureSpec)
                    super.onMeasure(
                        MeasureSpec.makeMeasureSpec(
                            (height * mWidthRatio) as Int, MeasureSpec.EXACTLY
                        ),
                        MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
                    )
                }
                mHeightRatio > 0 -> { // 宽度已知，根据比例，设置高度
                    val width = MeasureSpec.getSize(widthMeasureSpec)
                    super.onMeasure(
                        MeasureSpec.makeMeasureSpec(
                            width,
                            MeasureSpec.EXACTLY
                        ), MeasureSpec.makeMeasureSpec(
                            (width * mHeightRatio) as Int, MeasureSpec.EXACTLY
                        )
                    )
                }
                else -> super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            }
        }

    }

    private fun setUpShader() {
        val drawable = drawable ?: return
        val bmp = drawableToBitmap(drawable)
        mBitmapShader = BitmapShader(bmp, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        scale = 1.0f
        if (mBorderRadiusType == defaultTypeCircle) {
            val bSize = min(bmp.width, bmp.height)
            scale = mWidth * 1.0f / bSize

        } else if (mBorderRadiusType == defaultTypeRound) {
            //            Log.e("TAG",
            //                    "b'w = " + bmp.getWidth() + " , " + "b'h = "
            //                            + bmp.getHeight());
            if (!(bmp.width == width && bmp.height == height)) {
                scale = max(
                    width * 1.0f / bmp.width,
                    height * 1.0f / bmp.height
                )
            }

        }
        mMatrix.setScale(scale, scale)
        mBitmapShader?.also {
            mBitmapShader?.setLocalMatrix(mMatrix)
            mBitmapPaint.shader = mBitmapShader
        }
    }

    override fun onDraw(canvas: Canvas) {
        drawable ?: also {
            return
        }
        setUpShader()
        //        if (border_Width != 0) {
        //            path = new Path();
        //            paint = new Paint();
        //            drawCircleBorder(canvas, border_Width, border_Color, mRadius - border_Width / 2.0f);
        //        }
        if (mBorderRadiusType == defaultTypeRound) {
            mRoundRect?.also { mRoundRect ->
                canvas.drawRoundRect(
                    mRoundRect,
                    mBorderRadius.toFloat(),
                    mBorderRadius.toFloat(),
                    mBitmapPaint
                )
                when (mRoundType) {
                    1 -> {
                        mRoundRectB?.also { mRoundRectB ->
                            canvas.drawRect(mRoundRectB, mBitmapPaint)
                        }
                    }
                    2 -> {
                        mRoundRectC?.also { mRoundRectC ->
                            canvas.drawRect(mRoundRectC, mBitmapPaint)
                        }
                    }
//                    3 -> {
//                        mRoundRectD?.also { mRoundRectD ->
//                            canvas.drawRect(mRoundRectD, mBitmapPaint)
//                        }
//                    }
                    else -> {

                    }
                }
            }

//            canvas.drawRoundRect(
//                mRoundRect!!, mBorderRadius.toFloat(), mBorderRadius.toFloat(),
//                mBitmapPaint
//            )
        } else {
            canvas.drawCircle(mRadius.toFloat(), mRadius.toFloat(), mRadius.toFloat(), mBitmapPaint)
        }
        if (mBorderWidth != 0) {
            path = Path()
            paint = Paint()
            drawCircleBorderA(canvas, mBorderWidth, mBorderColor, mRadius - mBorderWidth / 2.0f)
            //            drawCircleBorderb(canvas, border_Width, border_Color, mRadius - border_Width / 2.0f);
            //            drawCircleBorderc(canvas, border_Width, border_Color, mRadius - border_Width / 2.0f);
        }
        if (mRoundType == 3) {
            pathB = Path()
            paintB = Paint()
            drawCircleBorderB(canvas, mBorderWidth, mBorderColor, mRadius - mBorderWidth / 2.0f)
        }


    }

    private fun drawCircleBorderA(
        canvas: Canvas,
        borderWidth: Int,
        borderColor: Int,
        radius: Float
    ) {
        initBorderPaint(borderWidth, borderColor)
        path?.also { path ->
            path?.addCircle(width / 2.0f, height / 2.0f, radius, Path.Direction.CCW)
            path?.addCircle(width / 2.0f + 0.5f, height / 2.0f + 0.5f, radius, Path.Direction.CCW)
            path?.addCircle(width / 2.0f - 0.5f, height / 2.0f - 0.5f, radius, Path.Direction.CCW)
            paint?.also { paint ->
                canvas.drawPath(path, paint)
            }
        }
    }

    private fun drawCircleBorderB(
        canvas: Canvas,
        borderWidth: Int,
        borderColor: Int,
        radius: Float
    ) {
        initBorderPaintB(borderWidth, borderColor)
        pathB?.also { path ->
            val w = width.toFloat()
            val h = height.toFloat()
            val leftM = 20f
            val topM = 40f
            val rightM = 60f
            path.moveTo(0.0f, topM)
            val topL1 = RectF(0f, leftM, topM, rightM)
            path.arcTo(topL1, 180f, 270f)
            val topL2 = RectF(leftM, 0f, rightM, topM)
            path.arcTo(topL2, 180f, 270f)
            path.lineTo(topM, 0f)
            path.lineTo(w - rightM, 0f)
            val topR1 = RectF(w - rightM, 0f, w - leftM, topM)
            path.arcTo(topR1, 270f, 359.9f)
            val topR2 = RectF(w - topM, leftM, w, rightM)
            path.arcTo(topR2, 270f, 359.9f)
            path.lineTo(w, topM)
            path.lineTo(w, h - rightM)
            val btR1 = RectF(w - topM, h - rightM, w, h - leftM)
            path.arcTo(btR1, 0f, 90f)
            val btR2 = RectF(w - rightM, h - topM, w - 20, h)
            path.arcTo(btR2, 0f, 90f)
            path.lineTo(w - rightM, h)
            path.lineTo(rightM, h)
            val btL1 = RectF(leftM, h - topM, rightM, h)
            path.arcTo(btL1, 90f, 270f)
            val btL2 = RectF(0f, h - rightM, topM, h - leftM)
            path.arcTo(btL2, 90f, 270f)
            path.lineTo(0f, h - topM)
            path.lineTo(0f, topM)
            path.close()

            val pf = PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
            canvas.drawFilter = pf
            canvas.clipPath(path)
            drawable.draw(canvas)
//            child.drawable.draw(canvas!!)

        }
    }

    //    private fun drawCircleBorderC(
//        canvas: Canvas,
//        borderWidth: Int,
//        borderColor: Int,
//        radius: Float
//    ) {
//        initBorderPaint(borderWidth, borderColor)
//        path?.also { path ->
//            path?.addCircle(width / 2.0f - 2.0f, height / 2.0f - 2.0f, radius, Path.Direction.CCW)
//            paint?.also { paint ->
//                canvas.drawPath(path, paint)
//            }
//        }
//    }
    private fun initBorderPaintB(borderWidth: Int, borderColor: Int) {
        pathB?.reset()
        paintB?.strokeWidth = borderWidth.toFloat()
        paintB?.color = borderColor
        paintB?.style = Paint.Style.STROKE
        paintB?.isAntiAlias = true
        paintB?.isFilterBitmap = true
    }

    private fun initBorderPaint(borderWidth: Int, borderColor: Int) {
        path?.reset()
        paint?.strokeWidth = borderWidth.toFloat()
        paint?.color = borderColor
        paint?.style = Paint.Style.STROKE
        paint?.isAntiAlias = true
        paint?.isFilterBitmap = true
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (mBorderRadiusType == defaultTypeRound) {
            mRoundRect = RectF(0f, 0f, w.toFloat(), h.toFloat())
            when (mRoundType) {
                1 -> mRoundRectB =
                    RectF(0f, h.toFloat() - mBorderRadius.toFloat(), w.toFloat(), h.toFloat())
                2 -> mRoundRectC = RectF(0f, 0f, w.toFloat(), h.toFloat() - mBorderRadius.toFloat())
//                3 -> mRoundRectD = RectF(
//                    0f + mBorderRadius.toFloat(),
//                    0f + mBorderRadius.toFloat(),
//                    w.toFloat() - mBorderRadius.toFloat(),
//                    h.toFloat() - mBorderRadius.toFloat()
//                )
            }
        }

    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        }
        var width1 = 1
        var height1 = 1
        val w = drawable.intrinsicWidth
        val h = drawable.intrinsicHeight
        if (w > 0) {
            width1 = w
        }
        if (h > 0) {
            height1 = h
        }
        val bitmap = Bitmap.createBitmap(width1, height1, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, width1, height1)
        drawable.draw(canvas)
        return bitmap
    }


    fun setBorderRadius(borderRadius: Int) {
        val pxVal = dp2px2(borderRadius)
        if (this.mBorderRadius != pxVal) {
            this.mBorderRadius = pxVal
            invalidate()
        }
    }

    fun setType(type: Int) {
        if (this.mBorderRadiusType != type) {
            this.mBorderRadiusType = type
            if (this.mBorderRadiusType != defaultTypeRound && this.mBorderRadiusType != defaultTypeCircle) {
                this.mBorderRadiusType = defaultTypeCircle
            }
            requestLayout()
        }

    }

    override fun onSaveInstanceState(): Parcelable? {
        val bundle = Bundle()
        bundle.putParcelable(defaultStateInstance, super.onSaveInstanceState())
        bundle.putInt(defaultStateType, mBorderRadiusType)
        bundle.putInt(defaultStateBorderRadius, mBorderRadius)
        bundle.putInt(defaultStateRoundType, mRoundType)
        bundle.putInt(defaultStateBorderWidth, mBorderWidth)
        bundle.putInt(defaultStateBorderColor, mBorderColor)
        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        if (state is Bundle) {
            super.onRestoreInstanceState(state.getParcelable(defaultStateInstance))
            this.mBorderRadiusType = state.getInt(defaultStateType)
            this.mBorderRadius = state.getInt(defaultStateBorderRadius)
            this.mRoundType = state.getInt(defaultStateRoundType)
            this.mBorderWidth = state.getInt(defaultStateBorderWidth)
            this.mBorderColor = state.getInt(defaultStateBorderColor)
        } else {
            super.onRestoreInstanceState(state)
        }
    }

    private fun dp2px2(dpVal: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dpVal.toFloat(), resources.displayMetrics
        ).toInt()
    }
}