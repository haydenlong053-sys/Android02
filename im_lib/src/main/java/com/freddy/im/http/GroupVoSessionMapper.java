package com.freddy.im.http;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.freddy.im.MessageType;
import com.freddy.im.http.model.im.GroupVo;
import com.freddy.im.http.model.im.SessionItem;

/**
 * 群详情 {@link GroupVo} → 会话 {@link SessionItem}。
 */
public final class GroupVoSessionMapper {

    private GroupVoSessionMapper() {
    }

    @NonNull
    public static SessionItem toSessionItem(@NonNull GroupVo group,
                                            @Nullable SessionItem existing,
                                            @Nullable String fallbackGroupId) {
        SessionItem item = new SessionItem();
        String groupId = !TextUtils.isEmpty(group.getId()) ? group.getId() : fallbackGroupId;
        if (groupId == null) {
            groupId = "";
        }
        item.setSessionId(groupId);
        item.setSessionName(group.getName());
        int conversation = group.getConversation();
        if (conversation <= 0) {
            conversation = MessageType.GROUP_CHAT.getMsgType();
        }
        item.setConversation(conversation);
        item.setIcon(group.getIcon());
        item.setMemCount(group.getMemCount());
        item.setMemChgTime(group.getMemChgTime());
        item.setOppositeUserId(group.getOppositeUserId());
        item.setOwerId(group.getOwerId());

        if (existing != null) {
            item.setUsername(existing.getUsername());
            item.setCreateTime(existing.getCreateTime());
            item.setUnreadCount(existing.getUnreadCount());
            item.setInternalFlag(existing.getInternalFlag());
            item.setTopNum(existing.getTopNum());
            item.setMuteNotify(existing.isMuteNotify());
            item.setReadedPosition(existing.getReadedPosition());
            item.setSelfReadPosition(existing.getSelfReadPosition());
            item.setBusi(existing.getBusi());
            item.setTags(existing.getTags());
            item.setPermission(existing.getPermission());
            item.setOppositeCompanyId(existing.getOppositeCompanyId());
            item.setExtAttrs(existing.getExtAttrs());
            item.setUserExtAttrs(existing.getUserExtAttrs());
            item.setLastMessageId(existing.getLastMessageId());
            item.setLastMessageContent(existing.getLastMessageContent());
            item.setLastContentType(existing.getLastContentType());
            item.setLastCustomInfo(existing.getLastCustomInfo());
            item.setLastMsgType(existing.getLastMsgType());
            item.setLastMessageTime(existing.getLastMessageTime());
            if (TextUtils.isEmpty(item.getSessionName()) && !TextUtils.isEmpty(existing.getSessionName())) {
                item.setSessionName(existing.getSessionName());
            }
            if (TextUtils.isEmpty(item.getIcon()) && !TextUtils.isEmpty(existing.getIcon())) {
                item.setIcon(existing.getIcon());
            }
            if (TextUtils.isEmpty(item.getOwerId()) && !TextUtils.isEmpty(existing.getOwerId())) {
                item.setOwerId(existing.getOwerId());
            }
            if (item.getMemChgTime() <= 0L && existing.getMemChgTime() > 0L) {
                item.setMemChgTime(existing.getMemChgTime());
            }
        }
        return item;
    }
}
