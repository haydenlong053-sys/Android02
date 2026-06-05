package com.freddy.im.http;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.freddy.im.http.model.im.ConversationLastMsgMap;
import com.freddy.im.http.model.im.ImMessageItem;
import com.freddy.im.http.model.im.SessionItem;

import java.util.ArrayList;
import java.util.List;

/**
 * 将 {@code getLastMsg} 结果合并进 {@link SessionItem}。
 */
public final class SessionLastMsgMerge {

    private SessionLastMsgMerge() {
    }

    public static void apply(@Nullable List<SessionItem> sessions,
                             @Nullable ConversationLastMsgMap lastMsgMap) {
        if (sessions == null || sessions.isEmpty() || lastMsgMap == null || lastMsgMap.isEmpty()) {
            return;
        }
        for (SessionItem item : sessions) {
            if (item == null || TextUtils.isEmpty(item.getSessionId())) {
                continue;
            }
            ImMessageItem last = lastMsgMap.get(item.getSessionId());
            if (last == null) {
                continue;
            }
            if (!TextUtils.isEmpty(last.getMessageId())) {
                item.setLastMessageId(last.getMessageId());
            }
            if (!TextUtils.isEmpty(last.getDesc())) {
                item.setLastMessageContent(last.getDesc());
            }
            if (!TextUtils.isEmpty(last.getContentType())) {
                item.setLastContentType(last.getContentType());
            }
            if (!TextUtils.isEmpty(last.getCustomInfo())) {
                item.setLastCustomInfo(last.getCustomInfo());
            }
            item.setLastMsgType(last.getMsgType());
        }
    }


    @NonNull
    public static List<String> collectSessionIds(@Nullable List<SessionItem> sessions) {
        List<String> ids = new ArrayList<>();
        if (sessions == null) {
            return ids;
        }
        for (SessionItem item : sessions) {
            if (item != null && !TextUtils.isEmpty(item.getSessionId())) {
                ids.add(item.getSessionId());
            }
        }
        return ids;
    }
}
