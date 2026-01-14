package com.cboda.agora.ui.ui.musicplayer;

import com.cboda.agora.ui.ui.musicplayer.listener.IMusicPlayerActionBroadListener;

import java.util.List;
import java.util.Map;



public interface IMusicPlayerBroadView {
    void setMusicPlayerActionListener(IMusicPlayerActionBroadListener listener);

    void setMusicPlayerActionDelegate(ActionDelegate actionDelegate);

    void setEffectProperties(Map<Integer,Integer> effectProperties);

    void setVoiceConversionList(List<ControllerEffectInfo> list);

    interface ActionDelegate {
        void onClickJoinChorus();
        void onClickLeaveChorus();
        void onClickSwitchSong();
        void onClickPlaying();
        void onClickOriginal();
        void onEarMonitoring(boolean enable);
        void onSignalVolume(int volume);
        void onMusicVolume(int volume);
        void onMusicPitch(int pitch);
        void onAudioEffect(int effectId);
        void onVoiceConversion(int voiceId);
        void onSkipPrelude(long seekPosition);
        void onSkipPostlude();
    }
}
