package com.freddy.chat.im.othbusi;

import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.cboda.baselibrary.utils.ELogUtilKt;
import com.freddy.chat.im.MessageProcessor;
import com.freddy.im.message.GroupPinnedSyncHelper;
import com.freddy.im.signal.OthBusiSigCodes;
import com.freddy.im.signal.othbusi.OthBusiSigHandler;
import com.freddy.im.signal.othbusi.OthBusiSigJson;

/**
 * 群置顶变更：code=1010，
 * data={"kind":"group_pin","groupId":"...","action":"pin|unpin","messageId":"...","operator":"..."}。
 * 收到后重拉 pinnedMessages。
 */
public final class GroupPinOthBusiSigHandler implements OthBusiSigHandler {

    private static final String TAG = "OthBusiSig.GroupPin";

    @Override
    public int code() {
        return OthBusiSigCodes.GROUP_PIN;
    }

    @Override
    public boolean handle(@Nullable String data) {
        if (TextUtils.isEmpty(data)) {
            return false;
        }
        String kind = OthBusiSigJson.readString(data, "kind");
        if (!TextUtils.isEmpty(kind) && !"group_pin".equals(kind)) {
            ELogUtilKt.eLogD(TAG, "ignore kind=" + kind);
            return false;
        }
        String groupId = OthBusiSigJson.readGroupId(data);
        if (TextUtils.isEmpty(groupId)) {
            return false;
        }
        ELogUtilKt.eLogD(TAG, "groupId=" + groupId
                + " action=" + OthBusiSigJson.readString(data, "action"));
        GroupPinnedSyncHelper.syncFromServer(groupId, (g, list) ->
                MessageProcessor.getInstance().notifyGroupPinnedUpdated(g));
        return true;
    }
}
