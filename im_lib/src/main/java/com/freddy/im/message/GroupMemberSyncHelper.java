package com.freddy.im.message;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.freddy.im.db.ImLocalStore;
import com.freddy.im.db.repository.ImSessionRepository;
import com.freddy.im.http.GroupVoSessionMapper;
import com.freddy.im.http.ImGroupHttpExample;
import com.freddy.im.http.model.im.GroupVo;
import com.freddy.im.http.model.im.SessionItem;
import com.freddy.im.listener.OnHttpImListener;

import java.util.Collections;

/**
 * 群成员同步：基于群详情 {@code memChgTime} 判断是否需要全量拉取成员列表。
 */
public final class GroupMemberSyncHelper {

    /** 进入群聊页单次群详情拉取成功后的回调（供禁言等复用，避免重复 requestGetGroupDetail）。 */
    public interface OnGroupDetailLoadedListener {
        void onGroupDetailLoaded(@NonNull GroupVo group);
    }

    private GroupMemberSyncHelper() {
    }

    /** 服务端成员变更时间晚于本地已同步时间时需要全量同步。 */
    public static boolean needsMemberSync(long localMemChgTime, long remoteMemChgTime) {
        return remoteMemChgTime > localMemChgTime;
    }

    /**
     * 进入群聊页：仅请求一次群详情，比对 {@code memChgTime} 并按需全量同步成员。
     *
     * @param detailListener 群详情成功回调（如应用全员禁言态）；可为 null
     */
    public static void syncOnEnterGroupChat(@NonNull String groupId,
                                            @Nullable OnGroupDetailLoadedListener detailListener,
                                            @Nullable Runnable onMembersReloaded) {
        ImGroupHttpExample.requestGetGroupDetail(groupId, new OnHttpImListener<GroupVo>() {
            @Override
            public void onHttpImSuccess(@NonNull GroupVo group) {
                if (detailListener != null) {
                    detailListener.onGroupDetailLoaded(group);
                }
                handleGroupDetailAfterFetch(groupId, group, onMembersReloaded);
            }
        }, () -> { });
    }

    /** @deprecated 请使用 {@link #syncOnEnterGroupChat} */
    @Deprecated
    public static void syncMembersOnEnterChatIfNeeded(@NonNull String groupId,
                                                      @Nullable Runnable onMembersReloaded) {
        syncOnEnterGroupChat(groupId, null, onMembersReloaded);
    }

    /** 拉群详情并 merge 会话（含 {@code memChgTime}）。 */
    public static void refreshMemChgTimeFromDetail(@NonNull String groupId,
                                                   @Nullable Runnable onComplete) {
        ImGroupHttpExample.requestGetGroupDetail(groupId, new OnHttpImListener<GroupVo>() {
            @Override
            public void onHttpImSuccess(@NonNull GroupVo group) {
                mergeGroupDetailSession(groupId, group);
                if (onComplete != null) {
                    onComplete.run();
                }
            }
        }, () -> {
            if (onComplete != null) {
                onComplete.run();
            }
        });
    }

    private static void handleGroupDetailAfterFetch(@NonNull String groupId,
                                                    @NonNull GroupVo group,
                                                    @Nullable Runnable onMembersReloaded) {
        final long remote = group.getMemChgTime();
        ImLocalStore.loadGroupMemChgTime(groupId, local -> {
            if (!needsMemberSync(local, remote)) {
                mergeGroupDetailSession(groupId, group);
                return;
            }
            ImGroupMemberChangeManager.getInstance().fetchAllMembersPaginated(
                    groupId,
                    () -> finishMemberSync(groupId, group, onMembersReloaded));
        });
    }

    private static void mergeGroupDetailSession(@NonNull String groupId, @NonNull GroupVo group) {
        SessionItem existing = ImSessionRepository.getInstance().queryBySessionId(groupId);
        SessionItem item = GroupVoSessionMapper.toSessionItem(group, existing, groupId);
        ImLocalStore.saveSessionsThen(Collections.singletonList(item), null);
    }

    private static void finishMemberSync(@NonNull String groupId,
                                         @NonNull GroupVo group,
                                         @Nullable Runnable onMembersReloaded) {
        mergeGroupDetailSession(groupId, group);
        if (onMembersReloaded != null) {
            onMembersReloaded.run();
        }
    }
}
