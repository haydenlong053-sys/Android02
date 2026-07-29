package com.freddy.im.group;

import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.cboda.baselibrary.utils.ELogUtilKt;
import com.cboda.baselibrary.utils.MMKVBroadUtils;
import com.freddy.im.http.ImGroupHttpExample;
import com.freddy.im.http.model.im.GroupVo;
import com.freddy.im.listener.OnHttpImListener;

/**
 * 会议与群全员禁言联动：
 * <ul>
 *   <li>会议开启：保存开会前禁言态，并默认开启全员禁言</li>
 *   <li>会议结束：恢复开会前禁言态（会前无禁言则解禁，会前已禁言则保持）</li>
 * </ul>
 * 仅创建会议的一端写入/恢复本地快照；其它端靠禁言信令同步 UI。
 */
public final class MeetGroupMuteHelper {

    private static final String TAG = "MeetGroupMuteHelper";
    private static final String KEY_PREFIX = "meet_pre_all_mute_";

    private MeetGroupMuteHelper() {
    }

    /** 会议创建成功后调用（房主）。 */
    public static void onMeetStarted(@Nullable String groupId) {
        if (TextUtils.isEmpty(groupId)) {
            return;
        }
        final String gid = groupId;
        // 已有快照说明本端已处理过本场会议，避免重复覆盖会前状态
        if (hasSnapshot(gid)) {
            ELogUtilKt.eLogD(TAG, "onMeetStarted skip, snapshot exists groupId=" + gid);
            setAllMute(gid, true);
            return;
        }
        ImGroupHttpExample.requestGetGroupDetail(
                gid,
                new OnHttpImListener<GroupVo>() {
                    @Override
                    public void onHttpImSuccess(GroupVo group) {
                        boolean prevMuted = group != null && group.isAllMute();
                        saveSnapshot(gid, prevMuted);
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
                    saveSnapshot(gid, false);
                    setAllMute(gid, true);
                });
    }

    /** 会议结束信令 / 解散成功后调用。 */
    public static void onMeetEnded(@Nullable String groupId) {
        if (TextUtils.isEmpty(groupId)) {
            return;
        }
        final String gid = groupId;
        if (!hasSnapshot(gid)) {
            ELogUtilKt.eLogD(TAG, "onMeetEnded no snapshot, skip groupId=" + gid);
            return;
        }
        boolean prevMuted = loadSnapshot(gid);
        clearSnapshot(gid);
        ELogUtilKt.eLogD(TAG, "onMeetEnded restore prevMuted=" + prevMuted + " groupId=" + gid);
        if (prevMuted) {
            // 会前已是全员禁言：保持禁言，无需再请求
            return;
        }
        // 会前无禁言：会议结束后解禁
        setAllMute(gid, false);
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

    private static String key(String groupId) {
        return KEY_PREFIX + groupId;
    }

    private static boolean hasSnapshot(String groupId) {
        String v = MMKVBroadUtils.Companion.getSqlStrData(key(groupId), "");
        return "0".equals(v) || "1".equals(v);
    }

    private static boolean loadSnapshot(String groupId) {
        return "1".equals(MMKVBroadUtils.Companion.getSqlStrData(key(groupId), "0"));
    }

    private static void saveSnapshot(String groupId, boolean muted) {
        MMKVBroadUtils.Companion.saveSqlStrData(key(groupId), muted ? "1" : "0");
    }

    private static void clearSnapshot(String groupId) {
        MMKVBroadUtils.Companion.saveSqlStrData(key(groupId), "");
    }
}
