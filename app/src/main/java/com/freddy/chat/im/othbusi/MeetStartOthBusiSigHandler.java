package com.freddy.chat.im.othbusi;

import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.cboda.baselibrary.utils.BaseLiveDataEventKt;
import com.cboda.baselibrary.utils.ELogUtilKt;
import com.freddy.im.signal.OthBusiSigCodes;
import com.freddy.im.signal.othbusi.OthBusiSigHandler;
import com.freddy.im.signal.othbusi.OthBusiSigJson;

/** 会议开始：code=4010，data={"groupIdNo":"..."} */
public final class MeetStartOthBusiSigHandler implements OthBusiSigHandler {

    private static final String TAG = "OthBusiSig.MeetStart";

    @Override
    public int code() {
        return OthBusiSigCodes.MEET_START;
    }

    @Override
    public boolean handle(@Nullable String data) {
        String groupIdNo = OthBusiSigJson.readGroupIdNo(data);
        if (TextUtils.isEmpty(groupIdNo)) {
            return false;
        }
        ELogUtilKt.eLogD(TAG, "groupIdNo=" + groupIdNo);
        BaseLiveDataEventKt.getMeetJoinMsgLiveData().postValue(groupIdNo);
        return true;
    }
}
