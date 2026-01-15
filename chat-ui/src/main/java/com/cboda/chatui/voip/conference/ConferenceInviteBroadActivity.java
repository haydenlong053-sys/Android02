/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.voip.conference;

import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import com.cboda.chatui.conversation.forward.ForwardPromptBroadView;
import com.cboda.chatui.conversation.pick.PickOrCreateConversationBroadActivity;
import com.cboda.chatui.group.GroupViewBroadModel;
import com.cboda.chatui.user.UserViewBroadModel;
import com.cboda.chatui.viewmodel.MessageViewBroadModel;
import cn.wildfirechat.message.ConferenceInviteMessageContent;
import cn.wildfirechat.message.Message;
import cn.wildfirechat.message.TextMessageContent;
import cn.wildfirechat.model.Conversation;
import cn.wildfirechat.model.GroupInfo;
import cn.wildfirechat.model.UserInfo;

public class ConferenceInviteBroadActivity extends PickOrCreateConversationBroadActivity {
    private ConferenceInviteMessageContent inviteMessage;
    private MessageViewBroadModel messageViewModel;
    private UserViewBroadModel userViewModel;
    private GroupViewBroadModel groupViewModel;

    @Override
    protected void afterViews() {
        super.afterViews();
        inviteMessage = getIntent().getParcelableExtra("inviteMessage");
        messageViewModel = new ViewModelProvider(this).get(MessageViewBroadModel.class);
        userViewModel = new ViewModelProvider(this).get(UserViewBroadModel.class);
        groupViewModel = new ViewModelProvider(this).get(GroupViewBroadModel.class);
    }

    @Override
    protected void onPickOrCreateConversation(Conversation conversation) {
        invite(conversation);
    }

    public void invite(Conversation conversation) {
        switch (conversation.type) {
            case Single:
                UserInfo userInfo = userViewModel.getUserInfo(conversation.target, false);
                invite(userInfo.displayName, userInfo.portrait, conversation);
                break;
            case Group:
                GroupInfo groupInfo = groupViewModel.getGroupInfo(conversation.target, false);
                invite(!TextUtils.isEmpty(groupInfo.remark) ? groupInfo.remark : groupInfo.name, groupInfo.portrait, conversation);
                break;
            default:
                break;
        }

    }

    private void invite(String targetName, String targetPortrait, Conversation targetConversation) {
        ForwardPromptBroadView view = new ForwardPromptBroadView(this);
        view.bind(targetName, targetPortrait, "会议邀请");
        MaterialDialog dialog = new MaterialDialog.Builder(this)
            .customView(view, false)
            .negativeText("取消")
            .positiveText("发送")
            .onPositive(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    Message extraMsg = null;
                    if (!TextUtils.isEmpty(view.getEditText())) {
                        TextMessageContent content = new TextMessageContent(view.getEditText());
                        extraMsg = new Message();
                        extraMsg.content = content;
                    }
                    messageViewModel.sendMessage(targetConversation, inviteMessage);
                    Toast.makeText(ConferenceInviteBroadActivity.this, "邀请成功", Toast.LENGTH_SHORT).show();
                    finish();
//                        .observe(ConferenceInviteBroadActivity.this, new Observer<OperateBroadResult<Integer>>() {
//                            @Override
//                            public void onChanged(@Nullable OperateBroadResult<Integer> integerOperateResult) {
//                                if (integerOperateResult.isSuccess()) {
//                                    Toast.makeText(ConferenceInviteBroadActivity.this, "邀请成功", Toast.LENGTH_SHORT).show();
//                                    finish();
//                                } else {
//                                    Toast.makeText(ConferenceInviteBroadActivity.this, "邀请失败" + integerOperateResult.getErrorCode(), Toast.LENGTH_SHORT).show();
//                                }
//                            }
//                        });
                }
            })
            .build();
        dialog.show();
    }
}
