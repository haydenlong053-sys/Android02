/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfirechat.message;

import static cn.wildfirechat.message.core.MessageContentBroadType.MESSAGE_CONTENT_TYPE_SYS_NOTICE;

import android.os.Parcel;

import org.json.JSONException;
import org.json.JSONObject;

import cn.wildfirechat.message.core.ContentTag;
import cn.wildfirechat.message.core.MessagePayload;
import cn.wildfirechat.message.core.PersistFlag;

/**
 * Created by heavyrain lee on 2017/12/6.
 * registerMessageViewHolder(NoticeContentViewBroadHolder.class, R.layout.conversation_item_msg_notice_broad_send, R.layout.conversation_item_msg_notice_broad_receive);
 *  消息公告内部自定义消息
 */

@ContentTag(type = MESSAGE_CONTENT_TYPE_SYS_NOTICE, flag = PersistFlag.Persist_And_Count)
public class NoticeMessageContent extends MessageContent {
    private String contentUrl;
    //    private String endTime;
//    private boolean forcePopup;
    private String id;
    //    private String imageUrl;
//    private int intervalNumber;
//    private int intervalUnit;
//    private boolean isRead;
//    private String lang;
    private String noticeTitle;
    private String previewContent;
    //    private int noticeType;
//    private String startTime;
    private String createTime;
    private boolean status;


    public NoticeMessageContent() {
    }

    public NoticeMessageContent(String contentUrl, String id, String createTime, String noticeTitle, String previewContent, boolean status) {
        this.contentUrl = contentUrl;
        this.id = id;
        this.createTime = createTime;
        this.noticeTitle = noticeTitle;
        this.previewContent = previewContent;
        this.status = status;
    }


    public String getContentUrl() {
        return contentUrl;
    }

    public void setContentUrl(String contentUrl) {
        this.contentUrl = contentUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNoticeTitle() {
        return noticeTitle;
    }

    public void setNoticeTitle(String noticeTitle) {
        this.noticeTitle = noticeTitle;
    }

    public String getPreviewContent() {
        return previewContent;
    }

    public void setPreviewContent(String previewContent) {
        this.previewContent = previewContent;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    @Override
    public MessagePayload encode() {
        MessagePayload payload = super.encode();
        payload.content = id;
        try {
            JSONObject objWrite = new JSONObject();
//            if (id!=null){
//                objWrite.put("id",id);
//            }
            if (contentUrl != null) {
                objWrite.put("contentUrl", contentUrl);
            }

            if (createTime != null) {
                objWrite.put("createTime", createTime);
            }
            if (noticeTitle != null) {
                objWrite.put("noticeTitle", noticeTitle);
            }
            if (previewContent != null) {
                objWrite.put("previewContent", previewContent);
            }

            objWrite.put("status", status ? 1 : 0);

            payload.binaryContent = objWrite.toString().getBytes();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return payload;
    }


    @Override
    public void decode(MessagePayload payload) {
        id = payload.content;
        try {
            if (payload.binaryContent != null) {
                JSONObject jsonObject = new JSONObject(new String(payload.binaryContent));
                contentUrl = jsonObject.optString("contentUrl");
                createTime = jsonObject.optString("createTime");
                noticeTitle = jsonObject.optString("noticeTitle");
                previewContent = jsonObject.optString("previewContent");
                status = jsonObject.optInt("status") > 0;
//                id = jsonObject.optString("id");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String digest(Message message) {
        return "[公告]";
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(contentUrl != null ? contentUrl : "");
        dest.writeString(id);
        dest.writeString(createTime != null ? createTime : "");
        dest.writeString(noticeTitle != null ? noticeTitle : "");
        dest.writeString(previewContent != null ? previewContent : "");
        dest.writeByte(status ? (byte) 1 : (byte) 0);
    }

    protected NoticeMessageContent(Parcel in) {
        super(in);
        contentUrl = in.readString();
        id = in.readString();
        createTime = in.readString();
        noticeTitle = in.readString();
        previewContent = in.readString();
        status = in.readByte() != 0;
    }

    public static final Creator<NoticeMessageContent> CREATOR = new Creator<NoticeMessageContent>() {
        @Override
        public NoticeMessageContent createFromParcel(Parcel source) {
            return new NoticeMessageContent(source);
        }

        @Override
        public NoticeMessageContent[] newArray(int size) {
            return new NoticeMessageContent[size];
        }
    };
}
