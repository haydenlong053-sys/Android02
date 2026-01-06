package com.cboda.agora.service.model;

import androidx.annotation.IntDef;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@IntDef({
        AUIPlayBroadStatus.idle,
        AUIPlayBroadStatus.playing,
})
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER, ElementType.FIELD})
public @interface AUIPlayBroadStatus {
    int idle = 0; // 未播放
    int playing = 1; // 播放中
}
