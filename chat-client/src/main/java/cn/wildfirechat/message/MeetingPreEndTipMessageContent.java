/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfirechat.message;

import static cn.wildfirechat.message.core.MessageContentBroadType.MESSAGE_CONTENT_TYPE_SYS_MEETING_PRE_TIP;

import android.os.Parcel;

import androidx.core.content.ContextCompat;

import org.json.JSONException;
import org.json.JSONObject;

import cn.wildfirechat.message.core.ContentTag;
import cn.wildfirechat.message.core.MessagePayload;
import cn.wildfirechat.message.core.PersistFlag;

/**
 * Created by heavyrain lee on 2017/12/6.
 * 后端在线主动推的会议即将结束的提醒
 */

@ContentTag(type = MESSAGE_CONTENT_TYPE_SYS_MEETING_PRE_TIP, flag = PersistFlag.Transparent)
public class MeetingPreEndTipMessageContent extends MessageContent {
//    private int intervalNumber;
//    private int intervalUnit;

    private String tipStr;


    public MeetingPreEndTipMessageContent() {
    }

    public String getTipStr() {
        return tipStr;
    }

    @Override
    public MessagePayload encode() {
        MessagePayload payload = super.encode();
        payload.pushData = "";

        return payload;
    }


    @Override
    public void decode(MessagePayload payload) {
//        Log.d("zhanglong","id-->"+id+"------------payload.pushData--->"+ payload.pushData);
        if (payload.pushData != null) {
            tipStr =payload.pushData;

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

    protected MeetingPreEndTipMessageContent(Parcel in) {
        super(in);
//        id = in.readString();
    }

    public static final Creator<MeetingPreEndTipMessageContent> CREATOR = new Creator<MeetingPreEndTipMessageContent>() {
        @Override
        public MeetingPreEndTipMessageContent createFromParcel(Parcel source) {
            return new MeetingPreEndTipMessageContent(source);
        }

        @Override
        public MeetingPreEndTipMessageContent[] newArray(int size) {
            return new MeetingPreEndTipMessageContent[size];
        }
    };
}
