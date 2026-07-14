package com.freddy.im.message;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.freddy.im.MessageType;
import com.freddy.im.bean.AppMessage;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 消息内容展示解析（对齐 IM 文档 §10 高级用法：系统消息、文本/文件/合并转发等 JSON content）。
 */
public final class MessageContentFormatter {

    /** 接口 {@link com.freddy.im.http.model.im.ImMessageItem#getMsgType()}：系统消息 */
    public static final int API_MSG_TYPE_SYSTEM = 2;

    /** 系统消息 content.type（JSON 内嵌） */
    private static final int SYSTEM_CREATE_GROUP = 0;
    private static final int SYSTEM_JOIN_MEMBER = 1;
    private static final int SYSTEM_RENAME_GROUP = 2;
    private static final int SYSTEM_REMOVE_MEMBER = 3;

    private static final Pattern PLACEHOLDER_USER = Pattern.compile("\\$\\{([^}]+)\\}");

    private MessageContentFormatter() {
    }

    public static boolean isApiSystemMsgType(int msgType) {
        return msgType == API_MSG_TYPE_SYSTEM;
    }

    public static boolean isSystemMessage(@Nullable AppMessage message) {
        if (message == null) {
            return false;
        }
        if (isApiSystemMsgType(message.getMsgType())) {
            return true;
        }
        return isSystemContentJson(message.getContent());
    }

    public static boolean isImageMessage(int msgContentType) {
        return MessageType.MessageContentType.isImage(msgContentType);
    }

    public static boolean isVoiceMessage(int msgContentType) {
        return MessageType.MessageContentType.isVoice(msgContentType);
    }

    public static boolean isVideoMessage(int msgContentType) {
        return MessageType.MessageContentType.isVideo(msgContentType);
    }

    public static boolean isFileMessage(int msgContentType) {
        return MessageType.MessageContentType.isFile(msgContentType);
    }

    public static boolean isMapMessage(int msgContentType) {
        return MessageType.MessageContentType.isMap(msgContentType);
    }

    public static boolean isMergeTranspondMessage(int msgContentType) {
        return MessageType.MessageContentType.isMergeTranspond(msgContentType);
    }

    public static boolean isCardMessage(int msgContentType) {
        return MessageType.MessageContentType.isCard(msgContentType);
    }

    public static boolean isNoticeMessage(int msgContentType) {
        return MessageType.MessageContentType.isNotice(msgContentType);
    }

    public static boolean isMeetStartMessage(int msgContentType) {
        return MessageType.MessageContentType.isMeetStart(msgContentType);
    }

    public static boolean isMeetEndMessage(int msgContentType) {
        return MessageType.MessageContentType.isMeetEnd(msgContentType);
    }

    public static boolean isMeetStatusMessage(int msgContentType) {
        return MessageType.MessageContentType.isMeetStatus(msgContentType);
    }

    @NonNull
    public static String formatDisplayText(@NonNull AppMessage message,
                                           @Nullable String currentUserId,
                                           @Nullable Map<String, String> userIdToDisplayName) {
        if (isSystemMessage(message)) {
            if (isSystemContentJson(message.getContent())) {
                return formatSystemDisplay(message.getContent(), message.getCustomInfo(),
                        currentUserId, userIdToDisplayName);
            }
            return formatTextDisplay(message.getContent(), message.getCustomInfo(),
                    currentUserId, userIdToDisplayName);
        }
        if (isCallMessageContent(message.getContent())) {
            return formatCallDisplayText(message.getContent(), false);
        }
        int type = message.getMsgContentType();
        if (isVoiceMessage(type)) {
            String duration = parseVoiceDurationLabel(message.getContent());
            return duration.isEmpty() ? "[语音]" : "[语音] " + duration;
        }
        if (isImageMessage(type)) {
            return "[图片]";
        }
        if (isVideoMessage(type)) {
            return "[视频]";
        }
        if (isFileMessage(type)) {
            return formatFileDisplay(message.getContent());
        }
        if (isMapMessage(type)) {
            return "[位置]";
        }
        if (isCardMessage(type)) {
            return "[个人名片]";
        }
        if (isNoticeMessage(type)) {
            return "[公告]";
        }
        if (isMeetStartMessage(type)) {
            return "会议中";
        }
        if (isMeetEndMessage(type)) {
            return "会议已结束";
        }
        if (isMergeTranspondMessage(type)) {
            return formatMergeTranspondDisplay(message.getContent());
        }
        return formatTextDisplay(message.getContent(), message.getCustomInfo(), currentUserId,
                userIdToDisplayName);
    }

    @NonNull
    public static String formatSessionPreview(@Nullable String content,
                                              @Nullable String contentType,
                                              @Nullable String lastMessageContent,
                                              @Nullable String lastCustomInfo,
                                              @Nullable String currentUserId) {
        return formatSessionPreview(content, contentType, lastMessageContent, lastCustomInfo,
                currentUserId, 0);
    }

    @NonNull
    public static String formatSessionPreview(@Nullable String content,
                                              @Nullable String contentType,
                                              @Nullable String lastMessageContent,
                                              @Nullable String lastCustomInfo,
                                              @Nullable String currentUserId,
                                              int lastMsgType) {
        if (isApiSystemMsgType(lastMsgType)) {
            String raw = !TextUtils.isEmpty(content) ? content : lastMessageContent;
            if (isSystemContentJson(raw)) {
                return formatSystemDisplay(raw, lastCustomInfo, currentUserId, null);
            }
            return formatTextDisplay(raw, lastCustomInfo, currentUserId, null);
        }
        int type = parseContentTypeInt(contentType);
        if (isSystemContentJson(content)) {
            return formatSystemDisplay(content, lastCustomInfo, currentUserId, null);
        }
        if (!TextUtils.isEmpty(lastMessageContent) && isSystemContentJson(lastMessageContent)) {
            return formatSystemDisplay(lastMessageContent, lastCustomInfo, currentUserId, null);
        }
        if (isCallMessageContent(content) || isCallMessageContent(lastMessageContent)) {
            String callRaw = isCallMessageContent(content) ? content : lastMessageContent;
            return formatCallDisplayText(callRaw, false);
        }
        if (isVoiceMessage(type)) {
            return "[语音]";
        }
        if (isImageMessage(type)) {
            return "[图片]";
        }
        if (isVideoMessage(type)) {
            return "[视频]";
        }
        if (isFileMessage(type)) {
            return formatFileDisplay(content);
        }
        if (isMapMessage(type)) {
            return "[位置]";
        }
        if (isCardMessage(type)) {
            return "[个人名片]";
        }
        if (isNoticeMessage(type)) {
            return "[公告]";
        }
        if (isMeetStartMessage(type)) {
            return "会议中";
        }
        if (isMeetEndMessage(type)) {
            return "会议已结束";
        }
        if (isMergeTranspondMessage(type)) {
            return formatMergeTranspondDisplay(content);
        }
        if (!TextUtils.isEmpty(lastMessageContent)) {
            return formatTextDisplay(lastMessageContent, lastCustomInfo, currentUserId, null);
        }
        return formatTextDisplay(content, lastCustomInfo, currentUserId, null);
    }

    public static boolean isSystemContentJson(@Nullable String raw) {
        JsonObject obj = parseJsonObject(raw);
        if (obj == null || !obj.has("type") || !obj.has("msg")) {
            return false;
        }
        try {
            int type = obj.get("type").getAsInt();
            return type >= SYSTEM_CREATE_GROUP && type <= SYSTEM_REMOVE_MEMBER;
        } catch (Exception e) {
            return false;
        }
    }

    @NonNull
    private static String formatSystemDisplay(@Nullable String raw,
                                              @Nullable String customInfoJson,
                                              @Nullable String currentUserId,
                                              @Nullable Map<String, String> userIdToDisplayName) {
        JsonObject obj = parseJsonObject(raw);
        if (obj == null) {
            return raw != null ? raw : "";
        }
        int type = obj.has("type") ? obj.get("type").getAsInt() : -1;
        String msg = obj.has("msg") ? obj.get("msg").getAsString() : "";
        Map<String, String> nameMap = mergeDisplayNameMaps(
                userIdToDisplayName, buildKeyToLabelMapFromCustomInfo(customInfoJson));
        if (!TextUtils.isEmpty(msg)) {
            return substituteUserPlaceholders(msg, currentUserId, nameMap);
        }
        return defaultSystemText(type, currentUserId);
    }

    @NonNull
    private static String defaultSystemText(int type, @Nullable String currentUserId) {
        switch (type) {
            case SYSTEM_CREATE_GROUP:
                return "创建了群聊";
            case SYSTEM_JOIN_MEMBER:
                return "有成员加入了群聊";
            case SYSTEM_RENAME_GROUP:
                return "修改了群名称";
            case SYSTEM_REMOVE_MEMBER:
                return "有成员被移出了群聊";
            default:
                return "";
        }
    }

    @NonNull
    private static String formatTextDisplay(@Nullable String raw,
                                            @Nullable String customInfoJson,
                                            @Nullable String currentUserId,
                                            @Nullable Map<String, String> userIdToDisplayName) {
        String text = extractTextFromContent(raw);
        if (TextUtils.isEmpty(text)) {
            return "";
        }
        if (!PLACEHOLDER_USER.matcher(text).find()) {
            return text;
        }
        Map<String, String> nameMap = mergeDisplayNameMaps(
                userIdToDisplayName, buildKeyToLabelMapFromCustomInfo(customInfoJson));
        return substituteUserPlaceholders(text, currentUserId, nameMap);
    }

    /**
     * 从文本消息 {@code content} 提取可复制纯文本（JSON 取 {@code msg}，否则返回原文）。
     */
    @NonNull
    public static String extractPlainText(@Nullable String content) {
        return extractTextFromContent(content);
    }

    @NonNull
    private static String extractTextFromContent(@Nullable String raw) {
        if (TextUtils.isEmpty(raw)) {
            return "";
        }
        String trimmed = raw.trim();
        JsonObject obj = parseJsonObject(trimmed);
        if (obj == null) {
            return trimmed;
        }
        if (isCallMessageContent(trimmed)) {
            return formatCallDisplayText(trimmed, false);
        }
        if (obj.has("msg") && obj.get("msg").isJsonPrimitive()) {
            return obj.get("msg").getAsString();
        }
        if (obj.has("messages")) {
            return formatMergeTranspondDisplay(trimmed);
        }
        if (obj.has("url")) {
            return formatFileDisplay(trimmed);
        }
        return trimmed;
    }

    /**
     * 通话结束/取消等会话通知 JSON：
     * {@code {"media":"voice","status":"ended","duration":3,"desc":"[语音通话]通话时长 0:03"}}
     */
    public static boolean isCallMessageContent(@Nullable String raw) {
        JsonObject obj = parseJsonObject(raw);
        if (obj == null || !obj.has("media")) {
            return false;
        }
        try {
            String media = obj.get("media").getAsString();
            if (!"voice".equalsIgnoreCase(media)
                    && !"audio".equalsIgnoreCase(media)
                    && !"video".equalsIgnoreCase(media)) {
                return false;
            }
            return obj.has("status") || obj.has("desc");
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isCallVideoMedia(@Nullable String raw) {
        JsonObject obj = parseJsonObject(raw);
        if (obj == null || !obj.has("media")) {
            return false;
        }
        try {
            return "video".equalsIgnoreCase(obj.get("media").getAsString());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * @param sentByMe 是否为当前用户发出的通话记录（影响「已取消」/「对方已取消」文案）
     */
    @NonNull
    public static String formatCallDisplayText(@Nullable String raw, boolean sentByMe) {
        JsonObject obj = parseJsonObject(raw);
        if (obj == null) {
            return "[通话]";
        }
        String status = "";
        if (obj.has("status") && obj.get("status").isJsonPrimitive()) {
            status = obj.get("status").getAsString();
        }
        String desc = "";
        if (obj.has("desc") && obj.get("desc").isJsonPrimitive()) {
            desc = obj.get("desc").getAsString();
        }
        String cleanedDesc = stripCallDescPrefix(desc);

        if ("cancelled".equalsIgnoreCase(status) || "canceled".equalsIgnoreCase(status)) {
            return sentByMe ? "对方已取消" : "已取消";
        }
        if ("missed".equalsIgnoreCase(status) || "timeout".equalsIgnoreCase(status)) {
            if (!TextUtils.isEmpty(cleanedDesc)) {
                return cleanedDesc;
            }
            return sentByMe ? "对方无应答" : "无人应答";
        }
        if ("ended".equalsIgnoreCase(status) || "connected".equalsIgnoreCase(status)) {
            if (!TextUtils.isEmpty(cleanedDesc) && cleanedDesc.contains("通话时长")) {
                return cleanedDesc;
            }
            int durationSec = 0;
            if (obj.has("duration")) {
                try {
                    durationSec = Math.max(0, obj.get("duration").getAsInt());
                } catch (Exception ignored) {
                }
            }
            return "通话时长 " + formatCallDuration(durationSec);
        }
        if (!TextUtils.isEmpty(cleanedDesc)) {
            return cleanedDesc;
        }
        return "[通话]";
    }

    @NonNull
    private static String stripCallDescPrefix(@Nullable String desc) {
        if (TextUtils.isEmpty(desc)) {
            return "";
        }
        String text = desc.trim();
        if (text.startsWith("[语音通话]")) {
            return text.substring("[语音通话]".length()).trim();
        }
        if (text.startsWith("[视频通话]")) {
            return text.substring("[视频通话]".length()).trim();
        }
        return text;
    }

    @NonNull
    private static String formatCallDuration(int durationSec) {
        int s = Math.max(0, durationSec);
        int hours = s / 3600;
        int minutes = (s % 3600) / 60;
        int seconds = s % 60;
        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        }
        return String.format("%02d:%02d", minutes, seconds);
    }

    @NonNull
    private static String formatFileDisplay(@Nullable String raw) {
        JsonObject obj = parseJsonObject(raw);
        if (obj == null) {
            return "[文件]";
        }
        String name = "";
        if (obj.has("msg") && obj.get("msg").isJsonPrimitive()) {
            name = obj.get("msg").getAsString();
        }
        if (TextUtils.isEmpty(name) && obj.has("fileName") && obj.get("fileName").isJsonPrimitive()) {
            name = obj.get("fileName").getAsString();
        }
        if (!TextUtils.isEmpty(name)) {
            return "[文件] " + name;
        }
        return "[文件]";
    }

    @NonNull
    private static String formatMergeTranspondDisplay(@Nullable String raw) {
        JsonObject obj = parseJsonObject(raw);
        if (obj != null) {
            JsonArray arr = null;
            if (obj.has("messageList") && obj.get("messageList").isJsonArray()) {
                arr = obj.getAsJsonArray("messageList");
            } else if (obj.has("messages") && obj.get("messages").isJsonArray()) {
                arr = obj.getAsJsonArray("messages");
            }
            if (arr != null) {
                int count = arr.size();
                if (count > 0) {
                    return "[聊天记录] " + count + "条";
                }
            }
        }
        return "[聊天记录]";
    }

    @NonNull
    private static String substituteUserPlaceholders(@NonNull String template,
                                                     @Nullable String currentUserId,
                                                     @Nullable Map<String, String> userIdToDisplayName) {
        Matcher matcher = PLACEHOLDER_USER.matcher(template);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String uid = matcher.group(1);
            String replacement = resolveDisplayName(uid, currentUserId, userIdToDisplayName);
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    @NonNull
    private static String resolveDisplayName(@Nullable String userId,
                                             @Nullable String currentUserId,
                                             @Nullable Map<String, String> userIdToDisplayName) {
        if (TextUtils.isEmpty(userId)) {
            return "";
        }
        if (userIdToDisplayName != null) {
            String name = userIdToDisplayName.get(userId);
            if (!TextUtils.isEmpty(name)) {
                return name;
            }
        }
        if (!TextUtils.isEmpty(currentUserId) && currentUserId.equals(userId)) {
            return "你";
        }
        return userId;
    }

    /**
     * 从 customInfo 解析 userId → 展示名：{@code at}、{@code sysAt} 的 key/value。
     */
    @Nullable
    private static Map<String, String> buildKeyToLabelMapFromCustomInfo(@Nullable String customInfoJson) {
        JsonObject custom = parseJsonObject(customInfoJson);
        if (custom == null || !custom.has("info") || !custom.get("info").isJsonArray()) {
            return null;
        }
        Map<String, String> map = new HashMap<>();
        for (JsonElement el : custom.getAsJsonArray("info")) {
            if (!el.isJsonObject()) {
                continue;
            }
            JsonObject item = el.getAsJsonObject();
            String type = item.has("type") ? item.get("type").getAsString() : "";
            if (!"at".equalsIgnoreCase(type) && !"sysAt".equalsIgnoreCase(type)) {
                continue;
            }
            if (!item.has("key") || !item.has("value")) {
                continue;
            }
            String key = item.get("key").getAsString();
            String value = item.get("value").getAsString();
            if (!TextUtils.isEmpty(key) && !TextUtils.isEmpty(value)) {
                map.put(key, value);
            }
        }
        return map.isEmpty() ? null : map;
    }

    @Nullable
    private static Map<String, String> mergeDisplayNameMaps(
            @Nullable Map<String, String> primary,
            @Nullable Map<String, String> fromCustomInfo) {
        if (primary == null && fromCustomInfo == null) {
            return null;
        }
        Map<String, String> merged = new HashMap<>();
        if (primary != null) {
            merged.putAll(primary);
        }
        if (fromCustomInfo != null) {
            merged.putAll(fromCustomInfo);
        }
        return merged.isEmpty() ? null : merged;
    }

    @NonNull
    public static String parseVoiceDurationLabel(@Nullable String content) {
        if (TextUtils.isEmpty(content)) {
            return "";
        }
        String trimmed = content.trim();
        JsonObject obj = parseJsonObject(trimmed);
        if (obj != null) {
            if (obj.has("duration")) {
                try {
                    int sec = obj.get("duration").getAsInt();
                    if (sec > 0 && sec < 120) {
                        return sec + "''";
                    }
                } catch (Exception ignored) {
                }
            }
            if (obj.has("msg") && obj.get("msg").isJsonPrimitive()) {
                trimmed = obj.get("msg").getAsString();
            }
        }
        try {
            int sec = Integer.parseInt(trimmed);
            if (sec > 0 && sec < 120) {
                return sec + "''";
            }
        } catch (NumberFormatException ignored) {
        }
        if (trimmed.contains("duration") || trimmed.contains("second")) {
            String digits = trimmed.replaceAll("[^0-9]", "");
            if (!digits.isEmpty()) {
                try {
                    int sec = Integer.parseInt(digits);
                    if (sec > 0 && sec < 120) {
                        return sec + "''";
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return "";
    }

    private static int parseContentTypeInt(@Nullable String contentType) {
        if (TextUtils.isEmpty(contentType)) {
            return 0;
        }
        try {
            return Integer.parseInt(contentType.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @Nullable
    private static JsonObject parseJsonObject(@Nullable String raw) {
        if (TextUtils.isEmpty(raw)) {
            return null;
        }
        String trimmed = raw.trim();
        if (!trimmed.startsWith("{")) {
            return null;
        }
        try {
            return JsonParser.parseString(trimmed).getAsJsonObject();
        } catch (Exception e) {
            return null;
        }
    }
}
