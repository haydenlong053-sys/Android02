package com.freddy.im.message;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.freddy.im.db.ImLocalStore;
import com.freddy.im.db.mapper.PinnedMessageRealmMapper;
import com.freddy.im.http.ImGroupHttpExample;
import com.freddy.im.http.model.im.GroupPinnedMessageRes;
import com.freddy.im.http.model.im.PinnedMessageItem;
import com.freddy.im.listener.OnHttpImListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 拉取并全量替换本地群置顶列表（进群 / 0x0501 code=1010）。
 */
public final class GroupPinnedSyncHelper {

    public interface OnSynced {
        void onSynced(@NonNull String groupId, @NonNull List<PinnedMessageItem> list);
    }

    private GroupPinnedSyncHelper() {
    }

    public static void syncFromServer(@NonNull String groupId, @Nullable OnSynced onSynced) {
        if (TextUtils.isEmpty(groupId)) {
            if (onSynced != null) {
                onSynced.onSynced(groupId, Collections.emptyList());
            }
            return;
        }
        ImGroupHttpExample.requestGetPinnedMessages(groupId, new OnHttpImListener<List<GroupPinnedMessageRes>>() {
            @Override
            public void onHttpImSuccess(@NonNull List<GroupPinnedMessageRes> result) {
                List<PinnedMessageItem> items = new ArrayList<>();
                for (GroupPinnedMessageRes res : result) {
                    if (res == null || TextUtils.isEmpty(res.getMessageId())) {
                        continue;
                    }
                    items.add(PinnedMessageRealmMapper.fromRes(groupId, res));
                }
                ImLocalStore.replaceGroupPinnedMessages(groupId, items, list -> {
                    if (onSynced != null) {
                        onSynced.onSynced(groupId, list);
                    }
                });
            }
        }, () -> {
            if (onSynced != null) {
                // 失败时仍回调本地已有数据，避免横幅空白
                ImLocalStore.loadGroupPinnedMessages(groupId, list -> {
                    if (onSynced != null) {
                        onSynced.onSynced(groupId, list);
                    }
                });
            }
        });
    }
}
