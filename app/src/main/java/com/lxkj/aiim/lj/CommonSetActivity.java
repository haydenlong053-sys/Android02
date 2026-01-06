package com.lxkj.aiim.lj;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cboda.intelligent.R;
import com.lxkj.aiim.activity.MessageSoundActivity;
import com.lxkj.aiim.base.BaseSimpleActivity;
import com.lxkj.aiim.lj.bean.ResultBean;
import com.lxkj.aiim.lj.dialogfra.NormalDialog;
import com.lxkj.aiim.lj.http.BaseCallback;
import com.lxkj.aiim.lj.http.OkHttpHelper;
import com.lxkj.aiim.lj.http.Url;
import com.lxkj.aiim.lj.utils.StringUtil;
import com.cboda.baselibrary.utils.MMKVBroadUtils;

import java.util.HashMap;
import java.util.Map;

import cn.wildfirechat.remote.ChatManager;
import okhttp3.Request;
import okhttp3.Response;

public class CommonSetActivity extends BaseSimpleActivity implements View.OnClickListener {


    private ResultBean bean;
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
        if (!StringUtil.isEmpty(MMKVBroadUtils.Companion.getZt()))
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
        return R.layout.activity_common_set;
    }

    @Override
    public void onClick(View view) {
        Intent intent;
        if (view.getId()==R.id.llFangshi){
            intent = new Intent(this, AddtypeActivity.class);
            startActivity(intent);
        }
        if (view.getId() == R.id.llZiti) {
            intent = new Intent(this, FontSizeActivity.class);
            startActivity(intent);
        }
        if (view.getId() == R.id.llBeijing){
            intent = new Intent(this, ChatBgActivity.class);
            intent.putExtra("type", "1");
            startActivity(intent);
        }
        if (view.getId() == R.id.llHeimingdan){
            intent = new Intent(this, BlackListActivity.class);
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
            intent = new Intent(this, ZhuTiSetActivity.class);
            startActivity(intent);
        }
        if (view.getId() == R.id.llTishiyin){
            intent = new Intent(this, MessageSoundActivity.class);
            startActivity(intent);
        }
        if (view.getId() ==R.id.llClear){
            new NormalDialog(this, getString(R.string.温馨提示), getString(R.string.确认清空记录), getString(R.string.取消), getString(R.string.确定), true)
                    .setOnButtonClickListener(new NormalDialog.OnButtonClick() {
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
        OkHttpHelper.getInstance().post_json(this, Url.editUserinfo, param, new BaseCallback<ResultBean>() {
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
                bean = resultBean;

                if (StringUtil.isEmpty(bean.data.isAddYz)) {
                    bean.data.isAddYz = "1";
                }
                if (StringUtil.isEmpty(bean.data.isQuantx)) {
                    bean.data.isQuantx = "1";
                }
                if (StringUtil.isEmpty(bean.data.isMsgtx)) {
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