package com.lxkj.aiim.lj;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.cboda.intelligent.R;
import com.lxkj.aiim.base.BaseSimpleActivity;
import com.lxkj.aiim.lj.bean.ResultBean;
import com.lxkj.aiim.lj.http.OkHttpHelper;
import com.lxkj.aiim.lj.http.SpotsCallBack;
import com.lxkj.aiim.lj.http.Url;
import com.lxkj.aiim.lj.utils.StringUtil;
import com.lxkj.aiim.lj.utils.ToastUtil;
import com.maning.pswedittextlibrary.MNPasswordEditText;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Response;

public class SetPayPasswordActivity extends BaseSimpleActivity implements View.OnClickListener {


    private int editIndex = 0;
    private String pwd1;
    private String pwd2;
    private String pwd3;
    private TextView tvHead;
    private TextView tvHint;
    private MNPasswordEditText pswEdit;
    private TextView tvEnter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();

        editIndex = Integer.parseInt(getIntent().getStringExtra("editIndex"));
        if (editIndex == 1) {
            tvHead.setText(getResources().getString(R.string.设置支付密码));
            tvHint.setText(getResources().getString(R.string.请设置支付密码用于支付验证));
        } else {
            tvHead.setText(getResources().getString(R.string.修改密码));
            tvHint.setText(getResources().getString(R.string.请输入支付密码以验证身份));
        }

        pswEdit.setOnTextChangeListener(new MNPasswordEditText.OnTextChangeListener() {
            @Override
            public void onTextChange(String text, boolean isComplete) {
                if (isComplete) {
                    switch (editIndex) {
                        case 0:
                            pwd1 = text;
                            checkPayPwd();
                            break;
                        case 1:
                            pwd2 = text;
                            editIndex = 2;
                            tvHead.setText(getResources().getString(R.string.设置支付密码));
                            tvHint.setText(getResources().getString(R.string.请再次填写以确认));
                            tvEnter.setVisibility(View.VISIBLE);
                            pswEdit.setText("");
                            break;
                        case 2:
                            pwd3 = text;
                            break;
                    }
                }
            }
        });

        tvEnter.setOnClickListener(this);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_set_pay_password;
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.tvEnter) {
            if (StringUtil.isEmpty(pwd3)) {
                ToastUtil.show(getString(R.string.请再次填写以确认));
                return;
            }
            if (!pwd2.equals(pwd3)) {
                ToastUtil.show(getString(R.string.两次密码不一致));
                return;
            }
            setPaypwd();
        }
    }

    /**
     * 设置支付密码
     */
    private void setPaypwd() {
        Map<String, Object> param = new HashMap<>();
        param.put("pwd", pwd3);
        OkHttpHelper.getInstance().post_json(this, Url.setPaypwd, param, new SpotsCallBack<ResultBean>(this) {
            @Override
            public void onSuccess(Response response, ResultBean resultBean) {
                ToastUtil.show(resultBean.msg);
//                EventBusBean eventBusBean = new EventBusBean();
//                eventBusBean.msg = "密码设置成功";
//                EventBus.getDefault().post(eventBusBean);
                finish();
            }

            @Override
            public void onError(Response response, int code, Exception e) {
            }
        });
    }

    /**
     * 验证支付密码
     */
    private void checkPayPwd() {
        Map<String, Object> param = new HashMap<>();
        param.put("pwd", pwd1);
        OkHttpHelper.getInstance().post_json(this, Url.checkPayPwd, param, new SpotsCallBack<ResultBean>(this) {
            @Override
            public void onSuccess(Response response, ResultBean resultBean) {
                editIndex = 1;
                tvHead.setText(getResources().getString(R.string.设置支付密码));
                tvHint.setText(getResources().getString(R.string.请设置支付密码用于支付验证));
                pswEdit.setText("");
            }

            @Override
            public void onError(Response response, int code, Exception e) {
            }
        });
    }

    private void initViews() {
        tvHead = findViewById(R.id.tvHead);
        tvHint = findViewById(R.id.tvHint);
        pswEdit = findViewById(R.id.pswEdit);
        tvEnter = findViewById(R.id.tvEnter);
    }
}