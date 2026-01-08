package com.cboda.intelligent.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.cboda.intelligent.R;
import com.cboda.intelligent.base.BaseSimpleBroadActivity;
import com.cboda.intelligent.bean.ResultBroadBean;
import com.cboda.intelligent.utils.BaseBroadCallback;
import com.cboda.intelligent.utils.OkHttpBroadHelper;
import com.cboda.intelligent.net.BroadUrl;
import com.cboda.intelligent.utils.StringBroadUtil;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Request;
import okhttp3.Response;

public class AddtypeBroadActivity extends BaseSimpleBroadActivity implements View.OnClickListener {


    private ResultBroadBean bean;
    private ImageView imYonghu;
    private ImageView imID;
    private ImageView imPhone;
    private ImageView imQun;
    private ImageView imMingpian;
    private ImageView imErweima;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();
        getToolbarTitle().setText(getResources().getString(R.string.添加我的方式));

        imYonghu.setOnClickListener(this);
        imPhone.setOnClickListener(this);
        imQun.setOnClickListener(this);
        imMingpian.setOnClickListener(this);
        imErweima.setOnClickListener(this);
        imID.setOnClickListener(this);
        userInfo();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_add_broad_type;
    }

    @Override
    public void onClick(View view) {
        if (view.getId()==R.id.imYonghu){
            if (bean.data.accSerch.equals("0")) {
                editUserinfo("1", "", "", "", "", "");
            } else {
                editUserinfo("0", "", "", "", "", "");
            }
        }else  if (view.getId()==R.id.imID){
            if (bean.data.idnoSerch.equals("0")) {
                editUserinfo("", "", "", "", "", "1");
            } else {
                editUserinfo("", "", "", "", "", "0");
            }
        }else  if (view.getId() ==R.id.imPhone){
            if (bean.data.phoneSerch.equals("0")) {
                editUserinfo("", "1", "", "", "", "");
            } else {
                editUserinfo("", "0", "", "", "", "");
            }
        }else  if (view.getId() ==R.id.imQun){
            if (bean.data.groAdd.equals("0")) {
                editUserinfo("", "", "1", "", "", "");
            } else {
                editUserinfo("", "", "0", "", "", "");
            }
        }else  if (view.getId() ==R.id.imMingpian){
            if (bean.data.mpAdd.equals("0")) {
                editUserinfo("", "", "", "1", "", "");
            } else {
                editUserinfo("", "", "", "0", "", "");
            }
        }else  if (view.getId() ==R.id.imErweima){
            if (bean.data.mpAdd.equals("0")) {
                editUserinfo("", "", "", "", "1", "");
            } else {
                editUserinfo("", "", "", "", "0", "");
            }
        }
    }

    /**
     * 修改资料
     */
    private void editUserinfo(String accSerch, String phoneSerch, String groAdd, String mpAdd, String qrAdd, String idnoSerch) {
        Map<String, Object> param = new HashMap<>();
        param.put("accSerch", accSerch);
        param.put("phoneSerch", phoneSerch);
        param.put("groAdd", groAdd);
        param.put("mpAdd", mpAdd);
        param.put("qrAdd", qrAdd);
        param.put("idnoSerch", idnoSerch);
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
                if (StringBroadUtil.isEmpty(bean.data.accSerch)) {
                    bean.data.accSerch = "1";
                }
                if (StringBroadUtil.isEmpty(bean.data.phoneSerch)) {
                    bean.data.phoneSerch = "1";
                }
                if (StringBroadUtil.isEmpty(bean.data.groAdd)) {
                    bean.data.groAdd = "1";
                }
                if (StringBroadUtil.isEmpty(bean.data.mpAdd)) {
                    bean.data.mpAdd = "1";
                }
                if (StringBroadUtil.isEmpty(bean.data.qrAdd)) {
                    bean.data.qrAdd = "1";
                }

                if (resultBean.data.accSerch.equals("0")) {
                    imYonghu.setImageResource(R.mipmap.ic_guan);
                } else {
                    imYonghu.setImageResource(R.mipmap.ic_kai);
                }
                if (resultBean.data.idnoSerch.equals("0")) {
                    imID.setImageResource(R.mipmap.ic_guan);
                } else {
                    imID.setImageResource(R.mipmap.ic_kai);
                }

                if (resultBean.data.phoneSerch.equals("0")) {
                    imPhone.setImageResource(R.mipmap.ic_guan);
                } else {
                    imPhone.setImageResource(R.mipmap.ic_kai);
                }
                if (resultBean.data.groAdd.equals("0")) {
                    imQun.setImageResource(R.mipmap.ic_guan);
                } else {
                    imQun.setImageResource(R.mipmap.ic_kai);
                }
                if (resultBean.data.mpAdd.equals("0")) {
                    imMingpian.setImageResource(R.mipmap.ic_guan);
                } else {
                    imMingpian.setImageResource(R.mipmap.ic_kai);
                }
                if (resultBean.data.qrAdd.equals("0")) {
                    imErweima.setImageResource(R.mipmap.ic_guan);
                } else {
                    imErweima.setImageResource(R.mipmap.ic_kai);
                }
            }

            @Override
            public void onError(Response response, int code, Exception e) {
            }
        });
    }

    private void initViews() {
        imYonghu = findViewById(R.id.imYonghu);
        imID = findViewById(R.id.imID);
        imPhone = findViewById(R.id.imPhone);
        imQun = findViewById(R.id.imQun);
        imMingpian = findViewById(R.id.imMingpian);
        imErweima = findViewById(R.id.imErweima);
    }
}