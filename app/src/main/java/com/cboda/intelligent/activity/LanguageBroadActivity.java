package com.cboda.intelligent.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.cboda.baselibrary.app.UtilBroadApplication;
import com.cboda.baselibrary.utils.HttpsBroadConfig;
import com.cboda.select.picture.utils.LanguageUtil;
import com.cboda.intelligent.MainBroadActivity;
import com.cboda.intelligent.R;
import com.cboda.intelligent.app.MyBroadApp;
import com.cboda.intelligent.base.BaseSimpleBroadActivity;
import com.cboda.intelligent.adapter.LanguageBroadAdapter;
import com.cboda.select.picture.entity.DataListBroadBean;
import com.cboda.intelligent.configuration.AppBroadConsts;
import com.cboda.intelligent.utils.SharePrefBroadBroadUtil;
import com.cboda.intelligent.utils.AppBroadUtils;
import com.cboda.baselibrary.utils.MMKVBroadUtils;

import java.util.ArrayList;
import java.util.List;


public class LanguageBroadActivity extends BaseSimpleBroadActivity {

    private LanguageBroadAdapter languageAdapter;
    private List<DataListBroadBean> listBeans = new ArrayList<>();
    private String language = "";
    private RecyclerView ryLangeuage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();
        getToolbarTitle().setText(getResources().getString(R.string.切换语言));

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
                String languageKey = listBeans.get(position).sqlKey;
                MMKVBroadUtils.Companion.saveAppLanguageKey(languageKey, LanguageBroadActivity.this);
                LanguageUtil.INSTANCE.setAppLanguage(languageKey, LanguageBroadActivity.this);
//                ThreadUtils.getMainHandler().postDelayed(() -> {
//                    ActivityUtils.finishAllActivities();
//                    HttpsBroadConfig.INSTANCE.restartApp();
//                }, 500L);
//                MMKVBroadUtils.Companion.saveYuYan(listBeans.get(position).name);
//                for (int i = 0; i < AppBroadUtils.Companion.getActivityList().size(); i++) {
//                    AppBroadUtils.Companion.getActivityList().get(i).finish();
//                }
//                if (TextUtils.isEmpty(SharePrefBroadBroadUtil.getString(MyBroadApp.Companion.getMContext(), AppBroadConsts.UID, ""))) {
//                    Intent intent = new Intent(MyBroadApp.Companion.getMContext(), NewEquipmentBroadActivity.class);
//                    startActivity(intent);
//                } else {
//                    Intent intent = new Intent(MyBroadApp.Companion.getMContext(), MainBroadActivity.class);
//                    startActivity(intent);
//                }
            }
        });

        listBeans.clear();
        listBeans.addAll(LanguageUtil.getAllLanguage());
        boolean ischeck = false;
        for (int i = 0; i < listBeans.size(); i++) {
            if (listBeans.get(i).sqlKey.equals(MMKVBroadUtils.Companion.getAppLanguageKey(LanguageBroadActivity.this))) {
                listBeans.get(i).isChecked = true;
                ischeck = true;
            } else {
                listBeans.get(i).isChecked = false;
            }
        }
        if (!ischeck && !listBeans.isEmpty()) {
            listBeans.get(0).isChecked = true;
//            MMKVBroadUtils.Companion.saveYuYan(listBeans.get(0).name);
            MMKVBroadUtils.Companion.saveAppLanguageKey(listBeans.get(0).sqlKey, LanguageBroadActivity.this);
        }

        languageAdapter.notifyDataSetChanged();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_broad_language;
    }

    private void initViews() {
        ryLangeuage = findViewById(R.id.ryLangeuage);
    }
}