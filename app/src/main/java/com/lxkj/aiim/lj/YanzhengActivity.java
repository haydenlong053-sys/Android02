package com.lxkj.aiim.lj;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lxkj.aiim.R;
import com.lxkj.aiim.base.BaseSimpleActivity;
import com.lxkj.aiim.lj.bean.ResultBean;
import com.lxkj.aiim.lj.http.OkHttpHelper;
import com.lxkj.aiim.lj.http.SpotsCallBack;
import com.lxkj.aiim.lj.http.Url;
import com.lxkj.aiim.lj.utils.StringUtil;
import com.lxkj.aiim.lj.utils.TimerUtil;
import com.lxkj.aiim.lj.utils.ToastUtil;

import java.util.HashMap;
import java.util.Map;

import cc.shinichi.library.tool.text.MD5Util;
import okhttp3.Response;

public class YanzhengActivity extends BaseSimpleActivity implements View.OnClickListener {


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

            if (StringUtil.isEmpty(etPhone.getText().toString())) {
                ToastUtil.show(getString(R.string.请输入手机号));
                return;
            }
            sendMsg();
        }
        if (view.getId() ==R.id.tvSubmit){
            if (StringUtil.isEmpty(etPhone.getText().toString())) {
                ToastUtil.show(getString(R.string.请输入手机号));
                return;
            }
            if (StringUtil.isEmpty(etYzm.getText().toString())) {
                ToastUtil.show(getString(R.string.请输入验证码));
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
        OkHttpHelper.getInstance().post_json(this, Url.changePhone, param, new SpotsCallBack<ResultBean>(this) {
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

    /**
     * 获取验证码
     */
    private void sendMsg() {
        Map<String, Object> param = new HashMap<>();
        param.put("phone", etPhone.getText().toString());
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

    private void initViews() {
        etPhone = findViewById(R.id.etPhone);
        tvGetCode = findViewById(R.id.tvGetCode);
        etYzm = findViewById(R.id.etYzm);
        llYzmPhone = findViewById(R.id.llYzmPhone);
        tvSubmit = findViewById(R.id.tvSubmit);
    }
}