package com.freddy.im.message;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.freddy.im.db.ImDbExecutor;
import com.freddy.im.db.ImLocalStore;
import com.freddy.im.db.repository.ImGroupMemberRepository;
import com.freddy.im.db.repository.ImSessionRepository;
import com.freddy.im.http.GroupVoSessionMapper;
import com.freddy.im.http.ImGroupHttpExample;
import com.freddy.im.http.model.im.GroupVo;
import com.freddy.im.http.model.im.SessionItem;
import com.freddy.im.listener.OnHttpImListener;

import java.util.Collections;

/**
 * 群成员同步：冷启动/半截全量走分页全量；日常按 {@code memChgTime} 走增量 {@code changeMemberList}。
 * <p>
 * 万人群全量 {@code memberList}：首屏立刻、余量闲时（见 {@link ImGroupMemberChangeManager}）；
 * 增量始终立刻。{@code memChgTime} 仅在全量或增量<strong>成功</strong>后写入；失败/杀进程不抬水位。
 */
public final class GroupMemberSyncHelper {

    private static final String TAG = "GroupMemberSync";
    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());

    /** 进入群聊页单次群详情拉取成功后的回调（供禁言等复用，避免重复 requestGetGroupDetail）。 */
    public interface OnGroupDetailLoadedListener {
        void onGroupDetailLoaded(@NonNull GroupVo group);
    }

    private GroupMemberSyncHelper() {
    }

    /** 服务端成员变更时间晚于本地已同步时间时需要同步。 */
    public static boolean needsMemberSync(long localMemChgTime, long remoteMemChgTime) {
        return remoteMemChgTime > localMemChgTime;
    }

    /**
     * 本地活跃成员相对远端存在<strong>严重缺口</strong>时才强制全量（半截全量 / 脏水位）。
     * <p>
     * 略少于远端（如万人群差几十人）不触发，避免误打全量 {@code memberList}。
     * 条件：缺口达到远端一半，或绝对缺口 &gt;= {@link #FULL_SYNC_ABS_GAP}。
     */
    private static final double FULL_SYNC_RATIO = 0.5d;
    private static final int FULL_SYNC_ABS_GAP = 101;

    public static boolean needsFullSyncByCount(int localActiveCount, int remoteMemCount) {
        if (remoteMemCount <= 0 || localActiveCount >= remoteMemCount) {
            return false;
        }
        int gap = remoteMemCount - localActiveCount;
        return gap >= FULL_SYNC_ABS_GAP
                || localActiveCount < remoteMemCount * FULL_SYNC_RATIO;
    }

    /**
     * 进入群聊页：仅请求一次群详情，按水位/人数决定全量或增量同步成员。
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

    /** 拉群详情并 merge 会话（含 {@code memChgTime}）。仅在确认成员已对齐时调用。 */
    public static void refreshMemChgTimeFromDetail(@NonNull String groupId,
                                                   @Nullable Runnable onComplete) {
        ImGroupHttpExample.requestGetGroupDetail(groupId, new OnHttpImListener<GroupVo>() {
            @Override
            public void onHttpImSuccess(@NonNull GroupVo group) {
                mergeGroupDetailSession(groupId, group, true);
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
        final int remoteMemCount = group.getMemCount();
        ImDbExecutor.runRead(() -> {
            SessionItem session = ImSessionRepository.getInstance().queryBySessionId(groupId);
            long local = session != null ? session.getMemChgTime() : 0L;
            int localCount = ImGroupMemberRepository.getInstance().countActive(groupId, false);
            MAIN_HANDLER.post(() ->
                    dispatchSync(groupId, group, local, localCount, remote, remoteMemCount,
                            onMembersReloaded));
        });
    }

    private static void dispatchSync(@NonNull String groupId,
                                     @NonNull GroupVo group,
                                     long localMemChgTime,
                                     int localActiveCount,
                                     long remoteMemChgTime,
                                     int remoteMemCount,
                                     @Nullable Runnable onMembersReloaded) {
        boolean needByTime = needsMemberSync(localMemChgTime, remoteMemChgTime);
        boolean needFullByCount = needsFullSyncByCount(localActiveCount, remoteMemCount);
        boolean coldStart = localMemChgTime <= 0L || localActiveCount <= 0;

        if (!needByTime && !needFullByCount) {
            mergeGroupDetailSession(groupId, group, true);
            return;
        }

        // 同步未完成前不抬 memChgTime；仍可刷新 name/icon/memCount
        mergeGroupDetailSession(groupId, group, false);

        if (coldStart || needFullByCount) {
            if (needFullByCount && !coldStart) {
                Log.w(TAG, "人数不足或半截全量，强制 memberList groupId=" + groupId
                        + " local=" + localActiveCount + " remoteMemCount=" + remoteMemCount);
            }
            // 万人群：首屏立刻，余量闲时；小群整链立刻。memChgTime 仍仅全量成功后抬。
            ImGroupMemberChangeManager.getInstance().fetchAllMembersPaginated(
                    groupId,
                    () -> finishMemberSync(groupId, group, onMembersReloaded),
                    () -> Log.w(TAG, "全量 memberList 未完成，保留旧 memChgTime groupId=" + groupId),
                    remoteMemCount);
            return;
        }

        // 日常：水位落后但本地已有底库 → 增量（立刻，不进闲时）
        Log.d(TAG, "changeMemberList since=" + localMemChgTime + " groupId=" + groupId);
        ImGroupMemberChangeManager.getInstance().fetchChangedMembers(
                groupId,
                localMemChgTime,
                () -> finishMemberSync(groupId, group, onMembersReloaded),
                () -> {
                    Log.w(TAG, "增量失败，降级全量 groupId=" + groupId);
                    ImGroupMemberChangeManager.getInstance().fetchAllMembersPaginated(
                            groupId,
                            () -> finishMemberSync(groupId, group, onMembersReloaded),
                            () -> Log.w(TAG, "降级全量仍失败，保留旧 memChgTime groupId=" + groupId),
                            remoteMemCount);
                });
    }

    private static void mergeGroupDetailSession(@NonNull String groupId,
                                                @NonNull GroupVo group,
                                                boolean applyRemoteMemChgTime) {
        SessionItem existing = ImSessionRepository.getInstance().queryBySessionId(groupId);
        SessionItem item = GroupVoSessionMapper.toSessionItem(
                group, existing, groupId, applyRemoteMemChgTime);
        ImLocalStore.saveSessionsThen(Collections.singletonList(item), null);
    }

    private static void finishMemberSync(@NonNull String groupId,
                                         @NonNull GroupVo group,
                                         @Nullable Runnable onMembersReloaded) {
        mergeGroupDetailSession(groupId, group, true);
        if (onMembersReloaded != null) {
            onMembersReloaded.run();
        }
    }
}
