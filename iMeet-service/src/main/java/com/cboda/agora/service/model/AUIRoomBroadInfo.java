package com.cboda.agora.service.model;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Map;

public class AUIRoomBroadInfo extends AUICreateRoomBroadInfo implements Serializable {

    @SerializedName("roomOwner")
    public @Nullable AUIUserThumbnailBroadInfo owner; // 房主信息
    @SerializedName("onlineUsers")
    public int memberCount = 0; // 房间人数
    public long createTime = 0; // 房间创建时间

    public Map<String, Object> customPayload; // 扩展字段

}
