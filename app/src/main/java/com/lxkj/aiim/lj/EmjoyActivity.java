package com.lxkj.aiim.lj;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cboda.intelligent.R;
import com.lxkj.aiim.base.BaseSimpleActivity;
import com.lxkj.aiim.lj.adapter.EmjoyAdapter;
import com.lxkj.aiim.lj.bean.DataListBean;
import com.lxkj.aiim.lj.bean.ResultBean;
import com.lxkj.aiim.lj.http.BaseCallback;
import com.lxkj.aiim.lj.http.OkHttpHelper;
import com.lxkj.aiim.lj.http.SpotsCallBack;
import com.lxkj.aiim.lj.http.Url;
import com.lxkj.aiim.lj.utils.StringUtil;
import com.lxkj.aiim.lj.utils.ToastUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Request;
import okhttp3.Response;

public class EmjoyActivity extends BaseSimpleActivity {

    private EmjoyAdapter emjoyAdapter;
    private List<DataListBean> listBeans = new ArrayList<>();
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

        emjoyAdapter = new EmjoyAdapter(this, listBeans);
        ryEnjoy.setLayoutManager(new GridLayoutManager(this, 4));
        ryEnjoy.setAdapter(emjoyAdapter);
        emjoyAdapter.setOnItemClickListener(new EmjoyAdapter.OnItemClickListener() {
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
            if (StringUtil.isEmpty(ids)) {
                ToastUtil.show(getString(R.string.请选择需要删除的表情));
                return;
            }
            delEmoji(ids.substring(0, ids.length() - 1));
        });

        emojiList();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_emjoy;
    }

    /**
     * 删除表情包
     */
    private void delEmoji(String ids) {
        Map<String, Object> param = new HashMap<>();
        param.put("ids", ids);
        OkHttpHelper.getInstance().post_json(this, Url.delEmoji, param, new SpotsCallBack<ResultBean>(this) {
            @Override
            public void onSuccess(Response response, ResultBean resultBean) {
                ToastUtil.show(resultBean.msg);
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
        OkHttpHelper.getInstance().post_json(this, Url.emojiList, param, new BaseCallback<ResultBean>() {
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