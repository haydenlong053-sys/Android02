package com.cboda.intelligent.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cboda.intelligent.R;
import com.cboda.intelligent.base.BaseSimpleBroadActivity;
import com.cboda.intelligent.bean.ResultBroadBean;
import com.cboda.intelligent.utils.OkHttpBroadHelper;
import com.cboda.intelligent.utils.SpotsCallBroadBack;
import com.cboda.intelligent.net.BroadUrl;
import com.cboda.intelligent.utils.StringBroadUtil;
import com.cboda.intelligent.utils.TimerBroadUtil;
import com.cboda.intelligent.utils.ToastBroadUtil;

import java.util.HashMap;
import java.util.Map;

import cc.shinichi.library.tool.text.MD5Util;
import okhttp3.Response;

public class YanzhengBroadActivity extends BaseSimpleBroadActivity implements View.OnClickListener {


    private EditText etPhone;
    private TextView tvGetCode;
    private EditText etYzm;
    private LinearLayout llYzmPhone;
    private TextView tvSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();
        getToolbarTitle().setText(getResources().getString(R.string.验证手机号));

        tvGetCode.setOnClickListener(this);
        tvSubmit.setOnClickListener(this);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_mine_yanzheng;
    }

    @Override
    public void onClick(View view) {
        Intent intent;
        if (view.getId()==R.id.tvGetCode){

            if (StringBroadUtil.isEmpty(etPhone.getText().toString())) {
                ToastBroadUtil.show(getString(R.string.请输入手机号));
                return;
            }
            sendMsg();
        }
        if (view.getId() ==R.id.tvSubmit){
            if (StringBroadUtil.isEmpty(etPhone.getText().toString())) {
                ToastBroadUtil.show(getString(R.string.请输入手机号));
                return;
            }
            if (StringBroadUtil.isEmpty(etYzm.getText().toString())) {
                ToastBroadUtil.show(getString(R.string.请输入验证码));
                return;
            }
            changePhone();
        }
    }

    /**
     * 更换手机号
     */
    private void changePhone() {
        Map<String, Object> param = new HashMap<>();
        param.put("phone", etPhone.getText().toString());
        param.put("code", etYzm.getText().toString());
        OkHttpBroadHelper.getInstance().post_json(this, BroadUrl.changePhone, param, new SpotsCallBroadBack<ResultBroadBean>(this) {
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

    /**
     * 获取验证码
     */
    private void sendMsg() {
        Map<String, Object> param = new HashMap<>();
        param.put("phone", etPhone.getText().toString());
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

    private void initViews() {
        etPhone = findViewById(R.id.etPhone);
        tvGetCode = findViewById(R.id.tvGetCode);
        etYzm = findViewById(R.id.etYzm);
        llYzmPhone = findViewById(R.id.llYzmPhone);
        tvSubmit = findViewById(R.id.tvSubmit);
    }
}