package com.freddy.im.http;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import androidx.annotation.NonNull;

import com.freddy.im.http.model.im.BaseMsgResponse;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.hjq.http.EasyLog;
import com.hjq.http.config.IRequestHandler;
import com.hjq.http.exception.CancelException;
import com.hjq.http.exception.DataException;
import com.hjq.http.exception.HttpException;
import com.hjq.http.exception.NetworkException;
import com.hjq.http.exception.NullBodyException;
import com.hjq.http.exception.ResponseException;
import com.hjq.http.exception.ServerException;
import com.hjq.http.exception.TimeoutException;
import com.hjq.http.request.HttpRequest;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * EasyHttp 全局响应解析与异常映射（精简自官方 Demo）。
 */
public final class ImRequestHandler implements IRequestHandler {

    private final Application app;
    private static final Gson GSON = ImGsonHelper.getGson();

    private final Gson gson = GSON;

    public ImRequestHandler(Application application) {
        this.app = application;
    }

    @NonNull
    @Override
    public Object requestSuccess(@NonNull HttpRequest httpRequest, @NonNull Response response, @NonNull Type type) throws Throwable {
        if (Response.class.equals(type)) {
            return response;
        }
        if (!response.isSuccessful()) {
            throw new ResponseException("HTTP " + response.code() + " " + response.message(), response);
        }
        ResponseBody body = response.body();
        if (body == null) {
            throw new NullBodyException("响应体为空");
        }
        if (ResponseBody.class.equals(type)) {
            return body;
        }
        if (type instanceof GenericArrayType) {
            Type component = ((GenericArrayType) type).getGenericComponentType();
            if (byte.class.equals(component)) {
                return body.bytes();
            }
        }
        if (InputStream.class.equals(type)) {
            return body.byteStream();
        }
        if (Bitmap.class.equals(type)) {
            return BitmapFactory.decodeStream(body.byteStream());
        }

        String text;
        try {
            text = body.string();
        } catch (IOException e) {
            throw new DataException("读取响应失败", e);
        }

        EasyLog.printJson(httpRequest, text);

        if (String.class.equals(type)) {
            return text;
        }

        try {
            Object parsed = gson.fromJson(text, type);
            if (parsed instanceof BaseMsgResponse) {
                BaseMsgResponse<?> biz = (BaseMsgResponse<?>) parsed;
                if (!biz.isSuccess()) {
                    ImAuthInvalidHandler.handleIfNeeded(biz.getErrorCode(), biz.getErrorMsg());
                }
            }
            return parsed;
        } catch (JsonSyntaxException e) {
            throw new DataException("JSON 解析失败", e);
        }
    }

    @NonNull
    @Override
    public Throwable requestFail(@NonNull HttpRequest httpRequest, @NonNull Throwable throwable) {
        if (throwable instanceof HttpException) {
            return throwable;
        }
        if (throwable instanceof SocketTimeoutException) {
            return new TimeoutException("连接或读取超时", throwable);
        }
        if (throwable instanceof UnknownHostException) {
            NetworkInfo info = ((ConnectivityManager) app.getSystemService(Context.CONNECTIVITY_SERVICE))
                    .getActiveNetworkInfo();
            if (info != null && info.isConnected()) {
                return new ServerException("无法解析主机，请检查接口域名", throwable);
            }
            return new NetworkException("网络不可用", throwable);
        }
        if (throwable instanceof IOException) {
            return new CancelException("请求已取消或中断", throwable);
        }
        return new HttpException(throwable.getMessage() != null ? throwable.getMessage() : "未知错误", throwable);
    }
}
