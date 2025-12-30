package com.lxkj.aiim.lj;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;

import com.lxkj.aiim.R;
import com.lxkj.aiim.base.BaseSimpleActivity;
import com.lxkj.aiim.databinding.ActivitySetPasswordBinding;
import com.lxkj.aiim.lj.bean.ResultBean;
import com.lxkj.aiim.lj.http.OkHttpHelper;
import com.lxkj.aiim.lj.http.SpotsCallBack;
import com.lxkj.aiim.lj.http.Url;
import com.lxkj.aiim.lj.utils.StringUtil;
import com.lxkj.aiim.lj.utils.ToastUtil;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Response;

public class SetPasswordActivity extends BaseSimpleActivity implements View.OnClickListener {


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
            intent = new Intent(this, RestOldPassworActivity.class);
            startActivity(intent);
        }
        if (view.getId() == R.id.tvSubmit) {
            if (StringUtil.isEmpty(mBinding.etOldPassword.getText().toString())) {
                ToastUtil.show(getString(R.string.请输入原密码));
                return;
            }
            if (StringUtil.isEmpty(mBinding.etNewPassword.getText().toString())) {
                ToastUtil.show(getString(R.string.请输入新密码));
                return;
            }
            if (StringUtil.isEmpty(mBinding.etConfirmNewPassword.getText().toString())) {
                ToastUtil.show(getString(R.string.请确认新密码));
                return;
            }
            if (!mBinding.etNewPassword.getText().toString().equals(mBinding.etConfirmNewPassword.getText().toString())) {
                ToastUtil.show(getString(R.string.两次密码不一致));
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
        OkHttpHelper.getInstance().post_json(this, Url.changePwd, param, new SpotsCallBack<ResultBean>(this) {
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