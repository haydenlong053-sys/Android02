/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.conversation.forward;

import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import com.cboda.chatui.bridge.YeHuoBroadEvent;
import com.cboda.chatui.common.OperateBroadResult;
import com.cboda.chatui.conversation.pick.PickOrCreateConversationBroadActivity;
import com.cboda.chatui.dialog.ShareBroadPop;
import com.cboda.chatui.group.GroupViewBroadModel;
import com.cboda.chatui.user.UserViewBroadModel;
import com.cboda.chatui.utils.Util;
import com.cboda.chatui.utils.WfcTextBroadUtils;
import cn.wildfirechat.message.CompositeMessageContent;
import cn.wildfirechat.message.ImageMessageContent;
import cn.wildfirechat.message.Message;
import cn.wildfirechat.message.TextMessageContent;
import cn.wildfirechat.message.VideoMessageContent;
import cn.wildfirechat.model.Conversation;
import cn.wildfirechat.model.GroupInfo;
import cn.wildfirechat.model.UserInfo;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;

public class ForwardBroadActivity extends PickOrCreateConversationBroadActivity {
    private List<Message> messages;
    private ForwardViewBroadModel forwardViewModel;
    private UserViewBroadModel userViewModel;
    private GroupViewBroadModel groupViewModel;

    @Override
    protected void afterViews() {
        super.afterViews();
        messages = getIntent().getParcelableArrayListExtra("messages");
        if (messages == null || messages.isEmpty()) {
            Message message = getIntent().getParcelableExtra("message");
            if (message != null) {
                messages = new ArrayList<>();
                messages.add(message);
            }
        }
        if (messages == null || messages.isEmpty()) {
            finish();
        }
        forwardViewModel = ViewModelProviders.of(this).get(ForwardViewBroadModel.class);
        userViewModel = ViewModelProviders.of(this).get(UserViewBroadModel.class);
        groupViewModel = ViewModelProviders.of(this).get(GroupViewBroadModel.class);
    }

    @Override
    protected void onPickOrCreateConversation(Conversation conversation) {
        forward(conversation);
    }

    public void forward(Conversation conversation) {
        switch (conversation.type) {
            case Single:
                UserInfo userInfo = userViewModel.getUserInfo(conversation.target, false);
                UserInfo myUserInfo = Util.getMyUserDisplayName(conversation.target, userInfo);
                forward(myUserInfo.displayName, myUserInfo.portrait, conversation);
                break;
            case Group:
                GroupInfo groupInfo = groupViewModel.getGroupInfo(conversation.target, false);
                forward(!TextUtils.isEmpty(groupInfo.remark) ? groupInfo.remark : groupInfo.name, groupInfo.portrait, conversation);
                break;
            default:
                break;
        }

    }

    private void forward(String targetName, String targetPortrait, Conversation targetConversation) {
        ForwardPromptBroadView view = new ForwardPromptBroadView(this);
        if (messages.size() == 1) {
            Message message = messages.get(0);
            if (message.content instanceof ImageMessageContent) {
                view.bind(targetName, targetPortrait, ((ImageMessageContent) message.content).getThumbnail());
            } else if (message.content instanceof VideoMessageContent) {
                view.bind(targetName, targetPortrait, ((VideoMessageContent) message.content).getThumbnail());
            } else if (message.content instanceof CompositeMessageContent) {
                view.bind(targetName, targetPortrait, "[聊天记录]: " + ((CompositeMessageContent) message.content).getTitle());
            } else {
                view.bind(targetName, targetPortrait, WfcTextBroadUtils.htmlToText(message.digest()));
            }
        } else {
            view.bind(targetName, targetPortrait, "[逐条转发]共" + messages.size() + "条消息");
        }
        ShareBroadPop sharePop =new ShareBroadPop(this,view);
        sharePop.setSureInvoke(new Function0<Unit>() {
            @Override
            public Unit invoke() {
                Message extraMsg = null;
                if (!TextUtils.isEmpty(view.getEditText())) {
                    TextMessageContent content = new TextMessageContent(view.getEditText());
                    extraMsg = new Message();
                    extraMsg.content = content;
                }
                if (extraMsg != null) {
                    messages.add(extraMsg);
                }
                forwardViewModel.forward(targetConversation, messages.toArray(new Message[0]))
                        .observe(ForwardBroadActivity.this, new Observer<OperateBroadResult<Integer>>() {
                            @Override
                            public void onChanged(@Nullable OperateBroadResult<Integer> integerOperateResult) {
                                if (integerOperateResult.isSuccess()) {
                                    EventBus.getDefault().post(new YeHuoBroadEvent("reloadMsgZhuanFa","",""));
                                    Toast.makeText(ForwardBroadActivity.this, "转发成功", Toast.LENGTH_SHORT).show();
                                    finish();
                                } else {
                                    Toast.makeText(ForwardBroadActivity.this, "转发失败" + integerOperateResult.getErrorCode(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                return null;
            }
        });
        sharePop.showPopupWindow();

//        MaterialDialog dialog = new MaterialDialog.Builder(this)
//            .customView(view, false)
//            .negativeText("取消")
//            .positiveText("发送")
//            .onPositive(new MaterialDialog.SingleButtonCallback() {
//                @Override
//                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
//                    Message extraMsg = null;
//                    if (!TextUtils.isEmpty(view.getEditText())) {
//                        TextMessageContent content = new TextMessageContent(view.getEditText());
//                        extraMsg = new Message();
//                        extraMsg.content = content;
//                    }
//                    if (extraMsg != null) {
//                        messages.add(extraMsg);
//                    }
//                    forwardViewModel.forward(targetConversation, messages.toArray(new Message[0]))
//                        .observe(ForwardBroadActivity.this, new Observer<OperateBroadResult<Integer>>() {
//                            @Override
//                            public void onChanged(@Nullable OperateBroadResult<Integer> integerOperateResult) {
//                                if (integerOperateResult.isSuccess()) {
//                                    EventBus.getDefault().post(new YeHuoBroadEvent("reloadMsgZhuanFa","",""));
//                                    Toast.makeText(ForwardBroadActivity.this, "转发成功", Toast.LENGTH_SHORT).show();
//                                    finish();
//                                } else {
//                                    Toast.makeText(ForwardBroadActivity.this, "转发失败" + integerOperateResult.getErrorCode(), Toast.LENGTH_SHORT).show();
//                                }
//                            }
//                        });
//
//                }
//            })
//            .build();
//        dialog.show();
    }
}
