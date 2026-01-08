package com.cboda.intelligent.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.cboda.intelligent.R;
import com.cboda.intelligent.base.BaseSimpleBroadActivity;
import com.cboda.baselibrary.bean.CommonResultBroadBean;
import com.cboda.intelligent.bean.ResultBroadBean;
import com.cboda.intelligent.utils.BaseBroadCallback;
import com.cboda.intelligent.utils.OkHttpBroadHelper;
import com.cboda.intelligent.net.BroadUrl;
import com.cboda.intelligent.net.manager.ServerBroadManager;

import java.util.HashMap;
import java.util.Map;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import okhttp3.Request;
import okhttp3.Response;

public class PaySettingBroadActivity extends BaseSimpleBroadActivity implements View.OnClickListener {

    private ResultBroadBean bean;
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
        return R.layout.activity_pay_broad_setting;
    }

    @Override
    public void onClick(View view) {
        Intent intent;
        if (view.getId()== R.id.llXiugai){
            intent = new Intent(this, SetPayPasswordBroadActivity.class);
            if (bean.data.hasPaypwd.equals("0")) {
                intent.putExtra("editIndex", "1");
            } else {
                intent.putExtra("editIndex", "0");
            }
            startActivity(intent);
        }
        if (view.getId()== R.id.llWangji){
            ServerBroadManager.INSTANCE.getUserInfo(this, "", new Function1<CommonResultBroadBean, Unit>() {
                @Override
                public Unit invoke(CommonResultBroadBean commonResultBean) {
                    if (bean != null) {
                        Intent intent1 = new Intent(PaySettingBroadActivity.this, ForgetPayPasswordBroadActivity.class);
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
        OkHttpBroadHelper.getInstance().post_json(this, BroadUrl.userInfo, param, new BaseBroadCallback<ResultBroadBean>() {
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