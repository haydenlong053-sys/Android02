package com.freddy.im.http.model.im;

import java.io.Serializable;

/**
 * 会话权限（会话列表 {@code sessionList[].permission} 对象）。
 */
@SuppressWarnings("unused")
public class SessionPermission implements Serializable {

    private boolean sendSysMsg;
    private boolean noDisturbing;
    private boolean changeAttr;
    private boolean delUser;
    private boolean addUser;
    private boolean groupDetail;
    private boolean onTop;
    private boolean memberDetail;
    private boolean sendMsg;
    private boolean callUser;

    public boolean isSendSysMsg() {
        return sendSysMsg;
    }

    public void setSendSysMsg(boolean sendSysMsg) {
        this.sendSysMsg = sendSysMsg;
    }

    public boolean isNoDisturbing() {
        return noDisturbing;
    }

    public void setNoDisturbing(boolean noDisturbing) {
        this.noDisturbing = noDisturbing;
    }

    public boolean isChangeAttr() {
        return changeAttr;
    }

    public void setChangeAttr(boolean changeAttr) {
        this.changeAttr = changeAttr;
    }

    public boolean isDelUser() {
        return delUser;
    }

    public void setDelUser(boolean delUser) {
        this.delUser = delUser;
    }

    public boolean isAddUser() {
        return addUser;
    }

    public void setAddUser(boolean addUser) {
        this.addUser = addUser;
    }

    public boolean isGroupDetail() {
        return groupDetail;
    }

    public void setGroupDetail(boolean groupDetail) {
        this.groupDetail = groupDetail;
    }

    public boolean isOnTop() {
        return onTop;
    }

    public void setOnTop(boolean onTop) {
        this.onTop = onTop;
    }

    public boolean isMemberDetail() {
        return memberDetail;
    }

    public void setMemberDetail(boolean memberDetail) {
        this.memberDetail = memberDetail;
    }

    public boolean isSendMsg() {
        return sendMsg;
    }

    public void setSendMsg(boolean sendMsg) {
        this.sendMsg = sendMsg;
    }

    public boolean isCallUser() {
        return callUser;
    }

    public void setCallUser(boolean callUser) {
        this.callUser = callUser;
    }
}
