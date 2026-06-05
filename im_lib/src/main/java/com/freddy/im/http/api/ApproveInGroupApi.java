package com.freddy.im.http.api;

import androidx.annotation.NonNull;

import com.hjq.http.config.IRequestApi;

/** POST {@code api/im/group/approveInGroup} */
public final class ApproveInGroupApi implements IRequestApi {

    private String applyId;
    private String approveStatus;

    @NonNull
    @Override
    public String getApi() {
        return "api/im/group/approveInGroup";
    }

    public ApproveInGroupApi setApplyId(String applyId) {
        this.applyId = applyId;
        return this;
    }

    public ApproveInGroupApi setApproveStatus(String approveStatus) {
        this.approveStatus = approveStatus;
        return this;
    }
}
