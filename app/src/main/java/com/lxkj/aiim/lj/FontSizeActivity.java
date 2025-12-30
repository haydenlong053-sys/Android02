package com.lxkj.aiim.lj;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.lxkj.aiim.MainActivity;
import com.lxkj.aiim.R;
import com.lxkj.aiim.base.BaseSimpleActivity;
import com.lxkj.aiim.lj.bean.ResultBean;
import com.lxkj.aiim.lj.configuration.AppConsts;
import com.lxkj.aiim.lj.http.BaseCallback;
import com.lxkj.aiim.lj.http.OkHttpHelper;
import com.lxkj.aiim.lj.http.Url;
import com.lxkj.aiim.lj.utils.SharePrefUtil;
import com.lxkj.aiim.lj.utils.StringUtil;
import com.lxkj.aiim.lj.view.FontSizeView;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Request;
import okhttp3.Response;

public class FontSizeActivity extends BaseSimpleActivity {

    private float fontSizeScale = 1f;
    private TextView tvRightTxt;
    private TextView tvLeftTxt;
    private FontSizeView fsvFontSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();
        getToolbarTitle().setText(getResources().getString(R.string.字体大小));

        String size = SharePrefUtil.getString(this, AppConsts.FontScale, "");
        if (!StringUtil.isEmpty(size)) {
            fsvFontSize.setDefaultPosition(new BigDecimal((Float.parseFloat(size) - 0.875) / 0.125).intValue());
        }

        //滑动返回监听
        fsvFontSize.setChangeCallbackListener(new FontSizeView.OnChangeCallbackListener() {
            @Override
            public void onChangeListener(int position) {
                int dimension = getResources().getDimensionPixelSize(R.dimen.sp_stander);
                //根据position 获取字体倍数
                fontSizeScale = (float) (0.875 + 0.125 * position);
                //放大后的sp单位
                double v = fontSizeScale * (int) px2sp(FontSizeActivity.this, dimension);
                //改变当前页面大小
                changeTextSize((int) v);
            }
        });

//        userInfo();

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
                String size = resultBean.data.ziti;
                int dimension = getResources().getDimensionPixelSize(R.dimen.sp_stander);
                if (!StringUtil.isEmpty(size)) {
                    //根据position 获取字体倍数
                    fontSizeScale = (float) (0.875 + 0.125 * Double.parseDouble(size));
                    //放大后的sp单位
                    double v = fontSizeScale * (int) px2sp(FontSizeActivity.this, dimension);
                    //改变当前页面大小
                    changeTextSize((int) v);
                }
            }

            @Override
            public void onError(Response response, int code, Exception e) {
            }
        });
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        SharePrefUtil.saveString(FontSizeActivity.this, AppConsts.FontScale, fontSizeScale + "");
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    /**
     * 改变textsize 大小
     */
    private void changeTextSize(int dimension) {
        tvRightTxt.setTextSize(dimension);
        tvLeftTxt.setTextSize(dimension);

    }

    public static int px2sp(Context context, float pxValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_font_size;
    }

    private void initViews() {
        tvRightTxt = findViewById(R.id.tvRightTxt);
        tvLeftTxt = findViewById(R.id.tvLeftTxt);
        fsvFontSize = findViewById(R.id.fsv_font_size);
    }
}