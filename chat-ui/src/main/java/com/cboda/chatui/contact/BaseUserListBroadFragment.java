/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.contact;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import com.cboda.chatui.R;
import com.cboda.chatui.contact.model.FooterBroadValue;
import com.cboda.chatui.contact.model.HeaderBroadValue;
import com.cboda.chatui.contact.model.UIUserBroadInfo;
import com.cboda.chatui.contact.viewholder.footer.FooterViewBroadHolder;
import com.cboda.chatui.contact.viewholder.header.HeaderViewBroadHolder;
import com.cboda.chatui.widget.ProgressBroadFragment;
import com.cboda.chatui.widget.QuickIndexBroadBar;

/**
 * @author dhl
 * 应该使用懒加载
 */
public abstract class BaseUserListBroadFragment extends ProgressBroadFragment implements QuickIndexBroadBar.OnLetterUpdateListener, UserListBroadAdapter.OnUserClickListener, UserListBroadAdapter.OnHeaderClickListener, UserListBroadAdapter.OnFooterClickListener {

    RecyclerView usersRecyclerView;
    QuickIndexBroadBar quickIndexBar;
    TextView indexLetterTextView;

    protected UserListBroadAdapter userListAdapter;

    private LinearLayoutManager linearLayoutManager;
    private boolean showQuickIndexBar;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int contentLayout() {
        return getContentLayoutResId();
    }

    @Override
    protected void afterViews(View view) {
        bindViews(view);
        initView();
    }

    private void bindViews(View view) {
        usersRecyclerView = view.findViewById(R.id.usersRecyclerView);
        quickIndexBar = view.findViewById(R.id.quickIndexBar);
        indexLetterTextView = view.findViewById(R.id.indexLetterTextView);
    }

    private void initView() {

        userListAdapter = onCreateUserListAdapter();
        userListAdapter.setOnUserClickListener(this);
        userListAdapter.setOnHeaderClickListener(this);
        userListAdapter.setOnFooterClickListener(this);

        initHeaderViewHolders();
        initFooterViewHolders();

        usersRecyclerView.setAdapter(userListAdapter);
        linearLayoutManager = new LinearLayoutManager(getActivity());
        usersRecyclerView.setLayoutManager(linearLayoutManager);

        if (showQuickIndexBar) {
            quickIndexBar.setVisibility(View.VISIBLE);
            quickIndexBar.setOnLetterUpdateListener(this);
        } else {
            quickIndexBar.setVisibility(View.GONE);
        }
    }

    public void initHeaderViewHolders() {
        // no header
    }

    public void initFooterViewHolders() {
        // no footer
    }

    /**
     * 一定要包含一个id为contactRecyclerView的RecyclerView
     *
     * @return
     */
    protected int getContentLayoutResId() {
        return R.layout.contact_contacts_fragment;
    }

    /**
     * the data for this userListAdapter should be set here
     *
     * @return
     */
    protected UserListBroadAdapter onCreateUserListAdapter() {
        userListAdapter = new UserListBroadAdapter(this);
        return userListAdapter;
    }

    protected void addHeaderViewHolder(Class<? extends HeaderViewBroadHolder> clazz, int layoutResId, HeaderBroadValue value) {
        userListAdapter.addHeaderViewHolder(clazz, layoutResId, value);
        // to do notify header changed
    }

    protected void addFooterViewHolder(Class<? extends FooterViewBroadHolder> clazz, int layoutResId, FooterBroadValue value) {
        userListAdapter.addFooterViewHolder(clazz, layoutResId, value);
    }

    @Override
    public void onLetterUpdate(String letter) {
        indexLetterTextView.setVisibility(View.VISIBLE);
        indexLetterTextView.setText(letter);

        List<UIUserBroadInfo> data = userListAdapter.getUsers();
        if(data == null || data.isEmpty()){
            return;
        }

        if ("↑".equalsIgnoreCase(letter)) {
            linearLayoutManager.scrollToPositionWithOffset(0, 0);
        } else if ("☆".equalsIgnoreCase(letter)) {
            linearLayoutManager.scrollToPositionWithOffset(userListAdapter.headerCount(), 0);
        } else if ("#".equalsIgnoreCase(letter)) {
            for (int i = 0; i < data.size(); i++) {
                UIUserBroadInfo friend = data.get(i);
                if (friend.getCategory().equals("#")) {
                    linearLayoutManager.scrollToPositionWithOffset(userListAdapter.headerCount() + i, 0);
                    break;
                }
            }
        } else {
            for (int i = 0; i < data.size(); i++) {
                UIUserBroadInfo friend = data.get(i);
                if (friend.getCategory().compareTo(letter) >= 0) {
                    linearLayoutManager.scrollToPositionWithOffset(i + userListAdapter.headerCount(), 0);
                    break;
                }
            }
        }
    }

    @Override
    public void onLetterCancel() {
        indexLetterTextView.setVisibility(View.GONE);
    }

    /**
     * 是否显示快速导航条
     *
     * @param show
     */
    public void showQuickIndexBar(boolean show) {
        this.showQuickIndexBar = show;
        if (quickIndexBar != null) {
            quickIndexBar.setVisibility(show ? View.VISIBLE : View.GONE);
            quickIndexBar.setOnLetterUpdateListener(this);
            quickIndexBar.invalidate();
        }
    }

    @Override
    public void onUserClick(UIUserBroadInfo userInfo) {

    }

    @Override
    public void onHeaderClick(HeaderViewBroadHolder holder) {

    }

    @Override
    public void onFooterClick(int index) {

    }
}
