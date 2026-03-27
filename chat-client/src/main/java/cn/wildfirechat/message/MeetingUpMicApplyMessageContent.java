/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfirechat.message;

import static com.cboda.baselibrary.utils.ELogUtilKt.APP_TAG;
import static cn.wildfirechat.message.core.MessageContentBroadType.MESSAGE_CONTENT_TYPE_MEET_UP_MIC_APPLY;

import android.os.Parcel;

import com.cboda.baselibrary.utils.ELogUtilKt;

import org.json.JSONException;
import org.json.JSONObject;

import cn.wildfirechat.message.core.ContentTag;
import cn.wildfirechat.message.core.MessagePayload;
import cn.wildfirechat.message.core.PersistFlag;

/**
 * Created by heavyrain lee on 2017/12/6.
 * 后端在线主动推的会议上麦申请
 */

@ContentTag(type = MESSAGE_CONTENT_TYPE_MEET_UP_MIC_APPLY, flag = PersistFlag.Transparent)
public class MeetingUpMicApplyMessageContent extends MessageContent {

    public String applicantUserId;
    public String groupIdNo;


    public MeetingUpMicApplyMessageContent() {
    }

    public MeetingUpMicApplyMessageContent(String userId, String groupIdNo) {
        this.applicantUserId = userId;
        this.groupIdNo = groupIdNo;
    }

    @Override
    public MessagePayload encode() {
        MessagePayload payload = super.encode();
        try {
            JSONObject objWrite = new JSONObject();

            if (applicantUserId !=null){
                objWrite.put("applicantUserId", applicantUserId);
            }
            if (groupIdNo != null) {
                objWrite.put("groupIdNo", groupIdNo);
            }

            payload.pushData = objWrite.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return payload;
    }

    @Override
    public void decode(MessagePayload payload) {
        ELogUtilKt.eLogD(APP_TAG,"------------payload.pushData--->"+ payload.pushData);

        try {
            if (payload.pushData != null) {
                JSONObject jsonObject = new JSONObject(payload.pushData);
                applicantUserId = jsonObject.optString("applicantUserId");
                groupIdNo = jsonObject.optString("groupIdNo");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String digest(Message message) {
        return "";
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(applicantUserId != null ? applicantUserId : "");
        dest.writeString(groupIdNo != null ? groupIdNo : "");
    }

    protected MeetingUpMicApplyMessageContent(Parcel in) {
        super(in);
        applicantUserId = in.readString();
        groupIdNo = in.readString();
    }

    public static final Creator<MeetingUpMicApplyMessageContent> CREATOR = new Creator<MeetingUpMicApplyMessageContent>() {
        @Override
        public MeetingUpMicApplyMessageContent createFromParcel(Parcel source) {
            return new MeetingUpMicApplyMessageContent(source);
        }

        @Override
        public MeetingUpMicApplyMessageContent[] newArray(int size) {
            return new MeetingUpMicApplyMessageContent[size];
        }
    };
}
