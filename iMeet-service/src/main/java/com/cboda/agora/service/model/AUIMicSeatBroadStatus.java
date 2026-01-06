package com.cboda.agora.service.model;

import androidx.annotation.IntDef;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@IntDef({
        AUIMicSeatBroadStatus.idle,
        AUIMicSeatBroadStatus.used,
        AUIMicSeatBroadStatus.locked,
})
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface AUIMicSeatBroadStatus {
    int idle = 0; // 空闲
    int used = 1; // 使用中
    int locked = 2; // 锁定
}
