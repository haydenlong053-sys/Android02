package com.freddy.im.http.model.im;

import java.util.List;

/**
 * 转发消息请求体（文档 §4.6）。
 */
@SuppressWarnings("unused")
public class TranspondMessageParam {

    private List<String> msgIdlist;
    private String to;
    private int conversation;
    private String contentType;

    public List<String> getMsgIdlist() {
        return msgIdlist;
    }

    public void setMsgIdlist(List<String> msgIdlist) {
        this.msgIdlist = msgIdlist;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public int getConversation() {
        return conversation;
    }

    public void setConversation(int conversation) {
        this.conversation = conversation;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}
