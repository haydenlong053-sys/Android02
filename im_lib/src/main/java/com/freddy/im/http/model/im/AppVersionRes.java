package com.freddy.im.http.model.im;

/**
 * GET {@code im/app/version} 响应 content。
 * {@code needUpdate}/{@code forceUpdate} 由服务端比较得出，客户端勿自行比版本号。
 */
@SuppressWarnings("unused")
public class AppVersionRes {

    private String latestVersion;
    private String minVersion;
    private String downloadUrl;
    private String releaseNote;
    private boolean needUpdate;
    private boolean forceUpdate;

    public String getLatestVersion() {
        return latestVersion;
    }

    public String getMinVersion() {
        return minVersion;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public String getReleaseNote() {
        return releaseNote;
    }

    public boolean isNeedUpdate() {
        return needUpdate;
    }

    public boolean isForceUpdate() {
        return forceUpdate;
    }
}
