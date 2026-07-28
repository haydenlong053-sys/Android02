package com.freddy.im.message;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.freddy.im.db.ImLocalStore;
import com.freddy.im.db.repository.ImSessionRepository;
import com.freddy.im.group.GroupRole;
import com.freddy.im.http.model.im.SessionItem;

import java.util.Collections;
import java.util.List;

/**
 * 转让群主（HTTP 成功 / 0x0104 {@code TransferGroupSig}）：回写本地 {@code owerId} 与成员角色。
 * <p>
 * 不额外拉群详情：信令已带 {@code newOwer}，本地更新即可驱动私群/公群分类刷新。
 */
public final class ImGroupTransferManager {

    private static final ImGroupTransferManager INSTANCE = new ImGroupTransferManager();

    private ImGroupTransferManager() {
    }

    public static ImGroupTransferManager getInstance() {
        return INSTANCE;
    }

    /**
     * @param listener 本地落库与角色更新完成后回调（会话可能为空）；可为 null
     */
    public void handleTransfer(@Nullable String groupId,
                               @Nullable String newOwnerId,
                               @Nullable OnTransferAppliedListener listener) {
        if (TextUtils.isEmpty(groupId) || TextUtils.isEmpty(newOwnerId)) {
            if (listener != null) {
                listener.onTransferApplied(groupId, Collections.emptyList());
            }
            return;
        }
        final String gid = groupId.trim();
        final String newOwner = newOwnerId.trim();
        SessionItem session = ImSessionRepository.getInstance().queryBySessionId(gid);
        final String oldOwner = session != null ? session.getOwerId() : null;

        Runnable afterDone = () -> {
            if (listener == null) {
                return;
            }
            List<SessionItem> sessions = session != null
                    ? Collections.singletonList(session)
                    : Collections.emptyList();
            listener.onTransferApplied(gid, sessions);
        };

        if (session != null) {
            session.setOwerId(newOwner);
            ImLocalStore.saveSessionsThen(Collections.singletonList(session),
                    () -> updateRoles(gid, oldOwner, newOwner, afterDone));
        } else {
            updateRoles(gid, oldOwner, newOwner, afterDone);
        }
    }

    private static void updateRoles(@NonNull String groupId,
                                    @Nullable String oldOwnerId,
                                    @NonNull String newOwnerId,
                                    @NonNull Runnable afterDone) {
        Runnable setNewOwner = () -> ImLocalStore.updateGroupMemberRolesThen(
                groupId,
                Collections.singletonList(newOwnerId),
                GroupRole.OWNER,
                afterDone);
        if (!TextUtils.isEmpty(oldOwnerId) && !oldOwnerId.equals(newOwnerId)) {
            ImLocalStore.updateGroupMemberRolesThen(
                    groupId,
                    Collections.singletonList(oldOwnerId),
                    GroupRole.MEMBER,
                    setNewOwner);
        } else {
            setNewOwner.run();
        }
    }

    public interface OnTransferAppliedListener {
        void onTransferApplied(@Nullable String groupId, @NonNull List<SessionItem> sessions);
    }
}
