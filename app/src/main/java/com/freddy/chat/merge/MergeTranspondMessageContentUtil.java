package com.freddy.chat.merge;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cboda.baselibrary.R;
import com.cboda.baselibrary.utils.ImUiStrings;
import com.freddy.im.MessageType;
import com.freddy.im.message.MessageContentFormatter;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 合并转发消息 content JSON 解析（type=102，{@code messageList}）。
 */
public final class MergeTranspondMessageContentUtil {

    private static final String KEY_MESSAGE_LIST = "messageList";
    private static final String KEY_MESSAGES = "messages";

    private MergeTranspondMessageContentUtil() {
    }

    @NonNull
    public static List<MergeTranspondMessageItem> parseMessageList(@Nullable String contentJson) {
        JsonObject root = parseObject(contentJson);
        if (root == null) {
            return Collections.emptyList();
        }
        JsonArray arr = null;
        if (root.has(KEY_MESSAGE_LIST) && root.get(KEY_MESSAGE_LIST).isJsonArray()) {
            arr = root.getAsJsonArray(KEY_MESSAGE_LIST);
        } else if (root.has(KEY_MESSAGES) && root.get(KEY_MESSAGES).isJsonArray()) {
            arr = root.getAsJsonArray(KEY_MESSAGES);
        }
        if (arr == null || arr.size() == 0) {
            return Collections.emptyList();
        }
        List<MergeTranspondMessageItem> list = new ArrayList<>(arr.size());
        for (JsonElement el : arr) {
            if (!el.isJsonObject()) {
                continue;
            }
            MergeTranspondMessageItem item = parseItem(el.getAsJsonObject());
            if (item != null) {
                list.add(item);
            }
        }
        return list;
    }

    @Nullable
    private static MergeTranspondMessageItem parseItem(@NonNull JsonObject obj) {
        MergeTranspondMessageItem item = new MergeTranspondMessageItem();
        item.setFromMsgId(readString(obj, "fromMsgId"));
        item.setFromUserId(readString(obj, "fromUserId"));
        item.setCustomInfo(readString(obj, "customInfo"));
        item.setContentType(parseContentType(obj));
        item.setContent(readString(obj, "content"));
        if (item.getContentType() <= 0 && TextUtils.isEmpty(item.getContent())) {
            return null;
        }
        return item;
    }

    private static int parseContentType(@NonNull JsonObject obj) {
        if (!obj.has("contentType")) {
            return 0;
        }
        try {
            if (obj.get("contentType").isJsonPrimitive()) {
                return Integer.parseInt(obj.get("contentType").getAsString().trim());
            }
        } catch (Exception ignored) {
        }
        try {
            return obj.get("contentType").getAsInt();
        } catch (Exception ignored) {
            return 0;
        }
    }

    @NonNull
    public static String buildTitle(boolean isGroupChat, @Nullable String peerDisplayName) {
        if (isGroupChat) {
            return ImUiStrings.get(R.string.merge_chat_history_group);
        }
        String peer = !TextUtils.isEmpty(peerDisplayName)
                ? peerDisplayName.trim()
                : ImUiStrings.get(R.string.base_peer);
        return ImUiStrings.get(R.string.merge_chat_history_private, peer);
    }

    @NonNull
    public static String buildCompositeDigest(@NonNull List<MergeTranspondMessageItem> items,
                                              @Nullable String currentUserId,
                                              @Nullable Map<String, String> userIdToDisplayName) {
        if (items.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        int previewCount = Math.min(items.size(), 3);
        for (int i = 0; i < previewCount; i++) {
            MergeTranspondMessageItem item = items.get(i);
            String name = resolveDisplayName(item.getFromUserId(), currentUserId, userIdToDisplayName);
            String digest = formatItemDigest(item.getContentType(), item.getContent());
            if (digest.length() > 36) {
                digest = digest.substring(0, 33) + "...";
            }
            sb.append(name).append(": ").append(digest).append('\n');
        }
        if (items.size() > 3) {
            sb.append("...");
        } else {
            sb.append('\n');
        }
        return sb.toString();
    }

    @NonNull
    public static String formatItemDigest(int contentType, @Nullable String contentJson) {
        if (MessageContentFormatter.isImageMessage(contentType)) {
            return ImUiStrings.get(R.string.base_digest_img);
        }
        if (MessageContentFormatter.isVoiceMessage(contentType)) {
            String duration = MessageContentFormatter.parseVoiceDurationLabel(contentJson);
            String audio = ImUiStrings.get(R.string.base_digest_audio);
            return duration.isEmpty() ? audio : audio + " " + duration;
        }
        if (MessageContentFormatter.isVideoMessage(contentType)) {
            return ImUiStrings.get(R.string.base_digest_video);
        }
        if (MessageContentFormatter.isFileMessage(contentType)) {
            return MessageContentFormatter.formatDisplayText(
                    buildStubMessage(contentType, contentJson), null, null);
        }
        if (MessageContentFormatter.isMapMessage(contentType)) {
            return ImUiStrings.get(R.string.base_digest_location);
        }
        if (MessageContentFormatter.isCardMessage(contentType)) {
            return ImUiStrings.get(R.string.base_digest_person_card);
        }
        if (MessageContentFormatter.isRedPacketMessage(contentType)) {
            return ImUiStrings.get(R.string.base_digest_red_package);
        }
//        if (MessageContentFormatter.isHongbaoClaimMessage(contentType)) {
//            return MessageContentFormatter.formatHongbaoClaimDisplay(contentJson, null);
//        }
        if (MessageContentFormatter.isNoticeMessage(contentType)) {
            return ImUiStrings.get(R.string.base_digest_notice);
        }
        if (MessageContentFormatter.isMeetStartMessage(contentType)) {
            return ImUiStrings.get(R.string.base_digest_meet_ongoing);
        }
        if (MessageContentFormatter.isMeetEndMessage(contentType)) {
            return ImUiStrings.get(R.string.base_digest_meet_ended);
        }
        if (MessageType.MessageContentType.isMergeTranspond(contentType)) {
            return ImUiStrings.get(R.string.base_digest_chat_record_plain);
        }
        return MessageContentFormatter.extractPlainText(contentJson);
    }

    @NonNull
    private static com.freddy.im.bean.AppMessage buildStubMessage(int contentType,
                                                                  @Nullable String contentJson) {
        com.freddy.im.bean.AppMessage message = new com.freddy.im.bean.AppMessage();
        message.setMsgContentType(contentType);
        message.setContent(contentJson != null ? contentJson : "");
        return message;
    }

    @NonNull
    public static String resolveDisplayName(@Nullable String userId,
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
            return ImUiStrings.get(R.string.base_you);
        }
        return userId;
    }

    @NonNull
    public static String resolvePortrait(@Nullable String userId,
                                         @Nullable Map<String, com.freddy.im.http.model.im.GroupMemberItem> members) {
        if (TextUtils.isEmpty(userId) || members == null) {
            return "";
        }
        com.freddy.im.http.model.im.GroupMemberItem member = members.get(userId);
        if (member == null || TextUtils.isEmpty(member.getHeadPortrait())) {
            return "";
        }
        return member.getHeadPortrait().trim();
    }

    @Nullable
    private static String readString(@NonNull JsonObject obj, @NonNull String key) {
        if (!obj.has(key) || !obj.get(key).isJsonPrimitive()) {
            return null;
        }
        String value = obj.get(key).getAsString();
        return TextUtils.isEmpty(value) ? null : value;
    }

    @Nullable
    private static JsonObject parseObject(@Nullable String raw) {
        if (TextUtils.isEmpty(raw)) {
            return null;
        }
        try {
            return JsonParser.parseString(raw.trim()).getAsJsonObject();
        } catch (Exception ignored) {
            return null;
        }
    }
}
