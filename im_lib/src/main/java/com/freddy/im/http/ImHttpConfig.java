package com.freddy.im.http;

import static com.cboda.baselibrary.utils.ViewExpandUtilKt.getAppVersion;

import com.hjq.http.EasyConfig;

/**
 * IM 接口全局请求头：{@code apptoken}、{@code userId}、{@code clientType}。
 */
public final class ImHttpConfig {
    public static final int anchorPageSize =30;
    private ImHttpConfig() {
    }

    public static void setGlobalHeaders(String appToken, String userId, int clientType) {
        EasyConfig ec = EasyConfig.getInstance();
        ec.removeHeader("apptoken");
        ec.removeHeader("userId");
        ec.removeHeader("clientType");
        ec.addHeader("apptoken", appToken != null ? appToken : "");
        ec.addHeader("userId", userId != null ? userId : "");
        ec.addHeader("clientType", String.valueOf(clientType));
        ec.addHeader("App-Platform", "Android");
        ec.addHeader("App-Version",  getAppVersion(null));
    }
}
