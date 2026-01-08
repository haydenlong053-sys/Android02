package com.cboda.intelligent.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.cboda.intelligent.R;
import com.cboda.intelligent.utils.StringBroadUtil;


/**
 * Created by kxn on 2019/12/10 0010.
 */
public class NormalBroadDialog extends Dialog {

    private Context context;      // 上下文
    TextView tvHint, tvLeft, tvRight,tvBiaoti;

    public NormalBroadDialog(Context context,String biaoti, String title, String left, String right, boolean isCan) {
        super(context, com.cboda.baselibrary.R.style.Theme_dialog); //dialog的样式
        this.context = context;
        Window window = getWindow();
        window.setGravity(Gravity.CENTER); // 此处可以设置dialog显示的位置为居中
        setContentView(R.layout.dialog_broad_normal);
        WindowManager windowManager = ((Activity) context).getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.width = display.getWidth() * 4 / 5; // 设置dialog宽度为屏幕的4/5
        lp.dimAmount = 0.5f;//dimAmount在0.0f和1.0f之间，0.0f完全不暗，即背景是可见的 ，1.0f时候，背景全部变黑暗。
        getWindow().setAttributes(lp);
        //要达到背景全变暗的效果，需设置getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND); 否则，背景无效果
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        setCanceledOnTouchOutside(isCan);// 点击Dialog外部消失
        setCancelable(isCan);
        //遍历控件id,添加点击事件
        tvHint = findViewById(R.id.tvHint);
        tvRight = findViewById(R.id.tvConfirm);
        tvLeft = findViewById(R.id.tvCancel);
        tvBiaoti = findViewById(R.id.tvBiaoti);
        tvBiaoti.setText(biaoti);
        tvHint.setText(title);
        if (StringBroadUtil.isEmpty(left)){
            tvLeft.setVisibility(View.GONE);
        }else {
            tvLeft.setVisibility(View.VISIBLE);
            tvLeft.setText(left);
        }

        tvRight.setText(right);

        tvLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                listener.OnLeftClick();
            }
        });
        tvRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                listener.OnRightClick();
            }
        });
    }

    private OnButtonClick listener;

    public interface OnButtonClick {
        void OnRightClick();

        void OnLeftClick();
    }

    public NormalBroadDialog setOnButtonClickListener(OnButtonClick listener) {
        this.listener = listener;
        return this;
    }

}

