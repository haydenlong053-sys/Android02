package com.lxkj.aiim.lj;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
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

public class NickNameActivity extends BaseSimpleActivity implements View.OnClickListener {

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


        if (StringUtil.isEmpty(etNickName.getText().toString())) {
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
                if (StringUtil.isEmpty(etNickName.getText().toString())) {
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
            ToastUtil.show(getString(R.string.请输入密码));
            return;
        }
        Map<String, Object> param = new HashMap<>();
        param.put("pwd", etNickName.getText().toString());
        OkHttpHelper.getInstance().post_json(this, Url.wanshan, param, new SpotsCallBack<ResultBean>(this) {
            @Override
            public void onSuccess(Response response, ResultBean resultBean) {
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
                if (StringUtil.isEmpty(etNickName.getText().toString())) {
                    ToastUtil.show(getString(R.string.请输入昵称));
                    return;
                }
                Intent i = new Intent();
                i.putExtra("name", etNickName.getText().toString());
                setResult(111, i);
                finish();
            } else {
                if (StringUtil.isEmpty(etNickName.getText().toString())) {
                    ToastUtil.show(getString(R.string.请输入密码));
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