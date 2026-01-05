/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.contact.pick;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import com.cboda.chatui.R;
import com.cboda.chatui.contact.model.UIUserBroadInfo;

public class PickedUserBlackBroadAdapter extends PickedUserBroadAdapter {

    public PickedUserBlackBroadAdapter(PickUserViewBroadModel pickUserViewModel) {
        super(pickUserViewModel);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.picked_user_black, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((Holder) holder).bind(users.get(position));
    }

    private class Holder extends RecyclerView.ViewHolder {

        private ImageView imageView;

        Holder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.avatar);
        }

        void bind(UIUserBroadInfo uiUserInfo) {
            Glide.with(imageView)
                    .load(uiUserInfo.getUserInfo().portrait)
                    .apply(mOptions)
                    .into(imageView);
        }
    }
}
