/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.search.viewHolder;

import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;

import com.cboda.chatui.R;
import com.cboda.chatui.third.utils.UIBroadUtils;
import cn.wildfirechat.model.GroupInfo;
import cn.wildfirechat.model.GroupSearchResult;

public class GroupViewBroadHolder2 extends ResultItemViewBroadHolder<GroupSearchResult> {
    ImageView portraitImageView;
    TextView nameTextView;
    TextView descTextView;
    View dividerLine;
    View mLlItemBg;

    public GroupViewBroadHolder2(Fragment fragment, View itemView) {
        super(fragment, itemView);
        bindViews(itemView);
    }

    private void bindViews(View itemView) {
        portraitImageView = itemView.findViewById(R.id.portraitImageView);
        nameTextView = itemView.findViewById(R.id.nameTextView);
        descTextView = itemView.findViewById(R.id.descTextView);
        dividerLine = itemView.findViewById(R.id.dividerLine);
        mLlItemBg = itemView.findViewById(R.id.mLlItemBg);
    }


    @Override
    public void onBind(String keyword, GroupSearchResult groupSearchResult) {
        goBind(keyword, groupSearchResult);

    }

    @Override
    public void onBind2(String keyword, GroupSearchResult groupSearchResult, int currentPosition, int startPosition, int endPosition) {
        goBind(keyword, groupSearchResult);
        if (currentPosition == endPosition) {
            dividerLine.setVisibility(View.GONE);
        } else {
            dividerLine.setVisibility(View.VISIBLE);
        }
        if (currentPosition == startPosition + 1 && currentPosition == endPosition) {
            //只有一条时
            mLlItemBg.setBackgroundResource(R.drawable.bg_rect_white_broad_16dp);
        }else if (currentPosition ==startPosition+1){
            //第一条
            mLlItemBg.setBackgroundResource(R.drawable.bg_rect_white_only_top_broad_16dp);
        }else if (currentPosition==endPosition){
            //最后一条
            mLlItemBg.setBackgroundResource(R.drawable.bg_rect_white_only_bottom_broad_16dp);
        }else {
            mLlItemBg.setBackgroundResource(R.color.white);
        }


    }

    private void goBind(String keyword, GroupSearchResult groupSearchResult) {
        GroupInfo groupInfo = groupSearchResult.groupInfo;
        nameTextView.setText(!TextUtils.isEmpty(groupInfo.remark) ? groupInfo.remark : groupInfo.name);
        String portrait = groupSearchResult.groupInfo.portrait;
        Glide.with(fragment)
                .load(portrait)
                .placeholder(R.mipmap.ic_group_chat)
                .transforms(new CenterCrop(), new RoundedCorners(UIBroadUtils.dip2Px(fragment.getContext(), 4)))
                .into(portraitImageView);

        String desc = "";
        if ((groupSearchResult.marchedType & GroupSearchResult.GroupSearchMarchTypeMask.Group_Name_Mask) > 0) {
            desc = "群名称包含: " + keyword;
        } else if ((groupSearchResult.marchedType & GroupSearchResult.GroupSearchMarchTypeMask.Group_Remark_Mask) > 0) {
            desc = "群备注包含: " + keyword;
        } else if ((groupSearchResult.marchedType & GroupSearchResult.GroupSearchMarchTypeMask.Member_Name_Mask) > 0) {
            desc = "群成员名包含: " + keyword;
        } else if ((groupSearchResult.marchedType & GroupSearchResult.GroupSearchMarchTypeMask.Member_Alias_Mask) > 0) {
            desc = "群成员备注包含: " + keyword;
        }
        descTextView.setText(desc);
    }
}
