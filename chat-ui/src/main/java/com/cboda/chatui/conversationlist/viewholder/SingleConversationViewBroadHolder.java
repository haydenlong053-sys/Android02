/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.conversationlist.viewholder;

import static com.blankj.utilcode.util.SizeUtils.dp2px;
import static com.cboda.baselibrary.utils.MMKVBroadUtils.USER_REGISTRATION_DAY_SCORE;
import static com.cboda.baselibrary.utils.ViewExpandUtilKt.anyToInt;
import static com.cboda.baselibrary.utils.ViewExpandUtilKt.formatWithBigDecimal;
import static com.cboda.baselibrary.utils.ViewExpandUtilKt.setBackColor;
import static com.cboda.baselibrary.utils.ViewExpandUtilKt.setTxtColor;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.view.View;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.cboda.baselibrary.bean.GlobalFriendBroadBean;
import com.cboda.baselibrary.bean.GlobalBroadInfo;
import com.bumptech.glide.Glide;

import com.cboda.baselibrary.utils.MMKVBroadUtils;
import com.cboda.chatui.ChatManagerBroadHolder;
import com.cboda.chatui.R;
import com.cboda.chatui.annotation.ConversationInfoBroadType;
import com.cboda.chatui.annotation.EnableContextBroadMenu;

import cn.wildfirechat.model.Conversation;
import cn.wildfirechat.model.ConversationInfo;
import cn.wildfirechat.model.NullUserInfo;
import cn.wildfirechat.model.UserInfo;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

@ConversationInfoBroadType(type = Conversation.ConversationType.Single, line = 0)
@EnableContextBroadMenu
public class SingleConversationViewBroadHolder extends ConversationViewBroadHolder {
    private String portrait;

    public SingleConversationViewBroadHolder(Fragment fragment, RecyclerView.Adapter adapter, View itemView) {
        super(fragment, adapter, itemView);
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onBindConversationInfo(ConversationInfo conversationInfo) {
        UserInfo userInfo = ChatManagerBroadHolder.gChatManager.getUserInfo(conversationInfo.conversation.target, false);
        this.portrait = userInfo.portrait;
        GlobalBroadInfo.INSTANCE.queryMyFriendInfo(conversationInfo.conversation.target, null, friendSmailListBean -> {
            String name = getMyUserDisplayName(userInfo, friendSmailListBean);
            Glide.with(fragment)
                    .load(portrait)
                    .placeholder(com.cboda.baselibrary.R.mipmap.ic_logo_broad_login)
                    .into(portraitImageView);
            if (null != friendSmailListBean) {
                if (friendSmailListBean.isNiceNum) {
                    setBackColor(gradeTv, com.cboda.baselibrary.R.color.black);
                    setTxtColor(gradeTv, com.cboda.baselibrary.R.color.color_FAE77C);
                } else {
                    int registrationDayScore = anyToInt(MMKVBroadUtils.Companion.getSqlStrData(USER_REGISTRATION_DAY_SCORE, ""));
                    int registrationDay = friendSmailListBean.registerDay;
                    if (registrationDay < registrationDayScore) {
                        setBackColor(gradeTv, com.cboda.baselibrary.R.color.color_D5271E);
                    } else {
                        setBackColor(gradeTv, com.cboda.baselibrary.R.color.color_7A66FA);
                    }
                    setTxtColor(gradeTv, com.cboda.baselibrary.R.color.white);
                }
            } else {
                setBackColor(gradeTv, com.cboda.baselibrary.R.color.color_D5271E);
                setTxtColor(gradeTv, com.cboda.baselibrary.R.color.white);
            }
            if (null != friendSmailListBean && friendSmailListBean.isVip) {
                vipImageView.setVisibility(View.VISIBLE);
                portraitImageView.setBorderWidthAndColor(com.cboda.baselibrary.R.color.color_FAD27C, 1f);
            } else {
                vipImageView.setVisibility(View.GONE);
                portraitImageView.setBorderWidthAndColor(com.cboda.baselibrary.R.color.transparent, 0f);
            }


            nameTextView.setText(name);
            if (null != friendSmailListBean && null != friendSmailListBean.grade) {
                gradeTv.setVisibility(View.VISIBLE);
//            gradeTv.setText(friendSmailListBean.grade + "分");
                gradeTv.setText(formatWithBigDecimal(friendSmailListBean.grade) + gradeTv.getContext().getString(R.string.msg_score_unit));
            } else {
                gradeTv.setVisibility(View.GONE);
            }
            if (null != friendSmailListBean && null != friendSmailListBean.registerDay) {
                nameTimeTv.setVisibility(View.VISIBLE);
                nameTimeTv.setText(friendSmailListBean.registerDay + nameTimeTv.getContext().getString(R.string.msg_days_unit));
            } else {
                nameTimeTv.setVisibility(View.VISIBLE);
                nameTimeTv.setText("0" + nameTimeTv.getContext().getString(R.string.msg_days_unit));
            }
            return null;
        });
    }


    private String getMyUserDisplayName(UserInfo userInfo, GlobalFriendBroadBean friendSmailListBean) {
        if (friendSmailListBean != null) {
            portrait = friendSmailListBean.icon;
        }
        if (friendSmailListBean != null) {
            if (!TextUtils.isEmpty(friendSmailListBean.remarkName)) {
                return friendSmailListBean.remarkName;
            } else if (!TextUtils.isEmpty(friendSmailListBean.nickName)) {
                return friendSmailListBean.nickName;
            } else {
                if (userInfo == null) {
                    return "";
                }
                return "<" + userInfo.uid + ">";
            }
        } else if (userInfo != null && !TextUtils.isEmpty(userInfo.friendAlias)) {
            return userInfo.friendAlias;
        } else if (userInfo != null && !(userInfo instanceof NullUserInfo) && !TextUtils.isEmpty(userInfo.displayName)) {
            return userInfo.displayName;
        } else {
            if (userInfo == null) {
                return "";
            }
            return "<" + userInfo.uid + ">";
        }
    }

}
