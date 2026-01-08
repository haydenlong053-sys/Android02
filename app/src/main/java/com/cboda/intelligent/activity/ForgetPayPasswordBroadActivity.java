package com.cboda.intelligent.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.cboda.intelligent.R;
import com.cboda.intelligent.base.BaseSimpleBroadActivity;


public class ForgetPayPasswordBroadActivity extends BaseSimpleBroadActivity implements View.OnClickListener {


    private String regType;
    private LinearLayout llLogin;
    private View viLine;
    private LinearLayout llPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();
        getToolbarTitle().setText(getResources().getString(R.string.忘记支付密码));

        regType = getIntent().getStringExtra("regType");
        switch (regType) {
            case "1"://设备号登录
            case "2"://用户名密码
                llLogin.setVisibility(View.VISIBLE);
                llPhone.setVisibility(View.GONE);
                viLine.setVisibility(View.GONE);
                break;
            case "3"://手机号密码
                llLogin.setVisibility(View.VISIBLE);
                llPhone.setVisibility(View.VISIBLE);
                viLine.setVisibility(View.VISIBLE);
                break;
        }

        llLogin.setOnClickListener(this);
        llPhone.setOnClickListener(this);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_forget_pay_broad_password;
    }

    @Override
    public void onClick(View view) {
        Intent intent;
        if (view.getId() == R.id.llLogin){
            intent = new Intent(this, EditPayPasswordBroadActivity.class);
            intent.putExtra("type", "0");
            startActivity(intent);
        }else if (view.getId() ==R.id.llPhone){
            intent = new Intent(this, EditPayPasswordBroadActivity.class);
            intent.putExtra("type", "1");
            startActivity(intent);
        }

    }

    private void initViews() {
        llLogin = findViewById(R.id.llLogin);
        viLine = findViewById(R.id.viLine);
        llPhone = findViewById(R.id.llPhone);
    }
}