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
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;

import com.cboda.chatui.R;
import com.cboda.chatui.contact.model.UIUserBroadInfo;

public class PickedUserBroadAdapter_bk extends RecyclerView.Adapter {

    List<UIUserBroadInfo> mData;
    RequestOptions mOptions;

    public PickedUserBroadAdapter_bk() {
        this.mData = new ArrayList<>();
        mOptions = new RequestOptions()
                .placeholder(R.mipmap.avatar_broad_def)
                .transforms(new CenterCrop(), new RoundedCorners(4));
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.picked_broad_user, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((Holder) holder).bind(mData.get(position));
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public void addUser(UIUserBroadInfo uiUserInfo) {
        mData.add(uiUserInfo);
        notifyDataSetChanged();
    }

    public void removeUser(UIUserBroadInfo uiUserInfo) {
        mData.remove(uiUserInfo);
        notifyDataSetChanged();
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
