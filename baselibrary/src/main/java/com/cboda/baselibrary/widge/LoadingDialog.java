package com.cboda.baselibrary.widge;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import com.cboda.baselibrary.R;

public class LoadingDialog extends Dialog {
    private Context context;      // 上下文
    private static LoadingDialog mInstance;

    /**
     * 获取单例引用
     * @param context
     * @return
     */
    public static LoadingDialog getInstance(Context context) {
        if (mInstance == null) {
            if (mInstance == null) {
                mInstance = new LoadingDialog(context);
            }
        }
        return mInstance;
    }


    public LoadingDialog(final Context context) {
        super(context, R.style.Theme_dialog); //dialog的样式
        this.context = context;
        Window window = getWindow();
        window.setGravity(Gravity.CENTER); // 此处可以设置dialog显示的位置为居中
        setContentView(R.layout.layout_dialog_loading);
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.dimAmount = 0.0f;//dimAmount在0.0f和1.0f之间，0.0f完全不暗，即背景是可见的 ，1.0f时候，背景全部变黑暗。
        getWindow().setAttributes(lp);
        //要达到背景全变暗的效果，需设置getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND); 否则，背景无效果
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        setCanceledOnTouchOutside(true);// 点击Dialog外部消失
        setCancelable(true);
    }
}
