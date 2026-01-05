/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.search;

import android.os.Bundle;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import com.cboda.chatui.R;
import com.cboda.chatui.WfcBaseNoToolbarBroadActivity;
import com.cboda.chatui.widget.SearchBroadView;
import cn.wildfirechat.remote.ChatManager;

/**
 * 如果启动{@link android.content.Intent}里面包含keyword，直接开始搜索
 */
public abstract class SearchBroadActivity extends WfcBaseNoToolbarBroadActivity {
    private SearchBroadFragment searchFragment;
    private List<SearchableBroadModule> modules = new ArrayList<>();

    public SearchBroadView searchView;

    public void onCancelClick() {
        finish();
    }

    private void bindEvents() {
        findViewById(R.id.cancel).setOnClickListener(v -> onCancelClick());
    }

    private void bindViews() {
        searchView = findViewById(R.id.search_view);
    }

    protected boolean hideSearchDescView() {
        return false;
    }

    /**
     * 子类如果替换布局，它的布局中必须要包含 R.layout.search_broad_bar
     *
     * @return 布局资源id
     */
    protected int contentLayout() {
        return R.layout.search_portal_broad_activity;
    }

    protected void beforeViews() {
        setStatusBarColor(R.color.color_main_bg);
    }

    protected void afterViews() {
        bindViews();
        bindEvents();
        initSearchView();
        initSearchFragment();
        String initialKeyword = getIntent().getStringExtra("keyword");
        ChatManager.Instance().getMainHandler().post(() -> {
            if (!TextUtils.isEmpty(initialKeyword)) {
                searchView.setQuery(initialKeyword);
            }
        });
        if (hideSearchDescView()) {
            searchView.clearFocus();
            hideInputMethod();
        }
    }

    private void initSearchView() {
        searchView.setOnQueryTextListener(this::search);
    }

    protected void initSearchFragment() {
        searchFragment = new SearchBroadFragment();
        Bundle args = new Bundle();
        args.putBoolean(SearchBroadFragment.HIDE_SEARCH_DESC_VIEW, hideSearchDescView());
        searchFragment.setArguments(args);
        getSupportFragmentManager().beginTransaction()
            .replace(R.id.containerFrameLayout, searchFragment)
            .commit();
        initSearchModule(modules);
    }

    void search(String keyword) {
        hintSearch(keyword);
        if (!TextUtils.isEmpty(keyword)) {
            searchFragment.search(keyword, modules);
        } else {
            searchFragment.reset();
        }

    }

    public void hintSearch(String keyWord) {

    }


    /**
     * @param modules 是一个输出参数，用来添加希望搜索的{@link SearchableBroadModule}
     */
    protected abstract void initSearchModule(List<SearchableBroadModule> modules);
}
