package com.freddy.chat.im.othbusi;

import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.cboda.baselibrary.utils.BaseLiveDataEventKt;
import com.cboda.baselibrary.utils.ELogUtilKt;
import com.freddy.im.signal.OthBusiSigCodes;
import com.freddy.im.signal.othbusi.OthBusiSigHandler;
import com.freddy.im.signal.othbusi.OthBusiSigJson;

/**
 * 麦位变更：code=4040，
 * data={"refreshType":"micSeat","groupId":"..."}（对齐野火 MeetingSeatChangeMessageContent）。
 */
public final class MeetSeatChangeOthBusiSigHandler implements OthBusiSigHandler {

    private static final String TAG = "OthBusiSig.MeetSeatChange";
    private static final String REFRESH_MIC_SEAT = "micSeat";

    @Override
    public int code() {
        return OthBusiSigCodes.MEET_SEAT_CHANGE;
    }

    @Override
    public boolean handle(@Nullable String data) {
        String groupIdNo = OthBusiSigJson.readGroupId(data);
        if (TextUtils.isEmpty(groupIdNo)) {
            return false;
        }
        String refreshType = OthBusiSigJson.readString(data, "refreshType");
        if (!TextUtils.isEmpty(refreshType) && !REFRESH_MIC_SEAT.equals(refreshType)) {
            ELogUtilKt.eLogD(TAG, "ignore refreshType=" + refreshType);
            return false;
        }
        ELogUtilKt.eLogD(TAG, "groupId=" + groupIdNo);
        BaseLiveDataEventKt.getMeetSeatChangeMsgLiveData().postValue(groupIdNo);
        return true;
    }
}
