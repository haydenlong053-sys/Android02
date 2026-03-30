package com.cboda.baselibrary.utils

import android.Manifest
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Outline
import android.graphics.Typeface
import android.os.Build
import android.os.SystemClock
import android.text.Editable
import android.text.InputFilter
import android.text.Spanned
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.EncryptUtils.encryptMD5ToString
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.PermissionUtils
import com.blankj.utilcode.util.TimeUtils
import com.cboda.baselibrary.R
import com.cboda.baselibrary.app.UtilBroadApplication
import com.cboda.baselibrary.bean.ComBottomListBean
import com.cboda.baselibrary.popup.CommonBottomListPop
import com.cboda.baselibrary.popup.CommonTipBroadPop
import com.cboda.baselibrary.widge.LoadingBroadDialog
import com.hjq.shape.layout.ShapeConstraintLayout
import com.hjq.shape.layout.ShapeLinearLayout
import com.hjq.shape.layout.ShapeRelativeLayout
import com.hjq.shape.view.ShapeEditText
import com.hjq.shape.view.ShapeImageView
import com.hjq.shape.view.ShapeTextView
import com.hjq.shape.view.ShapeView
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.Random
import java.util.UUID

private val tAG by lazy { "ViewExpandUtilClass" }

fun String.removeTrailingZeros(): String {
    // 匹配：末尾的连续0，并且如果这些0后面紧跟着小数点，则连小数点也一起移除
    return this.replace(Regex("(\\.(?:[0-9]*[1-9])?)0+$"), "$1").replace(Regex("\\.$"), "")
}

// 把这 10 行放进项目，以后所有点击都不用再担心重复
fun View.click(debounceTime: Long = 800, action: () -> Unit) {
//    setOnClickListener {
//        val now = SystemClock.uptimeMillis()
//        val key = 0x7fffffff
//        val last = (getTag(key) as? Long) ?: 0
//        if (now - last >= debounceTime) {
//            setTag(key, now)
//            action()
//        }
//    }
    click(this, action, debounceTime)
}


fun click(view: View, action: () -> Unit, debounceTime: Long = 800) {
    view.setOnClickListener {
        val now = SystemClock.uptimeMillis()
        val key = 0x7fffffff
        val last = (view.getTag(key) as? Long) ?: 0
        if (now - last >= debounceTime) {
            view.setTag(key, now)
            action()
        }
    }
}

fun click(view: View, action: () -> Unit) {
    click(view, action, 800)
}

/**
 * 裁剪字符串，并在末尾添加省略号
 * @param text 原始文本
 * @param maxLength 最大允许的字符数
 */
fun truncateTextWithEllipsis(text: String, maxLength: Int = 5): String {
    return if (text.length > maxLength) {
        text.substring(0, maxLength) + "..."
    } else {
        text
    }
}

fun checkAudioPermission(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        PermissionUtils.isGranted(
            Manifest.permission.RECORD_AUDIO
        )
    } else {
        PermissionUtils.isGranted(Manifest.permission.RECORD_AUDIO)
    }
}

fun getAppColor(context: Context? = null, color: Int): Int {
    return ActivityCompat.getColor(getAppContext(context), color)
}


fun getAppContext(context: Context? = null): Context {
    return context ?: (getResumeActivity() ?: UtilBroadApplication.cxt.applicationContext)
}


fun getResumeActivity(): AppCompatActivity? {
    return try {
        val activity = ActivityUtils.getTopActivity()
        if (null != activity && activity is AppCompatActivity && !activity.isFinishing && !activity.isDestroyed) activity else null
    } catch (e: Exception) {
        null
    }
}


fun getAppDimension(context: Context? = null, res: Int): Float =
    getAppContext(context).resources.getDimension(res)

fun View.getAppDimension(res: Int) = getAppDimension(this.context, res)
fun getAppIntDimension(context: Context? = null, res: Int): Int =
    getAppDimension(context, res).toInt() ?: 0


fun getAppDimensionPixelSize(context: Context?, res: Int): Float =
    getAppContext(context).resources.getDimensionPixelSize(res).toFloat()

fun View.getAppDimensionPixelSize(res: Int) = getAppDimensionPixelSize(this.context, res)
fun getAppIntDimensionPixelSize(context: Context?, res: Int): Int =
    getAppDimensionPixelSize(context, res).toInt() ?: 0


fun getNotScrollVerticalGridLayoutManager(
    context: Context?,
    spanCount: Int,
): GridLayoutManager {
    val layoutManager =
        object : GridLayoutManager(getAppContext(context), spanCount, VERTICAL, false) {
            override fun canScrollVertically(): Boolean {
                return false
            }
        }
    return layoutManager
}

fun getNotScrollHorizontalGridLayoutManager(
    context: Context?,
    spanCount: Int,
): GridLayoutManager {
    val layoutManager =
        object : GridLayoutManager(getAppContext(context), spanCount, HORIZONTAL, false) {
            override fun canScrollVertically(): Boolean {
                return false
            }
        }
    return layoutManager
}

private fun getNotScrollVerticalStaggeredGridLayoutManager(
    spanCount: Int,
): StaggeredGridLayoutManager {
    val layoutManager =
        object : StaggeredGridLayoutManager(spanCount, VERTICAL) {
            override fun canScrollVertically(): Boolean {
                return false
            }
        }
    //    layoutManager.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_NONE
    return layoutManager
}

// Temporarily commented out due to ConstraintLayout access issue
// fun ShapeConstraintBroadLayout.setBackColor(
//     @ColorRes color: Int,
//     radius: Float,
//     isTop: Boolean,
//     isBottom: Boolean
// ) {
//     this.shapeDrawableBuilder.solidColor = getAppColor(this.context, color)
//     if (isTop && isBottom) {
//         this.shapeDrawableBuilder.setRadius(radius)
//     } else {
//         if (isBottom) {
//             this.shapeDrawableBuilder.bottomLeftRadius = radius
//             this.shapeDrawableBuilder.bottomRightRadius = radius
//         }
//         if (isTop) {
//             this.shapeDrawableBuilder.topLeftRadius = radius
//             this.shapeDrawableBuilder.topRightRadius = radius
//         }
//     }
//     this.shapeDrawableBuilder.intoBackground()
// }

// fun ShapeConstraintBroadLayout.setBackColor(
//     @ColorRes color: Int
// ) {
//     this.shapeDrawableBuilder.solidColor = getAppColor(this.context, color)
//     this.shapeDrawableBuilder.intoBackground()
// }

// fun ShapeConstraintBroadLayout.setBackColor(
//     @ColorRes startColor: Int,
//     @ColorRes endColor: Int,
//     angle: Int = 0
// ) {
//     this.shapeDrawableBuilder.setSolidGradientColors(
//         getAppColor(this.context, startColor),
//         getAppColor(this.context, endColor)
//     )
//     this.shapeDrawableBuilder.angle = angle
//     this.shapeDrawableBuilder.intoBackground()
// }

fun ShapeImageView.setBackColor(@ColorRes startColor: Int, @ColorRes endColor: Int) {
    this.shapeDrawableBuilder.setSolidGradientColors(
        getAppColor(this.context, startColor),
        getAppColor(this.context, endColor)
    )
    this.shapeDrawableBuilder.intoBackground()
}

fun ShapeTextView.setBackColor(@ColorRes startColor: Int, @ColorRes endColor: Int) {
    this.shapeDrawableBuilder.setSolidGradientColors(
        getAppColor(this.context, startColor),
        getAppColor(this.context, endColor)
    )
    this.shapeDrawableBuilder.intoBackground()
}

fun ShapeTextView.setBackColor(@ColorRes color: Int) {
    this.shapeDrawableBuilder.solidColor = getAppColor(this.context, color)
    this.shapeDrawableBuilder.intoBackground()
}

fun ShapeTextView.setBackColor(@ColorRes color: Int, @ColorRes strokeColor: Int, strokeWidth: Int) {
    this.shapeDrawableBuilder.solidColor = getAppColor(this.context, color)
    this.shapeDrawableBuilder.strokeWidth = strokeWidth
    this.shapeDrawableBuilder.strokeColor = getAppColor(this.context, strokeColor)
    this.shapeDrawableBuilder.intoBackground()
}


fun ShapeTextView.setBackGradientColor(@ColorRes startColor: Int, @ColorRes endColor: Int) {
    this.shapeDrawableBuilder.setSolidGradientColors(
        getAppColor(this.context, startColor),
        getAppColor(this.context, endColor)
    )
    this.shapeDrawableBuilder.intoBackground()
}


fun ShapeTextView.setStrokeColorAndStrokeWidth(@ColorRes color: Int, strokeWidth: Int) {
    this.shapeDrawableBuilder.strokeWidth = strokeWidth
    this.shapeDrawableBuilder.strokeColor = getAppColor(this.context, color)
    this.shapeDrawableBuilder.intoBackground()
}

fun ShapeTextView.setStrokeWidth(strokeWidth: Int) {
    this.shapeDrawableBuilder.strokeWidth = strokeWidth
    this.shapeDrawableBuilder.intoBackground()
}

fun ShapeTextView.setBackColorAndStrokeWidth(@ColorRes color: Int, strokeWidth: Int) {
    this.shapeDrawableBuilder.strokeWidth = strokeWidth
    this.shapeDrawableBuilder.solidColor = getAppColor(this.context, color)
    this.shapeDrawableBuilder.intoBackground()
}

fun ShapeTextView.setBackColorAndRadius(
    @ColorRes color: Int,
    topLeftRadius: Int,
    topRightRadius: Int,
    bottomLeftRadius: Int,
    bottomRightRadius: Int
) {
    this.shapeDrawableBuilder.solidColor = getAppColor(this.context, color)
    this.shapeDrawableBuilder.setRadius(
        getAppDimension(this.context, topLeftRadius),
        getAppDimension(this.context, topRightRadius),
        getAppDimension(this.context, bottomLeftRadius),
        getAppDimension(this.context, bottomRightRadius)
    )
    this.shapeDrawableBuilder.intoBackground()
}


fun ShapeTextView.setTextGradientColor(
    @ColorRes startColor: Int,
    @ColorRes centerColor: Int,
    @ColorRes endColor: Int
) {
    this.textColorBuilder.setTextGradientColors(
        getAppColor(this.context, startColor),
        getAppColor(this.context, centerColor),
        getAppColor(this.context, endColor)
    )
    this.textColorBuilder.intoTextColor()
}

fun ShapeTextView.setTextGradientColor(
    @ColorRes startColor: Int,
    @ColorRes endColor: Int
) {
    this.textColorBuilder.setTextGradientColors(
        getAppColor(this.context, startColor),
        getAppColor(this.context, endColor)
    )
    this.textColorBuilder.intoTextColor()
}


fun ShapeTextView.setBackColorAndStrokeColor(
    @ColorRes startColor: Int,
    @ColorRes endColor: Int,
    @ColorRes strokeColor: Int
) {
    this.shapeDrawableBuilder.solidGradientColors = intArrayOf(getAppColor(this.context, startColor), getAppColor(this.context, endColor))
    this.shapeDrawableBuilder.strokeColor = getAppColor(this.context, strokeColor)
    this.shapeDrawableBuilder.intoBackground()
}

fun ShapeEditText.setStrokeColor(@ColorRes strokeColor: Int) {
    this.shapeDrawableBuilder.strokeColor = getAppColor(this.context, strokeColor)
    this.shapeDrawableBuilder.intoBackground()
}


fun ShapeConstraintLayout.setBackColor(@ColorRes color: Int) {
    this.shapeDrawableBuilder.solidColor = getAppColor(this.context, color)
    this.shapeDrawableBuilder.intoBackground()
}

fun ShapeConstraintLayout.setBackColor2(color: Int) {
    this.shapeDrawableBuilder.solidColor = color
    this.shapeDrawableBuilder.intoBackground()
}

fun ShapeRelativeLayout.setBackColor(@ColorRes color: Int, radius: Float) {
    this.shapeDrawableBuilder.solidColor = getAppColor(this.context, color)
    this.shapeDrawableBuilder.setRadius(radius)
    this.shapeDrawableBuilder.intoBackground()
}

fun ShapeLinearLayout.setBackColor(@ColorRes color: Int, radius: Float) {
    this.shapeDrawableBuilder.solidColor = getAppColor(this.context, color)
    this.shapeDrawableBuilder.setRadius(radius)
    this.shapeDrawableBuilder.intoBackground()
}

// Temporarily commented out due to ConstraintLayout access issue
// fun ShapeConstraintBroadLayout.setBackColor(@ColorRes color: Int, radius: Float) {
//     this.shapeDrawableBuilder.solidColor = getAppColor(this.context, color)
//     this.shapeDrawableBuilder.setRadius(radius)
//     this.shapeDrawableBuilder.intoBackground()
// }

fun ShapeView.setBackColor(@ColorRes color: Int, radius: Float) {
    this.shapeDrawableBuilder.solidColor = getAppColor(this.context, color)
    this.shapeDrawableBuilder.setRadius(radius)
    this.shapeDrawableBuilder.intoBackground()
}

fun ShapeView.setBackColor(@ColorRes color: Int) {
    this.shapeDrawableBuilder.solidColor = getAppColor(this.context, color)
    this.shapeDrawableBuilder.intoBackground()
}

fun ShapeView.seStrokeColor(@ColorRes color: Int) {
    this.shapeDrawableBuilder.strokeColor = getAppColor(this.context, color)
    this.shapeDrawableBuilder.intoBackground()
}

fun ShapeRelativeLayout.setBackColor(@ColorRes color: Int) {
    this.shapeDrawableBuilder.solidColor = getAppColor(this.context, color)
    this.shapeDrawableBuilder.intoBackground()
}

//fun ShapeRelativeLayout.setRadius(radius: Int) {
//    this.shapeDrawableBuilder.innerRadius=radius
//    this.shapeDrawableBuilder.intoBackground()
//}

fun ShapeLinearLayout.setBackColor(@ColorRes color: Int) {
    this.shapeDrawableBuilder.solidColor = getAppColor(this.context, color)
    this.shapeDrawableBuilder.intoBackground()
}

fun ShapeLinearLayout.setBackColorAndStrokeColor(@ColorRes backColor: Int, @ColorRes strokeColor: Int) {
    this.shapeDrawableBuilder.solidColor = getAppColor(this.context, backColor)
    this.shapeDrawableBuilder.strokeColor = getAppColor(this.context, strokeColor)
    this.shapeDrawableBuilder.intoBackground()
}


fun ShapeLinearLayout.setBackGradientColor(@ColorRes startColor: Int, @ColorRes endColor: Int) {
    this.shapeDrawableBuilder.setSolidGradientColors(
        getAppColor(this.context, startColor),
        getAppColor(this.context, endColor)
    )
    this.shapeDrawableBuilder.intoBackground()
}

fun ShapeLinearLayout.setBackColorAndStrokeWidth(@ColorRes color: Int, strokeWidth: Int) {
    this.shapeDrawableBuilder.strokeWidth = strokeWidth
    this.shapeDrawableBuilder.solidColor = getAppColor(this.context, color)
    this.shapeDrawableBuilder.intoBackground()
}

fun ShapeEditText.setStrokeColor(
    @ColorRes focusColor: Int = com.cboda.baselibrary.R.color.color_3B47DD,
    @ColorRes noFocusColor: Int = com.cboda.baselibrary.R.color.color_0E1832,
    callback: (Boolean) -> Unit = {}
) {
    this.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
        this.setStrokeColor(if (hasFocus) focusColor else noFocusColor)
        callback.invoke(hasFocus)
    }
}


fun ShapeEditText.setStrokeColorAndStartAlpha(
    @ColorRes focusColor: Int = com.cboda.baselibrary.R.color.color_3B47DD,
    @ColorRes noFocusColor: Int = com.cboda.baselibrary.R.color.color_0E1832,
    noFocusAlpha: Float = 0.5f,
    callback: (Boolean) -> Unit = {}
) {
    this.setDrawableStartAlpha(noFocusAlpha, start = true)
    this.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
        this.setStrokeColor(if (hasFocus) focusColor else noFocusColor)
        this.setDrawableStartAlpha(if (hasFocus) 1f else noFocusAlpha, start = true)
        callback.invoke(hasFocus)
    }
}

fun getUuIdMd5(): String {
    return try {
        encryptMD5ToString(getUuIdCreate())
    } catch (e: Exception) {
//        e.toLog(tAG)
        "defaultUUId"
    }
}

fun getUuIdCreate(): String {
    return try {
        getUuid2() + getTimeUuId()
    } catch (e: Exception) {
//        e.toLog(tAG)
        "defaultUUId"
    }
}

fun getUuid2(): String {
    return try {
        UUID.randomUUID().toString().replace("-", "")
    } catch (e: Exception) {
//        e.toLog(tAG)
        "defaultUUId"
    }
}

private fun getTimeUuId(): String {
    try {
        val timeStr = ("" + TimeUtils.getNowMills() + getChar(13)).trim()
        var timeUuId = timeStr
        try {
            if (timeStr.length > 25) {
                timeUuId = ""
                val timeLengthBy2 = timeStr.length / 2
                for (i in 0 until timeLengthBy2) {
                    timeUuId = timeUuId + timeStr.substring(i, i + 1) + timeStr.substring(
                        i + timeLengthBy2,
                        i + 1 + timeLengthBy2
                    )
                }
            }
        } catch (e: Exception) {
//            e.toLog(tAG)
            timeUuId = timeStr
        }
        return timeUuId
    } catch (e: Exception) {
//        e.toLog(tAG)
        return ""
    }
}

private fun getChar(length: Int): String {
    var valSb = ""
    val charStr = "AaBbCcDdEeFfGgHhIiJjKkLlMmNnOoPpQqRrSsTtUuVvWwXxYyZz"
    try {
        val random = Random()
        val charLength = charStr.length
        for (i in 0 until length) {
            val index = random.nextInt(charLength)
            valSb += charStr[index]
        }
    } catch (e: Exception) {
//        e.toLog(tAG)
        if (charStr.length > length + 1) {
            valSb = charStr.substring(0, length)
        } else {
            while (valSb.length < length) {
                valSb += charStr
            }
        }
    }
    return valSb
}

fun getExFilePath(): String {
    var filePath = ""
    val files = getAppContext().getExternalFilesDirs(null)
    if (files.isNotEmpty()) {
        filePath = files[0].path
    }
    return filePath
}

const val saveFileName = "appSaveFile"
fun getSaveImgFilePath() = getExFilePath() + File.separatorChar + saveFileName


//保存图片
fun saveImg(
    bitmap: Bitmap?,
    fileName: String,
    isPng: Boolean = false,
    canRecycle: Boolean = false,
    callback: (String) -> Unit = {}
) {
    //    var returnPath = ""
    bitmap?.also {
        val os = ByteArrayOutputStream()
        var fos: FileOutputStream? = null
        try {
            val path = getSaveImgFilePath()
            if (FileUtils.createOrExistsDir(path)) {
                //            val isHasSDCard = Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
                //            if (isHasSDCard) {
                //                val sdRoot = Environment.getExternalStorageDirectory()
                val filePath = path + File.separatorChar + fileName
                //                eLogD(tAG + "_delPath", "filePath:$filePath")
                if (FileUtils.isFileExists(filePath)) {
                    FileUtils.delete(filePath)
                }
                bitmap.compress(
                    if (isPng) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG,
                    100,
                    os
                )
                val file = File(path, fileName)
                try {
                    fos = FileOutputStream(file)
                    fos.write(os.toByteArray(), 0, os.toByteArray().size)
                    fos.flush()
                } catch (e: Exception) {
                    e.toLog(tAG)
                }
                callback.invoke(file.path)
            }
        } catch (e1: Exception) {
            e1.toLog(tAG)
        }
        try {
            os?.close()
            fos?.close()
            if (canRecycle) {
                bitmap?.recycle()
            }
        } catch (e: Exception) {
            e.toLog(tAG)
        }
    }
}

//fun snapView(view: View?): Bitmap? {
//    var bitmap: Bitmap? = null
//    view?.also {
//        try {
//            view.isDrawingCacheEnabled = true
//            view.buildDrawingCache()
//            bitmap = Bitmap.createBitmap(view.drawingCache)
//            view.isDrawingCacheEnabled = false
//        } catch (e: Exception) {
//            eLogD(tAG, e)
//        }
//    }
//    return bitmap
//}


/**
 * 得到bitmap的大小
 */
fun getBitmapSize(bitmap: Bitmap?): Int {
    return try {
        if (null != bitmap) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {    //API 19
                bitmap.allocationByteCount
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) { //API 12
                    bitmap.byteCount
                } else bitmap.rowBytes * bitmap.height
            }
        } else {
            0
        }
    } catch (e: Exception) {
        e.toLog(tAG)
        0
    }
}


//深度克隆，深度拷贝
fun <T> deepClone(data: T, classOfT: Class<T>): T? {
    return try {
        fromJson2(toJson2(data), classOfT)
    } catch (e: Exception) {
        null
    }
}

fun View.getDimen(res: Int) = this.context.resources.getDimensionPixelOffset(res)


fun checkAppIsRunning(appPackName: String, context: Context? = null): Boolean {
    val mContext = getAppContext(context)
    val activityManager = mContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val pI = activityManager.runningAppProcesses
    for (i in pI.indices) {
        if (pI[i].processName == appPackName) {
            return true
        }
    }
    return false
}

fun TextView.setLeftImg(@DrawableRes int: Int) {
// Kotlin
    setCompoundDrawablesWithIntrinsicBounds(
        int,  // left
        0,                // top
        0,                // right
        0                 // bottom
    )
}

fun TextView.setLeftImgAndTextColor(@DrawableRes int: Int, @ColorRes color: Int) {
// Kotlin
    setCompoundDrawablesWithIntrinsicBounds(
        int,  // left
        0,                // top
        0,                // right
        0                 // bottom
    )
    setTextColor(ContextCompat.getColor(context, color))
}

fun EditText.setInputTypeKeepingCursor(inputType: Int) {
    // 1. 保存光标位置
    val selectionStart = this.selectionStart
    val selectionEnd = this.selectionEnd

    // 2. 修改 inputType
    this.inputType = inputType

    // 3. 安全恢复光标位置
    val textLength = this.text?.length ?: 0
    val safeStart = selectionStart.coerceIn(0, textLength)
    val safeEnd = selectionEnd.coerceIn(0, textLength)

    this.setSelection(safeStart, safeEnd)
}

fun EditText.setEdTextChange(callback: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

        }

        override fun afterTextChanged(s: Editable?) {
            callback.invoke(this@setEdTextChange.text.toString())
        }
    })
}

fun insertSpacesRandomly(input: String): String {
    val intervals = listOf(2, 3, 4, 11, 5) // 给定的间隔序列
    val result = StringBuilder()
    var index = 0
    var intervalIndex = 0

    for (i in input.indices) {
        result.append(input[i])
        index++

        // 当前段已达到指定间隔，且不是最后一个字符
        if (index == intervals[intervalIndex] && i < input.lastIndex) {
            result.append(' ') // 插入空格
            index = 0 // 重置当前段计数
            intervalIndex = (intervalIndex + 1) % intervals.size // 循环取下一个间隔
        }
    }

    return result.toString()
}

//修改edittext内的图片的透明度
fun TextView.setDrawableStartAlpha(
    alpha: Float = 0.5f,
    start: Boolean = false,
    top: Boolean = false,
    end: Boolean = false,
    bottom: Boolean = false
) {
    if (start || top || end || bottom) {
        // 1. 获取当前 drawables（start, top, end, bottom）
        val drawables = this.compoundDrawablesRelative
        var drawableStart = drawables[0]
        var drawableTop = drawables[1]
        var drawableEnd = drawables[2]
        var drawableBottom = drawables[3]
        if (start && null == drawableStart) {
            return
        } else if (start) {
            val mutatedDrawable = drawableStart.mutate()
            mutatedDrawable.alpha = (255 * alpha).toInt()
            drawableStart = mutatedDrawable
        }
        if (top && null == drawableTop) {
            return
        } else if (top) {
            val mutatedDrawable = drawableTop.mutate()
            mutatedDrawable.alpha = (255 * alpha).toInt()
            drawableTop = mutatedDrawable
        }
        if (end && null == drawableEnd) {
            return
        } else if (end) {
            val mutatedDrawable = drawableEnd.mutate()
            mutatedDrawable.alpha = (255 * alpha).toInt()
            drawableEnd = mutatedDrawable
        }
        if (bottom && null == drawableBottom) {
            return
        } else if (bottom) {
            val mutatedDrawable = drawableBottom.mutate()
            mutatedDrawable.alpha = (255 * alpha).toInt()
            drawableBottom = mutatedDrawable
        }
        this.setCompoundDrawablesRelative(
            drawableStart,
            drawableTop,
            drawableEnd,
            drawableBottom
        )
    }
}

fun Context?.getOtherAppIntent(appUrl: String?): Intent? {
    if (null != appUrl && !TextUtils.isEmpty(appUrl)) {
        return getAppContext(this).packageManager.getLaunchIntentForPackage(appUrl)
    }
    return null
}

//获取包名
fun getAppPackName(): String {
    return try {
        getAppContext().packageName
    } catch (e: Exception) {
        e.toLog(tAG)
        "com.cboda.intelligent"
    }
}


fun showEditDialog(callBack: (String) -> Unit) {
    getResumeActivity()?.also { activity ->
        val etView = AppCompatEditText(activity)
        etView.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(50))
        val builder =
            AlertDialog.Builder(activity, R.style.NormalDialog).setView(etView)
                .setNegativeButton("取消", null)
                .setPositiveButton(
                    "确定"
                ) { _, _ -> callBack.invoke(etView.text.toString().trim()) }
        val alertDialog = builder.create()
        alertDialog.setCancelable(true)
        alertDialog.setCanceledOnTouchOutside(true)
        alertDialog.show()
        etView.setText("http:///api/")
    }
}

fun showAlertDialogList(items: Array<String>, callBack: (Int, Any) -> Unit) {
//    val list = ArrayList<Any>()
//    items.forEach {
//        list.add(it)
//    }
    showCenterListDialog(items, callBack)
}

fun showAlertDialogList(items: ArrayList<String>, callBack: (Int, Any) -> Unit) {
//    showCenterListDialog(ArrayList<Any>().apply { addAll(items) }, callBack)
    showCenterListDialog(items.toTypedArray(), callBack)
}


fun showCenterListDialog(items: Array<String>, callBack: (Int, Any) -> Unit) {
    AlertDialog.Builder(getAppContext())
        .setTitle("请选择")
        .setItems(items) { _, which ->
            callBack.invoke(which, items[which])
        }
        .show()
}

//edittext限制只能输入数字和英文(最大长度)
fun EditText.setIsNumAndEng(maxLength: Int = 0) {
    val inputFilter = InputFilter { source, start, end, _, _, _ ->
        val regex = "[a-zA-Z0-9]" // 匹配范围
        val isMatch = (start until end).all { index ->
            source[index].toString().matches(regex.toRegex())
        }
        if (!isMatch) "" else null // 返回空字符串拒绝非法字符，null表示不修改
    }
    val ifArray = if (maxLength > 0) {
        arrayOf(inputFilter, InputFilter.LengthFilter(maxLength))
    } else {
        arrayOf(inputFilter)
    }
    this.filters = ifArray
}


//edittext限制只能输入数字和英文和标点符号(最大长度)
fun EditText.setIsNumAndEngAndSymbol(maxLength: Int = 0) {
    val inputFilter = InputFilter { source, start, end, _, _, _ ->
        val allowedSpecialChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789~!@#$%^&*()_+-=[]{}|;:',.<>?/"
        if (source.isNullOrEmpty()) {
            null
        } else {
            // 遍历输入的每一个字符
            var returnData: String? = null
            for (i in start until end) {
                val char = source[i].toString()
                // 允许：字母、数字、指定特殊符号
                val isAllowed = allowedSpecialChars.contains(char)
                if (!isAllowed) {
                    // 发现非法字符（如中文、空格、Emoji），返回空字符串拦截
                    returnData = ""
                }
            }
            returnData
        }
    }
    val ifArray = if (maxLength > 0) {
        arrayOf(inputFilter, InputFilter.LengthFilter(maxLength))
    } else {
        arrayOf(inputFilter)
    }
    this.filters = ifArray
}


//edittext限制输入最大值和精确值
class AmountInputFilter(
    private val maxAmount: Double = 1000.0,
    private val decimalPlaces: Int = 2
) : InputFilter {

    override fun filter(
        source: CharSequence?,
        start: Int,
        end: Int,
        dest: Spanned?,
        dstart: Int,
        dend: Int
    ): CharSequence? {
        // 允许删除（source 为空）
        if (source.isNullOrEmpty()) return null

        // 构建新字符串
        val currentText = dest?.toString() ?: ""
        val newText = currentText.take(dstart) + source + currentText.substring(dend)

        // 空字符串允许
        if (newText.isEmpty()) return null

        // 不允许以多个 0 开头（如 "00", "01"），但允许 "0" 或 "0.xxx"
        if (newText.startsWith("0") && newText.length > 1 && newText[1] != '.') {
            return "" // 拒绝输入
        }

        // 不允许开头是小数点（如 ".5"），可选：自动补 0，但这里先拒绝
        if (newText.startsWith(".")) {
            return "" // 或返回 "0." 如果你想自动补
        }

        // 检查格式：只能是数字 + 可选小数点 + 最多 decimalPlaces 位小数
        val regex = "^(\\d{1,})?(\\.\\d{0,$decimalPlaces})?$".toRegex()
        if (!regex.matches(newText)) {
            return ""
        }

        // 尝试转为数字并校验最大值
        return try {
            val value = newText.toDouble()
            if (value <= maxAmount) {
                null // 接受
            } else {
                "" // 超出最大值，拒绝
            }
        } catch (e: NumberFormatException) {
            "" // 非法数字（理论上不会触发，因正则已过滤）
        }
    }
}

private var dialogHelper: LoadingBroadDialog? = null
fun showLoadingDialog(context: Context? = null, title: Int = 0, canReturn: Boolean = false, dismissCallback: () -> Unit = {}): LoadingBroadDialog? {
    return showLoadingDialog(context, if (title != 0) getAppContext(context).getString(title) else "", canReturn, dismissCallback)
}

fun showLoadingDialog(context: Context? = null, title: String = "", canReturn: Boolean = false, dismissCallback: () -> Unit = {}): LoadingBroadDialog? {
    dismissLoadingDialog()
    dialogHelper = LoadingBroadDialog(getAppContext(context), title)
    dialogHelper?.show()
    dialogHelper?.setCanceledOnTouchOutside(false)
    dialogHelper?.setCancelable(canReturn)
    dialogHelper?.setOnDismissListener {
        dismissCallback.invoke()
    }
    return dialogHelper
}

fun dismissLoadingDialog() {
    if (null != dialogHelper) {
        dialogHelper?.dismiss()
    }
}


fun TextView.setTxt(res: Int) {
    this.text = this.context.getString(res)
}

fun TextView.setTxtColor(@ColorRes color: Int) {
    this.setTextColor(getAppColor(this.context, color))
}

fun anyToInt(any: Any): Int {
    return try {
        any.toString().toFloat().toInt()
    } catch (e: Exception) {
        0
    }
}

fun TextView.initBold(style: Int) {
    try {
        when (style) {
            1 -> {
                typeface = Typeface.createFromAsset(context.assets, "font/broad_num_bold.ttf")
            }
        }
    } catch (e: Exception) {
        e.toLog(tAG)
    }
}


fun getAppStr(@StringRes res: Int, context: Context? = null): String = getAppContext(context).getString(res)

fun showCommonTip(
    @StringRes title: Int = R.string.wenxintishi,
    @StringRes content: Int,
    @StringRes cancelBtn: Int = R.string.bs_quxiao,
    @StringRes sureBtn: Int = R.string.bs_mine_sure,
    context: Context? = null,
    block: (Boolean) -> Unit
): CommonTipBroadPop {
    val mContext = getAppContext(context)
    return showCommonTip(
        title, content, cancelBtn, sureBtn,
        R.color.white, R.color.white, R.color.black, R.color.black_65,
        R.color.color_3B47DDk, R.color.white, R.color.color_3B47DD, R.color.color_3B47DD,
        R.color.white, R.color.white, R.color.color_3B47DD, R.color.color_3B47DD,
        mContext, block
    )
}

fun showCommonTip(
    @StringRes title: Int = R.string.wenxintishi,
    @StringRes content: Int,
    @StringRes cancelBtn: Int = R.string.bs_quxiao,
    @StringRes sureBtn: Int = R.string.bs_mine_sure,
    backColor: Int, strokeColor: Int,
    titleColor: Int, contentColor: Int, cancelTvColor: Int, sureTvColor: Int,
    cancelStrokeColor: Int, sureStrokeColor: Int,
    cancelStartColor: Int, cancelEndColor: Int,
    sureStartColor: Int, sureEndColor: Int,
    context: Context? = null,
    block: (Boolean) -> Unit
): CommonTipBroadPop {
    val mContext = getAppContext(context)
    return showCommonStrTip(
        getAppStr(title, mContext), getAppStr(content, mContext), getAppStr(cancelBtn, mContext), getAppStr(sureBtn, mContext), mContext,
        backColor, strokeColor, titleColor, contentColor, cancelTvColor, sureTvColor, cancelStrokeColor, sureStrokeColor, cancelStartColor, cancelEndColor, sureStartColor, sureEndColor, block
    )
}

fun showCommonStrTip(
    title: String, content: String, cancelBtn: String, sureBtn: String, context: Context? = null, backColor: Int = R.color.white, strokeColor: Int = R.color.white,
    titleColor: Int = R.color.black, contentColor: Int = R.color.black_65, cancelTvColor: Int = R.color.color_3B47DDk, sureTvColor: Int = R.color.white,
    cancelStrokeColor: Int = R.color.color_3B47DD, sureStrokeColor: Int = R.color.color_3B47DD,
    cancelStartColor: Int = R.color.white, cancelEndColor: Int = R.color.white,
    sureStartColor: Int = R.color.color_3B47DD, sureEndColor: Int = R.color.color_3B47DD, block: (Boolean) -> Unit
): CommonTipBroadPop {
    val mContext = getAppContext(context)
    val commonTipPop = CommonTipBroadPop(
        mContext,
        title,
        content,
        backColor, strokeColor, titleColor, contentColor, cancelTvColor, sureTvColor, cancelStrokeColor, sureStrokeColor, cancelStartColor, cancelEndColor, sureStartColor, sureEndColor
    )
    commonTipPop.setCancelBtn(cancelBtn)
    commonTipPop.setSureBtn(sureBtn)
    commonTipPop.sureInvoke = {
        block.invoke(true)
    }
    commonTipPop.cancelInvoke = {
        block.invoke(false)
    }
    commonTipPop.showPopupWindow()
    return commonTipPop
}

fun showCommonBottomListPop(
    list: List<ComBottomListBean>,
    context: Context? = null,
    @ColorRes viewColor: Int = R.color.color_F7F8FA,
    block: (Int, ComBottomListBean) -> Unit
): CommonBottomListPop {
    val mContext = getAppContext(context)
    val commonTipPop = CommonBottomListPop(
        mContext,
        list,
        viewColor
    )
    commonTipPop.backInvoke = { pos, bean ->
        block.invoke(pos, bean)
    }
    commonTipPop.showPopupWindow()
    return commonTipPop
}

fun showMeetLeaveTip(context: Context? = null, block: () -> Unit = {}) {
    val mContext = getAppContext(context)
    val commonTipPop = CommonTipBroadPop(
        mContext,
        mContext.getString(R.string.wenxintishi),
        mContext.getString(R.string.chat_meet_leave_tip)
    )
    commonTipPop.setCancelBtn(mContext.getString(R.string.bs_quxiao))
    commonTipPop.setSureBtn(mContext.getString(R.string.bs_mine_sure))
    commonTipPop.sureInvoke = {
        block.invoke()
    }
    commonTipPop.showPopupWindow()
}

//裁视图轮廓圆角
fun clipViewOutlineRound(view: View, radiusDp: Int) {
    val radius = radiusDp * view.resources.displayMetrics.density
    view.post {
        view.clipToOutline = true
        view.outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline?) {
                outline?.setRoundRect(0, 0, view.width, view.height, radius)
            }
        }
    }

}