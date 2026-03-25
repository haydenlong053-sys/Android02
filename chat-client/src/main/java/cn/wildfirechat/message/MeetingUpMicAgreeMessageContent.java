/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfirechat.message;

import static com.cboda.baselibrary.utils.ELogUtilKt.APP_TAG;
import static cn.wildfirechat.message.core.MessageContentBroadType.MESSAGE_CONTENT_TYPE_MEET_UP_MIC_AGREE;
import static cn.wildfirechat.message.core.MessageContentBroadType.MESSAGE_CONTENT_TYPE_MEET_UP_MIC_APPLY;

import android.os.Parcel;

import com.cboda.baselibrary.utils.ELogUtilKt;

import org.json.JSONObject;

import cn.wildfirechat.message.core.ContentTag;
import cn.wildfirechat.message.core.MessagePayload;
import cn.wildfirechat.message.core.PersistFlag;

/**
 * Created by heavyrain lee on 2017/12/6.
 * 后端在线主动推的会议同意上麦
 */

@ContentTag(type = MESSAGE_CONTENT_TYPE_MEET_UP_MIC_AGREE, flag = PersistFlag.Transparent)
public class MeetingUpMicAgreeMessageContent extends MessageContent {
//    private int intervalNumber;
//    private int intervalUnit;

    private String time;
    private String amount;


    public MeetingUpMicAgreeMessageContent() {
    }


    @Override
    public MessagePayload encode() {
        MessagePayload payload = super.encode();
        payload.pushData = "";

        return payload;
    }

    public String getTime() {
        return time;
    }

    public String getAmount() {
        return amount;
    }

    @Override
    public void decode(MessagePayload payload) {
        ELogUtilKt.eLogD(APP_TAG,"------------payload.pushData--->"+ payload.pushData);

        try {
            if (payload.pushData != null) {
                JSONObject jsonObject = new JSONObject(payload.pushData);
                time = jsonObject.optString("time");
                amount = jsonObject.optString("amount");
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
//        dest.writeString(id != null ? id : "");
    }

    protected MeetingUpMicAgreeMessageContent(Parcel in) {
        super(in);
//        id = in.readString();
    }

    public static final Creator<MeetingUpMicAgreeMessageContent> CREATOR = new Creator<MeetingUpMicAgreeMessageContent>() {
        @Override
        public MeetingUpMicAgreeMessageContent createFromParcel(Parcel source) {
            return new MeetingUpMicAgreeMessageContent(source);
        }

        @Override
        public MeetingUpMicAgreeMessageContent[] newArray(int size) {
            return new MeetingUpMicAgreeMessageContent[size];
        }
    };
}
