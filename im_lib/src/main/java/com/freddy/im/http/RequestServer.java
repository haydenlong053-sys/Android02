package com.freddy.im.http;

import androidx.annotation.NonNull;

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
        return "https://im-test.seekweb4.ai/";
    }

    @NonNull
    @Override
    public IHttpPostBodyStrategy getBodyType() {
        return RequestBodyType.JSON;
    }
}
