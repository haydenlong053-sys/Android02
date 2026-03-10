/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.conversation.ext;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import com.cboda.chatui.R;
import com.cboda.chatui.annotation.ExtContextMenuBroadItem;
import com.cboda.chatui.bridge.MyCommonBroadBridge;
import com.cboda.chatui.conversation.ext.core.ConversationBroadExt;
import com.cboda.chatui.conversation.ext.core.ConversationMidExt;
import com.cboda.chatui.conversation.forward.ForwardPromptBroadView;
import cn.wildfirechat.message.CardMessageContent;
import cn.wildfirechat.message.TextMessageContent;
import cn.wildfirechat.model.ChannelInfo;
import cn.wildfirechat.model.Conversation;
import cn.wildfirechat.model.GroupInfo;
import cn.wildfirechat.model.UserInfo;
import cn.wildfirechat.remote.ChatManager;
import com.cboda.baselibrary.common.BridgeImEnum;

public class UserCardBroadExt extends ConversationMidExt {

    /**
     * @param containerView 扩展view的container
     * @param conversation
     */
    @ExtContextMenuBroadItem
    public void pickContact(View containerView, Conversation conversation) {
//        Intent intent = new Intent(fragment.getActivity(), ContactListActivity.class);
//        if (conversation.type == Conversation.ConversationType.Single) {
//            ArrayList<String> filterUserList = new ArrayList<>();
//            filterUserList.add(conversation.target);
//            intent.putExtra(ContactListActivity.FILTER_USER_LIST, filterUserList);
//        }
//        startActivityForResult(intent, 100);

        if (conversation.type == Conversation.ConversationType.Group) {
            MyCommonBroadBridge.getInstance().setMyBridgeMethod(fragment.getActivity(), BridgeImEnum.BUSINESS_CARD.getValue(),"",conversation.target,null);
        } else {
            MyCommonBroadBridge.getInstance().setMyBridgeMethod(fragment.getActivity(), BridgeImEnum.BUSINESS_CARD.getValue(),conversation.target,"",null);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            UserInfo userInfo = data.getParcelableExtra("userInfo");
            ChannelInfo channelInfo = data.getParcelableExtra("channelInfo");
            if (userInfo != null) {
                sendUserCard(0, userInfo.uid, userInfo.name, userInfo.displayName, userInfo.portrait);
            } else if (channelInfo != null) {
                sendUserCard(3, channelInfo.channelId, channelInfo.name, channelInfo.name, channelInfo.portrait);
            }
        }
    }

    /**
     * @param type
     * @param target
     * @param name        用户名，只有显示联系人名片时有效
     * @param displayName
     * @param portrait
     */
    private void sendUserCard(int type, String target, String name, String displayName, String portrait) {
        ForwardPromptBroadView view = new ForwardPromptBroadView(fragment.getActivity());
        String desc = "";
        switch (type) {
            case 0:
                desc = fragment.getString(R.string.msg_personal_card);
                break;
            case 1:
                desc = fragment.getString(R.string.msg_group);
                break;
            case 2:
                desc = fragment.getString(R.string.msg_chat_room);
                break;
            case 3:
                desc = fragment.getString(R.string.msg_channel);
                break;
            default:
                break;
        }

        desc += displayName;

        if (conversation.type == Conversation.ConversationType.Single) {
            UserInfo targetUser = ChatManager.Instance().getUserInfo(conversation.target, false);
            view.bind(targetUser.displayName, portrait, desc);
        } else if (conversation.type == Conversation.ConversationType.Group) {
            GroupInfo groupInfo = ChatManager.Instance().getGroupInfo(conversation.target, false);
            view.bind(!TextUtils.isEmpty(groupInfo.remark) ? groupInfo.remark : groupInfo.name, groupInfo.portrait, desc);
        }
        MaterialDialog dialog = new MaterialDialog.Builder(fragment.getActivity())
            .customView(view, false)
            .negativeText(fragment.getString(R.string.quxiao))
            .positiveText(fragment.getString(R.string.send))
            .onPositive(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    CardMessageContent cardMessageContent = new CardMessageContent(type, target, displayName, portrait, ChatManager.Instance().getUserId());
                    cardMessageContent.setName(name);
                    messageViewModel.sendMessage(conversation, cardMessageContent,false);
                    if (!TextUtils.isEmpty(view.getEditText())) {
                        TextMessageContent content = new TextMessageContent(view.getEditText());
                        messageViewModel.sendMessage(conversation, content,false);
                    }
                    dialog.dismiss();
                }
            })
            .build();
        dialog.show();
    }

    @Override
    public int priority() {
        return 100;
    }

    @Override
    public int iconResId() {
        return R.mipmap.ic_user_broad_card;
    }

    @Override
    public String title(Context context) {
        return context.getString(R.string.msg_business_card);
    }

    @Override
    public String contextMenuTitle(Context context, String tag) {
        return title(context);
    }
}
