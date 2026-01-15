/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.contact.pick;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import com.cboda.chatui.R;
import com.cboda.chatui.contact.UserListBroadAdapter;
import com.cboda.chatui.contact.model.UIUserBroadInfo;

public class SearchAndPickUserBroadFragment extends Fragment implements UserListBroadAdapter.OnUserClickListener {
    private CheckableUserListBroadAdapter contactAdapter;
    private PickUserViewBroadModel pickUserViewModel;
    private PickUserBroadFragment pickUserFragment;

    RecyclerView contactRecyclerView;
    TextView tipTextView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.contact_search_broad_fragment, container, false);
        bindViews(view);
        bindEvents(view);
        init();
        return view;
    }

    private void bindEvents(View view) {
        view.findViewById(R.id.tipTextView).setOnClickListener(_v -> onTipTextViewClick());
    }

    private void bindViews(View view) {
        contactRecyclerView = view.findViewById(R.id.usersRecyclerView);
        tipTextView = view.findViewById(R.id.tipTextView);
    }

    public void setPickUserFragment(PickUserBroadFragment pickUserFragment) {
        this.pickUserFragment = pickUserFragment;
    }


    void onTipTextViewClick() {
        pickUserFragment.hideSearchContactFragment();
    }

    private void init() {
        pickUserViewModel = new ViewModelProvider(getActivity()).get(PickUserViewBroadModel.class);
        contactAdapter = new CheckableUserListBroadAdapter(this);
        contactAdapter.setOnUserClickListener(this);
        contactRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        contactRecyclerView.setAdapter(contactAdapter);
    }

    public void search(String keyword) {
        if (TextUtils.isEmpty(keyword)) {
            return;
        }
        List<UIUserBroadInfo> result = pickUserViewModel.searchUser(keyword);
        if (result == null || result.isEmpty()) {
            contactRecyclerView.setVisibility(View.GONE);
            tipTextView.setVisibility(View.VISIBLE);
        } else {
            contactRecyclerView.setVisibility(View.VISIBLE);
            tipTextView.setVisibility(View.GONE);
        }
        contactAdapter.setUsers(result);
        contactAdapter.notifyDataSetChanged();
    }

    public void rest() {
        tipTextView.setVisibility(View.VISIBLE);
        contactRecyclerView.setVisibility(View.GONE);
        contactAdapter.setUsers(null);
    }

    @Override
    public void onUserClick(UIUserBroadInfo userInfo) {
        if (userInfo.isCheckable()) {
            pickUserViewModel.checkUser(userInfo, !userInfo.isChecked());
            // the checked status has already changed by checkContact method
            contactAdapter.updateUserStatus(userInfo);
        }
    }
}
