package com.freddy.chat.im;

import android.graphics.BitmapFactory;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.freddy.chat.utils.AppGson;
import com.freddy.chat.utils.MediaMessageContentUtil;
import com.freddy.chat.utils.StringUtil;
import com.freddy.im.IMSConfig;
import com.freddy.im.MessageType;
import com.freddy.im.bean.AppMessage;
import com.freddy.im.http.model.im.UploadRes;
import com.freddy.im.signal.ImSignalProtos;

import java.io.File;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * App 消息与 imsignal {@link ImSignalProtos.SignalRequest} 转换。
 */
public class MessageBuilder {

    public static AppMessage buildNewTextMessage(String toId, int conversationType,String content) {
        return buildTextMessage(null,conversationType,toId,content);
    }

    public static AppMessage buildTextMessage(String sessionId,int conversationType,String toId, String content) {
        String userId = IMSClientBootstrap.getInstance().getCurrentUserId();
        AppMessage message = new AppMessage();
        if (!TextUtils.isEmpty(sessionId)){
            message.setConversationId(sessionId);
        }
        message.setClientMsgId(UUID.randomUUID().toString());
        message.setMsgContentType(MessageType.MessageContentType.TEXT.getMsgContentType());
        message.setFromId(userId);
        message.setToId(toId);
        message.setTimestamp(System.currentTimeMillis());
        message.setConversationTypeEnum(ImSignalProtos.ConversationType.forNumber(conversationType));
        message.setContent(wrapTextContentAsJson(content));
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
        String userId = IMSClientBootstrap.getInstance().getCurrentUserId();
        AppMessage message = new AppMessage();
        if (!TextUtils.isEmpty(sessionId)) {
            message.setConversationId(sessionId);
        }
        message.setClientMsgId(UUID.randomUUID().toString());
        message.setMsgContentType(MessageType.MessageContentType.IMAGE.getMsgContentType());
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

    /** 文件消息 content：{@code {"url":"https://...","msg":"name.ext"}} */
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

    /** 文本消息 content 包装为 {@code {"msg":"..."}} */
    private static String wrapTextContentAsJson(String content) {
        Map<String, String> body = Collections.singletonMap("msg", content != null ? content : "");
        return AppGson.get().toJson(body);
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
        Map<String, Object> body = new LinkedHashMap<>(3);
        body.put(MediaMessageContentUtil.KEY_URL, remoteUrl);
        body.put("msg", fileName);
        putLocalUrl(body, localPath);
        return AppGson.get().toJson(body);
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
