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
import com.cboda.chatui.organization.model.Organization;

public class PickedUserBroadAdapter extends RecyclerView.Adapter {

    private PickUserViewBroadModel pickUserViewModel;

    List<UIUserBroadInfo> users;
    List<Organization> organizations;
    RequestOptions mOptions;

    public PickedUserBroadAdapter(PickUserViewBroadModel pickUserViewModel) {
        this.pickUserViewModel = pickUserViewModel;
        this.users = new ArrayList<>();
        mOptions = new RequestOptions()
            .placeholder(R.mipmap.avatar_def)
            .transforms(new CenterCrop(), new RoundedCorners(4));
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == R.layout.picked_user) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.picked_user, parent, false);
            return new UserViewBroadHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.picked_organization, parent, false);
            return new OrganizationViewBroadHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (position < getOrganizationCount()) {
            ((OrganizationViewBroadHolder) holder).bind(organizations.get(position));
        } else {
            ((UserViewBroadHolder) holder).bind(users.get(position - getOrganizationCount()));
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position < getOrganizationCount()) {
            return R.layout.picked_organization;
        } else {
            return R.layout.picked_user;
        }
    }

    @Override
    public int getItemCount() {
        return getOrganizationCount() + users.size();
    }

    public void setOrganizations(List<Organization> organizations) {
        this.organizations = organizations;
        notifyDataSetChanged();
    }

    public void addUser(UIUserBroadInfo uiUserInfo) {
        users.add(uiUserInfo);
        notifyDataSetChanged();
    }

    public void removeUser(UIUserBroadInfo uiUserInfo) {
        users.remove(uiUserInfo);
        notifyDataSetChanged();
    }

    private int getOrganizationCount() {
        return organizations == null ? 0 : organizations.size();
    }

    private class UserViewBroadHolder extends RecyclerView.ViewHolder {

        private ImageView imageView;
        private UIUserBroadInfo uiUserInfo;

        UserViewBroadHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.avatar);
            itemView.setOnClickListener(v -> pickUserViewModel.checkUser(uiUserInfo, false));
        }

        void bind(UIUserBroadInfo uiUserInfo) {
            this.uiUserInfo = uiUserInfo;
            Glide.with(imageView)
                .load(uiUserInfo.getUserInfo().portrait)
                .apply(mOptions)
                .into(imageView);
        }
    }

    private class OrganizationViewBroadHolder extends RecyclerView.ViewHolder {

        private ImageView imageView;
        private Organization organization;

        OrganizationViewBroadHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.avatar);
            itemView.setOnClickListener(v -> {
                organizations.remove(getAdapterPosition());
                notifyDataSetChanged();
            });
        }

        void bind(Organization organization) {
            this.organization = organization;
            Glide.with(imageView)
                .load(organization.portraitUrl)
                .error(R.mipmap.ic_deparment)
                .apply(mOptions)
                .into(imageView);
        }
    }
}
