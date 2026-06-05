package com.freddy.chat.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 配合 PanelSwitchHelper {@code auto_reset_area}：列表处理完滑动/点击后，
 * 在 {@link MotionEvent#ACTION_UP} 返回 false，让 {@code LinearContentContainer} 收起键盘/面板。
 */
public class PanelResetRecyclerView extends RecyclerView {

    public PanelResetRecyclerView(@NonNull Context context) {
        super(context);
    }

    public PanelResetRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PanelResetRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        boolean handled = super.onTouchEvent(e);
        if (e.getAction() == MotionEvent.ACTION_UP) {
            return false;
        }
        return handled;
    }
}
