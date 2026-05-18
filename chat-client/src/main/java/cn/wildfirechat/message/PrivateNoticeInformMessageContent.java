/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfirechat.message;

import static com.cboda.baselibrary.utils.ViewExpandUtilKt.getAppContext;
import static cn.wildfirechat.message.core.MessageContentBroadType.MESSAGE_CONTENT_TYPE_SYS_NOTICE_INFORM;
import static cn.wildfirechat.message.core.MessageContentBroadType.MESSAGE_CONTENT_TYPE_SYS_NOTICE_INFORM_PRIVATE;

import android.os.Parcel;

import androidx.core.content.ContextCompat;

import org.json.JSONException;
import org.json.JSONObject;

import cn.wildfirechat.message.core.ContentTag;
import cn.wildfirechat.message.core.MessagePayload;
import cn.wildfirechat.message.core.PersistFlag;

/**
 * Created by heavyrain lee on 2017/12/6.
 * 后端在线主动推的系统公告消息--指定人
 */

@ContentTag(type = MESSAGE_CONTENT_TYPE_SYS_NOTICE_INFORM_PRIVATE, flag = PersistFlag.Transparent)
public class PrivateNoticeInformMessageContent extends MessageContent {
    private String contentUrl;
    //    private String endTime;
    private String id;
        private String imageUrl;
//    private int intervalNumber;
//    private int intervalUnit;

//    private String lang;
    private String noticeTitle;
    private String previewContent;
        private int noticeType;
//    private String startTime;
    private String createTime;
    private boolean status;
    private boolean forcePopup;
    private boolean isRead;



    public PrivateNoticeInformMessageContent() {
    }

//    public NoticeInformMessageContent(String meetingId, String idno, String title, String desc, boolean audioOnly, boolean audience) {
//        this.meetingId = meetingId;
//        this.idno = idno;
//        this.title = title;
//        this.desc = desc;
//        this.status = audioOnly;
//        this.allMute = audience;
//    }


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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
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

    public int getNoticeType() {
        return noticeType;
    }

    public void setNoticeType(int noticeType) {
        this.noticeType = noticeType;
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

    public boolean isForcePopup() {
        return forcePopup;
    }

    public void setForcePopup(boolean forcePopup) {
        this.forcePopup = forcePopup;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    @Override
    public MessagePayload encode() {
        MessagePayload payload = super.encode();
        payload.content = id;

        return payload;
    }


    @Override
    public void decode(MessagePayload payload) {
        id = payload.content;
//        Log.d(APP_TAG,"id-->"+id+"------------payload.pushData--->"+ payload.pushData);
        try {
            if (payload.pushData != null) {
                JSONObject jsonObject = new JSONObject(payload.pushData);
                contentUrl = jsonObject.optString("contentUrl");
                id = jsonObject.optString("id");
                imageUrl = jsonObject.optString("imageUrl");
                createTime = jsonObject.optString("createTime");
                noticeTitle = jsonObject.optString("noticeTitle");
                previewContent = jsonObject.optString("previewContent");
                status = jsonObject.optBoolean("status",false);
                forcePopup = jsonObject.optBoolean("forcePopup",false);
                isRead = jsonObject.optBoolean("isRead",false) ;

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String digest(Message message) {
        return ContextCompat.getString(getAppContext(null), com.cboda.baselibrary.R.string.base_digest_notice);
//        return "[公告]";
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
        dest.writeString(imageUrl);
        dest.writeString(createTime != null ? createTime : "");
        dest.writeString(noticeTitle != null ? noticeTitle : "");
        dest.writeString(previewContent != null ? previewContent : "");
        dest.writeByte(status ? (byte) 1 : (byte) 0);
        dest.writeByte(forcePopup ? (byte) 1 : (byte) 0);
        dest.writeByte(isRead ? (byte) 1 : (byte) 0);
    }

    protected PrivateNoticeInformMessageContent(Parcel in) {
        super(in);
        contentUrl = in.readString();
        id = in.readString();
        imageUrl = in.readString();
        createTime = in.readString();
        noticeTitle = in.readString();
        previewContent = in.readString();
        status = in.readByte() != 0;
        forcePopup = in.readByte() != 0;
        isRead = in.readByte() != 0;
    }

    public static final Creator<PrivateNoticeInformMessageContent> CREATOR = new Creator<PrivateNoticeInformMessageContent>() {
        @Override
        public PrivateNoticeInformMessageContent createFromParcel(Parcel source) {
            return new PrivateNoticeInformMessageContent(source);
        }

        @Override
        public PrivateNoticeInformMessageContent[] newArray(int size) {
            return new PrivateNoticeInformMessageContent[size];
        }
    };
}
