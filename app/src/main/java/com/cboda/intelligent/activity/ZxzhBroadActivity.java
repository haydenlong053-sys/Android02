package com.cboda.intelligent.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.google.gson.Gson;
import com.cboda.intelligent.R;
import com.cboda.intelligent.base.BaseSimpleBroadActivity;
import com.cboda.intelligent.bean.BaseBroadBean;
import com.cboda.intelligent.bean.ResultBroadBean;
import com.cboda.intelligent.dialog.ZxHintDialogBroadFra;
import com.cboda.intelligent.utils.OkHttpBroadHelper;
import com.cboda.intelligent.utils.SpotsCallBroadBack;
import com.cboda.intelligent.net.BroadUrl;
import com.cboda.intelligent.utils.ToastBroadUtil;
import com.cboda.intelligent.utils.JumpBroadUtils;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Response;

public class ZxzhBroadActivity extends BaseSimpleBroadActivity implements View.OnClickListener {


    private ImageView ivState;
    private RelativeLayout rlIcon;
    private TextView tvState;
    private TextView tvHint;
    private WebView webView;
    private LinearLayout llContent;
    private TextView tvConfirm;
    private CheckBox cbAgree;
    private TextView tvXy;
    private LinearLayout llXy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();
        getToolbarTitle().setText(getResources().getString(R.string.注销账号));
        tvConfirm.setOnClickListener(this);
        tvXy.setOnClickListener(this);
        initWeb();
        userInfo();
    }

    private void initWeb() {
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // if (sonicSession != null) {
                //     sonicSession.getSessionClient().pageFinish(url);
                // }
                dismissLoading();
                int colorContent = ContextCompat.getColor(ZxzhBroadActivity.this, R.color.color_text0);
                int colorBg = ContextCompat.getColor(ZxzhBroadActivity.this, R.color.bg_main);
                String colorContentCode = String.format("#%06X", (0xFFFFFF & colorContent));
                String colorBgCode = String.format("#%06X", (0xFFFFFF & colorBg));
                webView.evaluateJavascript(
                        "(function() {" +
                                "document.body.style.backgroundColor = '" + colorBgCode + "';" +
                                "document.body.style.color = '" + colorContentCode + "';" +
                                "})();",
                        null
                );
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                try {
                    String loadUrl = request.getUrl().toString();
                    if ((loadUrl != null && loadUrl.startsWith("http")) ||
                            (loadUrl != null && loadUrl.startsWith("https"))) {
                        view.loadUrl(loadUrl);
                        return false;
                    } else {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(loadUrl));
                        startActivity(intent);
                        return true;
                    }
                } catch (Exception e) {
                    return false;
                }
            }
        });

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);

        webView.loadUrl(BroadUrl.getAccountDeletionUrl());
    }


    private void zhuxiao() {
        Map<String, Object> param = new HashMap<>();
        OkHttpBroadHelper.getInstance().post_json(this, BroadUrl.zhuxiao, param, new SpotsCallBroadBack<String>(this) {
            @Override
            public void onSuccess(Response response, String result) {
                BaseBroadBean bean = new Gson().fromJson(result, BaseBroadBean.class);
                if (bean.code.equals("200") && bean.success) {
                    finish();
                } else {
                    new ZxHintDialogBroadFra().setContent(bean.msg).show(getSupportFragmentManager(), "");
                }
            }

            @Override
            public void onError(Response response, int code, Exception e) {
            }
        });
    }


    /**
     * 用户信息
     */
    private void userInfo() {
        Map<String, Object> param = new HashMap<>();
        OkHttpBroadHelper.getInstance().post_json(this, BroadUrl.userInfo, param, new SpotsCallBroadBack<ResultBroadBean>(this) {

            @Override
            public void onSuccess(Response response, ResultBroadBean resultBean) {
                if (null != resultBean.data.zxState) {
                    switch (resultBean.data.zxState) {
                        case "1"://审核中
                            tvState.setText("等待平台确认");
                            llContent.setVisibility(View.INVISIBLE);
                            llXy.setVisibility(View.INVISIBLE);
                            tvConfirm.setVisibility(View.INVISIBLE);
                            ivState.setImageResource(R.mipmap.ic_zxzh_broad_sh);
                            break;
                        case "3"://驳回
                            tvConfirm.setText("重新申请");
                            llXy.setVisibility(View.GONE);
                            tvHint.setText("拒绝原因：" + resultBean.data.zxReson);
                            tvState.setText("已拒绝");
                            ivState.setImageResource(R.mipmap.ic_zxzh_broad_jj);
                            tvHint.setVisibility(View.VISIBLE);
                            webView.setVisibility(View.GONE);
                            break;
                    }
                }
            }

            @Override
            public void onError(Response response, int code, Exception e) {
            }
        });
    }


    @Override
    protected int getLayoutId() {
        return R.layout.activity_zx_broad_zh;
    }

    @Override
    public void onClick(View view) {
        Intent intent;
        if (view.getId() == R.id.tvConfirm){
            if (tvHint.isShown()) {
                tvConfirm.setText("申请注销");
                llXy.setVisibility(View.VISIBLE);
                tvState.setText("申请注销账号");
                tvHint.setVisibility(View.GONE);
                webView.setVisibility(View.VISIBLE);
                return;
            }
            if (!cbAgree.isChecked()) {
                ToastBroadUtil.show("请先同意注销协议");
                return;
            }
            zhuxiao();
        }else  if (view.getId() == R.id.tvXy){
            JumpBroadUtils.Companion.startH5Activity(this, BroadUrl.getAccountDeletionUrl(), getString(R.string.注销协议));
        }
    }

    private void initViews() {
        ivState = findViewById(R.id.ivState);
        rlIcon = findViewById(R.id.rlIcon);
        tvState = findViewById(R.id.tvState);
        tvHint = findViewById(R.id.tvHint);
        webView = findViewById(R.id.webView);
        llContent = findViewById(R.id.llContent);
        tvConfirm = findViewById(R.id.tvConfirm);
        cbAgree = findViewById(R.id.cbAgree);
        tvXy = findViewById(R.id.tvXy);
        llXy = findViewById(R.id.llXy);
    }
}