/*
 * Copyright (c) 2022 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.voip.conference;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.cboda.chatui.R;

public class MicImageBroadView extends ImageView {
    private boolean muted = false;
    private int volume = 0;
    private static final int[] resources = {
        R.drawable.av_mic_broad_0,
        R.drawable.av_mic_broad_1,
        R.drawable.av_mic_broad_2,
        R.drawable.av_mic_broad_3,
        R.drawable.av_mic_broad_4,
        R.drawable.av_mic_broad_5,
        R.drawable.av_mic_broad_6,
        R.drawable.av_mic_broad_7,
        R.drawable.av_mic_broad_8,
        R.drawable.av_mic_broad_9,
        R.drawable.av_mic_broad_10,
    };

    public MicImageBroadView(Context context) {
        super(context);
        init();
    }

    public MicImageBroadView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MicImageBroadView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public MicImageBroadView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        setImageResource(R.drawable.av_mic_broad_0);
    }

    public void setMuted(boolean muted) {
        this.muted = muted;
        if (muted) {
            setImageResource(R.drawable.av_mic_broad_mute);
        } else {
            setImageResource(R.drawable.av_mic_broad_0);
        }
    }

    public void setVolume(int volume) {
        if (this.muted){
            return;
        }
        this.volume = volume;
        int v = volume / 1000;
        v = Math.max(v, 0);
        v = Math.min(v, 10);
        setImageResource(resources[v]);
    }
}
