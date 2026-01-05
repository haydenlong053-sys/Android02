/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.search.module;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import com.cboda.chatui.R;
import com.cboda.chatui.search.SearchableBroadModule;
import com.cboda.chatui.search.viewHolder.ChannelViewBroadHolder;
import cn.wildfirechat.model.ChannelInfo;
import cn.wildfirechat.remote.ChatManager;
import cn.wildfirechat.remote.SearchChannelCallback;

public class ChannelSearchBroadModule extends SearchableBroadModule<ChannelInfo, ChannelViewBroadHolder> {
    @Override
    public ChannelViewBroadHolder onCreateViewHolder(Fragment fragment, @NonNull ViewGroup parent, int type) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.channel_broad_item, parent, false);
        return new ChannelViewBroadHolder(view);
    }

    @Override
    public void onBind(Fragment fragment, ChannelViewBroadHolder holder, ChannelInfo channelInfo) {
        holder.bind(channelInfo);
    }

    @Override
    public void onClick(Fragment fragment, ChannelViewBroadHolder holder, View view, ChannelInfo channelInfo) {
        // TODO show channel info
    }

    @Override
    public int getViewType(ChannelInfo channelInfo) {
        return R.layout.channel_broad_item;
    }

    @Override
    public int priority() {
        return 0;
    }

    @Override
    public String category() {
        return "频道";
    }

    @Override
    public List<ChannelInfo> search(String keyword) {
        CountDownLatch latch = new CountDownLatch(1);
        List<ChannelInfo> result = new ArrayList<>();
        ChatManager.Instance().searchChannel(keyword, new SearchChannelCallback() {
            @Override
            public void onSuccess(List<ChannelInfo> channelInfos) {
                if (channelInfos != null) {
                    result.addAll(channelInfos);
                }
                latch.countDown();
            }

            @Override
            public void onFail(int errorCode) {
                Log.e(ChannelSearchBroadModule.class.getSimpleName(), "search failure: " + errorCode);
                latch.countDown();
            }
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return result;
    }
}
