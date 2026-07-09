package com.freddy.chat.im.othbusi;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;

import com.cboda.baselibrary.utils.ELogUtilKt;
import com.freddy.chat.im.MessageProcessor;
import com.freddy.im.db.ImLocalStore;
import com.freddy.im.group.GroupRole;
import com.freddy.im.signal.OthBusiSigCodes;
import com.freddy.im.signal.othbusi.OthBusiSigHandler;
import com.freddy.im.signal.othbusi.OthBusiSigJson;

import java.util.List;

/**
 * 群管理员角色变更：code=1004，
 * data={"groupId":"...","userIdList":[...],"newRole":1|2}（1 管理员，2 普通成员）。
 */
public final class GroupManagerRoleOthBusiSigHandler implements OthBusiSigHandler {

    private static final String TAG = "OthBusiSig.GroupManagerRole";

    @Override
    public int code() {
        return OthBusiSigCodes.GROUP_MANAGER_ROLE;
    }

    @Override
    public boolean handle(@Nullable String data) {
        String groupId = OthBusiSigJson.readGroupId(data);
        List<String> userIdList = OthBusiSigJson.readStringList(data, "userIdList");
        int newRole = OthBusiSigJson.readInt(data, "newRole", -1);
        if (TextUtils.isEmpty(groupId) || userIdList.isEmpty()
                || (newRole != GroupRole.ADMIN && newRole != GroupRole.MEMBER)) {
            return false;
        }
        ELogUtilKt.eLogD(TAG, "groupId=" + groupId + " newRole=" + newRole + " users=" + userIdList.size());
        ImLocalStore.updateGroupMemberRolesThen(groupId, userIdList, newRole, null);
        //先注释，此处暂时不发notifyGroupMemberRolesUpdated的更新ui通知
//        ImLocalStore.updateGroupMemberRolesThen(groupId, userIdList, newRole, () ->
//                MessageProcessor.getInstance().notifyGroupMemberRolesUpdated(groupId));
        return true;
    }
}
