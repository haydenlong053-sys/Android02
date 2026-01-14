package com.cboda.agora.ui.ui.musicplayer.listener;

/**
 * @author create by zhangwei03
 */
public interface IMusicPlayerEffectActionBroadListener {

    // 耳机返听
    void onEarChanged(boolean enable);

    // 音乐音量
    void onMusicVolChanged(int vol);

    // 人声音量
    void onSignalVolChanged(int vol);

    // 升降调
    void onMusicPitch(int pitch);

    // 混响
    void onAudioEffectChanged(int effectId);
}
