package com.cboda.intelligent.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatImageView;

import com.cboda.baselibrary.bean.GlobalBroadInfo;
import com.cboda.baselibrary.bean.GlobalFriendBroadBean;
import com.cboda.baselibrary.utils.ELogUtilKt;
import com.cboda.chatui.utils.GroupAvatarBroadLoader;
import com.cboda.intelligent.R;
import com.cboda.intelligent.base.BaseSimpleBroadActivity;
import com.cboda.intelligent.holder.CommanConfimBroadHolder;
import com.cboda.intelligent.bean.ResultBroadBean;
import com.cboda.intelligent.utils.OkHttpBroadHelper;
import com.cboda.intelligent.utils.SpotsCallBroadBack;
import com.cboda.intelligent.net.BroadUrl;
import com.cboda.intelligent.utils.GlideBroadUtil;
import com.cboda.select.picture.entity.CircleCommListBroadBean;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.wildfirechat.model.GroupInfo;
import cn.wildfirechat.model.UserInfo;
import cn.wildfirechat.remote.ChatManager;
import okhttp3.Response;

//升级群聊页
public class SjqlBroadActivity extends BaseSimpleBroadActivity implements View.OnClickListener {


    private ResultBroadBean bean;
    private String id, isvip = "0";
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
        return R.layout.activity_shen_gji_qun_broad_liao;
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.fanhui){
            finish();
        }
        if (view.getId() == R.id.tvKt){
            if (isvip.equals("0")) {
                startActivity(new Intent(this, HyzxBroadActivity.class));
            } else {
                if (null != bean && bean.data.gsize.equals("1")) {
                    CommanConfimBroadHolder holder = new CommanConfimBroadHolder(this, getString(R.string.wenxintishi),
                            getString(R.string.是否升级该群聊),
                            getString(R.string.queding),
                            getString(R.string.quxiao));
                    holder.setCallBack(new CommanConfimBroadHolder.OnConfimCallBack() {
                        @Override
                        public void onClickConfim(boolean isConfim) {
                            upGroup();
                        }
                    });
                    holder.show();
                }
            }

//                    JumpBroadUtils.Companion.startCreateQunActivity(this);
        }

    }


    private void ptConfig() {
        Map<String, Object> param = new HashMap<>();
        OkHttpBroadHelper.getInstance().post_json(this, BroadUrl.ptConfig, param, new SpotsCallBroadBack<ResultBroadBean>(this) {
            @Override
            public void onSuccess(Response response, ResultBroadBean resultBean) {
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
        OkHttpBroadHelper.getInstance().post_json(this, BroadUrl.userInfo, param, new SpotsCallBroadBack<ResultBroadBean>(this) {

            @Override
            public void onSuccess(Response response, ResultBroadBean resultBean) {
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
        OkHttpBroadHelper.getInstance().post_json(this, BroadUrl.groupInfo, param, new SpotsCallBroadBack<ResultBroadBean>(this) {
            @Override
            public void onSuccess(Response response, ResultBroadBean resultBean) {
                bean = resultBean;
                tvName.setText(bean.data.name);
                GroupInfo groupInfo = ChatManager.Instance().getGroupInfo(id, false);
//                ELogUtilKt.eLogD(APP_TAG,"groupInfo.portrait==="+groupInfo.portrait);

                // 提取头像URL列表
                List<String> avatarUrls = new ArrayList<>();
                if (bean.data.userList!=null && !bean.data.userList.isEmpty()){
                    for (CircleCommListBroadBean broadBean : bean.data.userList) {
                        avatarUrls.add(broadBean.icon);
                    }
                }
                if (TextUtils.isEmpty(bean.data.icon) && groupInfo!=null && !TextUtils.isEmpty(groupInfo.portrait)) {
                    GlideBroadUtil.setImag(SjqlBroadActivity.this, groupInfo.portrait, ivIcon);
                } else if (TextUtils.isEmpty(bean.data.icon)  && !avatarUrls.isEmpty()){
                    GroupAvatarBroadLoader.INSTANCE.loadToImageView(
                            SjqlBroadActivity.this,
                            avatarUrls,
                            ivIcon,
                            id
                    );
                }else {
                    GlideBroadUtil.setImag(SjqlBroadActivity.this, bean.data.icon, ivIcon);
                }



                if (isvip.equals("0")) {
                    tvKt.setText(getString(R.string.群主还未成为会员));
                    tvKt.setBackgroundResource(R.mipmap.ic_bg_btn2_broad_01);
                } else {
                    if (bean.data.gsize.equals("1")) {
                        tvGsize.setText(getString(R.string.群成员上限为1000人));
                        tvKt.setText(getString(R.string.立即升级));
                    } else {
                        tvGsize.setText(getString(R.string.群成员上限为10000人));
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
        OkHttpBroadHelper.getInstance().post_json(this, BroadUrl.upGroup, param, new SpotsCallBroadBack<ResultBroadBean>(this) {
            @Override
            public void onSuccess(Response response, ResultBroadBean resultBean) {
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