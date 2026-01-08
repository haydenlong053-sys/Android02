package com.cboda.intelligent.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cboda.intelligent.R;
import com.cboda.intelligent.base.BaseSimpleBroadActivity;
import com.cboda.intelligent.adapter.EmjoyBroadAdapter;
import com.cboda.intelligent.bean.DataListBroadBean;
import com.cboda.intelligent.bean.ResultBroadBean;
import com.cboda.intelligent.utils.BaseBroadCallback;
import com.cboda.intelligent.utils.OkHttpBroadHelper;
import com.cboda.intelligent.utils.SpotsCallBroadBack;
import com.cboda.intelligent.net.BroadUrl;
import com.cboda.intelligent.utils.StringBroadUtil;
import com.cboda.intelligent.utils.ToastBroadUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Request;
import okhttp3.Response;

public class EmjoyBroadActivity extends BaseSimpleBroadActivity {

    private EmjoyBroadAdapter emjoyAdapter;
    private List<DataListBroadBean> listBeans = new ArrayList<>();
    private boolean isShow = false;
    private TextView tvDelate;
    private ImageView ivNoData;
    private TextView tvNoData;
    private LinearLayout llNoData;
    private RecyclerView ryEnjoy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();
        getToolbarTitle().setText(getResources().getString(R.string.表情));
        getRightView().setText(getResources().getString(R.string.管理));

        getRightView().setOnClickListener(v -> {
            if (isShow) {
                isShow = false;
                getRightView().setText(getResources().getString(R.string.管理));
                tvDelate.setVisibility(View.GONE);
                for (int i = 0; i < listBeans.size(); i++) {
                    listBeans.get(i).isChecked = false;
                    listBeans.get(i).isShowChecked = false;
                }
            } else {
                isShow = true;
                getRightView().setText(getResources().getString(R.string.完成));
                tvDelate.setVisibility(View.VISIBLE);
                for (int i = 0; i < listBeans.size(); i++) {
                    listBeans.get(i).isChecked = false;
                    listBeans.get(i).isShowChecked = true;
                }
            }

            emjoyAdapter.notifyDataSetChanged();
        });

        emjoyAdapter = new EmjoyBroadAdapter(this, listBeans);
        ryEnjoy.setLayoutManager(new GridLayoutManager(this, 4));
        ryEnjoy.setAdapter(emjoyAdapter);
        emjoyAdapter.setOnItemClickListener(new EmjoyBroadAdapter.OnItemClickListener() {
            @Override
            public void OnItemClickListener(int position) {
                listBeans.get(position).isChecked = !listBeans.get(position).isChecked;
                emjoyAdapter.notifyDataSetChanged();
            }
        });

        tvDelate.setOnClickListener(v -> {
            String ids = "";
            for (int i = 0; i < listBeans.size(); i++) {
                if (listBeans.get(i).isChecked) {
                    ids += listBeans.get(i).id + "|";
                }
            }
            if (StringBroadUtil.isEmpty(ids)) {
                ToastBroadUtil.show(getString(R.string.请选择需要删除的表情));
                return;
            }
            delEmoji(ids.substring(0, ids.length() - 1));
        });

        emojiList();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_em_broad_joy;
    }

    /**
     * 删除表情包
     */
    private void delEmoji(String ids) {
        Map<String, Object> param = new HashMap<>();
        param.put("ids", ids);
        OkHttpBroadHelper.getInstance().post_json(this, BroadUrl.delEmoji, param, new SpotsCallBroadBack<ResultBroadBean>(this) {
            @Override
            public void onSuccess(Response response, ResultBroadBean resultBean) {
                ToastBroadUtil.show(resultBean.msg);
                emojiList();
            }

            @Override
            public void onError(Response response, int code, Exception e) {
            }
        });
    }

    /**
     * 我的表情
     */
    private void emojiList() {
        Map<String, Object> param = new HashMap<>();
        OkHttpBroadHelper.getInstance().post_json(this, BroadUrl.emojiList, param, new BaseBroadCallback<ResultBroadBean>() {
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
                for (int i = 0; i < listBeans.size(); i++) {
                    listBeans.get(i).isShowChecked = isShow;
                }
                emjoyAdapter.notifyDataSetChanged();
                if (listBeans.size() > 0) {
                    llNoData.setVisibility(View.GONE);
                } else {
                    llNoData.setVisibility(View.VISIBLE);

                    isShow = false;
                    getRightView().setText(getResources().getString(R.string.管理));
                    tvDelate.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(Response response, int code, Exception e) {
            }
        });
    }

    private void initViews() {
        tvDelate = findViewById(R.id.tvDelate);
        ivNoData = findViewById(R.id.ivNoData);
        tvNoData = findViewById(R.id.tvNoData);
        llNoData = findViewById(R.id.llNoData);
        ryEnjoy = findViewById(R.id.ryEnjoy);
    }
}