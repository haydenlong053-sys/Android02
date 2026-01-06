package com.cboda.agora.service.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cboda.agora.service.service.arbiter.AUIBroadArbiter;


public class AUIRoomBroadContext {
    private static volatile AUIRoomBroadContext instance = null;

    private AUIRoomBroadContext() {
        // 私有构造函数
    }

    public static AUIRoomBroadContext shared() {
        if (instance == null) {
            synchronized (AUIRoomBroadContext.class) {
                if (instance == null) {
                    instance = new AUIRoomBroadContext();
                }
            }
        }
        return instance;
    }

    public Map<String, AUIRoomBroadConfig> roomConfigMap = new HashMap<>();
    public Map<String, AUIBroadArbiter> roomArbiterMap = new HashMap<>();

    public @NonNull AUIUserThumbnailBroadInfo currentUserInfo = new AUIUserThumbnailBroadInfo();
    public @Nullable AUICommonBroadConfig mCommonConfig;
    private final Map<String, AUIRoomBroadInfo> roomInfoMap = new HashMap<>();

    private @Nullable NtpTimeSource ntpTimeSource;

    public void setCommonConfig(@NonNull AUICommonBroadConfig config) {
        mCommonConfig = config;
        currentUserInfo = config.owner;
    }

    public @NonNull AUICommonBroadConfig requireCommonConfig() {
        if (mCommonConfig == null) {
            throw new RuntimeException("mCommonConfig is null now!");
        }
        return mCommonConfig;
    }

    public boolean isRoomOwner(String channelName) {
        return isRoomOwner(channelName, currentUserInfo.userId);
    }

    public boolean isRoomOwner(String channelName, String userId) {
        AUIRoomBroadInfo roomInfo = roomInfoMap.get(channelName);
        if (roomInfo == null || roomInfo.owner == null) {
            return false;
        }
        return roomInfo.owner.userId.equals(userId);
    }

    public void resetRoomMap(@Nullable List<AUIRoomBroadInfo> roomInfoList) {
        roomInfoMap.clear();
        if (roomInfoList == null || roomInfoList.size() == 0) {
            return;
        }
        for (AUIRoomBroadInfo info : roomInfoList) {
            roomInfoMap.put(info.roomId, info);
        }
    }

    public void insertRoomInfo(AUIRoomBroadInfo info) {
        roomInfoMap.put(info.roomId, info);
    }

    public void cleanRoom(String channelName) {
        roomInfoMap.remove(channelName);
        roomConfigMap.remove(channelName);
        AUIBroadArbiter auiArbiter = roomArbiterMap.remove(channelName);
        if (auiArbiter != null) {
            auiArbiter.deInit();
        }
    }

    public String getRoomOwner(String channelName) {
        AUIRoomBroadInfo roomInfo = roomInfoMap.get(channelName);
        if (roomInfo == null || roomInfo.owner == null) {
            return "";
        }
        return roomInfo.owner.userId;
    }

    public @Nullable AUIRoomBroadInfo getRoomInfo(String channelName) {
        return roomInfoMap.get(channelName);
    }

    public @Nullable AUIBroadArbiter getArbiter(@NotNull String channelName) {
        return roomArbiterMap.get(channelName);
    }

    public void setNtpTimeSource(@Nullable NtpTimeSource ntpTimeSource) {
        this.ntpTimeSource = ntpTimeSource;
    }

    public long getNtpTime() {
        if (ntpTimeSource != null) {
            return ntpTimeSource.getNtpTime();
        }
        return System.currentTimeMillis();
    }

    public interface NtpTimeSource {
        long getNtpTime();
    }
}
