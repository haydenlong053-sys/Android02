package com.freddy.chat.media;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cboda.chatui.mm.MMPreviewBroadActivity;
import com.cboda.chatui.mm.MediaBroadEntry;
import com.cboda.intelligent.activity.VideoPlayOnlyBroadActivity;
import com.freddy.chat.utils.MediaMessageContentUtil;
import com.freddy.chat.utils.WeChatUiHelper;
import com.freddy.im.bean.AppMessage;
import com.freddy.im.db.mapper.MessageRealmMapper;

import java.util.ArrayList;
import java.util.List;

/**
 * 图片/视频消息预览（对齐野火 {@link MMPreviewBroadActivity}、{@link VideoPlayOnlyBroadActivity}）。
 */
public final class MessageMediaPreviewHelper {

    private MessageMediaPreviewHelper() {
    }

    public static void previewImages(@NonNull Context context,
                                     @NonNull List<AppMessage> messages,
                                     @NonNull AppMessage current,
                                     @Nullable String currentUserId) {
        List<MediaBroadEntry> entries = new ArrayList<>();
        int currentIndex = 0;
        int index = 0;
        for (AppMessage message : messages) {
            if (message == null || !WeChatUiHelper.isImageMessage(message.getMsgContentType())) {
                continue;
            }
            MediaBroadEntry entry = toImageEntry(message, currentUserId);
            if (entry == null) {
                continue;
            }
            entries.add(entry);
            if (isSameMessage(message, current)) {
                currentIndex = index;
            }
            index++;
        }
        if (entries.isEmpty()) {
            return;
        }
        MMPreviewBroadActivity.previewMedia(context, entries, currentIndex, false);
    }

    public static void previewVideo(@NonNull Context context,
                                    @NonNull AppMessage message,
                                    @Nullable String currentUserId) {
        boolean sentByMe = message.isSender(currentUserId);
        String localPath = MediaMessageContentUtil.resolveLocalUrl(message.getContent());
        String remoteUrl = MediaMessageContentUtil.resolveRemoteUrl(message.getContent());
        String playUrl = MediaMessageContentUtil.isUsableLocalFile(localPath) ? localPath : remoteUrl;
        if (TextUtils.isEmpty(playUrl)) {
            playUrl = MediaMessageContentUtil.resolveVideoPlayUrl(message.getContent(), sentByMe);
        }
        if (TextUtils.isEmpty(playUrl)) {
            return;
        }
        String coverUrl = MediaMessageContentUtil.resolveCoverDisplayUrl(message.getContent(), sentByMe);
        Intent intent = new Intent(context, VideoPlayOnlyBroadActivity.class);
        intent.putExtra("video", playUrl);
        intent.putExtra("videoImage", coverUrl);
        context.startActivity(intent);
    }

    @Nullable
    private static MediaBroadEntry toImageEntry(@NonNull AppMessage message,
                                                @Nullable String currentUserId) {
        boolean sentByMe = message.isSender(currentUserId);
        String displayUrl = MediaMessageContentUtil.resolveDisplayUrl(message.getContent(), sentByMe);
        if (TextUtils.isEmpty(displayUrl)) {
            return null;
        }
        String remoteUrl = MediaMessageContentUtil.resolveRemoteUrl(message.getContent());
        String localPath = MediaMessageContentUtil.resolveLocalUrl(message.getContent());
        String thumbUrl = MediaMessageContentUtil.resolveDisplayThumbUrl(message.getContent(), sentByMe);

        MediaBroadEntry entry = new MediaBroadEntry();
        entry.setType(MediaBroadEntry.TYPE_IMAGE);
        entry.setMediaUrl(!TextUtils.isEmpty(remoteUrl) ? remoteUrl : displayUrl);
        if (MediaMessageContentUtil.isUsableLocalFile(localPath)) {
            entry.setMediaLocalPath(localPath);
        }
        if (!TextUtils.isEmpty(thumbUrl)) {
            entry.setThumbnailUrl(thumbUrl);
        }
        return entry;
    }

    private static boolean isSameMessage(@NonNull AppMessage a, @NonNull AppMessage b) {
        String idA = MessageRealmMapper.clientMsgIdForPersist(a);
        String idB = MessageRealmMapper.clientMsgIdForPersist(b);
        if (!TextUtils.isEmpty(idA) && idA.equals(idB)) {
            return true;
        }
        String msgIdA = a.getMsgId();
        String msgIdB = b.getMsgId();
        return !TextUtils.isEmpty(msgIdA) && msgIdA.equals(msgIdB);
    }
}
