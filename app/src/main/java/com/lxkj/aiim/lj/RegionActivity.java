package com.lxkj.aiim.lj;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cboda.intelligent.R;
import com.lxkj.aiim.base.BaseSimpleActivity;
import com.lxkj.aiim.lj.adapter.BrandAdapter;
import com.lxkj.aiim.lj.adapter.LetterAdapter;
import com.lxkj.aiim.lj.bean.DataListBean;
import com.lxkj.aiim.lj.bean.ResultBean;
import com.lxkj.aiim.lj.http.OkHttpHelper;
import com.lxkj.aiim.lj.http.SpotsCallBack;
import com.lxkj.aiim.lj.http.Url;
import com.lxkj.aiim.lj.utils.StringUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Response;

public class RegionActivity extends BaseSimpleActivity {


    private BrandAdapter brandAdapter;
    private List<DataListBean> listBeans = new ArrayList<>();
    private List<DataListBean> copylistBeans = new ArrayList<>();

    private LetterAdapter letterAdapter;
    private List<String> letterList = new ArrayList<>();
    private EditText etSearch;
    private RecyclerView ryBrand;
    private RecyclerView ryLatter;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_region;
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
                    if (StringUtil.isEmpty(etSearch.getText().toString())) {
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

        brandAdapter = new BrandAdapter(this, listBeans);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        ryBrand.setLayoutManager(linearLayoutManager);
        ryBrand.setAdapter(brandAdapter);
        brandAdapter.setOnItemClickListener(new BrandAdapter.OnItemClickListener() {
            @Override
            public void OnItemClickListener(int position) {
                Intent intent = new Intent();
                intent.putExtra("no", listBeans.get(position).no);
                setResult(333, intent);
                finish();
            }
        });

        letterAdapter = new LetterAdapter(this, letterList);
        ryLatter.setLayoutManager(new LinearLayoutManager(this));
        ryLatter.setAdapter(letterAdapter);
        letterAdapter.setOnItemClickListener(new LetterAdapter.OnItemClickListener() {
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
        OkHttpHelper.getInstance().post_json(this, Url.quhaoList, param, new SpotsCallBack<ResultBean>(this) {
            @Override
            public void onSuccess(Response response, ResultBean resultBean) {
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