package com.freddy.chat.media;

import android.media.MediaPlayer;
import android.text.TextUtils;

import androidx.annotation.Nullable;

/**
 * 自研 IM 语音消息播放，交互对齐野火单例播放。
 */
public final class VoiceMessagePlayer {

    public interface Listener {
        void onPlayingMessageChanged(@Nullable String clientMsgId);
    }

    private static final VoiceMessagePlayer INSTANCE = new VoiceMessagePlayer();

    @Nullable
    private MediaPlayer mediaPlayer;
    @Nullable
    private String playingClientMsgId;
    @Nullable
    private Listener listener;

    private VoiceMessagePlayer() {
    }

    public static VoiceMessagePlayer getInstance() {
        return INSTANCE;
    }

    public void setListener(@Nullable Listener listener) {
        this.listener = listener;
    }

    @Nullable
    public String getPlayingClientMsgId() {
        return playingClientMsgId;
    }

    public boolean isPlaying(@Nullable String clientMsgId) {
        return !TextUtils.isEmpty(clientMsgId) && clientMsgId.equals(playingClientMsgId);
    }

    public void play(@Nullable String clientMsgId, @Nullable String localPath) {
        stopInternal(false);
        if (TextUtils.isEmpty(clientMsgId) || TextUtils.isEmpty(localPath)) {
            return;
        }
        try {
            MediaPlayer player = new MediaPlayer();
            player.setDataSource(localPath);
            player.setOnCompletionListener(mp -> stopInternal(true));
            player.setOnErrorListener((mp, what, extra) -> {
                stopInternal(true);
                return true;
            });
            player.prepare();
            player.start();
            mediaPlayer = player;
            playingClientMsgId = clientMsgId;
            notifyChanged();
        } catch (Exception ignored) {
            stopInternal(true);
        }
    }

    public void stop() {
        stopInternal(true);
    }

    private void stopInternal(boolean notify) {
        MediaPlayer player = mediaPlayer;
        mediaPlayer = null;
        playingClientMsgId = null;
        if (player != null) {
            try {
                player.stop();
            } catch (Exception ignored) {
            }
            try {
                player.release();
            } catch (Exception ignored) {
            }
        }
        if (notify) {
            notifyChanged();
        }
    }

    private void notifyChanged() {
        if (listener != null) {
            listener.onPlayingMessageChanged(playingClientMsgId);
        }
    }
}
