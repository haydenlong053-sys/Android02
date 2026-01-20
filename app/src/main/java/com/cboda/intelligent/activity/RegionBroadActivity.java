package com.cboda.intelligent.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cboda.intelligent.R;
import com.cboda.intelligent.base.BaseSimpleBroadActivity;
import com.cboda.intelligent.adapter.BrandBroadAdapter;
import com.cboda.intelligent.adapter.LetterBroadAdapter;
import com.cboda.select.picture.entity.DataListBroadBean;
import com.cboda.intelligent.bean.ResultBroadBean;
import com.cboda.intelligent.utils.OkHttpBroadHelper;
import com.cboda.intelligent.utils.SpotsCallBroadBack;
import com.cboda.intelligent.net.BroadUrl;
import com.cboda.intelligent.utils.StringBroadUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Response;

public class RegionBroadActivity extends BaseSimpleBroadActivity {


    private BrandBroadAdapter brandAdapter;
    private List<DataListBroadBean> listBeans = new ArrayList<>();
    private List<DataListBroadBean> copylistBeans = new ArrayList<>();

    private LetterBroadAdapter letterAdapter;
    private List<String> letterList = new ArrayList<>();
    private EditText etSearch;
    private RecyclerView ryBrand;
    private RecyclerView ryLatter;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_broad_region;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();
        getToolbarTitle().setText(getResources().getString(R.string.选择地区));

        etSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int type, KeyEvent keyEvent) {
                if (type == EditorInfo.IME_ACTION_SEARCH) {
                    if (StringBroadUtil.isEmpty(etSearch.getText().toString())) {
                        listBeans.clear();
                        listBeans.addAll(copylistBeans);
                        brandAdapter.notifyDataSetChanged();
                    } else {
                        listBeans.clear();
                        for (int i = 0; i < copylistBeans.size(); i++) {
                            if (copylistBeans.get(i).contry.contains(etSearch.getText().toString())) {
                                listBeans.add(copylistBeans.get(i));
                            }
                        }
                        brandAdapter.notifyDataSetChanged();
                    }
                    return true;
                }
                return false;
            }
        });

        brandAdapter = new BrandBroadAdapter(this, listBeans);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        ryBrand.setLayoutManager(linearLayoutManager);
        ryBrand.setAdapter(brandAdapter);
        brandAdapter.setOnItemClickListener(new BrandBroadAdapter.OnItemClickListener() {
            @Override
            public void OnItemClickListener(int position) {
                Intent intent = new Intent();
                intent.putExtra("no", listBeans.get(position).no);
                setResult(333, intent);
                finish();
            }
        });

        letterAdapter = new LetterBroadAdapter(this, letterList);
        ryLatter.setLayoutManager(new LinearLayoutManager(this));
        ryLatter.setAdapter(letterAdapter);
        letterAdapter.setOnItemClickListener(new LetterBroadAdapter.OnItemClickListener() {
            @Override
            public void OnItemClickListener(int position) {
//                for (int i = 0; i < listBeans.size(); i++) {
//                    if (letterList.get(position).equals(listBeans.get(i).letter)){
//                        linearLayoutManager.scrollToPositionWithOffset(i, 0);
//                        break;
//                    }
//                }
            }
        });

        quhaoList();
    }

    /**
     * 手机区号列表
     */
    private void quhaoList() {
        Map<String, Object> param = new HashMap<>();
        OkHttpBroadHelper.getInstance().post_json(this, BroadUrl.quhaoList, param, new SpotsCallBroadBack<ResultBroadBean>(this) {
            @Override
            public void onSuccess(Response response, ResultBroadBean resultBean) {
                listBeans.clear();
                copylistBeans.clear();
                listBeans.addAll(resultBean.dataList);
                copylistBeans.addAll(resultBean.dataList);
                brandAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Response response, int code, Exception e) {
            }
        });
    }

    private void initViews() {
        etSearch = findViewById(R.id.etSearch);
        ryBrand = findViewById(R.id.ryBrand);
        ryLatter = findViewById(R.id.ryLatter);
    }
}