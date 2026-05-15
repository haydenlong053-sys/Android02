package com.cboda.intelligent.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.SystemClock
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Base64
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import androidx.annotation.DrawableRes
import com.cboda.baselibrary.utils.getAppContext
import com.bumptech.glide.Glide
import com.cboda.intelligent.R
import com.cboda.intelligent.base.BaseSimple2BroadActivity
import com.cboda.intelligent.base.BaseSimpleBroadActivity
import com.cboda.intelligent.dialog.pop.AuthPasswordBroadPop
import com.cboda.baselibrary.popup.CommonTipBroadPop
import com.cboda.intelligent.configuration.CommonBroadConfig
import com.cboda.intelligent.configuration.SafeJumpBroadEnum
import com.cboda.intelligent.utils.AppBroadUtils.Companion.activityList
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
        ToastBroadUtil.show(context.getString(R.string.复制成功))
    }

}

// 方法1：直接替换内容
fun pasteToEditText(editText: EditText, canReplaceAllData: Boolean = false) {
    val context = editText.context
    val clipboardText = ClipboardPasteBroadUtils.getTextFromClipboard(context)
    if (clipboardText != null) {
        if (canReplaceAllData && !TextUtils.isEmpty(clipboardText)) {
            editText.setText("")
        }
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
    runOnUiThread { ToastBroadUtil.show(getAppContext().getString(message)) }
}

fun showToastMain(message: String) {
    runOnUiThread { ToastBroadUtil.show(message) }
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
    return str?.contains(CommonBroadConfig.seekWeb4) ?: false
}

//是否为区块钱包的code
fun isEthWalletLinkQrCode(str: String?): Boolean {
    return str?.startsWith(CommonBroadConfig.sw4bc) ?: false
}

fun View.isCanGo(boolean: Boolean) {
    this.isEnabled = boolean
    this.isSelected = boolean
}

fun View.isSubmitCanGo(
    boolean: Boolean,
    @DrawableRes negativeBg: Int = R.drawable.bg_rect_submit_no_broad_16dp,
    @DrawableRes positiveBg: Int = R.drawable.bg_rect_submit_broad_16dp,
) {
    this.isEnabled = boolean
    this.isSelected = boolean
    this.isSubmitCanGo2(boolean,negativeBg,positiveBg)
}

fun View.isSubmitCanGo2(
    boolean: Boolean,
    @DrawableRes negativeBg: Int = R.drawable.bg_rect_submit_no_broad_16dp,
    @DrawableRes positiveBg: Int = R.drawable.bg_rect_submit_broad_16dp,
) {
    this.setBackgroundResource(if (boolean) positiveBg else negativeBg)
}

fun getTopBaseSimple2Activity(): BaseSimple2BroadActivity? {
    if (activityList.isNotEmpty() && activityList.last() is BaseSimple2BroadActivity) {
        return activityList.last() as BaseSimple2BroadActivity
    }
    return null
}

fun showLoading() {
    if (activityList.isNotEmpty() && activityList.last() is BaseSimple2BroadActivity) {
        (activityList.last() as BaseSimple2BroadActivity).showLoading()
    } else if (activityList.isNotEmpty() && activityList.last() is BaseSimpleBroadActivity) {
        (activityList.last() as BaseSimpleBroadActivity).showLoading()
    }
}

fun dismissLoading() {
    if (activityList.isNotEmpty() && activityList.last() is BaseSimple2BroadActivity) {
        (activityList.last() as BaseSimple2BroadActivity).dismissLoading()
    } else if (activityList.isNotEmpty() && activityList.last() is BaseSimpleBroadActivity) {
        (activityList.last() as BaseSimpleBroadActivity).dismissLoading()
    }
}


fun showGuidePasswordTip(context: Context): CommonTipBroadPop {
    val commonTipPop = CommonTipBroadPop(
        context,
        context.getString(R.string.mine_tip_password),
        context.getString(R.string.mine_tip_password_des)
    )
    commonTipPop.setCancelBtn(context.getString(R.string.mine_tip_wait))
    commonTipPop.setSureBtn(context.getString(R.string.mine_ok))
    commonTipPop.sureInvoke = {
        val mPop = AuthPasswordBroadPop(context, SafeJumpBroadEnum.PasswordBind.sourceType)
        mPop.showPopupWindow()
    }
    commonTipPop.showPopupWindow()
    return commonTipPop
}

fun showSetTipDialog(context: Context, goBack: () -> Unit = {}): CommonTipBroadPop {
    val commonTipPop = CommonTipBroadPop(
        context,
        context.getString(R.string.ai_pop_tip),
        context.getString(R.string.bind_device_warning)
    )
    commonTipPop.setCancelBtn(context.getString(R.string.quxiao))
    commonTipPop.setSureBtn(context.getString(R.string.got_it))
    commonTipPop.sureInvoke = {
        goBack.invoke()
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

//一级页面接口数据临时翻译处理
fun goTempTranslateString(context: Context, oldString: String): String {
    when (oldString) {
        "亲密圈" -> {
            return context.getString(R.string.circle_intimate)
        }

        "社交圈" -> {
            return context.getString(R.string.circle_social)
        }

        "工作圈" -> {
            return context.getString(R.string.circle_work)
        }

        "公关圈" -> {
            return context.getString(R.string.circle_public_relations)
        }

        "家人,恋人,最好的朋友", "家人 | 恋人 | 最好的朋友" -> {
            return context.getString(R.string.circle_intimate_des)
        }

        "朋友,同学,校友,网友", "朋友 | 同学 | 校友 | 网友" -> {
            return context.getString(R.string.circle_social_des)
        }

        "同事,客户，合作伙伴", "同事 | 客户 | 合作伙伴" -> {
            return context.getString(R.string.circle_work_des)
        }

        "陌生人,临时联系", "陌生人 | 临时联系" -> {
            return context.getString(R.string.circle_public_relations_des)
        }
    }

    return oldString
}