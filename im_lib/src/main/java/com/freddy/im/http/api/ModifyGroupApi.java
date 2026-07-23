package com.freddy.im.http.api;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hjq.http.config.IRequestApi;

/** POST {@code api/im/group/modify} */
public final class ModifyGroupApi implements IRequestApi {

    private String groupId;
    private String name;
    private String icon;
    /** 1=允许群员邀请，0=不允许；未设置时不传该字段 */
    private Integer allowMemberInvite;
    /** 1=需要入群验证，0=不需要；未设置时不传该字段 */
    private Integer joinNeedVerify;

    @NonNull
    @Override
    public String getApi() {
        return "api/im/group/modify";
    }

    public ModifyGroupApi setGroupId(String groupId) {
        this.groupId = groupId;
        return this;
    }

    public ModifyGroupApi setName(String name) {
        this.name = name;
        return this;
    }

    public ModifyGroupApi setIcon(String icon) {
        this.icon = icon;
        return this;
    }

    public ModifyGroupApi setAllowMemberInvite(@Nullable Integer allowMemberInvite) {
        this.allowMemberInvite = allowMemberInvite;
        return this;
    }

    public ModifyGroupApi setJoinNeedVerify(@Nullable Integer joinNeedVerify) {
        this.joinNeedVerify = joinNeedVerify;
        return this;
    }
}
