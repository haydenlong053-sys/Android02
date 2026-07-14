package com.freddy.chat.utils;

import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cboda.baselibrary.bean.FriendBean;
import com.cboda.baselibrary.utils.MMKVBroadUtils;
import com.cboda.baselibrary.utils.RealmUtil;
import com.cboda.baselibrary.widge.VpGradeView;
import com.cboda.chatui.R;
import com.cboda.chatui.third.utils.TimeBroadBroadUtils;
import com.freddy.im.http.model.im.GroupMemberItem;
import com.hjq.shape.view.ShapeTextView;

import kotlin.Unit;

/**
 * 消息列表头像、信用分、入群时间绑定。
 * <p>
 * 群聊：信用分 / VIP / 靓号 / 注册天数（用于角标配色）/ 入群时间均来自本地群成员库 {@link GroupMemberItem}。
 * 单聊：仍从 Realm 用户库读取（顶栏与头像角标逻辑不变）。
 */
public final class MessageAvatarBinder {

    private MessageAvatarBinder() {
    }

    public static void bindAvatar(@NonNull VpGradeView avatarView,
                                  @Nullable String portraitUrl,
                                  @Nullable GroupMemberItem member,
                                  boolean isGroupChat,
                                  @Nullable String userId) {
        avatarView.loadUserImg(portraitUrl);
        if (isGroupChat) {
            bindAvatarFromGroupMember(avatarView, member);
            return;
        }
        if (TextUtils.isEmpty(userId)) {
            avatarView.setData3(null, null, null, null);
            return;
        }
        RealmUtil.INSTANCE.findUserData(userId, (FriendBean friend) -> {
            if (friend != null) {
                avatarView.setData3(
                        friend.isNiceNum,
                        friend.isVip,
                        null,
                        friend.registerDay);
            } else {
                avatarView.setData3(null, null, null, null);
            }
            return Unit.INSTANCE;
        });
    }

    private static void bindAvatarFromGroupMember(@NonNull VpGradeView avatarView,
                                                  @Nullable GroupMemberItem member) {
        if (member == null) {
            avatarView.setData3(null, null, null, null);
            return;
        }
        String grade = TextUtils.isEmpty(member.getGrade()) ? null : member.getGrade();
        avatarView.setData3(
                member.isNiceNumMember(),
                member.isVipMember(),
                grade,
                registerDaysFromMember(member));
    }

    /**
     * 将群成员 {@code registerTime} 转为注册天数，供 {@link VpGradeView} 信用分底色判断。
     */
    @Nullable
    static String registerDaysFromMember(@Nullable GroupMemberItem member) {
        if (member == null) {
            return null;
        }
        long millis = WeChatUiHelper.normalizeToMillis(member.getRegisterTime());
        if (millis <= 0L) {
            return null;
        }
        long days = (System.currentTimeMillis() - millis) / (24L * 60L * 60L * 1000L);
        return String.valueOf(Math.max(days, 0L));
    }

    public static void bindJoinTime(@NonNull ShapeTextView joinTimeTv,
                                    boolean show,
                                    @Nullable GroupMemberItem member) {
        if (!show) {
            joinTimeTv.setVisibility(View.GONE);
            return;
        }
        long joinTime = member != null ? member.getJoinTime() : 0L;
        if (joinTime > 0L) {
            applyJoinTime(joinTimeTv, joinTime);
        } else {
            joinTimeTv.setVisibility(View.GONE);
        }
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
