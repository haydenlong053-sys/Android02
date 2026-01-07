package com.cboda.intelligent.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatImageView;

import com.cboda.intelligent.R;
import com.cboda.intelligent.base.BaseSimpleBroadActivity;
import com.cboda.intelligent.bean.ResultBroadBean;
import com.cboda.intelligent.utils.BaseBroadCallback;
import com.cboda.intelligent.utils.OkHttpBroadHelper;
import com.cboda.intelligent.net.BroadUrl;
import com.cboda.intelligent.utils.BitmapBroadUtils1;
import com.cboda.intelligent.utils.GlideBroadUtil;
import com.cboda.intelligent.utils.ToastBroadUtil;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Request;
import okhttp3.Response;

public class ErWeiMaBroadActivity extends BaseSimpleBroadActivity implements View.OnClickListener {

    private View viHead;
    private AppCompatImageView imFinish;
    private RoundedImageView riIcon;
    private TextView tvName;
    private ImageView imCode;
    private LinearLayout llContent;
    private TextView tvSave;

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
        return R.layout.activity_er_wei_ma;
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

        userInfo();

        imFinish.setOnClickListener(this);
        tvSave.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view.getId()==R.id.imFinish){
            finish();
        }
        if (view.getId()==R.id.tvSave){
            BitmapBroadUtils1.saveBitmap2(BitmapBroadUtils1.convertMeasureBitmap(llContent), this);
            ToastBroadUtil.show("保存成功");
        }

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
                GlideBroadUtil.setHeadImag(ErWeiMaBroadActivity.this, resultBean.data.icon, riIcon);
                tvName.setText(resultBean.data.name);
                GlideBroadUtil.setImag(ErWeiMaBroadActivity.this, resultBean.data.qrcode, imCode);
            }

            @Override
            public void onError(Response response, int code, Exception e) {
            }
        });
    }

    private void initViews() {
        viHead = findViewById(R.id.viHead);
        imFinish = findViewById(R.id.imFinish);
        riIcon = findViewById(R.id.riIcon);
        tvName = findViewById(R.id.tvName);
        imCode = findViewById(R.id.imCode);
        llContent = findViewById(R.id.llContent);
        tvSave = findViewById(R.id.tvSave);
    }
}