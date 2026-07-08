package com.freddy.chat.im.othbusi;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cboda.baselibrary.utils.ELogUtilKt;
import com.freddy.chat.im.MessageProcessor;
import com.freddy.im.signal.OthBusiSigCodes;
import com.freddy.im.signal.othbusi.OthBusiSigHandler;
import com.freddy.im.signal.othbusi.OthBusiSigJson;

/** 群公告变更：code=1001，data={"groupId":"..."} */
public final class GroupNoticeOthBusiSigHandler implements OthBusiSigHandler {

    private static final String TAG = "OthBusiSig.GroupNotice";

    @Override
    public int code() {
        return OthBusiSigCodes.GROUP_NOTICE;
    }

    @Override
    public boolean handle(@Nullable String data) {
        String groupId = OthBusiSigJson.readGroupId(data);
        if (TextUtils.isEmpty(groupId)) {
            return false;
        }
        ELogUtilKt.eLogD(TAG, "groupId=" + groupId);
        MessageProcessor.getInstance().notifyGroupNoticeUpdated(groupId);
        return true;
    }
}
