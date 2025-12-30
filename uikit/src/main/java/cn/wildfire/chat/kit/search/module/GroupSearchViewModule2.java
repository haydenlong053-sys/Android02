/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfire.chat.kit.search.module;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.util.List;

import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.conversation.ConversationActivity;
import cn.wildfire.chat.kit.search.SearchableModule;
import cn.wildfire.chat.kit.search.viewHolder.GroupViewHolder2;
import cn.wildfirechat.model.Conversation;
import cn.wildfirechat.model.GroupSearchResult;
import cn.wildfirechat.remote.ChatManager;

public class GroupSearchViewModule2 extends SearchableModule<GroupSearchResult, GroupViewHolder2> {
    @Override
    public GroupViewHolder2 onCreateViewHolder(Fragment fragment, @NonNull ViewGroup parent, int type) {
        View itemView;
        itemView = LayoutInflater.from(fragment.getActivity()).inflate(R.layout.search_item_group2, parent, false);
        return new GroupViewHolder2(fragment, itemView);
    }

    @Override
    public void onBind(Fragment fragment, GroupViewHolder2 holder, GroupSearchResult groupSearchResult) {
        holder.onBind(keyword, groupSearchResult);
    }

    @Override
    public void onBind2(Fragment fragment, GroupViewHolder2 holder, GroupSearchResult groupSearchResult, int currentPosition, int startPosition, int endPosition) {
        holder.onBind2(keyword, groupSearchResult,currentPosition,startPosition,endPosition);
    }

    @Override
    public void onClick(Fragment fragment, GroupViewHolder2 holder, View view, GroupSearchResult groupSearchResult) {
        Intent intent = new Intent(fragment.getActivity(), ConversationActivity.class);
        Conversation conversation = new Conversation(Conversation.ConversationType.Group, groupSearchResult.groupInfo.target, 0);
        intent.putExtra("conversation", conversation);
        fragment.startActivity(intent);
        fragment.getActivity().finish();
    }

    @Override
    public int getViewType(GroupSearchResult groupSearchResult) {
        return R.layout.search_item_group2;
    }

    @Override
    public int priority() {
        return 90;
    }

    @Override
    public String category() {
        return "群组";
    }

    @Override
    public String category2(View view) {
        return view.getResources().getString(R.string.qunliao);
    }

    @Override
    public List<GroupSearchResult> search(String keyword) {
        return ChatManager.Instance().searchGroups(keyword);
    }

}
