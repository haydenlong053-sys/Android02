package com.cboda.intelligent.holder

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.cboda.intelligent.R
import com.cboda.intelligent.utils.AppBroadUtils
import com.maning.pswedittextlibrary.MNPasswordEditText

class PayPsBroadHolder(
    private val mContext: Context,
    private val price: String,
    private val type:String = "1",//1 hongbao 2 zhuanzhang
) :
    Dialog(mContext) {
    private var qianNum: TextView? = null
    private var rootView: View? = null
    private var tvtips:TextView? = null
    var lisetener: InputLisetener? = null
    private var pswEdit: MNPasswordEditText? = null
    interface InputLisetener {
        fun inputResult(content: String)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pay_ps_dialog_broad_layout)
        qianNum = findViewById(R.id.qianNum)
        tvtips = findViewById(R.id.tvtips)
        pswEdit =  findViewById(R.id.pswEdit)
        tvtips?.text = if (type == "1")context.getString(R.string.hongbaojine) else context.getString(R.string.zhuanzhangjine)
        qianNum?.text = price

        findViewById<View>(R.id.closezhifu).setOnClickListener {
            dismiss()
        }

        rootView = (mContext as AppCompatActivity).window?.decorView
        val r = Rect()
        var lastHeight = 0
        //键盘监听
        rootView?.viewTreeObserver?.addOnGlobalLayoutListener {
            rootView?.getWindowVisibleDisplayFrame(r)
            val height = r.height()
            if (lastHeight == 0) {
                lastHeight = height
            } else {
                val diff = lastHeight - height
                if (diff > 200) {
                    lastHeight = height
                } else if (diff < -200) {
                    lastHeight = height
//                    dismiss()
                }
            }
        }
        pswEdit?.post {
            pswEdit?.requestFocus()
        }
        pswEdit?.setOnTextChangeListener(MNPasswordEditText.OnTextChangeListener { text, isComplete ->
            if (isComplete) {
                AppBroadUtils.debug("密码", "--> $text")
                lisetener?.inputResult(text ?: "")
                dismiss()
            }
        })


    }


    override fun dismiss() {
        rootView?.viewTreeObserver?.removeOnGlobalLayoutListener {

        }
        val view = currentFocus
        if (view is TextView) {
            view.postDelayed({
                val mInputMethodManager =
                    mContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                mInputMethodManager.hideSoftInputFromWindow(
                    view.getWindowToken(),
                    InputMethodManager.RESULT_UNCHANGED_SHOWN
                )
            }, 200)
        }
        super.dismiss()
    }

    override fun onStart() {
        super.onStart()
        window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            val mLayoutParams = attributes
            mLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
            mLayoutParams.dimAmount = 0.3f
            mLayoutParams.gravity = Gravity.BOTTOM
            attributes = mLayoutParams
            setWindowAnimations(R.style.main_menu_animstyle2)
            setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        }
    }
}