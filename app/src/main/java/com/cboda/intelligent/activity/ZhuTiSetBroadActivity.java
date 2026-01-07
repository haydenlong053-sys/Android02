package com.cboda.intelligent.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cboda.intelligent.MainBroadActivity;
import com.cboda.intelligent.R;
import com.cboda.intelligent.app.LiXinBroadApp;
import com.cboda.intelligent.base.BaseSimpleBroadActivity;
import com.cboda.intelligent.adapter.LanguageBroadAdapter;
import com.cboda.intelligent.bean.DataListBroadBean;
import com.cboda.intelligent.configuration.AppBroadConsts;
import com.cboda.intelligent.utils.SharePrefBroadBroadUtil;
import com.cboda.intelligent.utils.AppBroadUtils;
import com.cboda.baselibrary.utils.MMKVBroadUtils;

import java.util.ArrayList;
import java.util.List;


public class ZhuTiSetBroadActivity extends BaseSimpleBroadActivity {

    private LanguageBroadAdapter languageAdapter;
    private List<DataListBroadBean> listBeans = new ArrayList<>();
    private String language = "";
    private RecyclerView ryLangeuage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();
        getToolbarTitle().setText(getResources().getString(R.string.主题色));

//        Locale current = getResources().getConfiguration().locale;
//        language = current.getLanguage(); // 获取当前系统语言
//        Log.e("TAG", "当前系统语言："+ language );

        languageAdapter = new LanguageBroadAdapter(this, listBeans);
        ryLangeuage.setLayoutManager(new LinearLayoutManager(this));
        ryLangeuage.setAdapter(languageAdapter);
        languageAdapter.setOnItemClickListener(new LanguageBroadAdapter.OnItemClickListener() {
            @Override
            public void OnItemClickListener(int position) {
                for (int i = 0; i < listBeans.size(); i++) {
                    listBeans.get(i).isChecked = false;
                }
                listBeans.get(position).isChecked = true;
                languageAdapter.notifyDataSetChanged();

                MMKVBroadUtils.Companion.saveZt(listBeans.get(position).name);
                for (int i = 0; i < AppBroadUtils.Companion.getActivityList().size(); i++) {
                    AppBroadUtils.Companion.getActivityList().get(i).finish();
                }
                if (TextUtils.isEmpty(SharePrefBroadBroadUtil.getString(LiXinBroadApp.Companion.getMContext(), AppBroadConsts.UID, ""))) {
                    Intent intent = new Intent(LiXinBroadApp.Companion.getMContext(), NewEquipmentBroadActivity.class);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(LiXinBroadApp.Companion.getMContext(), MainBroadActivity.class);
                    startActivity(intent);
                }
            }
        });

        listBeans.clear();
        for (int i = 0; i < 3; i++) {
            DataListBroadBean dataListBean = new DataListBroadBean();
            switch (i) {
                case 0:
                    dataListBean.name = "跟随系统";
                    break;
                case 1:
                    dataListBean.name = "深色模式";
                    break;
                case 2:
                    dataListBean.name = "浅色模式";
                    break;
            }
            listBeans.add(dataListBean);
        }
        boolean ischeck = false;
        for (int i = 0; i < listBeans.size(); i++) {
            if (listBeans.get(i).name.equals(MMKVBroadUtils.Companion.getZt())) {
                listBeans.get(i).isChecked = true;
                ischeck = true;
            } else {
                listBeans.get(i).isChecked = false;
            }
        }
        if (!ischeck) {
            listBeans.get(0).isChecked = true;
            MMKVBroadUtils.Companion.saveZt(listBeans.get(0).name);
        }

        languageAdapter.notifyDataSetChanged();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_language;
    }

    private void initViews() {
        ryLangeuage = findViewById(R.id.ryLangeuage);
    }
}