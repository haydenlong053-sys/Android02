package com.cboda.intelligent.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.interfaces.OnSelectListener;
import com.cboda.intelligent.R;
import com.cboda.intelligent.base.BaseSimpleBroadActivity;
import com.cboda.intelligent.bean.DataListBroadBean;
import com.cboda.intelligent.bean.ResultBroadBean;
import com.cboda.intelligent.utils.BaseBroadCallback;
import com.cboda.intelligent.utils.OkHttpBroadHelper;
import com.cboda.intelligent.utils.SpotsCallBroadBack;
import com.cboda.intelligent.net.BroadUrl;
import com.cboda.intelligent.utils.StringBroadUtil;
import com.cboda.intelligent.utils.ToastBroadUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Request;
import okhttp3.Response;

public class MiBaoBroadActivity extends BaseSimpleBroadActivity implements View.OnClickListener {


    private List<DataListBroadBean> listBeans = new ArrayList<>();
    private String type;
    private int position = 0;
    private TextView tvMibao;
    private LinearLayout llMibao;
    private EditText etMibaoIssue;
    private LinearLayout llZidingyiMibao;
    private EditText etMibao;
    private TextView tvOk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();


        type = getIntent().getStringExtra("type");
        if (type.equals("0")) {
            getToolbarTitle().setText(getResources().getString(R.string.设置密保问题));
//            mbFaqList();
        } else {
            getToolbarTitle().setText(getResources().getString(R.string.验证密保问题));
//            userInfo();
        }
        mbFaqList();
        llMibao.setOnClickListener(this);
        tvOk.setOnClickListener(this);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_mi_bao;
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.llMibao){
            String[] items = new String[listBeans.size()];
            for (int i = 0; i < listBeans.size(); i++) {
                items[i] = listBeans.get(i).title;
            }
            new XPopup.Builder(this)
                    .hasShadowBg(false) // 是否有半透明的背景，默认为true
                    .atView(llMibao)  // 依附于所点击的View，内部会自动判断在上方或者下方显示
                    .asAttachList(items,
                            null,
                            new OnSelectListener() {
                                @Override
                                public void onSelect(int xposition, String text) {
                                    position = xposition;
                                    tvMibao.setText(text);
                                    if (xposition == listBeans.size() - 1) {
                                        llZidingyiMibao.setVisibility(View.VISIBLE);
                                    } else {
                                        llZidingyiMibao.setVisibility(View.GONE);
                                    }
                                }
                            }, R.layout.popup_recycle, R.layout.custom_part_shadow_popup)
                    .show();
        }

        if (view.getId() ==R.id.tvOk){
            if (type.equals("0")) {
                if (position == listBeans.size() - 1) {
                    if (StringBroadUtil.isEmpty(etMibaoIssue.getText().toString())) {
                        ToastBroadUtil.show(getString(R.string.请输入密保问题));
                        return;
                    }
                } else {
                    if (StringBroadUtil.isEmpty(tvMibao.getText().toString())) {
                        ToastBroadUtil.show(getString(R.string.请选择密保问题));
                        return;
                    }
                }

                if (StringBroadUtil.isEmpty(etMibao.getText().toString())) {
                    ToastBroadUtil.show(getString(R.string.请输入密保答案));
                    return;
                }
                setMibao();
            } else {
                if (StringBroadUtil.isEmpty(etMibao.getText().toString())) {
                    ToastBroadUtil.show(getString(R.string.请输入密保答案));
                    return;
                }
                changeMibao();
            }
        }
    }

    /**
     * 验证密保问题
     */
    private void changeMibao() {
        Map<String, Object> param = new HashMap<>();
        param.put("title", tvMibao.getText().toString());
        param.put("answer", etMibao.getText().toString());
        OkHttpBroadHelper.getInstance().post_json(this, BroadUrl.changeMibao, param, new SpotsCallBroadBack<ResultBroadBean>(this) {
            @Override
            public void onSuccess(Response response, ResultBroadBean resultBean) {
                Intent intent = new Intent(MiBaoBroadActivity.this, RestPasswordBroadActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(Response response, int code, Exception e) {
            }
        });
    }

    /**
     * 设置密保问题
     */
    private void setMibao() {
        Map<String, Object> param = new HashMap<>();
        if (position == listBeans.size() - 1) {
            param.put("title", etMibaoIssue.getText().toString().toString());
        } else {
            param.put("title", tvMibao.getText().toString().toString());
        }
        param.put("answer", etMibao.getText().toString());
        OkHttpBroadHelper.getInstance().post_json(this, BroadUrl.setMibao, param, new SpotsCallBroadBack<ResultBroadBean>(this) {
            @Override
            public void onSuccess(Response response, ResultBroadBean resultBean) {
//                Intent intent = new Intent(MiBaoBroadActivity.this, PaySettingBroadActivity.class);
//                startActivity(intent);
                Intent intent = new Intent(MiBaoBroadActivity.this, SetPayPasswordBroadActivity.class);
                intent.putExtra("editIndex", "1");
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(Response response, int code, Exception e) {
            }
        });
    }

    /**
     * 密保问题列表
     */
    private void mbFaqList() {
        Map<String, Object> param = new HashMap<>();
        OkHttpBroadHelper.getInstance().post_json(this, BroadUrl.mbFaqList, param, new BaseBroadCallback<ResultBroadBean>() {
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
                listBeans.clear();
                listBeans.addAll(resultBean.dataList);
                DataListBroadBean dataListBean = new DataListBroadBean();
                dataListBean.title = "自定义密保问题";
                listBeans.add(dataListBean);

//                llMibao.setEnabled(true);
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
                tvMibao.setText(resultBean.data.mb);
                llMibao.setEnabled(false);
            }

            @Override
            public void onError(Response response, int code, Exception e) {
            }
        });
    }

    private void initViews() {
        tvMibao = findViewById(R.id.tvMibao);
        llMibao = findViewById(R.id.llMibao);
        etMibaoIssue = findViewById(R.id.etMibaoIssue);
        llZidingyiMibao = findViewById(R.id.llZidingyiMibao);
        etMibao = findViewById(R.id.etMibao);
        tvOk = findViewById(R.id.tvOk);
    }
}