package com.freddy.im.http.api;

import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hjq.http.config.IRequestApi;

/**
 * GET {@code im/app/version?platform=&version=}：检查客户端更新。
 */
public final class GetAppVersionApi implements IRequestApi {

    private String platform;
    private String version;

    @NonNull
    @Override
    public String getApi() {
//        String plat = platform != null ? platform : "";
//        StringBuilder sb = new StringBuilder("im/app/version?platform=")
//                .append(Uri.encode(plat));
//        if (!TextUtils.isEmpty(version)) {
//            sb.append("&version=").append(Uri.encode(version));
//        }
//        return sb.toString();
        return "im/app/version";
    }

    public GetAppVersionApi setPlatform(@NonNull String platform) {
        this.platform = platform;
        return this;
    }

    public GetAppVersionApi setVersion(@Nullable String version) {
        this.version = version;
        return this;
    }
}
