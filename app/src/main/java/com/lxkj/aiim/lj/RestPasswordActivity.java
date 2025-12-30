package com.lxkj.aiim.lj;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.lxkj.aiim.R;
import com.lxkj.aiim.base.BaseSimpleActivity;
import com.lxkj.aiim.lj.bean.ResultBean;
import com.lxkj.aiim.lj.http.OkHttpHelper;
import com.lxkj.aiim.lj.http.SpotsCallBack;
import com.lxkj.aiim.lj.http.Url;
import com.lxkj.aiim.lj.utils.StringUtil;
import com.lxkj.aiim.lj.utils.ToastUtil;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Response;

public class RestPasswordActivity extends BaseSimpleActivity implements View.OnClickListener {


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
            if (StringUtil.isEmpty(etNewPassword.getText().toString())) {
                ToastUtil.show(getString(R.string.请输入新密码));
                return;
            }
            if (StringUtil.isEmpty(etConfirmNewPassword.getText().toString())) {
                ToastUtil.show(getString(R.string.请确认新密码));
                return;
            }
            if (!etNewPassword.getText().toString().equals(etConfirmNewPassword.getText().toString())) {
                ToastUtil.show(getString(R.string.两次密码不一致));
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
        OkHttpHelper.getInstance().post_json(this, Url.changePwd, param, new SpotsCallBack<ResultBean>(this) {
            @Override
            public void onSuccess(Response response, ResultBean resultBean) {
                ToastUtil.show(resultBean.msg);
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