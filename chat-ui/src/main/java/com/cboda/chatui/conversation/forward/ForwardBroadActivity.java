/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.conversation.forward;

import android.graphics.Bitmap;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import com.cboda.baselibrary.bean.GlobalBroadInfo;
import com.cboda.baselibrary.utils.MMKVBroadUtils;
import com.cboda.chatui.R;
import com.cboda.chatui.bridge.MyBroadEvent;
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
import cn.wildfirechat.model.NullUserInfo;
import cn.wildfirechat.model.UserInfo;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import com.cboda.baselibrary.common.MyEventCodeEnum;

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
        forwardViewModel = new ViewModelProvider(this).get(ForwardViewBroadModel.class);
        userViewModel = new ViewModelProvider(this).get(UserViewBroadModel.class);
        groupViewModel = new ViewModelProvider(this).get(GroupViewBroadModel.class);
    }

    @Override
    protected void onPickOrCreateConversation(Conversation conversation) {
        forward(conversation);
    }

    public void forward(Conversation conversation) {
        switch (conversation.type) {
            case Single:
                UserInfo userInfo = userViewModel.getUserInfo(conversation.target, false);
//                UserInfo myUserInfo = Util.getMyUserDisplayName(conversation.target, userInfo);
                if (userInfo == null) {
                    userInfo = new UserInfo();
                }
                UserInfo finalUserInfo = userInfo;
                if (conversation.target.contains(MMKVBroadUtils.Companion.getKeFuId())) {
                    finalUserInfo.displayName = MMKVBroadUtils.Companion.getKeFuName();
                    forward(finalUserInfo.displayName, finalUserInfo.portrait, conversation);
                } else if (!TextUtils.isEmpty(finalUserInfo.friendAlias)) {
                    finalUserInfo.displayName = finalUserInfo.friendAlias;
                    forward(finalUserInfo.displayName, finalUserInfo.portrait, conversation);
                } else if (!(finalUserInfo instanceof NullUserInfo) && !TextUtils.isEmpty(finalUserInfo.displayName)) {
                    forward(finalUserInfo.displayName, finalUserInfo.portrait, conversation);
                } else {
                    GlobalBroadInfo.INSTANCE.queryMyFriendInfo(conversation.target, null, friendSmailListBean -> {
                        if (friendSmailListBean != null) {
                            finalUserInfo.portrait = friendSmailListBean.icon;
                            if (!TextUtils.isEmpty(friendSmailListBean.remarkName)) {
                                finalUserInfo.displayName = friendSmailListBean.remarkName;
                            } else if (!TextUtils.isEmpty(friendSmailListBean.nickName)) {
                                finalUserInfo.displayName = friendSmailListBean.nickName;
                            }
                        } else {
                            finalUserInfo.displayName = "<" + finalUserInfo.uid + ">";
                        }
                        forward(finalUserInfo.displayName, finalUserInfo.portrait, conversation);
                        return null;
                    });

                }

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
                Bitmap thumbnail = ((ImageMessageContent) message.content).getThumbnail();
                if (thumbnail==null){
                    view.bind2(targetName, targetPortrait, ((ImageMessageContent) message.content).remoteUrl,((int) ((ImageMessageContent) message.content).getImageWidth()),((int) ((ImageMessageContent) message.content).getImageHeight()));
                }else {
                    view.bind(targetName, targetPortrait, thumbnail);
                }

            } else if (message.content instanceof VideoMessageContent) {
                Bitmap thumbnail = ((VideoMessageContent) message.content).getThumbnail();
                if (thumbnail==null){
                    view.bind2(targetName, targetPortrait, ((VideoMessageContent) message.content).remoteUrl,0,0);
                }else {
                    view.bind3(targetName, targetPortrait, thumbnail);
                }
            } else if (message.content instanceof CompositeMessageContent) {
                view.bind(targetName, targetPortrait, getString(com.cboda.baselibrary.R.string.base_digest_chat_record) + ((CompositeMessageContent) message.content).getTitle());
            } else {
                view.bind(targetName, targetPortrait, WfcTextBroadUtils.htmlToText(message.digest()));
            }
        } else {
            view.bind(targetName, targetPortrait, getString(R.string.msg_forward_one_by_one_with_count, messages.size()));
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
                                    EventBus.getDefault().post(new MyBroadEvent(MyEventCodeEnum.RELOAD_FORWARD_MESSAGE.getValue(),"",""));
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
//                                    EventBus.getDefault().post(new MyBroadEvent(MyEventCodeEnum.RELOAD_FORWARD_MESSAGE.getValue(),"",""));
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
