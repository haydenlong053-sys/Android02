package com.lxkj.aiim.lj;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.viewbinding.ViewBinding;

import com.cboda.intelligent.R;
import com.lxkj.aiim.base.BaseSimpleActivity;
import com.cboda.intelligent.databinding.ActivityBlackListBinding;
import com.lxkj.aiim.lj.adapter.BlackListAdapter;
import com.lxkj.aiim.lj.bean.DataListBean;
import com.lxkj.aiim.lj.bean.ResultBean;
import com.lxkj.aiim.lj.http.BaseCallback;
import com.lxkj.aiim.lj.http.OkHttpHelper;
import com.lxkj.aiim.lj.http.Url;
import com.lxkj.aiim.lj.utils.ToastUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Request;
import okhttp3.Response;

public class BlackListActivity extends BaseSimpleActivity {

    private BlackListAdapter blacklistAdapter;
    private List<DataListBean> listBeans = new ArrayList<>();
    private ActivityBlackListBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = (ActivityBlackListBinding) getBinding();
        getToolbarTitle().setText(getResources().getString(R.string.通讯录黑名单));

        blacklistAdapter = new BlackListAdapter(this, listBeans);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mBinding.ryBlack.setLayoutManager(linearLayoutManager);
        mBinding.ryBlack.setAdapter(blacklistAdapter);
        blacklistAdapter.setOnItemClickListener(new BlackListAdapter.OnItemClickListener() {
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
        OkHttpHelper.getInstance().post_json(this, Url.black, param, new BaseCallback<ResultBean>() {
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
                ToastUtil.show(resultBean.msg);
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
        OkHttpHelper.getInstance().post_json(this, Url.blackList, param, new BaseCallback<ResultBean>() {
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
        return ActivityBlackListBinding.class;
    }

//    @Override
//    protected int getLayoutId() {
//        return R.layout.activity_black_list;
//    }
}