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
import com.cboda.intelligent.adapter.WithdrawRecordBroadAdapter;
import com.cboda.select.picture.entity.DataListBroadBean;
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

public class WithdrawRecordBroadActivity extends BaseSimpleBroadActivity {


    private WithdrawRecordBroadAdapter withdrawRecordAdapter;
    private List<DataListBroadBean> listBeans = new ArrayList<>();
    private int page = 1, totalPage = 1;
    private ImageView ivNoData;
    private TextView tvNoData;
    private LinearLayout llNoData;
    private RecyclerView ryJilu;
    private SmartRefreshLayout smart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();

        getToolbarTitle().setText(getString(R.string.提现记录));

        withdrawRecordAdapter = new WithdrawRecordBroadAdapter(this, listBeans);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        ryJilu.setLayoutManager(linearLayoutManager);
        ryJilu.setAdapter(withdrawRecordAdapter);
        withdrawRecordAdapter.setOnItemClickListener(new WithdrawRecordBroadAdapter.OnItemClickListener() {
            @Override
            public void OnItemClickListener(int position) {
                boolean isChecked = listBeans.get(position).isChecked;
                for (int i = 0; i < listBeans.size(); i++) {
                    listBeans.get(i).isChecked = false;
                }
                listBeans.get(position).isChecked = !isChecked;
                withdrawRecordAdapter.notifyDataSetChanged();
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
                txLogList();
            }

            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                page = 1;
                txLogList();
                refreshLayout.setNoMoreData(false);
            }
        });


        txLogList();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_withdraw_broad_record;
    }

    /**
     * 提现记录
     */
    private void txLogList() {
        Map<String, Object> param = new HashMap<>();
        param.put("pageNo", page);
        param.put("pageSize", "10");
        OkHttpBroadHelper.getInstance().post_json(this, BroadUrl.txLogList, param, new BaseBroadCallback<ResultBroadBean>() {
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
                withdrawRecordAdapter.notifyDataSetChanged();

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
        ryJilu = findViewById(R.id.ryJilu);
        smart = findViewById(R.id.smart);
    }
}