package com.freddy.chat.viewholder;

import android.graphics.drawable.AnimationDrawable;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.cboda.chatui.BroadConfig;
import com.cboda.intelligent.R;
import com.freddy.chat.media.VoiceMessagePlayer;
import com.freddy.chat.utils.MediaMessageContentUtil;
import com.freddy.im.bean.AppMessage;

public final class VoiceMessageVH extends BaseMessageVH {
    private final LinearLayout voiceRoot;
    private final RelativeLayout audioContentLayout;
    private final ImageView audioImageView;
    private final TextView durationTextView;
    private final View playStatusIndicator;

    public VoiceMessageVH(@NonNull View itemView) {
        super(itemView, R.layout.item_message_content_voice, false);
        voiceRoot = contentView.findViewById(R.id.voice_root);
        audioContentLayout = contentView.findViewById(R.id.audioContentLayout);
        audioImageView = contentView.findViewById(R.id.audioImageView);
        durationTextView = contentView.findViewById(R.id.durationTextView);
        playStatusIndicator = contentView.findViewById(R.id.playStatusIndicator);
    }

    public void bind(@NonNull AppMessage message,
                     int position,
                     @NonNull MessageViewHolderContext context,
                     @NonNull VoiceMessagePlayer player) {
        bindCommon(message, position, context);
        boolean sentByMe = message.isSender(context.currentUserId);
        int durationSec = Math.max(1, MediaMessageContentUtil.resolveVoiceDurationSec(message.getContent()));
        durationTextView.setText(durationSec + "''");

        int increment = resolveWidthIncrement(itemView.getResources().getDisplayMetrics(), durationSec);
        ViewGroup.LayoutParams params = audioContentLayout.getLayoutParams();
        params.width = dip2px(65) + increment;
        audioContentLayout.setLayoutParams(params);

        applyBroadBubbleBackground(audioContentLayout, sentByMe);
        if (sentByMe) {
            voiceRoot.setGravity(Gravity.END);
            audioImageView.setBackgroundResource(com.cboda.chatui.R.drawable.audio_animation_right_broad_list);
            RelativeLayout.LayoutParams iconLp = (RelativeLayout.LayoutParams) audioImageView.getLayoutParams();
            iconLp.removeRule(RelativeLayout.ALIGN_PARENT_START);
            iconLp.addRule(RelativeLayout.ALIGN_PARENT_END);
            iconLp.setMarginStart(0);
            iconLp.setMarginEnd(dip2px(15));
            audioImageView.setLayoutParams(iconLp);
            playStatusIndicator.setVisibility(View.GONE);
        } else {
            voiceRoot.setGravity(Gravity.START);
            audioImageView.setBackgroundResource(com.cboda.chatui.R.drawable.audio_animation_left_broad_list);
            RelativeLayout.LayoutParams iconLp = (RelativeLayout.LayoutParams) audioImageView.getLayoutParams();
            iconLp.removeRule(RelativeLayout.ALIGN_PARENT_END);
            iconLp.addRule(RelativeLayout.ALIGN_PARENT_START);
            iconLp.setMarginEnd(0);
            iconLp.setMarginStart(dip2px(15));
            audioImageView.setLayoutParams(iconLp);
            playStatusIndicator.setVisibility(View.VISIBLE);
        }

        boolean playing = player.isPlaying(message.getClientMsgId());
        updateAnimation(playing, sentByMe);
        audioContentLayout.setOnClickListener(v -> {
            String path = MediaMessageContentUtil.resolveDisplayUrl(message.getContent(), sentByMe);
            if (path.isEmpty()) {
                return;
            }
            if (player.isPlaying(message.getClientMsgId())) {
                player.stop();
            } else {
                player.play(message.getClientMsgId(), path);
            }
        });
        refreshContextMenuLongClickOnContent();
    }

    private void updateAnimation(boolean playing, boolean sentByMe) {
        if (playing) {
            AnimationDrawable animation = (AnimationDrawable) audioImageView.getBackground();
            if (animation != null && !animation.isRunning()) {
                animation.start();
            }
            return;
        }
        audioImageView.setBackground(null);
        audioImageView.setBackgroundResource(sentByMe
                ? com.cboda.chatui.R.drawable.audio_animation_right_broad_list
                : com.cboda.chatui.R.drawable.audio_animation_left_broad_list);
    }

    private static int resolveWidthIncrement(@NonNull DisplayMetrics metrics, int durationSec) {
        int screenThird = metrics.widthPixels / 3;
        int maxDuration = BroadConfig.DEFAULT_MAX_AUDIO_RECORD_TIME_SECOND;
        if (maxDuration <= 0) {
            return 0;
        }
        return screenThird / maxDuration * durationSec;
    }

    private int dip2px(int dip) {
        float density = itemView.getResources().getDisplayMetrics().density;
        return (int) (dip * density + 0.5f);
    }
}
