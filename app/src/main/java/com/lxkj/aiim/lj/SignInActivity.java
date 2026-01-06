package com.lxkj.aiim.lj;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cboda.intelligent.R;
import com.lxkj.aiim.base.BaseSimpleActivity;
import com.lxkj.aiim.lj.adapter.SignInAdapter;
import com.lxkj.aiim.lj.bean.DataListBean;
import com.lxkj.aiim.lj.bean.ResultBean;
import com.lxkj.aiim.lj.http.BaseCallback;
import com.lxkj.aiim.lj.http.OkHttpHelper;
import com.lxkj.aiim.lj.http.SpotsCallBack;
import com.lxkj.aiim.lj.http.Url;
import com.lxkj.aiim.utils.JumpUtils;
import com.lxkj.aiim.utils.StringUtils;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Request;
import okhttp3.Response;

public class SignInActivity extends BaseSimpleActivity implements View.OnClickListener {


    private SignInAdapter signInAdapter;
    private List<DataListBean> listBeans = new ArrayList<>();
    private int page = 1, totalPage = 1;
    private View viHead;
    private AppCompatImageView imFinish;
    private AppCompatImageView imIssue;
    private TextView tvMoney;
    private TextView tvDay;
    private TextView tvQiandao;
    private ImageView ivNoData;
    private TextView tvNoData;
    private LinearLayout llNoData;
    private RecyclerView ryQiandao;
    private SmartRefreshLayout smart;

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
        return R.layout.activity_sign_in;
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

        signInAdapter = new SignInAdapter(this, listBeans);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        ryQiandao.setLayoutManager(linearLayoutManager);
        ryQiandao.setAdapter(signInAdapter);
        signInAdapter.setOnItemClickListener(new SignInAdapter.OnItemClickListener() {
            @Override
            public void OnItemClickListener(int position) {
            }
        });
        smart.setOnRefreshLoadMoreListener(new OnRefreshLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                if (page >= totalPage) {
                    refreshLayout.setNoMoreData(true);
                    return;
                }
                page++;
                signLogList();
            }

            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                page = 1;
                signLogList();
                refreshLayout.setNoMoreData(false);
            }
        });


        signLogList();

        signInfo();

        imIssue.setOnClickListener(this);
        imFinish.setOnClickListener(this);
        tvQiandao.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.imFinish) {
            finish();
        } else if (view.getId() == R.id.imIssue) {
            JumpUtils.Companion.startH5Activity(this, Url.QDGZ, getString(R.string.签到规则));
        } else if (view.getId() == R.id.tvQiandao) {
            sign();
        }
    }

    /**
     * 签到
     */
    private void sign() {
        Map<String, Object> param = new HashMap<>();
        OkHttpHelper.getInstance().post_json(this, Url.sign, param, new BaseCallback<ResultBean>() {
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
                signInfo();
                signLogList();
            }

            @Override
            public void onError(Response response, int code, Exception e) {
            }
        });
    }

    /**
     * 签到页
     */
    private void signInfo() {
        Map<String, Object> param = new HashMap<>();
        OkHttpHelper.getInstance().post_json(this, Url.signInfo, param, new SpotsCallBack<ResultBean>(this) {
            @Override
            public void onSuccess(Response response, ResultBean resultBean) {
                tvMoney.setText(StringUtils.removeDecimalPart(resultBean.data.signMoney));
                tvDay.setText(String.format(getResources().getString(R.string.已连续签到), resultBean.data.days));
                if (resultBean.data.isSign.equals("0")) {
                    tvQiandao.setText(getString(R.string.签到领取红包));
                } else {
                    tvQiandao.setText(getString(R.string.已签到));
                }
            }

            @Override
            public void onError(Response response, int code, Exception e) {
            }
        });
    }

    /**
     * 签到金额明细
     */
    private void signLogList() {
        Map<String, Object> param = new HashMap<>();
        param.put("pageNo", page);
        param.put("pageSize", "10");
        OkHttpHelper.getInstance().post_json(this, Url.signLogList, param, new BaseCallback<ResultBean>() {
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
                if (null != resultBean.totalPage)
                    totalPage = Integer.parseInt(resultBean.totalPage);
                smart.finishLoadMore();
                smart.finishRefresh();

                if (page == 1) {
                    listBeans.clear();
                }
                listBeans.addAll(resultBean.dataList);
                signInAdapter.notifyDataSetChanged();

                if (listBeans.size() == 0) {
                    llNoData.setVisibility(View.VISIBLE);
                } else {
                    llNoData.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onError(Response response, int code, Exception e) {
            }
        });
    }

    private void initViews() {
        viHead = findViewById(R.id.viHead);
        imFinish = findViewById(R.id.imFinish);
        imIssue = findViewById(R.id.imIssue);
        tvMoney = findViewById(R.id.tvMoney);
        tvDay = findViewById(R.id.tvDay);
        tvQiandao = findViewById(R.id.tvQiandao);
        ivNoData = findViewById(R.id.ivNoData);
        tvNoData = findViewById(R.id.tvNoData);
        llNoData = findViewById(R.id.llNoData);
        ryQiandao = findViewById(R.id.ryQiandao);
        smart = findViewById(R.id.smart);
    }
}