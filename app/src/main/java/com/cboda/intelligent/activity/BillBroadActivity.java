package com.cboda.intelligent.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cboda.intelligent.R;
import com.cboda.intelligent.base.BaseSimpleBroadActivity;
import com.cboda.intelligent.adapter.BillBroadAdapter;
import com.cboda.baselibrary.bean.DataListBroadBean;
import com.cboda.intelligent.bean.ResultBroadBean;
import com.cboda.intelligent.utils.BaseBroadCallback;
import com.cboda.intelligent.utils.OkHttpBroadHelper;
import com.cboda.intelligent.net.BroadUrl;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Request;
import okhttp3.Response;

public class BillBroadActivity extends BaseSimpleBroadActivity {

    private BillBroadAdapter billAdapter;
    private List<DataListBroadBean> listBeans = new ArrayList<>();
    private int page = 1, totalPage = 1;
    private ImageView ivNoData;
    private TextView tvNoData;
    private LinearLayout llNoData;
    private RecyclerView ryMingxi;
    private SmartRefreshLayout smart;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_broad_bill;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getToolbarTitle().setText(getString(R.string.账单明细));
        initViews();

        billAdapter = new BillBroadAdapter(this, listBeans);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        ryMingxi.setLayoutManager(linearLayoutManager);
        ryMingxi.setAdapter(billAdapter);
        billAdapter.setOnItemClickListener(new BillBroadAdapter.OnItemClickListener() {
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
                balanceLogList();
            }

            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                page = 1;
                balanceLogList();
                refreshLayout.setNoMoreData(false);
            }
        });


        balanceLogList();
    }

    /**
     * 账户记录
     */
    private void balanceLogList() {
        Map<String, Object> param = new HashMap<>();
        param.put("pageNo", page);
        param.put("pageSize", "10");
        OkHttpBroadHelper.getInstance().post_json(this, BroadUrl.balanceLogList, param, new BaseBroadCallback<ResultBroadBean>() {
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
                billAdapter.notifyDataSetChanged();

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
        ivNoData = findViewById(R.id.ivNoData);
        tvNoData = findViewById(R.id.tvNoData);
        llNoData = findViewById(R.id.llNoData);
        ryMingxi = findViewById(R.id.ryMingxi);
        smart = findViewById(R.id.smart);
    }
}