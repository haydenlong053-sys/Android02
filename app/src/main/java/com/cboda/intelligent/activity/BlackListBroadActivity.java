package com.cboda.intelligent.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.viewbinding.ViewBinding;

import com.cboda.intelligent.R;
import com.cboda.intelligent.adapter.BlackListBroadAdapter;
import com.cboda.intelligent.base.BaseSimple2BroadActivity;
import com.cboda.intelligent.bean.ResultBroadBean;
import com.cboda.intelligent.databinding.ActivityBlackBroadListBinding;
import com.cboda.intelligent.net.BroadUrl;
import com.cboda.intelligent.utils.BaseBroadCallback;
import com.cboda.intelligent.utils.OkHttpBroadHelper;
import com.cboda.intelligent.utils.ToastBroadUtil;
import com.cboda.select.picture.entity.DataListBroadBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Request;
import okhttp3.Response;

/**
 * 通讯录黑名单。
 * 列表：POST im/friend/blackList（content = FriendItemVO[]）
 * 解除：POST im/friend/black（targetUserId + type=0）
 */
public class BlackListBroadActivity extends BaseSimple2BroadActivity {

    private BlackListBroadAdapter blacklistAdapter;
    private List<DataListBroadBean> listBeans = new ArrayList<>();
    private ActivityBlackBroadListBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRootBgColor(R.color.white);
        mBinding = (ActivityBlackBroadListBinding) getBinding();
        if (getToolbarTitle() != null) {
            getToolbarTitle().setText(getResources().getString(R.string.通讯录黑名单));
        }

        blacklistAdapter = new BlackListBroadAdapter(this, listBeans);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mBinding.ryBlack.setLayoutManager(linearLayoutManager);
        mBinding.ryBlack.setAdapter(blacklistAdapter);
        blacklistAdapter.setOnItemClickListener(new BlackListBroadAdapter.OnItemClickListener() {
            @Override
            public void OnItemClickListener(int position) {
                if (position < 0 || position >= listBeans.size()) {
                    return;
                }
                String userId = listBeans.get(position).userId;
                if (TextUtils.isEmpty(userId)) {
                    return;
                }
                unblack(userId);
            }
        });

        fetchBlackList();
    }

    @Override
    public boolean showBgTopTitleLayout() {
        return true;
    }

    /**
     * 解除拉黑：POST im/friend/black，type=0。
     */
    private void unblack(String uid) {
        Map<String, Object> param = new HashMap<>();
        param.put("targetUserId", uid);
        param.put("type", "0");
        OkHttpBroadHelper.getInstance().post_json(this, BroadUrl.black, param, new BaseBroadCallback<ResultBroadBean>() {
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
                if (resultBean != null && !TextUtils.isEmpty(resultBean.msg)) {
                    ToastBroadUtil.show(resultBean.msg);
                }
                fetchBlackList();
            }

            @Override
            public void onError(Response response, int code, Exception e) {
            }
        });
    }

    /**
     * 黑名单列表：POST im/friend/blackList，body 可省略（可选 keyword）。
     */
    private void fetchBlackList() {
        Map<String, Object> param = new HashMap<>();
        OkHttpBroadHelper.getInstance().post_json(this, BroadUrl.blackList, param, new BaseBroadCallback<ResultBroadBean>() {
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
                if (resultBean != null && resultBean.dataList != null) {
                    listBeans.addAll(resultBean.dataList);
                }
                blacklistAdapter.notifyDataSetChanged();
                mBinding.llNoData.setVisibility(listBeans.isEmpty() ? View.VISIBLE : View.INVISIBLE);
            }

            @Override
            public void onError(Response response, int code, Exception e) {
            }
        });
    }

    @Override
    protected boolean useViewBinding() {
        return true;
    }

    @Nullable
    @Override
    protected Class<? extends ViewBinding> getViewBindingClass() {
        return ActivityBlackBroadListBinding.class;
    }
}
