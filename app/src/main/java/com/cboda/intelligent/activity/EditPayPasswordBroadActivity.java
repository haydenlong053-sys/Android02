package com.cboda.intelligent.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cboda.intelligent.R;
import com.cboda.intelligent.base.BaseSimpleBroadActivity;
import com.cboda.intelligent.bean.ResultBroadBean;
import com.cboda.intelligent.utils.BaseBroadCallback;
import com.cboda.intelligent.utils.OkHttpBroadHelper;
import com.cboda.intelligent.utils.SpotsCallBroadBack;
import com.cboda.intelligent.net.BroadUrl;
import com.cboda.intelligent.utils.StringBroadUtil;
import com.cboda.intelligent.utils.TimerBroadUtil;
import com.cboda.intelligent.utils.ToastBroadUtil;

import java.util.HashMap;
import java.util.Map;

import cc.shinichi.library.tool.text.MD5Util;
import okhttp3.Request;
import okhttp3.Response;

public class EditPayPasswordBroadActivity extends BaseSimpleBroadActivity implements View.OnClickListener {
    private String type;
    private ResultBroadBean bean;
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
                if (StringBroadUtil.isEmpty(editable.toString())) {
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
        return R.layout.activity_edit_pay_broad_password;
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
                if (StringBroadUtil.isEmpty(etPassword.getText().toString())) {
                    ToastBroadUtil.show(getString(R.string.请输入您的登录密码));
                    return;
                }
                checkPwd();
            } else {
                if (StringBroadUtil.isEmpty(etYzm.getText().toString())) {
                    ToastBroadUtil.show(getString(R.string.请输入验证码));
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
        OkHttpBroadHelper.getInstance().post_json(this, BroadUrl.checkCode, param, new SpotsCallBroadBack<ResultBroadBean>(this) {
            @Override
            public void onSuccess(Response response, ResultBroadBean resultBean) {
                Intent intent = new Intent(EditPayPasswordBroadActivity.this, SetPayPasswordBroadActivity.class);
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
        OkHttpBroadHelper.getInstance().post_json(this, BroadUrl.checkPwd, param, new SpotsCallBroadBack<ResultBroadBean>(this) {
            @Override
            public void onSuccess(Response response, ResultBroadBean resultBean) {
                Intent intent = new Intent(EditPayPasswordBroadActivity.this, SetPayPasswordBroadActivity.class);
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
        OkHttpBroadHelper.getInstance().post_json(this, BroadUrl.sendMsg, param, new SpotsCallBroadBack<ResultBroadBean>(this) {
            @Override
            public void onSuccess(Response response, ResultBroadBean resultBean) {
                TimerBroadUtil mTimerUtil = new TimerBroadUtil(tvGetCode);
                mTimerUtil.timers();
                ToastBroadUtil.show(getString(R.string.验证码已发送请注意查收));
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