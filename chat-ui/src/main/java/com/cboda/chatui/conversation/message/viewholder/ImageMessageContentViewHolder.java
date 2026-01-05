/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.conversation.message.viewholder;

import static com.cboda.baselibrary.utils.GlideUtilsKt.loadDiyImg;
import static com.cboda.baselibrary.utils.GlideUtilsKt.loadDiyImg2;
import static com.blankj.utilcode.util.SizeUtils.dp2px;

import android.view.View;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.cboda.chatui.*;
import com.cboda.chatui.annotation.EnableContextMenu;
import com.cboda.chatui.annotation.MessageContentType;
import com.cboda.chatui.conversation.ConversationFragment;
import com.cboda.chatui.conversation.message.model.UiMessage;
import com.cboda.chatui.utils.DownloadManager;
import com.cboda.chatui.utils.FileUtils;
import cn.wildfirechat.message.ImageMessageContent;
import cn.wildfirechat.message.Message;
import cn.wildfirechat.message.core.MessageDirection;
import cn.wildfirechat.message.core.MessageStatus;
import cn.wildfirechat.model.Conversation;
import cn.wildfirechat.model.ImgBean;
import cn.wildfirechat.remote.ChatManager;

/**
 * 图片尺寸展示高仿微信
 * 占位图-缩略图-原图
 */
@MessageContentType(ImageMessageContent.class)
@EnableContextMenu
public class ImageMessageContentViewHolder extends MediaMessageContentViewHolder {

    private static final String TAG = "ImageMessageContentView";
    ImageView imageView;

    private String imagePath;

    public ImageMessageContentViewHolder(ConversationFragment fragment, RecyclerView.Adapter adapter, View itemView) {
        super(fragment, adapter, itemView);
        bindViews(itemView);
        bindEvents(itemView);
    }

    private void bindEvents(View itemView) {
        itemView.findViewById(R.id.imageView).setOnClickListener(_v -> preview());
    }

    private void bindViews(View itemView) {
        imageView = itemView.findViewById(R.id.imageView);
    }

    @Override
    public void onBind(UiMessage message) {
        ImageMessageContent imageMessage = (ImageMessageContent) message.message.content;
//        Bitmap thumbnail = imageMessage.getThumbnail();
//        int imageSize[] = WeChatImageUtils.getImageSizeByOrgSizeToWeChat((int) imageMessage.getImageWidth(), (int) imageMessage.getImageHeight());
//        int width = imageSize[0] > 0 ? imageSize[0] : 200;
//        int height = imageSize[1] > 0 ? imageSize[1] : 200;
//        imageView.getLayoutParams().width = width;
//        imageView.getLayoutParams().height = height;
        if (FileUtils.isFileExists(imageMessage.localPath)) {
            imagePath = imageMessage.localPath;
        } else {
            imagePath = imageMessage.remoteUrl;
        }
        if (message.message.conversation.type == Conversation.ConversationType.SecretChat) {
            imagePath = DownloadManager.buildSecretChatMediaUrl(message.message);
        }
//        loadMedia(thumbnail, imagePath, imageView);
        if (FileUtils.isFileExists(imagePath)) {
            loadDiyImg(imageView, imagePath, dp2px(140f), dp2px(140f), () -> null);
        } else {
            ImgBean imgBean = imageMessage.getImgBean();
            loadDiyImg2(imageView, imagePath, dp2px(140f), dp2px(140f), imgBean.getImgWidth(), imgBean.getImgHeight(), imgBean.getThumbnail(), () -> null);
        }
    }

    void preview() {
        if (message.message.direction == MessageDirection.Receive && message.message.status != MessageStatus.Played) {
            message.message.status = MessageStatus.Played;
            ChatManager.Instance().setMediaMessagePlayed(message.message.messageId);
        }
        previewMM();
    }

    @Override
    protected void setSendStatus(Message item) {
        super.setSendStatus(item);
//        MessageContent msgContent = item.content;
//        if (msgContent instanceof ImageMessageContent) {
//            boolean isSend = item.direction == MessageDirection.Send;
//            if (isSend) {
//                MessageStatus sentStatus = item.status;
//                if (sentStatus == MessageStatus.Sending) {
//                    imageView.setPercent(message.progress);
//                    imageView.setProgressVisible(true);
//                    imageView.showShadow(true);
//                } else if (sentStatus == MessageStatus.Send_Failure) {
//                    imageView.setProgressVisible(false);
//                    imageView.showShadow(false);
//                } else if (sentStatus == MessageStatus.Sent) {
//                    imageView.setProgressVisible(false);
//                    imageView.showShadow(false);
//                }
//            } else {
//                imageView.setProgressVisible(false);
//                imageView.showShadow(false);
//            }
//        }
    }

}
