package com.lxkj.aiim.lj;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatImageView;

import com.cboda.intelligent.R;
import com.lxkj.aiim.base.BaseSimpleActivity;
import com.lxkj.aiim.holder.CommanConfimHolder;
import com.lxkj.aiim.lj.bean.ResultBean;
import com.lxkj.aiim.lj.http.OkHttpHelper;
import com.lxkj.aiim.lj.http.SpotsCallBack;
import com.lxkj.aiim.lj.http.Url;
import com.lxkj.aiim.lj.utils.GlideUtil;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Response;

public class SjqlActivity extends BaseSimpleActivity implements View.OnClickListener {


    private ResultBean bean;
    private String id, isvip = "0";
    private View viHead;
    private AppCompatImageView fanhui;
    private RoundedImageView ivIcon;
    private TextView tvName;
    private TextView tvGsize;
    private TextView tvGroupinfo;
    private TextView tvKt;

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
        fanhui.setOnClickListener(this);
        tvKt.setOnClickListener(this);
        id = getIntent().getStringExtra("id");
        userInfo();
        ptConfig();
    }


    @Override
    protected int getLayoutId() {
        return R.layout.activity_shengjiqunliao;
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.fanhui){
            finish();
        }
        if (view.getId() == R.id.tvKt){
            if (isvip.equals("0")) {
                startActivity(new Intent(this, HyzxActivity.class));
            } else {
                if (null != bean && bean.data.gsize.equals("1")) {
                    CommanConfimHolder holder = new CommanConfimHolder(this, getString(R.string.wenxintishi),
                            getString(R.string.是否升级该群聊),
                            getString(R.string.queding),
                            getString(R.string.quxiao));
                    holder.setCallBack(new CommanConfimHolder.OnConfimCallBack() {
                        @Override
                        public void onClickConfim(boolean isConfim) {
                            upGroup();
                        }
                    });
                    holder.show();
                }
            }

//                    JumpUtils.Companion.startCreateQunActivity(this);
        }

    }


    private void ptConfig() {
        Map<String, Object> param = new HashMap<>();
        OkHttpHelper.getInstance().post_json(this, Url.ptConfig, param, new SpotsCallBack<ResultBean>(this) {
            @Override
            public void onSuccess(Response response, ResultBean resultBean) {
                tvGroupinfo.setText(resultBean.data.groupinfo);
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
                isvip = resultBean.data.isvip;
                groupInfo();
            }

            @Override
            public void onError(Response response, int code, Exception e) {
            }
        });
    }

    /**
     * 群组详情
     */
    private void groupInfo() {
        Map<String, Object> param = new HashMap<>();
        param.put("id", id);
        OkHttpHelper.getInstance().post_json(this, Url.groupInfo, param, new SpotsCallBack<ResultBean>(this) {
            @Override
            public void onSuccess(Response response, ResultBean resultBean) {
                bean = resultBean;
                tvName.setText(bean.data.name);
                GlideUtil.setImag(SjqlActivity.this, bean.data.icon, ivIcon);

                if (isvip.equals("0")) {
                    tvKt.setText(getString(R.string.群主还未成为会员));
                    tvKt.setBackgroundResource(R.mipmap.ic_bg_btn2_01);
                } else {
                    if (bean.data.gsize.equals("1")) {
                        tvGsize.setText(getString(R.string.群成员上限为1000人));
                        tvKt.setText(getString(R.string.立即升级));
                    } else {
                        tvGsize.setText(getString(R.string.群成员上限为5000人));
                        tvKt.setText(getString(R.string.您已升级成为最高群聊));
                    }
                }
            }

            @Override
            public void onError(Response response, int code, Exception e) {
            }
        });
    }


    /**
     * 升级群聊
     */
    private void upGroup() {
        Map<String, Object> param = new HashMap<>();
        param.put("id", id);
        OkHttpHelper.getInstance().post_json(this, Url.upGroup, param, new SpotsCallBack<ResultBean>(this) {
            @Override
            public void onSuccess(Response response, ResultBean resultBean) {
                groupInfo();
            }

            @Override
            public void onError(Response response, int code, Exception e) {
            }
        });
    }

    private void initViews() {
        viHead = findViewById(R.id.viHead);
        fanhui = findViewById(R.id.fanhui);
        ivIcon = findViewById(R.id.ivIcon);
        tvName = findViewById(R.id.tvName);
        tvGsize = findViewById(R.id.tvGsize);
        tvGroupinfo = findViewById(R.id.tvGroupinfo);
        tvKt = findViewById(R.id.tvKt);
    }
}