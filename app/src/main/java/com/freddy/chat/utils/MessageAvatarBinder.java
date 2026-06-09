package com.freddy.chat.utils;

import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cboda.baselibrary.bean.FriendBean;
import com.cboda.baselibrary.bean.UserGroupBean;
import com.cboda.baselibrary.utils.MMKVBroadUtils;
import com.cboda.baselibrary.utils.RealmUtil;
import com.cboda.baselibrary.widge.VpGradeView;
import com.cboda.chatui.R;
import com.cboda.chatui.third.utils.TimeBroadBroadUtils;
import com.freddy.im.http.model.im.GroupMemberItem;
import com.hjq.shape.view.ShapeTextView;

import kotlin.Unit;

/**
 * 消息列表头像、信用分、入群时间绑定（对齐野火 ConversationMessageBroadAdapter）。
 */
public final class MessageAvatarBinder {

    private MessageAvatarBinder() {
    }

    public static void bindAvatar(@NonNull VpGradeView avatarView,
                                  @Nullable String portraitUrl,
                                  @Nullable String userId,
                                  boolean isGroupChat) {
        avatarView.loadUserImg(portraitUrl);
        if (TextUtils.isEmpty(userId)) {
            avatarView.setData3(null, null, null, null);
            return;
        }
        RealmUtil.INSTANCE.findUserData(userId, (FriendBean friend) -> {
            if (friend != null) {
                avatarView.setData3(
                        friend.isNiceNum,
                        friend.isVip,
                        isGroupChat ? friend.grade : null,
                        friend.registerDay);
            } else {
                avatarView.setData3(null, null, null, null);
            }
            return Unit.INSTANCE;
        });
    }

    public static void bindJoinTime(@NonNull ShapeTextView joinTimeTv,
                                    boolean show,
                                    @Nullable GroupMemberItem member,
                                    @Nullable String groupId,
                                    @Nullable String userId) {
        if (!show) {
            joinTimeTv.setVisibility(View.GONE);
            return;
        }
        long joinTime = member != null ? member.getJoinTime() : 0L;
        if (joinTime > 0L) {
            applyJoinTime(joinTimeTv, joinTime);
            return;
        }
        if (TextUtils.isEmpty(groupId) || TextUtils.isEmpty(userId)) {
            joinTimeTv.setVisibility(View.GONE);
            return;
        }
        UserGroupBean bean = new UserGroupBean();
        bean.setGroupId(groupId);
        bean.setUserId(userId);
        RealmUtil.INSTANCE.findUserGroupData(bean, userGroup -> {
            if (userGroup != null
                    && userGroup.getJoinTime() != null
                    && userGroup.getJoinTime() != 0L) {
                applyJoinTime(joinTimeTv, userGroup.getJoinTime());
            } else {
                joinTimeTv.setVisibility(View.GONE);
            }
            return Unit.INSTANCE;
        });
    }

    private static void applyJoinTime(@NonNull ShapeTextView joinTimeTv, long joinTimeMillis) {
        long millis = WeChatUiHelper.normalizeToMillis(joinTimeMillis);
        joinTimeTv.setVisibility(View.VISIBLE);
        joinTimeTv.setText(TimeBroadBroadUtils.getJoinFormatTime(millis)
                + joinTimeTv.getContext().getString(R.string.message_joined_group));
        long inGroupDaysMs = (long) MMKVBroadUtils.Companion.getNewInGroupDays()
                * 24L * 60L * 60L * 1000L;
        joinTimeTv.setSelected((System.currentTimeMillis() - millis) < inGroupDaysMs);
    }
}
