package com.freddy.im.http.api;

import androidx.annotation.NonNull;

import com.hjq.http.config.IRequestApi;

/** POST {@code api/im/group/searchGroupAsst} */
public final class SearchGroupAsstApi implements IRequestApi {

    @NonNull
    @Override
    public String getApi() {
        return "im/group/searchGroupAsst";
    }
}
