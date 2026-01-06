package com.cboda.chatemoji;

public interface IEmotionSelectedBroadListener {
    void onEmojiSelected(String key);

    void onStickerSelected(String categoryName, String stickerName, String stickerBitmapPath);
}
