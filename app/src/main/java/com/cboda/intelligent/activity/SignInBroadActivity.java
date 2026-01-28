package com.cboda.intelligent.activity;

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
import com.cboda.intelligent.base.BaseSimpleBroadActivity;
import com.cboda.intelligent.adapter.SignInBroadAdapter;
import com.cboda.select.picture.entity.DataListBroadBean;
import com.cboda.intelligent.bean.ResultBroadBean;
import com.cboda.intelligent.utils.BaseBroadCallback;
import com.cboda.intelligent.utils.OkHttpBroadHelper;
import com.cboda.intelligent.utils.SpotsCallBroadBack;
import com.cboda.intelligent.net.BroadUrl;
import com.cboda.intelligent.utils.JumpBroadUtils;
import com.cboda.intelligent.utils.StringBroadUtils;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Request;
import okhttp3.Response;

public class SignInBroadActivity extends BaseSimpleBroadActivity implements View.OnClickListener {


    private SignInBroadAdapter signInAdapter;
    private List<DataListBroadBean> listBeans = new ArrayList<>();
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
        return R.layout.activity_broad_sign_in2;
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

        signInAdapter = new SignInBroadAdapter(this, listBeans);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        ryQiandao.setLayoutManager(linearLayoutManager);
        ryQiandao.setAdapter(signInAdapter);
        signInAdapter.setOnItemClickListener(new SignInBroadAdapter.OnItemClickListener() {
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
            JumpBroadUtils.Companion.startH5Activity(this, BroadUrl.getCheckinRewardsUrl(), getString(R.string.签到规则));
        } else if (view.getId() == R.id.tvQiandao) {
            sign();
        }
    }

    /**
     * 签到
     */
    private void sign() {
        Map<String, Object> param = new HashMap<>();
        OkHttpBroadHelper.getInstance().post_json(this, BroadUrl.sign, param, new BaseBroadCallback<ResultBroadBean>() {
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
        OkHttpBroadHelper.getInstance().post_json(this, BroadUrl.signInfo, param, new SpotsCallBroadBack<ResultBroadBean>(this) {
            @Override
            public void onSuccess(Response response, ResultBroadBean resultBean) {
                tvMoney.setText(StringBroadUtils.removeDecimalPart(resultBean.data.signMoney));
                tvDay.setText(String.format(getResources().getString(R.string.已连续签到), resultBean.data.days));
                if (resultBean.data.isSign.equals("0")) {
                    tvQiandao.setText(getString(R.string.签到));
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
        OkHttpBroadHelper.getInstance().post_json(this, BroadUrl.signLogList, param, new BaseBroadCallback<ResultBroadBean>() {
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