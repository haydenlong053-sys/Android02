package com.cboda.intelligent.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatImageView;

import com.cboda.baselibrary.utils.HttpsBroadConfig;
import com.cboda.intelligent.R;
import com.cboda.intelligent.base.BaseSimpleBroadActivity;
import com.cboda.intelligent.dialog.InputJhmBroadDialog;
import com.cboda.intelligent.bean.ResultBroadBean;
import com.cboda.intelligent.utils.OkHttpBroadHelper;
import com.cboda.intelligent.utils.SpotsCallBroadBack;
import com.cboda.intelligent.net.BroadUrl;
import com.cboda.intelligent.utils.GlideBroadUtil;
import com.cboda.intelligent.utils.JumpBroadUtils;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Response;

public class HyzxBroadActivity extends BaseSimpleBroadActivity implements View.OnClickListener {


    private ResultBroadBean bean;
    private View viHead;
    private AppCompatImageView imFinish;
    private RoundedImageView ivIcon;
    private TextView tvName;
    private TextView tvVipState;
    private TextView tvVipTime;
    private TextView tvTq;
    private TextView tvKt;
    private TextView tvDh;

    @Override
    public boolean showOrHideTitleLayout() {
        return false;
    }

    @Override
    protected boolean transparentStatus() {
        return true;
    }

    @Override
    protected boolean fitsSystemWindows() {
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();
        if (HttpsBroadConfig.isReviewEnv) {
            tvDh.setVisibility(View.GONE);
        } else {
            tvDh.setVisibility(View.VISIBLE);
        }
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height",
                "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        LinearLayout.LayoutParams linearParams = (LinearLayout.LayoutParams) viHead.getLayoutParams(); //取控件textView当前的布局参数
        linearParams.height = result;// 控件的高强制设成20
        viHead.setLayoutParams(linearParams);
        imFinish.setOnClickListener(this);
        tvDh.setOnClickListener(this);
        tvKt.setOnClickListener(this);
        userInfo();
        ptConfig();
    }


    @Override
    protected int getLayoutId() {
        return R.layout.activity_hy_broad_zx;
    }

    @Override
    public void onClick(View view) {
        Intent intent;
        if (view.getId() == R.id.imFinish) {
            finish();
        }
        if (view.getId() == R.id.tvDh) {
            new InputJhmBroadDialog(this, new InputJhmBroadDialog.OnInputListener() {
                @Override
                public void onInput(String num) {
                    duivip(num);
                }
            }).show();
        }
        if (view.getId() == R.id.tvKt) {
            JumpBroadUtils.Companion.startCreateQunActivity(this);
        }
    }


    private void ptConfig() {
        Map<String, Object> param = new HashMap<>();
        OkHttpBroadHelper.getInstance().post_json(this, BroadUrl.ptConfig, param, new SpotsCallBroadBack<ResultBroadBean>(this) {
            @Override
            public void onSuccess(Response response, ResultBroadBean resultBean) {
                tvTq.setText(resultBean.data.vipinfo);
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
        OkHttpBroadHelper.getInstance().post_json(this, BroadUrl.userInfo, param, new SpotsCallBroadBack<ResultBroadBean>(this) {
            @Override
            public void onSuccess(Response response, ResultBroadBean resultBean) {
                bean = resultBean;
                tvName.setText(bean.data.name);
                GlideBroadUtil.setImag(HyzxBroadActivity.this, bean.data.icon, ivIcon);
                if (null != bean.data.isvip && bean.data.isvip.equals("1") && !HttpsBroadConfig.isReviewEnv) {
                    tvVipState.setVisibility(View.GONE);
                    tvVipTime.setVisibility(View.VISIBLE);
                    tvVipTime.setText(bean.data.vipTime + getString(R.string.到期));
                    tvKt.setVisibility(View.VISIBLE);
                } else {
                    tvVipState.setVisibility(View.VISIBLE);
                    tvVipTime.setVisibility(View.GONE);
                    tvKt.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(Response response, int code, Exception e) {
            }
        });
    }


    /**
     * 兑换会员
     *
     * @param code
     */
    private void duivip(String code) {
        Map<String, Object> param = new HashMap<>();
        param.put("code", code);
        OkHttpBroadHelper.getInstance().post_json(this, BroadUrl.duivip, param, new SpotsCallBroadBack<ResultBroadBean>(this) {
            @Override
            public void onSuccess(Response response, ResultBroadBean resultBean) {
                userInfo();
            }

            @Override
            public void onError(Response response, int code, Exception e) {
            }
        });
    }

    private void initViews() {
        viHead = findViewById(R.id.viHead);
        imFinish = findViewById(R.id.imFinish);
        ivIcon = findViewById(R.id.ivIcon);
        tvName = findViewById(R.id.tvName);
        tvVipState = findViewById(R.id.tvVipState);
        tvVipTime = findViewById(R.id.tvVipTime);
        tvTq = findViewById(R.id.tvTq);
        tvKt = findViewById(R.id.tvKt);
        tvDh = findViewById(R.id.tvDh);
    }

    @Override
    protected boolean useStatusBarDarkFont() {
        return false;
    }
}