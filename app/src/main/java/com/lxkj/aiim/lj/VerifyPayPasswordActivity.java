package com.lxkj.aiim.lj;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;

import com.lxkj.aiim.R;
import com.lxkj.aiim.base.BaseSimpleActivity;
import com.lxkj.aiim.databinding.ActivityVerifyPayPasswordBinding;
import com.lxkj.aiim.lj.bean.ResultBean;
import com.lxkj.aiim.lj.http.BaseCallback;
import com.lxkj.aiim.lj.http.OkHttpHelper;
import com.lxkj.aiim.lj.http.SpotsCallBack;
import com.lxkj.aiim.lj.http.Url;
import com.maning.pswedittextlibrary.MNPasswordEditText;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Request;
import okhttp3.Response;

public class VerifyPayPasswordActivity extends BaseSimpleActivity {

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
        OkHttpHelper.getInstance().post_json(this, Url.checkPayPwd, param, new SpotsCallBack<ResultBean>(this) {
            @Override
            public void onSuccess(Response response, ResultBean resultBean) {
                Intent intent = new Intent(VerifyPayPasswordActivity.this, NickNameActivity.class);
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
        OkHttpHelper.getInstance().post_json(this, Url.ptConfig, param, new BaseCallback<ResultBean>() {
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
            public void onSuccess(Response response, ResultBean resultBean) {
                if (resultBean.data.openMibao.equals("0")) {
                    Intent intent = new Intent(VerifyPayPasswordActivity.this, RestPasswordActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Intent intent = new Intent(VerifyPayPasswordActivity.this, MiBaoActivity.class);
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