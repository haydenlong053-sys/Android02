package com.freddy.chat.im.othbusi;

import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.cboda.baselibrary.utils.BaseLiveDataEventKt;
import com.cboda.baselibrary.utils.ELogUtilKt;
import com.freddy.im.signal.OthBusiSigCodes;
import com.freddy.im.signal.othbusi.OthBusiSigHandler;
import com.freddy.im.signal.othbusi.OthBusiSigJson;

/** 会议开始：code=4010，data={"groupId":"...","meetingId":"...","roomName":"..."} */
public final class MeetStartOthBusiSigHandler implements OthBusiSigHandler {

    private static final String TAG = "OthBusiSig.MeetStart";

    @Override
    public int code() {
        return OthBusiSigCodes.MEET_START;
    }

    @Override
    public boolean handle(@Nullable String data) {
        String groupIdNo = OthBusiSigJson.readGroupId(data);
        if (TextUtils.isEmpty(groupIdNo)) {
            return false;
        }
        String meetingId = OthBusiSigJson.readString(data, "meetingId");
        String roomName = OthBusiSigJson.readString(data, "roomName");
        String title = OthBusiSigJson.readString(data, "title");
        if (!TextUtils.isEmpty(meetingId)) {
            com.freddy.im.meeting.GroupMeetSessionStore.put(groupIdNo, meetingId, roomName, title);
        }
        ELogUtilKt.eLogD(TAG, "groupId=" + groupIdNo + " meetingId=" + meetingId);
        BaseLiveDataEventKt.getMeetJoinMsgLiveData().postValue(groupIdNo);
        return true;
    }
}
