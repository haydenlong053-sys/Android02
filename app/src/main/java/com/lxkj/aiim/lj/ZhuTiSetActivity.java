package com.lxkj.aiim.lj;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lxkj.aiim.MainActivity;
import com.lxkj.aiim.R;
import com.lxkj.aiim.app.LiXinApp;
import com.lxkj.aiim.base.BaseSimpleActivity;
import com.lxkj.aiim.lj.adapter.LanguageAdapter;
import com.lxkj.aiim.lj.bean.DataListBean;
import com.lxkj.aiim.lj.configuration.AppConsts;
import com.lxkj.aiim.lj.utils.SharePrefBroadUtil;
import com.lxkj.aiim.utils.AppUtil;
import com.cboda.baselibrary.utils.MMKVBroadUtils;

import java.util.ArrayList;
import java.util.List;


public class ZhuTiSetActivity extends BaseSimpleActivity {

    private LanguageAdapter languageAdapter;
    private List<DataListBean> listBeans = new ArrayList<>();
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

        languageAdapter = new LanguageAdapter(this, listBeans);
        ryLangeuage.setLayoutManager(new LinearLayoutManager(this));
        ryLangeuage.setAdapter(languageAdapter);
        languageAdapter.setOnItemClickListener(new LanguageAdapter.OnItemClickListener() {
            @Override
            public void OnItemClickListener(int position) {
                for (int i = 0; i < listBeans.size(); i++) {
                    listBeans.get(i).isChecked = false;
                }
                listBeans.get(position).isChecked = true;
                languageAdapter.notifyDataSetChanged();

                MMKVBroadUtils.Companion.saveZt(listBeans.get(position).name);
                for (int i = 0; i < AppUtil.Companion.getActivityList().size(); i++) {
                    AppUtil.Companion.getActivityList().get(i).finish();
                }
                if (TextUtils.isEmpty(SharePrefBroadUtil.getString(LiXinApp.Companion.getMContext(), AppConsts.UID, ""))) {
                    Intent intent = new Intent(LiXinApp.Companion.getMContext(), NewEquipmentActivity.class);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(LiXinApp.Companion.getMContext(), MainActivity.class);
                    startActivity(intent);
                }
            }
        });

        listBeans.clear();
        for (int i = 0; i < 3; i++) {
            DataListBean dataListBean = new DataListBean();
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