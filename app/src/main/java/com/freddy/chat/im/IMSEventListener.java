package com.freddy.chat.im;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.Log;

import com.cboda.intelligent.app.MyBroadApp;
import com.freddy.im.crypto.ImCryptoKeys;
import com.freddy.im.listener.OnEventListener;
import com.freddy.im.message.ImGroupChangeManager;
import com.freddy.im.message.ImGroupMemberChangeManager;
import com.freddy.im.message.ImMessageManager;
import com.freddy.im.signal.ImSignalProtos;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * SeekWeb4 + imsignal 回调到应用层 {@link MessageProcessor}。
 */
public class IMSEventListener implements OnEventListener {

    private static final String TAG = "IMSEventListener";

    private static final String RSA_PUBLIC_ASSET = "public_key.pem";

    private final String userId;
    private final String token;

    private volatile String rsaPublicKeyPem;

    public IMSEventListener(String userId, String token) {
        this.userId = userId;
        this.token = token;
    }

    @Override
    public void onSignalResponse(ImSignalProtos.SignalResponse res) {
        if (!res.hasHeader()) {
            return;
        }
        int cmd = res.getHeader().getCmd();
        if (cmd == ImSignalProtos.Cmd.BIND_USER_RES_VALUE && res.getHeader().getCode() == 0) {
            //todo 第五步，连接成功刷新会话列表
            MessageProcessor.getInstance().notifyConversationList();

        }

    }

    @Override
    public void onSignalPush(ImSignalProtos.SignalPush push) {
        if (!push.hasHeader()) {
            return;
        }
        System.out.println("收到服务端的onSignalPush信令响应cmd: " + push.getHeader().getCmd()  );

        if (push.getHeader().getCmd() == ImSignalProtos.Cmd.NEW_MSGSIG_VALUE && push.hasBody()) {
            try {
                ImSignalProtos.NewMsgSignal nm = ImSignalProtos.NewMsgSignal.parseFrom(push.getBody().toByteArray());
                // messageId 形如 uid1!uid2-1046：uid1!uid2 为会话锚点，1046 为序列号；同锚点只保留序列最大的 MsgSig
                Map<String, ImSignalProtos.NewMsgSignal.MsgSig> bestByConversationAnchor = new LinkedHashMap<>();
                for (int i = 0; i < nm.getSignalsCount(); i++) {
                    ImSignalProtos.NewMsgSignal.MsgSig sig = nm.getSignals(i);
                    String fullMessageId = sig.getMessageId();
                    String anchor = ImMessageManager.extractConversationAnchor(fullMessageId);
                    if (anchor == null) {
                        Log.w(TAG, "跳过无法解析锚点的 messageId: " + fullMessageId);
                        continue;
                    }
                    long seq = ImMessageManager.extractTrailingSequence(fullMessageId);
                    if (seq == ImMessageManager.INVALID_SEQUENCE) {
                        Log.w(TAG, "跳过无有效序列号的 messageId: " + fullMessageId);
                        continue;
                    }
                    ImSignalProtos.NewMsgSignal.MsgSig existing = bestByConversationAnchor.get(anchor);
                    if (existing == null) {
                        bestByConversationAnchor.put(anchor, sig);
                        continue;
                    }
                    long existingSeq = ImMessageManager.extractTrailingSequence(existing.getMessageId());
                    if (seq > existingSeq) {
                        bestByConversationAnchor.put(anchor, sig);
                    }
                }

                List<ImSignalProtos.NewMsgSignal.MsgSig> deduped = new ArrayList<>(bestByConversationAnchor.values());
                for (ImSignalProtos.NewMsgSignal.MsgSig s : deduped) {
                    Log.d(TAG, "NEW_MSGSIG 拉列表 messageId=" + s.getMessageId() + " unread=" + s.getUnreadCount());
                    ImMessageManager.getInstance().fetchNewMessageList(
                            s.getMessageId(),
                            batch -> MessageProcessor.getInstance().receiveMsg(batch));
                }

            } catch (Exception e) {
                Log.e(TAG, "解析 NewMsgSignal 失败", e);
            }
        }
        // 群变更：0x0101 GroupChangeSig.groupList → 拉群详情 → 入库 → 通知 UI
        if (push.getHeader().getCmd() == ImSignalProtos.Cmd.GROUP_CHANGE_VALUE && push.hasBody()) {
            try {
                ImSignalProtos.GroupChangeSig sig = ImSignalProtos.GroupChangeSig.parseFrom(
                        push.getBody().toByteArray());
                List<String> groupList = sig.getGroupListList();
                Log.d(TAG, "GROUP_CHANGE groupList size=" + (groupList != null ? groupList.size() : 0));
                ImGroupChangeManager.getInstance().handleGroupChange(groupList, sessions ->
                        MessageProcessor.getInstance().notifySessionsUpdated(sessions));
            } catch (Exception e) {
                Log.e(TAG, "解析 GroupChangeSig 失败", e);
            }
        }

        // 群成员变更：0x0102 MemberChangeSig → 拉成员 / 软删除 → 入库 → 通知消息列表 UI
        if (push.getHeader().getCmd() == ImSignalProtos.Cmd.GROUP_MEMBER_CHANGE_VALUE && push.hasBody()) {
            try {
                ImSignalProtos.MemberChangeSig sig = ImSignalProtos.MemberChangeSig.parseFrom(
                        push.getBody().toByteArray());
                String groupId = sig.getGroupId();
                Log.d(TAG, "GROUP_MEMBER_CHANGE groupId=" + groupId
                        + " enters=" + sig.getEntersCount()
                        + " leaves=" + sig.getLeavesCount()
                        + " kickouts=" + sig.getKickoutsCount());
                ImGroupMemberChangeManager.getInstance().handleMemberChange(
                        groupId,
                        sig.getEntersList(),
                        sig.getLeavesList(),
                        sig.getKickoutsList());
            } catch (Exception e) {
                Log.e(TAG, "解析 MemberChangeSig 失败", e);
            }
        }

        // 单聊 0x0207：对方已读，messageId 序列及之前己方消息展示已读
        if (push.getHeader().getCmd() == ImSignalProtos.Cmd.MSG_READ_VALUE && push.hasBody()) {
            try {
                ImSignalProtos.MessageReadSig sig = ImSignalProtos.MessageReadSig.parseFrom(
                        push.getBody().toByteArray());
                String messageId = sig.getMessageId();
                Log.d(TAG, "MSG_READ messageId=" + messageId);
                MessageProcessor.getInstance().notifyPeerMessageRead(messageId);
            } catch (Exception e) {
                Log.e(TAG, "解析 MessageReadSig 失败", e);
            }
        }
        // 消息撤回：0x0202 MessageRepealSig
        if (push.getHeader().getCmd() == ImSignalProtos.Cmd.MSG_REPEAL_VALUE && push.hasBody()) {
            try {
                ImSignalProtos.MessageRepealSig sig = ImSignalProtos.MessageRepealSig.parseFrom(
                        push.getBody().toByteArray());
                String messageId = sig.getMessageId();
                Log.d(TAG, "MSG_REPEAL messageId=" + messageId);
                if (TextUtils.isEmpty(messageId)) {
                    return;
                }
                com.freddy.im.db.ImLocalStore.applyMessageRepeal(messageId, recalled -> {
                    if (recalled != null) {
                        MessageProcessor.getInstance().notifyMessageRepealed(messageId, recalled);
                        return;
                    }
                    ImMessageManager.getInstance().fetchNewMessageList(messageId,
                            batch -> MessageProcessor.getInstance().receiveMsg(batch));
                });
            } catch (Exception e) {
                Log.e(TAG, "解析 MessageRepealSig 失败", e);
            }
        }

        // 群聊 0x0210：已读人数变动
        if (push.getHeader().getCmd() == ImSignalProtos.Cmd.COUNT_MSG_READ_VALUE && push.hasBody()) {
            try {
                ImSignalProtos.CountMsgReadSig sig = ImSignalProtos.CountMsgReadSig.parseFrom(
                        push.getBody().toByteArray());
                Log.d(TAG, "COUNT_MSG_READ messageId=" + sig.getMessageId()
                        + " readPersonCount=" + sig.getReadPersonCount());
                MessageProcessor.getInstance().notifyGroupMessageReadCount(
                        sig.getMessageId(),
                        sig.getReadPersonCount());
            } catch (Exception e) {
                Log.e(TAG, "解析 CountMsgReadSig 失败", e);
            }
        }

    }

    @Override
    public void onKeyNegotiateResponse(short transportCode) {
        Log.d(TAG, "密钥协商传输层 code=" + transportCode);
    }

    @Override
    public boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) MyBroadApp.cxt.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        return info != null && info.isConnected();
    }

    @Override
    public int getReconnectInterval() {
        return 0;
    }

    @Override
    public int getConnectTimeout() {
        return 0;
    }

    @Override
    public int getForegroundHeartbeatInterval() {
        return 0;
    }

    @Override
    public int getBackgroundHeartbeatInterval() {
        return 0;
    }

    @Override
    public int getResendCount() {
        return 0;
    }

    @Override
    public int getResendInterval() {
        return 0;
    }

    @Override
    public String getUserId() {
        return userId;
    }

    @Override
    public String getBindToken() {
        return token;
    }

    @Override
    public String getRsaPublicKeyPem() {
        if (rsaPublicKeyPem != null) {
            return rsaPublicKeyPem;
        }
        synchronized (this) {
            if (rsaPublicKeyPem != null) {
                return rsaPublicKeyPem;
            }
            String pem;
            try (InputStream in = MyBroadApp.cxt.getAssets().open(RSA_PUBLIC_ASSET);
                 BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                StringBuilder sb = new StringBuilder(512);
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line).append('\n');
                }
                pem = sb.toString().trim();
            } catch (Exception e) {
                Log.e(TAG, "读取 assets/" + RSA_PUBLIC_ASSET + " 失败，回退 im_lib demo 公钥", e);
                pem = ImCryptoKeys.demoPublicKeyPem();
            }
            if (pem.isEmpty()) {
                pem = ImCryptoKeys.demoPublicKeyPem();
            }
            rsaPublicKeyPem = pem;
            return rsaPublicKeyPem;
        }
    }
}
