package com.freddy.im.listener;

import androidx.annotation.NonNull;

import com.hjq.http.config.IRequestApi;

/**
 *    desc   :
 */
public interface OnHttpImListener<T> {


    /**
     * 请求成功
     */
    void onHttpImSuccess(@NonNull T result);

    /**
     * 请求出错
     */
//    void onHttpFail(@NonNull Throwable throwable);

}