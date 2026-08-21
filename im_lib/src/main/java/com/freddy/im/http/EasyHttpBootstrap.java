package com.freddy.im.http;

import static com.cboda.baselibrary.utils.ELogUtilKt.getIsDebug;

import android.app.Application;

import androidx.annotation.NonNull;

import com.cboda.baselibrary.network.ResBroadInterceptor;
import com.freddy.im.db.ImLocalStore;
import com.hjq.http.EasyConfig;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

/**
 * 在宿主 {@link Application#onCreate()} 中调用一次，完成 EasyHttp 全局配置。
 */
public final class EasyHttpBootstrap {

    /** 视频等大文件上传：写超时需远长于普通 API 的 30s。 */
    private static final long MEDIA_CONNECT_TIMEOUT_SEC = 60L;
    private static final long MEDIA_READ_TIMEOUT_MIN = 5L;
    private static final long MEDIA_WRITE_TIMEOUT_MIN = 5L;
    private static final long MEDIA_CALL_TIMEOUT_MIN = 10L;

    private static volatile OkHttpClient mediaUploadClient;

    private EasyHttpBootstrap() {
    }

    public static void init(Application app) {
        ImLocalStore.init(app);
        OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS);
        if (getIsDebug()) {
            ResBroadInterceptor loggingInterceptor = new ResBroadInterceptor("AppHttpEasy", false);
            okHttpClientBuilder.addInterceptor(loggingInterceptor);
        }
        OkHttpClient okHttpClient = okHttpClientBuilder.build();
        EasyConfig.with(okHttpClient)
                .setLogEnabled(getIsDebug())
                .setServer(new RequestServer())
                .setHandler(new ImRequestHandler(app))
                .into();
    }

    /**
     * 媒体大文件上传专用 Client：更长 write/call 超时，且不挂会二次读 body 的日志拦截器，
     * 避免视频 batchUpload 30s 写超时或日志阶段 OOM。
     */
    @NonNull
    public static OkHttpClient getMediaUploadClient() {
        OkHttpClient cached = mediaUploadClient;
        if (cached != null) {
            return cached;
        }
        synchronized (EasyHttpBootstrap.class) {
            if (mediaUploadClient == null) {
                mediaUploadClient = new OkHttpClient.Builder()
                        .connectTimeout(MEDIA_CONNECT_TIMEOUT_SEC, TimeUnit.SECONDS)
                        .readTimeout(MEDIA_READ_TIMEOUT_MIN, TimeUnit.MINUTES)
                        .writeTimeout(MEDIA_WRITE_TIMEOUT_MIN, TimeUnit.MINUTES)
                        .callTimeout(MEDIA_CALL_TIMEOUT_MIN, TimeUnit.MINUTES)
                        .retryOnConnectionFailure(true)
                        .build();
            }
            return mediaUploadClient;
        }
    }
}
