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
import com.cboda.intelligent.bean.ResultBroadBean;
import com.cboda.intelligent.utils.OkHttpBroadHelper;
import com.cboda.intelligent.utils.SpotsCallBroadBack;
import com.cboda.intelligent.net.BroadUrl;
import com.cboda.intelligent.utils.TimerBroadUtil;
import com.cboda.intelligent.utils.ToastBroadUtil;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Response;


public class QrdlBroadDialog extends Dialog {

    private Context context;      // 上下文
    OnInputListener onInputListener;
    private String acc;
    private EditText etYzm;
    private TextView tvGetCode;
    private TextView tvConfirm;

    public QrdlBroadDialog(final Context context, final OnInputListener onInputListener, String acc) {
        super(context, com.cboda.baselibrary.R.style.Theme_dialog); //dialog的样式
        this.context = context;
        this.onInputListener = onInputListener;
        Window window = getWindow();
        window.setGravity(Gravity.CENTER); // 此处可以设置dialog显示的位置为居中
        setContentView(R.layout.layout_dialog_qrdl);
        initViews();
        WindowManager windowManager = ((Activity) context).getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.width = display.getWidth() * 6 / 7; // 设置dialog宽度为屏幕的4/5
        lp.dimAmount = 0.5f;//dimAmount在0.0f和1.0f之间，0.0f完全不暗，即背景是可见的 ，1.0f时候，背景全部变黑暗。
        getWindow().setAttributes(lp);
        //要达到背景全变暗的效果，需设置getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND); 否则，背景无效果
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        this.acc = acc;
        //遍历控件id,添加点击事件
        tvConfirm = findViewById(R.id.tvConfirm);
        etYzm = findViewById(R.id.etYzm);
        tvGetCode = findViewById(R.id.tvGetCode);
        tvGetCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getCode();
            }
        });
        tvConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(etYzm.getText())) {
                    ToastBroadUtil.show(getContext().getString(R.string.请输入验证码));
                    return;
                }
                onInputListener.onInput(etYzm.getText().toString());
                dismiss();
            }
        });
    }

    /**
     * 获取验证码
     */
    private void getCode() {
        Map<String, Object> param = new HashMap<>();
        param.put("acc", acc);
        OkHttpBroadHelper.getInstance().post_json(context, BroadUrl.getAccCode, param, new SpotsCallBroadBack<ResultBroadBean>(context) {
            @Override
            public void onSuccess(Response response, ResultBroadBean resultBean) {
                TimerBroadUtil mTimerUtil = new TimerBroadUtil(tvGetCode);
                mTimerUtil.timers();
                ToastBroadUtil.show(context.getString(R.string.验证码已发送请注意查收));
            }

            @Override
            public void onError(Response response, int code, Exception e) {
            }
        });
    }

    private void initViews() {
        etYzm = findViewById(R.id.etYzm);
        tvGetCode = findViewById(R.id.tvGetCode);
        tvConfirm = findViewById(R.id.tvConfirm);
    }


    public interface OnInputListener {
        void onInput(String code);
    }
}

