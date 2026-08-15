package com.freddy.chat.im;

import static com.cboda.baselibrary.utils.ELogUtilKt.eLogD;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

/**
 * IM 长连业务就绪状态（跨 ViewModel 共享，供会话页断连条使用）。
 */
public final class ImConnectStateStore {

    private static final String TAG = "ImConnectStateStore";
    private static final MutableLiveData<Boolean> IM_CONNECT_FAILED = new MutableLiveData<>(false);
    private static final MutableLiveData<Boolean> IM_CHANNEL_READY = new MutableLiveData<>(false);

    private ImConnectStateStore() {
    }

    public static LiveData<Boolean> imConnectFailed() {
        return IM_CONNECT_FAILED;
    }

    public static LiveData<Boolean> imChannelReady() {
        return IM_CHANNEL_READY;
    }

    public static void markConnecting() {
        eLogD(TAG, "markConnecting");
    }

    public static void markTcpConnected() {
        eLogD(TAG, "markTcpConnected");
    }

    public static void markImChannelReady() {
        eLogD(TAG, "markImChannelReady");
        IM_CHANNEL_READY.postValue(true);
        IM_CONNECT_FAILED.postValue(false);
    }

    public static void markConnectFailed() {
        eLogD(TAG, "markConnectFailed");
        IM_CHANNEL_READY.postValue(false);
        IM_CONNECT_FAILED.postValue(true);
    }

    public static void reset() {
        IM_CHANNEL_READY.postValue(false);
        IM_CONNECT_FAILED.postValue(false);
    }
}
