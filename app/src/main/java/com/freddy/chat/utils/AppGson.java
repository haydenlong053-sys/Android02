package com.freddy.chat.utils;

import com.google.gson.Gson;

import com.blankj.utilcode.util.GsonUtils;

/**
 * App 层 Gson 单例（hosts 解析、Intent 序列化等）；与 im_lib HTTP 解析隔离。
 */
public final class AppGson {

//    private static final Gson GSON = GsonUtils.getGson();

    private AppGson() {
    }

    public static Gson get() {
        return GsonUtils.getGson();
    }
}
