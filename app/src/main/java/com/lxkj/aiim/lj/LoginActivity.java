package com.lxkj.aiim.lj;

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
import com.lxkj.aiim.R;
import com.lxkj.aiim.base.BaseSimpleActivity;
import com.lxkj.aiim.lj.bean.DataListBean;
import com.lxkj.aiim.lj.bean.EventBusBean;
import com.lxkj.aiim.lj.bean.ResultBean;
import com.lxkj.aiim.lj.configuration.AppConsts;
import com.lxkj.aiim.lj.http.BaseCallback;
import com.lxkj.aiim.lj.http.OkHttpHelper;
import com.lxkj.aiim.lj.http.SpotsCallBack;
import com.lxkj.aiim.lj.http.Url;
import com.lxkj.aiim.lj.utils.ListUtil;
import com.lxkj.aiim.lj.utils.SharePrefUtil;
import com.lxkj.aiim.lj.utils.StringUtil;
import com.lxkj.aiim.lj.utils.ToastUtil;
import com.lxkj.aiim.utils.AppUtil;
import com.lxkj.aiim.utils.GlideLoaderHelper;
import com.lxkj.aiim.utils.JumpUtils;
import com.cboda.baselibrary.utils.MMKVUtils;
import com.lxkj.aiim.widge.LiXinStandardGSYVideoPlayer;
import com.shuyu.gsyvideoplayer.utils.GSYVideoType;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.wildfire.chat.kit.ChatManagerHolder;
import okhttp3.Request;
import okhttp3.Response;

public class LoginActivity extends BaseSimpleActivity implements View.OnClickListener {


    private String loginType;
    private ResultBean bean;
    private List<DataListBean> zhanghaoList = new ArrayList<>();
    private ImageView ivRegisterBg;
    private LiXinStandardGSYVideoPlayer videoplayer;
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
        return R.layout.activity_login3;
    }

    private void setImageview() {
        ivRegisterBg.setVisibility(View.VISIBLE);
        GlideLoaderHelper.Companion.loadUrl(
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

        tvLanguage.setText(MMKVUtils.Companion.getYuYan());

        EventBus.getDefault().register(this);

        zhanghaoList.clear();
        if (!StringUtil.isEmpty(SharePrefUtil.getString(this, AppConsts.zhanghaoList, ""))) {
            // 定义类型
            Type userListType = new TypeToken<List<DataListBean>>() {
            }.getType();
            List<DataListBean> listBeans = (List<DataListBean>) new Gson().fromJson(SharePrefUtil.getString(this, AppConsts.zhanghaoList, ""), userListType);
            zhanghaoList.addAll(listBeans);
        }


        switch (SharePrefUtil.getString(this, AppConsts.LOGINTYPE, "4")) {
            case "2"://用户名密码登录

                loginType = "2";

                rlUser.setVisibility(View.VISIBLE);
                tvUser.setTextColor(Color.parseColor("#222222"));
                imUser.setVisibility(View.GONE);
                rlPhone.setVisibility(View.GONE);

                llUser.setVisibility(View.VISIBLE);
                llPhone.setVisibility(View.GONE);

                if (!ListUtil.isEmpty(zhanghaoList)) {
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
                                    }, R.layout.popup_recycle, R.layout.custom_part_shadow_popup)
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

                if (!ListUtil.isEmpty(zhanghaoList)) {
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
                                    }, R.layout.popup_recycle, R.layout.custom_part_shadow_popup)
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
                if (StringUtil.isEmpty(editable.toString())) {
                    if (!ListUtil.isEmpty(zhanghaoList)) {
                        String[] items = new String[zhanghaoList.size()];
                        for (int i = 0; i < zhanghaoList.size(); i++) {
                            items[i] = zhanghaoList.get(i).zhanghao;
                        }
                        new XPopup.Builder(LoginActivity.this)
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
                                        }, R.layout.popup_recycle, R.layout.custom_part_shadow_popup)
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
            intent = new Intent(this, LanguageActivity.class);
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
                imEyes.setImageResource(R.mipmap.ic_biyan);
            } else {
                etPassword.setTransformationMethod(null);
                imEyes.setImageResource(R.mipmap.ic_zhengyan);
            }
            etPassword.setSelection(etPassword.getText().toString().length());
        }

        if (view.getId() == R.id.tvZhaohui){
            intent = new Intent(this, RetrieveActivity.class);
            startActivity(intent);
        }
        if (view.getId() == R.id.tvZhuce){
            intent = new Intent(this, RegisterActivity.class);
            intent.putExtra("loginType", loginType);
            startActivity(intent);
        }

        if (view.getId() == R.id.tvLogin){
            if (loginType.equals("2")) {
                if (StringUtil.isEmpty(etUser.getText().toString())) {
                    ToastUtil.show(getString(R.string.请输入用户名));
                    return;
                }
                if (StringUtil.isEmpty(etPassword.getText().toString())) {
                    ToastUtil.show(getString(R.string.请输入密码));
                    return;
                }
                if (!cbAgree.isChecked()) {
                    ToastUtil.show(getString(R.string.请先同意用户协议和隐私协议));
                    return;
                }
                zhLogin();
            } else if (loginType.equals("3")) {
                if (StringUtil.isEmpty(etPhone.getText().toString())) {
                    ToastUtil.show(getString(R.string.请输入手机号));
                    return;
                }
                if (StringUtil.isEmpty(etPassword.getText().toString())) {
                    ToastUtil.show(getString(R.string.请输入密码));
                    return;
                }
                if (!cbAgree.isChecked()) {
                    ToastUtil.show(getString(R.string.请先同意用户协议和隐私协议));
                    return;
                }
                pwdLogin();
            }
        }
        if (view.getId() ==R.id.tvDiqu){
            intent = new Intent(this, RegionActivity.class);
            startActivityForResult(intent, 333);
        }
        if (view.getId() ==R.id.tvXy){
            JumpUtils.Companion.startH5Activity(this, Url.YHXY, getString(R.string.用户协议));
        }
        if (view.getId() == R.id.tvYs){
            JumpUtils.Companion.startH5Activity(this, Url.YSXY, getString(R.string.隐私协议));
        }
        if (view.getId() ==R.id.imWangluo){
            intent = new Intent(this, NetWorkActivity.class);
            startActivity(intent);
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = false)
    public void senmessage(EventBusBean eventBusBean) {//登录成功
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
        param.put("clientId", ChatManagerHolder.gChatManager.getClientId());
        param.put("type", "2");
        OkHttpHelper.getInstance().post_json(this, Url.zhLogin, param, new SpotsCallBack<ResultBean>(this) {
            @Override
            public void onSuccess(Response response, ResultBean resultBean) {
                if (resultBean.isws.equals("0")) {//是否已完善资料
                    Intent intent = new Intent(LoginActivity.this, PushInfoActivity.class);
                    intent.putExtra("apptoken", resultBean.apptoken);
                    intent.putExtra("uid", resultBean.userId);
                    intent.putExtra("userSign", resultBean.userSign);
                    intent.putExtra("imtoken", resultBean.imtoken);
                    startActivity(intent);
                } else {
                    DataListBean dataListBean = new DataListBean();
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
                    SharePrefUtil.saveString(LoginActivity.this, AppConsts.zhanghaoList, new Gson().toJson(zhanghaoList));

                    AppUtil.Companion.imLogin(LoginActivity.this, resultBean.apptoken, resultBean.userSign, resultBean.userId, resultBean.imtoken);
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
        param.put("clientId", ChatManagerHolder.gChatManager.getClientId());
        param.put("type", "2");
        param.put("eqid", Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID));
        OkHttpHelper.getInstance().post_json(this, Url.pwdLogin, param, new SpotsCallBack<ResultBean>(this) {
            @Override
            public void onSuccess(Response response, ResultBean resultBean) {
                if (resultBean.isws.equals("0")) {//是否已完善资料
                    Intent intent = new Intent(LoginActivity.this, PushInfoActivity.class);
                    intent.putExtra("apptoken", resultBean.apptoken);
                    intent.putExtra("uid", resultBean.userId);
                    intent.putExtra("userSign", resultBean.userSign);
                    intent.putExtra("imtoken", resultBean.imtoken);
                    startActivity(intent);
                } else {
                    AppUtil.Companion.imLogin(LoginActivity.this, resultBean.apptoken, resultBean.userSign, resultBean.userId, resultBean.imtoken);

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
        OkHttpHelper.getInstance().post_json(this, Url.ptConfig, param, new BaseCallback<ResultBean>() {
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
            public void onSuccess(Response response, ResultBean resultBean) {
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