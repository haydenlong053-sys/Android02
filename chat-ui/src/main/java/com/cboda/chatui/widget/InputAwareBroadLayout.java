/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.SizeUtils;
import com.cboda.chatui.utils.ServiceBroadUtil;
import com.cboda.chatui.utils.ViewBroadUtil;

public class InputAwareBroadLayout extends KeyboardAwareLinearBroadLayout implements KeyboardAwareLinearBroadLayout.OnKeyboardShownListener {
    private InputView current;

    public InputAwareBroadLayout(Context context) {
        this(context, null);
    }

    public InputAwareBroadLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public InputAwareBroadLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        addOnKeyboardShownListener(this);
    }

    @Override
    public void onKeyboardShown() {
        hideAttachedInput(true);
    }

    public void show(@NonNull final EditText imeTarget, @NonNull final InputView input, int funcType) {
        if (isKeyboardOpen()) {
            hideSoftkey(imeTarget, new Runnable() {
                @Override
                public void run() {
                    hideAttachedInput(true);
                    if (funcType==1){
                        int px = SizeUtils.dp2px(217f);
                        input.show(px ,true);
                    }else {
                        input.show(getKeyboardHeight(), true);
                    }

                    current = input;
                }
            });
        } else {
            if (current != null) current.hide(true);
            if (funcType==1){
                int px = SizeUtils.dp2px(217f);
                input.show(px, current != null);
            }else {
                input.show(getKeyboardHeight(), current != null);
            }
            current = input;
        }
    }

    public InputView getCurrentInput() {
        return current;
    }

    public void hideCurrentInput(EditText imeTarget) {
        if (isKeyboardOpen()) hideSoftkey(imeTarget, null);
        else hideAttachedInput(false);
    }

    public void hideAttachedInput(boolean instant) {
        if (current != null) current.hide(instant);
        current = null;
    }

    public boolean isInputOpen() {
        return (isKeyboardOpen() || (current != null && current.isShowing()));
    }

    public void showSoftkey(final EditText inputTarget) {
        postOnKeyboardOpen(new Runnable() {
            @Override
            public void run() {
                hideAttachedInput(true);
            }
        });
        inputTarget.post(new Runnable() {
            @Override
            public void run() {
                inputTarget.requestFocus();
                ServiceBroadUtil.getInputMethodManager(inputTarget.getContext()).showSoftInput(inputTarget, 0);
            }
        });
    }

    public void hideSoftkey(final EditText inputTarget, @Nullable Runnable runAfterClose) {
        if (runAfterClose != null) postOnKeyboardClose(runAfterClose);

        ServiceBroadUtil.getInputMethodManager(inputTarget.getContext())
                .hideSoftInputFromWindow(inputTarget.getWindowToken(), 0);
    }

    public interface InputView {
        void show(int height, boolean immediate);

        void hide(boolean immediate);

        boolean isShowing();
    }
}

