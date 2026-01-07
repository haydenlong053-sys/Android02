package com.cboda.intelligent.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;

import com.cboda.intelligent.R;
import com.cboda.intelligent.base.BaseSimpleBroadActivity;
import com.cboda.intelligent.databinding.ActivityVerifyPayPasswordBinding;
import com.cboda.intelligent.bean.ResultBroadBean;
import com.cboda.intelligent.utils.BaseBroadCallback;
import com.cboda.intelligent.utils.OkHttpBroadHelper;
import com.cboda.intelligent.utils.SpotsCallBroadBack;
import com.cboda.intelligent.net.BroadUrl;
import com.maning.pswedittextlibrary.MNPasswordEditText;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Request;
import okhttp3.Response;

public class VerifyPayPasswordBroadActivity extends BaseSimpleBroadActivity {

    private ActivityVerifyPayPasswordBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding= (ActivityVerifyPayPasswordBinding) getBinding();
        mBinding.pswEdit.setOnTextChangeListener(new MNPasswordEditText.OnTextChangeListener() {
            @Override
            public void onTextChange(String text, boolean isComplete) {
                if (isComplete) {
                    checkPayPwd(text);
                }
            }
        });
    }

    /**
     * 验证支付密码
     */
    private void checkPayPwd(String pwd) {
        Map<String, Object> param = new HashMap<>();
        param.put("pwd", pwd);
        OkHttpBroadHelper.getInstance().post_json(this, BroadUrl.checkPayPwd, param, new SpotsCallBroadBack<ResultBroadBean>(this) {
            @Override
            public void onSuccess(Response response, ResultBroadBean resultBean) {
                Intent intent = new Intent(VerifyPayPasswordBroadActivity.this, NickNameBroadActivity.class);
                intent.putExtra("type", 1);
                startActivity(intent);
                finish();
//                ptConfig();
            }

            @Override
            public void onError(Response response, int code, Exception e) {
            }
        });
    }

    /**
     * 平台参数
     */
    private void ptConfig() {
        Map<String, Object> param = new HashMap<>();
        OkHttpBroadHelper.getInstance().post_json(this, BroadUrl.ptConfig, param, new BaseBroadCallback<ResultBroadBean>() {
            @Override
            public void onBeforeRequest(Request request) {

            }

            @Override
            public void onFailure(Request request, Exception e) {

            }

            @Override
            public void onResponse(Response response) {

            }

            @Override
            public void onSuccess(Response response, ResultBroadBean resultBean) {
                if (resultBean.data.openMibao.equals("0")) {
                    Intent intent = new Intent(VerifyPayPasswordBroadActivity.this, RestPasswordBroadActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Intent intent = new Intent(VerifyPayPasswordBroadActivity.this, MiBaoBroadActivity.class);
                    intent.putExtra("type", "1");
                    startActivity(intent);
                }
            }

            @Override
            public void onError(Response response, int code, Exception e) {
            }
        });
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_verify_pay_password;
    }

    @Override
    protected boolean useViewBinding() {
        return true;
    }

    @Nullable
    @Override
    protected Class<? extends ViewBinding> getViewBindingClass() {
        return ActivityVerifyPayPasswordBinding.class;
    }
}