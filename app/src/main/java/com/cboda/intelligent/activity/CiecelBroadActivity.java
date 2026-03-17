package com.cboda.intelligent.activity;

import static com.cboda.baselibrary.utils.ELogUtilKt.APP_TAG;

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

import com.cboda.baselibrary.utils.ELogUtilKt;
import com.cboda.baselibrary.view.AppBarStateChangeListener;
import com.cboda.intelligent.R;
import com.cboda.intelligent.base.BaseSimpleBroadActivity;
import com.cboda.intelligent.adapter.CiecelBroadAdapter;
import com.cboda.select.picture.entity.CircleCommListBroadBean;
import com.cboda.select.picture.entity.DataListBroadBean;
import com.cboda.intelligent.bean.EventBusBroadBean;
import com.cboda.intelligent.bean.ResultBroadBean;
import com.cboda.intelligent.configuration.AppBroadConsts;
import com.cboda.intelligent.dialog.InputDialogBroadFra;
import com.cboda.intelligent.dialog.NormalBroadDialog;
import com.cboda.intelligent.utils.BaseBroadCallback;
import com.cboda.intelligent.utils.OkHttpBroadHelper;
import com.cboda.intelligent.net.BroadUrl;
import com.cboda.intelligent.utils.GlideBroadUtil;
import com.cboda.intelligent.utils.SharePrefBroadBroadUtil;
import com.cboda.intelligent.utils.StringBroadUtil;
import com.cboda.intelligent.widge.MyScrollBroadView;
import com.google.android.material.appbar.AppBarLayout;
import com.makeramen.roundedimageview.RoundedImageView;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Request;
import okhttp3.Response;

public class CiecelBroadActivity extends BaseSimpleBroadActivity implements View.OnClickListener {

    private CiecelBroadAdapter ciecelAdapter;
    private List<DataListBroadBean> listBeans = new ArrayList<>();
    private int page = 1, totalPage = 1;
    private ImageView imBg;
    private TextView tvName;
    private RoundedImageView riIcon;
    private RoundedImageView riIcon1;
    private RoundedImageView riIcon2;
    private TextView tvHint;
    private LinearLayout llMessage;
    private ImageView ivNoData;
    private TextView tvNoData;
    private View llNoData;
    private RecyclerView ryCircle;
    private AppBarLayout ns;
    private SmartRefreshLayout smart;
    private View viHead;
    private ImageView imFinish;
    private ImageView imAdd;
    private LinearLayout llAdd;
    private LinearLayout llTool;

    @Override
    protected boolean fitsSystemWindows() {
        return true;
    }

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
        return R.layout.activity_cie_broad_cel;
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

        EventBus.getDefault().register(this);

        ns.addOnOffsetChangedListener(new AppBarStateChangeListener() {
            @Override
            public void onStateChanged(AppBarLayout appBarLayout, State state) {
                if (state == State.COLLAPSED) {
                    llTool.setBackgroundColor(Color.parseColor("#FFFFFF"));
                } else {
                    llTool.setBackgroundColor(Color.parseColor("#00000000"));
                }
                if (state == State.IDLING) {
                    ELogUtilKt.eLogD(APP_TAG, "========State.IDLING");
                    for (int j = 0; j < listBeans.size(); j++) {
                        listBeans.get(j).isChecked = false;
                    }
                    ciecelAdapter.notifyDataSetChanged();
                }
            }
        });

        ciecelAdapter = new CiecelBroadAdapter(this, listBeans);
        ryCircle.setLayoutManager(new LinearLayoutManager(this));
        ryCircle.setAdapter(ciecelAdapter);
        ciecelAdapter.setOnItemClickListener(new CiecelBroadAdapter.OnItemClickListener() {
            @Override
            public void OnItemClickListener(int position, String content) {
                switch (content) {
                    case "详情":
                        break;
                    case "更多":
                        boolean isChecked = listBeans.get(position).isChecked;
                        for (int i = 0; i < listBeans.size(); i++) {
                            listBeans.get(i).isChecked = false;
                        }
                        listBeans.get(position).isChecked = !isChecked;
                        ciecelAdapter.notifyDataSetChanged();
                        break;
                    case "删除":
                        new NormalBroadDialog(CiecelBroadActivity.this, getString(R.string.温馨提示), "确认删除该条动态？", "取消", "确定", true)
                                .setOnButtonClickListener(new NormalBroadDialog.OnButtonClick() {
                                    @Override
                                    public void OnRightClick() {
                                        delQuan(listBeans.get(position).id, position);
                                    }

                                    @Override
                                    public void OnLeftClick() {
                                    }
                                }).show();

                        break;
                    case "点赞":
                        if (listBeans.get(position).isDz.equals("0")) {
                            quanLike(listBeans.get(position).id, "1", position);
                        } else {
                            quanLike(listBeans.get(position).id, "0", position);
                        }
                        break;
                    case "评论":
                        for (int j = 0; j < listBeans.size(); j++) {
                            listBeans.get(j).isChecked = false;
                        }
                        ciecelAdapter.notifyDataSetChanged();
                        new InputDialogBroadFra().setShaixuanClick("", new InputDialogBroadFra.OnShaixuanClick() {
                            @Override
                            public void onShaixuanClick(String content) {
                                quanComm(listBeans.get(position).id, "", content, position);
                            }
                        }).show(getSupportFragmentManager(), "");
                        break;
                    case "地址":
                        break;
                    case "视频":
                        Intent intent = new Intent(CiecelBroadActivity.this, VideoPlayOnlyBroadActivity.class);
                        intent.putExtra("video", listBeans.get(position).video);
                        intent.putExtra("videoImage", listBeans.get(position).video);
                        startActivity(intent);
                        break;
                    case "主页":
                        Intent intent1 = new Intent(CiecelBroadActivity.this, MyCircleBroadActivity.class);
                        intent1.putExtra("id", listBeans.get(position).userId);
                        startActivity(intent1);
                        break;

                }
            }

            @Override
            public void OnItemClickListener(int position, int firstPosition, String content) {
                switch (content) {
                    case "回复":
                        new InputDialogBroadFra().setShaixuanClick(listBeans.get(position).commList.get(firstPosition).name, new InputDialogBroadFra.OnShaixuanClick() {
                            @Override
                            public void onShaixuanClick(String content) {
                                quanComm(listBeans.get(position).id, listBeans.get(position).commList.get(firstPosition).id, content, position);
                            }
                        }).show(getSupportFragmentManager(), "");
                        break;
                    case "主页":
//                        Intent intent1 = new Intent(CiecelBroadActivity.this, MyCircleBroadActivity.class);
//                        intent1.putExtra("id",listBeans.get(position).commList.get(firstPosition).userId);
//                        startActivity(intent1);
                        break;
                }
            }

            @Override
            public void OnItemClickListener(int position, String type, String content) {
                for (int i = 0; i < listBeans.get(position).likeList.size(); i++) {
                    if (!StringBroadUtil.isEmpty(listBeans.get(position).likeList.get(i).name)) {
                        if (content.contains(listBeans.get(position).likeList.get(i).name)) {
                            Intent intent1 = new Intent(CiecelBroadActivity.this, MyCircleBroadActivity.class);
                            intent1.putExtra("id", listBeans.get(position).likeList.get(i).id);
                            startActivity(intent1);
                        }
                    }
                }
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
                myQuanList();
            }

            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                page = 1;
                myQuanList();
                refreshLayout.setNoMoreData(false);
            }
        });


        myQuanList();

        imFinish.setOnClickListener(this);
        llAdd.setOnClickListener(this);
        llMessage.setOnClickListener(this);
        riIcon.setOnClickListener(this);
        imBg.setOnClickListener(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = false)
    public void senmessage(EventBusBroadBean eventBusBean) {//登录成功
        if (eventBusBean.msg.equals("发布成功")) {
            page = 1;
            myQuanList();
        }
    }

    @Override
    public void onClick(View view) {
        Intent intent;
        if (view.getId() == R.id.imFinish) {
            finish();
        } else if (view.getId() == R.id.llAdd) {
            intent = new Intent(this, PushBroadActivity.class);
            startActivity(intent);
        } else if (view.getId() == R.id.llMessage) {
            intent = new Intent(this, CircelMessageBroadActivity.class);
            startActivity(intent);
        } else if (view.getId() == R.id.riIcon) {
            intent = new Intent(this, MyCircleBroadActivity.class);
            intent.putExtra("id", SharePrefBroadBroadUtil.getString(this, AppBroadConsts.UID, ""));
            startActivity(intent);
        } else if (view.getId() == R.id.imBg) {
            intent = new Intent(this, ChatBgBroadActivity.class);
            intent.putExtra("type", "0");
            startActivity(intent);
        }
    }

    /**
     * 评论朋友圈
     */
    private void quanComm(String id, String commId, String info, int position) {
        Map<String, Object> param = new HashMap<>();
        param.put("id", id);
        param.put("commId", commId);
        param.put("info", info);
        OkHttpBroadHelper.getInstance().post_json(this, BroadUrl.quanComm, param, new BaseBroadCallback<ResultBroadBean>() {
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
                CircleCommListBroadBean circleCommListBean = new CircleCommListBroadBean();
                circleCommListBean.id = resultBean.data.id;
                circleCommListBean.icon = resultBean.data.icon;
                circleCommListBean.name = resultBean.data.name;
                circleCommListBean.info = resultBean.data.info;
                circleCommListBean.huifu = resultBean.data.huifu;
                circleCommListBean.time = resultBean.data.time;
                listBeans.get(position).commList.add(0, circleCommListBean);
                ciecelAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Response response, int code, Exception e) {
            }
        });
    }

    /**
     * 删除朋友圈
     */
    private void delQuan(String id, int position) {
        Map<String, Object> param = new HashMap<>();
        param.put("id", id);
        OkHttpBroadHelper.getInstance().post_json(this, BroadUrl.delQuan, param, new BaseBroadCallback<ResultBroadBean>() {
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
                listBeans.remove(position);
                ciecelAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Response response, int code, Exception e) {
            }
        });
    }

    /**
     * 点赞朋友圈
     */
    private void quanLike(String id, String type, int position) {
        Map<String, Object> param = new HashMap<>();
        param.put("id", id);
        param.put("type", type);
        OkHttpBroadHelper.getInstance().post_json(this, BroadUrl.quanLike, param, new BaseBroadCallback<ResultBroadBean>() {
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
                listBeans.get(position).isDz = type;
                listBeans.get(position).dzNames = resultBean.dzNames;
                listBeans.get(position).likeList = resultBean.likeList;
                for (int j = 0; j < listBeans.size(); j++) {
                    listBeans.get(j).isChecked = false;
                }
                ciecelAdapter.notifyDataSetChanged();

            }

            @Override
            public void onError(Response response, int code, Exception e) {
            }
        });
    }

    /**
     * 我的朋友动态
     */
    private void myQuanList() {
        Map<String, Object> param = new HashMap<>();
        param.put("pageNo", page);
        param.put("pageSize", "10");
        OkHttpBroadHelper.getInstance().post_json(this, BroadUrl.myQuanList, param, new BaseBroadCallback<ResultBroadBean>() {
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
                ciecelAdapter.notifyDataSetChanged();

                if (listBeans.size() == 0) {
                    llNoData.setVisibility(View.VISIBLE);
                } else {
                    llNoData.setVisibility(View.GONE);
                }
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
                if (!StringBroadUtil.isEmpty(resultBean.data.img))
                    GlideBroadUtil.setImag(CiecelBroadActivity.this, resultBean.data.img, imBg);
                GlideBroadUtil.setHeadImag(CiecelBroadActivity.this, resultBean.data.icon, riIcon);
                tvName.setText(resultBean.data.name);
                if (StringBroadUtil.isEmpty(resultBean.data.hdIcon)) {
                    llMessage.setVisibility(View.GONE);
                } else {
                    llMessage.setVisibility(View.VISIBLE);
                    GlideBroadUtil.setHeadImag(CiecelBroadActivity.this, resultBean.data.hdIcon, riIcon1);
                    tvHint.setText(resultBean.data.hudong);
                }
            }

            @Override
            public void onError(Response response, int code, Exception e) {
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        userInfo();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private void initViews() {
        imBg = findViewById(R.id.imBg);
        tvName = findViewById(R.id.tvName);
        riIcon = findViewById(R.id.riIcon);
        riIcon1 = findViewById(R.id.riIcon1);
        riIcon2 = findViewById(R.id.riIcon2);
        tvHint = findViewById(R.id.tvHint);
        llMessage = findViewById(R.id.llMessage);
        ivNoData = findViewById(R.id.ivNoData);
        tvNoData = findViewById(R.id.tvNoData);
        llNoData = findViewById(R.id.llNoData);
        ryCircle = findViewById(R.id.ryCircle);
        ns = findViewById(R.id.ns);
        smart = findViewById(R.id.smart);
        viHead = findViewById(R.id.viHead);
        imFinish = findViewById(R.id.imFinish);
        imAdd = findViewById(R.id.imAdd);
        llAdd = findViewById(R.id.llAdd);
        llTool = findViewById(R.id.llTool);
    }
}