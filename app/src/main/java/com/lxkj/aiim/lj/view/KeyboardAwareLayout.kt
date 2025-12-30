package com.lxkj.aiim.lj.view

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.widget.LinearLayout

class KeyboardAwareLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var keyboardHeight = 0
    private var originalPaddingBottom = 0
    private var isKeyboardShowing = false

    init {
        originalPaddingBottom = paddingBottom
        setupKeyboardListener()
    }

    private fun setupKeyboardListener() {
        viewTreeObserver.addOnGlobalLayoutListener {
            checkKeyboardState()
        }
    }

    private fun checkKeyboardState() {
        val rect = Rect()
        getWindowVisibleDisplayFrame(rect)

        val screenHeight = height
        val currentKeyboardHeight = screenHeight - rect.bottom

        if (currentKeyboardHeight > screenHeight * 0.15) {
            // 键盘显示
            if (!isKeyboardShowing) {
                isKeyboardShowing = true
                keyboardHeight = currentKeyboardHeight
                adjustLayoutForKeyboard()
            }
        } else {
            // 键盘隐藏
            if (isKeyboardShowing) {
                isKeyboardShowing = false
                restoreLayout()
            }
        }
    }

    private fun adjustLayoutForKeyboard() {
        // 增加底部内边距
        setPadding(
            paddingLeft,
            paddingTop,
            paddingRight,
            originalPaddingBottom + keyboardHeight
        )

        // 确保焦点视图可见
        ensureFocusedViewVisible()
    }

    private fun ensureFocusedViewVisible() {
        val focusedView = findFocus()
        if (focusedView != null) {
            post {
                focusedView.requestRectangleOnScreen(
                    Rect(
                        0,
                        0,
                        focusedView.width,
                        focusedView.height + keyboardHeight
                    )
                )
            }
        }
    }

    private fun restoreLayout() {
        setPadding(
            paddingLeft,
            paddingTop,
            paddingRight,
            originalPaddingBottom
        )
    }
}