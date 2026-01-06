package com.lxkj.aiim.lj;

import static com.cboda.baselibrary.utils.ELogUtilKt.getIsDebug;
import static com.lxkj.aiim.lj.utils.ViewExpandUtilKt.showGuidePasswordTip;
import static com.lxkj.aiim.lj.utils.ViewExpandUtilKt.showToastMain;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cboda.baselibrary.utils.HttpsBroadConfig;
import com.cboda.intelligent.R;
import com.lxkj.aiim.base.BaseSimpleActivity;
import com.lxkj.aiim.dialog.pop.AuthWayPop;
import com.lxkj.aiim.lj.bean.ResultBean;
import com.lxkj.aiim.lj.configuration.SafeJumpEnum;
import com.lxkj.aiim.lj.event.ActionRefreshEvent;
import com.lxkj.aiim.lj.http.BaseCallback;
import com.lxkj.aiim.lj.http.OkHttpHelper;
import com.lxkj.aiim.lj.http.Url;
import com.lxkj.aiim.lj.utils.StringUtil;
import com.lxkj.aiim.utils.AppUtil;
import com.lxkj.aiim.utils.GlideCatchUtil;
import com.lxkj.aiim.utils.JumpUtils;
import com.cboda.baselibrary.utils.MMKVBroadUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;
import java.util.Map;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import okhttp3.Request;
import okhttp3.Response;

public class UserSetActivity extends BaseSimpleActivity implements View.OnClickListener {

    private String eqno;
    private ResultBean bean;
    private ImageView imPengyouquan;
    //    private LinearLayout llCicle;
    private LinearLayout llUserInfo;
    private LinearLayout llZhanghu;
    private LinearLayout llTongyong;
    private LinearLayout llLianxi;
    private LinearLayout llGuanyu;
    private LinearLayout llYonghu;
    private LinearLayout llThird;
    private LinearLayout llYinsi;
    private LinearLayout llIdea;
    private LinearLayout llWangluo;
    private TextView tvhuancun, tvSwitch;
    private LinearLayout llhuancun;
    private LinearLayout llYuyan;
    private LinearLayout llFanYi;
    private TextView tvZx;
    private TextView tvLogout;
    private View llBindMachineNo;
    private boolean isHasPassword = false;

    @SuppressLint({"SetTextI18n", "HardwareIds"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();
        getToolbarTitle().setText(getResources().getString(R.string.设置));
        tvSwitch.setVisibility(getIsDebug() ? View.VISIBLE : View.GONE);
        tvSwitch.setText("切换环境（" + HttpsBroadConfig.INSTANCE.getDevType() + "）");
        tvSwitch.setOnClickListener(this);
        llUserInfo.setOnClickListener(this);
        EventBus.getDefault().register(this);
        eqno = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        llZhanghu.setOnClickListener(this);
        llTongyong.setOnClickListener(this);
        llGuanyu.setOnClickListener(this);
        llIdea.setOnClickListener(this);
        llWangluo.setOnClickListener(this);
        llYuyan.setOnClickListener(this);
        tvLogout.setOnClickListener(this);
        imPengyouquan.setOnClickListener(this);
        llYonghu.setOnClickListener(this);
        llThird.setOnClickListener(this);
        llYinsi.setOnClickListener(this);
        llLianxi.setOnClickListener(this);
        llFanYi.setOnClickListener(this);
        tvZx.setOnClickListener(this);
        llBindMachineNo.setOnClickListener(this);
        llhuancun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLoading();
                llhuancun.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        GlideCatchUtil.getInstance().cleanCatchDisk();
                        dismissLoading();
                        tvhuancun.setText("0M");
                    }
                }, 1500);
            }
        });
        userInfo();

        String cacheSize = GlideCatchUtil.getInstance().getCacheSize();
        AppUtil.Companion.debug("缓存", "--> $cacheSize");
        tvhuancun.setText(cacheSize);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_user_set;
    }

    @Override
    public void onClick(View view) {
        Intent intent;
        if (view.getId() == R.id.imPengyouquan) {
            if (bean.data.showQuan.equals("0")) {
                editUserinfo("1");
            } else {
                editUserinfo("0");
            }
        } else if (view.getId() == R.id.llUserInfo) {
            intent = new Intent(this, UserInfoActivity.class);
            startActivity(intent);

        } else if (view.getId() == R.id.llZhanghu) {
            //                intent = new Intent(this, AccountActivity.class);
            intent = new Intent(this, ZhaqActivity.class);
            startActivity(intent);
        } else if (view.getId() == R.id.llTongyong) {
            intent = new Intent(this, CommonSetActivity.class);
            startActivity(intent);
        } else if (view.getId() == R.id.llGuanyu) {
            JumpUtils.Companion.startH5Activity(this, Url.GYWM, getString(R.string.关于我们));
        } else if (view.getId() == R.id.llYonghu) {
            JumpUtils.Companion.startH5Activity(this, Url.YHXY, getString(R.string.用户协议));
        } else if (view.getId() == R.id.llYinsi) {
            JumpUtils.Companion.startH5Activity(this, Url.YSXY, getString(R.string.隐私协议));
        } else if (view.getId() == R.id.llThird) {
            JumpUtils.Companion.startH5Activity(this, Url.thirdContent, getString(R.string.第三方信息共享清单));
        } else if (view.getId() == R.id.llIdea) {
            intent = new Intent(this, FeedbackActivity.class);
            startActivity(intent);
        } else if (view.getId() == R.id.llWangluo) {
            intent = new Intent(this, NetWorkActivity.class);
            startActivity(intent);
        } else if (view.getId() == R.id.llYuyan) {
            intent = new Intent(this, LanguageActivity.class);
            startActivity(intent);
        } else if (view.getId() == R.id.llFanYi) {
            intent = new Intent(this, FanYiActivity.class);
            startActivity(intent);
        } else if (view.getId() == R.id.tvLogout) {
            AppUtil.Companion.exit(UserSetActivity.this);
        } else if (view.getId() == R.id.llLianxi) {
            AppUtil.Companion.goChat(UserSetActivity.this, MMKVBroadUtils.Companion.getKeFuId(), "");
        } else if (view.getId() == R.id.tvZx) {
            intent = new Intent(this, ZxzhActivity.class);
            startActivity(intent);
        } else if (view.getId() == R.id.llBindMachineNo) {
            if (!isHasPassword) {
                showGuidePasswordTip(this);
            } else {
                changBindMachine();
            }
        } else if (view.getId() == R.id.tvSwitch) {
            HttpsBroadConfig.INSTANCE.showSwitchDialog();
        }
    }

    private void changBindMachine() {
        AuthWayPop authWayPop = new AuthWayPop(this, SafeJumpEnum.ChangeMachineAuth.getSourceType(), false, null, null);
        authWayPop.transferInvoke = new Function0<Unit>() {
            @Override
            public Unit invoke() {
                goBindMachine();
                return null;
            }
        };
        authWayPop.showPopupWindow();
    }

    private void goBindMachine() {
        Map<String, Object> param = new HashMap<>();
//        param.put("showQuan", showQuan);
        OkHttpHelper.getInstance().post_json(this, Url.DEVICE_GET_AUTH, param, new BaseCallback<ResultBean>() {
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
                if (resultBean.data != null && !TextUtils.isEmpty(resultBean.data.authCode)) {
                    goBindMachineNext(resultBean.data.authCode);
                }

            }

            @Override
            public void onError(Response response, int code, Exception e) {
            }
        });
    }

    private void goBindMachineNext(String authCode) {
        Map<String, Object> param = new HashMap<>();
        param.put("authCode", authCode);
        param.put("eqno", eqno);
        OkHttpHelper.getInstance().post_json(this, Url.DEVICE_BIND, param, new BaseCallback<ResultBean>() {
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
                showToastMain(getString(R.string.mine_bind_success));
                llBindMachineNo.setVisibility(View.GONE);
                AppUtil.Companion.exit(UserSetActivity.this);

            }

            @Override
            public void onError(Response response, int code, Exception e) {
            }
        });
    }

    /**
     * 修改资料
     */
    private void editUserinfo(String showQuan) {
        Map<String, Object> param = new HashMap<>();
        param.put("showQuan", showQuan);
        OkHttpHelper.getInstance().post_json(this, Url.editUserinfo, param, new BaseCallback<ResultBean>() {
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
                userInfo();
            }

            @Override
            public void onError(Response response, int code, Exception e) {
            }
        });
    }

    /**
     * 用户信息
     */
    private void userInfo() {
        Map<String, Object> param = new HashMap<>();
        OkHttpHelper.getInstance().post_json(this, Url.userInfo, param, new BaseCallback<ResultBean>() {
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
                if (StringUtil.isEmpty(bean.data.showQuan)) {
                    bean.data.showQuan = "1";
                }
                isHasPassword = !StringUtil.isEmpty(bean.data.paypwd);
                if (!TextUtils.isEmpty(bean.data.eqno) && TextUtils.equals(bean.data.eqno, eqno)) {
                    llBindMachineNo.setVisibility(View.GONE);
                } else {
                    llBindMachineNo.setVisibility(View.VISIBLE);
                }


                if (resultBean.data.showQuan.equals("0")) {
                    imPengyouquan.setImageResource(R.mipmap.ic_guan);
                } else {
                    imPengyouquan.setImageResource(R.mipmap.ic_kai);
                }

                if (bean.data.isvip.equals("1"))
                    llFanYi.setVisibility(View.VISIBLE);
                else
                    llFanYi.setVisibility(View.GONE);
            }

            @Override
            public void onError(Response response, int code, Exception e) {
            }
        });
    }

    private void initViews() {
        imPengyouquan = findViewById(R.id.imPengyouquan);
//        llCicle = findViewById(R.id.llCicle);
        llUserInfo = findViewById(R.id.llUserInfo);
        llZhanghu = findViewById(R.id.llZhanghu);
        llTongyong = findViewById(R.id.llTongyong);
        llLianxi = findViewById(R.id.llLianxi);
        llGuanyu = findViewById(R.id.llGuanyu);
        llYonghu = findViewById(R.id.llYonghu);
        llThird = findViewById(R.id.llThird);
        llYinsi = findViewById(R.id.llYinsi);
        llIdea = findViewById(R.id.llIdea);
        llWangluo = findViewById(R.id.llWangluo);
        tvhuancun = findViewById(R.id.tvhuancun);
        llhuancun = findViewById(R.id.llhuancun);
        llYuyan = findViewById(R.id.llYuyan);
        llFanYi = findViewById(R.id.llFanYi);
        tvZx = findViewById(R.id.tvZx);
        tvLogout = findViewById(R.id.tvLogout);
        llBindMachineNo = findViewById(R.id.llBindMachineNo);
        tvSwitch = findViewById(R.id.tvSwitch);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAppEventReceiver(ActionRefreshEvent event) {
        userInfo();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}