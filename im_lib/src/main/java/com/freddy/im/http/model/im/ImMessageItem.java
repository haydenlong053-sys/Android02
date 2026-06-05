package com.freddy.im.http.model.im;

@SuppressWarnings("unused")
public class ImMessageItem {

    private String sender;
    private String to;
    private String sendType;
    private String messageId;
    private String clientMsgId;
    private int msgType;
    private String contentType;
    private String customInfo;
    private String content;
    private long sendTime;
    private String status;
    private int readPersonCount;
    private String groupId;
    private long sequenceId;
    private String desc;

    public String getSender() {
        return sender;
    }

    public String getMessageId() {
        return messageId;
    }

    public String getContent() {
        return content;
    }

    public long getSendTime() {
        return sendTime;
    }

    public String getSendType() {
        return sendType;
    }

    public String getClientMsgId() {
        return clientMsgId;
    }

    public int getMsgType() {
        return msgType;
    }

    public String getContentType() {
        return contentType;
    }

    public String getCustomInfo() {
        return customInfo;
    }

    public String getStatus() {
        return status;
    }

    public int getReadPersonCount() {
        return readPersonCount;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getTo() {
        return to;
    }

    public long getSequenceId() {
        return sequenceId;
    }

    public String getDesc() {
        return desc;
    }
}
