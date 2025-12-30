/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfirechat.message;

import static cn.wildfirechat.message.core.MessageContentType.ContentType_Meeting_Invite;

import android.os.Parcel;

import org.json.JSONException;
import org.json.JSONObject;

import cn.wildfirechat.message.core.ContentTag;
import cn.wildfirechat.message.core.MessagePayload;
import cn.wildfirechat.message.core.PersistFlag;

/**
 * Created by heavyrain lee on 2017/12/6.
 */

@ContentTag(type = ContentType_Meeting_Invite, flag = PersistFlag.Persist_And_Count)
public class MeetingInviteMessageContent extends MessageContent {


    private String meetingId;
    private String idno;
    private String title;
    private String desc;

    private boolean allMic;
    private boolean allMute;


    public MeetingInviteMessageContent() {
    }

    public MeetingInviteMessageContent(String meetingId, String idno, String title, String desc,  boolean audioOnly, boolean audience) {
        this.meetingId = meetingId;
        this.idno = idno;
        this.title = title;
        this.desc = desc;
        this.allMic = audioOnly;
        this.allMute = audience;
    }

    public String getMeetingId() {
        return meetingId;
    }

    public void setMeetingId(String meetingId) {
        this.meetingId = meetingId;
    }


    public boolean isAllMic() {
        return allMic;
    }

    public void setAllMic(boolean allMic) {
        this.allMic = allMic;
    }



    public String getIdno() {
        return idno;
    }

    public void setIdno(String idno) {
        this.idno = idno;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }



    public boolean isAllMute() {
        return allMute;
    }

    public void setAllMute(boolean allMute) {
        this.allMute = allMute;
    }




    @Override
    public MessagePayload encode() {
        MessagePayload payload = super.encode();
        payload.content = meetingId;

        try {
            JSONObject objWrite = new JSONObject();

            if (meetingId!=null){
                objWrite.put("meetingId",meetingId);
            }
            if (idno != null) {
                objWrite.put("idno", idno);
            }



            if (title != null) {
                objWrite.put("title", title);
            }

            if (desc != null) {
                objWrite.put("desc", desc);
            }

            objWrite.put("allMute", allMute ?1:0);

            objWrite.put("allMic", allMic ? 1 : 0);

            payload.binaryContent = objWrite.toString().getBytes();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return payload;
    }


    @Override
    public void decode(MessagePayload payload) {
        meetingId = payload.content;
        try {
            if (payload.binaryContent != null) {
                JSONObject jsonObject = new JSONObject(new String(payload.binaryContent));
                idno = jsonObject.optString("idno");
                title = jsonObject.optString("title");
                desc = jsonObject.optString("desc");
                allMute = jsonObject.optInt("allMute")>0;
                allMic = jsonObject.optInt("allMic") > 0;
                meetingId = jsonObject.optString("meetingId");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String digest(Message message) {
        return "[会议]";
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(meetingId);
        dest.writeString(idno !=null ? idno :"");
        dest.writeString(title!=null?title:"");
        dest.writeString(desc!=null?desc:"");
        dest.writeByte(allMic ? (byte) 1 : (byte) 0);
        dest.writeByte(allMute ? (byte) 1 : (byte) 0);
    }

    protected MeetingInviteMessageContent(Parcel in) {
        super(in);
        meetingId = in.readString();
        idno = in.readString();
        title = in.readString();
        desc = in.readString();

        allMic = in.readByte() != 0;
        allMute = in.readByte() != 0;
    }

    public static final Creator<MeetingInviteMessageContent> CREATOR = new Creator<MeetingInviteMessageContent>() {
        @Override
        public MeetingInviteMessageContent createFromParcel(Parcel source) {
            return new MeetingInviteMessageContent(source);
        }

        @Override
        public MeetingInviteMessageContent[] newArray(int size) {
            return new MeetingInviteMessageContent[size];
        }
    };
}
