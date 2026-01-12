/*
 * Copyright (c) 2022 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.voip.conference;

import static com.cboda.baselibrary.utils.ELogUtilKt.eLogD;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.cboda.chatui.AppServiceBroadProvider;
import com.cboda.chatui.WfcUIBroadKit;
import com.cboda.chatui.livebus.LiveDataBroadBroadBus;
import com.cboda.chatui.voip.conference.message.ConferenceChangeModeBroadContent;
import com.cboda.chatui.voip.conference.message.ConferenceCommandBroadContent;
import com.cboda.chatui.voip.conference.model.ConferenceBroadInfo;
import com.cboda.chatui.widget.AlertDialogBroadActivity;

import cn.wildfirechat.avenginekit.AVEngineKit;
import cn.wildfirechat.message.Message;
import cn.wildfirechat.model.Conversation;
import cn.wildfirechat.remote.ChatManager;
import cn.wildfirechat.remote.GeneralCallback;
import cn.wildfirechat.remote.OnReceiveMessageListener;

public class ConferenceBroadManager implements OnReceiveMessageListener {
    private static ConferenceBroadManager manager;
    private final Application context;
    private ConferenceBroadInfo currentConferenceInfo;
    private final List<String> applyingUnmuteMembers;
    private boolean isApplyingUnmute;
    private final List<String> handUpMembers;
    private boolean isHandUp;
    private boolean isMuteAll;

    private String localFocusUserId;

    private static final String TAG = "ConferenceBroadManager";

    private ConferenceBroadManager(Application application) {
        this.context = application;
        this.applyingUnmuteMembers = new ArrayList<>();
        this.handUpMembers = new ArrayList<>();
    }

    public static void init(Application application) {
        if (application == null) {
            throw new IllegalArgumentException("application cano be null");
        }
        manager = new ConferenceBroadManager(application);
    }

    public static ConferenceBroadManager getManager() {
        if (manager == null) {
            throw new IllegalStateException("not init");
        }
        return manager;
    }

    public ConferenceBroadInfo getCurrentConferenceInfo() {
        return currentConferenceInfo;
    }


    public void setCurrentConferenceInfo(ConferenceBroadInfo currentConferenceInfo) {
        this.currentConferenceInfo = currentConferenceInfo;
        if (currentConferenceInfo == null) {
            handUpMembers.clear();
            applyingUnmuteMembers.clear();
            isHandUp = false;
            isApplyingUnmute = false;
        }
    }

    public String getLocalFocusUserId() {
        return localFocusUserId;
    }

    public void setLocalFocusUserId(String localFocusUserId) {
        this.localFocusUserId = localFocusUserId;
    }

    public List<String> getApplyingUnmuteMembers() {
        return applyingUnmuteMembers;
    }

    public boolean isApplyingUnmute() {
        return isApplyingUnmute;
    }

    public List<String> getHandUpMembers() {
        return handUpMembers;
    }

    public boolean isHandUp() {
        return isHandUp;
    }

    public boolean isMuteAll() {
        return isMuteAll;
    }

    @Override
    public void onReceiveMessage(List<Message> messages, boolean hasMore) {
        AVEngineKit.CallSession session = AVEngineKit.Instance().getCurrentSession();
        if (session != null && session.getState() != AVEngineKit.CallState.Idle && session.isConference()) {
            for (Message msg : messages) {
                if (msg.content instanceof ConferenceChangeModeBroadContent) {
                    ConferenceChangeModeBroadContent content = (ConferenceChangeModeBroadContent) msg.content;
                    if (session.getCallId().equals(content.getCallId())) {

                    }
                    // TODO
                } else if (msg.content instanceof ConferenceCommandBroadContent) {
                    // TODO
                    ConferenceCommandBroadContent commandContent = (ConferenceCommandBroadContent) msg.content;
                    String senderName;
                    if (session.getCallId().equals(commandContent.getConferenceId())) {
                        switch (commandContent.getCommandType()) {
                            case ConferenceCommandBroadContent.ConferenceCommandType.MUTE_ALL:
                                reloadConferenceInfo();
                                onMuteAll();
                                break;
                            case ConferenceCommandBroadContent.ConferenceCommandType.CANCEL_MUTE_ALL:
                                reloadConferenceInfo();
                                onCancelMuteAll(commandContent.getBoolValue());
                                break;
                            case ConferenceCommandBroadContent.ConferenceCommandType.REQUEST_MUTE:
                                if (commandContent.getTargetUserId().equals(ChatManager.Instance().getUserId())) {
                                    onRequestMute(commandContent.getBoolValue());
                                }
                                break;
                            case ConferenceCommandBroadContent.ConferenceCommandType.REJECT_UNMUTE_REQUEST:
                                Toast.makeText(context, "主持人拒绝了你的发言请求", Toast.LENGTH_SHORT).show();
                                break;
                            case ConferenceCommandBroadContent.ConferenceCommandType.APPLY_UNMUTE:
                                senderName = ChatManager.Instance().getUserDisplayName(msg.sender);
                                Toast.makeText(context, senderName + " 请求发言", Toast.LENGTH_SHORT).show();
                                if (commandContent.getBoolValue()) {
                                    this.applyingUnmuteMembers.remove(msg.sender);
                                } else {
                                    if (!this.applyingUnmuteMembers.contains(msg.sender)) {
                                        this.applyingUnmuteMembers.add(msg.sender);
                                    }
                                }
                                LiveDataBroadBroadBus.setValue("kConferenceCommandStateChanged", new Object());
                                break;
                            case ConferenceCommandBroadContent.ConferenceCommandType.APPROVE_UNMUTE:
                            case ConferenceCommandBroadContent.ConferenceCommandType.APPROVE_ALL_UNMUTE:
                                if (this.isApplyingUnmute) {
                                    this.isApplyingUnmute = false;
                                    if (commandContent.getBoolValue()) {
                                        this.muteAudio(false);
                                        Toast.makeText(context, "主持人同意了你的发言请求", Toast.LENGTH_SHORT).show();
                                    }
                                }
                                break;
                            case ConferenceCommandBroadContent.ConferenceCommandType.HANDUP:
                                if (commandContent.getBoolValue()) {
                                    if (!this.handUpMembers.contains(msg.sender)) {
                                        this.handUpMembers.add(msg.sender);
                                    }
                                } else {
                                    this.handUpMembers.remove(msg.sender);
                                }
                                senderName = ChatManager.Instance().getUserDisplayName(msg.sender);
                                if (commandContent.getBoolValue()) {
                                    Toast.makeText(context, senderName + " 举手", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(context, senderName + " 放下举手", Toast.LENGTH_SHORT).show();
                                }

                                LiveDataBroadBroadBus.setValue("kConferenceCommandStateChanged", new Object());

                                break;
                            case ConferenceCommandBroadContent.ConferenceCommandType.PUT_HAND_DOWN:
                            case ConferenceCommandBroadContent.ConferenceCommandType.PUT_ALL_HAND_DOWN:
                                if (this.isHandUp) {
                                    this.isHandUp = false;
                                    Toast.makeText(context, "主持人放下你的举手", Toast.LENGTH_SHORT).show();
                                }
                                break;
                            case ConferenceCommandBroadContent.ConferenceCommandType.RECORDING:
                                this.reloadConferenceInfo();
                                Toast.makeText(context, commandContent.getBoolValue() ? "主持人开始录制" : "主持人结束录制", Toast.LENGTH_SHORT).show();
                                break;

                            case ConferenceCommandBroadContent.ConferenceCommandType.FOCUS:
                                this.currentConferenceInfo.setFocus(commandContent.getTargetUserId());
                                this.reloadConferenceInfo();
                                Toast.makeText(context, "主持人锁定焦点用户", Toast.LENGTH_SHORT).show();
                                LiveDataBroadBroadBus.setValue("kConferenceCommandStateChanged", commandContent);
                                break;
                            case ConferenceCommandBroadContent.ConferenceCommandType.CANCEL_FOCUS:
                                this.currentConferenceInfo.setFocus(null);
                                this.reloadConferenceInfo();
                                Toast.makeText(context, "主持人取消锁定焦点用户", Toast.LENGTH_SHORT).show();
                                LiveDataBroadBroadBus.setValue("kConferenceCommandStateChanged", commandContent);
                                break;

                            default:
                                break;
                        }

                    }
                }
            }
        }
    }

    public void joinChatRoom() {
        ChatManager.Instance().joinChatRoom(currentConferenceInfo.getConferenceId(), null);
    }

    public void applyUnmute(boolean isCancel) {
        this.isApplyingUnmute = !isCancel;
        this.sendCommandMessage(ConferenceCommandBroadContent.ConferenceCommandType.APPLY_UNMUTE, null, isCancel);
    }

    public void approveUnmute(String userId, boolean isAllow) {
        if (!this.isOwner()) {
            return;
        }

        this.applyingUnmuteMembers.remove(userId);
        this.sendCommandMessage(ConferenceCommandBroadContent.ConferenceCommandType.APPROVE_UNMUTE, userId, isAllow);
        LiveDataBroadBroadBus.setValue("kConferenceCommandStateChanged", new Object());
    }

    public void approveAllMemberUnmute(boolean isAllow) {
        if (!this.isOwner()) {
            return;
        }

        this.applyingUnmuteMembers.clear();
        this.sendCommandMessage(ConferenceCommandBroadContent.ConferenceCommandType.APPROVE_ALL_UNMUTE, null, isAllow);
        LiveDataBroadBroadBus.setValue("kConferenceCommandStateChanged", new Object());
    }

    public void muteAudio(boolean mute) {
        AVEngineKit.CallSession session = AVEngineKit.Instance().getCurrentSession();
        if (session != null && session.getState() == AVEngineKit.CallState.Connected) {
            if (mute) {
                if (!session.isAudience() && session.videoMuted) {
                    boolean result = session.switchAudience(true);
                    if (!result) {
                        eLogD(TAG, "switch to audience fail");
                        return;
                    }
                }
                session.muteAudio(true);
            } else {
                if (session.isAudience() && !session.canSwitchAudience()) {
                    eLogD(TAG, "can not switch to audience");
                    return;
                }
                if (session.isAudience() && isParticipantFull(session)) {
                    Toast.makeText(context, "发言人数已满，无法切换到发言人！", Toast.LENGTH_SHORT).show();
                    return;
                }

                session.muteAudio(false);
                if (session.videoMuted || session.isAudience()) {
                    session.switchAudience(false);
                }
            }
        }
        LiveDataBroadBroadBus.setValue("kConferenceMutedStateChanged", new Object());
    }

    public void muteVideo(boolean mute) {
        AVEngineKit.CallSession session = AVEngineKit.Instance().getCurrentSession();
        if (session != null && session.getState() == AVEngineKit.CallState.Connected) {
            if (mute) {
                if (!session.isAudience() && session.audioMuted) {
                    session.switchAudience(true);
                }
                session.muteVideo(true);
            } else {
                if (session.isAudience() && isParticipantFull(session)) {
                    Toast.makeText(context, "发言人数已满，无法切换到发言人！", Toast.LENGTH_SHORT).show();
                    return;
                }
                session.muteVideo(false);
                if (session.audioMuted || session.isAudience()) {
                    session.switchAudience(false);
                }
            }
        }
    }

    public void muteAudioVideo(boolean mute) {
        AVEngineKit.CallSession session = AVEngineKit.Instance().getCurrentSession();
        if (session == null || session.getState() == AVEngineKit.CallState.Idle) {
            return;
        }
        if (mute) {
            if (!session.isAudience()) {
                session.switchAudience(true);
            }
            session.muteVideo(true);
            session.muteAudio(true);
        } else {
            session.muteVideo(false);
            session.muteAudio(false);
            if (session.isAudience()) {
                session.switchAudience(false);
            }
        }
    }

    public void requestMemberMute(String userId, boolean mute) {
        if (!isOwner()) {
            return;
        }
        this.sendCommandMessage(ConferenceCommandBroadContent.ConferenceCommandType.REQUEST_MUTE, userId, mute);
    }

    public void requestMuteAll(boolean allowMemberUnmute) {
        if (!this.isOwner()) {
            return;
        }
        this.isMuteAll = true;
        currentConferenceInfo.setAudience(true);
        currentConferenceInfo.setAllowTurnOnMic(allowMemberUnmute);

        WfcUIBroadKit.getWfcUIKit().getAppServiceProvider().updateConference(currentConferenceInfo, new GeneralCallback() {
            @Override
            public void onSuccess() {
                sendCommandMessage(ConferenceCommandBroadContent.ConferenceCommandType.MUTE_ALL, null, allowMemberUnmute);
                LiveDataBroadBroadBus.setValue("kConferenceMutedStateChanged", new Object());
            }

            @Override
            public void onFail(int errorCode) {

            }
        });
    }

    public void requestUnmuteAll(boolean unmute) {
        if (!this.isOwner()) {
            return;
        }
        this.isMuteAll = false;
        currentConferenceInfo.setAudience(false);
        currentConferenceInfo.setAllowTurnOnMic(true);

        WfcUIBroadKit.getWfcUIKit().getAppServiceProvider().updateConference(currentConferenceInfo, new GeneralCallback() {
            @Override
            public void onSuccess() {
                sendCommandMessage(ConferenceCommandBroadContent.ConferenceCommandType.CANCEL_MUTE_ALL, null, unmute);
                LiveDataBroadBroadBus.setValue("kConferenceMutedStateChanged", new Object());
            }

            @Override
            public void onFail(int errorCode) {

            }
        });
    }

    public void handUp(boolean isHandUp) {
        this.isHandUp = isHandUp;
        this.sendCommandMessage(ConferenceCommandBroadContent.ConferenceCommandType.HANDUP, null, isHandUp);
        Toast.makeText(context, isHandUp ? "已举手，等待管理员处理" : "已放下举手", Toast.LENGTH_SHORT).show();
    }

    public void putMemberHandDown(String memberId) {
        if (!isOwner()) {
            return;
        }
        this.handUpMembers.remove(memberId);
        this.sendCommandMessage(ConferenceCommandBroadContent.ConferenceCommandType.PUT_HAND_DOWN, memberId, false);
        LiveDataBroadBroadBus.setValue("kConferenceCommandStateChanged", this.handUpMembers);
    }

    public void putAllHandDown() {
        if (!isOwner()) {
            return;
        }
        this.handUpMembers.clear();
        this.sendCommandMessage(ConferenceCommandBroadContent.ConferenceCommandType.PUT_ALL_HAND_DOWN, null, false);
        LiveDataBroadBroadBus.setValue("kConferenceCommandStateChanged", this.handUpMembers);
    }

    public void requestRecord(boolean record) {
        if (!isOwner()) {
            return;
        }

        WfcUIBroadKit.getWfcUIKit().getAppServiceProvider().recordConference(currentConferenceInfo.getConferenceId(), record, new GeneralCallback() {
            @Override
            public void onSuccess() {
                currentConferenceInfo.setRecording(record);
                Toast.makeText(context, record ? "开始录制" : "结束录制", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFail(int errorCode) {

            }
        });
    }

    public void destroyConference(String conferenceId, GeneralCallback callback) {
        if (!isOwner()) {
            return;
        }
        WfcUIBroadKit.getWfcUIKit().getAppServiceProvider().destroyConference(conferenceId, new GeneralCallback() {
            @Override
            public void onSuccess() {
                if (callback != null) {
                    callback.onSuccess();
                }
            }

            @Override
            public void onFail(int i) {
                if (callback != null) {
                    callback.onFail(i);
                }
            }
        });
    }

    public void requestFocus(String userId, GeneralCallback callback) {
        if (!isOwner()) {
            return;
        }
        WfcUIBroadKit.getWfcUIKit().getAppServiceProvider().setConferenceFocusUserId(currentConferenceInfo.getConferenceId(), userId, new GeneralCallback() {
            @Override
            public void onSuccess() {
                currentConferenceInfo.setFocus(userId);
                sendCommandMessage(ConferenceCommandBroadContent.ConferenceCommandType.FOCUS, userId, false);
                ConferenceCommandBroadContent content = new ConferenceCommandBroadContent(currentConferenceInfo.getConferenceId(), ConferenceCommandBroadContent.ConferenceCommandType.FOCUS);
                content.setTargetUserId(userId);
                LiveDataBroadBroadBus.setValue("kConferenceCommandStateChanged", content);
                if (callback != null) {
                    callback.onSuccess();
                }
            }

            @Override
            public void onFail(int i) {
                if (callback != null) {
                    callback.onFail(i);
                }
            }
        });
    }

    public void requestCancelFocus(GeneralCallback callback) {
        if (!isOwner()) {
            return;
        }
        WfcUIBroadKit.getWfcUIKit().getAppServiceProvider().setConferenceFocusUserId(currentConferenceInfo.getConferenceId(), null, new GeneralCallback() {
            @Override
            public void onSuccess() {
                currentConferenceInfo.setFocus(null);
                sendCommandMessage(ConferenceCommandBroadContent.ConferenceCommandType.CANCEL_FOCUS, null, false);
                ConferenceCommandBroadContent content = new ConferenceCommandBroadContent(currentConferenceInfo.getConferenceId(), ConferenceCommandBroadContent.ConferenceCommandType.FOCUS);
                content.setTargetUserId(null);
                LiveDataBroadBroadBus.setValue("kConferenceCommandStateChanged", content);
                if (callback != null) {
                    callback.onSuccess();
                }
            }

            @Override
            public void onFail(int i) {
                if (callback != null) {
                    callback.onFail(i);
                }
            }
        });
    }

    public void addHistory(ConferenceBroadInfo conferenceInfo, long durationMS) {
        Gson gson = new Gson();
        conferenceInfo.setEndTime(conferenceInfo.getStartTime() * 1000 + durationMS);
        SharedPreferences sp = context.getSharedPreferences("conf_history", Context.MODE_PRIVATE);
        String historyConfListStr = sp.getString("historyConfList", null);
        ArrayList<ConferenceBroadInfo> historyConfList = new ArrayList<>();
        if (historyConfListStr != null) {
            historyConfList = gson.fromJson(historyConfListStr, new TypeToken<List<ConferenceBroadInfo>>() {
            }.getType());
        }
        historyConfList.add(conferenceInfo);
        if (historyConfList.size() > 50) {
            historyConfList.remove(0);
        }
        historyConfListStr = gson.toJson(historyConfList);
        sp.edit().putString("historyConfList", historyConfListStr).commit();
    }

    public List<ConferenceBroadInfo> getHistoryConference() {
        Gson gson = new Gson();
        SharedPreferences sp = context.getSharedPreferences("conf_history", Context.MODE_PRIVATE);
        String historyConfListStr = sp.getString("historyConfList", null);
        ArrayList<ConferenceBroadInfo> historyConfList = new ArrayList<>();
        if (historyConfListStr != null) {
            historyConfList = gson.fromJson(historyConfListStr, new TypeToken<List<ConferenceBroadInfo>>() {
            }.getType());
        }
        Collections.reverse(historyConfList);
        return historyConfList;
    }

    public boolean isOwner() {
        return currentConferenceInfo.getOwner().equals(ChatManager.Instance().getUserId());
    }

    private void onRequestMute(boolean mute) {
        if (!mute) {
            AlertDialogBroadActivity.showAlterDialog(context, "主持人邀请你发言", false, "拒绝", "接受",
                    () -> Toast.makeText(context, "你拒绝了发言邀请", Toast.LENGTH_SHORT).show(),
                    () -> {
                        Toast.makeText(context, "你接受了发言邀请", Toast.LENGTH_SHORT).show();
                        muteAudioVideo(false);
                    });
        } else {
            Toast.makeText(context, "主持人关闭了你的发言", Toast.LENGTH_SHORT).show();
            muteAudioVideo(true);
        }
    }

    private void onCancelMuteAll(boolean requestUnmute) {
        if (requestUnmute) {
            AlertDialogBroadActivity.showAlterDialog(context, "主持人关闭了全员静音，是否要打开麦克风", false, "忽略", "打开",
                    () -> {
                    },
                    () -> {
                        muteAudio(false);
                    });
        }
        Toast.makeText(context, "主持人关闭了全员静音", Toast.LENGTH_SHORT).show();
    }


    private void onMuteAll() {
        reloadConferenceInfo();
        AVEngineKit.CallSession session = AVEngineKit.Instance().getCurrentSession();
        if (!session.isAudience()) {
            session.switchAudience(true);
            session.muteAudio(true);
            session.muteVideo(true);
        }
        Toast.makeText(context, "主持人开启了全员静音", Toast.LENGTH_SHORT).show();
    }

    private void reloadConferenceInfo() {
        WfcUIBroadKit.getWfcUIKit().getAppServiceProvider().queryConferenceInfo(this.currentConferenceInfo.getConferenceId(), this.currentConferenceInfo.getPassword(), new AppServiceBroadProvider.QueryConferenceInfoCallback() {
            @Override
            public void onSuccess(ConferenceBroadInfo info) {
                currentConferenceInfo = info;
            }

            @Override
            public void onFail(int code, String msg) {

            }
        });

    }

    private void sendCommandMessage(int commandType, String targetUser, boolean value) {
        ConferenceCommandBroadContent content = new ConferenceCommandBroadContent(this.currentConferenceInfo.getConferenceId(), commandType);
        content.setTargetUserId(targetUser);
        content.setBoolValue(value);
        Conversation conversation = new Conversation(Conversation.ConversationType.ChatRoom, this.currentConferenceInfo.getConferenceId(), 0);
        ChatManager.Instance().sendMessage(conversation, content, null, 0, null);

    }

    private boolean isParticipantFull(AVEngineKit.CallSession session) {
        if (currentConferenceInfo.getMaxParticipants() > 0) {
            int participantCount = 0;
            List<AVEngineKit.ParticipantProfile> profiles = session.getParticipantProfiles();
            for (AVEngineKit.ParticipantProfile p : profiles) {
                if (!p.isAudience()) {
                    participantCount++;
                }
            }
            return participantCount >= currentConferenceInfo.getMaxParticipants();
        }
        return false;
    }
}
