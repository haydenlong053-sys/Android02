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
import com.cboda.intelligent.utils.BaseBroadCallback;
import com.cboda.intelligent.utils.OkHttpBroadHelper;
import com.cboda.intelligent.utils.SpotsCallBroadBack;
import com.cboda.intelligent.net.BroadUrl;
import com.cboda.intelligent.utils.TimerBroadUtil;
import com.cboda.intelligent.utils.ToastBroadUtil;

import java.util.HashMap;
import java.util.Map;

import cc.shinichi.library.tool.text.MD5Util;
import okhttp3.Request;
import okhttp3.Response;

public class EditPasswordBroadActivity extends BaseSimpleBroadActivity implements View.OnClickListener {


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
        return R.layout.activity_edit_broad_password;
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
        OkHttpBroadHelper.getInstance().post_json(this, BroadUrl.checkCode, param, new SpotsCallBroadBack<ResultBroadBean>(this) {
            @Override
            public void onSuccess(Response response, ResultBroadBean resultBean) {
                Intent intent = new Intent(EditPasswordBroadActivity.this, RestPasswordBroadActivity.class);
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