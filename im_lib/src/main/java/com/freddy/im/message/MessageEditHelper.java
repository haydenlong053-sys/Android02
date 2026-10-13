package com.freddy.im.message;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.freddy.im.bean.AppMessage;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * 消息编辑（文档 msgEdit）：status=5；customInfo 根上 {@code edited}/{@code editedAt}。
 * 仅可改自己发出的纯文本（contentType=100），发出后 15 分钟内。
 */
public final class MessageEditHelper {

    /** 已编辑（服务端 status） */
    public static final int STATUS_EDITED = 5;

    /** 可编辑时限：15 分钟（秒） */
    public static final int EDIT_TIME_LIMIT_SEC = 15 * 60;

    private MessageEditHelper() {
    }

    public static boolean isEdited(@Nullable AppMessage message) {
        return message != null && isEdited(message.getStatus(), message.getCustomInfo());
    }

    public static boolean isEdited(int status, @Nullable String customInfo) {
        if (status == STATUS_EDITED) {
            return true;
        }
        return readEditedFlag(customInfo);
    }

    public static boolean isEdited(int status) {
        return status == STATUS_EDITED;
    }

    /** 将纯文本包成服务端 content：{@code {"msg":"..."}}。 */
    @NonNull
    public static String wrapPlainTextContent(@NonNull String plainText) {
        JsonObject obj = new JsonObject();
        obj.addProperty("msg", plainText);
        return obj.toString();
    }

    /**
     * 在原有 customInfo 根上写入 {@code edited:true}、{@code editedAt}。
     */
    @NonNull
    public static String mergeEditedCustomInfo(@Nullable String customInfo, long editedAtMs) {
        JsonObject root = parseObjectOrEmpty(customInfo);
        root.addProperty("edited", true);
        root.addProperty("editedAt", editedAtMs > 0L ? editedAtMs : System.currentTimeMillis());
        return root.toString();
    }

    @NonNull
    public static AppMessage toEditedMessage(@NonNull AppMessage original,
                                             @NonNull String contentJson,
                                             @Nullable String customInfoJson,
                                             long editedAtMs) {
        AppMessage edited = copyMessage(original);
        edited.setContent(contentJson);
        String custom = !TextUtils.isEmpty(customInfoJson)
                ? customInfoJson
                : mergeEditedCustomInfo(original.getCustomInfo(), editedAtMs);
        edited.setCustomInfo(custom);
        edited.setStatus(STATUS_EDITED);
        return edited;
    }

    private static boolean readEditedFlag(@Nullable String customInfo) {
        if (TextUtils.isEmpty(customInfo)) {
            return false;
        }
        try {
            JsonObject obj = parseObjectOrEmpty(customInfo);
            if (!obj.has("edited") || obj.get("edited").isJsonNull()) {
                return false;
            }
            if (obj.get("edited").isJsonPrimitive() && obj.get("edited").getAsJsonPrimitive().isBoolean()) {
                return obj.get("edited").getAsBoolean();
            }
            String raw = obj.get("edited").getAsString();
            return "true".equalsIgnoreCase(raw) || "1".equals(raw);
        } catch (Exception ignored) {
            return false;
        }
    }

    @NonNull
    private static JsonObject parseObjectOrEmpty(@Nullable String json) {
        if (TextUtils.isEmpty(json)) {
            return new JsonObject();
        }
        try {
            return JsonParser.parseString(json.trim()).getAsJsonObject();
        } catch (Exception ignored) {
            return new JsonObject();
        }
    }

    @NonNull
    private static AppMessage copyMessage(@NonNull AppMessage source) {
        AppMessage target = new AppMessage();
        target.setConversationType(source.getConversationType());
        target.setConversationId(source.getConversationId());
        target.setMsgId(source.getMsgId());
        target.setClientMsgId(source.getClientMsgId());
        target.setMsgType(source.getMsgType());
        target.setSendType(source.getSendType());
        target.setMsgContentType(source.getMsgContentType());
        target.setFromId(source.getFromId());
        target.setToId(source.getToId());
        target.setTimestamp(source.getTimestamp());
        target.setSequenceId(source.getSequenceId());
        target.setStatusReport(source.getStatusReport());
        target.setExtend(source.getExtend());
        target.setCustomInfo(source.getCustomInfo());
        target.setExtra(source.getExtra());
        target.setContent(source.getContent());
        target.setSendStatus(source.getSendStatus());
        target.setReadPersonCount(source.getReadPersonCount());
        target.setStatus(source.getStatus());
        return target;
    }
}
