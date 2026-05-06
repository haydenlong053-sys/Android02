/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.search;

import android.content.Context;
import android.content.Intent;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        if (!TextUtils.isEmpty(mKeyword.trim())) {
//            Log.d(APP_TAG, "keyword====" + mKeyword);
            Map<String, Object> map = new HashMap<>();
            map.put("text", mKeyword);
            NetworkBroadUtils.INSTANCE.postEncryptedList(UrlBroadConstants.FRIEND_CONTACT_LIST, map, new NetworkBroadUtils.ListCallback<GlobalFriendListBroadBean>() {
                @Override
                public void onError(@NotNull Throwable throwable) {

                }

                @Override
                public void onFailure(int code, @NotNull String message) {
//                    ToastBroadUtils.show(message);
                }

                @Override
                public void onSuccess(@org.jetbrains.annotations.Nullable List<? extends GlobalFriendListBroadBean> list, int totalCount, int totalPage) {
                    if (getActivity() != null && isAdded()) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (myFriendAdapter != null) {
                                    try {
                                        if (list != null && !list.isEmpty()) {
                                            Gson gson = GsonUtils.getGson();
                                            String jsonStr = gson.toJson(list);
                                            List<GlobalFriendListBroadBean> friendList = gson.fromJson(jsonStr, new TypeToken<List<GlobalFriendListBroadBean>>() {
                                            }.getType());
                                            myFriendAdapter.updateData(friendList, mKeyword);
                                        } else {
                                            myFriendAdapter.updateData(new ArrayList<>(), mKeyword);
                                        }

                                    } catch (Exception e) {
                                        myFriendAdapter.updateData(new ArrayList<>(), mKeyword);
                                        e.printStackTrace();
                                    }
                                }
                            }
                        });
                    }
                }
            });

        }
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
                    if (getActivity() != null) {
                        Intent intent = new Intent(getActivity(), ConversationBroadActivity.class);
                        Conversation conversation = new Conversation(Conversation.ConversationType.Single, bean.getUserId(), 0);
                        intent.putExtra("conversation", conversation);
                        startActivity(intent);
                        getActivity().finish();
                    }
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
