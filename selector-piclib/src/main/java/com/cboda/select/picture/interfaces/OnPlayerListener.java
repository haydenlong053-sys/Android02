package com.cboda.select.picture.interfaces;

/**
 * @author：luck
 * @date：2022/7/1 23:25 下午
 * @describe：OnPlayerListener
 */
public interface OnPlayerListener {
    /**
     * player error
     */
    void onPlayerError();

    /**
     * playing
     */
    void onPlayerReady();

    /**
     * preparing to play
     */
    void onPlayerLoading();

    /**
     * end of playback
     */
    void onPlayerEnd();
}
