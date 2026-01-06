package com.lxkj.aiim.holder;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.Display;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.cboda.intelligent.R;

public class PrivacyContentDialog extends Dialog {

    private Context context;      // 上下文

    public PrivacyContentDialog(Context context, OnAgreeListener onConfirmListener) {
        super(context, com.cboda.baselibrary.R.style.Theme_dialog); //dialog的样式
        this.context = context;
        Window window = getWindow();
        window.setGravity(Gravity.CENTER); // 此处可以设置dialog显示的位置为居中
        setContentView(R.layout.layout_private);
        WindowManager windowManager = ((Activity) context).getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.width = display.getWidth() * 7 / 8; // 设置dialog宽度为屏幕的4/5
        lp.height = display.getHeight() * 2 / 3;

        lp.dimAmount = 0.5f;//dimAmount在0.0f和1.0f之间，0.0f完全不暗，即背景是可见的 ，1.0f时候，背景全部变黑暗。
        getWindow().setAttributes(lp);
        //要达到背景全变暗的效果，需设置getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND); 否则，背景无效果
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        setCanceledOnTouchOutside(false);// 点击Dialog外部消失
        setCancelable(false);
        findViewById(R.id.tvAgree).setOnClickListener(v -> {
            onConfirmListener.onAgree();
            dismiss();
        });
        findViewById(R.id.tvDisagree).setOnClickListener(v -> {
            onConfirmListener.onDisagree();
            dismiss();
        });
        WebView mWeb = findViewById(R.id.vb_private);
        String url = "https://fuke.lixinapp.com/lixin2/display/agreement?id=2";
        mWeb.loadUrl(url);
        mWeb.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                if (url != null) {
                    view.loadUrl(url);
                }
                return super.shouldOverrideUrlLoading(view, request);
            }
        });


    }

    public interface OnAgreeListener {
        void onAgree();

        void onDisagree();

    }
}
