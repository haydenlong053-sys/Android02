package com.freddy.im.http.api;

import androidx.annotation.NonNull;

import com.hjq.http.config.IRequestApi;

/** POST {@code api/im/group/searchESGroups} */
public final class SearchEsGroupsApi implements IRequestApi {

    private String keyword;
    private Integer limit;

    @NonNull
    @Override
    public String getApi() {
        return "im/group/searchESGroups";
    }

    public SearchEsGroupsApi setKeyword(String keyword) {
        this.keyword = keyword;
        return this;
    }

    public SearchEsGroupsApi setLimit(int limit) {
        this.limit = limit;
        return this;
    }
}
