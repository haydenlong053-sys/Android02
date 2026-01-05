/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.conversation.message.viewholder;

import android.graphics.drawable.AnimationDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;

import com.cboda.chatui.BroadConfig;
import com.cboda.chatui.R;
import com.cboda.chatui.annotation.EnableContextBroadMenu;
import com.cboda.chatui.annotation.MessageContentBroadType;
import com.cboda.chatui.conversation.ConversationBroadFragment;
import com.cboda.chatui.conversation.message.model.UiBroadMessage;
import com.cboda.chatui.third.utils.UIBroadUtils;
import com.cboda.chatui.utils.DownloadBroadManager;
import cn.wildfirechat.message.PTTSoundMessageContent;
import cn.wildfirechat.message.SoundMessageContent;
import cn.wildfirechat.message.core.MessageDirection;
import cn.wildfirechat.message.core.MessageStatus;

@MessageContentBroadType(value = {SoundMessageContent.class, PTTSoundMessageContent.class})

@EnableContextBroadMenu
public class AudioMessageContentViewBroadHolder extends MediaMessageContentViewBroadHolder {
    ImageView ivAudio;
    TextView durationTextView;
    RelativeLayout contentLayout;
    @Nullable
    View playStatusIndicator;

    public AudioMessageContentViewBroadHolder(ConversationBroadFragment fragment, RecyclerView.Adapter adapter, View itemView) {
        super(fragment, adapter, itemView);
        bindViews(itemView);
        bindEvents(itemView);
    }

    private void bindEvents(View itemView) {
       itemView.findViewById(R.id.audioContentLayout).setOnClickListener(this::onClick);
    }

    private void bindViews(View itemView) {
        ivAudio =itemView.findViewById(R.id.audioImageView);
        durationTextView =itemView.findViewById(R.id.durationTextView);
        contentLayout =itemView.findViewById(R.id.audioContentLayout);
        playStatusIndicator =itemView.findViewById(R.id.playStatusIndicator);
    }

    @Override
    public void onBind(UiBroadMessage message) {
        super.onBind(message);
        SoundMessageContent voiceMessage = (SoundMessageContent) message.message.content;
        int increment = UIBroadUtils.getDisplayWidth(fragment.getContext()) / 3 / BroadConfig.DEFAULT_MAX_AUDIO_RECORD_TIME_SECOND * voiceMessage.getDuration();

        durationTextView.setText(voiceMessage.getDuration() + "''");
        ViewGroup.LayoutParams params = contentLayout.getLayoutParams();
        params.width = UIBroadUtils.dip2Px(65) + increment;
        contentLayout.setLayoutParams(params);
        if (message.message.direction == MessageDirection.Receive) {
            if (message.message.status != MessageStatus.Played) {
                playStatusIndicator.setVisibility(View.VISIBLE);
            } else {
                playStatusIndicator.setVisibility(View.GONE);
            }
        }

        AnimationDrawable animation;
        if (message.isPlaying) {
            animation = (AnimationDrawable) ivAudio.getBackground();
            if (!animation.isRunning()) {
                animation.start();
            }
        } else {
            // TODO 不知道怎么回事，动画开始了，就停不下来, 所以采用这种方式
            ivAudio.setBackground(null);
            if (message.message.direction == MessageDirection.Send) {
                ivAudio.setBackgroundResource(R.drawable.audio_animation_right_list);
            } else {
                ivAudio.setBackgroundResource(R.drawable.audio_animation_left_list);
            }
        }
    }

    @Override
    public void onViewRecycled() {
        // TODO 可实现语音是否持续播放、中断登录逻辑
    }

    public void onClick(View view) {
        File file = DownloadBroadManager.mediaMessageContentFile(message.message);
        if (file == null) {
            return;
        }
        if (file.exists()) {
            messageViewModel.playAudioMessage(message);
        } else {
            if (message.isDownloading) {
                return;
            }
            messageViewModel.downloadMedia(message, file);
        }
    }

}
