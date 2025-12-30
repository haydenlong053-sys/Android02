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
import com.lxkj.aiim.lj.http.BaseCallback;
import com.lxkj.aiim.lj.http.OkHttpHelper;
import com.lxkj.aiim.lj.http.SpotsCallBack;
import com.lxkj.aiim.lj.http.Url;
import com.lxkj.aiim.lj.utils.TimerUtil;
import com.lxkj.aiim.lj.utils.ToastUtil;

import java.util.HashMap;
import java.util.Map;

import cc.shinichi.library.tool.text.MD5Util;
import okhttp3.Request;
import okhttp3.Response;

public class EditPasswordActivity extends BaseSimpleActivity implements View.OnClickListener {


    private String phone;
    private TextView tvHint;
    private EditText etYzm;
    private TextView tvGetCode;
    private LinearLayout llCode;
    private TextView tvSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();
        getToolbarTitle().setText(getResources().getString(R.string.手机短信验证修改));


        tvSave.setOnClickListener(this);
        tvGetCode.setOnClickListener(this);

        userInfo();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_edit_password;
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.tvSave){
            checkCode();
        }else  if (view.getId() ==R.id.tvGetCode){
            sendMsg();
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
                Intent intent = new Intent(EditPasswordActivity.this, RestPasswordActivity.class);
                startActivity(intent);
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
        param.put("phone", phone);
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
                tvHint.setText(getResources().getString(R.string.您的手机号) + resultBean.data.phone);
                phone = resultBean.data.phone;
            }

            @Override
            public void onError(Response response, int code, Exception e) {
            }
        });
    }

    private void initViews() {
        tvHint = findViewById(R.id.tvHint);
        etYzm = findViewById(R.id.etYzm);
        tvGetCode = findViewById(R.id.tvGetCode);
        llCode = findViewById(R.id.llCode);
        tvSave = findViewById(R.id.tvSave);
    }
}