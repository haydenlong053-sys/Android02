/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.conversation.file;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import com.cboda.chatui.common.OperateBroadResult;
import cn.wildfirechat.model.Conversation;
import cn.wildfirechat.model.FileRecord;
import cn.wildfirechat.model.FileRecordOrder;
import cn.wildfirechat.remote.ChatManager;
import cn.wildfirechat.remote.GeneralCallback;
import cn.wildfirechat.remote.GetFileRecordCallback;

public class FileRecordViewBroadModel extends ViewModel {
    public LiveData<OperateBroadResult<List<FileRecord>>> getConversationFileRecords(Conversation conversation, String fromUser, long beforeMessageUid, int count) {
        MutableLiveData<OperateBroadResult<List<FileRecord>>> data = new MutableLiveData<>();
        ChatManager.Instance().getConversationFileRecords(conversation, fromUser, beforeMessageUid, FileRecordOrder.By_Size_Desc, count, new GetFileRecordCallback() {
            @Override
            public void onSuccess(List<FileRecord> records) {
                data.postValue(new OperateBroadResult<>(records, 0));
            }

            @Override
            public void onFail(int errorCode) {
                data.postValue(new OperateBroadResult<>(-1));
            }
        });
        return data;
    }

    public LiveData<OperateBroadResult<List<FileRecord>>> getMyFileRecords(long beforeMessageUid, int count) {
        MutableLiveData<OperateBroadResult<List<FileRecord>>> data = new MutableLiveData<>();
        ChatManager.Instance().getMyFileRecords(beforeMessageUid, FileRecordOrder.By_Size_Desc, count, new GetFileRecordCallback() {
            @Override
            public void onSuccess(List<FileRecord> records) {
                data.postValue(new OperateBroadResult<>(records, 0));
            }

            @Override
            public void onFail(int errorCode) {
                data.postValue(new OperateBroadResult<>(-1));
            }
        });
        return data;
    }

    public LiveData<OperateBroadResult<Boolean>> deleteFileRecord(long messageUid) {
        MutableLiveData<OperateBroadResult<Boolean>> data = new MutableLiveData<>();
        ChatManager.Instance().deleteFileRecord(messageUid, new GeneralCallback() {
            @Override
            public void onSuccess() {
                data.postValue(new OperateBroadResult<>(true, 0));
            }

            @Override
            public void onFail(int errorCode) {
                data.postValue(new OperateBroadResult<>(-1));
            }
        });
        return data;
    }
}
