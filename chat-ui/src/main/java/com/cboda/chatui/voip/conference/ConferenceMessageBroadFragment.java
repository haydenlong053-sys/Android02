/*
 * Copyright (c) 2022 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.voip.conference;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import com.cboda.chatui.R;
import com.cboda.chatui.conversation.message.model.UiBroadMessage;
import com.cboda.chatui.viewmodel.MessageViewBroadModel;
import com.cboda.chatui.voip.conference.model.ConferenceBroadInfo;
import cn.wildfirechat.model.Conversation;
import cn.wildfirechat.remote.ChatManager;

public class ConferenceMessageBroadFragment extends Fragment {
    RecyclerView messageRecyclerView;

    private MessageViewBroadModel messageViewModel;
    private List<UiBroadMessage> messages = new ArrayList<>();
    private MessageAdapter messageAdapter;
    private Handler handler;

    private final static long TIMEOUT = 30 * 1000;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.av_conference_broad_message, container, false);
        bindViews(view);
        bindEvents(view);
        messageViewModel = new ViewModelProvider(this).get(MessageViewBroadModel.class);
        init();
        return view;
    }

    private void bindEvents(View view) {
        view.findViewById(R.id.inputTextView).setOnClickListener(_v -> showMessageInputFragment());
    }

    private void bindViews(View view) {
        messageRecyclerView = view.findViewById(R.id.messageRecyclerView);
    }

    private void init() {
        messageViewModel.messageLiveData().observe(getViewLifecycleOwner(), new Observer<UiBroadMessage>() {
            @Override
            public void onChanged(UiBroadMessage uiMessage) {
                if (uiMessage.message.messageId == 0) {
                    return;
                }
                Conversation conversation = uiMessage.message.conversation;
                ConferenceBroadInfo conferenceInfo = ConferenceBroadManager.getManager().getCurrentConferenceInfo();
                if (conferenceInfo != null && conversation.type == Conversation.ConversationType.ChatRoom && conversation.line == 0 && conversation.target.equals(conferenceInfo.getConferenceId())) {
                    if (!contains(uiMessage)) {
                        messages.add(uiMessage);
                        messageAdapter.notifyItemInserted(messages.size() - 1);
                    }
                }
            }
        });
        messageAdapter = new MessageAdapter();
        messageRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        messageRecyclerView.setAdapter(messageAdapter);

        handler = new Handler();
        handler.postDelayed(this::checkMessageTimeout, TIMEOUT);
    }

    private boolean contains(UiBroadMessage uiMessage) {
        for (UiBroadMessage message : messages) {
            if (message.message.messageId == uiMessage.message.messageId || message.message.messageUid == uiMessage.message.messageUid) {
                return true;
            }
        }
        return false;
    }

    private void checkMessageTimeout() {
        long now = System.currentTimeMillis();
        int j = -1;
        for (int i = 0; i < messages.size(); i++) {
            if (now - messages.get(i).message.serverTime > TIMEOUT) {
                j = i;
            }
        }
        if (j > -1) {
            if (j + 1 > messages.size()) {
                messages.clear();
            } else {
                messages = messages.subList(j + 1, messages.size());
            }
        }
        messageAdapter.notifyItemRangeRemoved(0, j + 1);

        handler.postDelayed(this::checkMessageTimeout, TIMEOUT);
    }

    void showMessageInputFragment() {
        ((ConferenceBroadActivity) getActivity()).showKeyboardDialogFragment();
    }

    static abstract class MessageViewBroadHolder extends RecyclerView.ViewHolder {

        public MessageViewBroadHolder(@NonNull View itemView) {
            super(itemView);
        }

        abstract public void onBind(UiBroadMessage message);
    }

    static class TextMessageViewHolder extends MessageViewBroadHolder {
        TextView senderTextView;
        TextView messageContentTextView;

        public TextMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            bindViews(itemView);
        }

        private void bindViews(View itemView) {
            senderTextView = itemView.findViewById(R.id.senderTextView);
            messageContentTextView = itemView.findViewById(R.id.messageContentTextView);
        }

        @Override
        public void onBind(UiBroadMessage message) {
            senderTextView.setText(ChatManager.Instance().getUserDisplayName(message.message.sender) + ":");
            messageContentTextView.setText(message.message.digest());
        }
    }

    class MessageAdapter extends RecyclerView.Adapter<MessageViewBroadHolder> {

        @NonNull
        @Override
        public MessageViewBroadHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View itemView = inflater.inflate(R.layout.av_conference_message_broad_item, parent, false);
            return new TextMessageViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull MessageViewBroadHolder holder, int position) {
            holder.onBind(messages.get(position));
        }

        @Override
        public int getItemCount() {
            return messages.size();
        }
    }

}
