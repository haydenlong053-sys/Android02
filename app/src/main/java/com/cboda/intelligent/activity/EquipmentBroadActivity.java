//package com.cboda.intelligent.activity;
//
//import static com.cboda.baselibrary.utils.ELogUtilKt.eLogD;
//
//import android.content.Intent;
//import android.os.Build;
//import android.os.Bundle;
//import android.provider.Settings;
//import android.text.InputType;
//import android.text.TextUtils;
//import android.view.View;
//import android.widget.CompoundButton;
//import android.widget.LinearLayout;
//
//import androidx.annotation.Nullable;
//import androidx.viewbinding.ViewBinding;
//
//import com.cboda.baselibrary.event.AppBroadEvent;
//import com.cboda.baselibrary.event.AppEventBroadCode;
//import com.cboda.baselibrary.network.config.NetworkBroadConfig;
//import com.cboda.intelligent.databinding.ActivityBroadEquipmentBinding;
//import com.google.gson.Gson;
//import com.cboda.intelligent.R;
//import com.cboda.intelligent.base.BaseSimpleBroadActivity;
//import com.cboda.intelligent.dialog.QrdlBroadDialog;
//import com.cboda.intelligent.dialog.pop.AuthWayBroadPop;
//import com.cboda.intelligent.bean.EventBusBroadBean;
//import com.cboda.intelligent.bean.ResultBroadBean;
//import com.cboda.intelligent.configuration.AppBroadConsts;
//import com.cboda.intelligent.configuration.SafeJumpBroadEnum;
//import com.cboda.intelligent.configuration.ScanJumpBroadEnum;
//import com.cboda.intelligent.utils.SpotsCallBroadBack;
//import com.cboda.intelligent.net.BroadUrl;
//import com.cboda.intelligent.utils.SharePrefBroadBroadUtil;
//import com.cboda.intelligent.utils.StringBroadUtil;
//import com.cboda.intelligent.utils.TimerBroadUtil;
//import com.cboda.intelligent.utils.ToastBroadUtil;
//import com.cboda.intelligent.net.manager.UserServerBroadManager;
//import com.cboda.intelligent.utils.AppBroadUtils;
//import com.cboda.intelligent.utils.JumpBroadUtils;
//import com.cboda.baselibrary.utils.MMKVBroadUtils;
//
//import org.greenrobot.eventbus.EventBus;
//import org.greenrobot.eventbus.Subscribe;
//import org.greenrobot.eventbus.ThreadMode;
//
//import java.util.HashMap;
//import java.util.Map;
//
//import com.cboda.chatui.ChatManagerBroadHolder;
//import kotlin.Unit;
//import kotlin.jvm.functions.Function0;
//import okhttp3.Response;
//
//@Deprecated
//public class EquipmentBroadActivity extends BaseSimpleBroadActivity implements View.OnClickListener {
//
//
//    private String eqno;
//    private ActivityBroadEquipmentBinding mBinding;
//
//    @Override
//    public boolean showOrHideTitleLayout() {
//        return false;
//    }
//
//    @Override
//    protected boolean transparentStatus() {
//        return true;
//    }
//
////    @Override
////    protected int getLayoutId() {
////        return R.layout.activity_equipment;
////    }
//
//
//    @Override
//    protected boolean useViewBinding() {
//        return true;
//    }
//
//    @Nullable
//    @Override
//    protected Class<? extends ViewBinding> getViewBindingClass() {
//        return ActivityBroadEquipmentBinding.class;
//    }
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        mBinding = (ActivityBroadEquipmentBinding) getBinding();
//        int result = 0;
//        int resourceId = getResources().getIdentifier("status_bar_height",
//                "dimen", "android");
//        if (resourceId > 0) {
//            result = getResources().getDimensionPixelSize(resourceId);
//        }
//        LinearLayout.LayoutParams linearParams = (LinearLayout.LayoutParams) mBinding.viHead.getLayoutParams(); //取控件textView当前的布局参数
//        linearParams.height = result;// 控件的高强制设成20
//        mBinding.viHead.setLayoutParams(linearParams);
//
//        EventBus.getDefault().register(this);
//
//        eqno = DeviceBroadUtil.INSTANCE.getAndroidId(this);
//
////        mBinding.tvLanguage.setText(MMKVBroadUtils.Companion.getYuYan());
//        mBinding.tvLanguage.setOnClickListener(this);
//        mBinding.tvLogin.setOnClickListener(this);
//        mBinding.tvZhuce.setOnClickListener(this);
//        mBinding.ivScan.setOnClickListener(this);
//        mBinding.rlUser.setOnClickListener(this);
//        mBinding.rlPhone.setOnClickListener(this);
//        mBinding.tvXy.setOnClickListener(this);
//        mBinding.tvYs.setOnClickListener(this);
//        mBinding.tvGetCode.setOnClickListener(this);
//        mBinding.tvThrid.setOnClickListener(this);
//        mBinding.cbCheckPass.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                int passwordLength = mBinding.etNewPassword.getText().length();
//                mBinding.etNewPassword.setInputType(isChecked ?
//                        (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD) : (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)
//                );
//                mBinding.etNewPassword.setSelection(passwordLength);
//            }
//        });
//    }
//
//    private int loginType = 0;//0用户名密码  1一键登录
//
//    @Override
//    public void onClick(View view) {
//        Intent intent;
//        if (view.getId()==R.id.tvLanguage){
//            intent = new Intent(this, LanguageBroadActivity.class);
//            startActivity(intent);
//        }else  if (view.getId() == R.id.tvLogin){
//            if (mBinding.etPhone.getText().toString().trim().isEmpty()||mBinding.etNewPassword.getText().toString().trim().isEmpty()){
//                loginType=1;
//            } else {
//                loginType=0;
//            }
//            if (loginType==0){
//                accLogin("");
//            }else {
//                eqLogin();
//            }
//
//
//
////                if (!mBinding.cbAgree.isChecked()) {
////                    ToastBroadUtil.show(getString(R.string.请先同意用户协议和隐私协议));
////                    return;
////                }
////                if (loginType == 0) {
////                    if (StringBroadUtil.isEmpty(mBinding.etPhone.getText().toString())) {
////                        ToastBroadUtil.show(getString(R.string.请输入用户名));
////                        return;
////                    }
////                    if (StringBroadUtil.isEmpty(mBinding.etNewPassword.getText().toString())) {
////                        ToastBroadUtil.show(getString(R.string.请输入密码));
////                        return;
////                    }
//////                    if (StringBroadUtil.isEmpty(mBinding.etYzm.getText().toString())) {
//////                        ToastBroadUtil.show(getString(R.string.请输入验证码));
//////                        return;
//////                    }
////                    accLogin("");
////                } else {
////                    if (StringBroadUtil.isEmpty(eqno)) {
////                        ToastBroadUtil.show("设备号为空");
////                        return;
////                    }
////                eqLogin();
////                }
//        }else if (view.getId()==R.id.tvZhuce){
//            //                regist();
//            startActivity(new Intent(this, RegisterBroadActivity.class));
//        }else  if (view.getId() ==R.id.ivScan){
//            Intent intent1 = new Intent(this, ScanBroadActivity.class);
//            intent1.putExtra("jumpSource", ScanJumpBroadEnum.Login.getSourceType());
//            startActivity(intent1);
//        }else  if (view.getId() ==R.id.rlUser){
//            loginType = 0;
//            mBinding.imUser.setVisibility(View.VISIBLE);
//            mBinding.imPhone.setVisibility(View.GONE);
//            mBinding.llUser.setVisibility(View.VISIBLE);
//            mBinding.tvYjdl.setVisibility(View.GONE);
//        }else  if (view.getId() ==R.id.rlPhone){
//            loginType = 1;
//            mBinding.imPhone.setVisibility(View.VISIBLE);
//            mBinding.imUser.setVisibility(View.GONE);
//            mBinding.llUser.setVisibility(View.INVISIBLE);
//            mBinding.tvYjdl.setVisibility(View.VISIBLE);
//        }else  if (view.getId() ==R.id.tvXy){
//            JumpBroadUtils.Companion.startH5Activity(this,  BroadUrl.getUserAgreementUrl(), getString(R.string.用户协议));
//        }else  if (view.getId() ==R.id.tvYs){
//            JumpBroadUtils.Companion.startH5Activity(this, BroadUrl.getPrivacyPolicyUrl(), getString(R.string.隐私协议));
//        }else if (view.getId()==R.id.tvThrid){
//            JumpBroadUtils.Companion.startH5Activity(this, BroadUrl.getThirdContentUrl(), getString(R.string.第三方信息共享清单));
//        }else if (view.getId()==R.id.tvGetCode){
//            getCode();
//        }
//    }
//
//    @Subscribe(threadMode = ThreadMode.MAIN, sticky = false)
//    public void senmessage(EventBusBroadBean eventBusBean) {//登录成功
//        if (eventBusBean.msg.equals("登录成功")) {
//            finish();
//        }
//    }
//
//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void receiveEventMsg(AppBroadEvent<Object> eventBusBean) {
//        if (eventBusBean.getEventCode()== AppEventBroadCode.EVENT_NEW_MACHINE_CHECK && eventBusBean.getData()!=null) {
//            ResultBroadBean resultBean = (ResultBroadBean) eventBusBean.getData();
//            SharePrefBroadBroadUtil.saveString(this, AppBroadConsts.APPTOKEN, resultBean.apptoken);
//            NetworkBroadConfig.INSTANCE.updateAppToken(resultBean.apptoken);
//            AuthWayBroadPop authWayPop = new AuthWayBroadPop(this, SafeJumpBroadEnum.LoginNewMachine.getSourceType(),true,null,null);
//            authWayPop.transferInvoke =new Function0<Unit>() {
//                @Override
//                public Unit invoke() {
//                    AppBroadUtils.Companion.imLogin(EquipmentBroadActivity.this, resultBean.apptoken, resultBean.userSign, resultBean.userId, resultBean.imtoken);
//                    return null;
//                }
//            };
//            authWayPop.showPopupWindow();
//        }
//    }
//
//
//    /**
//     * 获取验证码
//     */
//    private void getCode() {
//        if (StringBroadUtil.isEmpty(mBinding.etPhone.getText().toString())) {
//            ToastBroadUtil.show(getString(R.string.请输入用户名));
//            return;
//        }
//        Map<String, Object> param = new HashMap<>();
//        param.put("acc", mBinding.etPhone.getText().toString());
//
//        UserServerBroadManager.INSTANCE.getCode(this, param, BroadUrl.getAccCode, new SpotsCallBroadBack<ResultBroadBean>(this) {
//            @Override
//            public void onSuccess(Response response, ResultBroadBean resultBean) {
//                TimerBroadUtil mTimerUtil = new TimerBroadUtil(mBinding.tvGetCode);
//                mTimerUtil.timers();
//                ToastBroadUtil.show(getString(R.string.验证码已发送请注意查收));
//            }
//
//            @Override
//            public void onError(Response response, int code, Exception e) {
//            }
//        });
//    }
//
//    /**
//     * 设备号登录
//     */
//    private void eqLogin() {
//        Map<String, Object> param = new HashMap<>();
//        param.put("eqno", eqno);
//        param.put("clientId", ChatManagerBroadHolder.gChatManager.getClientId());
//        param.put("type", "2");
//        param.put("model", Build.BRAND + " " + Build.MODEL);
//        eLogD("设备嘛",eqno +"  \n"+ChatManagerBroadHolder.gChatManager.getClientId());
//        UserServerBroadManager.INSTANCE.qeLogin(this, param, BroadUrl.eqLogin, new SpotsCallBroadBack<ResultBroadBean>(this) {
//
//            @Override
//            public void onSuccess(Response response, ResultBroadBean resultBean) {
////                if (resultBean.isws.equals("0")) {//是否已完善资料
////                    Intent intent = new Intent(EquipmentBroadActivity.this, PushInfoBroadActivity.class);
////                    intent.putExtra("apptoken", resultBean.apptoken);
////                    intent.putExtra("uid", resultBean.userId);
////                    intent.putExtra("userSign", resultBean.userSign);
////                    intent.putExtra("imtoken", resultBean.imtoken);
////                    intent.putExtra("idno", resultBean.idno);
////                    intent.putExtra("qrcode", resultBean.qrcode);
////                    startActivity(intent);
////                } else {
//                AppBroadUtils.Companion.imLogin(EquipmentBroadActivity.this, resultBean.apptoken, resultBean.userSign, resultBean.userId, resultBean.imtoken);
////                }
//
//            }
//
//            @Override
//            public void onError(Response response, int code, Exception e) {
//
//            }
//        });
//
//    }
//
//    /**
//     * 账号密码登录
//     */
//    private void accLogin(String code) {
//        Map<String, Object> param = new HashMap<>();
//        param.put("acc", mBinding.etPhone.getText().toString());
//        param.put("pwd", mBinding.etNewPassword.getText().toString());
//        param.put("eqno", eqno);
//        param.put("code", code);
//        param.put("clientId", ChatManagerBroadHolder.gChatManager.getClientId());
//        param.put("type", "2");
//        param.put("model", Build.BRAND + " " + Build.MODEL);
//
//        UserServerBroadManager.INSTANCE.accLogin(this, param, BroadUrl.accLogin, new SpotsCallBroadBack<String>(this) {
//            @Override
//            public void onSuccess(Response response, String result) {
//                ResultBroadBean resultBean = new Gson().fromJson(result, ResultBroadBean.class);
//                if (resultBean.code.equals("202")) {
//                    new QrdlBroadDialog(EquipmentBroadActivity.this, new QrdlBroadDialog.OnInputListener() {
//                        @Override
//                        public void onInput(String code) {
//                            accLogin(code);
//                        }
//                    }, mBinding.etPhone.getText().toString()).show();
//                } else if (resultBean.code.equals("200") && resultBean.success) {
//                    AppBroadUtils.Companion.imLogin(EquipmentBroadActivity.this, resultBean.apptoken, resultBean.userSign, resultBean.userId, resultBean.imtoken);
//                }else {
//                    if (!TextUtils.isEmpty(resultBean.msg)){
//                        ToastBroadUtil.show(resultBean.msg);
//                    }
//
//                }
////                if (resultBean.isws.equals("0")) {//是否已完善资料
////                    Intent intent = new Intent(EquipmentBroadActivity.this, PushInfoBroadActivity.class);
////                    intent.putExtra("apptoken", resultBean.apptoken);
////                    intent.putExtra("uid", resultBean.userId);
////                    intent.putExtra("userSign", resultBean.userSign);
////                    intent.putExtra("imtoken", resultBean.imtoken);
////                    intent.putExtra("idno", resultBean.idno);
////                    intent.putExtra("qrcode", resultBean.qrcode);
////                    startActivity(intent);
////                } else {
//
////                }
//            }
//
//            @Override
//            public void onError(Response response, int code, Exception e) {
//            }
//        });
//    }
//
//
//    private void regist() {
//        Map<String, Object> param = new HashMap<>();
//        param.put("eqno", eqno);
//        param.put("clientId", ChatManagerBroadHolder.gChatManager.getClientId());
//        param.put("type", "2");
//        param.put("model", Build.BRAND + " " + Build.MODEL);
//
//        UserServerBroadManager.INSTANCE.regist(this, param, BroadUrl.regist, new SpotsCallBroadBack<ResultBroadBean>(this) {
//            @Override
//            public void onSuccess(Response response, ResultBroadBean resultBean) {
//                if (resultBean.isws.equals("0")) {//是否已完善资料
//                    Intent intent = new Intent(EquipmentBroadActivity.this, PushInfoBroadActivity.class);
//                    intent.putExtra("apptoken", resultBean.apptoken);
//                    intent.putExtra("uid", resultBean.userId);
//                    intent.putExtra("userSign", resultBean.userSign);
//                    intent.putExtra("imtoken", resultBean.imtoken);
//                    intent.putExtra("idno", resultBean.idno);
//                    intent.putExtra("qrcode", resultBean.qrcode);
//                    startActivity(intent);
//                } else {
//                    AppBroadUtils.Companion.imLogin(EquipmentBroadActivity.this, resultBean.apptoken, resultBean.userSign, resultBean.userId, resultBean.imtoken);
//                }
//            }
//
//            @Override
//            public void onError(Response response, int code, Exception e) {
//            }
//        });
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        EventBus.getDefault().unregister(this);
//    }
//}