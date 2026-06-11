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
    private final LinearLayout voiceMetaStart;
    private final LinearLayout voiceMetaEnd;
    private final RelativeLayout audioContentLayout;
    private final ImageView audioImageView;
    private final TextView durationTextViewStart;
    private final TextView durationTextViewEnd;
    private final View playStatusIndicatorStart;
    private final View playStatusIndicatorEnd;

    public VoiceMessageVH(@NonNull View itemView) {
        super(itemView, R.layout.item_message_content_voice, false);
        voiceRoot = contentView.findViewById(R.id.voice_root);
        voiceMetaStart = contentView.findViewById(R.id.voice_meta_start);
        voiceMetaEnd = contentView.findViewById(R.id.voice_meta_end);
        audioContentLayout = contentView.findViewById(R.id.audioContentLayout);
        audioImageView = contentView.findViewById(R.id.audioImageView);
        durationTextViewStart = contentView.findViewById(R.id.durationTextViewStart);
        durationTextViewEnd = contentView.findViewById(R.id.durationTextViewEnd);
        playStatusIndicatorStart = contentView.findViewById(R.id.playStatusIndicatorStart);
        playStatusIndicatorEnd = contentView.findViewById(R.id.playStatusIndicatorEnd);
    }

    public void bind(@NonNull AppMessage message,
                     int position,
                     @NonNull MessageViewHolderContext context,
                     @NonNull VoiceMessagePlayer player) {
        bindCommon(message, position, context);
        boolean sentByMe = message.isSender(context.currentUserId);
        int durationSec = Math.max(1, MediaMessageContentUtil.resolveVoiceDurationSec(message.getContent()));
        String durationLabel = durationSec + "''";
        durationTextViewStart.setText(durationLabel);
        durationTextViewEnd.setText(durationLabel);

        int increment = resolveWidthIncrement(itemView.getResources().getDisplayMetrics(), durationSec);
        ViewGroup.LayoutParams params = audioContentLayout.getLayoutParams();
        params.width = dip2px(65) + increment;
        audioContentLayout.setLayoutParams(params);

        applyVoiceMetadataSide(sentByMe);
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
        } else {
            voiceRoot.setGravity(Gravity.START);
            audioImageView.setBackgroundResource(com.cboda.chatui.R.drawable.audio_animation_left_broad_list);
            RelativeLayout.LayoutParams iconLp = (RelativeLayout.LayoutParams) audioImageView.getLayoutParams();
            iconLp.removeRule(RelativeLayout.ALIGN_PARENT_END);
            iconLp.addRule(RelativeLayout.ALIGN_PARENT_START);
            iconLp.setMarginEnd(0);
            iconLp.setMarginStart(dip2px(15));
            audioImageView.setLayoutParams(iconLp);
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

    /** 发送方显示左侧时长，接收方显示右侧时长；未读红点暂不展示。 */
    private void applyVoiceMetadataSide(boolean sentByMe) {
        playStatusIndicatorStart.setVisibility(View.GONE);
        playStatusIndicatorEnd.setVisibility(View.GONE);
        if (sentByMe) {
            voiceMetaStart.setVisibility(View.VISIBLE);
            voiceMetaEnd.setVisibility(View.GONE);
        } else {
            voiceMetaStart.setVisibility(View.GONE);
            voiceMetaEnd.setVisibility(View.VISIBLE);
        }
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
