package com.freddy.chat.collect;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ThreadUtils;
import com.cboda.baselibrary.toast.ToastBroadUtils;
import com.cboda.baselibrary.utils.JavaBroadUtil;
import com.cboda.baselibrary.utils.ViewExpandUtilKt;
import com.cboda.intelligent.R;
import com.cboda.intelligent.base.BaseSimple2BroadActivity;
import com.cboda.intelligent.bean.ResultBroadBean;
import com.cboda.intelligent.dialog.DelCollPop;
import com.cboda.intelligent.net.BroadUrl;
import com.cboda.intelligent.utils.BaseBroadCallback;
import com.cboda.intelligent.utils.OkHttpBroadHelper;
import com.cboda.select.picture.entity.DataListBroadBean;
import com.freddy.chat.im.MessageProcessor;
import com.freddy.im.bean.AppMessage;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Request;
import okhttp3.Response;

/**
 * 自研 IM 收藏夹：列表展示、去收藏、点击/转发发送走 {@link MessageProcessor}。
 * <p>
 * 复制自 {@link com.cboda.intelligent.activity.MyCollectBroadActivity}，收藏上游见
 * {@link com.cboda.intelligent.holder.MyImManagerBroadHolder}（{@code /logColl}），
 * 去收藏见 {@link BroadUrl#DEL_COLL}（{@code delColl}）。
 */
public class MyCollectActivity extends BaseSimple2BroadActivity {

    private MyCollectAdapter myCollectAdapter;
    private final List<DataListBroadBean> listBeans = new ArrayList<>();
    private int page = 1;
    private int totalPage = 1;
    private LinearLayout llNoData;
    private RecyclerView ryCollect;
    private SmartRefreshLayout smart;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_my_broad_collect;
    }

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();
        getToolbarTitle().setText(getResources().getString(com.cboda.baselibrary.R.string.collect));
        setRootBgColor(com.cboda.chatui.R.color.white);

        myCollectAdapter = new MyCollectAdapter(this, listBeans);
        ryCollect.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
        ryCollect.setAdapter(myCollectAdapter);

        myCollectAdapter.setOnItemClickListener(new MyCollectAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                handleSendToCurrentChat(position);
            }

            @Override
            public void onItemChildClick(View view, int position) {
                handleForward(position);
            }

            @Override
            public void onItemDelClick(View view, int position) {
                handleDeleteCollect(view, position);
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
                collList();
            }

            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                page = 1;
                collList();
                refreshLayout.setNoMoreData(false);
            }
        });

        collList();
    }

    private void handleSendToCurrentChat(int position) {
        if (position < 0 || position >= listBeans.size()) {
            return;
        }
        DataListBroadBean bean = listBeans.get(position);
        if (bean.info == null || "null".equals(bean.info)) {
            ToastBroadUtils.show("服务器查询数据失败" + bean.info);
            return;
        }
        CollectSessionTarget target = CollectSessionTarget.fromIntent(getIntent());
        if (target == null || !target.isValid()) {
            return;
        }
        if (!CollectContentParser.isSupportedSendType(bean.type)) {
            ToastBroadUtils.show("暂不支持的分享类型");
            return;
        }
        if (CollectContentParser.TYPE_TEXT.equals(bean.type)) {
            String plain = CollectContentParser.resolveDisplayText(bean.type, bean.info);
            String day = JavaBroadUtil.containsLinkReturnDay(plain);
            if (!TextUtils.isEmpty(day)) {
                ToastBroadUtils.show(String.format(
                        ViewExpandUtilKt.getAppContext(null).getString(
                                com.cboda.baselibrary.R.string.registered_less_than_days), day));
                return;
            }
        }
        AppMessage outbound = CollectMessageFactory.buildOutboundMessage(
                target, bean.type, bean.info);
        if (outbound == null) {
            ToastBroadUtils.show("消息构造失败");
            return;
        }
        MessageProcessor.getInstance().sendMsg(outbound, () -> runOnUiThread(() -> {
            ToastBroadUtils.show("转发成功");
            finish();
        }), true);
    }

    private void handleForward(int position) {
        if (position < 0 || position >= listBeans.size()) {
            return;
        }
        DataListBroadBean bean = listBeans.get(position);
        if (bean.info == null || "null".equals(bean.info)) {
            ToastBroadUtils.show("服务器查询数据失败" + bean.info);
            return;
        }
        if (!CollectContentParser.isSupportedSendType(bean.type)) {
            ToastBroadUtils.show("暂不支持的转发类型");
            return;
        }
        if (CollectContentParser.TYPE_TEXT.equals(bean.type)) {
            String plain = CollectContentParser.resolveDisplayText(bean.type, bean.info);
            String day = JavaBroadUtil.containsLinkReturnDay(plain);
            if (!TextUtils.isEmpty(day)) {
                ToastBroadUtils.show(String.format(
                        ViewExpandUtilKt.getAppContext(null).getString(
                                com.cboda.baselibrary.R.string.registered_less_than_days), day));
                return;
            }
        }
        AppMessage outbound = CollectMessageFactory.buildForwardableMessage(
                bean.type, bean.info);
        if (outbound == null) {
            ToastBroadUtils.show("消息构造失败");
            return;
        }
        CollectForwardHelper.startForward(this, outbound);
    }

    /**
     * 去收藏：确认弹窗后调用 {@code POST delColl}，成功后本地移除条目。
     */
    private void handleDeleteCollect(View anchor, int position) {
        DelCollPop delCollPop = new DelCollPop(this);
        delCollPop.backInvoke = () -> {
            if (position < 0 || position >= listBeans.size()) {
                return null;
            }
            DataListBroadBean bean = listBeans.get(position);
            Map<String, Object> param = new HashMap<>();
            param.put("ids", bean.id);
            OkHttpBroadHelper.getInstance().post_json(
                    MyCollectActivity.this, BroadUrl.DEL_COLL, param,
                    new BaseBroadCallback<ResultBroadBean>() {
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
                            if (position < listBeans.size()) {
                                listBeans.remove(position);
                            }
                            ThreadUtils.runOnUiThread(() -> {
                                if (myCollectAdapter.getItemCount() > position && position >= 0) {
                                    myCollectAdapter.notifyItemRemoved(position);
                                } else {
                                    myCollectAdapter.notifyDataSetChanged();
                                }
                                updateEmptyState();
                            });
                        }

                        @Override
                        public void onError(Response response, int code, Exception e) {
                        }
                    });
            return null;
        };
        delCollPop.showPopupWindow();
    }

    private void collList() {
        Map<String, Object> param = new HashMap<>();
        param.put("pageNo", page);
        param.put("pageSize", "10");
        OkHttpBroadHelper.getInstance().post_json(this, BroadUrl.collList, param,
                new BaseBroadCallback<ResultBroadBean>() {
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
                        if (resultBean.totalPage != null) {
                            totalPage = Integer.parseInt(resultBean.totalPage);
                        }
                        smart.finishLoadMore();
                        smart.finishRefresh();
                        if (page == 1) {
                            listBeans.clear();
                        }
                        if (resultBean.dataList != null) {
                            for (DataListBroadBean bean : resultBean.dataList) {
                                if (!CollectContentParser.TYPE_VOICE.equals(bean.type)
                                        && CollectContentParser.isJsonObjectInfo(bean.info)) {
                                    listBeans.add(bean);
                                }
                            }
                        }
                        ThreadUtils.runOnUiThread(() -> myCollectAdapter.notifyDataSetChanged());
                        updateEmptyState();
                    }

                    @Override
                    public void onError(Response response, int code, Exception e) {
                    }
                });
    }

    private void updateEmptyState() {
        llNoData.setVisibility(listBeans.isEmpty() ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public boolean showTransparentTopTitleLayout() {
        return true;
    }

    private void initViews() {
        llNoData = findViewById(R.id.llNoData);
        ryCollect = findViewById(R.id.ryCollect);
        smart = findViewById(R.id.smart);
    }
}
