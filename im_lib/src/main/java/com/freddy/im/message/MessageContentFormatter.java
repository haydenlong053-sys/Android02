package com.freddy.im.message;

import static com.cboda.baselibrary.utils.ELogUtilKt.eLogD;
import static com.cboda.baselibrary.utils.GsonUtilsKt.toJson2;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.freddy.im.MessageType;
import com.freddy.im.bean.AppMessage;
import com.freddy.im.call.CallMissedLocalStore;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
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
    /** 群通话展示句：xxx发起了语音/视频通话 */
    private static final Pattern GROUP_CALL_SENTENCE =
            Pattern.compile("^(.*)发起了(语音|视频)通话$");

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
            String raw = resolveSystemContentRaw(message.getContent(), message.getExtra());
            if (isSystemContentJson(raw)) {
                return sanitizeOutgoingDisplayText(formatSystemDisplay(
                        raw, message.getCustomInfo(), currentUserId, userIdToDisplayName));
            }
            if (!isBlankOrEmptyJson(raw)) {
                return sanitizeOutgoingDisplayText(formatTextDisplay(
                        raw, message.getCustomInfo(), currentUserId, userIdToDisplayName));
            }
            // content 无效时仍尝试用 customInfo + 默认文案
            return sanitizeOutgoingDisplayText(formatSystemDisplay(
                    raw, message.getCustomInfo(), currentUserId, userIdToDisplayName));
        }
        if (isCallMessageContent(message.getContent())) {
            boolean sentByMe = !TextUtils.isEmpty(currentUserId)
                    && TextUtils.equals(currentUserId, message.getFromId());
            boolean groupCall = MessageType.isGroupChat(message.getMsgType())
                    || isGroupCallContent(message.getContent());
            String initiatorName = resolveCallInitiatorName(
                    message.getContent(), message.getFromId(), currentUserId, userIdToDisplayName);
            return sanitizeOutgoingDisplayText(formatCallDisplayText(
                    message.getContent(), sentByMe, currentUserId, groupCall, initiatorName,
                    resolveCallPeerUserId(message, currentUserId)));
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
        return sanitizeOutgoingDisplayText(formatTextDisplay(
                message.getContent(), message.getCustomInfo(), currentUserId, userIdToDisplayName));
    }

    @NonNull
    public static String formatSessionPreview(@Nullable String content,
                                              @Nullable String contentType,
                                              @Nullable String lastMessageContent,
                                              @Nullable String lastCustomInfo,
                                              @Nullable String currentUserId) {
        return formatSessionPreview(content, contentType, lastMessageContent, lastCustomInfo,
                currentUserId, null, 0, MessageRepealHelper.STATUS_NORMAL, false, null);
    }

    @NonNull
    public static String formatSessionPreview(@Nullable String content,
                                              @Nullable String contentType,
                                              @Nullable String lastMessageContent,
                                              @Nullable String lastCustomInfo,
                                              @Nullable String currentUserId,
                                              int lastMsgType) {
        return formatSessionPreview(content, contentType, lastMessageContent, lastCustomInfo,
                currentUserId, null, lastMsgType, MessageRepealHelper.STATUS_NORMAL, false, null);
    }

    @NonNull
    public static String formatSessionPreview(@Nullable String content,
                                              @Nullable String contentType,
                                              @Nullable String lastMessageContent,
                                              @Nullable String lastCustomInfo,
                                              @Nullable String currentUserId,
                                              @Nullable Map<String, String> userIdToDisplayName,
                                              int lastMsgType) {
        return formatSessionPreview(content, contentType, lastMessageContent, lastCustomInfo,
                currentUserId, userIdToDisplayName, lastMsgType, MessageRepealHelper.STATUS_NORMAL,
                false, null);
    }

    @NonNull
    public static String formatSessionPreview(@Nullable String content,
                                              @Nullable String contentType,
                                              @Nullable String lastMessageContent,
                                              @Nullable String lastCustomInfo,
                                              @Nullable String currentUserId,
                                              @Nullable Map<String, String> userIdToDisplayName,
                                              int lastMsgType,
                                              int lastMessageStatus,
                                              boolean groupChat) {
        return formatSessionPreview(content, contentType, lastMessageContent, lastCustomInfo,
                currentUserId, userIdToDisplayName, lastMsgType, lastMessageStatus, groupChat, null);
    }

    @NonNull
    public static String formatSessionPreview(@Nullable String content,
                                              @Nullable String contentType,
                                              @Nullable String lastMessageContent,
                                              @Nullable String lastCustomInfo,
                                              @Nullable String currentUserId,
                                              @Nullable Map<String, String> userIdToDisplayName,
                                              int lastMsgType,
                                              int lastMessageStatus,
                                              boolean groupChat,
                                              @Nullable String lastFromUserId) {
        // 撤回优先：不依赖 contentType，避免图片/语音等快捷分支盖过撤回文案
        if (MessageRepealHelper.isRecalled(lastMessageStatus)) {
            String raw = !TextUtils.isEmpty(lastMessageContent) ? lastMessageContent : content;
            return sanitizeOutgoingDisplayText(
                    formatTextDisplay(raw, lastCustomInfo, currentUserId, userIdToDisplayName));
        }
        if (isApiSystemMsgType(lastMsgType)) {
            String raw = !TextUtils.isEmpty(content) ? content : lastMessageContent;
            if (isBlankOrEmptyJson(raw) && !TextUtils.isEmpty(lastMessageContent)
                    && !isBlankOrEmptyJson(lastMessageContent)) {
                raw = lastMessageContent;
            }
            if (isSystemContentJson(raw) || isBlankOrEmptyJson(raw)) {
                return sanitizeOutgoingDisplayText(
                        formatSystemDisplay(raw, lastCustomInfo, currentUserId, userIdToDisplayName));
            }
            return sanitizeOutgoingDisplayText(
                    formatTextDisplay(raw, lastCustomInfo, currentUserId, userIdToDisplayName));
        }
        int type = parseContentTypeInt(contentType);
        if (isSystemContentJson(content)) {
            return sanitizeOutgoingDisplayText(
                    formatSystemDisplay(content, lastCustomInfo, currentUserId, userIdToDisplayName));
        }
        if (!TextUtils.isEmpty(lastMessageContent) && isSystemContentJson(lastMessageContent)) {
            return sanitizeOutgoingDisplayText(formatSystemDisplay(
                    lastMessageContent, lastCustomInfo, currentUserId, userIdToDisplayName));
        }
        if (isCallMessageContent(content) || isCallMessageContent(lastMessageContent)) {
            String callRaw = isCallMessageContent(content) ? content : lastMessageContent;
            boolean sentByMe = !TextUtils.isEmpty(currentUserId)
                    && TextUtils.equals(currentUserId, lastFromUserId);
            String initiatorName = resolveCallInitiatorName(
                    callRaw, lastFromUserId, currentUserId, userIdToDisplayName);
            return sanitizeOutgoingDisplayText(
                    formatCallDisplayText(callRaw, sentByMe, currentUserId, groupChat, initiatorName,
                            sentByMe ? null : lastFromUserId));
        }
        if (groupChat && isPlainCallDescPreview(content, lastMessageContent)) {
            String previewRaw = !TextUtils.isEmpty(content) ? content : lastMessageContent;
            boolean sentByMe = !TextUtils.isEmpty(currentUserId)
                    && TextUtils.equals(currentUserId, lastFromUserId);
            String initiatorName = resolveCallInitiatorName(
                    previewRaw, lastFromUserId, currentUserId, userIdToDisplayName);
            return sanitizeOutgoingDisplayText(
                    formatGroupCallDisplayText(previewRaw, initiatorName, sentByMe));
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
            return sanitizeOutgoingDisplayText(formatTextDisplay(
                    lastMessageContent, lastCustomInfo, currentUserId, userIdToDisplayName));
        }
        return sanitizeOutgoingDisplayText(
                formatTextDisplay(content, lastCustomInfo, currentUserId, userIdToDisplayName));
    }

    /**
     * 从 content / 展示文案中提取 {@code ${userId}} 占位符，用于会话预览补齐昵称映射。
     */
    @NonNull
    public static List<String> extractPlaceholderUserIds(@Nullable String raw) {
        String text = extractTextFromContent(raw);
        if (TextUtils.isEmpty(text) || !PLACEHOLDER_USER.matcher(text).find()) {
            return Collections.emptyList();
        }
        Matcher matcher = PLACEHOLDER_USER.matcher(text);
        LinkedHashSet<String> ids = new LinkedHashSet<>();
        while (matcher.find()) {
            String uid = matcher.group(1);
            if (!TextUtils.isEmpty(uid)) {
                ids.add(uid.trim());
            }
        }
        return new ArrayList<>(ids);
    }

    public static boolean isSystemContentJson(@Nullable String raw) {
        JsonObject obj = parseJsonObject(raw);
        if (obj == null || !obj.has("type")) {
            return false;
        }
        try {
            int type = obj.get("type").getAsInt();
            return type >= SYSTEM_CREATE_GROUP && type <= SYSTEM_REMOVE_MEMBER;
        } catch (Exception e) {
            return false;
        }
    }

    /** content 为空、"{}" 或无有效字段的 JSON 对象。 */
    public static boolean isBlankOrEmptyJson(@Nullable String raw) {
        if (TextUtils.isEmpty(raw)) {
            return true;
        }
        String trimmed = raw.trim();
        if ("{}".equals(trimmed) || "null".equalsIgnoreCase(trimmed) || "[]".equals(trimmed)) {
            return true;
        }
        JsonObject obj = parseJsonObject(trimmed);
        return obj != null && obj.size() == 0;
    }

    @NonNull
    private static String resolveSystemContentRaw(@Nullable String content, @Nullable String extraOrDesc) {
        if (!isBlankOrEmptyJson(content)) {
            return content;
        }
        if (!TextUtils.isEmpty(extraOrDesc)) {
            return extraOrDesc.trim();
        }
        return content != null ? content : "";
    }

    @NonNull
    private static String formatSystemDisplay(@Nullable String raw,
                                              @Nullable String customInfoJson,
                                              @Nullable String currentUserId,
                                              @Nullable Map<String, String> userIdToDisplayName) {
        Map<String, String> nameMap = mergeDisplayNameMaps(
                userIdToDisplayName, buildKeyToLabelMapFromCustomInfo(customInfoJson));

        // 纯文案摘要（来自 desc / extra）
        if (!TextUtils.isEmpty(raw) && !isBlankOrEmptyJson(raw) && parseJsonObject(raw) == null) {
            return substituteUserPlaceholders(raw.trim(), currentUserId, nameMap);
        }

        JsonObject obj = parseJsonObject(raw);
        int type = -1;
        if (obj != null && obj.has("type")) {
            try {
                type = obj.get("type").getAsInt();
            } catch (Exception ignored) {
                type = -1;
            }
        }
        String msg = "";
        if (obj != null) {
            msg = firstPrimitiveString(obj, "msg", "text", "desc", "message", "content");
        }
        if (!TextUtils.isEmpty(msg) && !isBlankOrEmptyJson(msg)) {
            return substituteUserPlaceholders(msg, currentUserId, nameMap);
        }

        String names = joinSysAtDisplayNames(customInfoJson, currentUserId, nameMap);
        String typed = defaultSystemText(type, names);
        if (!TextUtils.isEmpty(typed)) {
            return typed;
        }
        if (!TextUtils.isEmpty(names)) {
            return names;
        }
//        eLogD("formatSystemDisplay","raw:"+raw);
//        eLogD("formatSystemDisplay",currentUserId);
//        eLogD("formatSystemDisplay",customInfoJson);
//        eLogD("formatSystemDisplay","userIdToDisplayName:"+toJson2(userIdToDisplayName));
        return "[系统消息]";
    }

    @NonNull
    private static String defaultSystemText(int type, @Nullable String actorNames) {
        String who = TextUtils.isEmpty(actorNames) ? "" : actorNames;
        switch (type) {
            case SYSTEM_CREATE_GROUP:
                return TextUtils.isEmpty(who) ? "创建了群聊" : (who + "创建了群聊");
            case SYSTEM_JOIN_MEMBER:
                return TextUtils.isEmpty(who) ? "有成员加入了群聊" : (who + "加入了群聊");
            case SYSTEM_RENAME_GROUP:
                return TextUtils.isEmpty(who) ? "修改了群名称" : (who + "修改了群名称");
            case SYSTEM_REMOVE_MEMBER:
                return TextUtils.isEmpty(who) ? "有成员被移出了群聊" : (who + "被移出了群聊");
            default:
                return "";
        }
    }

    @Nullable
    private static String firstPrimitiveString(@NonNull JsonObject obj, @NonNull String... keys) {
        for (String key : keys) {
            if (!obj.has(key) || !obj.get(key).isJsonPrimitive()) {
                continue;
            }
            try {
                String v = obj.get(key).getAsString();
                if (!TextUtils.isEmpty(v)) {
                    return v.trim();
                }
            } catch (Exception ignored) {
                // continue
            }
        }
        return null;
    }

    @NonNull
    private static String joinSysAtDisplayNames(@Nullable String customInfoJson,
                                                @Nullable String currentUserId,
                                                @Nullable Map<String, String> userIdToDisplayName) {
        Map<String, String> fromCustom = buildKeyToLabelMapFromCustomInfo(customInfoJson);
        if (fromCustom == null || fromCustom.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> e : fromCustom.entrySet()) {
            String name = resolveDisplayName(e.getKey(), currentUserId, userIdToDisplayName);
            if (TextUtils.isEmpty(name)) {
                name = preferShortDisplayName(e.getValue());
            }
            if (TextUtils.isEmpty(name)) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append("、");
            }
            sb.append(name);
        }
        return sb.toString();
    }

    @NonNull
    private static String formatTextDisplay(@Nullable String raw,
                                            @Nullable String customInfoJson,
                                            @Nullable String currentUserId,
                                            @Nullable Map<String, String> userIdToDisplayName) {
        String text = extractTextFromContent(raw);
        if (TextUtils.isEmpty(text) || isBlankOrEmptyJson(text)) {
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
        if (TextUtils.isEmpty(raw) || isBlankOrEmptyJson(raw)) {
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
        String msg = firstPrimitiveString(obj, "msg", "text", "desc", "message");
        if (!TextUtils.isEmpty(msg) && !isBlankOrEmptyJson(msg)) {
            return msg;
        }
        if (obj.has("messages")) {
            return formatMergeTranspondDisplay(trimmed);
        }
        if (obj.has("url")) {
            return formatFileDisplay(trimmed);
        }
        // 勿把 "{}" / 未知 JSON 原样当文案
        return "";
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

    /** 会话列表里可能只有服务端 desc（如 [语音通话]xxx），非 JSON。 */
    private static boolean isPlainCallDescPreview(@Nullable String content,
                                                  @Nullable String lastMessageContent) {
        return isPlainCallDesc(content) || isPlainCallDesc(lastMessageContent);
    }

    private static boolean isPlainCallDesc(@Nullable String raw) {
        if (TextUtils.isEmpty(raw)) {
            return false;
        }
        String text = raw.trim();
        return text.startsWith("[语音通话]") || text.startsWith("[视频通话]")
                || text.equals("语音通话") || text.equals("视频通话");
    }

    /**
     * 1v1 通话记录展示文案（区分自己 / 对方）；群通话仅展示「谁发起了语音/视频通话」。
     * <p>
     * 忙线优先顺序：显式 status（remote_busy / local_busy）→ content 角色字段 →
     * desc「忙线未接听」→ {@code sentByMe}（共享一条时 fromId 通常为主叫）。
     * <ul>
     *   <li>ended + duration&gt;0 → 通话时长 mm:ss</li>
     *   <li>ended + duration=0 → 通话结束</li>
     *   <li>cancelled → 已取消 / 对方已取消</li>
     *   <li>rejected → 拨打方是自己：对方已拒绝；拨打方是别人：已拒绝</li>
     *   <li>busy → 对方正忙 / 忙线未接听</li>
     *   <li>missed / timeout / 本机振铃超时 → 自己拨打「对方未接听」；对方拨打「未接听」</li>
     * </ul>
     *
     * @param sentByMe 是否为当前用户发出的通话记录（通常主叫为 true）
     */
    @NonNull
    public static String formatCallDisplayText(@Nullable String raw, boolean sentByMe) {
        return formatCallDisplayText(raw, sentByMe, null, isGroupCallContent(raw), null, null);
    }

    @NonNull
    public static String formatCallDisplayText(@Nullable String raw,
                                              boolean sentByMe,
                                              @Nullable String currentUserId) {
        return formatCallDisplayText(raw, sentByMe, currentUserId, isGroupCallContent(raw), null, null);
    }

    @NonNull
    public static String formatCallDisplayText(@Nullable String raw,
                                              boolean sentByMe,
                                              @Nullable String currentUserId,
                                              boolean groupCall) {
        return formatCallDisplayText(raw, sentByMe, currentUserId, groupCall, null, null);
    }

    @NonNull
    public static String formatCallDisplayText(@Nullable String raw,
                                              boolean sentByMe,
                                              @Nullable String currentUserId,
                                              boolean groupCall,
                                              @Nullable String initiatorName) {
        return formatCallDisplayText(raw, sentByMe, currentUserId, groupCall, initiatorName, null);
    }

    @NonNull
    public static String formatCallDisplayText(@Nullable String raw,
                                              boolean sentByMe,
                                              @Nullable String currentUserId,
                                              boolean groupCall,
                                              @Nullable String initiatorName,
                                              @Nullable String peerUserId) {
        if (groupCall || isGroupCallContent(raw)) {
            return formatGroupCallDisplayText(raw, initiatorName, sentByMe);
        }
        JsonObject obj = parseJsonObject(raw);
        if (obj == null) {
            return "[通话]";
        }
        String status = readCallStatus(obj);
        int durationSec = readCallDurationSec(obj);
        String cleanedDesc = stripCallDescPrefix(readCallDesc(obj));
        String personalized = matchPersonalizedCallDesc(cleanedDesc);
        String callId = firstCallString(obj, "callId", "call_id", "roomName", "roomId", "matchId");
        boolean iAmCaller = resolveIAmCaller(obj, sentByMe, currentUserId);

        // 拒绝优先：避免本机超时标记把「对方已拒绝」误判成「对方未接听」
        if (isCallStatus(status, "rejected", "reject", "refused", "refuse")
                || (!TextUtils.isEmpty(cleanedDesc) && cleanedDesc.contains("拒绝"))) {
            return resolveRejectDisplay(obj, cleanedDesc, sentByMe, currentUserId);
        }

        // 超时未接：自己拨打→对方未接听；对方拨打→未接听
        if (isTimeoutOrMissedHint(obj, status, cleanedDesc, personalized, callId, peerUserId)) {
            return resolveMissedDisplay(callId, peerUserId, iAmCaller, sentByMe);
        }
        if (isCallStatus(status, "cancelled", "canceled", "cancel")
                || isPersonalizedCancel(personalized)) {
            // 仅当本条能对上超时 callId/peer，或近期超时且文案为取消类时，才按未接听
            if (isCancelCausedByTimeout(callId, peerUserId, status, personalized, cleanedDesc)) {
                return resolveMissedDisplay(callId, peerUserId, iAmCaller, sentByMe);
            }
            // 被叫侧收到主叫主动取消
            if (CallMissedLocalStore.wasIncoming(callId, peerUserId)
                    || isIncomingDirection(obj, currentUserId, sentByMe)) {
                return "对方已取消";
            }
            if (isPersonalizedCancel(personalized)) {
                return personalized;
            }
            return iAmCaller ? "已取消" : "对方已取消";
        }
        if (isCallStatus(status, "remote_busy")) {
            return "对方正忙";
        }
        if (isCallStatus(status, "local_busy", "busy_missed", "line_busy")) {
            return "忙线未接听";
        }
        if (isCallStatus(status, "busy")) {
            return resolveBusyDisplay(obj, cleanedDesc, sentByMe, currentUserId);
        }
        if (isCallStatus(status, "missed", "timeout", "noanswer", "no_answer", "no-answer")) {
            return resolveMissedDisplay(callId, peerUserId, iAmCaller, sentByMe);
        }
        if (isCallStatus(status, "ended", "connected", "hangup", "finished", "complete", "completed")) {
            if (durationSec > 0) {
                return "通话时长 " + formatCallDuration(durationSec);
            }
            return "通话结束";
        }

        if (!TextUtils.isEmpty(cleanedDesc)) {
            if (isPersonalizedMissed(personalized)) {
                return resolveMissedDisplay(callId, peerUserId, iAmCaller, sentByMe);
            }
            if (personalized != null && !isPersonalizedCancel(personalized)) {
                return personalized;
            }
            return mapCallDescFallback(cleanedDesc, iAmCaller, durationSec);
        }
        if (durationSec > 0) {
            return "通话时长 " + formatCallDuration(durationSec);
        }
        return "[通话]";
    }

    /**
     * 超时未接文案：
     * <ul>
     *   <li>自己是拨打方 → 对方未接听</li>
     *   <li>对方是拨打方（自己未接）→ 未接听</li>
     * </ul>
     */
    @NonNull
    private static String resolveMissedDisplay(@Nullable String callId,
                                               @Nullable String peerUserId,
                                               boolean iAmCaller,
                                               boolean sentByMe) {
        // 明确超时角色优先；不要用「近期任意超时」覆盖本条角色判断
        if (CallMissedLocalStore.isTimeoutCaller(callId, peerUserId)) {
            return "对方未接听";
        }
        if (CallMissedLocalStore.isTimeoutCallee(callId, peerUserId)
                || CallMissedLocalStore.wasIncoming(callId, peerUserId)) {
            return "未接听";
        }
        if (CallMissedLocalStore.hasRecentTimeoutCaller()
                && !CallMissedLocalStore.hasRecentTimeoutCallee()) {
            return "对方未接听";
        }
        if (CallMissedLocalStore.hasRecentTimeoutCallee()
                && !CallMissedLocalStore.hasRecentTimeoutCaller()) {
            return "未接听";
        }
        if (!sentByMe) {
            return "未接听";
        }
        return iAmCaller ? "对方未接听" : "未接听";
    }

    private static boolean isIncomingDirection(@NonNull JsonObject obj,
                                               @Nullable String currentUserId,
                                               boolean sentByMe) {
        Boolean caller = resolveIAmCallerOrNull(obj, currentUserId);
        if (caller != null) {
            return !caller;
        }
        return !sentByMe;
    }

    @Nullable
    private static String resolveCallPeerUserId(@NonNull AppMessage message,
                                                 @Nullable String currentUserId) {
        if (!TextUtils.isEmpty(currentUserId) && TextUtils.equals(currentUserId, message.getFromId())) {
            return message.getToId();
        }
        return message.getFromId();
    }

    /**
     * 群通话记录文案：不区分取消/忙线/未接等，仅展示「谁发起了语音/视频通话」。
     */
    @NonNull
    public static String formatGroupCallDisplayText(@Nullable String raw) {
        return formatGroupCallDisplayText(raw, null, false);
    }

    @NonNull
    public static String formatGroupCallDisplayText(@Nullable String raw,
                                                    @Nullable String initiatorName,
                                                    boolean sentByMe) {
        String media = isCallVideoMedia(raw) ? "视频通话" : "语音通话";
        if (sentByMe) {
            return "你发起了" + media;
        }
        String name = preferShortDisplayName(initiatorName);
        if (TextUtils.isEmpty(name)) {
            JsonObject obj = parseJsonObject(raw);
            if (obj != null) {
                name = preferShortDisplayName(firstCallString(obj,
                        "fromUsername", "fromUserName", "callerName", "initiatorName"));
            }
        }
        if (!TextUtils.isEmpty(name)) {
            return name + "发起了" + media;
        }
        return "发起了" + media;
    }

    /**
     * 将「昵称（备注）」缩成备注，避免文案过长（如「好计较（用过解决）」→「用过解决」）；
     * 同时去掉包裹名的引号（如 {@code "用过解决"} → {@code 用过解决}）。
     */
    @NonNull
    public static String preferShortDisplayName(@Nullable String name) {
        if (TextUtils.isEmpty(name)) {
            return "";
        }
        String trimmed = stripWrappingQuotes(name.trim());
        String extracted = extractParentheticalAlias(trimmed);
        return TextUtils.isEmpty(extracted) ? trimmed : extracted;
    }

    /** 去掉首尾成对引号：ASCII {@code "}、中文 {@code “”}/{@code 「」}。 */
    @NonNull
    private static String stripWrappingQuotes(@NonNull String name) {
        String s = name;
        while (s.length() >= 2) {
            char first = s.charAt(0);
            char last = s.charAt(s.length() - 1);
            boolean pair = (first == '"' && last == '"')
                    || (first == '\u201C' && last == '\u201D')
                    || (first == '\u300C' && last == '\u300D');
            if (!pair) {
                break;
            }
            s = s.substring(1, s.length() - 1).trim();
        }
        return s;
    }

    /**
     * 兜底修正群通话展示句：
     * 「好计较（用过解决）发起了语音通话」→「用过解决发起了语音通话」；
     * 也去掉人名两侧引号：{@code "XXX"发起了语音通话} → {@code XXX发起了语音通话}。
     * 非该句式原文返回，避免误伤普通消息。
     */
    @NonNull
    public static String sanitizeOutgoingDisplayText(@Nullable String text) {
        if (TextUtils.isEmpty(text)) {
            return "";
        }
        String trimmed = text.trim();
        Matcher matcher = GROUP_CALL_SENTENCE.matcher(trimmed);
        if (!matcher.matches()) {
            return trimmed;
        }
        String name = preferShortDisplayName(matcher.group(1));
        String media = matcher.group(2);
        if (TextUtils.isEmpty(name)) {
            return "发起了" + media + "通话";
        }
        return name + "发起了" + media + "通话";
    }

    @Nullable
    private static String extractParentheticalAlias(@NonNull String name) {
        String inner = extractInnerAlias(name, '（', '）');
        if (!TextUtils.isEmpty(inner)) {
            return inner;
        }
        return extractInnerAlias(name, '(', ')');
    }

    @Nullable
    private static String extractInnerAlias(@NonNull String name, char left, char right) {
        int start = name.indexOf(left);
        if (start <= 0) {
            return null;
        }
        int end = name.indexOf(right, start + 1);
        if (end <= start + 1) {
            return null;
        }
        // 仅当括号后无其它内容（纯「昵称（备注）」）时取备注
        if (end != name.length() - 1) {
            return null;
        }
        String inner = name.substring(start + 1, end).trim();
        return TextUtils.isEmpty(inner) ? null : inner;
    }

    /** 从通话 content JSON 中读取主叫 userId（会话预览无本地消息时兜底）。 */
    @Nullable
    public static String extractCallCallerUserId(@Nullable String raw) {
        JsonObject obj = parseJsonObject(raw);
        if (obj == null) {
            return null;
        }
        return firstCallString(obj,
                "callerId", "caller", "initiator", "inviterId", "fromId", "fromUserId");
    }

    @Nullable
    private static String resolveCallInitiatorName(@Nullable String raw,
                                                   @Nullable String fromId,
                                                   @Nullable String currentUserId,
                                                   @Nullable Map<String, String> userIdToDisplayName) {
        if (!TextUtils.isEmpty(fromId) && userIdToDisplayName != null) {
            String mapped = userIdToDisplayName.get(fromId);
            if (!TextUtils.isEmpty(mapped)) {
                return preferShortDisplayName(mapped);
            }
        }
        JsonObject obj = parseJsonObject(raw);
        if (obj != null) {
            String callerId = firstCallString(obj,
                    "callerId", "caller", "initiator", "inviterId", "fromId", "fromUserId");
            if (!TextUtils.isEmpty(callerId)
                    && userIdToDisplayName != null
                    && !TextUtils.equals(callerId, currentUserId)) {
                String mapped = userIdToDisplayName.get(callerId);
                if (!TextUtils.isEmpty(mapped)) {
                    return preferShortDisplayName(mapped);
                }
            }
            String name = firstCallString(obj,
                    "fromUsername", "fromUserName", "callerName", "initiatorName");
            if (!TextUtils.isEmpty(name)) {
                return preferShortDisplayName(name);
            }
        }
        return null;
    }

    /** content 内可识别为群/多人通话（如 groupId、多人 targetIds）。 */
    public static boolean isGroupCallContent(@Nullable String raw) {
        JsonObject obj = parseJsonObject(raw);
        if (obj == null) {
            return false;
        }
        try {
            String groupId = firstCallString(obj, "groupId", "group_id");
            if (!TextUtils.isEmpty(groupId)) {
                return true;
            }
            String scope = firstCallString(obj, "scope", "callType", "scene");
            if (isCallStatus(scope, "group", "multi", "multiparty", "conference")) {
                return true;
            }
            if (jsonArraySize(obj, "targetIds") > 1
                    || jsonArraySize(obj, "toUserIds") > 1
                    || jsonArraySize(obj, "participants") > 1) {
                return true;
            }
        } catch (Exception ignored) {
            return false;
        }
        return false;
    }

    private static int jsonArraySize(@NonNull JsonObject obj, @NonNull String key) {
        if (!obj.has(key) || !obj.get(key).isJsonArray()) {
            return 0;
        }
        try {
            return obj.getAsJsonArray(key).size();
        } catch (Exception e) {
            return 0;
        }
    }

    @NonNull
    private static String readCallStatus(@NonNull JsonObject obj) {
        if (!obj.has("status")) {
            return "";
        }
        try {
            JsonElement el = obj.get("status");
            if (el != null && el.isJsonPrimitive() && el.getAsJsonPrimitive().isNumber()) {
                return normalizeNumericCallStatus(el.getAsInt());
            }
            return normalizeCallStatusAlias(el != null ? el.getAsString().trim() : "");
        } catch (Exception e) {
            return "";
        }
    }

    /** 兼容 status 为字符串数字（\"5\"）或中文别名。 */
    @NonNull
    private static String normalizeCallStatusAlias(@Nullable String raw) {
        if (TextUtils.isEmpty(raw)) {
            return "";
        }
        String s = raw.trim();
        if (s.matches("^-?\\d+$")) {
            try {
                return normalizeNumericCallStatus(Integer.parseInt(s));
            } catch (NumberFormatException ignored) {
                return s;
            }
        }
        if (s.contains("忙线未接听")) {
            return "local_busy";
        }
        if (s.contains("对方正忙") || s.contains("对方忙线") || s.contains("对方忙")) {
            return "remote_busy";
        }
        if (s.contains("正忙") || s.contains("忙线") || "busy".equalsIgnoreCase(s)) {
            return "busy";
        }
        if (s.contains("已拒绝") || s.contains("拒绝") || "reject".equalsIgnoreCase(s)
                || "rejected".equalsIgnoreCase(s)) {
            return "rejected";
        }
        if (s.contains("对方未接听") || s.contains("未接来电") || s.contains("未接听")
                || s.contains("无应答") || s.contains("未应答") || s.contains("超时")) {
            return "missed";
        }
        if (s.contains("已取消") || s.contains("取消")) {
            return "cancelled";
        }
        return s;
    }

    /**
     * 超时未接听判定。明确拒绝/忙线不算未接。
     */
    private static boolean isTimeoutOrMissedHint(@NonNull JsonObject obj,
                                                 @Nullable String status,
                                                 @Nullable String cleanedDesc,
                                                 @Nullable String personalized,
                                                 @Nullable String callId,
                                                 @Nullable String peerUserId) {
        // 拒绝 / 忙线绝不能走未接听
        if (isCallStatus(status, "rejected", "reject", "refused", "refuse")
                || isCallStatus(status, "busy", "remote_busy", "local_busy", "busy_missed", "line_busy")) {
            return false;
        }
        if (!TextUtils.isEmpty(cleanedDesc)
                && (cleanedDesc.contains("拒绝") || cleanedDesc.contains("正忙") || cleanedDesc.contains("忙线"))) {
            return false;
        }
        if (isCallStatus(status, "missed", "timeout", "noanswer", "no_answer", "no-answer")) {
            return true;
        }
        if (isPersonalizedMissed(personalized)) {
            return true;
        }
        // 本条 callId/peer 明确标记为超时，且文案是取消类（服务端常把超时落成 cancelled）
        boolean markedTimeout = CallMissedLocalStore.isTimeoutMissed(callId)
                || CallMissedLocalStore.isTimeoutCallee(callId, peerUserId)
                || CallMissedLocalStore.isTimeoutCaller(callId, peerUserId);
        if (markedTimeout && isCancelLike(status, personalized, cleanedDesc)) {
            return true;
        }
        // 无 callId 时：仅当近期超时 + 本条是取消类，才兜底为未接
        if ((CallMissedLocalStore.hasRecentTimeoutCaller() || CallMissedLocalStore.hasRecentTimeoutCallee())
                && isCancelLike(status, personalized, cleanedDesc)) {
            return true;
        }
        String reason = firstCallString(obj,
                "reason", "endReason", "cancelReason", "cause", "result", "hangupReason");
        if (!TextUtils.isEmpty(reason)) {
            String r = reason.trim();
            if (r.contains("拒绝") || r.contains("busy") || r.contains("忙")) {
                return false;
            }
            if (isCallStatus(r, "timeout", "missed", "noanswer", "no_answer", "no-answer")
                    || r.toLowerCase().contains("timeout")
                    || r.contains("未接")
                    || r.contains("无应答")
                    || r.contains("超时")) {
                return true;
            }
        }
        if (TextUtils.isEmpty(cleanedDesc)) {
            return false;
        }
        String d = cleanedDesc.trim();
        return d.contains("未接来电")
                || d.contains("对方未接听")
                || d.contains("未接听")
                || d.contains("无应答")
                || d.contains("未应答")
                || d.contains("对方未接");
    }

    /** 取消类文案/状态（超时未接时常被服务端落成 cancel）。 */
    private static boolean isCancelLike(@Nullable String status,
                                        @Nullable String personalized,
                                        @Nullable String cleanedDesc) {
        if (isCallStatus(status, "cancelled", "canceled", "cancel")) {
            return true;
        }
        if (isPersonalizedCancel(personalized)) {
            return true;
        }
        if (TextUtils.isEmpty(status) && TextUtils.isEmpty(cleanedDesc)) {
            return true;
        }
        return !TextUtils.isEmpty(cleanedDesc) && cleanedDesc.contains("取消");
    }

    private static boolean isCancelCausedByTimeout(@Nullable String callId,
                                                   @Nullable String peerUserId,
                                                   @Nullable String status,
                                                   @Nullable String personalized,
                                                   @Nullable String cleanedDesc) {
        if (!isCancelLike(status, personalized, cleanedDesc)) {
            return false;
        }
        if (CallMissedLocalStore.isTimeoutMissed(callId)
                || CallMissedLocalStore.isTimeoutCallee(callId, peerUserId)
                || CallMissedLocalStore.isTimeoutCaller(callId, peerUserId)) {
            return true;
        }
        return CallMissedLocalStore.hasRecentTimeoutCaller()
                || CallMissedLocalStore.hasRecentTimeoutCallee();
    }

    /**
     * 拒绝文案：
     * <ul>
     *   <li>拨打方是自己 → 对方已拒绝</li>
     *   <li>拨打方是别人（自己拒接）→ 已拒绝</li>
     * </ul>
     */
    @NonNull
    private static String resolveRejectDisplay(@NonNull JsonObject obj,
                                               @Nullable String cleanedDesc,
                                               boolean sentByMe,
                                               @Nullable String currentUserId) {
        if (!TextUtils.isEmpty(currentUserId)) {
            String rejectorId = firstCallString(obj, "rejectorId", "rejectUserId", "rejectedBy");
            if (!TextUtils.isEmpty(rejectorId)) {
                return TextUtils.equals(rejectorId, currentUserId) ? "已拒绝" : "对方已拒绝";
            }
        }
        Boolean iAmCaller = resolveIAmCallerOrNull(obj, currentUserId);
        if (iAmCaller != null) {
            return iAmCaller ? "对方已拒绝" : "已拒绝";
        }
        // 服务端按视角写了「已拒绝」（无「对方」）时信任，适配双方各自一条且 sentByMe 都为 true
        if (!TextUtils.isEmpty(cleanedDesc)
                && cleanedDesc.contains("已拒绝")
                && !cleanedDesc.contains("对方")) {
            return "已拒绝";
        }
        // 默认约定：消息发送方=拨打方
        return sentByMe ? "对方已拒绝" : "已拒绝";
    }

    /**
     * 忙线文案：
     * <ul>
     *   <li>content 带 callerId / direction / busyUserId → 按角色</li>
     *   <li>desc 已是「忙线未接听」→ 信任（双方各自发记录时 sentByMe 恒为 true）</li>
     *   <li>否则按 sentByMe（共享一条、fromId=主叫）</li>
     * </ul>
     */
    @NonNull
    private static String resolveBusyDisplay(@NonNull JsonObject obj,
                                             @Nullable String cleanedDesc,
                                             boolean sentByMe,
                                             @Nullable String currentUserId) {
        Boolean iAmCaller = resolveIAmCallerOrNull(obj, currentUserId);
        if (iAmCaller != null) {
            return iAmCaller ? "对方正忙" : "忙线未接听";
        }
        if (!TextUtils.isEmpty(cleanedDesc) && cleanedDesc.contains("忙线未接听")) {
            return "忙线未接听";
        }
        return sentByMe ? "对方正忙" : "忙线未接听";
    }

    /**
     * 仅当 content 内有明确角色字段时返回；否则 null，交由 sentByMe / desc 处理。
     */
    @Nullable
    private static Boolean resolveIAmCallerOrNull(@NonNull JsonObject obj,
                                                  @Nullable String currentUserId) {
        String direction = firstCallString(obj, "direction", "callDirection", "dir");
        if (!TextUtils.isEmpty(direction)) {
            if (isCallStatus(direction, "out", "outgoing", "caller", "mo", "send")) {
                return true;
            }
            if (isCallStatus(direction, "in", "incoming", "callee", "mt", "receive", "recv")) {
                return false;
            }
        }
        String role = firstCallString(obj, "role", "callRole", "selfRole");
        if (!TextUtils.isEmpty(role)) {
            if (isCallStatus(role, "caller", "out", "outgoing")) {
                return true;
            }
            if (isCallStatus(role, "callee", "called", "in", "incoming")) {
                return false;
            }
        }
        if (!TextUtils.isEmpty(currentUserId)) {
            String busyUserId = firstCallString(obj, "busyUserId", "busyId", "busyUser");
            if (!TextUtils.isEmpty(busyUserId)) {
                return !TextUtils.equals(busyUserId, currentUserId);
            }
            String callerId = firstCallString(obj,
                    "callerId", "caller", "initiator", "inviterId");
            if (!TextUtils.isEmpty(callerId)) {
                return TextUtils.equals(callerId, currentUserId);
            }
        }
        return null;
    }

    /**
     * 当前用户是否为主叫。用于忙线等需要区分视角、但双方消息 fromId 都是自己的场景。
     */
    private static boolean resolveIAmCaller(@NonNull JsonObject obj,
                                            boolean sentByMe,
                                            @Nullable String currentUserId) {
        Boolean resolved = resolveIAmCallerOrNull(obj, currentUserId);
        return resolved != null ? resolved : sentByMe;
    }

    @Nullable
    private static String firstCallString(@NonNull JsonObject obj, @NonNull String... keys) {
        for (String key : keys) {
            if (!obj.has(key) || !obj.get(key).isJsonPrimitive()) {
                continue;
            }
            try {
                String v = obj.get(key).getAsString();
                if (!TextUtils.isEmpty(v)) {
                    return v.trim();
                }
            } catch (Exception ignored) {
                // continue
            }
        }
        return null;
    }

    /** 从 desc 识别服务端已按视角写好的文案。 */
    @Nullable
    private static String matchPersonalizedCallDesc(@Nullable String cleanedDesc) {
        if (TextUtils.isEmpty(cleanedDesc)) {
            return null;
        }
        String d = cleanedDesc.trim();
        if (d.contains("忙线未接听")) {
            return "忙线未接听";
        }
        if (d.contains("对方正忙") || d.contains("对方忙线")) {
            return "对方正忙";
        }
        if (d.contains("对方已拒绝") || d.contains("已拒绝")) {
            return d.contains("对方") ? "对方已拒绝" : "已拒绝";
        }
        if (d.contains("对方未接听") || d.contains("对方未接")) {
            return "对方未接听";
        }
        if (d.contains("未接来电")) {
            return "未接听";
        }
        if (d.contains("对方已取消")) {
            return "对方已取消";
        }
        if ("已取消".equals(d) || d.startsWith("已取消")) {
            return "已取消";
        }
        if ("未接听".equals(d) || d.startsWith("未接听")) {
            return "未接听";
        }
        return null;
    }

    private static boolean isPersonalizedCancel(@Nullable String text) {
        return "已取消".equals(text) || "对方已取消".equals(text);
    }

    private static boolean isPersonalizedMissed(@Nullable String text) {
        return "未接听".equals(text)
                || "未接来电".equals(text)
                || "对方未接听".equals(text);
    }

    @NonNull
    private static String normalizeNumericCallStatus(int code) {
        // 对齐 CallSignalType：3 reject / 4 cancel / 5 busy / 6 hangup / 9 end
        switch (code) {
            case 3:
                return "rejected";
            case 4:
                return "cancelled";
            case 5:
                return "busy";
            case 6:
            case 9:
                return "ended";
            default:
                return String.valueOf(code);
        }
    }

    private static int readCallDurationSec(@NonNull JsonObject obj) {
        if (!obj.has("duration")) {
            return 0;
        }
        try {
            return Math.max(0, obj.get("duration").getAsInt());
        } catch (Exception e) {
            return 0;
        }
    }

    @NonNull
    private static String readCallDesc(@NonNull JsonObject obj) {
        if (!obj.has("desc") || !obj.get("desc").isJsonPrimitive()) {
            return "";
        }
        try {
            return obj.get("desc").getAsString();
        } catch (Exception e) {
            return "";
        }
    }

    private static boolean isCallStatus(@Nullable String status, @NonNull String... candidates) {
        if (TextUtils.isEmpty(status)) {
            return false;
        }
        for (String c : candidates) {
            if (c.equalsIgnoreCase(status)) {
                return true;
            }
        }
        return false;
    }

    @NonNull
    private static String mapCallDescFallback(@NonNull String desc, boolean sentByMe, int durationSec) {
        String d = desc.trim();
        if (d.contains("通话时长")) {
            if (durationSec > 0 && !d.matches(".*\\d.*")) {
                return "通话时长 " + formatCallDuration(durationSec);
            }
            return d.startsWith("通话时长") ? d : ("通话时长 " + formatCallDuration(durationSec));
        }
        if (d.contains("通话结束")) {
            return "通话结束";
        }
        // 未接优先于「取消」，避免服务端误把超时写成带「取消」的 desc
        if (d.contains("未接来电")) {
            return sentByMe ? "对方未接听" : "未接听";
        }
        if (d.contains("对方未接听") || d.contains("对方未接")) {
            return "对方未接听";
        }
        if (d.contains("未接听") || d.contains("无应答") || d.contains("未应答")) {
            // 「忙线未接听」已在上方处理；此处勿把拒绝误归未接（拒绝已在入口优先处理）
            return sentByMe ? "对方未接听" : "未接听";
        }
        if (d.contains("已取消") || d.contains("取消")) {
            if (CallMissedLocalStore.hasRecentTimeoutCaller()) {
                return "对方未接听";
            }
            if (CallMissedLocalStore.hasRecentTimeoutCallee()) {
                return "未接听";
            }
            return sentByMe ? "已取消" : "对方已取消";
        }
        if (d.contains("已拒绝") || d.contains("拒绝")) {
            return sentByMe ? "对方已拒绝" : "已拒绝";
        }
        if (d.contains("正忙") || d.contains("忙线") || d.contains("忙")) {
            // 不因 desc 含「对方」就两端都显示「对方正忙」；统一按主叫/被叫视角
            return sentByMe ? "对方正忙" : "忙线未接听";
        }
        return sanitizeOutgoingDisplayText(d);
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
            return String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds);
        }
        return String.format(Locale.US, "%02d:%02d", minutes, seconds);
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
                return preferShortDisplayName(name);
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
        Map<String, String> map = new LinkedHashMap<>();
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
