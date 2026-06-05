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
    @Nullable
    private String currentUserAvatarUrl = "";
    private final String defaultAvatarUrl = "https://d2ixzsnmawz3gt.cloudfront.net/file/app/icon.jpg";

    private IMSClientBootstrap() {
    }

    public static IMSClientBootstrap getInstance() {
        return INSTANCE;
    }

    public synchronized void init(String userId, String token, String hosts, int appStatus) {
        if (TextUtils.isEmpty(userId)) {
            System.out.println("init IMSClientBootstrap error, userId is empty");
            return;
        }
        Vector<String> serverUrlList = convertHosts(hosts);
        if (serverUrlList == null || serverUrlList.isEmpty()) {
            System.out.println("init IMSClientBootstrap error, ims hosts is null");
            return;
        }

        if (imsClient != null) {
            imsClient.close();
            imsClient = null;
        }

        ImLocalStore.openForUser(userId);

        isActive = true;
        this.currentUserId = userId;
        this.currentToken = token;
        this.currentUserAvatarUrl = null;
        System.out.println("init IMSClientBootstrap, userId=" + userId + ", servers=" + hosts);
        imsClient = IMSClientFactory.getIMSClient();
        updateAppStatus(appStatus);
        imsClient.init(serverUrlList, new IMSEventListener(userId, token), new IMSConnectStatusListener());
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
        currentUserAvatarUrl = null;
    }

    public boolean isActive() {
        return isActive;
    }

    public String getBindToken() {
        return currentToken;
    }

    public String getCurrentUserId() {
        return currentUserId;
    }

    @Nullable
    public String getCurrentUserAvatarUrl() {
        return currentUserAvatarUrl==null?defaultAvatarUrl:currentUserAvatarUrl;
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
