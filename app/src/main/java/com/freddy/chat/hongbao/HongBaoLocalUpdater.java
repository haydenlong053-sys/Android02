package com.freddy.chat.hongbao;

import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.freddy.chat.event.CEventCenter;
import com.freddy.chat.event.Events;
import com.freddy.chat.utils.HongBaoMessageContentUtil;
import com.freddy.im.db.ImLocalStore;
import com.freddy.im.db.repository.ImMessageRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * 自研本地红包 content 状态 patch，并派发 {@link Events#CHAT_MESSAGE_UPDATE}。
 */
public final class HongBaoLocalUpdater {

    private HongBaoLocalUpdater() {
    }

    public static void applyLingquStatus(@Nullable String msgIdOrClientMsgId,
                                         @Nullable String lingquStatus) {
        applyLingquStatus(msgIdOrClientMsgId, lingquStatus, null);
    }

    /**
     * @param lqUsers 为 null 时保留原 lqUsers；非 null 则整体替换
     */
    public static void applyLingquStatus(@Nullable String msgIdOrClientMsgId,
                                         @Nullable String lingquStatus,
                                         @Nullable List<String> lqUsers) {
        if (TextUtils.isEmpty(msgIdOrClientMsgId) || TextUtils.isEmpty(lingquStatus)) {
            return;
        }
        final String key = msgIdOrClientMsgId.trim();
        ImMessageRepository.getInstance().findMessageByMsgIdOrClientMsgId(key, found -> {
            if (found == null) {
                return;
            }
            List<String> users = lqUsers;
            if (users == null) {
                users = new ArrayList<>(HongBaoMessageContentUtil.resolveLqUsers(found.getContent()));
            }
            String patched = HongBaoMessageContentUtil.patchLingquStatus(
                    found.getContent(), lingquStatus, users);
            if (patched == null) {
                return;
            }
            ImLocalStore.updateMessageContentById(key, patched, updated -> {
                if (updated != null) {
                    CEventCenter.dispatchEvent(Events.CHAT_MESSAGE_UPDATE, 0, 0, updated);
                }
            });
        });
    }

    /** 领取成功后把当前用户写入 lqUsers，并将 lingquStatus 置为 1。 */
    public static void markClaimedByUser(@Nullable String msgIdOrClientMsgId,
                                         @Nullable String currentUserId) {
        if (TextUtils.isEmpty(msgIdOrClientMsgId)) {
            return;
        }
        final String key = msgIdOrClientMsgId.trim();
        ImMessageRepository.getInstance().findMessageByMsgIdOrClientMsgId(key, found -> {
            if (found == null) {
                return;
            }
            List<String> users = new ArrayList<>(
                    HongBaoMessageContentUtil.resolveLqUsers(found.getContent()));
            if (!TextUtils.isEmpty(currentUserId) && !users.contains(currentUserId)) {
                users.add(currentUserId);
            }
            String patched = HongBaoMessageContentUtil.patchLingquStatus(
                    found.getContent(), "1", users);
            if (patched == null) {
                return;
            }
            ImLocalStore.updateMessageContentById(key, patched, updated -> {
                if (updated != null) {
                    CEventCenter.dispatchEvent(Events.CHAT_MESSAGE_UPDATE, 0, 0, updated);
                }
            });
        });
    }
}
