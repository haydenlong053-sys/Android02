/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.search;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.GsonUtils;
import com.cboda.baselibrary.bean.GlobalFriendListBroadBean;
import com.cboda.baselibrary.common.UrlBroadConstants;
import com.cboda.baselibrary.network.NetworkBroadUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.cboda.chatui.R;
import com.cboda.chatui.conversation.ConversationBroadActivity;

import cn.wildfirechat.model.Conversation;

public class SearchBroadFragment2 extends Fragment {
    RecyclerView recyclerView;
    //    LinearLayout emptyLinearLayout;
//    LinearLayout descLinearLayout;
    private SearchResultBroadAdapter2 adapter;
    private MyFriendBroadAdapter myFriendAdapter;
    private SearchViewBroadModel searchViewModel;
    private Observer<SearchBroadResult> searchResultObserver = this::onSearchResult;
    private InputMethodManager inputManager;

    private boolean hideSearchDescView = false;
    private String keyword;
    private List<SearchableBroadModule> searchableModules;

    public static final String HIDE_SEARCH_DESC_VIEW = "hideSearchDescView";
    private Handler mHandler = new Handler(Looper.getMainLooper());


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        hideSearchDescView = args != null && args.getBoolean(HIDE_SEARCH_DESC_VIEW);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.search_broad_fragment2, container, false);
        bindViews(view);
        searchViewModel = new ViewModelProvider(this).get(SearchViewBroadModel.class);
        searchViewModel.getResultLiveData().observeForever(searchResultObserver);
        inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                inputManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        });
//        descLinearLayout.setVisibility(hideSearchDescView ? View.GONE : View.VISIBLE);
        return view;
    }

    private void bindViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerView);
//        emptyLinearLayout = view.findViewById(R.id.emptyLinearLayout);
//        descLinearLayout = view.findViewById(R.id.descLinearLayout);
        initAdapter();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mHandler.removeCallbacks(mTimerRunnable);
        searchViewModel.getResultLiveData().removeObserver(searchResultObserver);
    }

    public void search(String keyword, List<SearchableBroadModule> searchableModules) {
        this.keyword = keyword;
        this.searchableModules = searchableModules;
        mHandler.removeCallbacks(mTimerRunnable);
        mHandler.postDelayed(mTimerRunnable, 500); // 1000毫秒 = 1秒

    }

    private Runnable mTimerRunnable = new Runnable() {
        @Override
        public void run() {
            if (adapter != null) {
                adapter.reset();
            }
//            descLinearLayout.setVisibility(View.GONE);
            searchViewModel.search(keyword, searchableModules);
            queryFriendList(keyword);
        }
    };

    private void queryFriendList(String mKeyword) {
        String keyword = mKeyword != null ? mKeyword.trim() : "";
        if (TextUtils.isEmpty(keyword)) {
            return;
        }
        // GET im/user/search/{keyword}（非加密）
        String url = UrlBroadConstants.FRIEND_CONTACT_LIST + Uri.encode(keyword);
        NetworkBroadUtils.INSTANCE.get(url, Collections.emptyMap(),
                new NetworkBroadUtils.NetworkCallback<Object>() {
                    @Override
                    public void onSuccess(Object data) {
                        applyFriendSearchResult(parseFriendSearchPayload(data), keyword);
                    }

                    @Override
                    public void onFailure(int code, @NotNull String message, Object data) {
                        applyFriendSearchResult(new ArrayList<>(), keyword);
                    }

                    @Override
                    public void onError(@NotNull Throwable throwable) {
                        applyFriendSearchResult(new ArrayList<>(), keyword);
                    }
                });
    }

    /**
     * 兼容 content 为数组、单对象、或 {@code userList} 包装。
     */
    private List<GlobalFriendListBroadBean> parseFriendSearchPayload(Object data) {
        List<GlobalFriendListBroadBean> result = new ArrayList<>();
        if (data == null) {
            return result;
        }
        try {
            Gson gson = GsonUtils.getGson();
            String json = gson.toJson(data);
            if (TextUtils.isEmpty(json) || "null".equals(json)) {
                return result;
            }
            if (json.trim().startsWith("[")) {
                List<GlobalFriendListBroadBean> list = gson.fromJson(json,
                        new TypeToken<List<GlobalFriendListBroadBean>>() {
                        }.getType());
                if (list != null) {
                    result.addAll(list);
                }
                return result;
            }
            com.google.gson.JsonObject obj = gson.fromJson(json, com.google.gson.JsonObject.class);
            if (obj != null) {
                if (obj.has("userList") && obj.get("userList").isJsonArray()) {
                    List<GlobalFriendListBroadBean> list = gson.fromJson(obj.get("userList"),
                            new TypeToken<List<GlobalFriendListBroadBean>>() {
                            }.getType());
                    if (list != null) {
                        result.addAll(list);
                    }
                    return result;
                }
                if (obj.has("list") && obj.get("list").isJsonArray()) {
                    List<GlobalFriendListBroadBean> list = gson.fromJson(obj.get("list"),
                            new TypeToken<List<GlobalFriendListBroadBean>>() {
                            }.getType());
                    if (list != null) {
                        result.addAll(list);
                    }
                    return result;
                }
                GlobalFriendListBroadBean bean = gson.fromJson(obj, GlobalFriendListBroadBean.class);
                if (bean != null) {
                    if (TextUtils.isEmpty(bean.getUserId()) && !TextUtils.isEmpty(bean.getId())) {
                        bean.setUserId(bean.getId());
                    }
                    if (!TextUtils.isEmpty(bean.getUserId())) {
                        result.add(bean);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private void applyFriendSearchResult(List<GlobalFriendListBroadBean> friendList, String mKeyword) {
        if (getActivity() == null || !isAdded() || myFriendAdapter == null) {
            return;
        }
        getActivity().runOnUiThread(() -> {
            if (myFriendAdapter != null) {
                myFriendAdapter.updateData(
                        friendList != null ? friendList : new ArrayList<>(),
                        mKeyword
                );
            }
        });
    }

    public void reset() {
        if (adapter != null) {
            adapter.reset();
        }
//        descLinearLayout.setVisibility(View.VISIBLE);
//        emptyLinearLayout.setVisibility(View.GONE);
//        recyclerView.setVisibility(View.GONE);
    }

    private void onSearchResult(SearchBroadResult result) {
        if (result == null) {
//            recyclerView.setVisibility(View.GONE);
            //注意此处需判断头部是否有数据，然后在展示为空
//            emptyLinearLayout.setVisibility(View.VISIBLE);
            return;
        } else {
//            recyclerView.setVisibility(View.VISIBLE);
//            emptyLinearLayout.setVisibility(View.GONE);
        }
//        initAdapter();


        adapter.submitSearResult(result);
    }

    private void initAdapter() {
        if (adapter == null) {
            adapter = new SearchResultBroadAdapter2(this);
            myFriendAdapter = new MyFriendBroadAdapter(new ArrayList<>(), getContext());
            myFriendAdapter.setOnItemClickListener(new MyFriendBroadAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(int position, GlobalFriendListBroadBean bean) {
                    if (!(getActivity() instanceof SearchBroadActivity2)) {
                        return;
                    }
                    String userId = bean.getUserId();
                    if (userId == null || userId.isEmpty()) {
                        return;
                    }
                    String name = !TextUtils.isEmpty(bean.getRemarkName())
                            ? bean.getRemarkName() : bean.getNickName();
                    SearchBroadActivity2 activity = (SearchBroadActivity2) getActivity();
                    if (activity.onSearchFriendItemClick(SearchBroadFragment2.this, userId, name)) {
                        return;
                    }
                    Intent intent = new Intent(activity, ConversationBroadActivity.class);
                    Conversation conversation = new Conversation(
                            Conversation.ConversationType.Single, userId, 0);
                    intent.putExtra("conversation", conversation);
                    startActivity(intent);
                    activity.finish();
                }
            });
            // 2. 使用 ConcatAdapter 将它们按顺序拼接[citation:5]
            ConcatAdapter concatAdapter = new ConcatAdapter(myFriendAdapter, adapter);
            recyclerView.setAdapter(concatAdapter);
//            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        }
    }
}
