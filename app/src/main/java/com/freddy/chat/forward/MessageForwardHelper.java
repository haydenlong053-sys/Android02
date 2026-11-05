package com.freddy.chat.forward;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cboda.baselibrary.utils.ELogUtilKt;
import com.cboda.chatui.conversation.forward.ForwardPromptBroadView;
import com.freddy.chat.im.IMSClientBootstrap;
import com.freddy.chat.im.MessageBuilder;
import com.freddy.chat.im.MessageProcessor;
import com.freddy.chat.utils.MediaMessageContentUtil;
import com.freddy.chat.utils.WeChatUiHelper;
import com.freddy.im.MessageType;
import com.freddy.im.bean.AppMessage;
import com.freddy.im.http.ImMessageHttpExample;
import com.freddy.im.http.model.im.SessionItem;
import com.freddy.im.http.model.im.TranspondMessageContent;
import com.freddy.im.http.model.im.TranspondMessageParam;
import com.freddy.im.listener.OnHttpImListener;
import com.freddy.im.message.ImMessageManager;
import com.freddy.im.message.MessageContentFormatter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 转发确认预览与发送（逐条：{@link MessageProcessor#sendMsg}；合并：transpond）。
 */
public final class MessageForwardHelper {

    private MessageForwardHelper() {
    }

    @NonNull
    public static String buildMergeTitle(boolean sourceIsGroup,
                                         @Nullable String peerDisplayName) {
        if (sourceIsGroup) {
            return "群的聊天记录";
        }
        String peer = !TextUtils.isEmpty(peerDisplayName) ? peerDisplayName.trim() : "对方";
        return "你和" + peer + "的聊天记录";
    }

    public static void bindForwardPrompt(@NonNull ForwardPromptBroadView view,
                                         @NonNull SessionItem target,
                                         @NonNull List<AppMessage> sourceMessages,
                                         @NonNull ForwardMode mode,
                                         boolean sourceIsGroup,
                                         @Nullable String sourcePeerName) {
        String targetName = resolveSessionDisplayName(target);
        String targetPortrait = WeChatUiHelper.resolveAvatarUrl(target);
        if (mode == ForwardMode.MERGE && sourceMessages.size() > 1) {
            String title = buildMergeTitle(sourceIsGroup, sourcePeerName);
            view.bind(targetName, targetPortrait,
                    view.getContext().getString(com.cboda.baselibrary.R.string.base_digest_chat_record) + title);
            return;
        }
        if (sourceMessages.size() == 1) {
            bindSingleMessagePrompt(view, targetName, targetPortrait, sourceMessages.get(0));
            return;
        }
        view.bind(targetName, targetPortrait,
                view.getContext().getString(com.cboda.chatui.R.string.msg_forward_one_by_one_with_count,
                        sourceMessages.size()));
    }

    private static void bindSingleMessagePrompt(@NonNull ForwardPromptBroadView view,
                                                @NonNull String targetName,
                                                @Nullable String targetPortrait,
                                                @NonNull AppMessage message) {
//        ELogUtilKt.eLogD(ELogUtilKt.APP_IM_TAG,"======"+targetName);
        int type = message.getMsgContentType();
        String currentUserId = IMSClientBootstrap.getInstance().getCurrentUserId();
        boolean sentByMe = message.isSender(currentUserId);
        if (MessageContentFormatter.isImageMessage(type)) {
//            String url = MediaMessageContentUtil.resolveDisplayUrl(message.getContent(), sentByMe);
//            view.bind2(targetName, targetPortrait, url, 0, 0);
            view.bind(targetName, targetPortrait,
                    view.getContext().getString(com.cboda.baselibrary.R.string.base_digest_img));
            return;
        }
        if (MessageContentFormatter.isVideoMessage(type)) {
//            String cover = MediaMessageContentUtil.resolveCoverDisplayUrl(message.getContent(), sentByMe);
//            if (TextUtils.isEmpty(cover)) {
//                cover = MediaMessageContentUtil.resolveDisplayUrl(message.getContent(), sentByMe);
//            }
//            view.bind2(targetName, targetPortrait, cover, 0, 0);

            view.bind(targetName, targetPortrait,
                    view.getContext().getString(com.cboda.baselibrary.R.string.base_digest_video));
            return;
        }
        if (MessageContentFormatter.isMergeTranspondMessage(type)) {
            view.bind(targetName, targetPortrait,
                    view.getContext().getString(com.cboda.baselibrary.R.string.base_digest_chat_record)
                            + MessageContentFormatter.formatDisplayText(message, currentUserId, null));
            return;
        }
        view.bind(targetName, targetPortrait,
                MessageContentFormatter.formatDisplayText(message, currentUserId, null));
    }

    public static void executeForward(@NonNull ForwardMode mode,
                                      @NonNull List<AppMessage> sourceMessages,
                                      @NonNull SessionItem target,
                                      @Nullable String extraText,
                                      @NonNull Runnable onSuccess,
                                      @NonNull Runnable onFail) {
        if (sourceMessages.isEmpty()) {
            onFail.run();
            return;
        }
        if (mode == ForwardMode.MERGE) {
            transpondMerge(sourceMessages, target, () -> sendExtraTextIfNeeded(target, extraText, onSuccess, onFail),
                    onFail);
        } else {
            sendOneByOne(sourceMessages, target,
                    () -> sendExtraTextIfNeeded(target, extraText, onSuccess, onFail), onFail);
        }
    }

    private static void transpondMerge(@NonNull List<AppMessage> sourceMessages,
                                       @NonNull SessionItem target,
                                       @NonNull Runnable onSuccess,
                                       @NonNull Runnable onFail) {
        List<String> msgIds = collectMsgIds(sourceMessages);
        if (msgIds.isEmpty()) {
            onFail.run();
            return;
        }
        TranspondMessageParam param = buildParam(target, msgIds,
                String.valueOf(MessageType.MessageContentType.MERGE_TRANSPOND.getMsgContentType()));
        ImMessageHttpExample.requestTranspondMessage(param, new OnHttpImListener<TranspondMessageContent>() {
            @Override
            public void onHttpImSuccess(@NonNull TranspondMessageContent result) {
                ELogUtilKt.eLogI(ELogUtilKt.APP_IM_TAG,"getMessageId="+result.getMessageId());
                ImMessageManager.getInstance().fetchNewMessageList(
                        result.getMessageId(),
                        batch -> {
                            onSuccess.run();
                            MessageProcessor.getInstance().receiveMsg(batch);
                        });

            }
        }, onFail);
    }

    /**
     * 逐条转发：每条复制内容后走 {@link MessageProcessor#sendMsg}（对齐野火 {@code ForwardViewBroadModel}）。
     */
    private static void sendOneByOne(@NonNull List<AppMessage> sourceMessages,
                                     @NonNull SessionItem target,
                                     @NonNull Runnable onSuccess,
                                     @NonNull Runnable onFail) {
        sendOneByOneAt(sourceMessages, 0, target, onSuccess, onFail);
    }

    private static void sendOneByOneAt(@NonNull List<AppMessage> sourceMessages,
                                       int index,
                                       @NonNull SessionItem target,
                                       @NonNull Runnable onSuccess,
                                       @NonNull Runnable onFail) {
        if (index >= sourceMessages.size()) {
            onSuccess.run();
            return;
        }
        AppMessage source = sourceMessages.get(index);
        if (source == null) {
            sendOneByOneAt(sourceMessages, index + 1, target, onSuccess, onFail);
            return;
        }
        String to = resolveTargetId(target);
        if (TextUtils.isEmpty(to)) {
            onFail.run();
            return;
        }
        AppMessage outbound = MessageBuilder.buildForwardedMessage(
                source, target.getSessionId(), target.getConversation(), to);
        MessageProcessor.getInstance().sendMsg(outbound,
                () -> sendOneByOneAt(sourceMessages, index + 1, target, onSuccess, onFail), true);
    }

    private static void sendExtraTextIfNeeded(@NonNull SessionItem target,
                                              @Nullable String extraText,
                                              @NonNull Runnable onSuccess,
                                              @NonNull Runnable onFail) {
        if (TextUtils.isEmpty(extraText)) {
            onSuccess.run();
            return;
        }
        String to = resolveTargetId(target);
        if (TextUtils.isEmpty(to)) {
            onSuccess.run();
            return;
        }
        AppMessage textMessage = MessageBuilder.buildNewTextMessage(
                to, target.getConversation(), extraText.trim());
        if (!TextUtils.isEmpty(target.getSessionId())) {
            textMessage.setConversationId(target.getSessionId());
        }
        MessageProcessor.getInstance().sendMsg(textMessage, onSuccess, true);
    }

    @NonNull
    private static TranspondMessageParam buildParam(@NonNull SessionItem target,
                                                    @NonNull List<String> msgIds,
                                                    @NonNull String contentType) {
        String userId = IMSClientBootstrap.getInstance().getCurrentUserId();
        TranspondMessageParam param = new TranspondMessageParam();
        param.setMsgIdlist(msgIds);
        param.setConversation(target.getConversation());
        param.setFrom(userId);
        param.setTo(resolveTargetId(target));
        param.setContentType(contentType);
        param.setIdempotentId(UUID.randomUUID().toString());
        param.setClientMsgId(UUID.randomUUID().toString());
        return param;
    }

    @NonNull
    public static List<AppMessage> filterMessagesByIds(@Nullable List<AppMessage> all,
                                                       @NonNull List<String> msgIds) {
        Map<String, AppMessage> byId = new HashMap<>();
        if (all != null) {
            for (AppMessage message : all) {
                if (message != null && !TextUtils.isEmpty(message.getMsgId())) {
                    byId.put(message.getMsgId(), message);
                }
            }
        }
        List<AppMessage> ordered = new ArrayList<>();
        for (String msgId : msgIds) {
            AppMessage message = byId.get(msgId);
            if (message != null) {
                ordered.add(message);
            }
        }
        return ordered;
    }

    @NonNull
    private static List<String> collectMsgIds(@NonNull List<AppMessage> messages) {
        List<String> msgIds = new ArrayList<>();
        for (AppMessage message : messages) {
            if (message != null && !TextUtils.isEmpty(message.getMsgId())) {
                msgIds.add(message.getMsgId());
            }
        }
        return msgIds;
    }

    @NonNull
    public static String resolveSessionDisplayName(@NonNull SessionItem session) {
        if (!TextUtils.isEmpty(session.getSessionName())) {
            return session.getSessionName().trim();
        }
        if (!TextUtils.isEmpty(session.getUsername())) {
            return session.getUsername().trim();
        }
        if (!TextUtils.isEmpty(session.getOppositeUserId())) {
            return session.getOppositeUserId();
        }
        return session.getSessionId() != null ? session.getSessionId() : "";
    }

    /**
     * 解析发送目标 toId：
     * <ul>
     *   <li>群聊 / 我的收藏：用 sessionId（收藏为 {@code saved}+userId）</li>
     *   <li>单聊：优先 oppositeUserId</li>
     * </ul>
     * 收藏会话若误用 oppositeUserId（纯 userId），服务端会报「收藏会话只能发给自己」。
     */
    @Nullable
    public static String resolveTargetId(@NonNull SessionItem session) {
        if (MessageType.isGroupChat(session.getConversation())
                || MessageType.isMyCollect(session.getConversation())
                || ImMessageManager.isMyCollectSession(session.getSessionId())) {
            return session.getSessionId();
        }
        if (!TextUtils.isEmpty(session.getOppositeUserId())) {
            return session.getOppositeUserId();
        }
        return session.getSessionId();
    }
}
