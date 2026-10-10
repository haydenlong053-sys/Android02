package com.freddy.im.http;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.freddy.im.http.api.GetAppVersionApi;
import com.freddy.im.http.model.im.AppVersionRes;
import com.freddy.im.http.model.im.BaseMsgResponse;
import com.freddy.im.listener.OnHttpImListener;
import com.hjq.http.EasyHttp;
import com.hjq.http.lifecycle.ApplicationLifecycle;
import com.hjq.http.listener.OnHttpListener;

/**
 * 应用级 IM HTTP（版本检查等）。
 */
public final class ImAppHttpExample {

    private static final String TAG = "app_http";

    private ImAppHttpExample() {
    }

    /**
     * GET {@code im/app/version}：检查客户端更新。
     *
     * @param platform {@code ios}/{@code android}/{@code pc}
     * @param version  客户端当前版本号，如 {@code 1.1.0}；空则服务端 {@code needUpdate=false}
     */
    public static void requestGetAppVersion(@NonNull String platform,
                                            @Nullable String version,
                                            @NonNull OnHttpImListener<AppVersionRes> callback,
                                            @Nullable Runnable onFail) {
        if (TextUtils.isEmpty(platform)) {
            if (onFail != null) {
                onFail.run();
            }
            return;
        }
        EasyHttp.get(ApplicationLifecycle.getInstance())
                .api(new GetAppVersionApi().setPlatform(platform).setVersion(version))
                .request(new OnHttpListener<BaseMsgResponse<AppVersionRes>>() {
                    @Override
                    public void onHttpSuccess(@NonNull BaseMsgResponse<AppVersionRes> result) {
                        if (!result.isSuccess()) {
                            Log.e(TAG, "app/version 业务错误: errorCode=" + result.getErrorCode()
                                    + " msg=" + result.getErrorMsg());
                            if (onFail != null) {
                                onFail.run();
                            }
                            return;
                        }
                        AppVersionRes content = result.getContent();
                        if (content == null) {
                            // 未配置时成功但 content 可能为空，不当作故障
                            content = new AppVersionRes();
                        }
                        callback.onHttpImSuccess(content);
                    }

                    @Override
                    public void onHttpFail(@NonNull Throwable throwable) {
                        Log.e(TAG, "app/version 请求失败: " + throwable.getMessage());
                        if (onFail != null) {
                            onFail.run();
                        }
                    }
                });
    }
}
