package com.freddy.chat.im.othbusi;

import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.cboda.baselibrary.utils.BaseLiveDataEventKt;
import com.cboda.baselibrary.utils.ELogUtilKt;
import com.freddy.im.signal.OthBusiSigCodes;
import com.freddy.im.signal.othbusi.OthBusiSigHandler;
import com.freddy.im.signal.othbusi.OthBusiSigJson;

/**
 * 上麦申请：code=4030，
 * data={"applicantUserId":"...","groupId":"..."}（对齐野火 MeetingUpMicApplyMessageContent）。
 */
public final class MeetUpMicApplyOthBusiSigHandler implements OthBusiSigHandler {

    private static final String TAG = "OthBusiSig.MeetUpMicApply";

    @Override
    public int code() {
        return OthBusiSigCodes.MEET_UP_MIC_APPLY;
    }

    @Override
    public boolean handle(@Nullable String data) {
        String groupIdNo = OthBusiSigJson.readGroupId(data);
        String applicantUserId = OthBusiSigJson.readString(data, "applicantUserId");
        if (TextUtils.isEmpty(groupIdNo) || TextUtils.isEmpty(applicantUserId)) {
            return false;
        }
        ELogUtilKt.eLogD(TAG, "groupId=" + groupIdNo + " applicant=" + applicantUserId);
        BaseLiveDataEventKt.getMeetUpMicMsgLiveData().postValue(groupIdNo);
        return true;
    }
}
