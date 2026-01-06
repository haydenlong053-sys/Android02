package com.lxkj.aiim.lj;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cboda.intelligent.R;
import com.lxkj.aiim.base.BaseSimple2Activity;
import com.lxkj.aiim.dialog.pop.CommonTipPop;
import com.lxkj.aiim.lj.bean.ResultBean;
import com.lxkj.aiim.lj.dialogfra.NormalDialog;
import com.lxkj.aiim.lj.http.BaseCallback;
import com.lxkj.aiim.lj.http.OkHttpHelper;
import com.lxkj.aiim.lj.http.Url;
import com.lxkj.aiim.lj.utils.StringUtil;
import com.lxkj.aiim.utils.StringUtils;

import java.util.HashMap;
import java.util.Map;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import okhttp3.Request;
import okhttp3.Response;

public class QianbaoActivity extends BaseSimple2Activity implements View.OnClickListener {


    private TextView tvMoney;
    private TextView mTvPoint;
    private LinearLayout llChongzhi;
    private LinearLayout llTixian;
    private LinearLayout llMingxi;
    private LinearLayout llShezhi;
    private LinearLayout mLlTransfer;
    private TextView mTvSysCurrency;
    private boolean isHasSecurity = false;
    private boolean isGoogleAuth = false;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_qianbao;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();
        mTvSysCurrency.setText(String.format(getString(R.string.mine_sys_currency), getString(R.string.webX_name)));
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height",
                "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
//        getToolbarTitle().setText(getResources().getString(R.string.我的钱包));
        llChongzhi.setOnClickListener(this);
        llTixian.setOnClickListener(this);
        llMingxi.setOnClickListener(this);
        llShezhi.setOnClickListener(this);
        mLlTransfer.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        Intent intent;
        if (view.getId() == R.id.imFinish) {
            finish();
        }
        if (view.getId() == R.id.llChongzhi) {
            intent = new Intent(this, RechargeActivity.class);
            startActivity(intent);

        }
        if (view.getId() == R.id.llTixian) {
            if (isGoogleAuth || isHasSecurity) {
                intent = new Intent(this, TransferActivity.class);
                intent.putExtra("type",1);
                startActivity(intent);
            } else {
               showGuideTip();
            }

        }
        if (view.getId() == R.id.llMingxi) {
            intent = new Intent(this, BillActivity.class);
            startActivity(intent);
        }
        if (view.getId() == R.id.llShezhi) {
            intent = new Intent(this, PaySettingActivity.class);
            startActivity(intent);

        }
        if (view.getId() == R.id.mLlTransfer) {
            if (isGoogleAuth || isHasSecurity) {
                intent = new Intent(this, TransferActivity.class);
                intent.putExtra("type",0);
                startActivity(intent);

            } else {
                showGuideTip();
            }

        }


    }

    //0是转账，1是提现
    private void showGuideTip() {
        CommonTipPop commonTipPop = new CommonTipPop(
                this,
                getString(R.string.mine_tip_other_title),
                getString(R.string.mine_tip_other_des)
        );
        commonTipPop.setCancelBtn(getString(R.string.mine_tip_wait));
        commonTipPop.setSureBtn(getString(R.string.mine_ok));
        commonTipPop.setSureInvoke(new Function0<Unit>() {
            @Override
            public Unit invoke() {
                Intent  intent = new Intent(getBaseContext(), SafeCenterActivity.class);
                startActivity(intent);
                return null;
            }
        });
        commonTipPop.showPopupWindow();
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
                tvMoney.setText(StringUtils.removeDecimalPart(resultBean.data.balalce));
                mTvPoint.setText(resultBean.data.pointTotal);
                if (resultBean.data.hasPaypwd.equals("0")) {
                    new NormalDialog(QianbaoActivity.this, getString(R.string.温馨提示), getString(R.string.未设置支付密码), getString(R.string.取消), getString(R.string.前去设置), false)
                            .setOnButtonClickListener(new NormalDialog.OnButtonClick() {
                                @Override
                                public void OnRightClick() {
                                    if (StringUtil.isEmpty(resultBean.data.mb)) {
                                        ptConfig();
                                    } else {
//                                        Intent intent = new Intent(QianbaoActivity.this, PaySettingActivity.class);
//                                        startActivity(intent);
                                        Intent intent = new Intent(QianbaoActivity.this, SetPayPasswordActivity.class);
                                        intent.putExtra("editIndex", "1");
                                        startActivity(intent);
                                    }
                                }

                                @Override
                                public void OnLeftClick() {
                                    finish();
                                }
                            }).show();
                }
                isHasSecurity = !TextUtils.isEmpty(resultBean.data.mbid);
                isGoogleAuth = !TextUtils.isEmpty(resultBean.data.twoFactorCode);
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
                if (resultBean.data.openMibao.equals("1")) {
                    Intent intent = new Intent(QianbaoActivity.this, MiBaoActivity.class);
                    intent.putExtra("type", "0");
                    startActivity(intent);
                } else {
//                    Intent intent = new Intent(QianbaoActivity.this, PaySettingActivity.class);
//                    startActivity(intent);
                    Intent intent = new Intent(QianbaoActivity.this, SetPayPasswordActivity.class);
                    intent.putExtra("editIndex", "1");
                    startActivity(intent);
                }
            }

            @Override
            public void onError(Response response, int code, Exception e) {
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        userInfo();
    }

    private void initViews() {
        tvMoney = findViewById(R.id.tvMoney);
        mTvPoint = findViewById(R.id.mTvPoint);
        llChongzhi = findViewById(R.id.llChongzhi);
        llTixian = findViewById(R.id.llTixian);
        llMingxi = findViewById(R.id.llMingxi);
        llShezhi = findViewById(R.id.llShezhi);
        mLlTransfer = findViewById(R.id.mLlTransfer);
        mTvSysCurrency = findViewById(R.id.mTvSysCurrency);
    }
}