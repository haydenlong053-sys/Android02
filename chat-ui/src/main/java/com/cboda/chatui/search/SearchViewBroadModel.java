/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.search;

import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import cn.wildfirechat.message.Message;
import cn.wildfirechat.model.Conversation;
import cn.wildfirechat.remote.ChatManager;

public class SearchViewBroadModel extends ViewModel {
    private MutableLiveData<SearchBroadResult> resultLiveData = new MutableLiveData<>();

    private Handler workHandler;
    private Handler mainHandler = new Handler();
    private String keyword;

    public SearchViewBroadModel() {
        init();
    }

    public void search(String keyword, List<SearchableBroadModule> searchableModules) {
        if (TextUtils.isEmpty(keyword)) {
            return;
        }
        if (this.keyword != null && this.keyword.equals(keyword)) {
            return;
        }
        this.keyword = keyword;
        workHandler.post(() -> {
            boolean found = false;
            for (SearchableBroadModule module : searchableModules) {
                List result = module.searchInternal(keyword);
                if (keyword.equals(SearchViewBroadModel.this.keyword) && result != null && !result.isEmpty()) {
                    found = true;
                    mainHandler.post(() -> resultLiveData.setValue(new SearchBroadResult(module, result)));
                }
            }

            if (keyword.equals(SearchViewBroadModel.this.keyword) && !found) {
                mainHandler.post(() -> resultLiveData.setValue(null));
            }
            this.keyword = null;
        });
    }


    public LiveData<List<Message>> searchMessage(Conversation conversation, String keyword) {
        MutableLiveData<List<Message>> result = new MutableLiveData<>();
        workHandler.post(() -> {
            List<Message> messages = ChatManager.Instance().searchMessage(conversation, keyword, true, 100, 0, null);
            result.postValue(messages);
        });
        return result;
    }

    public MutableLiveData<SearchBroadResult> getResultLiveData() {
        return resultLiveData;
    }

    private void init() {
        if (workHandler == null) {
            HandlerThread thread = new HandlerThread("search");
            thread.start();
            workHandler = new Handler(thread.getLooper());
        }
    }
}
