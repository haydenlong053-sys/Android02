package com.lxkj.aiim.lj;

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
import com.lxkj.aiim.base.BaseSimpleActivity;
import com.lxkj.aiim.lj.adapter.CiecelAdapter;
import com.lxkj.aiim.lj.bean.CircleCommListBean;
import com.lxkj.aiim.lj.bean.DataListBean;
import com.lxkj.aiim.lj.bean.EventBusBean;
import com.lxkj.aiim.lj.bean.ResultBean;
import com.lxkj.aiim.lj.configuration.AppConsts;
import com.lxkj.aiim.lj.dialogfra.InputDialogFra;
import com.lxkj.aiim.lj.dialogfra.NormalDialog;
import com.lxkj.aiim.lj.http.BaseCallback;
import com.lxkj.aiim.lj.http.OkHttpHelper;
import com.lxkj.aiim.lj.http.Url;
import com.lxkj.aiim.lj.utils.GlideUtil;
import com.lxkj.aiim.lj.utils.SharePrefBroadUtil;
import com.lxkj.aiim.lj.utils.StringUtil;
import com.lxkj.aiim.lj.view.MyScrollView;
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

public class CiecelActivity extends BaseSimpleActivity implements View.OnClickListener {

    private CiecelAdapter ciecelAdapter;
    private List<DataListBean> listBeans = new ArrayList<>();
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
    private LinearLayout llNoData;
    private RecyclerView ryCircle;
    private MyScrollView ns;
    private SmartRefreshLayout smart;
    private View viHead;
    private ImageView imFinish;
    private ImageView imAdd;
    private LinearLayout llAdd;
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
        return R.layout.activity_ciecel;
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

        ns.setOnScrollChanged(new MyScrollView.OnScrollChanged() {
            @Override
            public void onScroll(int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (scrollY > 600) {
                    llTool.setBackgroundColor(Color.parseColor("#FFFFFF"));
                } else {
                    llTool.setBackgroundColor(Color.parseColor("#00000000"));
                }

                for (int j = 0; j < listBeans.size(); j++) {
                    listBeans.get(j).isChecked = false;
                }
                ciecelAdapter.notifyDataSetChanged();
            }
        });

        ciecelAdapter = new CiecelAdapter(this, listBeans);
        ryCircle.setLayoutManager(new LinearLayoutManager(this));
        ryCircle.setAdapter(ciecelAdapter);
        ciecelAdapter.setOnItemClickListener(new CiecelAdapter.OnItemClickListener() {
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
                        new NormalDialog(CiecelActivity.this, getString(R.string.温馨提示), "确认删除该条动态？", "取消", "确定", true)
                                .setOnButtonClickListener(new NormalDialog.OnButtonClick() {
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
                        new InputDialogFra().setShaixuanClick("", new InputDialogFra.OnShaixuanClick() {
                            @Override
                            public void onShaixuanClick(String content) {
                                quanComm(listBeans.get(position).id, "", content, position);
                            }
                        }).show(getSupportFragmentManager(), "");
                        break;
                    case "地址":
                        break;
                    case "视频":
                        Intent intent = new Intent(CiecelActivity.this, VideoPlayOnlyActivity.class);
                        intent.putExtra("video", listBeans.get(position).video);
                        intent.putExtra("videoImage", listBeans.get(position).video);
                        startActivity(intent);
                        break;
                    case "主页":
                        Intent intent1 = new Intent(CiecelActivity.this, MyCircleActivity.class);
                        intent1.putExtra("id", listBeans.get(position).userId);
                        startActivity(intent1);
                        break;

                }
            }

            @Override
            public void OnItemClickListener(int position, int firstPosition, String content) {
                switch (content) {
                    case "回复":
                        new InputDialogFra().setShaixuanClick(listBeans.get(position).commList.get(firstPosition).name, new InputDialogFra.OnShaixuanClick() {
                            @Override
                            public void onShaixuanClick(String content) {
                                quanComm(listBeans.get(position).id, listBeans.get(position).commList.get(firstPosition).id, content, position);
                            }
                        }).show(getSupportFragmentManager(), "");
                        break;
                    case "主页":
//                        Intent intent1 = new Intent(CiecelActivity.this, MyCircleActivity.class);
//                        intent1.putExtra("id",listBeans.get(position).commList.get(firstPosition).userId);
//                        startActivity(intent1);
                        break;
                }
            }

            @Override
            public void OnItemClickListener(int position, String type, String content) {
                for (int i = 0; i < listBeans.get(position).likeList.size(); i++) {
                    if (!StringUtil.isEmpty(listBeans.get(position).likeList.get(i).name)) {
                        if (content.contains(listBeans.get(position).likeList.get(i).name)) {
                            Intent intent1 = new Intent(CiecelActivity.this, MyCircleActivity.class);
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
    public void senmessage(EventBusBean eventBusBean) {//登录成功
        if (eventBusBean.msg.equals("发布成功")) {
            page = 1;
            myQuanList();
        }
    }

    @Override
    public void onClick(View view) {
        Intent intent;
        if (view.getId()==R.id.imFinish){
            finish();
        }else  if (view.getId() == R.id.llAdd){
            intent = new Intent(this, PushActivity.class);
            startActivity(intent);
        }else  if (view.getId() ==R.id.llMessage){
            intent = new Intent(this, CircelMessageActivity.class);
            startActivity(intent);
        }else  if (view.getId() ==R.id.riIcon){
            intent = new Intent(this, MyCircleActivity.class);
            intent.putExtra("id", SharePrefBroadUtil.getString(this, AppConsts.UID, ""));
            startActivity(intent);
        }else  if (view.getId() ==R.id.imBg){
            intent = new Intent(this, ChatBgActivity.class);
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
        OkHttpHelper.getInstance().post_json(this, Url.quanComm, param, new BaseCallback<ResultBean>() {
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
                CircleCommListBean circleCommListBean = new CircleCommListBean();
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
        OkHttpHelper.getInstance().post_json(this, Url.delQuan, param, new BaseCallback<ResultBean>() {
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
        OkHttpHelper.getInstance().post_json(this, Url.quanLike, param, new BaseCallback<ResultBean>() {
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
        OkHttpHelper.getInstance().post_json(this, Url.myQuanList, param, new BaseCallback<ResultBean>() {
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
                ciecelAdapter.notifyDataSetChanged();

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
                if (!StringUtil.isEmpty(resultBean.data.img))
                    GlideUtil.setImag(CiecelActivity.this, resultBean.data.img, imBg);
                GlideUtil.setHeadImag(CiecelActivity.this, resultBean.data.icon, riIcon);
                tvName.setText(resultBean.data.name);
                if (StringUtil.isEmpty(resultBean.data.hdIcon)) {
                    llMessage.setVisibility(View.GONE);
                } else {
                    llMessage.setVisibility(View.VISIBLE);
                    GlideUtil.setHeadImag(CiecelActivity.this, resultBean.data.hdIcon, riIcon1);
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