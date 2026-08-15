package com.freddy.chat.im;

import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.freddy.chat.utils.AppGson;
import com.freddy.im.IMSClientFactory;
import com.freddy.im.db.ImLocalStore;
import com.freddy.im.interf.IMSClientInterface;
import com.freddy.im.signal.ImSignalProtos;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Vector;

/**
 * 应用层的 imsClient 启动器。
 */
public class IMSClientBootstrap {

    private static final IMSClientBootstrap INSTANCE = new IMSClientBootstrap();
    private static final Type HOST_LIST_TYPE = new TypeToken<List<ImHostEntry>>() {
    }.getType();

    private IMSClientInterface imsClient;
    private boolean isActive;
    private String currentUserId;
    private String currentToken;
    /** 最近一次成功 init 时使用的 hosts 原文，用于环境切换判断。 */
    @Nullable
    private String currentHosts;
    @Nullable
    private String currentUserAvatarUrl = "";
    private final String defaultAvatarUrl = "https://d2ixzsnmawz3gt.cloudfront.net/file/app/icon.jpg";

    private IMSClientBootstrap() {
    }

    public static IMSClientBootstrap getInstance() {
        return INSTANCE;
    }

    /**
     * @return 是否成功拉起长连（hosts 非法时返回 false，且不破坏已有会话）
     */
    public synchronized boolean init(String userId, String token, String hosts, int appStatus) {
        if (TextUtils.isEmpty(userId)) {
            System.out.println("init IMSClientBootstrap error, userId is empty");
            return false;
        }
        if (TextUtils.isEmpty(token)) {
            System.out.println("init IMSClientBootstrap error, token is empty");
            return false;
        }
        Vector<String> serverUrlList = convertHosts(hosts);
        if (serverUrlList == null || serverUrlList.isEmpty()) {
            System.out.println("init IMSClientBootstrap error, ims hosts is null");
            return false;
        }

        if (imsClient != null) {
            imsClient.close();
            imsClient = null;
        }

        ImLocalStore.openForUser(userId);

        isActive = true;
        this.currentUserId = userId;
        this.currentToken = token;
        this.currentHosts = hosts;
        this.currentUserAvatarUrl = null;
        System.out.println("init IMSClientBootstrap, userId=" + userId + ", servers=" + hosts);
        imsClient = IMSClientFactory.getIMSClient();
        updateAppStatus(appStatus);
        imsClient.init(serverUrlList, new IMSEventListener(userId, token), new IMSConnectStatusListener());
        return true;
    }

    /**
     * 登出：断开长连接并关闭当前用户 Realm 库。
     */
    public synchronized void logout() {
        if (imsClient != null) {
            imsClient.close();
            imsClient = null;
        }
        ImLocalStore.closeForUser();
        isActive = false;
        currentUserId = null;
        currentToken = null;
        currentHosts = null;
        currentUserAvatarUrl = null;
        ImConnectStateStore.reset();
    }

    public boolean isActive() {
        return isActive;
    }

    /**
     * 长连客户端仍可用：已激活且未 close（断网重连中仍视为可用，由 Netty 内部重连）。
     */
    public synchronized boolean isClientUsable() {
        return isActive && imsClient != null && !imsClient.isClosed();
    }

    public synchronized boolean isImChannelReady() {
        return isClientUsable() && imsClient.isUserChannelBound();
    }

    /**
     * 同账号、同 token、同 hosts，且客户端可用 → 可复用，无需重 init。
     */
    public synchronized boolean canReuseSession(String userId, String token, String hosts) {
        return isClientUsable()
                && TextUtils.equals(userId, currentUserId)
                && TextUtils.equals(token, currentToken)
                && TextUtils.equals(hosts, currentHosts);
    }

    /** 系统网络恢复时尝试踢醒重连（若已在重连环内则 no-op）。 */
    public synchronized void notifyNetworkAvailable() {
        if (imsClient != null && !imsClient.isClosed()) {
            imsClient.notifyNetworkAvailable();
        }
    }

    public synchronized void resetConnect() {
        if (imsClient != null && !imsClient.isClosed()) {
            imsClient.resetConnect(false);
        }
    }

    public String getBindToken() {
        return currentToken;
    }

    @Nullable
    public String getBindHosts() {
        return currentHosts;
    }

    public String getCurrentUserId() {
        return currentUserId;
    }

    @Nullable
    public String getCurrentUserAvatarUrl() {
        return currentUserAvatarUrl == null ? defaultAvatarUrl : currentUserAvatarUrl;
    }

    public void setCurrentUserAvatarUrl(@Nullable String avatarUrl) {
        this.currentUserAvatarUrl = avatarUrl;
    }

    /**
     * 发送 imsignal 请求（内部 CMD 0x02 + AES）。
     */
    public void sendSignalRequest(ImSignalProtos.SignalRequest request, boolean joinTimeoutManager) {
        if (isActive && imsClient != null) {
            imsClient.sendSignalRequest(request, joinTimeoutManager);
        }
    }

    private Vector<String> convertHosts(String hosts) {
        if (hosts == null || hosts.isEmpty()) {
            return null;
        }
        List<ImHostEntry> hostArray = AppGson.get().fromJson(hosts, HOST_LIST_TYPE);
        if (hostArray == null || hostArray.isEmpty()) {
            return null;
        }
        Vector<String> serverUrlList = new Vector<>();
        for (ImHostEntry entry : hostArray) {
            if (entry != null && entry.host != null) {
                serverUrlList.add(entry.host + " " + entry.port);
            }
        }
        return serverUrlList.isEmpty() ? null : serverUrlList;
    }

    public void updateAppStatus(int appStatus) {
        if (imsClient == null) {
            return;
        }
        imsClient.setAppStatus(appStatus);
    }

    /**
     * 上报消息已读（{@link ImSignalProtos.MessageReadSig}）。
     */
    public void reportMessageRead(String messageId) {
        if (isActive && imsClient != null && messageId != null) {
            imsClient.reportMessageReadSig(messageId);
        }
    }

    /**
     * hosts JSON 数组元素：{@code {"host":"x.x.x.x","port":1081}}
     */
    @SuppressWarnings("unused")
    private static final class ImHostEntry {
        String host;
        int port;
    }
}
