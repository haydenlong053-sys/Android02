/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfirechat.message;

import static com.cboda.baselibrary.utils.ELogUtilKt.APP_TAG;
import static cn.wildfirechat.message.core.MessageContentBroadType.MESSAGE_CONTENT_TYPE_MEET_SEAT_CHANGE;

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
 * payload.pushData--->{"refreshType":"micSeat","groupIdNo":"1fd4d614470c41659fb1ce7a9ef82185"}
 */

@ContentTag(type = MESSAGE_CONTENT_TYPE_MEET_SEAT_CHANGE, flag = PersistFlag.Transparent)
public class MeetingSeatChangeMessageContent extends MessageContent {

    public String refreshType;
    public String groupIdNo;


    public MeetingSeatChangeMessageContent() {
    }

    public MeetingSeatChangeMessageContent(String refreshType, String groupIdNo) {
        this.refreshType = refreshType;
        this.groupIdNo = groupIdNo;
    }

    @Override
    public MessagePayload encode() {
        MessagePayload payload = super.encode();
        try {
            JSONObject objWrite = new JSONObject();

            if (refreshType !=null){
                objWrite.put("refreshType", refreshType);
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
                refreshType = jsonObject.optString("refreshType");
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
        dest.writeString(refreshType != null ? refreshType : "");
        dest.writeString(groupIdNo != null ? groupIdNo : "");
    }

    protected MeetingSeatChangeMessageContent(Parcel in) {
        super(in);
        refreshType = in.readString();
        groupIdNo = in.readString();
    }

    public static final Creator<MeetingSeatChangeMessageContent> CREATOR = new Creator<MeetingSeatChangeMessageContent>() {
        @Override
        public MeetingSeatChangeMessageContent createFromParcel(Parcel source) {
            return new MeetingSeatChangeMessageContent(source);
        }

        @Override
        public MeetingSeatChangeMessageContent[] newArray(int size) {
            return new MeetingSeatChangeMessageContent[size];
        }
    };
}
