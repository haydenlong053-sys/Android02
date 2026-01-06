package com.lxkj.aiim.lj;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatImageView;

import com.cboda.intelligent.R;
import com.lxkj.aiim.base.BaseSimpleActivity;
import com.lxkj.aiim.dialog.InputJhmDialog;
import com.lxkj.aiim.lj.bean.ResultBean;
import com.lxkj.aiim.lj.http.OkHttpHelper;
import com.lxkj.aiim.lj.http.SpotsCallBack;
import com.lxkj.aiim.lj.http.Url;
import com.lxkj.aiim.lj.utils.GlideUtil;
import com.lxkj.aiim.utils.JumpUtils;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Response;

public class HyzxActivity extends BaseSimpleActivity implements View.OnClickListener {


    private ResultBean bean;
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
        imFinish.setOnClickListener(this);
        tvDh.setOnClickListener(this);
        tvKt.setOnClickListener(this);
        userInfo();
        ptConfig();
    }


    @Override
    protected int getLayoutId() {
        return R.layout.activity_hyzx;
    }

    @Override
    public void onClick(View view) {
        Intent intent;
        if (view.getId()==R.id.imFinish){
            finish();
        }
        if (view.getId() == R.id.tvDh){
            new InputJhmDialog(this, new InputJhmDialog.OnInputListener() {
                @Override
                public void onInput(String num) {
                    duivip(num);
                }
            }).show();
        }
        if (view.getId() == R.id.tvKt){
            JumpUtils.Companion.startCreateQunActivity(this);
        }
    }


    private void ptConfig() {
        Map<String, Object> param = new HashMap<>();
        OkHttpHelper.getInstance().post_json(this, Url.ptConfig, param, new SpotsCallBack<ResultBean>(this) {
            @Override
            public void onSuccess(Response response, ResultBean resultBean) {
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
        OkHttpHelper.getInstance().post_json(this, Url.userInfo, param, new SpotsCallBack<ResultBean>(this) {
            @Override
            public void onSuccess(Response response, ResultBean resultBean) {
                bean = resultBean;
                tvName.setText(bean.data.name);
                GlideUtil.setImag(HyzxActivity.this, bean.data.icon, ivIcon);
                if (null != bean.data.isvip && bean.data.isvip.equals("1")) {
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
        OkHttpHelper.getInstance().post_json(this, Url.duivip, param, new SpotsCallBack<ResultBean>(this) {
            @Override
            public void onSuccess(Response response, ResultBean resultBean) {
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
}