/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.conversation.pick;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import com.cboda.chatui.R;
import com.cboda.chatui.conversation.forward.viewholder.CategoryViewBroadHolder;
import com.cboda.chatui.conversation.forward.viewholder.ConversationViewBroadHolder;
import com.cboda.chatui.conversation.forward.viewholder.CreateConversationViewBroadHolder;
import cn.wildfirechat.model.ConversationInfo;

public class PickOrCreateConversationBroadAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Fragment fragment;
    private List<ConversationInfo> conversations;
    private OnConversationItemClickListener conversationItemClickListener;
    private OnNewConversationItemClickListener newConversationItemClickListener;

    public PickOrCreateConversationBroadAdapter(Fragment fragment) {
        this.fragment = fragment;
    }

    public void setConversations(List<ConversationInfo> conversations) {
        this.conversations = conversations;
    }

    public void setOnConversationItemClickListener(OnConversationItemClickListener listener) {
        this.conversationItemClickListener = listener;
    }

    public void setNewConversationItemClickListener(OnNewConversationItemClickListener listener) {
        this.newConversationItemClickListener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
        RecyclerView.ViewHolder holder;
        if (viewType == R.layout.forward_item_create_conversation) {
            holder = new CreateConversationViewBroadHolder(view);
            processOnClick(holder);
        } else if (viewType == R.layout.forward_item_category) {
            holder = new CategoryViewBroadHolder(view);
        } else {
            holder = new ConversationViewBroadHolder(fragment, view);
            processOnClick(holder);
        }
        return holder;
    }

    private void processOnClick(RecyclerView.ViewHolder holder) {
        holder.itemView.setOnClickListener((v) -> {
            if (holder instanceof ConversationViewBroadHolder && conversationItemClickListener != null) {
                int position = holder.getAdapterPosition();
                ConversationInfo conversationInfo = conversations.get(position - 2);
                conversationItemClickListener.onConversationItemClick(conversationInfo);
            } else if (holder instanceof CreateConversationViewBroadHolder && newConversationItemClickListener != null) {
                newConversationItemClickListener.onNewConversationItemClick();
            }
        });
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch (position) {
            case 0:
                break;
            case 1:
                break;
            default:
                ((ConversationViewBroadHolder) holder).onBind(conversations.get(position - 2));
                break;
        }
    }

    @Override
    public int getItemCount() {
        return conversations == null ? 2 : 2 + conversations.size();
    }

    @Override
    public int getItemViewType(int position) {
        int type;
        switch (position) {
            case 0:
                type = R.layout.forward_item_create_conversation;
                break;
            case 1:
                type = R.layout.forward_item_category;
                break;
            default:
                type = R.layout.forward_item_recent_conversation;
                break;
        }
        return type;
    }

    public interface OnNewConversationItemClickListener {
        void onNewConversationItemClick();
    }

    public interface OnConversationItemClickListener {
        void onConversationItemClick(ConversationInfo conversationInfo);
    }
}
