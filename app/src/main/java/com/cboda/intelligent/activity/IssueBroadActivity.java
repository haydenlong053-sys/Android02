package com.cboda.intelligent.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cboda.intelligent.R;
import com.cboda.intelligent.base.BaseSimpleBroadActivity;
import com.cboda.intelligent.adapter.IssueBroadAdapter;
import com.cboda.baselibrary.bean.DataListBroadBean;
import com.cboda.intelligent.bean.ResultBroadBean;
import com.cboda.intelligent.utils.BaseBroadCallback;
import com.cboda.intelligent.utils.OkHttpBroadHelper;
import com.cboda.intelligent.net.BroadUrl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Request;
import okhttp3.Response;

public class IssueBroadActivity extends BaseSimpleBroadActivity {

    private IssueBroadAdapter issueAdapter;
    private List<DataListBroadBean> listBeans = new ArrayList<>();
    private ImageView ivNoData;
    private TextView tvNoData;
    private LinearLayout llNoData;
    private RecyclerView ryIssue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();
        getToolbarTitle().setText(getResources().getString(R.string.常见问题));

        issueAdapter = new IssueBroadAdapter(this, listBeans);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        ryIssue.setLayoutManager(linearLayoutManager);
        ryIssue.setAdapter(issueAdapter);
        issueAdapter.setOnItemClickListener(new IssueBroadAdapter.OnItemClickListener() {
            @Override
            public void OnItemClickListener(int position) {
            }
        });

        qaList();
    }

    /**
     * 常见问题
     */
    private void qaList() {
        Map<String, Object> param = new HashMap<>();
        OkHttpBroadHelper.getInstance().post_json(this, BroadUrl.qaList, param, new BaseBroadCallback<ResultBroadBean>() {
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
                issueAdapter.notifyDataSetChanged();

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

    @Override
    protected int getLayoutId() {
        return R.layout.activity_broad_issue;
    }

    private void initViews() {
        ivNoData = findViewById(R.id.ivNoData);
        tvNoData = findViewById(R.id.tvNoData);
        llNoData = findViewById(R.id.llNoData);
        ryIssue = findViewById(R.id.ryIssue);
    }
}