package com.lxkj.aiim.lj;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.util.Base64;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.lxkj.aiim.R;
import com.lxkj.aiim.base.BaseSimpleActivity;
import com.lxkj.aiim.lj.bean.ResultBean;
import com.lxkj.aiim.lj.http.OkHttpHelper;
import com.lxkj.aiim.lj.http.SpotsCallBack;
import com.lxkj.aiim.lj.http.Url;
import com.lxkj.aiim.lj.utils.StringUtil;
import com.lxkj.aiim.lj.utils.TimerUtil;
import com.lxkj.aiim.lj.utils.ToastUtil;
import com.lxkj.aiim.utils.JumpUtils;
import com.base.commonlibrary.utils.MMKVUtils;

import java.util.HashMap;
import java.util.Map;

import cc.shinichi.library.tool.text.MD5Util;
import okhttp3.Response;

public class RetrieveActivity extends BaseSimpleActivity implements View.OnClickListener {


    private String loginType = "0";
    private View viHead;
    private ImageView imFinish;
    private TextView tvLanguage;
    private ImageView imYzm;
    private TextView tvYzm;
    private RelativeLayout rlYzm;
    private ImageView imPassword;
    private TextView tvPassword;
    private RelativeLayout rlPassword;
    private TextView tvDiqu;
    private EditText etPhone;
    private EditText etYzm;
    private TextView tvGetCode;
    private LinearLayout llYzmPhone;
    private EditText etPasswordPhone;
    private EditText etPayPassword;
    private EditText etPasswordYzm;
    private ImageView ivYzm;
    private LinearLayout llPassword;
    private EditText etNewPassword;
    private ImageView imNewEyes;
    private EditText etPassword;
    private ImageView imEnterEyes;
    private TextView tvSubmit;
    private CheckBox cbAgree;
    private TextView tvXy;
    private TextView tvYs;

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
        return R.layout.activity_retrieve;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height",
                "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        LinearLayout.LayoutParams linearParams = (LinearLayout.LayoutParams) viHead.getLayoutParams(); //取控件textView当前的布局参数
        linearParams.height = result;// 控件的高强制设成20
        viHead.setLayoutParams(linearParams);

        tvLanguage.setText(MMKVUtils.Companion.getYuYan());

        imFinish.setOnClickListener(this);
        tvLanguage.setOnClickListener(this);
        tvSubmit.setOnClickListener(this);
        rlYzm.setOnClickListener(this);
        rlPassword.setOnClickListener(this);
        tvDiqu.setOnClickListener(this);
        imNewEyes.setOnClickListener(this);
        imEnterEyes.setOnClickListener(this);
        tvXy.setOnClickListener(this);
        tvYs.setOnClickListener(this);
        tvGetCode.setOnClickListener(this);
        ivYzm.setOnClickListener(this);

        getCode();
    }

    @Override
    public void onClick(View view) {
        Intent intent;
        if (view.getId() ==R.id.imFinish){
            finish();
        }else if (view.getId()==R.id.tvLanguage){
            intent = new Intent(this, LanguageActivity.class);
            startActivity(intent);
        }else  if (view.getId() ==R.id.tvGetCode){
            if (StringUtil.isEmpty(etPhone.getText().toString())) {
                ToastUtil.show(getString(R.string.请输入手机号));
                return;
            }
            sendMsg();
        }else   if (view.getId() == R.id.ivYzm){
            getCode();
        }else  if (view.getId() == R.id.tvSubmit){
            if (loginType.equals("0")) {//验证码找回
                if (StringUtil.isEmpty(etPhone.getText().toString())) {
                    ToastUtil.show(getString(R.string.请输入手机号));
                    return;
                }
                if (StringUtil.isEmpty(etYzm.getText().toString())) {
                    ToastUtil.show(getString(R.string.请输入验证码));
                    return;
                }
                if (StringUtil.isEmpty(etNewPassword.getText().toString())) {
                    ToastUtil.show(getString(R.string.请输入新密码));
                    return;
                }
                if (StringUtil.isEmpty(etPassword.getText().toString())) {
                    ToastUtil.show(getString(R.string.请确认新密码));
                    return;
                }
                if (!etNewPassword.getText().toString().equals(etPassword.getText().toString())) {
                    ToastUtil.show(getString(R.string.两次密码不一致));
                    return;
                }
                if (!cbAgree.isChecked()) {
                    ToastUtil.show(getString(R.string.请先同意用户协议和隐私协议));
                    return;
                }
                zhaohui();
            } else if (loginType.equals("1")) {//支付密码找回
                if (StringUtil.isEmpty(etPasswordPhone.getText().toString())) {
                    ToastUtil.show(getString(R.string.请输入手机号));
                    return;
                }
                if (StringUtil.isEmpty(etPayPassword.getText().toString())) {
                    ToastUtil.show(getString(R.string.请输入支付密码));
                    return;
                }
                if (StringUtil.isEmpty(etPasswordYzm.getText().toString())) {
                    ToastUtil.show(getString(R.string.请输入图文验证码));
                    return;
                }
                if (StringUtil.isEmpty(etNewPassword.getText().toString())) {
                    ToastUtil.show(getString(R.string.请输入新密码));
                    return;
                }
                if (StringUtil.isEmpty(etPassword.getText().toString())) {
                    ToastUtil.show(getString(R.string.请确认新密码));
                    return;
                }
                if (!etNewPassword.getText().toString().equals(etPassword.getText().toString())) {
                    ToastUtil.show(getString(R.string.两次密码不一致));
                    return;
                }
                if (!cbAgree.isChecked()) {
                    ToastUtil.show(getString(R.string.请先同意用户协议和隐私协议));
                    return;
                }
                zhaohui2();
            }
        }else  if (view.getId() ==R.id.rlYzm){
            loginType = "0";
            tvYzm.setTextColor(Color.parseColor("#222222"));
            tvPassword.setTextColor(Color.parseColor("#515763"));
            llPassword.setVisibility(View.GONE);
            imPassword.setVisibility(View.GONE);
            llYzmPhone.setVisibility(View.VISIBLE);
            imYzm.setVisibility(View.VISIBLE);
        }else  if (view.getId() == R.id.rlPassword){
            loginType = "1";
            tvYzm.setTextColor(Color.parseColor("#515763"));
            tvPassword.setTextColor(Color.parseColor("#222222"));
            llPassword.setVisibility(View.VISIBLE);
            imPassword.setVisibility(View.VISIBLE);
            llYzmPhone.setVisibility(View.GONE);
            imYzm.setVisibility(View.GONE);
        }else  if(view.getId() ==R.id.tvDiqu){
            intent = new Intent(this, RegionActivity.class);
            startActivityForResult(intent, 333);
        }else  if (view.getId() == R.id.imNewEyes){
            if (etNewPassword.getTransformationMethod() == null) {
                etNewPassword.setTransformationMethod(new PasswordTransformationMethod());
                imNewEyes.setImageResource(R.mipmap.ic_biyan);
            } else {
                etNewPassword.setTransformationMethod(null);
                imNewEyes.setImageResource(R.mipmap.ic_zhengyan);
            }
            etNewPassword.setSelection(etNewPassword.getText().toString().length());
        }else  if (view.getId() == R.id.imEnterEyes) {
            if (etPassword.getTransformationMethod() == null) {
                etPassword.setTransformationMethod(new PasswordTransformationMethod());
                imEnterEyes.setImageResource(R.mipmap.ic_biyan);
            } else {
                etPassword.setTransformationMethod(null);
                imEnterEyes.setImageResource(R.mipmap.ic_zhengyan);
            }
            etPassword.setSelection(etPassword.getText().toString().length());
        }else  if (view.getId() ==R.id.tvXy){
            JumpUtils.Companion.startH5Activity(this, Url.YHXY, getString(R.string.用户协议));
        }else  if (view.getId()==R.id.tvYs){
            JumpUtils.Companion.startH5Activity(this, Url.YSXY, getString(R.string.隐私协议));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 333 && resultCode == 333) {
            tvDiqu.setText("+" + data.getStringExtra("no"));
        }
    }

    private String uuid = "";

    /**
     * 获取图形验证码
     */
    private void getCode() {
        Map<String, Object> param = new HashMap<>();
        OkHttpHelper.getInstance().post_json(this, Url.getCode, param, new SpotsCallBack<ResultBean>(this) {
            @Override
            public void onSuccess(Response response, ResultBean resultBean) {
                uuid = resultBean.uuid;
                //Base64编码地址(地址太长，省略)
                String base64String = resultBean.yanzm;
                //将Base64编码字符串解码成Bitmap
                byte[] decodedString = Base64.decode(base64String, Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                //设置ImageView图片
                ivYzm.setImageBitmap(decodedByte);
            }

            @Override
            public void onError(Response response, int code, Exception e) {
            }
        });
    }

    /**
     * 支付密码找回密码
     */
    private void zhaohui2() {
        Map<String, Object> param = new HashMap<>();
        param.put("phone", etPasswordPhone.getText().toString());
        param.put("payPwd", etPayPassword.getText().toString());
        param.put("code", etPasswordYzm.getText().toString());
        param.put("pwd", etPassword.getText().toString());
        param.put("uuid", uuid);
        OkHttpHelper.getInstance().post_json(this, Url.zhaohui2, param, new SpotsCallBack<ResultBean>(this) {
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
     * 验证码找回密码
     */
    private void zhaohui() {
        Map<String, Object> param = new HashMap<>();
        param.put("phone", etPhone.getText().toString());
        param.put("qu", tvDiqu.getText().toString().replace("+", ""));
        param.put("code", etYzm.getText().toString());
        param.put("pwd", etPassword.getText().toString());
        OkHttpHelper.getInstance().post_json(this, Url.zhaohui, param, new SpotsCallBack<ResultBean>(this) {
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
        viHead = findViewById(R.id.viHead);
        imFinish = findViewById(R.id.imFinish);
        tvLanguage = findViewById(R.id.tvLanguage);
        imYzm = findViewById(R.id.imYzm);
        tvYzm = findViewById(R.id.tvYzm);
        rlYzm = findViewById(R.id.rlYzm);
        imPassword = findViewById(R.id.imPassword);
        tvPassword = findViewById(R.id.tvPassword);
        rlPassword = findViewById(R.id.rlPassword);
        tvDiqu = findViewById(R.id.tvDiqu);
        etPhone = findViewById(R.id.etPhone);
        etYzm = findViewById(R.id.etYzm);
        tvGetCode = findViewById(R.id.tvGetCode);
        llYzmPhone = findViewById(R.id.llYzmPhone);
        etPasswordPhone = findViewById(R.id.etPasswordPhone);
        etPayPassword = findViewById(R.id.etPayPassword);
        etPasswordYzm = findViewById(R.id.etPasswordYzm);
        ivYzm = findViewById(R.id.ivYzm);
        llPassword = findViewById(R.id.llPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        imNewEyes = findViewById(R.id.imNewEyes);
        etPassword = findViewById(R.id.etPassword);
        imEnterEyes = findViewById(R.id.imEnterEyes);
        tvSubmit = findViewById(R.id.tvSubmit);
        cbAgree = findViewById(R.id.cbAgree);
        tvXy = findViewById(R.id.tvXy);
        tvYs = findViewById(R.id.tvYs);
    }
}