package com.freddy.im.http;

import static com.cboda.baselibrary.utils.ELogUtilKt.getIsDebug;

import android.app.Application;

import com.cboda.baselibrary.network.ResBroadInterceptor;
import com.freddy.im.BuildConfig;
import com.freddy.im.db.ImLocalStore;
import com.hjq.http.EasyConfig;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

/**
 * 在宿主 {@link Application#onCreate()} 中调用一次，完成 EasyHttp 全局配置。
 */
public final class EasyHttpBootstrap {

    private EasyHttpBootstrap() {
    }

    public static void init(Application app) {
        ImLocalStore.init(app);
        OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS);
        if (getIsDebug()) {
            ResBroadInterceptor loggingInterceptor = new ResBroadInterceptor("AppEasyHttp");
            okHttpClientBuilder.addInterceptor(loggingInterceptor);
        }
        OkHttpClient okHttpClient = okHttpClientBuilder.build();
        EasyConfig.with(okHttpClient)
                .setLogEnabled(false)
                .setServer(new RequestServer())
                .setHandler(new ImRequestHandler(app))
                .into();

    }
}
