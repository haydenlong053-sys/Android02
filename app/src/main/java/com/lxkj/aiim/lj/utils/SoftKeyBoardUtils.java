package com.lxkj.aiim.lj.utils;

import android.content.Context;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import java.util.Timer;
import java.util.TimerTask;

/**
 * 软键盘工具类
 * Created by Tony Chen on 2015/12/27.
 */
public class SoftKeyBoardUtils {
    /**
     * 打开软键盘
     */
    public static void openSoftKeyBoard(final EditText et) {
        if (et == null) return;
        et.setFocusableInTouchMode(true);
        et.requestFocus();

        // 跳转页弹出软键盘，设置一个延迟，否则可能软键盘无法弹出
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                InputMethodManager inputManager = (InputMethodManager) et
                        .getContext().getSystemService(
                                Context.INPUT_METHOD_SERVICE);
                if (inputManager == null) return;
                inputManager.showSoftInput(et, 0);
            }
        }, 100);

}

    /**
     * 关闭软键盘
     */
    public static void closeSoftKeyBoard(Context context, EditText et) {
        if (et == null) return;
        et.requestFocus();
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm == null) return;
        imm.hideSoftInputFromWindow(et.getWindowToken(), 0);
    }
}
