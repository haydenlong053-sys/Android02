package com.lxkj.aiim.lj;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;

import com.cboda.baselibrary.event.AppBroadEvent;
import com.cboda.baselibrary.event.AppEventBroadCode;
import com.cboda.baselibrary.network.config.NetworkBroadConfig;
import com.google.gson.Gson;
import com.lxkj.aiim.R;
import com.lxkj.aiim.base.BaseSimpleActivity;
import com.lxkj.aiim.databinding.ActivityEquipmentBinding;
import com.lxkj.aiim.dialog.QrdlDialog;
import com.lxkj.aiim.dialog.pop.AuthWayPop;
import com.lxkj.aiim.lj.bean.EventBusBean;
import com.lxkj.aiim.lj.bean.ResultBean;
import com.lxkj.aiim.lj.configuration.AppConsts;
import com.lxkj.aiim.lj.configuration.SafeJumpEnum;
import com.lxkj.aiim.lj.configuration.ScanJumpEnum;
import com.lxkj.aiim.lj.http.SpotsCallBack;
import com.lxkj.aiim.lj.http.Url;
import com.lxkj.aiim.lj.utils.SharePrefBroadUtil;
import com.lxkj.aiim.lj.utils.StringUtil;
import com.lxkj.aiim.lj.utils.TimerUtil;
import com.lxkj.aiim.lj.utils.ToastUtil;
import com.lxkj.aiim.net.manager.UserServerManager;
import com.lxkj.aiim.utils.AppUtil;
import com.lxkj.aiim.utils.JumpUtils;
import com.cboda.baselibrary.utils.MMKVBroadUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;
import java.util.Map;

import com.cboda.chatui.ChatManagerHolder;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import okhttp3.Response;

@Deprecated
public class EquipmentActivity extends BaseSimpleActivity implements View.OnClickListener {


    private String eqno;
    private ActivityEquipmentBinding mBinding;

    @Override
    public boolean showOrHideTitleLayout() {
        return false;
    }

    @Override
    protected boolean transparentStatus() {
        return true;
    }

//    @Override
//    protected int getLayoutId() {
//        return R.layout.activity_equipment;
//    }


    @Override
    protected boolean useViewBinding() {
        return true;
    }

    @Nullable
    @Override
    protected Class<? extends ViewBinding> getViewBindingClass() {
        return ActivityEquipmentBinding.class;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = (ActivityEquipmentBinding) getBinding();
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height",
                "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        LinearLayout.LayoutParams linearParams = (LinearLayout.LayoutParams) mBinding.viHead.getLayoutParams(); //取控件textView当前的布局参数
        linearParams.height = result;// 控件的高强制设成20
        mBinding.viHead.setLayoutParams(linearParams);

        EventBus.getDefault().register(this);

        eqno = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        mBinding.tvLanguage.setText(MMKVBroadUtils.Companion.getYuYan());
        mBinding.tvLanguage.setOnClickListener(this);
        mBinding.tvLogin.setOnClickListener(this);
        mBinding.tvZhuce.setOnClickListener(this);
        mBinding.ivScan.setOnClickListener(this);
        mBinding.rlUser.setOnClickListener(this);
        mBinding.rlPhone.setOnClickListener(this);
        mBinding.tvXy.setOnClickListener(this);
        mBinding.tvYs.setOnClickListener(this);
        mBinding.tvGetCode.setOnClickListener(this);
        mBinding.tvThrid.setOnClickListener(this);
        mBinding.cbCheckPass.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                int passwordLength = mBinding.etNewPassword.getText().length();
                mBinding.etNewPassword.setInputType(isChecked ?
                        (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD) : (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)
                );
                mBinding.etNewPassword.setSelection(passwordLength);
            }
        });
    }

    private int loginType = 0;//0用户名密码  1一键登录

    @Override
    public void onClick(View view) {
        Intent intent;
        if (view.getId()==R.id.tvLanguage){
            intent = new Intent(this, LanguageActivity.class);
            startActivity(intent);
        }else  if (view.getId() == R.id.tvLogin){
            if (mBinding.etPhone.getText().toString().trim().isEmpty()||mBinding.etNewPassword.getText().toString().trim().isEmpty()){
                loginType=1;
            } else {
                loginType=0;
            }
            if (loginType==0){
                accLogin("");
            }else {
                eqLogin();
            }



//                if (!mBinding.cbAgree.isChecked()) {
//                    ToastUtil.show(getString(R.string.请先同意用户协议和隐私协议));
//                    return;
//                }
//                if (loginType == 0) {
//                    if (StringUtil.isEmpty(mBinding.etPhone.getText().toString())) {
//                        ToastUtil.show(getString(R.string.请输入用户名));
//                        return;
//                    }
//                    if (StringUtil.isEmpty(mBinding.etNewPassword.getText().toString())) {
//                        ToastUtil.show(getString(R.string.请输入密码));
//                        return;
//                    }
////                    if (StringUtil.isEmpty(mBinding.etYzm.getText().toString())) {
////                        ToastUtil.show(getString(R.string.请输入验证码));
////                        return;
////                    }
//                    accLogin("");
//                } else {
//                    if (StringUtil.isEmpty(eqno)) {
//                        ToastUtil.show("设备号为空");
//                        return;
//                    }
//                eqLogin();
//                }
        }else if (view.getId()==R.id.tvZhuce){
            //                regist();
            startActivity(new Intent(this, RegisterActivity.class));
        }else  if (view.getId() ==R.id.ivScan){
            Intent intent1 = new Intent(this, ScanActivity.class);
            intent1.putExtra("jumpSource", ScanJumpEnum.Login.getSourceType());
            startActivity(intent1);
        }else  if (view.getId() ==R.id.rlUser){
            loginType = 0;
            mBinding.imUser.setVisibility(View.VISIBLE);
            mBinding.imPhone.setVisibility(View.GONE);
            mBinding.llUser.setVisibility(View.VISIBLE);
            mBinding.tvYjdl.setVisibility(View.GONE);
        }else  if (view.getId() ==R.id.rlPhone){
            loginType = 1;
            mBinding.imPhone.setVisibility(View.VISIBLE);
            mBinding.imUser.setVisibility(View.GONE);
            mBinding.llUser.setVisibility(View.INVISIBLE);
            mBinding.tvYjdl.setVisibility(View.VISIBLE);
        }else  if (view.getId() ==R.id.tvXy){
            JumpUtils.Companion.startH5Activity(this, Url.YHXY, getString(R.string.用户协议));
        }else  if (view.getId() ==R.id.tvYs){
            JumpUtils.Companion.startH5Activity(this, Url.YSXY, getString(R.string.隐私协议));
        }else if (view.getId()==R.id.tvThrid){
            JumpUtils.Companion.startH5Activity(this, Url.thirdContent, getString(R.string.第三方信息共享清单));
        }else if (view.getId()==R.id.tvGetCode){
            getCode();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = false)
    public void senmessage(EventBusBean eventBusBean) {//登录成功
        if (eventBusBean.msg.equals("登录成功")) {
            finish();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void receiveEventMsg(AppBroadEvent<Object> eventBusBean) {
        if (eventBusBean.getEventCode()== AppEventBroadCode.EVENT_NEW_MACHINE_CHECK && eventBusBean.getData()!=null) {
            ResultBean resultBean = (ResultBean) eventBusBean.getData();
            SharePrefBroadUtil.saveString(this, AppConsts.APPTOKEN, resultBean.apptoken);
            NetworkBroadConfig.INSTANCE.updateAppToken(resultBean.apptoken);
            AuthWayPop authWayPop = new AuthWayPop(this, SafeJumpEnum.LoginNewMachine.getSourceType(),true,null,null);
            authWayPop.transferInvoke =new Function0<Unit>() {
                @Override
                public Unit invoke() {
                    AppUtil.Companion.imLogin(EquipmentActivity.this, resultBean.apptoken, resultBean.userSign, resultBean.userId, resultBean.imtoken);
                    return null;
                }
            };
            authWayPop.showPopupWindow();
        }
    }


    /**
     * 获取验证码
     */
    private void getCode() {
        if (StringUtil.isEmpty(mBinding.etPhone.getText().toString())) {
            ToastUtil.show(getString(R.string.请输入用户名));
            return;
        }
        Map<String, Object> param = new HashMap<>();
        param.put("acc", mBinding.etPhone.getText().toString());

        UserServerManager.INSTANCE.getCode(this, param, Url.getAccCode, new SpotsCallBack<ResultBean>(this) {
            @Override
            public void onSuccess(Response response, ResultBean resultBean) {
                TimerUtil mTimerUtil = new TimerUtil(mBinding.tvGetCode);
                mTimerUtil.timers();
                ToastUtil.show(getString(R.string.验证码已发送请注意查收));
            }

            @Override
            public void onError(Response response, int code, Exception e) {
            }
        });
    }

    /**
     * 设备号登录
     */
    private void eqLogin() {
        Map<String, Object> param = new HashMap<>();
        param.put("eqno", eqno);
        param.put("clientId", ChatManagerHolder.gChatManager.getClientId());
        param.put("type", "2");
        param.put("model", Build.BRAND + " " + Build.MODEL);
        Log.d("设备嘛",eqno +"  \n"+ChatManagerHolder.gChatManager.getClientId());
        UserServerManager.INSTANCE.qeLogin(this, param, Url.eqLogin, new SpotsCallBack<ResultBean>(this) {

            @Override
            public void onSuccess(Response response, ResultBean resultBean) {
//                if (resultBean.isws.equals("0")) {//是否已完善资料
//                    Intent intent = new Intent(EquipmentActivity.this, PushInfoActivity.class);
//                    intent.putExtra("apptoken", resultBean.apptoken);
//                    intent.putExtra("uid", resultBean.userId);
//                    intent.putExtra("userSign", resultBean.userSign);
//                    intent.putExtra("imtoken", resultBean.imtoken);
//                    intent.putExtra("idno", resultBean.idno);
//                    intent.putExtra("qrcode", resultBean.qrcode);
//                    startActivity(intent);
//                } else {
                AppUtil.Companion.imLogin(EquipmentActivity.this, resultBean.apptoken, resultBean.userSign, resultBean.userId, resultBean.imtoken);
//                }

            }

            @Override
            public void onError(Response response, int code, Exception e) {

            }
        });

    }

    /**
     * 账号密码登录
     */
    private void accLogin(String code) {
        Map<String, Object> param = new HashMap<>();
        param.put("acc", mBinding.etPhone.getText().toString());
        param.put("pwd", mBinding.etNewPassword.getText().toString());
        param.put("eqno", eqno);
        param.put("code", code);
        param.put("clientId", ChatManagerHolder.gChatManager.getClientId());
        param.put("type", "2");
        param.put("model", Build.BRAND + " " + Build.MODEL);

        UserServerManager.INSTANCE.accLogin(this, param, Url.accLogin, new SpotsCallBack<String>(this) {
            @Override
            public void onSuccess(Response response, String result) {
                ResultBean resultBean = new Gson().fromJson(result, ResultBean.class);
                if (resultBean.code.equals("202")) {
                    new QrdlDialog(EquipmentActivity.this, new QrdlDialog.OnInputListener() {
                        @Override
                        public void onInput(String code) {
                            accLogin(code);
                        }
                    }, mBinding.etPhone.getText().toString()).show();
                } else if (resultBean.code.equals("200") && resultBean.success) {
                    AppUtil.Companion.imLogin(EquipmentActivity.this, resultBean.apptoken, resultBean.userSign, resultBean.userId, resultBean.imtoken);
                }else {
                    if (!TextUtils.isEmpty(resultBean.msg)){
                        ToastUtil.show(resultBean.msg);
                    }

                }
//                if (resultBean.isws.equals("0")) {//是否已完善资料
//                    Intent intent = new Intent(EquipmentActivity.this, PushInfoActivity.class);
//                    intent.putExtra("apptoken", resultBean.apptoken);
//                    intent.putExtra("uid", resultBean.userId);
//                    intent.putExtra("userSign", resultBean.userSign);
//                    intent.putExtra("imtoken", resultBean.imtoken);
//                    intent.putExtra("idno", resultBean.idno);
//                    intent.putExtra("qrcode", resultBean.qrcode);
//                    startActivity(intent);
//                } else {

//                }
            }

            @Override
            public void onError(Response response, int code, Exception e) {
            }
        });
    }


    private void regist() {
        Map<String, Object> param = new HashMap<>();
        param.put("eqno", eqno);
        param.put("clientId", ChatManagerHolder.gChatManager.getClientId());
        param.put("type", "2");
        param.put("model", Build.BRAND + " " + Build.MODEL);

        UserServerManager.INSTANCE.regist(this, param, Url.regist, new SpotsCallBack<ResultBean>(this) {
            @Override
            public void onSuccess(Response response, ResultBean resultBean) {
                if (resultBean.isws.equals("0")) {//是否已完善资料
                    Intent intent = new Intent(EquipmentActivity.this, PushInfoActivity.class);
                    intent.putExtra("apptoken", resultBean.apptoken);
                    intent.putExtra("uid", resultBean.userId);
                    intent.putExtra("userSign", resultBean.userSign);
                    intent.putExtra("imtoken", resultBean.imtoken);
                    intent.putExtra("idno", resultBean.idno);
                    intent.putExtra("qrcode", resultBean.qrcode);
                    startActivity(intent);
                } else {
                    AppUtil.Companion.imLogin(EquipmentActivity.this, resultBean.apptoken, resultBean.userSign, resultBean.userId, resultBean.imtoken);
                }
            }

            @Override
            public void onError(Response response, int code, Exception e) {
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}