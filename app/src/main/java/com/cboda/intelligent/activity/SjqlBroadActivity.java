package com.cboda.intelligent.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;

import com.cboda.chatui.utils.E;
import com.cboda.intelligent.R;
import com.cboda.intelligent.base.BaseSimpleBroadActivity;
import com.cboda.intelligent.bean.ResultBroadBean;
import com.cboda.intelligent.holder.CommanConfimBroadHolder;
import com.cboda.intelligent.net.BroadUrl;
import com.cboda.intelligent.utils.OkHttpBroadHelper;
import com.cboda.intelligent.utils.SpotsCallBroadBack;
import com.freddy.im.http.ImGroupHttpExample;
import com.freddy.im.http.model.im.GroupVo;
import com.freddy.im.listener.OnHttpImListener;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Response;

/**
 * 升级群聊页。
 * <p>
 * 群资料改走自研 {@code GET im/group/detail}（含 {@code gsize}）；
 * 会员态 / 规则文案 / 升级动作仍走业务 {@code userInfo}、{@code ptConfig}、{@code upGroup}。
 */
public class SjqlBroadActivity extends BaseSimpleBroadActivity implements View.OnClickListener {

    private String id;
    private String isvip = "0";
    /** 对齐业务字段：{@code "1"} 可升级，其它已是最高档 */
    private String gsize = "1";
    private View viHead;
    private AppCompatImageView fanhui;
    private ImageView ivIcon;
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
    protected boolean fitsSystemWindows() {
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
        LinearLayout.LayoutParams linearParams = (LinearLayout.LayoutParams) viHead.getLayoutParams();
        linearParams.height = result;
        viHead.setLayoutParams(linearParams);
        fanhui.setOnClickListener(this);
        tvKt.setOnClickListener(this);
        id = getIntent().getStringExtra("id");
        userInfo();
        ptConfig();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_shen_gji_qun_broad_liao;
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.fanhui) {
            finish();
            return;
        }
        if (view.getId() != R.id.tvKt) {
            return;
        }
        if ("0".equals(isvip)) {
            startActivity(new Intent(this, HyzxBroadActivity.class));
            return;
        }
        if (!"1".equals(gsize)) {
            return;
        }
        CommanConfimBroadHolder holder = new CommanConfimBroadHolder(this, getString(R.string.wenxintishi),
                getString(R.string.是否升级该群聊),
                getString(R.string.queding),
                getString(R.string.quxiao));
        holder.setCallBack(new CommanConfimBroadHolder.OnConfimCallBack() {
            @Override
            public void onClickConfim(boolean isConfim) {
                if (isConfim) {
                    upGroup();
                }
            }
        });
        holder.show();
    }

    private void ptConfig() {
//        Map<String, Object> param = new HashMap<>();
//        OkHttpBroadHelper.getInstance().post_json(this, BroadUrl.ptConfig, param, new SpotsCallBroadBack<ResultBroadBean>(this) {
//            @Override
//            public void onSuccess(Response response, ResultBroadBean resultBean) {
//                if (resultBean != null && resultBean.data != null) {
//                    tvGroupinfo.setText(resultBean.data.groupinfo);
//                }
//            }
//
//            @Override
//            public void onError(Response response, int code, Exception e) {
//            }
//        });
    }

    private void userInfo() {
        Map<String, Object> param = new HashMap<>();
        OkHttpBroadHelper.getInstance().post_json(this, BroadUrl.userInfo, param, new SpotsCallBroadBack<ResultBroadBean>(this) {
            @Override
            public void onSuccess(Response response, ResultBroadBean resultBean) {
                if (resultBean != null && resultBean.data != null && resultBean.data.isvip != null) {
                    isvip = resultBean.data.isvip;
                }
                loadGroupDetail();
            }

            @Override
            public void onError(Response response, int code, Exception e) {
            }
        });
    }

    /** 自研群详情（含 {@code gsize}），替换原业务 {@code /groupInfo}。 */
    private void loadGroupDetail() {
        if (TextUtils.isEmpty(id)) {
            return;
        }
        ImGroupHttpExample.requestGetGroupDetail(id, new OnHttpImListener<GroupVo>() {
            @Override
            public void onHttpImSuccess(@NonNull GroupVo group) {
                runOnUiThread(() -> bindGroupDetail(group));
            }
        }, () -> {
        });
    }

    private void bindGroupDetail(@NonNull GroupVo group) {
        tvName.setText(group.getName() != null ? group.getName() : "");
        E.INSTANCE.loadGroupImg(ivIcon, id, group.getIcon());
        String remoteGsize = null;
        if (group.getBusiExt() != null) {
            remoteGsize = group.getBusiExt().getGsize();
        }
        gsize = TextUtils.isEmpty(remoteGsize) ? "1" : remoteGsize;
        if ("0".equals(isvip)) {
            tvKt.setText(getString(R.string.群主还未成为会员));
            tvKt.setBackgroundResource(R.mipmap.ic_bg_btn2_broad_01);
            return;
        }
        if ("1".equals(gsize)) {
            tvGsize.setText(getString(R.string.群成员上限为1000人));
            tvKt.setText(getString(R.string.立即升级));
        } else {
            tvGsize.setText(getString(R.string.群成员上限为10000人));
            tvKt.setText(getString(R.string.您已升级成为最高群聊));
        }
    }

    private void upGroup() {
        Map<String, Object> param = new HashMap<>();
        param.put("id", id);
        OkHttpBroadHelper.getInstance().post_json(this, BroadUrl.upGroup, param, new SpotsCallBroadBack<ResultBroadBean>(this) {
            @Override
            public void onSuccess(Response response, ResultBroadBean resultBean) {
                loadGroupDetail();
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
