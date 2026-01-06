/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.viewmodel;

import static com.cboda.baselibrary.utils.GsonUtilsKt.toJson2;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.cboda.baselibrary.bean.CommonDataListBroadBean;
import com.cboda.baselibrary.bean.MsgBroadExtra;
import com.cboda.baselibrary.utils.CompressionBroadUtil;
import com.cboda.baselibrary.utils.SharePrefBroadBroadUtil;
import com.cboda.select.picture.utils.ToastBroadUtils;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cboda.chatui.WfcUIBroadKit;
import com.cboda.chatui.audio.AudioPlayBroadManager;
import com.cboda.chatui.audio.IAudioPlayBroadListener;
import com.cboda.chatui.bridge.YeHuoCommonBroadBridge;
import com.cboda.chatui.bridge.YeHuoBroadEvent;
import com.cboda.chatui.common.OperateBroadResult;
import com.cboda.chatui.conversation.message.model.UiBroadMessage;
import com.cboda.chatui.conversation.message.viewholder.AudioMessageContentViewBroadHolder;
import com.cboda.chatui.third.location.data.LocationBroadData;
import com.cboda.chatui.third.utils.UIBroadUtils;
import com.cboda.chatui.utils.DownloadBroadManager;
import com.cboda.chatui.utils.E;
import com.cboda.chatui.utils.FileBroadUtils;
import cn.wildfirechat.message.FileMessageContent;
import cn.wildfirechat.message.ImageMessageContent;
import cn.wildfirechat.message.LocationMessageContent;
import cn.wildfirechat.message.MediaMessageContent;
import cn.wildfirechat.message.Message;
import cn.wildfirechat.message.MessageContent;
import cn.wildfirechat.message.SoundMessageContent;
import cn.wildfirechat.message.StickerMessageContent;
import cn.wildfirechat.message.TextMessageContent;
import cn.wildfirechat.message.TypingMessageContent;
import cn.wildfirechat.message.UnknownMessageContent;
import cn.wildfirechat.message.VideoMessageContent;
import cn.wildfirechat.message.core.MessageDirection;
import cn.wildfirechat.message.core.MessageStatus;
import cn.wildfirechat.model.Conversation;
import cn.wildfirechat.model.ReadEntry;
import cn.wildfirechat.remote.ChatManager;
import cn.wildfirechat.remote.GeneralCallback;
import cn.wildfirechat.remote.GeneralCallbackBytes;
import cn.wildfirechat.remote.OnClearMessageListener;
import cn.wildfirechat.remote.OnDeleteMessageListener;
import cn.wildfirechat.remote.OnMessageDeliverListener;
import cn.wildfirechat.remote.OnMessageReadListener;
import cn.wildfirechat.remote.OnMessageUpdateListener;
import cn.wildfirechat.remote.OnRecallMessageListener;
import cn.wildfirechat.remote.OnReceiveMessageListener;
import cn.wildfirechat.remote.OnSendMessageListener;
import cn.wildfirechat.remote.SecretMessageBurnStateListener;
import cn.wildfirechat.remote.SendMessageCallback;

public class MessageViewBroadModel extends ViewModel implements OnReceiveMessageListener,
        OnSendMessageListener,
        OnDeleteMessageListener,
        OnRecallMessageListener,
        OnMessageUpdateListener,
        OnMessageDeliverListener,
        OnMessageReadListener,
        OnClearMessageListener,
        SecretMessageBurnStateListener {
    private MutableLiveData<UiBroadMessage> messageLiveData;
    private MutableLiveData<UiBroadMessage> messageUpdateLiveData;
    private MutableLiveData<UiBroadMessage> messageRemovedLiveData;
    private MutableLiveData<Map<String, String>> mediaUploadedLiveData;
    private MutableLiveData<Object> clearMessageLiveData;
    private MutableLiveData<Map<String, Long>> messageDeliverLiveData;
    private MutableLiveData<List<ReadEntry>> messageReadLiveData;
    private MutableLiveData<Pair<String, Long>> messageStartBurnLiveData;
    private MutableLiveData<List<Long>> messageBurnedLiveData;

    private Message playingAudioMessage;

    public MessageViewBroadModel() {
        ChatManager.Instance().addOnReceiveMessageListener(this);
        ChatManager.Instance().addRecallMessageListener(this);
        ChatManager.Instance().addSendMessageListener(this);
        ChatManager.Instance().addOnMessageUpdateListener(this);
        ChatManager.Instance().addClearMessageListener(this);
        ChatManager.Instance().addMessageDeliverListener(this);
        ChatManager.Instance().addMessageReadListener(this);
        ChatManager.Instance().addSecretMessageBurnStateListener(this);
        ChatManager.Instance().addDeleteMessageListener(this);
    }

    @Override
    protected void onCleared() {
        ChatManager.Instance().removeOnReceiveMessageListener(this);
        ChatManager.Instance().removeRecallMessageListener(this);
        ChatManager.Instance().removeSendMessageListener(this);
        ChatManager.Instance().removeOnMessageUpdateListener(this);
        ChatManager.Instance().removeClearMessageListener(this);
        ChatManager.Instance().removeMessageDeliverListener(this);
        ChatManager.Instance().removeMessageReadListener(this);
        ChatManager.Instance().removeSecretMessageBurnStateListener(this);
        ChatManager.Instance().removeDeleteMessageListener(this);
    }

    @Override
    public void onReceiveMessage(List<Message> messages, boolean hasMore) {
        if (messageLiveData != null && messages != null) {
            for (Message msg : messages) {
                postNewMessage(new UiBroadMessage(msg));
                if (msg.content instanceof UnknownMessageContent) {
                    UnknownMessageContent tongzhiContent = (UnknownMessageContent) msg.content;
                    if (tongzhiContent.getOrignalPayload().type == 1001) {
                        Log.e("野火", "自定义通知--> " + tongzhiContent.getOrignalPayload().content + "  " + tongzhiContent.getOrignalPayload().extra);
                        EventBus.getDefault().post(new YeHuoBroadEvent("xiugaigongaole", tongzhiContent.getOrignalPayload().content, tongzhiContent.getOrignalPayload().extra));
                    } else if (tongzhiContent.getOrignalPayload().type == 1004) {
                        EventBus.getDefault().post(new YeHuoBroadEvent("xiugaifayanshijian", tongzhiContent.getOrignalPayload().content, ""));
                    }
//                    else if (tongzhiContent.getOrignalPayload().type == 1005) {
//                        EventBus.getDefault().post(new YeHuoBroadEvent("beifeng", tongzhiContent.getOrignalPayload().content, ""));
//                    }
                }
            }
        }
    }

    public MutableLiveData<UiBroadMessage> messageLiveData() {
        if (messageLiveData == null) {
            messageLiveData = new MutableLiveData<>();
        }
        return messageLiveData;
    }

    public MutableLiveData<UiBroadMessage> messageUpdateLiveData() {
        if (messageUpdateLiveData == null) {
            messageUpdateLiveData = new MutableLiveData<>();
        }
        return messageUpdateLiveData;
    }

    public MutableLiveData<UiBroadMessage> messageRemovedLiveData() {
        if (messageRemovedLiveData == null) {
            messageRemovedLiveData = new MutableLiveData<>();
        }
        return messageRemovedLiveData;
    }

    public MutableLiveData<Map<String, String>> mediaUpdateLiveData() {
        if (mediaUploadedLiveData == null) {
            mediaUploadedLiveData = new MutableLiveData<>();
        }
        return mediaUploadedLiveData;
    }

    public MutableLiveData<Object> clearMessageLiveData() {
        if (clearMessageLiveData == null) {
            clearMessageLiveData = new MutableLiveData<>();
        }
        return clearMessageLiveData;
    }

    public MutableLiveData<Map<String, Long>> messageDeliverLiveData() {
        if (messageDeliverLiveData == null) {
            messageDeliverLiveData = new MutableLiveData<>();
        }
        return messageDeliverLiveData;
    }

    public MutableLiveData<List<ReadEntry>> messageReadLiveData() {
        if (messageReadLiveData == null) {
            messageReadLiveData = new MutableLiveData<>();
        }
        return messageReadLiveData;
    }

    public MutableLiveData<Pair<String, Long>> messageStartBurnLiveData() {
        if (messageStartBurnLiveData == null) {
            messageStartBurnLiveData = new MutableLiveData<>();
        }
        return messageStartBurnLiveData;
    }

    public MutableLiveData<List<Long>> messageBurnedLiveData() {
        if (messageBurnedLiveData == null) {
            messageBurnedLiveData = new MutableLiveData<>();
        }
        return messageBurnedLiveData;
    }

    @Override
    public void onRecallMessage(Message message) {
        if (message != null) {
            UiBroadMessage uiMessage = new UiBroadMessage(message);
            if (playingAudioMessage != null && playingAudioMessage.messageUid == message.messageUid) {
                stopPlayAudio();
            }
            postMessageUpdate(uiMessage);
        }
    }

    // TODO 可优化成moveTo
    public void resendMessage(Message message) {
        deleteMessage(message);
        sendMessage(message.conversation, message.content);
    }

    public void recallMessage(Message message) {
        ChatManager.Instance().recallMessage(message, new GeneralCallback() {
            @Override
            public void onSuccess() {
                Message msg = message;
                if (message.messageId > 0) {
                    msg = ChatManager.Instance().getMessage(message.messageId);
                }
                postMessageUpdate(new UiBroadMessage(msg));
            }

            @Override
            public void onFail(int errorCode) {
                Log.e(MessageViewBroadModel.class.getSimpleName(), "撤回失败: " + errorCode);
                // TODO 撤回失败
            }
        });
    }

    @Override
    public void onDeleteMessage(Message message) {
        if (messageRemovedLiveData != null) {
            messageRemovedLiveData.setValue(new UiBroadMessage(message));
        }
    }

    /**
     * 删除消息
     *
     * @param message
     */
    public void deleteMessage(Message message) {
        if (messageRemovedLiveData != null) {
            messageRemovedLiveData.setValue(new UiBroadMessage(message));
        }
        ChatManager.Instance().deleteMessage(message);
    }

    public void deleteRemoteMessage(Message message) {
        ChatManager.Instance().deleteRemoteMessage(message.messageUid, new GeneralCallback() {
            @Override
            public void onSuccess() {
                if (messageRemovedLiveData != null) {
                    messageRemovedLiveData.setValue(new UiBroadMessage(message));
                }
            }

            @Override
            public void onFail(int errorCode) {
                Log.e("Message", "delete remote message error: " + errorCode);
            }
        });
    }

    public void playAudioMessage(UiBroadMessage message) {
        if (message == null || !(message.message.content instanceof SoundMessageContent)) {
            return;
        }

        if (playingAudioMessage != null && playingAudioMessage.equals(message.message)) {
            AudioPlayBroadManager.getInstance().stopPlay();
            message.continuousPlayAudio = false;
            playingAudioMessage = null;
            return;
        }

        playingAudioMessage = message.message;
        if (message.message.direction == MessageDirection.Receive && message.message.status != MessageStatus.Played) {
            message.message.status = MessageStatus.Played;
            ChatManager.Instance().setMediaMessagePlayed(message.message.messageId);
            message.continuousPlayAudio = true;
        }

        File file = DownloadBroadManager.mediaMessageContentFile(message.message);

        if (file == null) {
            return;
        }
        if (file.exists()) {
            playAudio(message, file);
        } else {
            Log.e("ConversationViewBroadHolder", "audio not exist");
        }
    }

    public void stopPlayAudio() {
        AudioPlayBroadManager.getInstance().stopPlay();
    }

    public void sendMessage(Conversation conversation, List<String> toUsers, MessageContent content) {

    }

    public Context context = ChatManager.Instance().getApplicationContext();
    public boolean shifouLanjie = false;
    public int jiangeshijan = 0;
    public boolean shifoumianci = false;
    public String mgc;
    public String openMgc;
    private CommonDataListBroadBean.CommonBean userGroupData;

    public CommonDataListBroadBean.CommonBean getUserGroupData() {
        return userGroupData;
    }

    public void getMsgDetailUserData(String groupId) {
        E.INSTANCE.getDetailUserData(groupId, commonBean -> {
            userGroupData = commonBean;
            return null;
        });
    }


    public void sendMessage(Conversation conversation, MessageContent content) {
        if (conversation.type == Conversation.ConversationType.Group && null == userGroupData) {
            E.INSTANCE.getDetailUserData(conversation.target, commonBean -> {
                userGroupData = commonBean;
                sendMessageAfter(conversation, content);
                return null;
            });
        } else {
            sendMessageAfter(conversation, content);
        }
    }

    public void sendMessageAfter(Conversation conversation, MessageContent content) {
        if (conversation.type == Conversation.ConversationType.Group && null != userGroupData) {
            content.extra = toJson2(new MsgBroadExtra(userGroupData.getGrade(), userGroupData.getJoinTime()));
        }
        //野火发送消息
        if (content instanceof TypingMessageContent) {
            Message msg = new Message();
            msg.conversation = conversation;
            msg.content = content;
            sendMessage(msg);
        } else {
            if (shifouLanjie) {
                long shiji = System.currentTimeMillis() - ChatManager.Instance().shangcishijan;
                long shijicha = jiangeshijan * 1000L;
                Log.e("野火", "发言时间差--> " + shiji + "  " + shijicha);
                if (shiji >= shijicha) {
                    faxiaoxi(conversation, content);
                } else {

                    ToastBroadUtils.showToast(context, "发言过于频繁");
                }

            } else {
                faxiaoxi(conversation, content);
            }
        }
    }

    private void faxiaoxi(Conversation conversation, MessageContent content) {
        String ptMgc = SharePrefBroadBroadUtil.getString(context, "pingtaimgc", "");
        List<String> items = new ArrayList<>();
        if (ptMgc.contains("|")) {
            items.addAll(Arrays.asList(ptMgc.split("\\|")));
        }
        if (content instanceof TextMessageContent) {
            for (int i = 0; i < items.size(); i++) {
                if (items.get(i).equals(((TextMessageContent) content).getContent())) {
                    ToastBroadUtils.showToast(context, "发言包含敏感词");
                    return;
                }
            }
        }
        Log.e("野火", "发消息--> " + shifoumianci + "  " + mgc + " " + openMgc + "  ");
        if (shifoumianci && content instanceof TextMessageContent) {
            String content1 = ((TextMessageContent) content).getContent();
            if (content1.contains("|")) {
                items.addAll(Arrays.asList(content1.split("\\|")));
            }

            boolean isMgc = false;
            for (int i = 0; i < items.size(); i++) {
                if (items.get(i).equals(((TextMessageContent) content).getContent())) {
                    isMgc = true;
                    ToastBroadUtils.showToast(context, "发言包含敏感词");
                    if ("1".equals(openMgc)) {
                        YeHuoCommonBroadBridge.getInstance().setYeHuoQiaoJie(context, "mingancichufajianyan", conversation.target, ChatManager.Instance().getUserId(), null);
                    }
                }
            }
            if (!isMgc) {
                Message msg = new Message();
                msg.conversation = conversation;
                msg.content = content;
                sendMessage(msg);
            }
        } else {
            Message msg = new Message();
            msg.conversation = conversation;
            msg.content = content;
            sendMessage(msg);
        }
    }

    public MutableLiveData<OperateBroadResult<Void>> sendMessageEx(Message message) {
        MutableLiveData<OperateBroadResult<Void>> result = new MutableLiveData<>();
        ChatManager.Instance().sendMessage(message, 0, new SendMessageCallback() {
            @Override
            public void onSuccess(long messageUid, long timestamp) {
                result.setValue(new OperateBroadResult<>(0));
            }

            @Override
            public void onFail(int errorCode) {
                result.setValue(new OperateBroadResult<>(-1));
            }

            @Override
            public void onPrepare(long messageId, long savedTime) {

            }
        });
        return result;
    }

    public void sendMessage(Message message) {
        // the call back would be called on the ui thread
        message.sender = ChatManager.Instance().getUserId();
        ChatManager.Instance().sendMessage(message, null);
    }

    public void sendTextMsg(Conversation conversation, TextMessageContent txtContent) {
        sendMessage(conversation, txtContent);
        ChatManager.Instance().setConversationDraft(conversation, null);
    }

    public void saveDraft(Conversation conversation, String draftString) {
        ChatManager.Instance().setConversationDraft(conversation, draftString);
    }

    public void setConversationSilent(Conversation conversation, boolean silent) {
        ChatManager.Instance().setConversationSilent(conversation, silent);
    }

    public void sendImgMsg(Conversation conversation, Uri imageFileThumbUri, Uri imageFileSourceUri) {
        ImageMessageContent imgContent = new ImageMessageContent(imageFileSourceUri.getEncodedPath());
        String thumbParam = ChatManager.Instance().getImageThumbPara();
        if (!TextUtils.isEmpty(thumbParam)) {
            imgContent.setThumbPara(ChatManager.Instance().getImageThumbPara());
        }
        sendMessage(conversation, imgContent);
    }

    public void sendImgMsg(Conversation conversation, File imageFileThumb, File imageFileSource) {
        // Uri.fromFile()遇到中文檔名會轉 ASCII，這個 ASCII 的 path 將導致後面 ChatManager.sendMessage()
        // 在 new File()時找不到 File 而 return
        Uri imageFileThumbUri = Uri.parse(Uri.decode(imageFileThumb.getAbsolutePath()));
//        Uri imageFileThumbUri = Uri.fromFile(imageFileThumb);
        CompressionBroadUtil.imgCompression(context, imageFileSource.getAbsolutePath(), 100, s -> {
            Uri imageFileSourceUri = Uri.parse(Uri.decode(s));
            sendImgMsg(conversation, imageFileThumbUri, imageFileSourceUri);
            return null;
        });
//        Uri imageFileSourceUri = Uri.parse(Uri.decode(imageFileSource.getAbsolutePath()));
//        Uri imageFileSourceUri = Uri.fromFile(imageFileSource);
    }

    public void sendVideoMsg(Conversation conversation, File file) {
        CompressionBroadUtil.sendVideoCompress(file.getPath(), (s, s2) -> {
            VideoMessageContent videoMessageContent = new VideoMessageContent(s);
            sendMessage(conversation, videoMessageContent);
            return null;
        });
//        VideoMessageContent videoMessageContent = new VideoMessageContent(file.getPath());
//        sendMessage(conversation, videoMessageContent);
    }

    public void sendStickerMsg(Conversation conversation, String localPath, String remoteUrl) {
        StickerMessageContent stickerMessageContent = new StickerMessageContent(localPath);
        stickerMessageContent.remoteUrl = remoteUrl;
        sendMessage(conversation, stickerMessageContent);
    }

    public void sendFileMsg(Conversation conversation, File file) {
        FileMessageContent fileMessageContent = new FileMessageContent(file.getPath());
        sendMessage(conversation, fileMessageContent);
    }

    public void sendLocationMessage(Conversation conversation, LocationBroadData locationData) {
        LocationMessageContent locCont = new LocationMessageContent();
        locCont.setTitle(locationData.getPoi());
        locCont.getLocation().setLatitude(locationData.getLat());
        locCont.getLocation().setLongitude(locationData.getLng());
        locCont.setThumbnail(locationData.getThumbnail());

        sendMessage(conversation, locCont);
    }

    public void sendAudioFile(Conversation conversation, Uri audioPath, int duration) {
        if (audioPath != null) {
            File file = new File(audioPath.getPath());
            if (!file.exists() || file.length() == 0L) {
                Log.e("ConversationViewBroadModel", "send audio file fail");
                return;
            }
            SoundMessageContent soundContent = new SoundMessageContent(file.getAbsolutePath());
            soundContent.setDuration(duration);
            sendMessage(conversation, soundContent);
        }
    }

    private void playAudio(UiBroadMessage message, File file) {
        Uri uri = Uri.fromFile(file);
        IAudioPlayBroadListener audioPlayListener = new IAudioPlayBroadListener() {
            @Override
            public void onStart(Uri var1) {
                if (uri.equals(var1)) {
                    message.isPlaying = true;
                    postMessageUpdate(message);
                }
            }

            @Override
            public void onStop(Uri var1) {
                if (uri.equals(var1)) {
                    message.isPlaying = false;
                    playingAudioMessage = null;
                    postMessageUpdate(message);
                }
            }

            @Override
            public void onComplete(Uri var1) {
                if (uri.equals(var1)) {
                    message.isPlaying = false;
                    message.audioPlayCompleted = true;
                    playingAudioMessage = null;
                    postMessageUpdate(message);
                }
            }
        };

        if (message.message.conversation.type == Conversation.ConversationType.SecretChat) {
            byte[] encryptedBytes = FileBroadUtils.readBytesFromFile(file.getAbsolutePath());
            ChatManager.Instance().decodeSecretDataAsync(message.message.conversation.target, encryptedBytes, new GeneralCallbackBytes() {
                @Override
                public void onSuccess(byte[] data) {
                    AudioPlayBroadManager.getInstance().startPlay(WfcUIBroadKit.getWfcUIKit().getApplication(), uri, data, audioPlayListener);
                }

                @Override
                public void onFail(int errorCode) {
                    message.isDownloading = false;
                    Log.d("MessageVideModel", "decodeSecretDataAsync error " + errorCode);
                }
            });

        } else {
            AudioPlayBroadManager.getInstance().startPlay(WfcUIBroadKit.getWfcUIKit().getApplication(), uri, audioPlayListener);
        }
    }

    public void downloadMedia(UiBroadMessage message, File targetFile) {
        MessageContent content = message.message.content;
        if (!(content instanceof MediaMessageContent)) {
            return;
        }

        if (message.isDownloading) {
            return;
        }
        message.isDownloading = true;
        postMessageUpdate(message);

        DownloadBroadManager.download(((MediaMessageContent) content).remoteUrl, targetFile.getParent(), targetFile.getName(), new DownloadBroadManager.OnDownloadListener() {
            @Override
            public void onSuccess(File file) {
                message.isDownloading = false;
                message.progress = 100;
                postMessageUpdate(message);
            }

            @Override
            public void onProgress(int percent) {
                message.progress = percent;
                postMessageUpdate(message);
            }

            @Override
            public void onFail() {
                message.isDownloading = false;
                message.progress = 0;
                postMessageUpdate(message);
                Log.e(AudioMessageContentViewBroadHolder.class.getSimpleName(), "download failed: " + message.message.messageId);
            }
        });
    }

    private void postMessageUpdate(UiBroadMessage message) {
        if (message == null || message.message == null) {
            return;
        }
        if (messageUpdateLiveData != null) {
            UIBroadUtils.postTaskSafely(() -> messageUpdateLiveData.setValue(message));
        }
    }

    private void postNewMessage(UiBroadMessage message) {
        if (message == null || message.message == null) {
            return;
        }
        if (messageLiveData != null) {
            UIBroadUtils.postTaskSafely(() -> messageLiveData.setValue(message));
        }
    }

    @Override
    public void onSendSuccess(Message message) {
        postMessageUpdate(new UiBroadMessage(message));
    }

    @Override
    public void onSendFail(Message message, int errorCode) {
        postMessageUpdate(new UiBroadMessage(message));
        if (errorCode == 248) {
            EventBus.getDefault().post(new YeHuoBroadEvent("bushihaoyoufaxiaoxi", "", ""));
        }
    }

    @Override
    public void onSendPrepare(Message message, long savedTime) {
        postNewMessage(new UiBroadMessage(message));
    }

    @Override
    public void onProgress(Message message, long uploaded, long total) {
        UiBroadMessage uiMessage = new UiBroadMessage(message);
        uiMessage.progress = (int) (uploaded * 100 / total);
        postMessageUpdate(uiMessage);
    }

    @Override
    public void onMediaUpload(Message message, String remoteUrl) {
        String key;
        MediaMessageContent content = (MediaMessageContent) message.content;
        if (message.conversation.type == Conversation.ConversationType.SecretChat) {
            key = message.conversation.target + "_" + content.localPath;
        } else {
            key = content.localPath;
        }
        SharedPreferences sharedPreferences = ChatManager.Instance().getApplicationContext().getSharedPreferences("sticker", Context.MODE_PRIVATE);
        sharedPreferences.edit()
                .putString(key, content.remoteUrl)
                .apply();

        if (mediaUploadedLiveData != null) {
            Map<String, String> map = new HashMap<>();
            map.put(((MediaMessageContent) message.content).localPath, remoteUrl);
            UIBroadUtils.postTaskSafely(() -> mediaUploadedLiveData.setValue(map));
        }
    }

    @Override
    public void onMessageUpdate(Message message) {
        postMessageUpdate(new UiBroadMessage(message));
    }

    @Override
    public void onClearMessage(Conversation conversation) {
        if (clearMessageLiveData != null) {
            clearMessageLiveData.postValue(new Object());
        }
    }

    @Override
    public void onMessageDelivered(Map<String, Long> deliveries) {
        if (messageDeliverLiveData != null) {
            messageDeliverLiveData.postValue(deliveries);
        }
    }

    @Override
    public void onMessageRead(List<ReadEntry> readEntries) {
        if (messageReadLiveData != null) {
            messageReadLiveData.postValue(readEntries);
        }
    }

    @Override
    public void onSecretMessageStartBurning(String targetId, long playedMsgId) {
        if (messageStartBurnLiveData != null) {
            messageStartBurnLiveData.postValue(new Pair<>(targetId, playedMsgId));
        }
    }

    @Override
    public void onSecretMessageBurned(List<Long> messageIds) {
        if (messageBurnedLiveData != null) {
            messageBurnedLiveData.postValue(messageIds);
        }
    }
}
