package com.lxkj.aiim.utils

import android.app.Activity
import android.content.Context
import android.view.inputmethod.InputMethodManager
import android.widget.EditText


class KeyboardBroadUtils {

    companion object {
        /**
         * 开启软键盘
         */
        fun openSoftKeyboard(et: EditText) {
            val inputManager: InputMethodManager =
                et.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputManager.showSoftInput(et, 0)
        }

        /**
         * 关闭软键盘
         */
        fun closeSoftKeyboard(context: Context) {
            val inputMethodManager: InputMethodManager =
                context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            if ((context as Activity).currentFocus != null) {
                inputMethodManager.hideSoftInputFromWindow(
                    context.currentFocus?.windowToken, 0
                )
            }
        }

        /**
         * 关闭软键盘
         * 一般用于dialog隐藏软键盘,需要把输入框传递进来
         */
        fun closeSoftKeyboard(context: Context, editText: EditText?) {
            val inputMethodManager: InputMethodManager =
                context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            if (editText != null && editText.windowToken != null) {
                inputMethodManager.hideSoftInputFromWindow(editText.windowToken, 0)
            }
        }
    }
}