package com.lxkj.aiim.lj;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.lxkj.aiim.R;
import com.lxkj.aiim.base.BaseSimpleActivity;
import com.base.commonlibrary.bean.CommonResultBean;
import com.lxkj.aiim.lj.bean.ResultBean;
import com.lxkj.aiim.lj.http.BaseCallback;
import com.lxkj.aiim.lj.http.OkHttpHelper;
import com.lxkj.aiim.lj.http.Url;
import com.lxkj.aiim.net.manager.ServerManager;

import java.util.HashMap;
import java.util.Map;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import okhttp3.Request;
import okhttp3.Response;

public class PaySettingActivity extends BaseSimpleActivity implements View.OnClickListener {

    private ResultBean bean;
    private LinearLayout llXiugai;
    private LinearLayout llWangji;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();
        getToolbarTitle().setText(getResources().getString(R.string.支付设置));
        llXiugai.setOnClickListener(this);
        llWangji.setOnClickListener(this);


    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_pay_setting;
    }

    @Override
    public void onClick(View view) {
        Intent intent;
        if (view.getId()== R.id.llXiugai){
            intent = new Intent(this, SetPayPasswordActivity.class);
            if (bean.data.hasPaypwd.equals("0")) {
                intent.putExtra("editIndex", "1");
            } else {
                intent.putExtra("editIndex", "0");
            }
            startActivity(intent);
        }
        if (view.getId()== R.id.llWangji){
            ServerManager.INSTANCE.getUserInfo(this, "", new Function1<CommonResultBean, Unit>() {
                @Override
                public Unit invoke(CommonResultBean commonResultBean) {
                    if (bean != null) {
                        Intent intent1 = new Intent(PaySettingActivity.this, ForgetPayPasswordActivity.class);
                        intent1.putExtra("regType", bean.data.regType);
                        startActivity(intent1);
                    }
                    return Unit.INSTANCE;
                }
            });

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        userInfo();
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
                bean = resultBean;
            }

            @Override
            public void onError(Response response, int code, Exception e) {
            }
        });
    }

    private void initViews() {
        llXiugai = findViewById(R.id.llXiugai);
        llWangji = findViewById(R.id.llWangji);
    }
}