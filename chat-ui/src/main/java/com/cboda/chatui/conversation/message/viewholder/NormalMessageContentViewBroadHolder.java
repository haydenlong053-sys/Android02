/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.conversation.message.viewholder;

import static com.cboda.baselibrary.utils.ELogUtilKt.eLogD;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.cboda.baselibrary.bean.GlobalFriendBroadBean;
import com.cboda.baselibrary.bean.GlobalBroadInfo;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.cboda.chatui.AppServiceBroadProvider;
import com.cboda.chatui.ChatManagerBroadHolder;
import com.cboda.chatui.BroadConfig;
import com.cboda.chatui.R;
import com.cboda.chatui.WfcUIBroadKit;
import com.cboda.chatui.annotation.MessageContextMenuBroadItem;
import com.cboda.chatui.conversation.ConversationBroadActivity;
import com.cboda.chatui.conversation.ConversationBroadFragment;
import com.cboda.chatui.conversation.ConversationMessageBroadAdapter;
import com.cboda.chatui.conversation.forward.ForwardBroadActivity;
import com.cboda.chatui.conversation.message.model.UiBroadMessage;
import com.cboda.chatui.favorite.FavoriteBroadItem;
import com.cboda.chatui.group.GroupViewBroadModel;
import com.cboda.chatui.net.SimpleCallback;
import com.cboda.chatui.user.UserViewBroadModel;
import cn.wildfirechat.message.ArticlesMessageContent;
import cn.wildfirechat.message.CallStartMessageContent;
import cn.wildfirechat.message.CompositeMessageContent;
import cn.wildfirechat.message.FileMessageContent;
import cn.wildfirechat.message.ImageMessageContent;
import cn.wildfirechat.message.Message;
import cn.wildfirechat.message.MessageContent;
import cn.wildfirechat.message.SoundMessageContent;
import cn.wildfirechat.message.StickerMessageContent;
import cn.wildfirechat.message.TextMessageContent;
import cn.wildfirechat.message.VideoMessageContent;
import cn.wildfirechat.message.core.ContentTag;
import cn.wildfirechat.message.core.MessageDirection;
import cn.wildfirechat.message.core.MessageStatus;
import cn.wildfirechat.message.core.PersistFlag;
import cn.wildfirechat.model.ChannelInfo;
import cn.wildfirechat.model.Conversation;
import cn.wildfirechat.model.GroupInfo;
import cn.wildfirechat.model.GroupMember;
import cn.wildfirechat.model.UserInfo;
import cn.wildfirechat.remote.ChatManager;
import cn.wildfirechat.remote.UserSettingScope;

/**
 * 普通消息
 */
public abstract class NormalMessageContentViewBroadHolder extends MessageContentViewBroadHolder {
    ImageView portraitImageView;
    LinearLayout errorLinearLayout;
    TextView nameTextView;
    ProgressBar progressBar;
    CheckBox checkBox;

    @Nullable
    ImageView singleReceiptImageView;

    @Nullable
    FrameLayout groupReceiptFrameLayout;

    @Nullable
    ProgressBar deliveryProgressBar;
    @Nullable
    ProgressBar readProgressBar;

    public NormalMessageContentViewBroadHolder(ConversationBroadFragment fragment, RecyclerView.Adapter adapter, View itemView) {
        super(fragment, adapter, itemView);
        bindViews(itemView);
        bindEvents(itemView);
    }

    private void bindEvents(View itemView) {
        if (errorLinearLayout != null) {
            errorLinearLayout.setOnClickListener(this::onRetryClick);
        }
        if (groupReceiptFrameLayout != null) {
            groupReceiptFrameLayout.setOnClickListener(this::OnGroupMessageReceiptClick);
        }
    }

    private void bindViews(View itemView) {
        portraitImageView = itemView.findViewById(R.id.portraitImageView);
        errorLinearLayout = itemView.findViewById(R.id.errorLinearLayout);
        nameTextView = itemView.findViewById(R.id.nameTextView);
        progressBar = itemView.findViewById(R.id.progressBar);
        checkBox = itemView.findViewById(R.id.checkbox);
        singleReceiptImageView = itemView.findViewById(R.id.singleReceiptImageView);
        groupReceiptFrameLayout = itemView.findViewById(R.id.groupReceiptFrameLayout);
        deliveryProgressBar = itemView.findViewById(R.id.deliveryProgressBar);
        readProgressBar = itemView.findViewById(R.id.readProgressBar);
    }

    @Override
    public void onBind(UiBroadMessage message, int position) {
        super.onBind(message, position);
        setSenderAvatar(message.message);
        setSenderName(message.message);
        setSendStatus(message.message);
        try {
            onBind(message);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (message.isFocus) {
            highlightItem(itemView, message);
        }
    }

    protected abstract void onBind(UiBroadMessage message);

    /**
     * when animation finish, do not forget to set {@link UiBroadMessage#isFocus} to {@code true}
     *
     * @param itemView the item view
     * @param message  the message to highlight
     */
    protected void highlightItem(View itemView, UiBroadMessage message) {
        Animation animation = new AlphaAnimation((float) 0.4, (float) 0.2);
        itemView.setBackgroundColor(itemView.getResources().getColor(R.color.colorPrimary));
        animation.setRepeatCount(2);
        animation.setDuration(500);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                itemView.setBackground(null);
                message.isFocus = false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        itemView.startAnimation(animation);
    }

    // TODO 也用注解来做？
    public boolean checkable(UiBroadMessage message) {
        return true;
    }

    public void onRetryClick(View itemView) {
//        new MaterialDialog.Builder(fragment.getContext())
//            .content("重新发送?")
//            .negativeText("取消")
//            .positiveText("重发")
//            .onPositive((dialog, which) -> messageViewModel.resendMessage(message.message))
//            .build()
//            .show();
        messageViewModel.resendMessage(message.message);
    }

    public void OnGroupMessageReceiptClick(View itemView) {
        ((ConversationMessageBroadAdapter) adapter).onGroupMessageReceiptClick(message.message);
    }

    @MessageContextMenuBroadItem(tag = MessageContextMenuItemBroadTags.TAG_RECALL, priority = 10)
    public void recall(View itemView, UiBroadMessage message) {
        messageViewModel.recallMessage(message.message);
    }

    @MessageContextMenuBroadItem(tag = MessageContextMenuItemBroadTags.TAG_DELETE, confirm = false, priority = 11)
    public void removeMessage(View itemView, UiBroadMessage message) {

        List<String> items = new ArrayList();
        items.add("删除本地消息");
        boolean isSuperGroup = false;
        if (message.message.conversation.type == Conversation.ConversationType.Group) {
            GroupInfo groupInfo = ChatManager.Instance().getGroupInfo(message.message.conversation.target, false);
            if (groupInfo != null && groupInfo.superGroup == 1) {
                isSuperGroup = true;
            }
        }
        // 超级群组不支持远端删除
        if ((message.message.conversation.type == Conversation.ConversationType.Group && !isSuperGroup) || message.message.conversation.type == Conversation.ConversationType.Single) {
            items.add("删除远程消息");
        } else if (message.message.conversation.type == Conversation.ConversationType.SecretChat) {
            items.add("删除自己及对方消息");
        }

        new MaterialDialog.Builder(fragment.getContext())
                .items(items)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {
                        if (position == 0) {
                            messageViewModel.deleteMessage(message.message);
                        } else {
                            messageViewModel.deleteRemoteMessage(message.message);
                        }
                    }
                })
                .show();
    }

    @MessageContextMenuBroadItem(tag = MessageContextMenuItemBroadTags.TAG_FORWARD, priority = 11)
    public void forwardMessage(View itemView, UiBroadMessage message) {
        Intent intent = new Intent(fragment.getContext(), ForwardBroadActivity.class);
        intent.putExtra("message", message.message);
        fragment.startActivity(intent);
    }

    @MessageContextMenuBroadItem(tag = MessageContextMenuItemBroadTags.TAG_MULTI_CHECK, priority = 13)
    public void checkMessage(View itemView, UiBroadMessage message) {
        fragment.toggleMultiMessageMode(message);
    }

    @MessageContextMenuBroadItem(tag = MessageContextMenuItemBroadTags.TAG_CHANNEL_PRIVATE_CHAT, priority = 12)
    public void startChanelPrivateChat(View itemView, UiBroadMessage message) {
        Intent intent = ConversationBroadActivity.buildConversationIntent(fragment.getContext(), Conversation.ConversationType.Channel, message.message.conversation.target, message.message.conversation.line, message.message.sender);
        fragment.startActivity(intent);
    }

    @MessageContextMenuBroadItem(tag = MessageContextMenuItemBroadTags.TAG_QUOTE, priority = 14)
    public void quoteMessage(View itemView, UiBroadMessage message) {
        fragment.getConversationInputPanel().quoteMessage(message.message);
    }

    @MessageContextMenuBroadItem(tag = MessageContextMenuItemBroadTags.TAG_FAV, confirm = false, priority = 12)
    public void fav(View itemView, UiBroadMessage message) {
        AppServiceBroadProvider appServiceProvider = WfcUIBroadKit.getWfcUIKit().getAppServiceProvider();
        FavoriteBroadItem favoriteItem = FavoriteBroadItem.fromMessage(message.message);

        appServiceProvider.addFavoriteItem(favoriteItem, new SimpleCallback<Void>() {
            @Override
            public void onUiSuccess(Void aVoid) {
                Toast.makeText(fragment.getContext(), "收藏成功", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onUiFailure(int code, String msg) {
                Toast.makeText(fragment.getContext(), "收藏失败: " + code, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public String contextMenuTitle(Context context, String tag) {
        String title = "未设置";
        switch (tag) {
            case MessageContextMenuItemBroadTags.TAG_RECALL:
                title = "撤回";
                break;
            case MessageContextMenuItemBroadTags.TAG_DELETE:
                title = "删除";
                break;
            case MessageContextMenuItemBroadTags.TAG_FORWARD:
                title = "转发";
                break;
            case MessageContextMenuItemBroadTags.TAG_QUOTE:
                title = "引用";
                break;
            case MessageContextMenuItemBroadTags.TAG_MULTI_CHECK:
                title = "多选";
                break;
            case MessageContextMenuItemBroadTags.TAG_CHANNEL_PRIVATE_CHAT:
                title = "私聊";
                break;
            case MessageContextMenuItemBroadTags.TAG_FAV:
                title = "收藏";
                break;
            default:
                break;
        }
        return title;
    }

    @Override
    public String contextConfirmPrompt(Context context, String tag) {
        String title = "未设置";
        switch (tag) {
            case MessageContextMenuItemBroadTags.TAG_DELETE:
                title = "确认删除此消息";
                break;
            default:
                break;
        }
        return title;
    }

    @Override
    public boolean contextMenuItemFilter(UiBroadMessage uiMessage, String tag) {
        Message message = uiMessage.message;

        if (message.conversation.type == Conversation.ConversationType.SecretChat) {
            if (MessageContextMenuItemBroadTags.TAG_FORWARD.equals(tag)) {
                return true;
            }
            if (MessageContextMenuItemBroadTags.TAG_FAV.equals(tag)) {
                return true;
            }
            return false;
        }

        if (MessageContextMenuItemBroadTags.TAG_RECALL.equals(tag)) {
            if (message.conversation.type == Conversation.ConversationType.Group) {
                GroupViewBroadModel groupViewModel = ViewModelProviders.of(fragment).get(GroupViewBroadModel.class);
                GroupMember groupMember = groupViewModel.getGroupMember(message.conversation.target, ChatManager.Instance().getUserId());
                GroupMember fromMember = groupViewModel.getGroupMember(message.conversation.target, message.sender);
                if (groupMember == null || fromMember == null) {
                    return true;
                }
                // 群主允许撤回所有消息
//                if (groupMember.type == GroupMember.GroupMemberType.Owner) {
//                    return false;
//                }
//
//                // 管理员可以测试普通成员的消息
//                if (groupMember.type == GroupMember.GroupMemberType.Manager
//                    && (fromMember.type != GroupMember.GroupMemberType.Owner && fromMember.type != GroupMember.GroupMemberType.Manager)) {
//                    return false;
//                }
            }

            long delta = ChatManager.Instance().getServerDeltaTime();
            long now = System.currentTimeMillis();
            if (message.direction == MessageDirection.Send
                    && TextUtils.equals(message.sender, ChatManager.Instance().getUserId())
                    && now - (message.serverTime - delta) < BroadConfig.RECALL_TIME_LIMIT * 1000) {
                return false;
            } else {
                return true;
            }
        }

        // 只有channel 主可以发起
        if (MessageContextMenuItemBroadTags.TAG_CHANNEL_PRIVATE_CHAT.equals(tag)) {
            if (uiMessage.message.conversation.type == Conversation.ConversationType.Channel
                    && uiMessage.message.direction == MessageDirection.Receive) {
                return false;
            }
            return true;
        }

        // 只有部分消息支持引用
        if (MessageContextMenuItemBroadTags.TAG_QUOTE.equals(tag)) {
            MessageContent messageContent = message.content;
            if (messageContent instanceof TextMessageContent
                    || messageContent instanceof FileMessageContent
                    || messageContent instanceof VideoMessageContent
                    || messageContent instanceof StickerMessageContent
                    || messageContent instanceof ImageMessageContent) {
                return false;
            }
            return true;
        }

        // 只有部分消息支持收藏
        if (MessageContextMenuItemBroadTags.TAG_FAV.equals(tag)) {
            MessageContent messageContent = message.content;
            if (messageContent instanceof TextMessageContent
                    || messageContent instanceof FileMessageContent
                    || messageContent instanceof CompositeMessageContent
                    || messageContent instanceof VideoMessageContent
                    || messageContent instanceof SoundMessageContent
                    || messageContent instanceof ArticlesMessageContent
                    || messageContent instanceof ImageMessageContent) {
                return false;
            }
            return true;
        }

        return false;
    }

    private void setSenderAvatar(Message item) {
        // TODO get user info from viewModel
        String portraitUrl = null;
        if (item.conversation.type == Conversation.ConversationType.Channel && item.direction == MessageDirection.Receive) {
            ChannelInfo channelInfo = ChatManager.Instance().getChannelInfo(item.conversation.target, false);
            if (channelInfo != null) {
                portraitUrl = channelInfo.portrait;
            }
        } else {
            UserInfo userInfo = ChatManagerBroadHolder.gChatManager.getUserInfo(item.sender, false);
            if (userInfo != null) {
                portraitUrl = userInfo.portrait;
            }
        }

        if (portraitUrl == null) {
            GlobalFriendBroadBean bean = GlobalBroadInfo.INSTANCE.queryMyFriendInfo(item.sender);
            if (bean != null) {
                portraitUrl = bean.icon;
            }
        }
        if (portraitImageView != null) {
            //默认头像
            Glide.with(fragment)
                    .load(portraitUrl == null ? "" : portraitUrl)
                    .placeholder(com.cboda.baselibrary.R.mipmap.ic_logo_login)
                    .into(portraitImageView);
        }
    }

    private void setSenderName(Message item) {
        if (item.conversation.type == Conversation.ConversationType.Single) {
            nameTextView.setVisibility(View.GONE);
        } else if (item.conversation.type == Conversation.ConversationType.Group) {
            showGroupMemberAlias(message.message.conversation, message.message, message.message.sender);
        } else {
            // todo
        }
    }

    private void showGroupMemberAlias(Conversation conversation, Message message, String sender) {
        UserViewBroadModel userViewModel = ViewModelProviders.of(fragment).get(UserViewBroadModel.class);
        String hideGroupNickName = userViewModel.getUserSetting(UserSettingScope.GroupHideNickname, conversation.target);
        if ((!TextUtils.isEmpty(hideGroupNickName) && !"1".equals(hideGroupNickName)) || message.direction == MessageDirection.Send) {
            nameTextView.setVisibility(View.GONE);
            return;
        }
        nameTextView.setVisibility(View.VISIBLE);
        // TODO optimize 缓存userInfo吧
//        if (Conversation.equals(nameTextView.getTag(), sender)) {
//            return;
//        }
        nameTextView.setText(getMyGroupMemberDisplayName(conversation.target, sender));
        nameTextView.setTag(sender);
    }

    public String getMyGroupMemberDisplayName(String groupId, String memberId) {
        UserInfo userInfo = ChatManager.Instance().getUserInfo(memberId, groupId, false);
        if (userInfo != null) {
            if (!TextUtils.isEmpty(userInfo.groupAlias)) {
                return userInfo.groupAlias;
            } else if (!TextUtils.isEmpty(userInfo.friendAlias)) {
                return userInfo.friendAlias;
            } else if (!TextUtils.isEmpty(userInfo.displayName)) {
                return userInfo.displayName;
            }
        }
        GlobalFriendBroadBean globalFriendBean = GlobalBroadInfo.INSTANCE.queryMyFriendInfo(memberId);
        if (null != globalFriendBean) {
            if (!TextUtils.isEmpty(globalFriendBean.remarkName)) {
                return globalFriendBean.remarkName;
            } else if (!TextUtils.isEmpty(globalFriendBean.nickName)) {
                return globalFriendBean.nickName;
            }
        }
        return "<" + memberId + ">";
    }

    protected boolean showMessageReceipt(Message message) {
        ContentTag tag = message.content.getClass().getAnnotation(ContentTag.class);
        return (tag != null && (tag.flag() == PersistFlag.Persist_And_Count));
    }

    protected void setSendStatus(Message item) {
        MessageStatus sentStatus = item.status;
//        ELogUtilKt.eLogD("zhanglong","item.direction=="+item.direction+"---"+sentStatus);
        if (item.direction == MessageDirection.Receive) {
            return;
        }
        if (sentStatus == MessageStatus.Sending) {
            progressBar.setVisibility(View.VISIBLE);
            errorLinearLayout.setVisibility(View.GONE);
            return;
        } else if (sentStatus == MessageStatus.Send_Failure) {
            progressBar.setVisibility(View.GONE);
            errorLinearLayout.setVisibility(View.VISIBLE);
            return;
        } else if (sentStatus == MessageStatus.Sent) {
            progressBar.setVisibility(View.GONE);
            errorLinearLayout.setVisibility(View.GONE);
        } else if (sentStatus == MessageStatus.Readed) {
            progressBar.setVisibility(View.GONE);
            errorLinearLayout.setVisibility(View.GONE);
            return;
        }
//        ELogUtilKt.eLogD("zhanglong",ChatManager.Instance().isReceiptEnabled()+"==="+ChatManager.Instance().isUserEnableReceipt()+"==="+showMessageReceipt(message.message));
        if (!ChatManager.Instance().isReceiptEnabled() || !ChatManager.Instance().isUserEnableReceipt() || !showMessageReceipt(message.message)) {
            return;
        }

        Map<String, Long> readEntries = ((ConversationMessageBroadAdapter) adapter).getReadEntries();

        if (item.conversation.type == Conversation.ConversationType.Single) {
            singleReceiptImageView.setVisibility(View.VISIBLE);
            groupReceiptFrameLayout.setVisibility(View.GONE);
            Long readTimestamp = readEntries != null && !readEntries.isEmpty() ? readEntries.get(message.message.conversation.target) : null;

            if (readTimestamp != null && readTimestamp >= message.message.serverTime) {
                //已读状态
                singleReceiptImageView.setImageResource(R.drawable.bg_circle_read_broad_icon);
            } else {
                singleReceiptImageView.setImageResource(R.drawable.bg_circle_unread_broad_icon);
            }
        } else if (item.conversation.type == Conversation.ConversationType.Group) {
            singleReceiptImageView.setVisibility(View.GONE);

            if (sentStatus == MessageStatus.Sent) {
                if (item.content instanceof CallStartMessageContent || (item.content.getPersistFlag().ordinal() & 0x2) == 0) {
                    groupReceiptFrameLayout.setVisibility(View.GONE);
                } else {
                    groupReceiptFrameLayout.setVisibility(View.VISIBLE);
                }
//                int deliveryCount = 0;
//                if (deliveries != null) {
//                    for (Map.Entry<String, Long> delivery : deliveries.entrySet()) {
//                        if (delivery.getValue() >= item.serverTime) {
//                            deliveryCount++;
//                        }
//                    }
//                }
                int readCount = 0;
                if (readEntries != null) {
                    for (Map.Entry<String, Long> readEntry : readEntries.entrySet()) {
                        if (readEntry.getValue() >= item.serverTime) {
                            readCount++;
                        }
                    }
                }

                GroupInfo groupInfo = ChatManager.Instance().getGroupInfo(item.conversation.target, false);
                if (groupInfo == null) {
                    return;
                }
//                deliveryProgressBar.setMax(groupInfo.memberCount - 1);
//                deliveryProgressBar.setProgress(deliveryCount);
                readProgressBar.setMax(groupInfo.memberCount - 1);
                readProgressBar.setProgress(readCount);
            } else {
                groupReceiptFrameLayout.setVisibility(View.GONE);
            }
        }
    }
}
