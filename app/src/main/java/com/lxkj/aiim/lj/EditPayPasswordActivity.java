package com.lxkj.aiim.lj;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lxkj.aiim.R;
import com.lxkj.aiim.base.BaseSimpleActivity;
import com.lxkj.aiim.lj.bean.ResultBean;
import com.lxkj.aiim.lj.http.BaseCallback;
import com.lxkj.aiim.lj.http.OkHttpHelper;
import com.lxkj.aiim.lj.http.SpotsCallBack;
import com.lxkj.aiim.lj.http.Url;
import com.lxkj.aiim.lj.utils.StringUtil;
import com.lxkj.aiim.lj.utils.TimerUtil;
import com.lxkj.aiim.lj.utils.ToastUtil;

import java.util.HashMap;
import java.util.Map;

import cc.shinichi.library.tool.text.MD5Util;
import okhttp3.Request;
import okhttp3.Response;

public class EditPayPasswordActivity extends BaseSimpleActivity implements View.OnClickListener {
    private String type;
    private ResultBean bean;
    private TextView tvHint;
    private EditText etPassword;
    private ImageView imDelate;
    private LinearLayout llPassword;
    private EditText etYzm;
    private TextView tvGetCode;
    private LinearLayout llCode;
    private TextView tvSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();
        type = getIntent().getStringExtra("type");

        etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (StringUtil.isEmpty(editable.toString())) {
                    imDelate.setVisibility(View.GONE);
                } else {
                    imDelate.setVisibility(View.VISIBLE);
                }
            }
        });

        tvGetCode.setOnClickListener(this);
        imDelate.setOnClickListener(this);
        tvSave.setOnClickListener(this);

        userInfo();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_edit_pay_password;
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.imDelate){
            etPassword.setText("");
        }
        if (view.getId() == R.id.tvGetCode){
            sendMsg();
        }
        if (view.getId() == R.id.tvSave){
            if (type.equals("0")) {
                if (StringUtil.isEmpty(etPassword.getText().toString())) {
                    ToastUtil.show(getString(R.string.请输入您的登录密码));
                    return;
                }
                checkPwd();
            } else {
                if (StringUtil.isEmpty(etYzm.getText().toString())) {
                    ToastUtil.show(getString(R.string.请输入验证码));
                    return;
                }
                checkCode();
            }
        }
    }

    /**
     * 验证短信验证码
     */
    private void checkCode() {
        Map<String, Object> param = new HashMap<>();
        param.put("code", etYzm.getText().toString());
        OkHttpHelper.getInstance().post_json(this, Url.checkCode, param, new SpotsCallBack<ResultBean>(this) {
            @Override
            public void onSuccess(Response response, ResultBean resultBean) {
                Intent intent = new Intent(EditPayPasswordActivity.this, SetPayPasswordActivity.class);
                if (bean.isFirst.equals("0")) {
                    intent.putExtra("editIndex", "1");
                } else {
                    intent.putExtra("editIndex", "0");
                }
                startActivity(intent);
            }

            @Override
            public void onError(Response response, int code, Exception e) {
            }
        });
    }

    /**
     * 验证登录密码
     */
    private void checkPwd() {
        Map<String, Object> param = new HashMap<>();
        param.put("pwd", etPassword.getText().toString());
        OkHttpHelper.getInstance().post_json(this, Url.checkPwd, param, new SpotsCallBack<ResultBean>(this) {
            @Override
            public void onSuccess(Response response, ResultBean resultBean) {
                Intent intent = new Intent(EditPayPasswordActivity.this, SetPayPasswordActivity.class);
                if (resultBean.isFirst.equals("0")) {
                    intent.putExtra("editIndex", "1");
                    startActivity(intent);
                    finish();
                } else {
                    intent.putExtra("editIndex", "0");
                    startActivity(intent);
                }


            }

            @Override
            public void onError(Response response, int code, Exception e) {
            }
        });
    }

    /**
     * 获取验证码
     */
    private void sendMsg() {
        Map<String, Object> param = new HashMap<>();
        param.put("phone", bean.data.phone);
        param.put("sign", MD5Util.md5Encode(System.currentTimeMillis() + "lixinkeji"));
        param.put("timestamp", String.valueOf(System.currentTimeMillis()));
        OkHttpHelper.getInstance().post_json(this, Url.sendMsg, param, new SpotsCallBack<ResultBean>(this) {
            @Override
            public void onSuccess(Response response, ResultBean resultBean) {
                TimerUtil mTimerUtil = new TimerUtil(tvGetCode);
                mTimerUtil.timers();
                ToastUtil.show(getString(R.string.验证码已发送请注意查收));
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
                if (type.equals("0")) {
                    getToolbarTitle().setText(getResources().getString(R.string.登录密码验证修改));
                    tvHint.setText(getResources().getString(R.string.请输入您的登录密码));
                    llPassword.setVisibility(View.VISIBLE);
                    llCode.setVisibility(View.GONE);
                } else {
                    getToolbarTitle().setText(getResources().getString(R.string.手机短信验证修改));
                    tvHint.setText(getResources().getString(R.string.您的手机号) + resultBean.data.phone);
                    llPassword.setVisibility(View.GONE);
                    llCode.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onError(Response response, int code, Exception e) {
            }
        });
    }

    private void initViews() {
        tvHint = findViewById(R.id.tvHint);
        etPassword = findViewById(R.id.etPassword);
        imDelate = findViewById(R.id.imDelate);
        llPassword = findViewById(R.id.llPassword);
        etYzm = findViewById(R.id.etYzm);
        tvGetCode = findViewById(R.id.tvGetCode);
        llCode = findViewById(R.id.llCode);
        tvSave = findViewById(R.id.tvSave);
    }
}