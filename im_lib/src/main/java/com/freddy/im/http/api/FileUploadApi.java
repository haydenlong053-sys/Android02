package com.freddy.im.http.api;

import androidx.annotation.NonNull;

import com.hjq.http.config.IHttpPostBodyStrategy;
import com.hjq.http.config.IRequestApi;
import com.hjq.http.config.IRequestBodyType;
import com.hjq.http.model.RequestBodyType;

import java.io.File;

import okhttp3.RequestBody;

/**
 * POST {@code filehub/upload}：multipart 上传文件（§7 文件服务）。
 */
public final class FileUploadApi implements IRequestApi, IRequestBodyType {

    private File file;
    private RequestBody param;

    @NonNull
    @Override
    public String getApi() {
        return "filehub/upload";
    }

    @NonNull
    @Override
    public IHttpPostBodyStrategy getBodyType() {
        return RequestBodyType.FORM;
    }

    public FileUploadApi setFile(File file) {
        this.file = file;
        return this;
    }

    public FileUploadApi setParam(RequestBody param) {
        this.param = param;
        return this;
    }
}
