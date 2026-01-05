/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.search;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import com.cboda.chatui.R;

public class SearchBroadFragment extends Fragment {
    RecyclerView recyclerView;
    LinearLayout emptyLinearLayout;
    LinearLayout descLinearLayout;
    private SearchResultBroadAdapter adapter;
    private SearchViewBroadModel searchViewModel;
    private Observer<SearchBroadResult> searchResultObserver = this::onSearchResult;
    private InputMethodManager inputManager;

    private boolean hideSearchDescView = false;

    public static final String HIDE_SEARCH_DESC_VIEW = "hideSearchDescView";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        hideSearchDescView = args != null && args.getBoolean(HIDE_SEARCH_DESC_VIEW);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.search_broad_fragment, container, false);
        searchViewModel = ViewModelProviders.of(this).get(SearchViewBroadModel.class);
        searchViewModel.getResultLiveData().observeForever(searchResultObserver);
        bindViews(view);
        inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                inputManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        });
        descLinearLayout.setVisibility(hideSearchDescView ? View.GONE: View.VISIBLE);
        return view;
    }

    private void bindViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerView);
        emptyLinearLayout = view.findViewById(R.id.emptyLinearLayout);
        descLinearLayout = view.findViewById(R.id.descLinearLayout);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        searchViewModel.getResultLiveData().removeObserver(searchResultObserver);
    }

    public void search(String keyword, List<SearchableBroadModule> searchableModules) {
        if (adapter != null) {
            adapter.reset();
        }
        descLinearLayout.setVisibility(View.GONE);
        searchViewModel.search(keyword, searchableModules);
    }

    public void reset() {
        if (adapter != null) {
            adapter.reset();
        }
        descLinearLayout.setVisibility(View.VISIBLE);
        emptyLinearLayout.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
    }

    private void onSearchResult(SearchBroadResult result) {
        if (result == null) {
            recyclerView.setVisibility(View.GONE);
            emptyLinearLayout.setVisibility(View.VISIBLE);
            return;
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyLinearLayout.setVisibility(View.GONE);
        }
        if (adapter == null) {
            adapter = new SearchResultBroadAdapter(this);
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        }
        adapter.submitSearResult(result);
    }
}
