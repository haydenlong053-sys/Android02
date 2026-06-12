package com.freddy.chat.collect;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.freddy.chat.im.IMSClientBootstrap;
import com.freddy.chat.utils.AppGson;
import com.freddy.chat.utils.MediaMessageContentUtil;
import com.freddy.im.IMSConfig;
import com.freddy.im.MessageType;
import com.freddy.im.bean.AppMessage;
import com.freddy.im.signal.ImSignalProtos;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 将收藏项转为可发送的 {@link AppMessage}（content 为自研 JSON）。
 */
public final class CollectMessageFactory {

    private CollectMessageFactory() {
    }

    @Nullable
    public static AppMessage buildOutboundMessage(@NonNull CollectSessionTarget target,
                                                  @Nullable String collectType,
                                                  @Nullable String info) {
        AppMessage message = buildMessageContent(collectType, info);
        if (message == null) {
            return null;
        }
        if (!TextUtils.isEmpty(target.sessionId)) {
            message.setConversationId(target.sessionId);
        }
        message.setToId(target.toId);
        message.setConversationType(target.conversationType);
        message.setConversationTypeEnum(ImSignalProtos.ConversationType.forNumber(target.conversationType));
        return message;
    }

    /** 仅构造内容与类型，供转发到其它会话时由 {@link com.freddy.chat.forward.MessageForwardHelper} 写入目标。 */
    @Nullable
    public static AppMessage buildForwardableMessage(@Nullable String collectType,
                                                     @Nullable String info) {
        return buildMessageContent(collectType, info);
    }

    @Nullable
    private static AppMessage buildMessageContent(@Nullable String collectType,
                                                  @Nullable String info) {
        if (TextUtils.isEmpty(collectType) || TextUtils.isEmpty(info)) {
            return null;
        }
        int msgContentType = mapCollectType(collectType);
        if (msgContentType <= 0) {
            return null;
        }
        if (TextUtils.isEmpty(info)) {
            return null;
        }
        String userId = IMSClientBootstrap.getInstance().getCurrentUserId();
        AppMessage message = new AppMessage();
        message.setClientMsgId(UUID.randomUUID().toString());
        message.setMsgContentType(msgContentType);
        message.setFromId(userId);
        message.setTimestamp(System.currentTimeMillis());
        message.setContent(info);
        message.setSendStatus(IMSConfig.SEND_STATUS_SENDING);
        return message;
    }

    private static int mapCollectType(@NonNull String collectType) {
        switch (collectType) {
            case CollectContentParser.TYPE_TEXT:
                return MessageType.MessageContentType.TEXT.getMsgContentType();
            case CollectContentParser.TYPE_IMAGE:
                return MessageType.MessageContentType.IMAGE.getMsgContentType();
            case CollectContentParser.TYPE_VIDEO:
                return MessageType.MessageContentType.VIDEO.getMsgContentType();
            case CollectContentParser.TYPE_VOICE:
                return MessageType.MessageContentType.VOICE.getMsgContentType();
            default:
                return 0;
        }
    }

  @NonNull
    private static String normalizeContent(@NonNull String collectType,
                                           @NonNull String info,
                                           @Nullable String miao) {
        if (info.trim().startsWith("{")) {
            return info;
        }
        switch (collectType) {
            case CollectContentParser.TYPE_TEXT:
                return wrapText(info);
            case CollectContentParser.TYPE_IMAGE:
                return wrapImageUrl(info);
            case CollectContentParser.TYPE_VIDEO:
                return wrapVideoUrl(info, miao);
            case CollectContentParser.TYPE_VOICE:
                return wrapVoiceUrl(info, miao);
            default:
                return "";
        }
    }

    @NonNull
    private static String wrapText(@NonNull String text) {
        Map<String, String> body = Collections.singletonMap("msg", text);
        return AppGson.get().toJson(body);
    }

    @NonNull
    private static String wrapImageUrl(@NonNull String url) {
        Map<String, Object> body = new LinkedHashMap<>(2);
        body.put(MediaMessageContentUtil.KEY_URL, url);
        return AppGson.get().toJson(body);
    }

    @NonNull
    private static String wrapVideoUrl(@NonNull String url, @Nullable String miao) {
        Map<String, Object> body = new LinkedHashMap<>(4);
        body.put(MediaMessageContentUtil.KEY_URL, url);
        int duration = 0;
        if (!TextUtils.isEmpty(miao)) {
            try {
                duration = Math.max(0, Integer.parseInt(miao.trim()));
            } catch (NumberFormatException ignored) {
            }
        }
        body.put("duration", duration);
        body.put("dw", 180);
        body.put("dh", 120);
        return AppGson.get().toJson(body);
    }

    @NonNull
    private static String wrapVoiceUrl(@NonNull String url, @Nullable String miao) {
        Map<String, Object> body = new LinkedHashMap<>(2);
        body.put(MediaMessageContentUtil.KEY_URL, url);
        int duration = 0;
        if (!TextUtils.isEmpty(miao)) {
            try {
                duration = Math.max(0, Integer.parseInt(miao.trim()));
            } catch (NumberFormatException ignored) {
            }
        }
        body.put("duration", duration);
        return AppGson.get().toJson(body);
    }
}
