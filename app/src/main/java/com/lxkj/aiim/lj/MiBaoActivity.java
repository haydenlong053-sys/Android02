package com.lxkj.aiim.lj;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.interfaces.OnSelectListener;
import com.cboda.intelligent.R;
import com.lxkj.aiim.base.BaseSimpleActivity;
import com.lxkj.aiim.lj.bean.DataListBean;
import com.lxkj.aiim.lj.bean.ResultBean;
import com.lxkj.aiim.lj.http.BaseCallback;
import com.lxkj.aiim.lj.http.OkHttpHelper;
import com.lxkj.aiim.lj.http.SpotsCallBack;
import com.lxkj.aiim.lj.http.Url;
import com.lxkj.aiim.lj.utils.StringUtil;
import com.lxkj.aiim.lj.utils.ToastUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Request;
import okhttp3.Response;

public class MiBaoActivity extends BaseSimpleActivity implements View.OnClickListener {


    private List<DataListBean> listBeans = new ArrayList<>();
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
                    if (StringUtil.isEmpty(etMibaoIssue.getText().toString())) {
                        ToastUtil.show(getString(R.string.请输入密保问题));
                        return;
                    }
                } else {
                    if (StringUtil.isEmpty(tvMibao.getText().toString())) {
                        ToastUtil.show(getString(R.string.请选择密保问题));
                        return;
                    }
                }

                if (StringUtil.isEmpty(etMibao.getText().toString())) {
                    ToastUtil.show(getString(R.string.请输入密保答案));
                    return;
                }
                setMibao();
            } else {
                if (StringUtil.isEmpty(etMibao.getText().toString())) {
                    ToastUtil.show(getString(R.string.请输入密保答案));
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
        OkHttpHelper.getInstance().post_json(this, Url.changeMibao, param, new SpotsCallBack<ResultBean>(this) {
            @Override
            public void onSuccess(Response response, ResultBean resultBean) {
                Intent intent = new Intent(MiBaoActivity.this, RestPasswordActivity.class);
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
        OkHttpHelper.getInstance().post_json(this, Url.setMibao, param, new SpotsCallBack<ResultBean>(this) {
            @Override
            public void onSuccess(Response response, ResultBean resultBean) {
//                Intent intent = new Intent(MiBaoActivity.this, PaySettingActivity.class);
//                startActivity(intent);
                Intent intent = new Intent(MiBaoActivity.this, SetPayPasswordActivity.class);
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
        OkHttpHelper.getInstance().post_json(this, Url.mbFaqList, param, new BaseCallback<ResultBean>() {
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
                listBeans.clear();
                listBeans.addAll(resultBean.dataList);
                DataListBean dataListBean = new DataListBean();
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