package com.freddy.chat.im.othbusi;

import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.cboda.baselibrary.utils.BaseLiveDataEventKt;
import com.cboda.baselibrary.utils.ELogUtilKt;
import com.freddy.im.signal.OthBusiSigCodes;
import com.freddy.im.signal.othbusi.OthBusiSigHandler;
import com.freddy.im.signal.othbusi.OthBusiSigJson;

/** 会议结束：code=4020，data={"groupIdNo":"..."} */
public final class MeetEndOthBusiSigHandler implements OthBusiSigHandler {

    private static final String TAG = "OthBusiSig.MeetEnd";

    @Override
    public int code() {
        return OthBusiSigCodes.MEET_END;
    }

    @Override
    public boolean handle(@Nullable String data) {
        String groupIdNo = OthBusiSigJson.readGroupIdNo(data);
        if (TextUtils.isEmpty(groupIdNo)) {
            return false;
        }
        ELogUtilKt.eLogD(TAG, "groupIdNo=" + groupIdNo);
        BaseLiveDataEventKt.meetEndMsgLiveData.postValue(groupIdNo);
        return true;
    }
}
