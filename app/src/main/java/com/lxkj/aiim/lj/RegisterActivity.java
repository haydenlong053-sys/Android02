package com.lxkj.aiim.lj;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lxkj.aiim.R;
import com.lxkj.aiim.base.BaseSimpleActivity;
import com.lxkj.aiim.lj.bean.EventBusBean;
import com.lxkj.aiim.lj.bean.ResultBean;
import com.lxkj.aiim.lj.http.OkHttpHelper;
import com.lxkj.aiim.lj.http.SpotsCallBack;
import com.lxkj.aiim.lj.http.Url;
import com.lxkj.aiim.lj.utils.StringUtil;
import com.lxkj.aiim.lj.utils.ToastUtil;
import com.lxkj.aiim.utils.AppUtil;
import com.lxkj.aiim.utils.JumpUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;
import java.util.Map;

import com.cboda.chatui.ChatManagerHolder;
import okhttp3.Response;

public class RegisterActivity extends BaseSimpleActivity implements View.OnClickListener {


    private View viHead;
    private EditText etPhone;
    private EditText etNewPassword;
    private CheckBox cbCheckPass;
    private EditText etNewPassword2;
    private CheckBox cbCheckPass2;
    private EditText etInvite;
    private LinearLayout llInvite;
    private TextView tvSubmit;
    private TextView tvBack;
    private CheckBox cbAgree;
    private TextView tvXy;
    private TextView tvYs;
    private TextView tvThird;

    @Override
    public boolean showOrHideTitleLayout() {
        return false;
    }

    @Override
    protected boolean transparentStatus() {
        return true;
    }

    @Override
    protected int getLayoutId() {

        return R.layout.activity_register;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height",
                "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        initViews();
        LinearLayout.LayoutParams linearParams = (LinearLayout.LayoutParams) viHead.getLayoutParams(); //取控件textView当前的布局参数
        linearParams.height = result;// 控件的高强制设成20
        viHead.setLayoutParams(linearParams);

        EventBus.getDefault().register(this);
        tvSubmit.setOnClickListener(this);
        tvXy.setOnClickListener(this);
        tvYs.setOnClickListener(this);
        tvBack.setOnClickListener(this);
        tvThird.setOnClickListener(this);

        cbCheckPass.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                int passwordLength = etNewPassword.getText().length();
                etNewPassword.setInputType(isChecked ?
                        (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD) : (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)
                );
                etNewPassword.setSelection(passwordLength);
            }
        });
        cbCheckPass2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                int passwordLength = etNewPassword2.getText().length();
                etNewPassword2.setInputType(isChecked ?
                        (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD) : (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)
                );
                etNewPassword2.setSelection(passwordLength);
            }
        });

    }


    @Override
    public void onClick(View view) {
        Intent intent;
        if (view.getId() == R.id.tvBack){
            finish();
        }
        if (view.getId() == R.id.tvLanguage) {
            intent = new Intent(this, LanguageActivity.class);
            startActivity(intent);
        }
        if (view.getId() == R.id.tvDiqu) {
            intent = new Intent(this, RegionActivity.class);
            startActivityForResult(intent, 333);
        }
        if (view.getId() == R.id.tvSubmit) {
            if (StringUtil.isEmpty(etPhone.getText().toString())) {
                ToastUtil.show(getString(R.string.请输入用户名));
                return;
            }
            if (StringUtil.isEmpty(etNewPassword.getText().toString())) {
                ToastUtil.show(getString(R.string.请输入密码));
                return;
            }
            if (StringUtil.isEmpty(etNewPassword2.getText().toString())) {
                ToastUtil.show(getString(R.string.请再次输入密码));
                return;
            }
            if (!cbAgree.isChecked()) {
                ToastUtil.show(getString(R.string.请先同意用户协议和隐私协议));
                return;
            }
            if (!etNewPassword.getText().toString().equals(etNewPassword2.getText().toString())) {
                ToastUtil.show(getString(R.string.密码不一致));
                return;
            }
            regist();
        }
        if (view.getId() == R.id.tvXy){
            JumpUtils.Companion.startH5Activity(this, Url.YHXY, getString(R.string.用户协议));
        }
        if (view.getId()==R.id.tvThrid){
            JumpUtils.Companion.startH5Activity(this, Url.thirdContent, getString(R.string.第三方信息共享清单));
        }

        if (view.getId() == R.id.tvYs){
            JumpUtils.Companion.startH5Activity(this, Url.YSXY, getString(R.string.隐私协议));
        }

    }


    private String eqno;

    private void regist() {
        eqno = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        Map<String, Object> param = new HashMap<>();
        param.put("eqno", eqno);
        param.put("clientId", ChatManagerHolder.gChatManager.getClientId());
        param.put("type", "2");
        param.put("model", Build.BRAND + " " + Build.MODEL);
        param.put("account", etPhone.getText().toString());
        param.put("pwd", etNewPassword.getText().toString());

        OkHttpHelper.getInstance().post_json(this, Url.regist, param, new SpotsCallBack<ResultBean>(this) {
            @Override
            public void onSuccess(Response response, ResultBean resultBean) {
                if (resultBean.isws.equals("0")) {//是否已完善资料
                    Intent intent = new Intent(RegisterActivity.this, PushInfo2Activity.class);
                    intent.putExtra("apptoken", resultBean.apptoken);
                    intent.putExtra("uid", resultBean.userId);
                    intent.putExtra("userSign", resultBean.userSign);
                    intent.putExtra("imtoken", resultBean.imtoken);
                    intent.putExtra("idno", resultBean.idno);
                    intent.putExtra("qrcode", resultBean.qrcode);
                    intent.putExtra("account", etPhone.getText().toString());
                    intent.putExtra("pwd", etNewPassword.getText().toString());
                    startActivity(intent);
                } else {
                    AppUtil.Companion.imLogin(RegisterActivity.this, resultBean.apptoken, resultBean.userSign, resultBean.userId, resultBean.imtoken);
                }
            }

            @Override
            public void onError(Response response, int code, Exception e) {

            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = false)
    public void senmessage(EventBusBean eventBusBean) {//登录成功
        if (eventBusBean.msg.equals("登录成功")) {
            finish();
        }
    }

    private void initViews() {
        viHead = findViewById(R.id.viHead);
        etPhone = findViewById(R.id.etPhone);
        etNewPassword = findViewById(R.id.etNewPassword);
        cbCheckPass = findViewById(R.id.cb_check_pass);
        etNewPassword2 = findViewById(R.id.etNewPassword2);
        cbCheckPass2 = findViewById(R.id.cb_check_pass2);
        etInvite = findViewById(R.id.etInvite);
        llInvite = findViewById(R.id.llInvite);
        tvSubmit = findViewById(R.id.tvSubmit);
        tvBack = findViewById(R.id.tvBack);
        cbAgree = findViewById(R.id.cbAgree);
        tvXy = findViewById(R.id.tvXy);
        tvYs = findViewById(R.id.tvYs);
        tvThird = findViewById(R.id.tvThrid);
    }
}