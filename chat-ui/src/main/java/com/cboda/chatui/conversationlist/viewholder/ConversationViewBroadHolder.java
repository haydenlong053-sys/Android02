/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.conversationlist.viewholder;

import android.content.Context;
import android.content.Intent;
import android.text.style.ImageSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import com.cboda.chatemoji.MoonUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.cboda.chatui.R;
import com.cboda.chatui.annotation.ConversationContextMenuBroadItem;
import com.cboda.chatui.conversation.ConversationBroadActivity;
import com.cboda.chatui.conversation.ConversationViewBroadModel;
import com.cboda.chatui.conversation.Draft;
import com.cboda.chatui.conversationlist.ConversationListViewBroadModel;
import com.cboda.chatui.conversationlist.ConversationListViewModelBroadFactory;
import com.cboda.chatui.group.GroupViewBroadModel;
import com.cboda.chatui.third.utils.TimeBroadBroadUtils;
import com.cboda.chatui.utils.WfcTextBroadUtils;
import cn.wildfirechat.message.Message;
import cn.wildfirechat.message.core.MessageDirection;
import cn.wildfirechat.message.core.MessageStatus;
import cn.wildfirechat.message.notification.NotificationMessageContent;
import cn.wildfirechat.model.Conversation;
import cn.wildfirechat.model.ConversationInfo;
import cn.wildfirechat.remote.ChatManager;

@SuppressWarnings("unused")
public abstract class ConversationViewBroadHolder extends RecyclerView.ViewHolder {
    protected Fragment fragment;
    protected View itemView;
    protected ConversationInfo conversationInfo;
    protected RecyclerView.Adapter adapter;
    protected ConversationListViewBroadModel conversationListViewModel;
    private ConversationViewBroadModel conversationViewModel;

    protected TextView nameTextView;
    protected TextView timeTextView;
    protected ImageView portraitImageView;
    protected ImageView silentImageView;
    protected TextView unreadCountTextView;
    protected View redDotView;
    protected TextView contentTextView;
    protected TextView promptTextView, gradeTv, nameTimeTv;

    protected ImageView statusImageView;

    protected ImageView secretChatIndicator;

    protected ImageView ivCheckCnv;

    protected boolean isSelectMode;

    public ConversationViewBroadHolder(Fragment fragment, RecyclerView.Adapter adapter, View itemView) {
        super(itemView);
        this.fragment = fragment;
        this.itemView = itemView;
        this.adapter = adapter;
        bindViews(itemView);
        conversationListViewModel = new ViewModelProvider(fragment.getActivity(), new ConversationListViewModelBroadFactory(Arrays.asList(Conversation.ConversationType.Single, Conversation.ConversationType.Group, Conversation.ConversationType.SecretChat), Arrays.asList(0)))
                .get(ConversationListViewBroadModel.class);
        conversationViewModel = ViewModelProviders.of(fragment).get(ConversationViewBroadModel.class);
    }

    // ConversationViewBroadHolder.java 里加
    public void updateSelectedState(boolean selected) {
        itemView.setSelected(selected);
        // 你的 checkbox、勾选图标、背景色等
        if (ivCheckCnv != null) {
            ivCheckCnv.setImageResource(selected ? R.mipmap.ic_check_cnv_select : R.mipmap.ic_check_cnv_normal);
        }
        // 或者你用 ImageView 打勾
        // selectedIcon.setVisibility(selected ? View.VISIBLE : View.GONE);
    }

    // 供 Adapter 在创建或绑定时传递选择模式标志
    public void setSelectMode(boolean selectMode) {
        this.isSelectMode = selectMode;
    }

    private void bindViews(View itemView) {
        nameTextView = itemView.findViewById(R.id.nameTextView);
        timeTextView = itemView.findViewById(R.id.timeTextView);
        portraitImageView = itemView.findViewById(R.id.portraitImageView);
        gradeTv = itemView.findViewById(R.id.gradeTv);
        nameTimeTv = itemView.findViewById(R.id.nameTimeTv);
        silentImageView = itemView.findViewById(R.id.slient);
        unreadCountTextView = itemView.findViewById(R.id.unreadCountTextView);
        redDotView = itemView.findViewById(R.id.redDotView);
        contentTextView = itemView.findViewById(R.id.contentTextView);
        promptTextView = itemView.findViewById(R.id.promptTextView);
        statusImageView = itemView.findViewById(R.id.statusImageView);
        secretChatIndicator = itemView.findViewById(R.id.secretChatIndicator);
        ivCheckCnv = itemView.findViewById(R.id.iv_check_cnv);
    }

    final public void onBind(ConversationInfo conversationInfo, int position) {
        this.conversationInfo = conversationInfo;
        onBind(conversationInfo);
    }

    /**
     * 设置头像、名称
     *
     * @param conversationInfo
     */
    protected abstract void onBindConversationInfo(ConversationInfo conversationInfo);

    public void onBind(ConversationInfo conversationInfo) {
        secretChatIndicator.setVisibility(View.GONE);
        onBindConversationInfo(conversationInfo);

        ivCheckCnv.setVisibility(isSelectMode ? View.VISIBLE : View.GONE);
        ivCheckCnv.setImageResource(conversationInfo.isSelect ? R.mipmap.ic_check_cnv_select : R.mipmap.ic_check_cnv_normal);

        timeTextView.setText(TimeBroadBroadUtils.getMsgFormatTime(conversationInfo.timestamp));
        silentImageView.setVisibility(conversationInfo.isSilent ? View.VISIBLE : View.GONE);
        statusImageView.setVisibility(View.GONE);

//        itemView.setBackgroundResource(conversationInfo.top > 0 ? R.drawable.selector_stick_top_broad_item : R.drawable.selector_common_broad_item);
        itemView.setBackgroundResource(conversationInfo.top > 0 ? R.drawable.selector_stick_top_broad_item : 0);
        redDotView.setVisibility(View.GONE);
//        ELogUtilKt.eLogD("zhanglong",conversationInfo.conversation.target+conversationInfo.isSilent+"-未读数--"+conversationInfo.unreadCount.unread);
        if (conversationInfo.isSilent) {
            if (conversationInfo.unreadCount.unread > 0) { // 显示红点
                unreadCountTextView.setText("");
                unreadCountTextView.setVisibility(View.GONE);
                redDotView.setVisibility(View.VISIBLE);
            } else {
                unreadCountTextView.setVisibility(View.GONE);
            }
        } else {
            if (conversationInfo.unreadCount.unread > 0) {
                unreadCountTextView.setVisibility(View.VISIBLE);
                unreadCountTextView.setText(conversationInfo.unreadCount.unread > 99 ? "99+" : conversationInfo.unreadCount.unread + "");
            } else {
                unreadCountTextView.setVisibility(View.GONE);
            }
        }


        Draft draft = Draft.fromDraftJson(conversationInfo.draft);
        if (draft != null) {
            String draftString = draft.getContent() != null ? draft.getContent() : "[草稿]";
            MoonUtils.identifyFaceExpression(fragment.getActivity(), contentTextView, draft.getContent(), ImageSpan.ALIGN_BOTTOM);
            setViewVisibility(R.id.promptTextView, View.VISIBLE);
            setViewVisibility(R.id.contentTextView, View.VISIBLE);
        } else {
            if (conversationInfo.unreadCount.unreadMentionAll > 0 || conversationInfo.unreadCount.unreadMention > 0) {
                promptTextView.setText("[有人@我]");
                promptTextView.setVisibility(View.VISIBLE);
            } else {
                promptTextView.setVisibility(View.GONE);
            }
            setViewVisibility(R.id.contentTextView, View.VISIBLE);
            if (conversationInfo.lastMessage != null && conversationInfo.lastMessage.content != null) {
                String content = "";
                Message lastMessage = conversationInfo.lastMessage;
                // the message maybe invalid
                try {
                    if (conversationInfo.conversation.type == Conversation.ConversationType.Group
                            && lastMessage.direction == MessageDirection.Receive
                            && !(lastMessage.content instanceof NotificationMessageContent)) {
                        GroupViewBroadModel groupViewModel = ViewModelProviders.of(fragment).get(GroupViewBroadModel.class);
                        String senderDisplayName = groupViewModel.getGroupMemberDisplayName(conversationInfo.conversation.target, conversationInfo.lastMessage.sender);
                        content = senderDisplayName + ":" + lastMessage.digest();
                    } else {
                        content = lastMessage.digest();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                content = WfcTextBroadUtils.htmlToText(content);
                MoonUtils.identifyFaceExpression(fragment.getActivity(), contentTextView, content, ImageSpan.ALIGN_BOTTOM);

                switch (lastMessage.status) {
                    case Sending:
                        statusImageView.setVisibility(View.VISIBLE);
                        // TODO update sending image resource
                        statusImageView.setImageResource(R.mipmap.ic_sending);
                        break;
                    case Send_Failure:
                        statusImageView.setVisibility(View.VISIBLE);
                        statusImageView.setImageResource(R.mipmap.img_error);
                        break;
                    default:
                        statusImageView.setVisibility(View.GONE);
                        break;
                }

            } else {
                contentTextView.setText("");
            }
        }
    }

    public void onClick(View itemView) {
//        if (isSelectMode){
//            ConversationListBroadAdapter adapter = (ConversationListBroadAdapter) getBindingAdapter();
//            int adapterPos = getAdapterPosition();
//
//
//            if (adapter!=null){
//                if (adapterPos == RecyclerView.NO_POSITION) {
//                    return;
//                }
//                int dataIndex = adapterPos - adapter.headerCount();
//                if (dataIndex < 0 || dataIndex >= adapter.getData().size()) {
//                    return;
//                }
//                // 传递数据项与适配器位置，避免越界并确保刷新正确的行
//               adapter.selectConversation(adapter.getData().get(dataIndex), adapterPos);
//            }
//
//
//        }else {
        // 点击会话
        Intent intent = new Intent(fragment.getActivity(), ConversationBroadActivity.class);
        intent.putExtra("conversation", conversationInfo.conversation);
        fragment.startActivity(intent);
//        }

    }

    @ConversationContextMenuBroadItem(tag = ConversationContextMenuItemBroadTags.TAG_REMOVE,
            confirm = true,
            priority = 0)
    public void removeConversation(View itemView, ConversationInfo conversationInfo) {
        conversationListViewModel.removeConversation(conversationInfo, true);
    }

    @ConversationContextMenuBroadItem(tag = ConversationContextMenuItemBroadTags.TAG_TOP, priority = 1)
    public void stickConversationTop(View itemView, ConversationInfo conversationInfo) {
        conversationListViewModel.setConversationTop(conversationInfo, 1);
    }

    @ConversationContextMenuBroadItem(tag = ConversationContextMenuItemBroadTags.TAG_CANCEL_TOP, priority = 2)
    public void cancelStickConversationTop(View itemView, ConversationInfo conversationInfo) {
        conversationListViewModel.setConversationTop(conversationInfo, 0);
    }

    @ConversationContextMenuBroadItem(tag = ConversationContextMenuItemBroadTags.TAG_MarkAsRead, priority = 3)
    public void clearConversationUnread(View itemView, ConversationInfo conversationInfo) {
        conversationListViewModel.clearConversationUnread(conversationInfo);
    }

    @ConversationContextMenuBroadItem(tag = ConversationContextMenuItemBroadTags.TAG_MarkAsUnread, priority = 2)
    public void markConversationUnread(View itemView, ConversationInfo conversationInfo) {
        conversationListViewModel.markConversationUnread(conversationInfo);
    }

    /**
     * 长按触发的context menu的标题
     *
     * @param tag
     * @return
     */
    public String contextMenuTitle(Context context, String tag) {
        String title = "未设置";
        switch (tag) {
            case ConversationContextMenuItemBroadTags.TAG_REMOVE:
                title = "删除会话";
                break;
            case ConversationContextMenuItemBroadTags.TAG_TOP:
                title = "置顶";
                break;
            case ConversationContextMenuItemBroadTags.TAG_CANCEL_TOP:
                title = "取消置顶";
                break;
            case ConversationContextMenuItemBroadTags.TAG_MarkAsRead:
                title = "设为已读";
                break;
            case ConversationContextMenuItemBroadTags.TAG_MarkAsUnread:
                title = "标记未读";
            default:
                break;

        }
        return title;
    }

    /**
     * 执行长按menu操作，需要确认时的提示信息。比如长按会话 -> 删除 -> 提示框进行二次确认
     *
     * @param tag
     * @return
     */
    public String contextConfirmPrompt(Context context, String tag) {
        String title = "未设置";
        switch (tag) {
            case ConversationContextMenuItemBroadTags.TAG_REMOVE:
                title = "确认删除会话?";
                break;
        }
        return title;
    }

    /**
     * @param conversationInfo
     * @param itemTag
     * @return 返回true，将从context menu中排除
     */
    public boolean contextMenuItemFilter(ConversationInfo conversationInfo, String itemTag) {
        if (ConversationContextMenuItemBroadTags.TAG_TOP.equals(itemTag)) {
            return conversationInfo.top > 0;
        }

        if (ConversationContextMenuItemBroadTags.TAG_CANCEL_TOP.equals(itemTag)) {
            return conversationInfo.top == 0;
        }

        if (ConversationContextMenuItemBroadTags.TAG_MarkAsRead.equals(itemTag)) {
            return conversationInfo.unreadCount.unread == 0 && conversationInfo.unreadCount.unreadMention == 0 && conversationInfo.unreadCount.unreadMentionAll == 0;
        }

        if (ConversationContextMenuItemBroadTags.TAG_MarkAsUnread.equals(itemTag)) {
            if (conversationInfo.unreadCount.unread > 0 || conversationInfo.unreadCount.unreadMention > 0 || conversationInfo.unreadCount.unreadMentionAll > 0) {
                return true;
            }
            List<Integer> messageStatuses = new ArrayList<>();
            messageStatuses.add(MessageStatus.Readed.value());
            messageStatuses.add(MessageStatus.Played.value());
            List<Message> messages = ChatManager.Instance().getMessagesByMessageStatus(conversationInfo.conversation, messageStatuses, 0, false, 1, "");
            return messages == null || messages.size() == 0;
        }

        return false;
    }

    protected <T extends View> T getView(int viewId) {
        View view;
        view = itemView.findViewById(viewId);
        return (T) view;
    }

    protected ConversationViewBroadHolder setViewVisibility(int viewId, int visibility) {
        View view = itemView.findViewById(viewId);
        view.setVisibility(visibility);
        return this;
    }

    public Fragment getFragment() {
        return fragment;
    }
}
