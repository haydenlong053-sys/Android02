package com.cboda.intelligent.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cboda.intelligent.R;
import com.cboda.intelligent.base.BaseSimpleBroadActivity;
import com.cboda.intelligent.bean.ResultBroadBean;
import com.cboda.intelligent.utils.BaseBroadCallback;
import com.cboda.intelligent.utils.OkHttpBroadHelper;
import com.cboda.intelligent.net.BroadUrl;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Request;
import okhttp3.Response;

public class AccountBroadActivity extends BaseSimpleBroadActivity implements View.OnClickListener {

    private TextView tvPhone;
    private LinearLayout llPhone;
    private TextView tvName;
    private LinearLayout llUserName;
    private LinearLayout llGenggai;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();
        getToolbarTitle().setText(getResources().getString(R.string.账号与安全));


        llPhone.setOnClickListener(this);
        llGenggai.setOnClickListener(this);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_broad_account;
    }

    @Override
    public void onClick(View view) {
        Intent intent;
        if (view.getId() ==R.id.llPhone){
            intent = new Intent(this, SetPhoneBroadActivity.class);
            intent.putExtra("phone", tvPhone.getText().toString());
            startActivity(intent);
        }
        if (view.getId() == R.id.llGenggai){
            intent = new Intent(this, SetPasswordBroadActivity.class);
            startActivity(intent);
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
                tvName.setText(resultBean.data.acount);
                switch (resultBean.data.regType) {
                    case "1"://设备号
                    case "2"://设备号
                        llPhone.setVisibility(View.GONE);
                        break;
                    case "3":
                        llPhone.setVisibility(View.VISIBLE);
                        tvPhone.setText(resultBean.data.phone);
                        break;
                }
            }

            @Override
            public void onError(Response response, int code, Exception e) {
            }
        });
    }

    private void initViews() {
        tvPhone = findViewById(R.id.tvPhone);
        llPhone = findViewById(R.id.llPhone);
        tvName = findViewById(R.id.tvName);
        llUserName = findViewById(R.id.llUserName);
        llGenggai = findViewById(R.id.llGenggai);
    }
}