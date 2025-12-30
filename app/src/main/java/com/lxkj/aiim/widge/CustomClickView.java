package com.lxkj.aiim.widge;

/**
 * Created by kxn on 2025/5/19 星期一.
 */
import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

public class CustomClickView extends View {

    private static final long DOUBLE_CLICK_TIME_DELAY = 300; // 双击时间间隔（毫秒）

    private long lastClickTime = 0;
    private OnClickAndDoubleClickListener listener;

    // 自定义View的构造函数，可以根据需要添加更多的构造函数
    public CustomClickView(Context context) {
        super(context);
        init();
    }

    public CustomClickView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomClickView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // 初始化代码，如设置背景、颜色等
    }

    // 设置单击和双击事件的回调接口
    public void setOnClickAndDoubleClickListener(OnClickAndDoubleClickListener listener) {
        this.listener = listener;
    }

    // 自定义的单击和双击事件回调接口
    public interface OnClickAndDoubleClickListener {
        void onSingleClick();
        void onDoubleClick();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastClickTime < DOUBLE_CLICK_TIME_DELAY) {
                    // 双击事件
                    if (listener != null) {
                        listener.onDoubleClick();
                    }
                    // 重置点击时间，避免误判为多次双击
                    lastClickTime = 0;
                } else {
                    // 单击事件（等待双击超时后再确认）
                    lastClickTime = currentTime;

                    // 使用Handler来延迟判断是否为单击事件
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (System.currentTimeMillis() - lastClickTime >= DOUBLE_CLICK_TIME_DELAY) {
                                // 确认是单击事件
                                if (listener != null) {
                                    listener.onSingleClick();
                                }
                            }
                            // 重置点击时间，为下一次点击做准备
                            lastClickTime = 0;
                        }
                    }, DOUBLE_CLICK_TIME_DELAY);
                }
                break;
        }
        return true; // 消耗事件，避免其他View处理
    }
}

