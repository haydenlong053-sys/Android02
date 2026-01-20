package com.cboda.intelligent.activity;

import android.content.Intent;
import android.graphics.Color;
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
import com.cboda.intelligent.adapter.MyCircleBroadAdapter;
import com.cboda.select.picture.entity.DataListBroadBean;
import com.cboda.intelligent.bean.ResultBroadBean;
import com.cboda.intelligent.configuration.AppBroadConsts;
import com.cboda.intelligent.utils.BaseBroadCallback;
import com.cboda.intelligent.utils.OkHttpBroadHelper;
import com.cboda.intelligent.net.BroadUrl;
import com.cboda.intelligent.utils.GlideBroadUtil;
import com.cboda.intelligent.utils.SharePrefBroadBroadUtil;
import com.cboda.intelligent.widge.MyScrollBroadView;
import com.makeramen.roundedimageview.RoundedImageView;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Request;
import okhttp3.Response;

public class MyCircleBroadActivity extends BaseSimpleBroadActivity implements View.OnClickListener {

    private MyCircleBroadAdapter myCircleAdapter;
    private List<DataListBroadBean> listBeans = new ArrayList<>();
    private int page = 1, totalPage = 1;
    private String id;
    private ImageView imBg;
    private TextView tvName;
    private RoundedImageView riIcon;
    private ImageView ivNoData;
    private TextView tvNoData;
    private LinearLayout llNoData;
    private RecyclerView ryCircle;
    private MyScrollBroadView ns;
    private SmartRefreshLayout smart;
    private View viHead;
    private ImageView imFinish;
    private ImageView imAdd;
    private LinearLayout llTool;

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
        return R.layout.activity_my_broad_circle;
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

        id = getIntent().getStringExtra("id");
        if (id.equals(SharePrefBroadBroadUtil.getString(this, AppBroadConsts.UID, ""))) {
            imAdd.setVisibility(View.VISIBLE);
        } else {
            imAdd.setVisibility(View.GONE);
        }

        ns.setOnScrollChanged(new MyScrollBroadView.OnScrollChanged() {
            @Override
            public void onScroll(int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (scrollY > 600) {
                    llTool.setBackgroundColor(Color.parseColor("#FFFFFF"));
                } else {
                    llTool.setBackgroundColor(Color.parseColor("#00000000"));
                }
            }
        });

        myCircleAdapter = new MyCircleBroadAdapter(this, listBeans);
        ryCircle.setLayoutManager(new LinearLayoutManager(this));
        ryCircle.setAdapter(myCircleAdapter);
        myCircleAdapter.setOnItemClickListener(new MyCircleBroadAdapter.OnItemClickListener() {
            @Override
            public void OnItemClickListener(int position) {
                Intent intent = new Intent(MyCircleBroadActivity.this, CircleDetailBroadActivity.class);
                intent.putExtra("id", listBeans.get(position).id);
                startActivity(intent);
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
                duiQuanList();
            }

            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                page = 1;
                duiQuanList();
                refreshLayout.setNoMoreData(false);
            }
        });


        duiQuanList();

        imFinish.setOnClickListener(this);
        imAdd.setOnClickListener(this);
        imBg.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        Intent intent;
        if (view.getId() ==R.id.imFinish){
            finish();
        }else  if (view.getId() ==R.id.imAdd){
            intent = new Intent(this, CircelMessageBroadActivity.class);
            startActivity(intent);
        }else  if (view.getId() == R.id.imBg){
            if (id.equals(SharePrefBroadBroadUtil.getString(this, AppBroadConsts.UID, ""))) {
                intent = new Intent(this, ChatBgBroadActivity.class);
                intent.putExtra("type", "0");
                startActivity(intent);
            }
        }
    }

    /**
     * 对方朋友圈
     */
    private void duiQuanList() {
        Map<String, Object> param = new HashMap<>();
        param.put("id", id);
        OkHttpBroadHelper.getInstance().post_json(this, BroadUrl.duiQuanList, param, new BaseBroadCallback<ResultBroadBean>() {
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

                GlideBroadUtil.setImag(MyCircleBroadActivity.this, resultBean.backImg, imBg);
                GlideBroadUtil.setHeadImag(MyCircleBroadActivity.this, resultBean.icon, riIcon);
                tvName.setText(resultBean.name);

                if (null != resultBean.totalPage)
                    totalPage = Integer.parseInt(resultBean.totalPage);
                smart.finishLoadMore();
                smart.finishRefresh();

                if (page == 1) {
                    listBeans.clear();
                }
                listBeans.addAll(resultBean.dataList);
                myCircleAdapter.notifyDataSetChanged();

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
        imBg = findViewById(R.id.imBg);
        tvName = findViewById(R.id.tvName);
        riIcon = findViewById(R.id.riIcon);
        ivNoData = findViewById(R.id.ivNoData);
        tvNoData = findViewById(R.id.tvNoData);
        llNoData = findViewById(R.id.llNoData);
        ryCircle = findViewById(R.id.ryCircle);
        ns = findViewById(R.id.ns);
        smart = findViewById(R.id.smart);
        viHead = findViewById(R.id.viHead);
        imFinish = findViewById(R.id.imFinish);
        imAdd = findViewById(R.id.imAdd);
        llTool = findViewById(R.id.llTool);
    }
}