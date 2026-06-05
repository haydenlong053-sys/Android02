package com.freddy.im.http.model;

import java.util.List;

@SuppressWarnings("unused")
@Deprecated
public class WanBannerResponse {

    private int errorCode;
    private String errorMsg;
    private List<WanBannerItem> data;

    public int getErrorCode() {
        return errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public List<WanBannerItem> getData() {
        return data;
    }

    public boolean isSuccess() {
        return errorCode == 0;
    }
}
