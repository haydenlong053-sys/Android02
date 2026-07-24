package com.freddy.im.bean;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.freddy.im.signal.ImSignalProtos;

/**
 * IM SDK 统一消息载体（长连接、HTTP 拉取、宿主 UI 共用）。
 */
public class AppMessage implements Parcelable {

    /** 对应 {@link ImSignalProtos.ConversationType} 的 number 值 */
    private int conversationType;

    private String conversationId;
    private String msgId;
    private String clientMsgId;
    private int msgType;
    private String sendType;
    private int msgContentType;
    private String fromId;
    private String toId;
    private long timestamp;
    private long sequenceId;

    private int statusReport;
    private String extend;
    private String customInfo;
    private String extra;
    private String content;

    /** 对应 {@link com.freddy.im.db.entity.MessageEntity} 的发送状态 */
    private int sendStatus;

    /** 群聊：已读人数（HTTP 列表或 0x0210 信令） */
    private int readPersonCount;

    /** 消息状态：0 正常，1 撤回（见 {@link com.freddy.im.message.MessageRepealHelper}） */
    private int status;

    public AppMessage() {
    }

    public int getConversationType() {
        return conversationType;
    }

    public void setConversationType(int conversationType) {
        this.conversationType = conversationType;
    }

    public ImSignalProtos.ConversationType getConversationTypeEnum() {
        return ImSignalProtos.ConversationType.forNumber(conversationType);
    }

    public void setConversationTypeEnum(ImSignalProtos.ConversationType conversationType) {
        this.conversationType = conversationType != null ? conversationType.getNumber() : 0;
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public String getClientMsgId() {
        return clientMsgId;
    }

    public void setClientMsgId(String clientMsgId) {
        this.clientMsgId = clientMsgId;
    }

    public int getMsgType() {
        return msgType;
    }

    public void setMsgType(int msgType) {
        this.msgType = msgType;
    }

    public String getSendType() {
        return sendType;
    }

    public void setSendType(String sendType) {
        this.sendType = sendType;
    }

    public int getMsgContentType() {
        return msgContentType;
    }

    public void setMsgContentType(int msgContentType) {
        this.msgContentType = msgContentType;
    }

    public String getFromId() {
        return fromId;
    }

    public void setFromId(String fromId) {
        this.fromId = fromId;
    }

    public String getToId() {
        return toId;
    }

    public void setToId(String toId) {
        this.toId = toId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getStatusReport() {
        return statusReport;
    }

    public void setStatusReport(int statusReport) {
        this.statusReport = statusReport;
    }

    public String getExtend() {
        return extend;
    }

    public void setExtend(String extend) {
        this.extend = extend;
    }


    public String getCustomInfo() {
        return customInfo;
    }

    public void setCustomInfo(String customInfo) {
        this.customInfo = customInfo;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public long getSequenceId() {
        return sequenceId;
    }

    public void setSequenceId(long sequenceId) {
        this.sequenceId = sequenceId;
    }

    public int getSendStatus() {
        return sendStatus;
    }

    public void setSendStatus(int sendStatus) {
        this.sendStatus = sendStatus;
    }

    public int getReadPersonCount() {
        return readPersonCount;
    }

    public void setReadPersonCount(int readPersonCount) {
        this.readPersonCount = readPersonCount;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    /**
     * 自己是否为发送方：{@code userId} 与 {@link #fromId} 相同则视为发送者。
     */
    public boolean isSender(@Nullable String userId) {
        return !TextUtils.isEmpty(userId)
                && !TextUtils.isEmpty(fromId)
                && TextUtils.equals(fromId, userId);
    }

    protected AppMessage(Parcel in) {
        conversationType = in.readInt();
        conversationId = in.readString();
        msgId = in.readString();
        clientMsgId = in.readString();
        msgType = in.readInt();
        sendType = in.readString();
        msgContentType = in.readInt();
        fromId = in.readString();
        toId = in.readString();
        timestamp = in.readLong();
        sequenceId = in.readLong();
        statusReport = in.readInt();
        extend = in.readString();
        customInfo = in.readString();
        extra = in.readString();
        content = in.readString();
        sendStatus = in.readInt();
        readPersonCount = in.readInt();
        status = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(conversationType);
        dest.writeString(conversationId);
        dest.writeString(msgId);
        dest.writeString(clientMsgId);
        dest.writeInt(msgType);
        dest.writeString(sendType);
        dest.writeInt(msgContentType);
        dest.writeString(fromId);
        dest.writeString(toId);
        dest.writeLong(timestamp);
        dest.writeLong(sequenceId);
        dest.writeInt(statusReport);
        dest.writeString(extend);
        dest.writeString(customInfo);
        dest.writeString(extra);
        dest.writeString(content);
        dest.writeInt(sendStatus);
        dest.writeInt(readPersonCount);
        dest.writeInt(status);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<AppMessage> CREATOR = new Creator<AppMessage>() {
        @Override
        public AppMessage createFromParcel(Parcel in) {
            return new AppMessage(in);
        }

        @Override
        public AppMessage[] newArray(int size) {
            return new AppMessage[size];
        }
    };

    @Override
    public String toString() {
        return "AppMessage{" +
                "conversationType=" + conversationType +
                ", msgId='" + msgId + '\'' +
                ", msgType=" + msgType +
                ", sendType='" + sendType + '\'' +
                ", msgContentType=" + msgContentType +
                ", fromId='" + fromId + '\'' +
                ", toId='" + toId + '\'' +
                ", timestamp=" + timestamp +
                ", statusReport=" + statusReport +
                ", extend='" + extend + '\'' +
                ", content='" + content + '\'' +
                ", sequenceId='" + sequenceId + '\'' +
                '}';
    }
}
