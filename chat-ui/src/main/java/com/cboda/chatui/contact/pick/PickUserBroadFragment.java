/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.contact.pick;

import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cboda.chatui.R;
import com.cboda.chatui.contact.BaseUserListBroadFragment;
import com.cboda.chatui.contact.UserListBroadAdapter;
import com.cboda.chatui.contact.model.UIUserBroadInfo;
import com.cboda.chatui.third.utils.UIBroadUtils;
import com.cboda.chatui.widget.QuickIndexBroadBar;
import com.cboda.chatui.widget.SimpleTextBroadWatcher;

public abstract class PickUserBroadFragment extends BaseUserListBroadFragment implements QuickIndexBroadBar.OnLetterUpdateListener {
    private SearchAndPickUserBroadFragment searchAndPickUserFragment;
    protected PickUserViewBroadModel pickUserViewModel;

    protected RecyclerView pickedUserRecyclerView;
    EditText searchEditText;
    FrameLayout searchUserFrameLayout;
    protected View hintView;

    private boolean isSearchFragmentShowing = false;
    protected PickedUserBroadAdapter pickedUserAdapter;

    private final Observer<UIUserBroadInfo> contactCheckStatusUpdateLiveDataObserver = userInfo -> {
        ((CheckableUserListBroadAdapter) userListAdapter).updateUserStatus(userInfo);
        hideSearchContactFragment();
        updatePickedUserView(userInfo);
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pickUserViewModel = ViewModelProviders.of(getActivity()).get(PickUserViewBroadModel.class);
        pickUserViewModel.userCheckStatusUpdateLiveData().observeForever(contactCheckStatusUpdateLiveDataObserver);
    }

    private void bindViews(View view) {
        pickedUserRecyclerView = view.findViewById(R.id.pickedUserRecyclerView);
        searchEditText = view.findViewById(R.id.searchEditText);
        searchUserFrameLayout = view.findViewById(R.id.searchFrameLayout);
        hintView = view.findViewById(R.id.hint_view);

        searchEditText.setOnFocusChangeListener(this::onSearchEditTextFocusChange);
        searchEditText.addTextChangedListener(new SimpleTextBroadWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                search(s);
            }
        });
    }

    @Override
    protected void afterViews(View view) {
        super.afterViews(view);
        bindViews(view);
        initView();
        setupPickFromUsers();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        pickUserViewModel.userCheckStatusUpdateLiveData().removeObserver(contactCheckStatusUpdateLiveDataObserver);
    }

    private void initView() {
        configPickedUserRecyclerView();
        pickedUserAdapter = getPickedUserAdapter();
        pickedUserRecyclerView.setAdapter(pickedUserAdapter);
    }

    protected void configPickedUserRecyclerView() {
        RecyclerView.LayoutManager pickedContactRecyclerViewLayoutManager = new LinearLayoutManager(getActivity(), GridLayoutManager.HORIZONTAL, false);
        pickedUserRecyclerView.setLayoutManager(pickedContactRecyclerViewLayoutManager);
        pickedUserRecyclerView.addItemDecoration(new Decoration());
    }

    protected PickedUserBroadAdapter getPickedUserAdapter() {
        return new PickedUserBroadAdapter(pickUserViewModel);
    }

    void onSearchEditTextFocusChange(View view, boolean focus) {
        if (getActivity() == null || getActivity().isFinishing()) {
            return;
        }
        if (focus) {
            showSearchContactFragment();
        } else {
            hideSearchContactFragment();
        }
        handleHintView(focus);
    }

    protected void handleHintView(boolean focus) {
        if (pickedUserAdapter.getItemCount() == 0 && !focus) {
            hintView.setVisibility(View.VISIBLE);
        } else {
            hintView.setVisibility(View.GONE);
        }
    }

    void search(Editable editable) {
        // restore view state
        if (searchAndPickUserFragment == null) {
            return;
        }
        String key = editable.toString();
        if (!TextUtils.isEmpty(key)) {
            searchAndPickUserFragment.search(key);
        } else {
            searchAndPickUserFragment.rest();
        }
    }

    @Override
    public int getContentLayoutResId() {
        return R.layout.contact_pick_broad_fragment;
    }

    @Override
    public UserListBroadAdapter onCreateUserListAdapter() {
        return new CheckableUserListBroadAdapter(this);
    }

    abstract protected void setupPickFromUsers();

    private void showSearchContactFragment() {
        if (searchAndPickUserFragment == null) {
            searchAndPickUserFragment = new SearchAndPickUserBroadFragment();
            searchAndPickUserFragment.setPickUserFragment(this);
        }
        searchUserFrameLayout.setVisibility(View.VISIBLE);
        getChildFragmentManager().beginTransaction()
            .replace(R.id.searchFrameLayout, searchAndPickUserFragment)
            .commit();
        isSearchFragmentShowing = true;
    }

    public void hideSearchContactFragment() {
        if (!isSearchFragmentShowing) {
            return;
        }

        searchEditText.setText("");
        searchEditText.clearFocus();
        searchUserFrameLayout.setVisibility(View.GONE);
        getChildFragmentManager().beginTransaction().remove(searchAndPickUserFragment).commit();
        isSearchFragmentShowing = false;
    }

    @Override
    public void onUserClick(UIUserBroadInfo userInfo) {
        if (userInfo.isCheckable()) {
            if (!pickUserViewModel.checkUser(userInfo, !userInfo.isChecked())) {
                Toast.makeText(getActivity(), "选人超限", Toast.LENGTH_SHORT).show();
            }
        }
    }

    protected void handleEditText() {
        if (pickedUserAdapter.getItemCount() == 0) {
            searchEditText.setHint("");
        } else {
            searchEditText.setHint("搜索");
        }
    }

    private void updatePickedUserView(UIUserBroadInfo userInfo) {
        if (userInfo.isChecked()) {
            pickedUserAdapter.addUser(userInfo);
        } else {
            pickedUserAdapter.removeUser(userInfo);
        }
        handleHintView(false);
        handleEditText();
    }

    private static class Decoration extends RecyclerView.ItemDecoration {
        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view);
            if (position < parent.getAdapter().getItemCount() - 1) {
                outRect.right = UIBroadUtils.dip2Px(4);
            } else {
                outRect.right = 0;
            }
        }
    }
}
