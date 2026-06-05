package com.freddy.im.http.model.im;

/**
 * {@code POST /filehub/upload} 成功时 {@code content}。
 */
public final class UploadRes {

    private String url;
    private String thumbImage;
    private String largeImage;

    public String getUrl() {
        return url;
    }

    public String getThumbImage() {
        return thumbImage;
    }

    public String getLargeImage() {
        return largeImage;
    }
}
