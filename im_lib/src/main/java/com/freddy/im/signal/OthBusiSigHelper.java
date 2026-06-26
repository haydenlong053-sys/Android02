package com.freddy.im.signal;

import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

/**
 * 解析 0x0501 {@link ImSignalProtos.OthBusiSig} 推送 body。
 */
public final class OthBusiSigHelper {

    private static final Gson GSON = new Gson();

    private OthBusiSigHelper() {
    }

    /**
     * 群公告通知 data 载荷。
     */
    private static final class GroupNoticeData {
        @SerializedName("groupId")
        String groupId;
    }

    /**
     * 若 body 为群公告变更信令（code=1001），返回 data 中的 groupId；否则返回 null。
     */
    @Nullable
    public static String parseGroupNoticeGroupId(@Nullable byte[] body) {
        if (body == null || body.length == 0) {
            return null;
        }
        try {
            ImSignalProtos.OthBusiSig sig = ImSignalProtos.OthBusiSig.parseFrom(body);
            if (sig.getCode() != OthBusiSigCodes.GROUP_NOTICE) {
                return null;
            }
            String data = sig.getData();
            if (TextUtils.isEmpty(data)) {
                return null;
            }
            GroupNoticeData parsed = GSON.fromJson(data, GroupNoticeData.class);
            if (parsed == null || TextUtils.isEmpty(parsed.groupId)) {
                return null;
            }
            return parsed.groupId;
        } catch (Exception ignored) {
            return null;
        }
    }
}
