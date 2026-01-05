/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.conversation.pick;

import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import java.util.ArrayList;
import java.util.List;

import com.cboda.chatui.R;
import com.cboda.chatui.WfcBaseBroadActivity;
import com.cboda.chatui.search.OnResultItemClickBroadListener;
import com.cboda.chatui.search.SearchBroadFragment;
import com.cboda.chatui.search.SearchableBroadModule;
import com.cboda.chatui.search.module.ContactSearchBroadModule;
import com.cboda.chatui.search.module.GroupSearchViewBroadModule;
import com.cboda.chatui.widget.SimpleTextBroadWatcher;
import cn.wildfirechat.model.Conversation;
import cn.wildfirechat.model.GroupSearchResult;
import cn.wildfirechat.model.UserInfo;

abstract public class PickOrCreateConversationBroadActivity extends WfcBaseBroadActivity {
    private SearchBroadFragment searchFragment;
    private List<SearchableBroadModule> searchableModules;

    EditText editText;

    protected void bindViews() {
        super.bindViews();
        editText = findViewById(R.id.searchEditText);
        editText.addTextChangedListener(new SimpleTextBroadWatcher(){
            @Override
            public void afterTextChanged(Editable s) {
                search(s);
            }
        });
    }

    @Override
    protected void afterViews() {
        PickOrCreateConversationBroadFragment pickOrCreateConversationFragment = new PickOrCreateConversationBroadFragment();
        pickOrCreateConversationFragment.setListener(this::onPickOrCreateConversation);
        getSupportFragmentManager()
            .beginTransaction()
            .add(R.id.containerFrameLayout, pickOrCreateConversationFragment)
            .commit();
        initSearch();
    }

    private void initSearch() {
        searchableModules = new ArrayList<>();
        SearchableBroadModule module = new ContactSearchBroadModule();
        module.setOnResultItemListener(new OnResultItemClickBroadListener<UserInfo>() {
            @Override
            public void onResultItemClick(Fragment fragment, View itemView, View view, UserInfo userInfo) {
                Conversation conversation = new Conversation(Conversation.ConversationType.Single, userInfo.uid, 0);
                onPickOrCreateConversation(conversation);
            }
        });
        searchableModules.add(module);

        module = new GroupSearchViewBroadModule();
        module.setOnResultItemListener(new OnResultItemClickBroadListener<GroupSearchResult>() {
            @Override
            public void onResultItemClick(Fragment fragment, View itemView, View view, GroupSearchResult gr) {
                Conversation conversation = new Conversation(Conversation.ConversationType.Group, gr.groupInfo.target, 0);
                onPickOrCreateConversation(conversation);
            }
        });
        searchableModules.add(module);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        editText.setText("");
        editText.clearFocus();
    }

    void search(Editable editable) {
        String keyword = editable.toString().trim();
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (!TextUtils.isEmpty(keyword)) {
            if (fragmentManager.findFragmentByTag("search") == null) {
                searchFragment = new SearchBroadFragment();
                fragmentManager.beginTransaction()
                    .add(R.id.containerFrameLayout, searchFragment, "search")
                    .addToBackStack("search-back")
                    .commit();
            }
            new Handler().post(() -> {
                searchFragment.search(keyword, searchableModules);
            });
        } else {
            getSupportFragmentManager().popBackStackImmediate();
        }
    }

    protected abstract void onPickOrCreateConversation(Conversation conversation);

    @Override
    protected int contentLayout() {
        return R.layout.pick_or_create_conversation_broad_activity;
    }
}
