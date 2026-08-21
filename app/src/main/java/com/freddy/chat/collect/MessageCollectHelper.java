package com.freddy.chat.collect;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.freddy.chat.utils.MediaMessageContentUtil;
import com.freddy.im.MessageType;
import com.freddy.im.bean.AppMessage;
import com.freddy.im.message.MessageContentFormatter;

/**
 * 长按收藏参数构造（对齐野火 {@code ConversationMessageBroadAdapter} + {@code BridgeImEnum.COLLECT}）。
 */
public final class MessageCollectHelper {

    private MessageCollectHelper() {
    }

    /**
     * @return {@code info|type} 或 {@code info|type|extra}，无法收藏时返回 null
     */
    @Nullable
    public static String buildCollectParams(@NonNull AppMessage message) {
        int type = message.getMsgContentType();
        String contentJson = message.getContent();
        if (TextUtils.isEmpty(contentJson)) {
            return null;
        }
        if (MessageType.MessageContentType.isText(type)) {
            return contentJson + "|" + CollectContentParser.TYPE_TEXT;
        }
        if (MessageContentFormatter.isImageMessage(type)) {
            return contentJson + "|" + CollectContentParser.TYPE_IMAGE;
        }
        if (MessageContentFormatter.isVideoMessage(type)) {
            int duration = MediaMessageContentUtil.resolveVoiceDurationSec(message.getContent());
            return contentJson + "|" + CollectContentParser.TYPE_VIDEO + "|" + duration;
        }
        if (MessageContentFormatter.isFileMessage(type)) {
            return contentJson + "|" + CollectContentParser.TYPE_FILE;
        }
        // 语音类消息暂不开启收藏
        return null;
    }
}
