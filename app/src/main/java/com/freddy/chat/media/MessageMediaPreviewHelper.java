package com.freddy.chat.media;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import com.cboda.chatui.mm.MMPreviewBroadActivity;
import com.cboda.chatui.mm.MediaBroadEntry;
import com.cboda.intelligent.activity.VideoPlayOnlyBroadActivity;
import com.freddy.chat.utils.MediaMessageContentUtil;
import com.freddy.chat.utils.QuoteMessageContentUtil;
import com.freddy.chat.utils.WeChatUiHelper;
import com.freddy.im.bean.AppMessage;
import com.freddy.im.db.mapper.MessageRealmMapper;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
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
        previewVideoUrl(context, playUrl, coverUrl);
    }

    /** 引用卡片：直接打开图片（优先用会话内原消息做相册预览）。 */
    public static void previewQuotedImage(@NonNull Context context,
                                          @NonNull QuoteMessageContentUtil.QuoteInfo quote,
                                          @Nullable List<AppMessage> sessionMessages,
                                          @Nullable AppMessage quotedMessage,
                                          @Nullable String currentUserId) {
        if (quotedMessage != null
                && sessionMessages != null
                && WeChatUiHelper.isImageMessage(quotedMessage.getMsgContentType())) {
            previewImages(context, sessionMessages, quotedMessage, currentUserId);
            return;
        }
        String mediaUrl = firstNonEmpty(quote.mediaUrl, quote.thumbUrl);
        if (TextUtils.isEmpty(mediaUrl)) {
            return;
        }
        MediaBroadEntry entry = new MediaBroadEntry();
        entry.setType(MediaBroadEntry.TYPE_IMAGE);
        if (MediaMessageContentUtil.isUsableLocalFile(mediaUrl)) {
            entry.setMediaLocalPath(mediaUrl);
            entry.setMediaUrl(mediaUrl);
        } else {
            entry.setMediaUrl(mediaUrl);
        }
        if (!TextUtils.isEmpty(quote.thumbUrl)) {
            entry.setThumbnailUrl(quote.thumbUrl);
        }
        MMPreviewBroadActivity.previewMedia(context, Collections.singletonList(entry), 0, false);
    }

    /** 引用卡片：直接打开视频。 */
    public static void previewQuotedVideo(@NonNull Context context,
                                          @NonNull QuoteMessageContentUtil.QuoteInfo quote,
                                          @Nullable AppMessage quotedMessage,
                                          @Nullable String currentUserId) {
        if (quotedMessage != null && WeChatUiHelper.isVideoMessage(quotedMessage.getMsgContentType())) {
            previewVideo(context, quotedMessage, currentUserId);
            return;
        }
        if (TextUtils.isEmpty(quote.mediaUrl)) {
            return;
        }
        previewVideoUrl(context, quote.mediaUrl, quote.thumbUrl);
    }

    /** 引用卡片：直接打开文件。 */
    public static void openQuotedFile(@NonNull Context context,
                                      @NonNull QuoteMessageContentUtil.QuoteInfo quote,
                                      @Nullable AppMessage quotedMessage,
                                      @Nullable String currentUserId) {
        String path = "";
        if (quotedMessage != null && WeChatUiHelper.isFileMessage(quotedMessage.getMsgContentType())) {
            boolean sentByMe = quotedMessage.isSender(currentUserId);
            path = MediaMessageContentUtil.resolveDisplayUrl(quotedMessage.getContent(), sentByMe);
        }
        if (TextUtils.isEmpty(path)) {
            path = quote.mediaUrl == null ? "" : quote.mediaUrl;
        }
        if (TextUtils.isEmpty(path)) {
            Toast.makeText(context, com.cboda.baselibrary.R.string.file_pick_failed, Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            Uri uri;
            if (path.startsWith("http://") || path.startsWith("https://")) {
                uri = Uri.parse(path);
            } else {
                File file = new File(path);
                if (!file.isFile()) {
                    Toast.makeText(context, com.cboda.baselibrary.R.string.file_pick_failed, Toast.LENGTH_SHORT).show();
                    return;
                }
                uri = FileProvider.getUriForFile(
                        context,
                        context.getPackageName() + ".provider",
                        file);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
            intent.setDataAndType(uri, "*/*");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(context, com.cboda.baselibrary.R.string.file_pick_failed, Toast.LENGTH_SHORT).show();
        }
    }

    public static void previewVideoUrl(@NonNull Context context,
                                       @NonNull String playUrl,
                                       @Nullable String coverUrl) {
        if (TextUtils.isEmpty(playUrl)) {
            return;
        }
        Intent intent = new Intent(context, VideoPlayOnlyBroadActivity.class);
        intent.putExtra("video", playUrl);
        intent.putExtra("videoImage", coverUrl == null ? "" : coverUrl);
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

    @NonNull
    private static String firstNonEmpty(@Nullable String a, @Nullable String b) {
        if (!TextUtils.isEmpty(a)) {
            return a.trim();
        }
        if (!TextUtils.isEmpty(b)) {
            return b.trim();
        }
        return "";
    }
}
