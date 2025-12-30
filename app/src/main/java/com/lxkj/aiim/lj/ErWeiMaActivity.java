package com.lxkj.aiim.lj;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatImageView;

import com.lxkj.aiim.R;
import com.lxkj.aiim.base.BaseSimpleActivity;
import com.lxkj.aiim.lj.bean.ResultBean;
import com.lxkj.aiim.lj.http.BaseCallback;
import com.lxkj.aiim.lj.http.OkHttpHelper;
import com.lxkj.aiim.lj.http.Url;
import com.lxkj.aiim.lj.utils.BitmapUtils1;
import com.lxkj.aiim.lj.utils.GlideUtil;
import com.lxkj.aiim.lj.utils.ToastUtil;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Request;
import okhttp3.Response;

public class ErWeiMaActivity extends BaseSimpleActivity implements View.OnClickListener {

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
            BitmapUtils1.saveBitmap2(BitmapUtils1.convertMeasureBitmap(llContent), this);
            ToastUtil.show("保存成功");
        }

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
                GlideUtil.setHeadImag(ErWeiMaActivity.this, resultBean.data.icon, riIcon);
                tvName.setText(resultBean.data.name);
                GlideUtil.setImag(ErWeiMaActivity.this, resultBean.data.qrcode, imCode);
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