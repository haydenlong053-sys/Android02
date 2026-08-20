package com.freddy.chat.media;

import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cboda.baselibrary.utils.ELogUtilKt;

/**
 * 自研 IM 语音消息播放，交互对齐野火单例播放。
 */
public final class VoiceMessagePlayer {

    public interface Listener {
        void onPlayingMessageChanged(@Nullable String clientMsgId);
    }

    private static final VoiceMessagePlayer INSTANCE = new VoiceMessagePlayer();
    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());
    private static final String TAG = "VoiceMessagePlayer";
    private static final long PREPARE_TIMEOUT_MS = 10_000L;

    @Nullable
    private MediaPlayer mediaPlayer;
    @Nullable
    private String playingClientMsgId;
    @Nullable
    private Listener listener;
    @Nullable
    private Runnable pendingPrepareTimeout;

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
                ELogUtilKt.eLogE(TAG, "play error what=" + what + " extra=" + extra + " path=" + localPath);
                stopInternal(true);
                return true;
            });
            mediaPlayer = player;
            playingClientMsgId = clientMsgId;
            notifyChanged();
            player.setOnPreparedListener(mp -> {
                clearPrepareTimeout();
                try {
                    mp.start();
                } catch (Exception ignored) {
                    ELogUtilKt.eLogE(TAG, "start failed path=" + localPath);
                    stopInternal(true);
                }
            });
            // 避免在主线程执行阻塞式 prepare() 引发点击 ANR
            schedulePrepareTimeout(player, clientMsgId, localPath);
            player.prepareAsync();
        } catch (Exception e) {
            ELogUtilKt.eLogE(TAG, "play init failed path=" + localPath, e);
            stopInternal(true);
        }
    }

    public void stop() {
        stopInternal(true);
    }

    private void stopInternal(boolean notify) {
        clearPrepareTimeout();
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

    private void schedulePrepareTimeout(@NonNull MediaPlayer expectedPlayer,
                                        @NonNull String expectedClientMsgId,
                                        @NonNull String path) {
        clearPrepareTimeout();
        Runnable timeoutTask = () -> {
            if (mediaPlayer != expectedPlayer) {
                return;
            }
            if (!expectedClientMsgId.equals(playingClientMsgId)) {
                return;
            }
            Log.e(TAG, "prepareAsync timeout path=" + path + " clientMsgId=" + expectedClientMsgId);
            stopInternal(true);
        };
        pendingPrepareTimeout = timeoutTask;
        MAIN_HANDLER.postDelayed(timeoutTask, PREPARE_TIMEOUT_MS);
    }

    private void clearPrepareTimeout() {
        Runnable timeoutTask = pendingPrepareTimeout;
        if (timeoutTask != null) {
            MAIN_HANDLER.removeCallbacks(timeoutTask);
            pendingPrepareTimeout = null;
        }
    }

    private void notifyChanged() {
        Listener l = listener;
        if (l == null) {
            return;
        }
        MAIN_HANDLER.post(() -> l.onPlayingMessageChanged(playingClientMsgId));
    }
}
