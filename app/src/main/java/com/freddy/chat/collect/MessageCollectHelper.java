package com.freddy.chat.collect;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.freddy.chat.im.IMSClientBootstrap;
import com.freddy.chat.utils.MediaMessageContentUtil;
import com.freddy.im.MessageType;
import com.freddy.im.bean.AppMessage;
import com.freddy.im.message.MessageContentFormatter;

/**
 * 长按收藏参数构造（对齐野火 {@code ConversationMessageBroadAdapter} + {@code BridgeImEnum.COLLECT}）。
 */
public final class MessageCollectHelper {

    private static final String TYPE_TEXT = "1";
    private static final String TYPE_IMAGE = "2";
    private static final String TYPE_VIDEO = "3";
    private static final String TYPE_VOICE = "4";

    private MessageCollectHelper() {
    }

    /**
     * @return {@code info|type} 或 {@code info|type|extra}，无法收藏时返回 null
     */
    @Nullable
    public static String buildCollectParams(@NonNull AppMessage message) {
        int type = message.getMsgContentType();
        String contentJson =message.getContent();
        if (TextUtils.isEmpty(contentJson)) {
            return null;
        }
        if (MessageType.MessageContentType.isText(type)) {
            return contentJson + "|" + TYPE_TEXT;
        }
        if (MessageContentFormatter.isImageMessage(type)) {
            return contentJson + "|" + TYPE_IMAGE;
        }
        if (MessageContentFormatter.isVideoMessage(type)) {
            int duration = MediaMessageContentUtil.resolveVoiceDurationSec(message.getContent());
            return contentJson + "|" + TYPE_VIDEO + "|" + duration;
        }
        //语音类消息不开启收藏
//        if (MessageContentFormatter.isVoiceMessage(type)) {
//            int duration = MediaMessageContentUtil.resolveVoiceDurationSec(message.getContent());
//            return contentJson + "|" + TYPE_VOICE + "|" + duration;
//        }
        return null;
    }
}
