package com.cboda.intelligent.activity;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.viewbinding.ViewBinding;

import com.cboda.intelligent.R;
import com.cboda.intelligent.base.BaseSimpleBroadActivity;
import com.cboda.intelligent.adapter.BlackListBroadAdapter;
import com.cboda.intelligent.bean.DataListBroadBean;
import com.cboda.intelligent.bean.ResultBroadBean;
import com.cboda.intelligent.databinding.ActivityBlackBroadListBinding;
import com.cboda.intelligent.utils.BaseBroadCallback;
import com.cboda.intelligent.utils.OkHttpBroadHelper;
import com.cboda.intelligent.net.BroadUrl;
import com.cboda.intelligent.utils.ToastBroadUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Request;
import okhttp3.Response;

public class BlackListBroadActivity extends BaseSimpleBroadActivity {

    private BlackListBroadAdapter blacklistAdapter;
    private List<DataListBroadBean> listBeans = new ArrayList<>();
    private ActivityBlackBroadListBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = (ActivityBlackBroadListBinding) getBinding();
        getToolbarTitle().setText(getResources().getString(R.string.通讯录黑名单));

        blacklistAdapter = new BlackListBroadAdapter(this, listBeans);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mBinding.ryBlack.setLayoutManager(linearLayoutManager);
        mBinding.ryBlack.setAdapter(blacklistAdapter);
        blacklistAdapter.setOnItemClickListener(new BlackListBroadAdapter.OnItemClickListener() {
            @Override
            public void OnItemClickListener(int position) {
                black(listBeans.get(position).userId);
            }
        });

        blackList();
    }

    /**
     * 拉黑、解除拉黑好友
     */
    private void black(String uid) {
        Map<String, Object> param = new HashMap<>();
        param.put("uid", uid);
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
                ToastBroadUtil.show(resultBean.msg);
                blackList();
            }

            @Override
            public void onError(Response response, int code, Exception e) {
            }
        });
    }

    /**
     * 黑名单列表
     */
    private void blackList() {
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
                listBeans.addAll(resultBean.dataList);
                blacklistAdapter.notifyDataSetChanged();

                if (listBeans.size() == 0) {
                    mBinding.llNoData.setVisibility(View.VISIBLE);
                } else {
                    mBinding.llNoData.setVisibility(View.INVISIBLE);
                }
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

//    @Override
//    protected int getLayoutId() {
//        return R.layout.activity_black_list;
//    }
}