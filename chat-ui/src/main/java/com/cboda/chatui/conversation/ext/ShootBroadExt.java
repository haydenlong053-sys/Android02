/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.conversation.ext;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.cboda.baselibrary.popup.PermissionExplainBroadPopup;
import com.cboda.baselibrary.utils.AppPermissionBroadUtils;

import java.io.File;

import com.cboda.chatui.R;
import com.cboda.chatui.WfcBaseBroadActivity;
import com.cboda.chatui.annotation.ExtContextMenuBroadItem;
import com.cboda.chatui.conversation.ext.core.ConversationBroadExt;
import com.cboda.chatui.conversation.ext.core.ConversationMidExt;
import com.cboda.chatui.mm.TakePhotoBroadActivity;
import com.cboda.chatui.third.utils.ImageBroadUtils;
import cn.wildfirechat.message.TypingMessageContent;
import cn.wildfirechat.model.Conversation;

public class ShootBroadExt extends ConversationMidExt {

    /**
     * @param containerView 扩展view的container
     * @param conversation
     */
    @ExtContextMenuBroadItem
    public void shoot(View containerView, Conversation conversation) {
        boolean hasPermission = AppPermissionBroadUtils.INSTANCE.hasCameraPermission(activity);
        boolean hasAudioPerMission=AppPermissionBroadUtils.INSTANCE.hasAudioPermission(activity);
        if (hasPermission&&hasAudioPerMission) {
            Intent intent = new Intent(activity, TakePhotoBroadActivity.class);
            startActivityForResult(intent, 100);
            TypingMessageContent content = new TypingMessageContent(TypingMessageContent.TYPING_CAMERA);
            messageViewModel.sendMessage(conversation, content);
        } else {
            new PermissionExplainBroadPopup.BroadBuilder(activity)
                    .setTitle(activity.getString(R.string.shoot_warm_reminder))
                    .addPermissionExplain(activity.getString(R.string.shoot_camera_permission_explain))
                    .addPermissionExplain(activity.getString(R.string.shoot_microphone_permission_explain))
                    .setOnConfirm(new Runnable() {
                        @Override
                        public void run() {
                                    String[] permissions = new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA};
                            AppPermissionBroadUtils.INSTANCE.requestPermissions(activity,permissions,1002);
                        }
                    })
                    .show();

        }
//        String[] permissions = new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA};
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (!((WfcBaseBroadActivity) activity).checkPermission(permissions)) {
//                activity.requestPermissions(permissions, 100);
//                return;
//            }
//        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            String path = data.getStringExtra("path");
            if (TextUtils.isEmpty(path)) {
                Toast.makeText(activity, activity.getString(R.string.shoot_error_feedback), Toast.LENGTH_SHORT).show();
                return;
            }
            if (data.getBooleanExtra("take_photo", true)) {
                //照片
                File file = new File(path);
                messageViewModel.sendImgMsg(conversation, ImageBroadUtils.genThumbImgFile(path), file);
                ImageBroadUtils.saveMedia2Album(fragment.getContext(), file, true);
            } else {
                //小视频
                File file = new File(path);
                messageViewModel.sendVideoMsg(conversation, new File(path),activity);
                ImageBroadUtils.saveMedia2Album(fragment.getContext(), file, false);
            }
        }
    }

    @Override
    public int priority() {
        return 100;
    }

    @Override
    public int iconResId() {
        return R.mipmap.ic_func_broad_shot;
    }

    @Override
    public String title(Context context) {
        return context.getString(R.string.shot);
    }

    @Override
    public String contextMenuTitle(Context context, String tag) {
        return title(context);
    }
}
