package com.cboda.intelligent.activity;

import static com.shuyu.gsyvideoplayer.video.base.GSYVideoView.CURRENT_STATE_PAUSE;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.interfaces.OnSelectListener;
import com.cboda.intelligent.R;
import com.cboda.intelligent.base.BaseSimpleBroadActivity;
import com.cboda.intelligent.bean.DataListBroadBean;
import com.cboda.intelligent.bean.EventBusBroadBean;
import com.cboda.intelligent.bean.ResultBroadBean;
import com.cboda.intelligent.configuration.AppBroadConsts;
import com.cboda.intelligent.utils.BaseBroadCallback;
import com.cboda.intelligent.utils.OkHttpBroadHelper;
import com.cboda.intelligent.utils.SpotsCallBroadBack;
import com.cboda.intelligent.net.BroadUrl;
import com.cboda.intelligent.utils.ListBroadUtil;
import com.cboda.intelligent.utils.SharePrefBroadBroadUtil;
import com.cboda.intelligent.utils.StringBroadUtil;
import com.cboda.intelligent.utils.ToastBroadUtil;
import com.cboda.intelligent.utils.AppBroadUtils;
import com.cboda.intelligent.utils.GlideLoaderBroadHelper;
import com.cboda.intelligent.utils.JumpBroadUtils;
import com.cboda.baselibrary.utils.MMKVBroadUtils;
import com.cboda.intelligent.widge.MyStandardGSYVideoBroadPlayer;
import com.shuyu.gsyvideoplayer.utils.GSYVideoType;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cboda.chatui.ChatManagerBroadHolder;
import okhttp3.Request;
import okhttp3.Response;

public class LoginBroadActivity extends BaseSimpleBroadActivity implements View.OnClickListener {


    private String loginType;
    private ResultBroadBean bean;
    private List<DataListBroadBean> zhanghaoList = new ArrayList<>();
    private ImageView ivRegisterBg;
    private MyStandardGSYVideoBroadPlayer videoplayer;
    private ImageView thumbImage;
    private FrameLayout videoLayout;
    private View viHead;
    private TextView tvLanguage;
    private ImageView imWangluo;
    private ImageView imUser;
    private TextView tvUser;
    private RelativeLayout rlUser;
    private ImageView imPhone;
    private TextView tvPhone;
    private RelativeLayout rlPhone;
    private EditText etUser;
    private LinearLayout llUser;
    private TextView tvDiqu;
    private EditText etPhone;
    private LinearLayout llPhone;
    private EditText etPassword;
    private ImageView imEyes;
    private TextView tvZhaohui;
    private TextView tvZhuce;
    private TextView tvLogin;
    private CheckBox cbAgree;
    private TextView tvXy;
    private TextView tvYs;
    private ImageView imVpn;
    private ImageView imKefu;

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
        return R.layout.activity_broad_login3;
    }

    private void setImageview() {
        ivRegisterBg.setVisibility(View.VISIBLE);
        GlideLoaderBroadHelper.Companion.loadUrl(
                this,
                bean.data.regImg,
                ivRegisterBg,
                0,
                true
        );
    }

    private void setvideo() {
        videoLayout.setVisibility(View.VISIBLE);
        GSYVideoType.setShowType(GSYVideoType.SCREEN_TYPE_FULL);
        videoplayer.setUp(bean.data.regImg, true, "");
        videoplayer.setAutoFullWithSize(true);
        videoplayer.setIfCurrentIsFullscreen(true);
        videoplayer.setLooping(true);
        videoplayer.startPlayLogic();
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

        tvLanguage.setText(MMKVBroadUtils.Companion.getYuYan());

        EventBus.getDefault().register(this);

        zhanghaoList.clear();
        if (!StringBroadUtil.isEmpty(SharePrefBroadBroadUtil.getString(this, AppBroadConsts.zhanghaoList, ""))) {
            // 定义类型
            Type userListType = new TypeToken<List<DataListBroadBean>>() {
            }.getType();
            List<DataListBroadBean> listBeans = (List<DataListBroadBean>) new Gson().fromJson(SharePrefBroadBroadUtil.getString(this, AppBroadConsts.zhanghaoList, ""), userListType);
            zhanghaoList.addAll(listBeans);
        }


        switch (SharePrefBroadBroadUtil.getString(this, AppBroadConsts.LOGINTYPE, "4")) {
            case "2"://用户名密码登录

                loginType = "2";

                rlUser.setVisibility(View.VISIBLE);
                tvUser.setTextColor(Color.parseColor("#222222"));
                imUser.setVisibility(View.GONE);
                rlPhone.setVisibility(View.GONE);

                llUser.setVisibility(View.VISIBLE);
                llPhone.setVisibility(View.GONE);

                if (!ListBroadUtil.isEmpty(zhanghaoList)) {
                    String[] items = new String[zhanghaoList.size()];
                    for (int i = 0; i < zhanghaoList.size(); i++) {
                        items[i] = zhanghaoList.get(i).zhanghao;
                    }
                    new XPopup.Builder(this)
                            .hasShadowBg(false) // 是否有半透明的背景，默认为true
                            .atView(etUser)  // 依附于所点击的View，内部会自动判断在上方或者下方显示
                            .asAttachList(items,
                                    null,
                                    new OnSelectListener() {
                                        @Override
                                        public void onSelect(int xposition, String text) {
                                            etUser.setText(text);
                                            etPassword.setText(zhanghaoList.get(xposition).mima);
                                        }
                                    }, R.layout.popup_broad_recycle, R.layout.custom_part_shadow_broad_popup)
                            .show();
                }

                break;
            case "3"://手机号密码登录

                loginType = "3";

                rlUser.setVisibility(View.GONE);
                rlPhone.setVisibility(View.VISIBLE);
                tvPhone.setTextColor(Color.parseColor("#222222"));
                imPhone.setVisibility(View.GONE);

                llUser.setVisibility(View.GONE);
                llPhone.setVisibility(View.VISIBLE);
                break;
            case "4"://手机号、用户密码登录

                loginType = "2";

                rlUser.setVisibility(View.VISIBLE);
                rlPhone.setVisibility(View.VISIBLE);

                if (!ListBroadUtil.isEmpty(zhanghaoList)) {
                    String[] items = new String[zhanghaoList.size()];
                    for (int i = 0; i < zhanghaoList.size(); i++) {
                        items[i] = zhanghaoList.get(i).zhanghao;
                    }
                    new XPopup.Builder(this)
                            .hasShadowBg(false) // 是否有半透明的背景，默认为true
                            .atView(etUser)  // 依附于所点击的View，内部会自动判断在上方或者下方显示
                            .asAttachList(items,
                                    null,
                                    new OnSelectListener() {
                                        @Override
                                        public void onSelect(int xposition, String text) {
                                            etUser.setText(text);
                                            etPassword.setText(zhanghaoList.get(xposition).mima);
                                        }
                                    }, R.layout.popup_broad_recycle, R.layout.custom_part_shadow_broad_popup)
                            .show();
                }
                break;
        }

        etUser.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (StringBroadUtil.isEmpty(editable.toString())) {
                    if (!ListBroadUtil.isEmpty(zhanghaoList)) {
                        String[] items = new String[zhanghaoList.size()];
                        for (int i = 0; i < zhanghaoList.size(); i++) {
                            items[i] = zhanghaoList.get(i).zhanghao;
                        }
                        new XPopup.Builder(LoginBroadActivity.this)
                                .hasShadowBg(false) // 是否有半透明的背景，默认为true
                                .atView(etUser)  // 依附于所点击的View，内部会自动判断在上方或者下方显示
                                .asAttachList(items,
                                        null,
                                        new OnSelectListener() {
                                            @Override
                                            public void onSelect(int xposition, String text) {
                                                etUser.setText(text);
                                                etPassword.setText(zhanghaoList.get(xposition).mima);
                                            }
                                        }, R.layout.popup_broad_recycle, R.layout.custom_part_shadow_broad_popup)
                                .show();
                    }
                }
            }
        });

        ptConfig();

        tvLanguage.setOnClickListener(this);
        imWangluo.setOnClickListener(this);
        rlUser.setOnClickListener(this);
        rlPhone.setOnClickListener(this);
        imEyes.setOnClickListener(this);
        tvZhaohui.setOnClickListener(this);
        tvZhuce.setOnClickListener(this);
        tvLogin.setOnClickListener(this);
        imVpn.setOnClickListener(this);
        imKefu.setOnClickListener(this);
        tvXy.setOnClickListener(this);
        tvYs.setOnClickListener(this);
        tvDiqu.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        Intent intent;
        if (view.getId() ==R.id.tvLanguage){
            intent = new Intent(this, LanguageBroadActivity.class);
            startActivity(intent);
        }
        if (view.getId() ==R.id.rlUser){
            loginType = "2";
            tvUser.setTextColor(Color.parseColor("#222222"));
            tvPhone.setTextColor(Color.parseColor("#515763"));
            llPhone.setVisibility(View.GONE);
            imPhone.setVisibility(View.GONE);
            llUser.setVisibility(View.VISIBLE);
            imUser.setVisibility(View.VISIBLE);
        }

        if (view.getId() == R.id.rlPhone){
            loginType = "3";
            tvUser.setTextColor(Color.parseColor("#515763"));
            tvPhone.setTextColor(Color.parseColor("#222222"));
            llPhone.setVisibility(View.VISIBLE);
            imPhone.setVisibility(View.VISIBLE);
            llUser.setVisibility(View.GONE);
            imUser.setVisibility(View.GONE);
        }

        if (view.getId() ==R.id.imEyes){
            if (etPassword.getTransformationMethod() == null) {
                etPassword.setTransformationMethod(new PasswordTransformationMethod());
                imEyes.setImageResource(R.mipmap.ic_broad_biyan);
            } else {
                etPassword.setTransformationMethod(null);
                imEyes.setImageResource(R.mipmap.ic_broad_zhengyan);
            }
            etPassword.setSelection(etPassword.getText().toString().length());
        }

        if (view.getId() == R.id.tvZhaohui){
            intent = new Intent(this, RetrieveBroadActivity.class);
            startActivity(intent);
        }
        if (view.getId() == R.id.tvZhuce){
            intent = new Intent(this, RegisterBroadActivity.class);
            intent.putExtra("loginType", loginType);
            startActivity(intent);
        }

        if (view.getId() == R.id.tvLogin){
            if (loginType.equals("2")) {
                if (StringBroadUtil.isEmpty(etUser.getText().toString())) {
                    ToastBroadUtil.show(getString(R.string.请输入用户名));
                    return;
                }
                if (StringBroadUtil.isEmpty(etPassword.getText().toString())) {
                    ToastBroadUtil.show(getString(R.string.请输入密码));
                    return;
                }
                if (!cbAgree.isChecked()) {
                    ToastBroadUtil.show(getString(R.string.请先同意用户协议和隐私协议));
                    return;
                }
                zhLogin();
            } else if (loginType.equals("3")) {
                if (StringBroadUtil.isEmpty(etPhone.getText().toString())) {
                    ToastBroadUtil.show(getString(R.string.请输入手机号));
                    return;
                }
                if (StringBroadUtil.isEmpty(etPassword.getText().toString())) {
                    ToastBroadUtil.show(getString(R.string.请输入密码));
                    return;
                }
                if (!cbAgree.isChecked()) {
                    ToastBroadUtil.show(getString(R.string.请先同意用户协议和隐私协议));
                    return;
                }
                pwdLogin();
            }
        }
        if (view.getId() ==R.id.tvDiqu){
            intent = new Intent(this, RegionBroadActivity.class);
            startActivityForResult(intent, 333);
        }
        if (view.getId() ==R.id.tvXy){
            JumpBroadUtils.Companion.startH5Activity(this, BroadUrl.YHXY, getString(R.string.用户协议));
        }
        if (view.getId() == R.id.tvYs){
            JumpBroadUtils.Companion.startH5Activity(this, BroadUrl.YSXY, getString(R.string.隐私协议));
        }
        if (view.getId() ==R.id.imWangluo){
            intent = new Intent(this, NetWorkBroadActivity.class);
            startActivity(intent);
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = false)
    public void senmessage(EventBusBroadBean eventBusBean) {//登录成功
        if (eventBusBean.msg.equals("登录成功")) {
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 333 && resultCode == 333) {
            tvDiqu.setText("+" + data.getStringExtra("no"));
        }
    }

    /**
     * 账号密码登陆
     */
    private void zhLogin() {
        Map<String, Object> param = new HashMap<>();
        param.put("acount", etUser.getText().toString());
        param.put("pwd", etPassword.getText().toString());
        param.put("qu", tvDiqu.getText().toString().replace("+", ""));
        param.put("eqid", Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID));
        param.put("clientId", ChatManagerBroadHolder.gChatManager.getClientId());
        param.put("type", "2");
        OkHttpBroadHelper.getInstance().post_json(this, BroadUrl.zhLogin, param, new SpotsCallBroadBack<ResultBroadBean>(this) {
            @Override
            public void onSuccess(Response response, ResultBroadBean resultBean) {
                if (resultBean.isws.equals("0")) {//是否已完善资料
                    Intent intent = new Intent(LoginBroadActivity.this, PushInfoBroadActivity.class);
                    intent.putExtra("apptoken", resultBean.apptoken);
                    intent.putExtra("uid", resultBean.userId);
                    intent.putExtra("userSign", resultBean.userSign);
                    intent.putExtra("imtoken", resultBean.imtoken);
                    startActivity(intent);
                } else {
                    DataListBroadBean dataListBean = new DataListBroadBean();
                    dataListBean.zhanghao = etUser.getText().toString();
                    dataListBean.mima = etPassword.getText().toString();
                    boolean isChecked = false;
                    for (int i = 0; i < zhanghaoList.size(); i++) {
                        if (zhanghaoList.get(i).zhanghao.equals(etUser.getText().toString())) {
                            isChecked = true;
                            zhanghaoList.get(i).zhanghao = etUser.getText().toString();
                            zhanghaoList.get(i).mima = etPassword.getText().toString();
                        }
                    }
                    if (!isChecked) {
                        zhanghaoList.add(dataListBean);
                    }
                    SharePrefBroadBroadUtil.saveString(LoginBroadActivity.this, AppBroadConsts.zhanghaoList, new Gson().toJson(zhanghaoList));

                    AppBroadUtils.Companion.imLogin(LoginBroadActivity.this, resultBean.apptoken, resultBean.userSign, resultBean.userId, resultBean.imtoken);
                }
            }

            @Override
            public void onError(Response response, int code, Exception e) {
            }
        });
    }

    /**
     * 手机号密码登陆
     */
    private void pwdLogin() {
        Map<String, Object> param = new HashMap<>();
        param.put("phone", etPhone.getText().toString());
        param.put("qu", etPhone.getText().toString());
        param.put("pwd", etPassword.getText().toString());
        param.put("clientId", ChatManagerBroadHolder.gChatManager.getClientId());
        param.put("type", "2");
        param.put("eqid", Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID));
        OkHttpBroadHelper.getInstance().post_json(this, BroadUrl.pwdLogin, param, new SpotsCallBroadBack<ResultBroadBean>(this) {
            @Override
            public void onSuccess(Response response, ResultBroadBean resultBean) {
                if (resultBean.isws.equals("0")) {//是否已完善资料
                    Intent intent = new Intent(LoginBroadActivity.this, PushInfoBroadActivity.class);
                    intent.putExtra("apptoken", resultBean.apptoken);
                    intent.putExtra("uid", resultBean.userId);
                    intent.putExtra("userSign", resultBean.userSign);
                    intent.putExtra("imtoken", resultBean.imtoken);
                    startActivity(intent);
                } else {
                    AppBroadUtils.Companion.imLogin(LoginBroadActivity.this, resultBean.apptoken, resultBean.userSign, resultBean.userId, resultBean.imtoken);

                }
            }

            @Override
            public void onError(Response response, int code, Exception e) {
            }
        });
    }

    /**
     * 平台参数
     */
    private void ptConfig() {
        Map<String, Object> param = new HashMap<>();
        OkHttpBroadHelper.getInstance().post_json(this, BroadUrl.ptConfig, param, new BaseBroadCallback<ResultBroadBean>() {
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
                bean = resultBean;
                if ("1".equals(resultBean.data.regType)) {
                    if (!TextUtils.isEmpty(resultBean.data.regImg)) {
                        setImageview();
                    }
                } else if ("2".equals(resultBean.data.regType)) {
                    if (!TextUtils.isEmpty(resultBean.data.regImg)) {
                        setvideo();
                    }
                }
            }

            @Override
            public void onError(Response response, int code, Exception e) {
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        videoplayer.onVideoPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (videoplayer.getCurrentState() == CURRENT_STATE_PAUSE) {
            videoplayer.onVideoResume(false);
        } else {
            videoplayer.startPlayLogic();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        videoplayer.release();
        EventBus.getDefault().unregister(this);
    }

    private void initViews() {
        ivRegisterBg = findViewById(R.id.ivRegisterBg);
        videoplayer = findViewById(R.id.videoplayer);
        thumbImage = findViewById(R.id.thumbImage);
        videoLayout = findViewById(R.id.videoLayout);
        viHead = findViewById(R.id.viHead);
        tvLanguage = findViewById(R.id.tvLanguage);
        imWangluo = findViewById(R.id.imWangluo);
        imUser = findViewById(R.id.imUser);
        tvUser = findViewById(R.id.tvUser);
        rlUser = findViewById(R.id.rlUser);
        imPhone = findViewById(R.id.imPhone);
        tvPhone = findViewById(R.id.tvPhone);
        rlPhone = findViewById(R.id.rlPhone);
        etUser = findViewById(R.id.etUser);
        llUser = findViewById(R.id.llUser);
        tvDiqu = findViewById(R.id.tvDiqu);
        etPhone = findViewById(R.id.etPhone);
        llPhone = findViewById(R.id.llPhone);
        etPassword = findViewById(R.id.etPassword);
        imEyes = findViewById(R.id.imEyes);
        tvZhaohui = findViewById(R.id.tvZhaohui);
        tvZhuce = findViewById(R.id.tvZhuce);
        tvLogin = findViewById(R.id.tvLogin);
        cbAgree = findViewById(R.id.cbAgree);
        tvXy = findViewById(R.id.tvXy);
        tvYs = findViewById(R.id.tvYs);
        imVpn = findViewById(R.id.imVpn);
        imKefu = findViewById(R.id.imKefu);
    }
}