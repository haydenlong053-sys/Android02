package com.freddy.im.http.model.im;


import java.util.List;

import io.netty.util.internal.StringUtil;

/**
 * 转发消息请求体（文档 §4.6）。
 */
@SuppressWarnings("unused")
public class TranspondMessageParam {
//    ImSignalProtos.SendMsgReq.Builder smb = ImSignalProtos.SendMsgReq.newBuilder()
//            .setConversation(message.getConversationTypeEnum())
//            .setFrom(message.getFromId())
//            .setTo(message.getToId())
//            .setContentType(String.valueOf(message.getMsgContentType()))
////                .setContentType("text/plain")
//            .setContent(message.getContent() != null ? message.getContent() : "")
//            .setIdempotentId(message.getClientMsgId())
//            .setClientMsgId(message.getClientMsgId());
//        if (!StringUtil.isEmpty(message.getCustomInfo())) {
//        smb.setCustomInfo(message.getCustomInfo());
//    }

    private List<String> msgIdlist;
    private int conversation;
    private String from;
    private String to;
    private String content;
    private String contentType;
    private String clientMsgId;
    private String customInfo;
    private List<String> target;
    private String idempotentId;
    private int isCountRead;

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

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public int getConversation() {
        return conversation;
    }

    public void setConversation(int conversation) {
        this.conversation = conversation;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getClientMsgId() {
        return clientMsgId;
    }

    public void setClientMsgId(String clientMsgId) {
        this.clientMsgId = clientMsgId;
    }

    public String getCustomInfo() {
        return customInfo;
    }

    public void setCustomInfo(String customInfo) {
        this.customInfo = customInfo;
    }

    public List<String> getTarget() {
        return target;
    }

    public void setTarget(List<String> target) {
        this.target = target;
    }

    public String getIdempotentId() {
        return idempotentId;
    }

    public void setIdempotentId(String idempotentId) {
        this.idempotentId = idempotentId;
    }

    public int getIsCountRead() {
        return isCountRead;
    }

    public void setIsCountRead(int isCountRead) {
        this.isCountRead = isCountRead;
    }
}
