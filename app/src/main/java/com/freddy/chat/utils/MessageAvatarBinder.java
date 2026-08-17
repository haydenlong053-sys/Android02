package com.freddy.chat.utils;

import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cboda.baselibrary.bean.FriendBean;
import com.cboda.baselibrary.utils.MMKVBroadUtils;
import com.cboda.baselibrary.utils.RealmUtil;
import com.cboda.baselibrary.widge.VpGradeView;
import com.cboda.chatui.R;
import com.cboda.chatui.third.utils.TimeBroadBroadUtils;
import com.freddy.im.MessageType;
import com.freddy.im.http.model.im.GroupMemberItem;
import com.freddy.im.http.model.im.SessionItem;
import com.hjq.shape.view.ShapeTextView;

import kotlin.Unit;

/**
 * 消息 / 会话列表头像、信用分、靓号、会员、入群时间绑定。
 * <p>
 * 群聊消息：来自本地群成员库 {@link GroupMemberItem}。
 * 单聊消息 / 会话列表：来自野火用户表 {@link FriendBean}（{@link RealmUtil}）。
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

    /**
     * 会话列表头像：单聊展示信用分 / 靓号 / 会员；群聊展示创建天数角标（对齐野火）。
     * 群天数取会话表 {@link SessionItem#getCreateTime()}（由群详情 {@code GroupVo.createTime} 入库）。
     * 天数为 0 或 createTime 为空时不展示。
     */
    public static void bindSessionListAvatar(@NonNull VpGradeView avatarView,
                                             @Nullable SessionItem session,
                                             @Nullable String currentUserId) {
        String portrait = WeChatUiHelper.resolveAvatarUrl(session);
        if (session == null) {
            avatarView.setData(null, null, null, null, null);
            return;
        }
        if (MessageType.isGroupChat(session.getConversation())) {
            // 对齐野火 GroupConversationViewBroadHolder：紫色「N天」角标
            avatarView.loadUserImg(portrait);
            avatarView.setData3(null, null, null, null);
            Integer days = groupDaysFromCreateTime(session.getCreateTime());
            avatarView.setRegisterDay(days, R.string.msg_days_unit2);
            return;
        }
        if (!MessageType.isSingleChat(session.getConversation())) {
            avatarView.setData(portrait, null, null, null, null);
            return;
        }
        String peerId = SessionDisplayEnricher.INSTANCE.resolvePeerUserId(session, currentUserId);
        if (TextUtils.isEmpty(peerId)
                || TextUtils.equals(peerId, MMKVBroadUtils.Companion.getKeFuId())) {
            avatarView.setData(portrait, null, null, null, null);
            return;
        }
        RealmUtil.INSTANCE.findUserData(peerId, (FriendBean friend) -> {
            if (friend != null) {
                avatarView.setData(
                        portrait,
                        friend.isNiceNum,
                        friend.isVip,
                        friend.grade,
                        friend.registerDay);
            } else {
                avatarView.setData(portrait, null, null, null, null);
            }
            return Unit.INSTANCE;
        });
    }

    /**
     * 由 GroupVo/会话 createTime 计算群天数；无效或不足 1 天返回 null（不展示）。
     */
    @Nullable
    static Integer groupDaysFromCreateTime(long createTime) {
        long millis = WeChatUiHelper.normalizeToMillis(createTime);
        if (millis <= 0L) {
            return null;
        }
        long days = (System.currentTimeMillis() - millis) / (24L * 60L * 60L * 1000L);
        if (days <= 0L) {
            return null;
        }
        return (int) days;
    }

    /**
     * 会话列表名称旁注册天数（单聊，野火用户表）。
     */
    public static void bindSessionRegisterDay(@NonNull TextView registerDayTv,
                                              @Nullable SessionItem session,
                                              @Nullable String currentUserId) {
        if (session == null || !MessageType.isSingleChat(session.getConversation())) {
            registerDayTv.setVisibility(View.GONE);
            return;
        }
        String peerId = SessionDisplayEnricher.INSTANCE.resolvePeerUserId(session, currentUserId);
        if (TextUtils.isEmpty(peerId)
                || TextUtils.equals(peerId, MMKVBroadUtils.Companion.getKeFuId())) {
            registerDayTv.setVisibility(View.GONE);
            return;
        }
        RealmUtil.INSTANCE.findUserData(peerId, (FriendBean friend) -> {
            if (friend != null && !TextUtils.isEmpty(friend.registerDay)) {
                registerDayTv.setVisibility(View.VISIBLE);
                registerDayTv.setText(friend.registerDay
                        + registerDayTv.getContext().getString(R.string.msg_days_unit));
            } else {
                registerDayTv.setVisibility(View.GONE);
            }
            return Unit.INSTANCE;
        });
    }

    /**
     * 会话列表群聊改名次数（对齐野火 {@code nameTimeTv}：「改名 N 次」）。
     * 未同步到改名次数时按 0 展示。
     */
    public static void bindSessionRenameCount(@NonNull TextView renameCountTv,
                                              @Nullable SessionItem session) {
        if (session == null || !MessageType.isGroupChat(session.getConversation())) {
            renameCountTv.setVisibility(View.GONE);
            return;
        }
        int count = session.getRenameCount() != null ? session.getRenameCount() : 0;
        renameCountTv.setVisibility(View.VISIBLE);
        renameCountTv.setText(
                renameCountTv.getContext().getString(R.string.rename)
                        + "\u00A0"
                        + count
                        + "\u00A0"
                        + renameCountTv.getContext().getString(R.string.times_unit));
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
