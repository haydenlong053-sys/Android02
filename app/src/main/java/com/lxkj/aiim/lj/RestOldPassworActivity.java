package com.lxkj.aiim.lj;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.lxkj.aiim.R;
import com.lxkj.aiim.base.BaseSimpleActivity;
import com.lxkj.aiim.lj.bean.ResultBean;
import com.lxkj.aiim.lj.http.BaseCallback;
import com.lxkj.aiim.lj.http.OkHttpHelper;
import com.lxkj.aiim.lj.http.Url;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Request;
import okhttp3.Response;

public class RestOldPassworActivity extends BaseSimpleActivity {


    private LinearLayout llPay;
    private View viLine;
    private LinearLayout llPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();
        getToolbarTitle().setText(getResources().getString(R.string.忘记原密码1));

        llPay.setOnClickListener(view -> {
            Intent intent = new Intent(this, VerifyPayPasswordActivity.class);
            startActivity(intent);
        });
        llPhone.setOnClickListener(view -> {
            Intent intent = new Intent(this, EditPasswordActivity.class);
            startActivity(intent);
        });

        userInfo();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_rest_old_passwor;
    }

    /**
     * 用户信息
     */
    private void userInfo() {
        Map<String, Object> param = new HashMap<>();
        OkHttpHelper.getInstance().post_json(this, Url.userInfo, param, new BaseCallback<ResultBean>() {
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
                switch (resultBean.data.regType) {
                    case "1"://设备号
                    case "2"://用户名
                        viLine.setVisibility(View.GONE);
                        llPhone.setVisibility(View.GONE);
                        break;
                    case "3"://手机号
                        viLine.setVisibility(View.VISIBLE);
                        llPhone.setVisibility(View.VISIBLE);
                        break;
                }
            }

            @Override
            public void onError(Response response, int code, Exception e) {
            }
        });
    }

    private void initViews() {
        llPay = findViewById(R.id.llPay);
        viLine = findViewById(R.id.viLine);
        llPhone = findViewById(R.id.llPhone);
    }
}