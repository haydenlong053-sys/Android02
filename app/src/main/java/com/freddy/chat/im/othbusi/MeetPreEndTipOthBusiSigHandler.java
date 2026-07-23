package com.freddy.chat.im.othbusi;

import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.cboda.baselibrary.utils.BaseLiveDataEventKt;
import com.cboda.baselibrary.utils.ELogUtilKt;
import com.freddy.im.signal.OthBusiSigCodes;
import com.freddy.im.signal.othbusi.OthBusiSigHandler;
import com.freddy.im.signal.othbusi.OthBusiSigJson;

/**
 * 会议倒计时提醒：code=6011，
 * data={"time":"可加时分钟","amount":"加时价格DUALX"}（对齐野火 MeetingPreEndTipMessageContent）。
 * 透传原始 data，由 UI 层解析并弹加时提醒。
 */
public final class MeetPreEndTipOthBusiSigHandler implements OthBusiSigHandler {

    private static final String TAG = "OthBusiSig.MeetPreEndTip";

    @Override
    public int code() {
        return OthBusiSigCodes.MEET_PRE_END_TIP;
    }

    @Override
    public boolean handle(@Nullable String data) {
        String time = OthBusiSigJson.readString(data, "time");
        if (TextUtils.isEmpty(time)) {
            return false;
        }
        ELogUtilKt.eLogD(TAG, "data=" + data);
        BaseLiveDataEventKt.getMeetPreEndTipMsgLiveData().postValue(data);
        return true;
    }
}
