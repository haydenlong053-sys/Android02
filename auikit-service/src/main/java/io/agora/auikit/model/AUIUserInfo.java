package io.agora.auikit.model;

public class AUIUserInfo extends AUIUserThumbnailInfo {
    public int muteAudio = 0;  //是否静音状态
    public int muteVideo = 0;  //是否关闭视频状态
    public int micIndex = -1;  //用户所在麦位号 -1表示不在麦位
    public int  isRead = 1;//是否点开过


    @Override
    public String toString() {
        return "AUIUserInfo{" +
                "muteAudio=" + muteAudio +
                ", muteVideo=" + muteVideo +
                ", micIndex=" + micIndex +
                "} " + super.toString();
    }
}
