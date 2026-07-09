/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.conversation.ext;

import android.Manifest;
import android.content.Context;
import android.os.Build;
import android.view.View;

import com.cboda.baselibrary.toast.ToastBroadUtils;
import com.freddy.webtrc.bean.CallTokenBean;
import com.freddy.webtrc.utils.CallUtil;

import com.cboda.chatui.R;
import com.cboda.chatui.annotation.ExtContextMenuBroadItem;
import com.cboda.chatui.conversation.ConversationBroadFragment;
import com.cboda.chatui.conversation.ext.core.ConversationBroadExt;
import com.cboda.chatui.conversation.ext.core.ConversationMidExt;
import com.freddy.webtrc.utils.BroadE;

import cn.wildfirechat.avenginekit.AVEngineKit;
import cn.wildfirechat.model.Conversation;
import cn.wildfirechat.model.UserInfo;
import cn.wildfirechat.remote.ChatManager;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class VoipBroadExt extends ConversationMidExt {

//    @ExtContextMenuBroadItem(tag = ConversationExtMenuBroadTags.TAG_VOIP_VIDEO)
//    public void video(View containerView, Conversation conversation) {
//        String[] permissions;
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
//            permissions = new String[]{
//                Manifest.permission.RECORD_AUDIO,
//                Manifest.permission.CAMERA,
//                Manifest.permission.BLUETOOTH_CONNECT
//            };
//        } else {
//            permissions = new String[]{
//                Manifest.permission.RECORD_AUDIO,
//                Manifest.permission.CAMERA
//            };
//        }
//        String[] notGrantedPermissions = checkPermissions(permissions);
//        if (notGrantedPermissions.length > 0) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                activity.requestPermissions(notGrantedPermissions, 100);
//            }
//            return;
//        }
//        switch (conversation.type) {
//            case Single:
//                videoChat(conversation.target);
//                break;
//            case Group:
//                ((ConversationBroadFragment) fragment).pickGroupMemberToVoipChat(false);
//                break;
//            default:
//                break;
//        }
//    }

    //    @ExtContextMenuBroadItem(tag = ConversationExtMenuBroadTags.TAG_VOIP_AUDIO)
    @ExtContextMenuBroadItem
    public void audio(View containerView, Conversation conversation) {
        String[] permissions;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            permissions = new String[]{
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.BLUETOOTH_CONNECT
            };
        } else {
            permissions = new String[]{
                    Manifest.permission.RECORD_AUDIO
            };
        }
        String[] notGrantedPermissions = checkPermissions(permissions);
        if (notGrantedPermissions.length > 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ToastBroadUtils.show(activity.getString(R.string.voip_permission_request));
                activity.requestPermissions(notGrantedPermissions, 100);
                return;
            }
        }
        switch (conversation.type) {
            case Single:
                audioChat(conversation.target);
                break;
            case Group:
                ((ConversationBroadFragment) fragment).pickGroupMemberToVoipChat(true);
                break;
            default:
                break;
        }
    }

    private void audioChat(String targetId) {
        startVoipCall(targetId, false);
//        WfcUIBroadKit.singleCall(activity, targetId, true);
    }

    private void videoChat(String targetId) {
        startVoipCall(targetId, true);
//        WfcUIBroadKit.singleCall(activity, targetId, false);
    }

    private void startVoipCall(String targetId, boolean video) {
        BroadE.INSTANCE.startCall(targetId, video, new Function1<CallTokenBean, Unit>() {
            @Override
            public Unit invoke(CallTokenBean callTokenBean) {
                if (callTokenBean != null) {
                    CallUtil.INSTANCE.joinRoom(callTokenBean.getUrl(), callTokenBean.getToken(), activity);
                } else {
                    ToastBroadUtils.show(activity.getString(R.string.call_network_error));
                }
                return Unit.INSTANCE;
            }
        });
    }

    @Override
    public int priority() {
        return 99;
    }

    @Override
    public int iconResId() {
        return R.mipmap.ic_func_broad_video;
    }

    @Override
    public boolean filter(Conversation conversation) {
        if ((conversation.type == Conversation.ConversationType.Group && AVEngineKit.isSupportMultiCall())) {
            return false;
        }

        if (conversation.type == Conversation.ConversationType.Single) {
            UserInfo userInfo = ChatManager.Instance().getUserInfo(conversation.target, false);
            // robot
            if (userInfo.type == 1) {
                return true;
            }
            return false;
        }
        return true;
    }


    @Override
    public String title(Context context) {
        return context.getString(R.string.yuyintonghua);
    }


    @Override
    public String contextMenuTitle(Context context, String tag) {
//        if (ConversationExtMenuBroadTags.TAG_VOIP_AUDIO.equals(tag)) {
//            return "语音通话";
//        } else {
//            return "视频通话";
//        }
        return title(context);
    }
}
