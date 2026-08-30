package com.freddy.chat.im;

import android.graphics.BitmapFactory;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.freddy.chat.utils.AppGson;
import com.freddy.chat.utils.CardMessageContentUtil;
import com.freddy.chat.utils.GroupInviteMessageContentUtil;
import com.freddy.chat.utils.HongBaoMessageContentUtil;
import com.freddy.chat.utils.NoticeMessageContentUtil;
import com.freddy.chat.utils.MediaMessageContentUtil;
import com.freddy.chat.utils.QuoteMessageContentUtil;
import com.freddy.chat.utils.StringUtil;
import com.freddy.im.IMSConfig;
import com.freddy.im.MessageType;
import com.freddy.im.bean.AppMessage;
import com.freddy.im.http.model.im.UploadRes;
import com.freddy.im.signal.ImSignalProtos;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * App 消息与 imsignal {@link ImSignalProtos.SignalRequest} 转换。
 */
public class MessageBuilder {

    public static AppMessage buildNewTextMessage(String toId, int conversationType,String content) {
        return buildTextMessage(null,conversationType,toId,content);
    }

    /**
     * 逐条转发：复制源消息内容与类型，构造发往目标会话的新消息（走 SEND_MSG 信令）。
     */
    @NonNull
    public static AppMessage buildForwardedMessage(@NonNull AppMessage source,
                                                   @Nullable String targetSessionId,
                                                   int targetConversationType,
                                                   @NonNull String targetToId) {
        String userId = IMSClientBootstrap.getInstance().getCurrentUserId();
        AppMessage message = new AppMessage();
        if (!TextUtils.isEmpty(targetSessionId)) {
            message.setConversationId(targetSessionId);
        }
        message.setClientMsgId(UUID.randomUUID().toString());
        message.setMsgContentType(source.getMsgContentType());
        message.setFromId(userId);
        message.setToId(targetToId);
        message.setTimestamp(System.currentTimeMillis());
        message.setConversationTypeEnum(ImSignalProtos.ConversationType.forNumber(targetConversationType));
        message.setContent(source.getContent() != null ? source.getContent() : "");
        message.setCustomInfo(source.getCustomInfo());
        message.setExtend(source.getExtend());
        message.setSendStatus(IMSConfig.SEND_STATUS_SENDING);
        return message;
    }

    public static AppMessage buildTextMessage(String sessionId, int conversationType, String toId, String content) {
        return buildTextMessage(sessionId, conversationType, toId, content, null);
    }

    public static AppMessage buildTextMessage(String sessionId,
                                              int conversationType,
                                              String toId,
                                              String content,
                                              @Nullable String customInfo) {
        return buildTextMessage(sessionId, conversationType, toId, content, customInfo, null);
    }

    public static AppMessage buildTextMessage(String sessionId,
                                              int conversationType,
                                              String toId,
                                              String content,
                                              @Nullable String customInfo,
                                              @Nullable QuoteMessageContentUtil.QuoteInfo quoteInfo) {
        String userId = IMSClientBootstrap.getInstance().getCurrentUserId();
        AppMessage message = new AppMessage();
        if (!TextUtils.isEmpty(sessionId)) {
            message.setConversationId(sessionId);
        }
        message.setClientMsgId(UUID.randomUUID().toString());
        message.setMsgContentType(MessageType.MessageContentType.TEXT.getMsgContentType());
        message.setFromId(userId);
        message.setToId(toId);
        message.setTimestamp(System.currentTimeMillis());
        message.setConversationTypeEnum(ImSignalProtos.ConversationType.forNumber(conversationType));
        message.setContent(wrapTextContentAsJson(content, quoteInfo));
        if (!TextUtils.isEmpty(customInfo)) {
            message.setCustomInfo(customInfo);
        }
        message.setSendStatus(IMSConfig.SEND_STATUS_SENDING);
        return message;
    }

    /**
     * 构造图片消息：{@code url} 为文件服务返回的远程地址；{@code dw/dh/msg} 由本地文件解析。
     */
    @NonNull
    public static AppMessage buildImageMessage(String sessionId,
                                               int conversationType,
                                               @NonNull String toId,
                                               @NonNull String localImagePath,
                                               @NonNull String remoteUrl,
                                               @Nullable String thumbImage,
                                               @Nullable String largeImage) {
        return buildImageMessage(sessionId, conversationType, toId, localImagePath, remoteUrl,
                thumbImage, largeImage, MessageType.MessageContentType.IMAGE.getMsgContentType());
    }

    /**
     * @param contentType {@link MessageType.MessageContentType#IMAGE}(200)
     *                    或 {@link MessageType.MessageContentType#MEETING_IMAGE}(201)
     */
    @NonNull
    public static AppMessage buildImageMessage(String sessionId,
                                               int conversationType,
                                               @NonNull String toId,
                                               @NonNull String localImagePath,
                                               @NonNull String remoteUrl,
                                               @Nullable String thumbImage,
                                               @Nullable String largeImage,
                                               int contentType) {
        String userId = IMSClientBootstrap.getInstance().getCurrentUserId();
        AppMessage message = new AppMessage();
        if (!TextUtils.isEmpty(sessionId)) {
            message.setConversationId(sessionId);
        }
        message.setClientMsgId(UUID.randomUUID().toString());
        int type = MessageType.MessageContentType.isImage(contentType)
                ? contentType
                : MessageType.MessageContentType.IMAGE.getMsgContentType();
        message.setMsgContentType(type);
        message.setFromId(userId);
        message.setToId(toId);
        message.setTimestamp(System.currentTimeMillis());
        message.setConversationTypeEnum(ImSignalProtos.ConversationType.forNumber(conversationType));
        message.setContent(wrapImageContentAsJson(localImagePath, remoteUrl, thumbImage, largeImage));
        message.setSendStatus(IMSConfig.SEND_STATUS_SENDING);
        return message;
    }

    @NonNull
    public static AppMessage buildVideoMessage(@Nullable String sessionId,
                                               int conversationType,
                                               @NonNull String toId,
                                               int durationSec,
                                               @NonNull String localVideoPath,
                                               @Nullable String remoteVideoUrl,
                                               @Nullable String localCoverPath,
                                               @Nullable String remoteCoverUrl,
                                               int displayWidth,
                                               int displayHeight) {
        String userId = IMSClientBootstrap.getInstance().getCurrentUserId();
        AppMessage message = new AppMessage();
        if (!TextUtils.isEmpty(sessionId)) {
            message.setConversationId(sessionId);
        }
        message.setClientMsgId(UUID.randomUUID().toString());
        message.setMsgContentType(MessageType.MessageContentType.VIDEO.getMsgContentType());
        message.setFromId(userId);
        message.setToId(toId);
        message.setTimestamp(System.currentTimeMillis());
        message.setConversationTypeEnum(ImSignalProtos.ConversationType.forNumber(conversationType));
        message.setContent(wrapVideoContentAsJson(
                durationSec,
                localVideoPath,
                remoteVideoUrl,
                localCoverPath,
                remoteCoverUrl,
                displayWidth,
                displayHeight));
        message.setSendStatus(IMSConfig.SEND_STATUS_SENDING);
        return message;
    }

    /** 上传完成后将图片 content 中的 url 替换为 CDN 地址。 */
    public static void applyImageUpload(@NonNull AppMessage message,
                                      @NonNull String localImagePath,
                                      @NonNull UploadRes upload) {
        message.setContent(wrapImageContentAsJson(
                localImagePath,
                upload.getUrl(),
                upload.getThumbImage(),
                upload.getLargeImage()));
    }

    /** 批量上传完成后写入远端视频/封面 URL，保留本地路径。 */
    public static void applyVideoUpload(@NonNull AppMessage message,
                                      int durationSec,
                                      @NonNull String localVideoPath,
                                      @Nullable String localCoverPath,
                                      @NonNull String remoteVideoUrl,
                                      @Nullable String remoteCoverUrl,
                                      int displayWidth,
                                      int displayHeight) {
        message.setContent(wrapVideoContentAsJson(
                durationSec,
                localVideoPath,
                remoteVideoUrl,
                localCoverPath,
                remoteCoverUrl,
                displayWidth,
                displayHeight));
    }

    public static void applyFileUpload(@NonNull AppMessage message,
                                       @NonNull String fileName,
                                       @NonNull String localPath,
                                       @NonNull String remoteUrl) {
        message.setContent(wrapFileContentAsJson(fileName, remoteUrl, localPath));
    }

    public static void applyVoiceUpload(@NonNull AppMessage message,
                                        int durationSec,
                                        @NonNull String localPath,
                                        @NonNull String remoteUrl) {
        message.setContent(wrapVoiceContentAsJson(durationSec, remoteUrl, localPath));
    }

    /** 文件消息 content：{@code {"url":"https://...","msg":"name.ext","size":12345}} */
    @NonNull
    public static AppMessage buildFileMessage(@Nullable String sessionId,
                                              int conversationType,
                                              @NonNull String toId,
                                              @NonNull String fileName,
                                              @NonNull String remoteUrl) {
        String userId = IMSClientBootstrap.getInstance().getCurrentUserId();
        AppMessage message = new AppMessage();
        if (!TextUtils.isEmpty(sessionId)) {
            message.setConversationId(sessionId);
        }
        message.setClientMsgId(UUID.randomUUID().toString());
        message.setMsgContentType(MessageType.MessageContentType.FILE.getMsgContentType());
        message.setFromId(userId);
        message.setToId(toId);
        message.setTimestamp(System.currentTimeMillis());
        message.setConversationTypeEnum(ImSignalProtos.ConversationType.forNumber(conversationType));
        message.setContent(wrapFileContentAsJson(fileName, remoteUrl, remoteUrl));
        message.setSendStatus(IMSConfig.SEND_STATUS_SENDING);
        return message;
    }

    /** 语音消息 content：{@code localUrl} + {@code url} */
    @NonNull
    public static AppMessage buildVoiceMessage(@Nullable String sessionId,
                                               int conversationType,
                                               @NonNull String toId,
                                               int durationSec,
                                               @NonNull String remoteUrl) {
        String userId = IMSClientBootstrap.getInstance().getCurrentUserId();
        AppMessage message = new AppMessage();
        if (!TextUtils.isEmpty(sessionId)) {
            message.setConversationId(sessionId);
        }
        message.setClientMsgId(UUID.randomUUID().toString());
        message.setMsgContentType(MessageType.MessageContentType.VOICE.getMsgContentType());
        message.setFromId(userId);
        message.setToId(toId);
        message.setTimestamp(System.currentTimeMillis());
        message.setConversationTypeEnum(ImSignalProtos.ConversationType.forNumber(conversationType));
        message.setContent(wrapVoiceContentAsJson(durationSec, remoteUrl, remoteUrl));
        message.setSendStatus(IMSConfig.SEND_STATUS_SENDING);
        return message;
    }

    /** 个人名片消息 contentType=700。 */
    @NonNull
    public static AppMessage buildCardMessage(@Nullable String sessionId,
                                              int conversationType,
                                              @NonNull String toId,
                                              @NonNull String cardUserId,
                                              @NonNull String displayName,
                                              @Nullable String portrait,
                                              @Nullable String idno) {
        String userId = IMSClientBootstrap.getInstance().getCurrentUserId();
        AppMessage message = new AppMessage();
        if (!TextUtils.isEmpty(sessionId)) {
            message.setConversationId(sessionId);
        }
        message.setClientMsgId(UUID.randomUUID().toString());
        message.setMsgContentType(MessageType.MessageContentType.CARD.getMsgContentType());
        message.setFromId(userId);
        message.setToId(toId);
        message.setTimestamp(System.currentTimeMillis());
        message.setConversationTypeEnum(ImSignalProtos.ConversationType.forNumber(conversationType));
        message.setContent(wrapCardContentAsJson(cardUserId, displayName, portrait, idno, userId));
        message.setSendStatus(IMSConfig.SEND_STATUS_SENDING);
        return message;
    }

    /**
     * 群邀请卡片（contentType=700，content.msg=GroupInvite），发往被邀请人的单聊。
     */
    @NonNull
    public static AppMessage buildGroupInviteMessage(@Nullable String sessionId,
                                                     @NonNull String toUserId,
                                                     @NonNull String groupId,
                                                     @Nullable String groupName,
                                                     @Nullable String groupIcon,
                                                     @Nullable String fromName) {
        String userId = IMSClientBootstrap.getInstance().getCurrentUserId();
        AppMessage message = new AppMessage();
        if (!TextUtils.isEmpty(sessionId)) {
            message.setConversationId(sessionId);
        }
        message.setClientMsgId(UUID.randomUUID().toString());
        message.setMsgContentType(MessageType.MessageContentType.CARD.getMsgContentType());
        message.setFromId(userId);
        message.setToId(toUserId);
        message.setTimestamp(System.currentTimeMillis());
        message.setConversationTypeEnum(ImSignalProtos.ConversationType.forNumber(
                MessageType.SINGLE_CHAT.getMsgType()));
        message.setContent(wrapGroupInviteContentAsJson(
                groupId, groupName, groupIcon, userId, fromName));
        message.setSendStatus(IMSConfig.SEND_STATUS_SENDING);
        return message;
    }

    /** 红包气泡消息 contentType=720。 */
    @NonNull
    public static AppMessage buildRedPacketMessage(@Nullable String sessionId,
                                                   int conversationType,
                                                   @NonNull String toId,
                                                   @Nullable String hongbaoId,
                                                   @Nullable String price,
                                                   @Nullable String hongbaoDesc,
                                                   @Nullable String lingquStatus,
                                                   @Nullable String hongbaoLeixing,
                                                   @Nullable String zhuanshuUserId,
                                                   @Nullable String zhuanshuUserName,
                                                   @Nullable String zhuanshuUserAvatar,
                                                   @Nullable List<String> lqUsers) {
        String userId = IMSClientBootstrap.getInstance().getCurrentUserId();
        AppMessage message = new AppMessage();
        if (!TextUtils.isEmpty(sessionId)) {
            message.setConversationId(sessionId);
        }
        message.setClientMsgId(UUID.randomUUID().toString());
        message.setMsgContentType(MessageType.MessageContentType.RED_PACKET.getMsgContentType());
        message.setFromId(userId);
        message.setToId(toId);
        message.setTimestamp(System.currentTimeMillis());
        message.setConversationTypeEnum(ImSignalProtos.ConversationType.forNumber(conversationType));
        message.setContent(wrapRedPacketContentAsJson(
                hongbaoId, price, hongbaoDesc, lingquStatus, hongbaoLeixing,
                zhuanshuUserId, zhuanshuUserName, zhuanshuUserAvatar, lqUsers));
        message.setSendStatus(IMSConfig.SEND_STATUS_SENDING);
        return message;
    }

//    /** 群红包领取通知 contentType=104。 */
//    @NonNull
//    public static AppMessage buildHongbaoClaimMessage(@Nullable String sessionId,
//                                                      int conversationType,
//                                                      @NonNull String toId,
//                                                      @Nullable String tipType,
//                                                      @Nullable String sendUserId,
//                                                      @Nullable String sendUserName,
//                                                      @Nullable String lingquUserId,
//                                                      @Nullable String lingquUserName) {
//        String userId = IMSClientBootstrap.getInstance().getCurrentUserId();
//        AppMessage message = new AppMessage();
//        if (!TextUtils.isEmpty(sessionId)) {
//            message.setConversationId(sessionId);
//        }
//        message.setClientMsgId(UUID.randomUUID().toString());
//        message.setMsgContentType(MessageType.MessageContentType.HONGBAO_CLAIM.getMsgContentType());
//        message.setFromId(userId);
//        message.setToId(toId);
//        message.setTimestamp(System.currentTimeMillis());
//        message.setConversationTypeEnum(ImSignalProtos.ConversationType.forNumber(conversationType));
//        message.setContent(wrapHongbaoClaimContentAsJson(
//                tipType, sendUserId, sendUserName, lingquUserId, lingquUserName));
//        message.setSendStatus(IMSConfig.SEND_STATUS_SENDING);
//        return message;
//    }

    /** 系统公告消息 contentType=710。 */
    @NonNull
    public static AppMessage buildNoticeMessage(@Nullable String sessionId,
                                                int conversationType,
                                                @NonNull String toId,
                                                @Nullable String id,
                                                @Nullable String contentUrl,
                                                @Nullable String createTime,
                                                @Nullable String noticeTitle,
                                                @Nullable String previewContent,
                                                int status) {
        String userId = IMSClientBootstrap.getInstance().getCurrentUserId();
        AppMessage message = new AppMessage();
        if (!TextUtils.isEmpty(sessionId)) {
            message.setConversationId(sessionId);
        }
        message.setClientMsgId(UUID.randomUUID().toString());
        message.setMsgContentType(MessageType.MessageContentType.SYS_NOTICE.getMsgContentType());
        message.setFromId(userId);
        message.setToId(toId);
        message.setTimestamp(System.currentTimeMillis());
        message.setConversationTypeEnum(ImSignalProtos.ConversationType.forNumber(conversationType));
        message.setContent(wrapNoticeContentAsJson(id, contentUrl, createTime, noticeTitle, previewContent, status));
        message.setSendStatus(IMSConfig.SEND_STATUS_SENDING);
        return message;
    }

    /** 公告列表/详情分享：仅构造内容与类型，供内联转发。 */
    @NonNull
    public static AppMessage buildForwardableNoticeMessage(@Nullable String id,
                                                           @Nullable String contentUrl,
                                                           @Nullable String createTime,
                                                           @Nullable String noticeTitle,
                                                           @Nullable String previewContent,
                                                           int status) {
        String userId = IMSClientBootstrap.getInstance().getCurrentUserId();
        AppMessage message = new AppMessage();
        message.setClientMsgId(UUID.randomUUID().toString());
        message.setMsgContentType(MessageType.MessageContentType.SYS_NOTICE.getMsgContentType());
        message.setFromId(userId);
        message.setTimestamp(System.currentTimeMillis());
        message.setContent(wrapNoticeContentAsJson(id, contentUrl, createTime, noticeTitle, previewContent, status));
        message.setSendStatus(IMSConfig.SEND_STATUS_SENDING);
        return message;
    }

    @NonNull
    private static String wrapNoticeContentAsJson(@Nullable String id,
                                                  @Nullable String contentUrl,
                                                  @Nullable String createTime,
                                                  @Nullable String noticeTitle,
                                                  @Nullable String previewContent,
                                                  int status) {
        Map<String, Object> body = new LinkedHashMap<>(7);
        body.put("msg", NoticeMessageContentUtil.MSG_ANNOUNCEMENT);
        if (!TextUtils.isEmpty(id)) {
            body.put("id", id);
        }
        if (!TextUtils.isEmpty(contentUrl)) {
            body.put("contentUrl", contentUrl);
        }
        if (!TextUtils.isEmpty(createTime)) {
            body.put("createTime", createTime);
        }
        if (!TextUtils.isEmpty(noticeTitle)) {
            body.put("noticeTitle", noticeTitle);
        }
        if (!TextUtils.isEmpty(previewContent)) {
            body.put("previewContent", previewContent);
        }
        body.put("status", status);
        return AppGson.get().toJson(body);
    }

    @NonNull
    private static String wrapCardContentAsJson(@NonNull String cardUserId,
                                                @NonNull String displayName,
                                                @Nullable String portrait,
                                                @Nullable String idno,
                                                @NonNull String fromUserId) {
        Map<String, Object> body = new LinkedHashMap<>(6);
        body.put("msg", CardMessageContentUtil.MSG_CARD);
        body.put("userId", cardUserId);
        body.put("displayName", displayName);
        if (!TextUtils.isEmpty(portrait)) {
            body.put("portrait", portrait);
        }
        if (!TextUtils.isEmpty(idno)) {
            body.put("idno", idno);
        }
        body.put("from", fromUserId);
        return AppGson.get().toJson(body);
    }

    @NonNull
    private static String wrapGroupInviteContentAsJson(@NonNull String groupId,
                                                       @Nullable String groupName,
                                                       @Nullable String groupIcon,
                                                       @NonNull String fromUserId,
                                                       @Nullable String fromName) {
        Map<String, Object> body = new LinkedHashMap<>(6);
        body.put("msg", GroupInviteMessageContentUtil.MSG_GROUP_INVITE);
        body.put("groupId", groupId);
        body.put("groupName", groupName != null ? groupName : "");
        if (!TextUtils.isEmpty(groupIcon)) {
            body.put("groupIcon", groupIcon);
        }
        body.put("from", fromUserId);
        body.put("fromName", fromName != null ? fromName : "");
        return AppGson.get().toJson(body);
    }

    @NonNull
    private static String wrapRedPacketContentAsJson(@Nullable String hongbaoId,
                                                     @Nullable String price,
                                                     @Nullable String hongbaoDesc,
                                                     @Nullable String lingquStatus,
                                                     @Nullable String hongbaoLeixing,
                                                     @Nullable String zhuanshuUserId,
                                                     @Nullable String zhuanshuUserName,
                                                     @Nullable String zhuanshuUserAvatar,
                                                     @Nullable List<String> lqUsers) {
        Map<String, Object> body = new LinkedHashMap<>(9);
        body.put(HongBaoMessageContentUtil.KEY_HONGBAO_ID, hongbaoId != null ? hongbaoId : "");
        body.put(HongBaoMessageContentUtil.KEY_PRICE, price != null ? price : "");
        body.put(HongBaoMessageContentUtil.KEY_HONGBAO_DESC, hongbaoDesc != null ? hongbaoDesc : "");
        body.put(HongBaoMessageContentUtil.KEY_LINGQU_STATUS, lingquStatus != null ? lingquStatus : "0");
        body.put(HongBaoMessageContentUtil.KEY_HONGBAO_LEIXING, hongbaoLeixing != null ? hongbaoLeixing : "");
        body.put(HongBaoMessageContentUtil.KEY_ZHUANSHU_USER_ID, zhuanshuUserId != null ? zhuanshuUserId : "");
        body.put(HongBaoMessageContentUtil.KEY_ZHUANSHU_USER_NAME, zhuanshuUserName != null ? zhuanshuUserName : "");
        body.put(HongBaoMessageContentUtil.KEY_ZHUANSHU_USER_AVATAR,
                zhuanshuUserAvatar != null ? zhuanshuUserAvatar : "");
        body.put(HongBaoMessageContentUtil.KEY_LQ_USERS,
                lqUsers != null ? lqUsers : new ArrayList<String>());
        return AppGson.get().toJson(body);
    }

    @NonNull
    private static String wrapHongbaoClaimContentAsJson(@Nullable String tipType,
                                                        @Nullable String sendUserId,
                                                        @Nullable String sendUserName,
                                                        @Nullable String lingquUserId,
                                                        @Nullable String lingquUserName) {
        Map<String, Object> body = new LinkedHashMap<>(5);
        body.put(HongBaoMessageContentUtil.KEY_TIP_TYPE, tipType != null ? tipType : "");
        body.put(HongBaoMessageContentUtil.KEY_SEND_USER_ID, sendUserId != null ? sendUserId : "");
        body.put(HongBaoMessageContentUtil.KEY_SEND_USER_NAME, sendUserName != null ? sendUserName : "");
        body.put(HongBaoMessageContentUtil.KEY_LINGQU_USER_ID, lingquUserId != null ? lingquUserId : "");
        body.put(HongBaoMessageContentUtil.KEY_LINGQU_USER_NAME, lingquUserName != null ? lingquUserName : "");
        return AppGson.get().toJson(body);
    }

    /** 文本消息 content 包装为 {@code {"msg":"..."}} */
    private static String wrapTextContentAsJson(String content) {
        return wrapTextContentAsJson(content, null);
    }

    private static String wrapTextContentAsJson(String content,
                                                @Nullable QuoteMessageContentUtil.QuoteInfo quoteInfo) {
        com.google.gson.JsonObject body = new com.google.gson.JsonObject();
        body.addProperty("msg", content != null ? content : "");
        if (quoteInfo != null) {
            body.add(QuoteMessageContentUtil.KEY_QUOTE, QuoteMessageContentUtil.toJsonObject(quoteInfo));
        }
        return body.toString();
    }

    /** 图片消息 content：{@code localUrl} 本地路径，{@code url} 远程/CDN。 */
    @NonNull
    private static String wrapImageContentAsJson(@NonNull String localImagePath,
                                                 @NonNull String remoteUrl,
                                                 @Nullable String thumbImage,
                                                 @Nullable String largeImage) {
        Map<String, Object> body = new LinkedHashMap<>(7);
        body.put("msg", new File(localImagePath).getName());
        int[] size = resolveImageSize(localImagePath);
        body.put("dw", size[0]);
        body.put("dh", size[1]);
        body.put(MediaMessageContentUtil.KEY_URL, remoteUrl);
        putLocalUrl(body, localImagePath);
        if (!TextUtils.isEmpty(thumbImage)) {
            body.put(MediaMessageContentUtil.KEY_THUMB_URL, thumbImage);
        }
        if (!TextUtils.isEmpty(largeImage)) {
            body.put("largeImage", largeImage);
        }
        return AppGson.get().toJson(body);
    }

    @NonNull
    private static String wrapFileContentAsJson(@NonNull String fileName,
                                                @NonNull String remoteUrl,
                                                @Nullable String localPath) {
        Map<String, Object> body = new LinkedHashMap<>(4);
        body.put(MediaMessageContentUtil.KEY_URL, remoteUrl);
        body.put("msg", fileName);
        putLocalUrl(body, localPath);
        long sizeBytes = resolveLocalFileSizeBytes(localPath);
        if (sizeBytes > 0L) {
            body.put("size", sizeBytes);
        }
        return AppGson.get().toJson(body);
    }

    private static long resolveLocalFileSizeBytes(@Nullable String localPath) {
        if (TextUtils.isEmpty(localPath)) {
            return 0L;
        }
        String trimmed = localPath.trim();
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return 0L;
        }
        File file = new File(trimmed);
        return file.isFile() ? file.length() : 0L;
    }

    @NonNull
    private static String wrapVoiceContentAsJson(int durationSec,
                                                 @NonNull String remoteUrl,
                                                 @Nullable String localPath) {
        Map<String, Object> body = new LinkedHashMap<>(3);
        body.put("duration", Math.max(0, durationSec));
        body.put(MediaMessageContentUtil.KEY_URL, remoteUrl);
        putLocalUrl(body, localPath);
        return AppGson.get().toJson(body);
    }

    @NonNull
    private static String wrapVideoContentAsJson(int durationSec,
                                                 @NonNull String localVideoPath,
                                                 @Nullable String remoteVideoUrl,
                                                 @Nullable String localCoverPath,
                                                 @Nullable String remoteCoverUrl,
                                                 int displayWidth,
                                                 int displayHeight) {
        Map<String, Object> body = new LinkedHashMap<>(9);
        body.put("msg", new File(localVideoPath).getName());
        body.put("duration", Math.max(0, durationSec));
        int dw = Math.max(displayWidth, 0);
        int dh = Math.max(displayHeight, 0);
        if (dw <= 0) {
            dw = 180;
        }
        if (dh <= 0) {
            dh = 120;
        }
        body.put("dw", dw);
        body.put("dh", dh);
        if (!TextUtils.isEmpty(remoteVideoUrl)) {
            body.put(MediaMessageContentUtil.KEY_URL, remoteVideoUrl);
        }
        putLocalUrl(body, localVideoPath);
        if (!TextUtils.isEmpty(remoteCoverUrl)) {
            body.put(MediaMessageContentUtil.KEY_COVER_URL, remoteCoverUrl);
        }
        if (!TextUtils.isEmpty(localCoverPath)) {
            body.put(MediaMessageContentUtil.KEY_LOCAL_COVER_URL, localCoverPath);
        }
        return AppGson.get().toJson(body);
    }

    private static void putLocalUrl(@NonNull Map<String, Object> body, @Nullable String localPath) {
        if (!TextUtils.isEmpty(localPath)) {
            body.put(MediaMessageContentUtil.KEY_LOCAL_URL, localPath);
        }
    }

    @NonNull
    private static int[] resolveImageSize(@NonNull String imagePath) {
        int width = 0;
        int height = 0;
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(imagePath, options);
            width = Math.max(options.outWidth, 0);
            height = Math.max(options.outHeight, 0);
        } catch (Exception ignored) {
        }
        return new int[]{width, height};
    }

    /**
     * 构造发送单聊消息的 {@link ImSignalProtos.SignalRequest}（外层由 IMS 再 AES+0x02 封装）。
     */
    public static ImSignalProtos.SignalRequest buildSendMsgSignalRequest(AppMessage message, String bindToken) {
        ImSignalProtos.SendMsgReq.Builder smb = ImSignalProtos.SendMsgReq.newBuilder()
                .setConversation(message.getConversationTypeEnum())
                .setFrom(message.getFromId())
                .setTo(message.getToId())
                .setContentType(String.valueOf(message.getMsgContentType()))
//                .setContentType("text/plain")
                .setContent(message.getContent() != null ? message.getContent() : "")
                .setIdempotentId(message.getClientMsgId())
                .setClientMsgId(message.getClientMsgId());
        if (!StringUtil.isEmpty(message.getCustomInfo())) {
            smb.setCustomInfo(message.getCustomInfo());
        }
        ImSignalProtos.ClientRequestHeader.Builder hb = ImSignalProtos.ClientRequestHeader.newBuilder()
                .setVersion(1)
                .setUserId(message.getFromId())
                .setClientType(ImSignalProtos.ClientType.ANDROID_VALUE)
                .setCmd(ImSignalProtos.Cmd.SEND_MSG_VALUE);
        if (!StringUtil.isEmpty(bindToken)) {
            hb.setToken(bindToken);
        }
        return ImSignalProtos.SignalRequest.newBuilder()
                .setHeader(hb.build())
                .setBody(smb.build().toByteString())
                .build();
    }
}
