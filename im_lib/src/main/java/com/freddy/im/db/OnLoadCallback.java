package com.freddy.im.db;

import androidx.annotation.NonNull;

/**
 * 异步本地库查询结果回调（结果在主线程交付）。
 *
 * @param <T> 查询结果类型
 */
public interface OnLoadCallback<T> {

    void onLoaded(@NonNull T result);
}
