package com.freddy.chat.im.othbusi;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;

import com.freddy.chat.im.MessageProcessor;
import com.freddy.im.signal.OthBusiSigCodes;
import com.freddy.im.signal.othbusi.OthBusiSigHandler;
import com.freddy.im.signal.othbusi.OthBusiSigJson;

/**
 * 全员禁言变更：code=1002，
 * data={"allMute":0,"groupId":"...","operator":"..."}。
 */
public final class GroupAllMuteOthBusiSigHandler implements OthBusiSigHandler {

    private static final String TAG = "OthBusiSig.GroupAllMute";

    @Override
    public int code() {
        return OthBusiSigCodes.GROUP_ALL_MUTE;
    }

    @Override
    public boolean handle(@Nullable String data) {
        String groupId = OthBusiSigJson.readGroupId(data);
        int allMute = OthBusiSigJson.readInt(data, "allMute", -1);
        if (TextUtils.isEmpty(groupId) || (allMute != 0 && allMute != 1)) {
            return false;
        }
        Log.d(TAG, "groupId=" + groupId + " allMute=" + allMute);
        MessageProcessor.getInstance().notifyGroupMuteUpdated(groupId, allMute);
        return true;
    }
}
