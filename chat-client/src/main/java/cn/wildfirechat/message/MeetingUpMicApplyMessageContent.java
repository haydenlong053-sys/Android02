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

    private String userId;
    private String idno;


    public MeetingUpMicApplyMessageContent() {
    }

    public MeetingUpMicApplyMessageContent(String userId, String idno) {
        this.userId = userId;
        this.idno = idno;
    }

    @Override
    public MessagePayload encode() {
        MessagePayload payload = super.encode();
        try {
            JSONObject objWrite = new JSONObject();

            if (userId!=null){
                objWrite.put("userId",userId);
            }
            if (idno != null) {
                objWrite.put("idno", idno);
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
                userId = jsonObject.optString("userId");
                idno = jsonObject.optString("idno");
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
        dest.writeString(userId != null ? userId : "");
        dest.writeString(idno != null ? idno : "");
    }

    protected MeetingUpMicApplyMessageContent(Parcel in) {
        super(in);
        userId = in.readString();
        idno = in.readString();
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
