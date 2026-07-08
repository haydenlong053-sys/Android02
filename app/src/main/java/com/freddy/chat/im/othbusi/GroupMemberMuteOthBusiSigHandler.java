package com.freddy.chat.im.othbusi;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;

import com.cboda.baselibrary.utils.ELogUtilKt;
import com.freddy.chat.im.MessageProcessor;
import com.freddy.im.signal.OthBusiSigCodes;
import com.freddy.im.signal.othbusi.OthBusiSigHandler;
import com.freddy.im.signal.othbusi.OthBusiSigJson;

/**
 * 指定成员禁言变更：code=1003（推送给被操作成员），
 * data={"groupId":"...","mute":0|1,"operator":"..."}。
 */
public final class GroupMemberMuteOthBusiSigHandler implements OthBusiSigHandler {

    private static final String TAG = "OthBusiSig.GroupMemberMute";

    @Override
    public int code() {
        return OthBusiSigCodes.GROUP_MEMBER_MUTE;
    }

    @Override
    public boolean handle(@Nullable String data) {
        String groupId = OthBusiSigJson.readGroupId(data);
        int mute = OthBusiSigJson.readInt(data, "mute", -1);
        if (TextUtils.isEmpty(groupId) || (mute != 0 && mute != 1)) {
            return false;
        }
        ELogUtilKt.eLogD(TAG, "groupId=" + groupId + " mute=" + mute);
        MessageProcessor.getInstance().notifyGroupMemberMuteUpdated(groupId, mute);
        return true;
    }
}
