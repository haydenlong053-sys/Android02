package com.cboda.intelligent.activity;

import static com.cboda.baselibrary.utils.ELogUtilKt.getIsDebug;
import static com.cboda.baselibrary.utils.ViewExpandUtilKt.showCommonTip;
import static com.cboda.intelligent.utils.ViewExpandUtilKt.showToastMain;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.lifecycle.ViewModelProvider;

import com.cboda.baselibrary.utils.ELogUtilKt;
import com.cboda.intelligent.R;
import com.cboda.intelligent.base.BaseSimple2BroadActivity;
import com.cboda.intelligent.compose.navgraph.WTRoute;
import com.cboda.baselibrary.popup.CommonTipBroadPop;
import com.cboda.intelligent.bean.ResultBroadBean;
import com.cboda.intelligent.dialog.NormalBroadDialog;
import com.cboda.intelligent.utils.BaseBroadCallback;
import com.cboda.intelligent.utils.OkHttpBroadHelper;
import com.cboda.intelligent.net.BroadUrl;
import com.cboda.intelligent.utils.StringBroadUtil;
import com.cboda.intelligent.utils.StringBroadUtils;
import com.cboda.intelligent.viewmodel.WTViewModel;

import java.util.HashMap;
import java.util.Map;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;
import okhttp3.Request;
import okhttp3.Response;

public class QianbaoBroadActivity extends BaseSimple2BroadActivity implements View.OnClickListener {


    private TextView tvMoney;
    private TextView mTvPoint;
    private LinearLayout llChongzhi;
    private LinearLayout llTixian;
    private LinearLayout llMingxi;
    private LinearLayout llShezhi;
    private LinearLayout mLlTransfer;
    private TextView mTvSysCurrency;
    private LinearLayout llWallet;
    private View walletView;
    private boolean isHasSecurity = false;
    private boolean isGoogleAuth = false;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_qi_broad_an_bao;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();
        if (getIsDebug()) {
            llWallet.setVisibility(View.VISIBLE);
            walletView.setVisibility(View.VISIBLE);
        } else {
            llWallet.setVisibility(View.GONE);
            walletView.setVisibility(View.GONE);
        }
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
        llWallet.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        Intent intent;
        if (view.getId() == R.id.imFinish) {
            finish();
        }
        if (view.getId() == R.id.llChongzhi) {
            intent = new Intent(this, RechargeBroadActivity.class);
            startActivity(intent);

        }
        if (view.getId() == R.id.llTixian) {
            if (isGoogleAuth || isHasSecurity) {
                intent = new Intent(this, TransferBroadActivity.class);
                intent.putExtra("type", 1);
                startActivity(intent);
            } else {
                showGuideTip();
            }

        }
        if (view.getId() == R.id.llMingxi) {
            intent = new Intent(this, BillBroadActivity.class);
            startActivity(intent);
        }
        if (view.getId() == R.id.llShezhi) {
            intent = new Intent(this, PaySettingBroadActivity.class);
            startActivity(intent);

        }
        if (view.getId() == R.id.mLlTransfer) {
            if (isGoogleAuth || isHasSecurity) {
                intent = new Intent(this, TransferBroadActivity.class);
                intent.putExtra("type", 0);
                startActivity(intent);

            } else {
                showGuideTip();
            }

        }
        if (view.getId() == R.id.ll_wallet) {
//            NetworkManager.INSTANCE.updateConfig("http://47.118.18.244:8080/", NetworkConfig.INSTANCE.getConnectTimeout(), NetworkConfig.INSTANCE.getReadTimeout(), NetworkConfig.INSTANCE.getWriteTimeout(),true);
            WTViewModel viewModel = new ViewModelProvider(this).get(WTViewModel.class);
            viewModel.checkWalletList(97, new WTViewModel.CheckWalletCallback() {
                @Override
                public void onSuccess(boolean isEmpty) {
                    ELogUtilKt.eLogD("列表", "isEmpty" + isEmpty);
                    if (isEmpty) {
                        Intent intent = new Intent(QianbaoBroadActivity.this, WTEmptyActivity.class);
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent(QianbaoBroadActivity.this, WalletActivity.class);
                        intent.putExtra("start_route", WTRoute.ASSET_WT);
                        startActivity(intent);
                    }
                }

                @Override
                public void onError(String msg) {
                    showToastMain(getString(R.string.httperror));
                }
            });
        }


    }

    //0是转账，1是提现
    private void showGuideTip() {
        showCommonTip(R.string.mine_tip_other_title, R.string.mine_tip_other_des, R.string.mine_tip_wait, R.string.mine_ok, this, aBoolean -> {
            if (aBoolean) {
                Intent intent = new Intent(getBaseContext(), SafeCenterBroadActivity.class);
                startActivity(intent);
            }
            return null;
        });
    }

    /**
     * 用户信息
     */
    private void userInfo() {
        Map<String, Object> param = new HashMap<>();
        OkHttpBroadHelper.getInstance().post_json(this, BroadUrl.userInfo, param, new BaseBroadCallback<ResultBroadBean>() {
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
                tvMoney.setText(StringBroadUtils.removeDecimalPart(resultBean.data.balalce));
                mTvPoint.setText(resultBean.data.pointTotal);
                if (resultBean.data.hasPaypwd.equals("0")) {
                    new NormalBroadDialog(QianbaoBroadActivity.this, getString(R.string.温馨提示), getString(R.string.未设置支付密码), getString(R.string.取消), getString(R.string.前去设置), false)
                            .setOnButtonClickListener(new NormalBroadDialog.OnButtonClick() {
                                @Override
                                public void OnRightClick() {
                                    if (StringBroadUtil.isEmpty(resultBean.data.mb)) {
                                        ptConfig();
                                    } else {
//                                        Intent intent = new Intent(QianbaoBroadActivity.this, PaySettingBroadActivity.class);
//                                        startActivity(intent);
                                        Intent intent = new Intent(QianbaoBroadActivity.this, SetPayPasswordBroadActivity.class);
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
                if (resultBean.data.openMibao.equals("1")) {
                    Intent intent = new Intent(QianbaoBroadActivity.this, MiBaoBroadActivity.class);
                    intent.putExtra("type", "0");
                    startActivity(intent);
                } else {
//                    Intent intent = new Intent(QianbaoBroadActivity.this, PaySettingBroadActivity.class);
//                    startActivity(intent);
                    Intent intent = new Intent(QianbaoBroadActivity.this, SetPayPasswordBroadActivity.class);
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
        llWallet = findViewById(R.id.ll_wallet);
        walletView = findViewById(R.id.walletView);
    }
}