package com.cboda.intelligent.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
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

public class NickNameBroadActivity extends BaseSimpleBroadActivity implements View.OnClickListener {

    private String name;
    private int type;//0昵称  1密码
    private TextView tvTitle1;
    private EditText etNickName;
    private ImageView imDelate;
    private TextView tvSave;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_nick_name;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();
        type = getIntent().getIntExtra("type", 0);
        if (type == 1) {
            getToolbarTitle().setText(getResources().getString(R.string.设置密码));
            etNickName.setHint(getResources().getString(R.string.请输入密码));
            TextView tvTitle1 = findViewById(R.id.tvTitle1);
            tvTitle1.setText(getResources().getString(R.string.设置密码));

        } else {
            name = getIntent().getStringExtra("name");
            etNickName.setText(name);
            getToolbarTitle().setText(getResources().getString(R.string.设置昵称));
        }


        if (StringBroadUtil.isEmpty(etNickName.getText().toString())) {
            imDelate.setVisibility(View.GONE);
        } else {
            imDelate.setVisibility(View.VISIBLE);
        }

        etNickName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (StringBroadUtil.isEmpty(etNickName.getText().toString())) {
                    imDelate.setVisibility(View.GONE);
                } else {
                    imDelate.setVisibility(View.VISIBLE);
                }

            }
        });

        imDelate.setOnClickListener(this);
        tvSave.setOnClickListener(this);
    }

    private void wanshan() {
        if (TextUtils.isEmpty(etNickName.getText())) {
            ToastBroadUtil.show(getString(R.string.请输入密码));
            return;
        }
        Map<String, Object> param = new HashMap<>();
        param.put("pwd", etNickName.getText().toString());
        OkHttpBroadHelper.getInstance().post_json(this, BroadUrl.wanshan, param, new SpotsCallBroadBack<ResultBroadBean>(this) {
            @Override
            public void onSuccess(Response response, ResultBroadBean resultBean) {
                finish();
            }

            @Override
            public void onError(Response response, int code, Exception e) {
            }
        });
    }


    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.imDelate){
            etNickName.setText("");
            imDelate.setVisibility(View.GONE);
        }
        if (view.getId() == R.id.tvSave){
            if (type == 0) {
                if (StringBroadUtil.isEmpty(etNickName.getText().toString())) {
                    ToastBroadUtil.show(getString(R.string.请输入昵称));
                    return;
                }
                Intent i = new Intent();
                i.putExtra("name", etNickName.getText().toString());
                setResult(111, i);
                finish();
            } else {
                if (StringBroadUtil.isEmpty(etNickName.getText().toString())) {
                    ToastBroadUtil.show(getString(R.string.请输入密码));
                    return;
                }
                wanshan();
            }
        }
    }

    private void initViews() {
        tvTitle1 = findViewById(R.id.tvTitle1);
        etNickName = findViewById(R.id.etNickName);
        imDelate = findViewById(R.id.imDelate);
        tvSave = findViewById(R.id.tvSave);
    }
}