package com.cboda.intelligent.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.cboda.intelligent.R;
import com.cboda.intelligent.utils.ToastBroadUtil;



public class InputJhmBroadDialog extends Dialog {

    EditText etNum;
    TextView tvCancel;
    TextView tvConfirm;
    private Context context;      // 上下文
    OnInputListener onInputListener;

    public InputJhmBroadDialog(final Context context, final OnInputListener onInputListener) {
        super(context, com.cboda.baselibrary.R.style.Theme_dialog); //dialog的样式
        this.context = context;
        this.onInputListener = onInputListener;
        Window window = getWindow();
        window.setGravity(Gravity.CENTER); // 此处可以设置dialog显示的位置为居中
        setContentView(R.layout.layout_dialog_input_broad_jhm);
        WindowManager windowManager = ((Activity) context).getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.width = display.getWidth() * 6 / 7; // 设置dialog宽度为屏幕的4/5
        lp.dimAmount = 0.5f;//dimAmount在0.0f和1.0f之间，0.0f完全不暗，即背景是可见的 ，1.0f时候，背景全部变黑暗。
        getWindow().setAttributes(lp);
        //要达到背景全变暗的效果，需设置getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND); 否则，背景无效果
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        //遍历控件id,添加点击事件
        tvConfirm = findViewById(R.id.tvConfirm);
        etNum = findViewById(R.id.etNum);
        tvCancel = findViewById(R.id.tvCancel);
        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        tvConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(etNum.getText())) {
                    ToastBroadUtil.show(getContext().getString(R.string.输入激活码));
                    return;
                }
                onInputListener.onInput(etNum.getText().toString());
                dismiss();
            }
        });
    }

    public interface OnInputListener {
        void onInput(String num);
    }
}

