/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.conversation.message.viewholder;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.cboda.chatui.R;
import com.cboda.chatui.annotation.EnableContextBroadMenu;
import com.cboda.chatui.annotation.MessageContentBroadType;
import com.cboda.chatui.bridge.MyCommonBroadBridge;
import com.cboda.chatui.conversation.ConversationBroadFragment;
import com.cboda.chatui.conversation.message.model.UiBroadMessage;
import com.cboda.chatui.third.utils.TimeConvertBroadUtils;
import com.cboda.chatui.utils.FileBroadUtils;
import com.cboda.chatui.widget.BubbleImageBroadView;
import cn.wildfirechat.message.VideoMessageContent;
import cn.wildfirechat.message.core.MessageDirection;
import cn.wildfirechat.message.core.MessageStatus;
import cn.wildfirechat.remote.ChatManager;
import cn.wildfirechat.utils.WeChatImageUtils;
import com.cboda.baselibrary.common.BridgeImEnum;

/**
 * 小视频尺寸展示高仿微信,并且带上时间
 * 占位图-缩略图-原图
 */
@MessageContentBroadType(VideoMessageContent.class)
@EnableContextBroadMenu
public class VideoMessageContentViewBroadHolder extends MediaMessageContentViewBroadHolder {
    private static final String TAG = "VideoMessageContentView";
    BubbleImageBroadView imageView;
    ImageView playImageView;

    TextView time_tv;

    private  String imagePath ;

    public VideoMessageContentViewBroadHolder(ConversationBroadFragment fragment, RecyclerView.Adapter adapter, View itemView) {
        super(fragment, adapter, itemView);
        bindViews(itemView);
        bindEvents(itemView);
    }

    private void bindEvents(View itemView) {
       itemView.findViewById(R.id.videoContentLayout).setOnClickListener(_v -> play());
    }

    private void bindViews(View itemView) {
        imageView =itemView.findViewById(R.id.imageView);
        playImageView =itemView.findViewById(R.id.playImageView);
        time_tv =itemView.findViewById(R.id.time_tv);
    }

    @Override
    public void onBind(UiBroadMessage message) {
        VideoMessageContent videoMessageContent = (VideoMessageContent) message.message.content;
        Bitmap thumbnail = videoMessageContent.getThumbnail();
        time_tv.setText(TimeConvertBroadUtils.formatLongTime(videoMessageContent.getDuration()/1000));
        int width = 200;
        int height = 200;
        if(thumbnail != null) {
            int imageSize[] = WeChatImageUtils.getImageSizeByOrgSizeToWeChat(thumbnail.getWidth(), thumbnail.getHeight());
            width = imageSize[0] > 0 ? imageSize[0] : 200;
            height = imageSize[1] > 0 ? imageSize[1] : 200;
        }
        imageView.getLayoutParams().width = width;
        imageView.getLayoutParams().height = height;
        playImageView.setVisibility(View.VISIBLE);
        if(FileBroadUtils.isFileExists(videoMessageContent.localPath)){
            imagePath = videoMessageContent.localPath;
        }else {
            imagePath = videoMessageContent.remoteUrl;
        }
        loadMedia(thumbnail,imagePath,imageView);

    }

    void play() {
//        previewMM();
        if (message.message.direction == MessageDirection.Receive && message.message.status != MessageStatus.Played) {
            message.message.status = MessageStatus.Played;
            ChatManager.Instance().setMediaMessagePlayed(message.message.messageId);
        }
        VideoMessageContent videoMessageContent = (VideoMessageContent) message.message.content;
//        ELogUtilKt.eLogD("zhanglong","localPath------"+videoMessageContent.localPath);
        if(FileBroadUtils.isFileExists(videoMessageContent.localPath)){
            MyCommonBroadBridge.getInstance().setMyBridgeMethod(time_tv.getContext(), BridgeImEnum.BOFANG_SHIPIN.getValue(),videoMessageContent.localPath,"",null);
        }else {
            MyCommonBroadBridge.getInstance().setMyBridgeMethod(time_tv.getContext(), BridgeImEnum.BOFANG_SHIPIN.getValue(),videoMessageContent.remoteUrl,"",message);
        }

    }
}
