/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.contact;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import com.cboda.chatui.R;
import com.cboda.chatui.contact.model.FooterBroadValue;
import com.cboda.chatui.contact.model.HeaderBroadValue;
import com.cboda.chatui.contact.model.UIUserBroadInfo;
import com.cboda.chatui.contact.viewholder.UserViewBroadHolder;
import com.cboda.chatui.contact.viewholder.footer.FooterViewBroadHolder;
import com.cboda.chatui.contact.viewholder.header.HeaderViewBroadHolder;

public class UserListBroadAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_CONTACT = 1024;
    private static final int TYPE_FOOTER_START_INDEX = 2048;
    protected List<UIUserBroadInfo> users;
    protected List<UIUserBroadInfo> favUsers;
    protected Fragment fragment;
    private List<HeaderValueWrapper> headerValues;
    private List<FooterValueWrapper> footerValues;
    protected OnUserClickListener onUserClickListener;
    protected OnUserLongClickListener onUserLongClickListener;
    protected OnHeaderClickListener onHeaderClickListener;
    protected OnFooterClickListener onFooterClickListener;

    public UserListBroadAdapter(Fragment fragment) {
        super();
        this.fragment = fragment;
    }

    public List<UIUserBroadInfo> getUsers() {
        return users;
    }

    public List<UIUserBroadInfo> getFavUsers() {
        return favUsers;
    }

    public int getContactCount() {
        return userCount();
    }

    public void setUsers(List<UIUserBroadInfo> users) {
        this.users = users;
        notifyDataSetChanged();
    }

    public void setFavUsers(List<UIUserBroadInfo> favUsers) {
        this.favUsers = favUsers;
        notifyDataSetChanged();
    }

    public void setOnUserClickListener(OnUserClickListener onUserClickListener) {
        this.onUserClickListener = onUserClickListener;
    }

    public void setOnUserLongClickListener(OnUserLongClickListener onUserLongClickListener) {
        this.onUserLongClickListener = onUserLongClickListener;
    }

    public void setOnHeaderClickListener(OnHeaderClickListener onHeaderClickListener) {
        this.onHeaderClickListener = onHeaderClickListener;
    }

    public void setOnFooterClickListener(OnFooterClickListener onFooterClickListener) {
        this.onFooterClickListener = onFooterClickListener;
    }

    @NonNull
    @Override
    final public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder;
        View itemView;
        // header
        if (viewType < TYPE_CONTACT) {
            HeaderValueWrapper wrapper = headerValues.get(viewType);
            itemView = LayoutInflater.from(fragment.getActivity()).inflate(wrapper.layoutResId, parent, false);
            try {
                Constructor constructor = wrapper.headerViewHolderClazz.getConstructor(Fragment.class, UserListBroadAdapter.class, View.class);
                viewHolder = (HeaderViewBroadHolder) constructor.newInstance(fragment, this, itemView);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("create header viewHolder failed");
            }
            itemView.setOnClickListener(v -> {
                if (onHeaderClickListener != null) {
                    onHeaderClickListener.onHeaderClick((HeaderViewBroadHolder) viewHolder);
                }
            });
            // contact
        } else if (viewType == TYPE_CONTACT) {
            viewHolder = onCreateContactViewHolder(parent, viewType);
            // footer
        } else {
            FooterValueWrapper wrapper = footerValues.get(viewType - TYPE_FOOTER_START_INDEX);
            itemView = LayoutInflater.from(fragment.getActivity()).inflate(wrapper.layoutResId, parent, false);
            try {
                Constructor constructor = wrapper.footerViewHolderClazz.getConstructor(Fragment.class, UserListBroadAdapter.class, View.class);
                viewHolder = (FooterViewBroadHolder) constructor.newInstance(fragment, this, itemView);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("create footer viewHolder failed");
            }
            itemView.setOnClickListener(v -> {
                if (onFooterClickListener != null) {
                    onFooterClickListener.onFooterClick(viewHolder.getAdapterPosition() - headerCount() - userCount() - favUserCount());
                }
            });

        }
        return viewHolder;
    }

    protected RecyclerView.ViewHolder onCreateContactViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView;
        itemView = LayoutInflater.from(fragment.getActivity()).inflate(R.layout.contact_item_broad_contact, parent, false);
        UserViewBroadHolder viewHolder = new UserViewBroadHolder(fragment, this, itemView);
        itemView.setOnClickListener(v -> {
            if (onUserClickListener != null) {
                int position = viewHolder.getAdapterPosition();
                if (favUserCount() > 0 && position - headerCount() < favUserCount()) {
                    onUserClickListener.onUserClick(favUsers.get(position - headerCount()));
                } else {
                    onUserClickListener.onUserClick(users.get(position - headerCount() - favUserCount()));
                }
            }
        });
        itemView.setOnLongClickListener(v -> {
            if (onUserLongClickListener == null) {
                return false;
            }
            int position = viewHolder.getAdapterPosition();
            if (favUserCount() > 0 && position - headerCount() < favUserCount()) {
                onUserLongClickListener.onUserLongClick(favUsers.get(position - headerCount()));
            } else {
                onUserLongClickListener.onUserLongClick(users.get(position - headerCount() - favUserCount()));
            }
            return true;
        });
        return viewHolder;
    }

    private void processOnLongClick(RecyclerView.ViewHolder viewHolder) {
        // not implement yet
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof UserViewBroadHolder) {
            if (favUserCount() > 0 && position - headerCount() < favUserCount()) {
                ((UserViewBroadHolder) holder).onBind(favUsers.get(position - headerCount()));
            } else {
                ((UserViewBroadHolder) holder).onBind(users.get(position - headerCount() - favUserCount()));
            }
        } else if (holder instanceof HeaderViewBroadHolder) {
            HeaderValueWrapper wrapper = headerValues.get(position);
            ((HeaderViewBroadHolder) holder).onBind(wrapper.headerValue);
        } else if (holder instanceof FooterViewBroadHolder) {
            FooterValueWrapper wrapper = footerValues.get(position - headerCount() - userCount() - favUserCount());
            ((FooterViewBroadHolder<FooterBroadValue>) holder).onBind(wrapper.footerValue);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position < headerCount()) {
            return position;
        } else if (position < headerCount() + userCount() + favUserCount()) {
            return TYPE_CONTACT;
        } else {
            return TYPE_FOOTER_START_INDEX + (position - headerCount() - userCount() - favUserCount());
        }
    }

    @Override
    public int getItemCount() {
        return headerCount() + footerCount() + userCount() + favUserCount();
    }

    public void addHeaderViewHolder(Class<? extends HeaderViewBroadHolder> clazz, int layoutResId, HeaderBroadValue value) {

        if (headerValues == null) {
            headerValues = new ArrayList<>();
        }
        int position = headerCount();
        headerValues.add(new HeaderValueWrapper(clazz, layoutResId, value));

        notifyItemInserted(position);
    }

    public void updateHeader(int index, HeaderBroadValue value) {
        if (headerValues == null) {
            return;
        }
        HeaderValueWrapper wrapper = headerValues.get(index);
        headerValues.set(index, new HeaderValueWrapper(wrapper.headerViewHolderClazz, wrapper.layoutResId, value));
        notifyItemChanged(index);
    }

    public void addFooterViewHolder(Class<? extends FooterViewBroadHolder> clazz, int layoutResId, FooterBroadValue value) {
        if (footerValues == null) {
            footerValues = new ArrayList<>();
        }
        int footerCount = footerCount();
        footerValues.add(new FooterValueWrapper(clazz, layoutResId, value));
        notifyItemInserted(headerCount() + userCount() + favUserCount() + footerCount);
    }

    public void updateFooter(int index, FooterBroadValue value) {
        FooterValueWrapper wrapper = footerValues.get(index);
        footerValues.set(index, new FooterValueWrapper(wrapper.footerViewHolderClazz, wrapper.layoutResId, value));
        notifyItemChanged(headerCount() + userCount() + favUserCount() + index);
    }

    public int headerCount() {
        return headerValues == null ? 0 : headerValues.size();
    }

    private int userCount() {
        return users == null ? 0 : users.size();
    }

    private int favUserCount() {
        return favUsers == null ? 0 : favUsers.size();
    }

    public int footerCount() {
        return footerValues == null ? 0 : footerValues.size();
    }

    public interface OnUserClickListener {
        void onUserClick(UIUserBroadInfo userInfo);
    }

    public interface OnUserLongClickListener {
        void onUserLongClick(UIUserBroadInfo userInfo);
    }

    public interface OnHeaderClickListener {
        void onHeaderClick(HeaderViewBroadHolder holder);
    }

    public interface OnFooterClickListener {
        void onFooterClick(int index);
    }

    static class HeaderValueWrapper {
        Class<? extends HeaderViewBroadHolder> headerViewHolderClazz;
        int layoutResId;
        HeaderBroadValue headerValue;

        public HeaderValueWrapper(Class<? extends HeaderViewBroadHolder> headerViewHolderClazz, int layoutResId, HeaderBroadValue headerValue) {
            this.headerViewHolderClazz = headerViewHolderClazz;
            this.layoutResId = layoutResId;
            this.headerValue = headerValue;
        }
    }

    static class FooterValueWrapper {
        Class<? extends FooterViewBroadHolder> footerViewHolderClazz;
        int layoutResId;
        FooterBroadValue footerValue;

        public FooterValueWrapper(Class<? extends FooterViewBroadHolder> footerViewHolderClazz, int layoutResId, FooterBroadValue footerValue) {
            this.footerViewHolderClazz = footerViewHolderClazz;
            this.layoutResId = layoutResId;
            this.footerValue = footerValue;
        }
    }
}
