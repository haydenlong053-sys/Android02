package com.freddy.im.http;

import androidx.annotation.NonNull;

import com.cboda.baselibrary.utils.HttpsBroadConfig;
import com.hjq.http.config.IHttpPostBodyStrategy;
import com.hjq.http.config.IRequestServer;
import com.hjq.http.model.RequestBodyType;

/**
 * IM 测试环境：基址与默认 POST 体为 JSON。
 */
public final class RequestServer implements IRequestServer {

    @NonNull
    @Override
    public String getHost() {
//        return "http://95.40.9.142/";
        return HttpsBroadConfig.myImHttpsHost;
    }

    @NonNull
    @Override
    public IHttpPostBodyStrategy getBodyType() {
        return RequestBodyType.JSON;
    }
}
