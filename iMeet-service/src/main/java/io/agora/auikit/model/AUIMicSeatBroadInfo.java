package io.agora.auikit.model;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

public class AUIMicSeatBroadInfo {

    @SerializedName("owner")
    public @Nullable AUIUserThumbnailBroadInfo user;

    /** 麦位索引，可以不需要，根据麦位list可以计算出 */
    @SerializedName("micSeatNo")
    public int seatIndex = 0;
    /** 麦位状态 */
    @SerializedName("micSeatStatus")
    public @AUIMicSeatBroadStatus int seatStatus = AUIMicSeatBroadStatus.idle;
    /** 麦位禁用声音 */
    @SerializedName("isMuteAudio")
    public boolean muteAudio = false;

    /** 麦位禁用视频 */
    @SerializedName("isMuteVideo")
    public boolean muteVideo = false;

    @Override
    public String toString() {
        return "AUIMicSeatBroadInfo{" +
                "user=" + user +
                ", seatIndex=" + seatIndex +
                ", seatStatus=" + seatStatus +
                ", muteAudio=" + muteAudio +
                ", muteVideo=" + muteVideo +
                '}';
    }
}
