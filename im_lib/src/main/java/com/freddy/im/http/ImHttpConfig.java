package com.freddy.im.http;

import static com.cboda.baselibrary.utils.MMKVBroadUtils.APP_TOKEN;
import static com.cboda.baselibrary.utils.ViewExpandUtilKt.getAppVersion;

import com.cboda.baselibrary.utils.MMKVBroadUtils;
import com.hjq.http.EasyConfig;
import com.hjq.http.EasyHttp;

/**
 * IM 接口全局请求头：{@code apptoken}、{@code userId}、{@code clientType}。
 */
public final class ImHttpConfig {
    public static final int anchorPageSize = 30;

    private ImHttpConfig() {
    }

    public static void setGlobalHeaders(String appToken, String userId, int clientType) {
        String myImToken = MMKVBroadUtils.Companion.getMyImToken();
        EasyConfig ec = EasyConfig.getInstance();
        ec.removeHeader(APP_TOKEN);
        ec.removeHeader("token");
        ec.removeHeader("userId");
        ec.removeHeader("clientType");
        ec.addHeader(APP_TOKEN, appToken != null ? appToken : "");
        ec.addHeader("token", myImToken);
        ec.addHeader("userId", userId != null ? userId : "");
        ec.addHeader("clientType", String.valueOf(clientType));
        ec.addHeader("App-Platform", "Android");
        ec.addHeader("App-Version", getAppVersion(null));
    }

    /**
     * 登出/被踢时清空 IM EasyHttp 全局头。
     */
    public static void clearGlobalHeaders() {
        EasyHttp.cancelAll();
        MMKVBroadUtils.Companion.saveMyImToken("");
        setGlobalHeaders("", "", 0);
    }
}
