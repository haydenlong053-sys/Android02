package com.cboda.intelligent.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.cboda.intelligent.R;
import com.cboda.intelligent.base.BaseSimpleBroadActivity;
import com.cboda.intelligent.adapter.HyyBroadAdapter;
import com.cboda.baselibrary.bean.DataListBroadBean;
import com.cboda.intelligent.bean.ResultBroadBean;
import com.cboda.intelligent.dialog.NormalBroadDialog;
import com.cboda.intelligent.utils.AppBroadUtil;
import com.cboda.intelligent.utils.BaseBroadCallback;
import com.cboda.intelligent.utils.OkHttpBroadHelper;
import com.cboda.intelligent.utils.SpotsCallBroadBack;
import com.cboda.intelligent.net.BroadUrl;
import com.cboda.intelligent.utils.JumpBroadUtils;
import com.cboda.intelligent.widge.SpacingItemBroadDecoration;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Request;
import okhttp3.Response;

public class HyyBroadActivity extends BaseSimpleBroadActivity {


    private HyyBroadAdapter hyyAdapter;
    private List<DataListBroadBean> listBeans = new ArrayList<>();
    private String id;
    private ImageView ivNoData;
    private TextView tvNoData;
    private LinearLayout llNoData;
    private RecyclerView ryCollect;
    private SmartRefreshLayout smart;
    private TextView tvAdd;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_broad_hyy;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();
        getToolbarTitle().setText(getResources().getString(R.string.欢迎语));

        id = getIntent().getStringExtra("id");

        hyyAdapter = new HyyBroadAdapter(this, listBeans);
        ryCollect.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL));
        ryCollect.addItemDecoration(new SpacingItemBroadDecoration(1, AppBroadUtil.dp2px(this, 10), true));
        ryCollect.setAdapter(hyyAdapter);
        hyyAdapter.setOnItemClickListener(new HyyBroadAdapter.OnItemClickListener() {
            @Override
            public void OnItemClickListener(int position) {

            }

            @Override
            public void OnItemDeleteClickListener(int position) {
                new NormalBroadDialog(HyyBroadActivity.this, getString(R.string.wenxintishi), getString(R.string.是否确认删除), getString(R.string.取消), getString(R.string.确定), true).setOnButtonClickListener(new NormalBroadDialog.OnButtonClick() {
                    @Override
                    public void OnRightClick() {
                        delHy(position);
                    }

                    @Override
                    public void OnLeftClick() {

                    }
                }).show();
            }
        });

        smart.setOnRefreshLoadMoreListener(new OnRefreshLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
            }

            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                groupHyList();
            }
        });
        smart.setEnableLoadMore(false);


        tvAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                JumpBroadUtils.Companion.startHyyAddActivity(HyyBroadActivity.this, id);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        groupHyList();
    }

    /**
     * 群欢迎语
     */
    private void groupHyList() {
        Map<String, Object> param = new HashMap<>();
        param.put("id", id);
        OkHttpBroadHelper.getInstance().post_json(this, BroadUrl.groupHyList, param, new BaseBroadCallback<ResultBroadBean>() {
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
                smart.finishLoadMore();
                smart.finishRefresh();
                listBeans.clear();
                listBeans.addAll(resultBean.dataList);
                hyyAdapter.notifyDataSetChanged();
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

    /**
     * 群欢迎语
     */
    private void delHy(int position) {
        Map<String, Object> param = new HashMap<>();
        param.put("id", listBeans.get(position).id);
        OkHttpBroadHelper.getInstance().post_json(this, BroadUrl.delHy, param, new SpotsCallBroadBack<ResultBroadBean>(this) {

            @Override
            public void onSuccess(Response response, ResultBroadBean resultBean) {
                listBeans.remove(position);
                hyyAdapter.notifyDataSetChanged();
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
        ryCollect = findViewById(R.id.ryCollect);
        smart = findViewById(R.id.smart);
        tvAdd = findViewById(R.id.tvAdd);
    }
}