package com.freddy.im.group;

import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.cboda.baselibrary.utils.ELogUtilKt;
import com.cboda.baselibrary.utils.MMKVBroadUtils;
import com.freddy.im.http.ImGroupHttpExample;
import com.freddy.im.http.model.im.GroupVo;
import com.freddy.im.listener.OnHttpImListener;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 会议与群全员禁言联动：
 * <ul>
 *   <li>会议开启：保存开会前禁言态，并默认开启全员禁言</li>
 *   <li>会议结束：恢复开会前禁言态（会前无禁言则解禁，会前已禁言则保持）</li>
 * </ul>
 * 仅创建会议的一端写入/恢复本地快照；其它端靠禁言信令同步 UI。
 * <p>
 * 快照可能同时挂在 groupId / channelId 等多个 key 下，避免自动结束与手动解散 ID 不一致时漏恢复。
 */
public final class MeetGroupMuteHelper {

    private static final String TAG = "MeetGroupMuteHelper";
    private static final String KEY_PREFIX = "meet_pre_all_mute_";

    private MeetGroupMuteHelper() {
    }

    /** 会议创建成功后调用（房主）。{@code groupId} 须为 IM 群会话 ID。 */
    public static void onMeetStarted(@Nullable String groupId) {
        onMeetStarted(groupId, (String[]) null);
    }

    /**
     * @param groupId   IM 群会话 ID（用于 setAllMute）
     * @param aliasIds  可选别名（如 channelId），同样写入快照便于结束时命中
     */
    public static void onMeetStarted(@Nullable String groupId, @Nullable String... aliasIds) {
        if (TextUtils.isEmpty(groupId)) {
            return;
        }
        final String gid = groupId;
        final String[] aliases = aliasIds;
        // 已有快照说明本端已处理过本场会议，避免重复覆盖会前状态
        if (hasSnapshot(gid) || hasAnySnapshot(aliases)) {
            ELogUtilKt.eLogD(TAG, "onMeetStarted skip, snapshot exists groupId=" + gid);
            saveUnderAliases(gid, aliases, loadMuted(firstSnapshotKey(gid, aliases)));
            setAllMute(gid, true);
            return;
        }
        ImGroupHttpExample.requestGetGroupDetail(
                gid,
                new OnHttpImListener<GroupVo>() {
                    @Override
                    public void onHttpImSuccess(GroupVo group) {
                        boolean prevMuted = group != null && group.isAllMute();
                        saveUnderAliases(gid, aliases, prevMuted);
                        ELogUtilKt.eLogD(TAG, "onMeetStarted saved prevMuted=" + prevMuted
                                + " groupId=" + gid);
                        if (!prevMuted) {
                            setAllMute(gid, true);
                        }
                    }
                },
                () -> {
                    // 拉详情失败：按未禁言处理并强制开会禁言，结束时解禁
                    ELogUtilKt.eLogD(TAG, "onMeetStarted getDetail fail, assume unmuted groupId=" + gid);
                    saveUnderAliases(gid, aliases, false);
                    setAllMute(gid, true);
                });
    }

    /** 会议结束信令 / 解散成功后调用。会前无禁言则解禁，会前已禁言则保持。 */
    public static void onMeetEnded(@Nullable String groupId) {
        onMeetEnded(groupId, (String[]) null);
    }

    /**
     * @param groupId  优先作为解禁 API 的群 ID（一般为信令里的 groupId / groupIdNo）
     * @param aliasIds 其它可能存有快照的 ID（channelId 等）
     */
    public static void onMeetEnded(@Nullable String groupId, @Nullable String... aliasIds) {
        Set<String> ids = new LinkedHashSet<>();
        if (!TextUtils.isEmpty(groupId)) {
            ids.add(groupId);
        }
        if (aliasIds != null) {
            for (String alias : aliasIds) {
                if (!TextUtils.isEmpty(alias)) {
                    ids.add(alias);
                }
            }
        }
        if (ids.isEmpty()) {
            return;
        }

        String matchedKey = null;
        for (String id : ids) {
            if (hasSnapshot(id)) {
                matchedKey = id;
                break;
            }
        }
        if (matchedKey == null) {
            ELogUtilKt.eLogD(TAG, "onMeetEnded no snapshot, skip ids=" + ids);
            return;
        }

        boolean prevMuted = loadMuted(matchedKey);
        String primaryGroupId = loadPrimaryGroupId(matchedKey);
        if (TextUtils.isEmpty(primaryGroupId)) {
            primaryGroupId = !TextUtils.isEmpty(groupId) ? groupId : matchedKey;
        }

        for (String id : ids) {
            clearSnapshot(id);
        }
        clearSnapshot(matchedKey);

        ELogUtilKt.eLogD(TAG, "onMeetEnded restore prevMuted=" + prevMuted
                + " primaryGroupId=" + primaryGroupId + " matchedKey=" + matchedKey);
        if (prevMuted) {
            // 会前已是全员禁言：保持禁言，无需再请求
            return;
        }
        // 会前无禁言：会议结束后解禁
        setAllMute(primaryGroupId, false);
    }

    private static void setAllMute(String groupId, boolean mute) {
        ImGroupHttpExample.requestSetAllMute(
                groupId,
                mute,
                new OnHttpImListener<Boolean>() {
                    @Override
                    public void onHttpImSuccess(Boolean result) {
                        ELogUtilKt.eLogD(TAG, "setAllMute ok mute=" + mute + " groupId=" + groupId);
                    }
                },
                () -> ELogUtilKt.eLogD(TAG, "setAllMute fail mute=" + mute + " groupId=" + groupId));
    }

    private static void saveUnderAliases(String primaryGroupId, @Nullable String[] aliases, boolean muted) {
        saveSnapshot(primaryGroupId, muted, primaryGroupId);
        if (aliases == null) {
            return;
        }
        for (String alias : aliases) {
            if (!TextUtils.isEmpty(alias) && !TextUtils.equals(alias, primaryGroupId)) {
                saveSnapshot(alias, muted, primaryGroupId);
            }
        }
    }

    private static boolean hasAnySnapshot(@Nullable String[] aliases) {
        if (aliases == null) {
            return false;
        }
        for (String alias : aliases) {
            if (!TextUtils.isEmpty(alias) && hasSnapshot(alias)) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    private static String firstSnapshotKey(String primary, @Nullable String[] aliases) {
        if (hasSnapshot(primary)) {
            return primary;
        }
        if (aliases != null) {
            for (String alias : aliases) {
                if (!TextUtils.isEmpty(alias) && hasSnapshot(alias)) {
                    return alias;
                }
            }
        }
        return primary;
    }

    private static String key(String groupId) {
        return KEY_PREFIX + groupId;
    }

    private static boolean hasSnapshot(String groupId) {
        String v = MMKVBroadUtils.Companion.getSqlStrData(key(groupId), "");
        return !TextUtils.isEmpty(v) && (v.charAt(0) == '0' || v.charAt(0) == '1');
    }

    private static boolean loadMuted(String groupId) {
        String v = MMKVBroadUtils.Companion.getSqlStrData(key(groupId), "0");
        return !TextUtils.isEmpty(v) && v.charAt(0) == '1';
    }

    /** 快照值：{@code 0|primary} / {@code 1|primary}；兼容旧值 {@code 0}/{@code 1}。 */
    @Nullable
    private static String loadPrimaryGroupId(String snapshotKey) {
        String v = MMKVBroadUtils.Companion.getSqlStrData(key(snapshotKey), "");
        if (TextUtils.isEmpty(v)) {
            return snapshotKey;
        }
        int idx = v.indexOf('|');
        if (idx >= 0 && idx + 1 < v.length()) {
            String primary = v.substring(idx + 1).trim();
            return TextUtils.isEmpty(primary) ? snapshotKey : primary;
        }
        return snapshotKey;
    }

    private static void saveSnapshot(String keyId, boolean muted, String primaryGroupId) {
        String primary = TextUtils.isEmpty(primaryGroupId) ? keyId : primaryGroupId;
        MMKVBroadUtils.Companion.saveSqlStrData(key(keyId), (muted ? "1" : "0") + "|" + primary);
    }

    private static void clearSnapshot(String groupId) {
        MMKVBroadUtils.Companion.saveSqlStrData(key(groupId), "");
    }
}
