package com.cboda.intelligent.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.cboda.intelligent.R;
import com.cboda.intelligent.base.BaseSimpleBroadActivity;
import com.cboda.intelligent.bean.ResultBroadBean;
import com.cboda.intelligent.utils.OkHttpBroadHelper;
import com.cboda.intelligent.utils.SpotsCallBroadBack;
import com.cboda.intelligent.net.BroadUrl;
import com.cboda.intelligent.utils.StringBroadUtil;
import com.cboda.intelligent.utils.ToastBroadUtil;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Response;

public class RestPasswordBroadActivity extends BaseSimpleBroadActivity implements View.OnClickListener {


    private EditText etNewPassword;
    private EditText etConfirmNewPassword;
    private TextView tvSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getToolbarTitle().setText(getResources().getString(R.string.更改密码));
        initViews();
        tvSubmit.setOnClickListener(this);

    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_rest_password;
    }

    @Override
    public void onClick(View view) {
        Intent intent;
        if (view.getId()==R.id.tvSubmit){
            if (StringBroadUtil.isEmpty(etNewPassword.getText().toString())) {
                ToastBroadUtil.show(getString(R.string.请输入新密码));
                return;
            }
            if (StringBroadUtil.isEmpty(etConfirmNewPassword.getText().toString())) {
                ToastBroadUtil.show(getString(R.string.请确认新密码));
                return;
            }
            if (!etNewPassword.getText().toString().equals(etConfirmNewPassword.getText().toString())) {
                ToastBroadUtil.show(getString(R.string.两次密码不一致));
                return;
            }
            changePwd();
        }
    }

    /**
     * 更换登录密码
     */
    private void changePwd() {
        Map<String, Object> param = new HashMap<>();
        param.put("oldPwd", "");
        param.put("pwd", etConfirmNewPassword.getText().toString());
        OkHttpBroadHelper.getInstance().post_json(this, BroadUrl.changePwd, param, new SpotsCallBroadBack<ResultBroadBean>(this) {
            @Override
            public void onSuccess(Response response, ResultBroadBean resultBean) {
                ToastBroadUtil.show(resultBean.msg);
                finish();
            }

            @Override
            public void onError(Response response, int code, Exception e) {
            }
        });
    }

    private void initViews() {
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmNewPassword = findViewById(R.id.etConfirmNewPassword);
        tvSubmit = findViewById(R.id.tvSubmit);
    }
}