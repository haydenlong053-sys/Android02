/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.contact.viewholder;

import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;

import com.cboda.chatui.R;
import com.cboda.chatui.contact.UserListAdapter;
import com.cboda.chatui.contact.model.UIUserInfo;
import com.cboda.chatui.user.UserViewModel;

public class UserViewHolder2 extends RecyclerView.ViewHolder {
    protected Fragment fragment;
    protected UserListAdapter adapter;
    ImageView portraitImageView;
    TextView nameTextView;
    TextView descTextView;
    protected TextView categoryTextView;

    protected UIUserInfo userInfo;

    public UserViewHolder2(Fragment fragment, UserListAdapter adapter, View itemView) {
        super(itemView);
        this.fragment = fragment;
        this.adapter = adapter;
        bindViews(itemView);
    }

    private void bindViews(View itemView) {
        portraitImageView = itemView.findViewById(R.id.portraitImageView);
        nameTextView = itemView.findViewById(R.id.nameTextView);
        descTextView = itemView.findViewById(R.id.descTextView);
        categoryTextView = itemView.findViewById(R.id.categoryTextView);
    }

    public void onBind(UIUserInfo userInfo) {
        this.userInfo = userInfo;
//        Log.e("野火","好友字母--> "+userInfo.isShowCategory()+"  "+userInfo.getCategory());
        if (userInfo.isShowCategory()) {
            categoryTextView.setVisibility(View.VISIBLE);
            categoryTextView.setText(userInfo.getCategory());
        } else {
            categoryTextView.setVisibility(View.GONE);
        }
        UserViewModel userViewModel = ViewModelProviders.of(fragment).get(UserViewModel.class);
        nameTextView.setText(userViewModel.getUserDisplayName(userInfo.getUserInfo()));
//        nameTextView.setText(userInfo.getUserInfo().lixinUserName);
        if (!TextUtils.isEmpty(userInfo.getDesc())) {
            descTextView.setVisibility(View.VISIBLE);
            descTextView.setText(userInfo.getDesc());
        } else {
            descTextView.setVisibility(View.GONE);
        }
        Glide.with(fragment).load(userInfo.getUserInfo().portrait).placeholder(R.mipmap.avatar_def)
            .transforms(new CenterCrop(), new RoundedCorners(10))
            .into(portraitImageView);
    }

    public UIUserInfo getBindContact() {
        return userInfo;
    }
}
