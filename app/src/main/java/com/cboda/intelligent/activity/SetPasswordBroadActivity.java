package com.cboda.intelligent.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;

import com.cboda.intelligent.R;
import com.cboda.intelligent.base.BaseSimpleBroadActivity;
import com.cboda.intelligent.databinding.ActivitySetPasswordBinding;
import com.cboda.intelligent.bean.ResultBroadBean;
import com.cboda.intelligent.utils.OkHttpBroadHelper;
import com.cboda.intelligent.utils.SpotsCallBroadBack;
import com.cboda.intelligent.net.BroadUrl;
import com.cboda.intelligent.utils.StringBroadUtil;
import com.cboda.intelligent.utils.ToastBroadUtil;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Response;

public class SetPasswordBroadActivity extends BaseSimpleBroadActivity implements View.OnClickListener {


    private ActivitySetPasswordBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getToolbarTitle().setText(getResources().getString(R.string.更改密码));
        mBinding= (ActivitySetPasswordBinding) getBinding();

        mBinding.tvZhaohui.setOnClickListener(this);
        mBinding.tvSubmit.setOnClickListener(this);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_set_password;
    }

    @Override
    public void onClick(View view) {
        Intent intent;
        if (view.getId() == R.id.tvZhaohui){
            intent = new Intent(this, RestOldPassworBroadActivity.class);
            startActivity(intent);
        }
        if (view.getId() == R.id.tvSubmit) {
            if (StringBroadUtil.isEmpty(mBinding.etOldPassword.getText().toString())) {
                ToastBroadUtil.show(getString(R.string.请输入原密码));
                return;
            }
            if (StringBroadUtil.isEmpty(mBinding.etNewPassword.getText().toString())) {
                ToastBroadUtil.show(getString(R.string.请输入新密码));
                return;
            }
            if (StringBroadUtil.isEmpty(mBinding.etConfirmNewPassword.getText().toString())) {
                ToastBroadUtil.show(getString(R.string.请确认新密码));
                return;
            }
            if (!mBinding.etNewPassword.getText().toString().equals(mBinding.etConfirmNewPassword.getText().toString())) {
                ToastBroadUtil.show(getString(R.string.两次密码不一致));
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
        param.put("oldPwd", mBinding.etOldPassword.getText().toString());
        param.put("pwd", mBinding.etNewPassword.getText().toString());
        OkHttpBroadHelper.getInstance().post_json(this, BroadUrl.changePwd, param, new SpotsCallBroadBack<ResultBroadBean>(this) {
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
    @Override
    protected boolean useViewBinding() {
        return true;
    }

    @Nullable
    @Override
    protected Class<? extends ViewBinding> getViewBindingClass() {
        return ActivitySetPasswordBinding.class;
    }
}