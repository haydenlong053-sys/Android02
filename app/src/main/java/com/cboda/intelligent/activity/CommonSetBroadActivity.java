package com.cboda.intelligent.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cboda.intelligent.R;
import com.cboda.intelligent.base.BaseSimpleBroadActivity;
import com.cboda.intelligent.bean.ResultBroadBean;
import com.cboda.intelligent.dialog.NormalBroadDialog;
import com.cboda.intelligent.utils.BaseBroadCallback;
import com.cboda.intelligent.utils.OkHttpBroadHelper;
import com.cboda.intelligent.net.BroadUrl;
import com.cboda.intelligent.utils.StringBroadUtil;
import com.cboda.baselibrary.utils.MMKVBroadUtils;

import java.util.HashMap;
import java.util.Map;

import cn.wildfirechat.remote.ChatManager;
import okhttp3.Request;
import okhttp3.Response;

public class CommonSetBroadActivity extends BaseSimpleBroadActivity implements View.OnClickListener {


    private ResultBroadBean bean;
    private ImageView imHoayou;
    private ImageView imjinzhi;
    private ImageView imGengxin;
    private ImageView imXiaoxi;
    private TextView tvZts;
    private LinearLayout llZts;
    private LinearLayout llFangshi;
    private LinearLayout llZiti;
    private LinearLayout llBeijing;
    private LinearLayout llHeimingdan;
    private LinearLayout llClear;
    private LinearLayout llTishiyin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getToolbarTitle().setText(getResources().getString(R.string.通用));
        initViews();

        llFangshi.setOnClickListener(this);
        llZiti.setOnClickListener(this);
        llBeijing.setOnClickListener(this);
        llHeimingdan.setOnClickListener(this);
        imHoayou.setOnClickListener(this);
        imGengxin.setOnClickListener(this);
        imXiaoxi.setOnClickListener(this);
        llClear.setOnClickListener(this);
        llZts.setOnClickListener(this);
        llTishiyin.setOnClickListener(this);
        if (!StringBroadUtil.isEmpty(MMKVBroadUtils.Companion.getZt()))
            tvZts.setText(MMKVBroadUtils.Companion.getZt());

        userInfo();

        imjinzhi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ("1".equals(bean.data.noadd)) {
                    editUserinfo("", "", "", "0");
                } else {
                    editUserinfo("", "", "", "1");
                }
            }
        });
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_common_broad_set;
    }

    @Override
    public void onClick(View view) {
        Intent intent;
        if (view.getId()==R.id.llFangshi){
            intent = new Intent(this, AddtypeBroadActivity.class);
            startActivity(intent);
        }
        if (view.getId() == R.id.llZiti) {
            intent = new Intent(this, FontSizeBroadActivity.class);
            startActivity(intent);
        }
        if (view.getId() == R.id.llBeijing){
            intent = new Intent(this, ChatBgBroadActivity.class);
            intent.putExtra("type", "1");
            startActivity(intent);
        }
        if (view.getId() == R.id.llHeimingdan){
            intent = new Intent(this, BlackListBroadActivity.class);
            startActivity(intent);
        }

        if (view.getId() == R.id.imHoayou){
            if (bean.data.isAddYz.equals("0")) {
                editUserinfo("1", "", "", "");
            } else {
                editUserinfo("0", "", "", "");
            }
        }
        if (view.getId() == R.id.imGengxin) {
            if (bean.data.isQuantx.equals("0")) {
                editUserinfo("", "1", "", "");
            } else {
                editUserinfo("", "0", "", "");
            }
        }

        if (view.getId() == R.id.imXiaoxi){
            if (bean.data.isMsgtx.equals("0")) {
                editUserinfo("", "", "1", "");
            } else {
                editUserinfo("", "", "0", "");
            }
        }
        if (view.getId() ==R.id.llZts){
            intent = new Intent(this, ZhuTiSetBroadActivity.class);
            startActivity(intent);
        }
        if (view.getId() == R.id.llTishiyin){
            intent = new Intent(this, MessageSoundBroadActivity.class);
            startActivity(intent);
        }
        if (view.getId() ==R.id.llClear){
            new NormalBroadDialog(this, getString(R.string.温馨提示), getString(R.string.确认清空记录), getString(R.string.取消), getString(R.string.确定), true)
                    .setOnButtonClickListener(new NormalBroadDialog.OnButtonClick() {
                        @Override
                        public void OnRightClick() {
                            ChatManager.Instance().clearAllMessages(true);
                        }

                        @Override
                        public void OnLeftClick() {
                        }
                    }).show();
        }


    }

    /**
     * 修改资料
     */
    private void editUserinfo(String isAddYz, String isQuantx, String isMsgtx, String noadd) {
        Map<String, Object> param = new HashMap<>();
        param.put("isAddYz", isAddYz);
        param.put("isQuantx", isQuantx);
        param.put("isMsgtx", isMsgtx);
        param.put("noadd", noadd);
        OkHttpBroadHelper.getInstance().post_json(this, BroadUrl.editUserinfo, param, new BaseBroadCallback<ResultBroadBean>() {
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
                userInfo();
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
                bean = resultBean;

                if (StringBroadUtil.isEmpty(bean.data.isAddYz)) {
                    bean.data.isAddYz = "1";
                }
                if (StringBroadUtil.isEmpty(bean.data.isQuantx)) {
                    bean.data.isQuantx = "1";
                }
                if (StringBroadUtil.isEmpty(bean.data.isMsgtx)) {
                    bean.data.isMsgtx = "1";
                }


                if (resultBean.data.isAddYz.equals("0")) {
                    imHoayou.setImageResource(R.mipmap.ic_guan);
                } else {
                    imHoayou.setImageResource(R.mipmap.ic_kai);
                }
                if (resultBean.data.isQuantx.equals("0")) {
                    imGengxin.setImageResource(R.mipmap.ic_guan);
                } else {
                    imGengxin.setImageResource(R.mipmap.ic_kai);
                }
                if (resultBean.data.isMsgtx.equals("0")) {
                    imXiaoxi.setImageResource(R.mipmap.ic_guan);
                } else {
                    imXiaoxi.setImageResource(R.mipmap.ic_kai);
                }

                if ("1".equals(resultBean.data.noadd)) {
                    imjinzhi.setImageResource(R.mipmap.ic_kai);
                } else {
                    imjinzhi.setImageResource(R.mipmap.ic_guan);
                }
            }

            @Override
            public void onError(Response response, int code, Exception e) {
            }
        });
    }

    private void initViews() {
        imHoayou = findViewById(R.id.imHoayou);
        imjinzhi = findViewById(R.id.imjinzhi);
        imGengxin = findViewById(R.id.imGengxin);
        imXiaoxi = findViewById(R.id.imXiaoxi);
        tvZts = findViewById(R.id.tvZts);
        llZts = findViewById(R.id.llZts);
        llFangshi = findViewById(R.id.llFangshi);
        llZiti = findViewById(R.id.llZiti);
        llBeijing = findViewById(R.id.llBeijing);
        llHeimingdan = findViewById(R.id.llHeimingdan);
        llClear = findViewById(R.id.llClear);
        llTishiyin = findViewById(R.id.llTishiyin);
    }
}