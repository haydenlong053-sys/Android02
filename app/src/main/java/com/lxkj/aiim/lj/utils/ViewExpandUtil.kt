package com.lxkj.aiim.lj.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.SystemClock
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.DrawableRes
import com.cboda.baselibrary.utils.getAppContext
import com.bumptech.glide.Glide
import com.luck.picture.lib.thread.PictureThreadUtils
import com.lxkj.aiim.R
import com.lxkj.aiim.base.BaseSimple2Activity
import com.lxkj.aiim.base.BaseSimpleActivity
import com.lxkj.aiim.dialog.pop.AuthPasswordPop
import com.lxkj.aiim.dialog.pop.CommonTipPop
import com.lxkj.aiim.lj.configuration.CommonConfig
import com.lxkj.aiim.lj.configuration.SafeJumpEnum
import com.lxkj.aiim.utils.AppUtil.Companion.activityList
import com.lzy.okgo.utils.HttpUtils.runOnUiThread
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

// 复制内容到剪切板
fun copyToClipboard(context: Context, text: String?) {
    if (text?.isNotEmpty() == true) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("label", text)
        clipboard.setPrimaryClip(clip)
        ToastUtil.show(context.getString(R.string.复制成功))
    }

}

// 方法1：直接替换内容
fun pasteToEditText(editText: EditText) {
    val context = editText.context
    val clipboardText = ClipboardPasteUtils.getTextFromClipboard(context)
    if (clipboardText != null) {
        val start = editText.selectionStart
        val end = editText.selectionEnd

        // 替换选中的文本，或者插入到光标位置
        editText.text.replace(
            minOf(start, end),
            maxOf(start, end),
            clipboardText
        )
    }
}

//展示base64的IV
fun showBase64QrCodeIv(context: Context, imageView: ImageView, strBase64: String?) {
    // 1. 清理数据：如果字符串包含Data URI前缀（"data:image/...;base64,"）
    if (strBase64?.isNotEmpty() == true && strBase64.contains("base64,")) {
        val base64String = strBase64.substring(strBase64.indexOf("base64,") + 7)
        try {
            // 2. 解码：将纯Base64字符串解码成字节数组[citation:2]
            val imageByteArray = Base64.decode(base64String, Base64.DEFAULT)

            // 3. 加载：使用Glide加载字节数组[citation:2]
            Glide.with(context)
                .load(imageByteArray)
                .into(imageView)

        } catch (e: IllegalArgumentException) {
            // 处理解码失败的情况，例如Base64字符串不合法
            e.printStackTrace()
        }
    }

}

fun showToastMain(message: Int) {
    runOnUiThread { ToastUtil.show(getAppContext().getString(message)) }
}

fun showToastMain(message: String) {
    runOnUiThread { ToastUtil.show(message) }
}

// 扩展函数版本 - 推荐
fun String.maskWithLastChar(): String {
    return when {
        isEmpty() -> ""
        length == 1 -> "*"
        else -> "*".repeat(length - 1) + last()
    }
}

// 扩展函数，将 EditText 文本变化转换为 Flow
fun EditText.textChanges(): Flow<CharSequence> = callbackFlow {
    val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable?) {
            s?.let { trySend(it) }
        }
    }
    addTextChangedListener(textWatcher)
    awaitClose { removeTextChangedListener(textWatcher) }
}

//是否为转账链接的code
fun isTransferLinkQrCode(str: String?): Boolean {
    return str?.contains(CommonConfig.seekWeb4) ?: false
}

fun View.isCanGo(boolean: Boolean) {
    this.isEnabled = boolean
    this.isSelected = boolean
}

fun View.isSubmitCanGo(
    boolean: Boolean,
    @DrawableRes negativeBg: Int = R.drawable.bg_rect_submit_no_16dp,
    @DrawableRes positiveBg: Int = R.drawable.bg_rect_submit_16dp,
) {
    this.isEnabled = boolean
    this.isSelected = boolean
    this.setBackgroundResource(if (boolean) positiveBg else negativeBg)
}

fun getTopBaseSimple2Activity(): BaseSimple2Activity? {
    if (activityList.isNotEmpty() && activityList.last() is BaseSimple2Activity) {
        return activityList.last() as BaseSimple2Activity
    }
    return null
}

fun showLoading() {
    if (activityList.isNotEmpty() && activityList.last() is BaseSimple2Activity) {
        (activityList.last() as BaseSimple2Activity).showLoading()
    } else if (activityList.isNotEmpty() && activityList.last() is BaseSimpleActivity) {
        (activityList.last() as BaseSimpleActivity).showLoading()
    }
}

fun dismissLoading() {
    if (activityList.isNotEmpty() && activityList.last() is BaseSimple2Activity) {
        (activityList.last() as BaseSimple2Activity).dismissLoading()
    } else if (activityList.isNotEmpty() && activityList.last() is BaseSimpleActivity) {
        (activityList.last() as BaseSimpleActivity).dismissLoading()
    }
}


fun showGuidePasswordTip(context: Context): CommonTipPop {
    val commonTipPop = CommonTipPop(
        context,
        context.getString(R.string.mine_tip_password),
        context.getString(R.string.mine_tip_password_des)
    )
    commonTipPop.setCancelBtn(context.getString(R.string.mine_tip_wait))
    commonTipPop.setSureBtn(context.getString(R.string.mine_ok))
    commonTipPop.sureInvoke = {
        val mPop = AuthPasswordPop(context, SafeJumpEnum.PasswordBind.sourceType)
        mPop.showPopupWindow()
    }
    commonTipPop.showPopupWindow()
    return commonTipPop
}

// 把这 10 行放进项目，以后所有点击都不用再担心重复
fun View.click(debounceTime: Long = 800, action: () -> Unit) {
    setOnClickListener {
        val now = SystemClock.uptimeMillis()
        val key = 0x7fffffff
        val last = (getTag(key) as? Long) ?: 0
        if (now - last >= debounceTime) {
            setTag(key, now)
            action()
        }
    }
}