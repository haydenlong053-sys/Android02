/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.group;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cboda.chatui.R;
import com.cboda.chatui.contact.UserListBroadAdapter;
import com.cboda.chatui.contact.pick.CheckableUserListBlackBroadAdapter;
import com.cboda.chatui.contact.pick.PickedUserBroadAdapter;
import com.cboda.chatui.contact.pick.PickedUserBlackBroadAdapter;
import com.cboda.chatui.third.utils.UIBroadUtils;
import cn.wildfirechat.model.GroupInfo;

public class PickGroupMemberBlackBroadFragment extends PickGroupMemberBroadFragment {

    private static final int SPAN = 5;

    public static PickGroupMemberBlackBroadFragment newInstance(GroupInfo groupInfo) {
        Bundle args = new Bundle();
        args.putParcelable("groupInfo", groupInfo);
        PickGroupMemberBlackBroadFragment fragment = new PickGroupMemberBlackBroadFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getContentLayoutResId() {
        return R.layout.contact_pick_fragment_broad_black;
    }

    @Override
    public UserListBroadAdapter onCreateUserListAdapter() {
        return new CheckableUserListBlackBroadAdapter(this);
    }

    @Override
    protected void configPickedUserRecyclerView() {
        RecyclerView.LayoutManager pickedContactRecyclerViewLayoutManager = new GridLayoutManager(getActivity(), SPAN);
        pickedUserRecyclerView.setLayoutManager(pickedContactRecyclerViewLayoutManager);
        int space = (UIBroadUtils.getDisplayWidth(getContext()) - SPAN * UIBroadUtils.dip2Px(52) - 2 * UIBroadUtils.dip2Px(16)) / (SPAN - 1);
        pickedUserRecyclerView.addItemDecoration(new Decoration(space));
    }

    @Override
    protected PickedUserBroadAdapter getPickedUserAdapter() {
        return new PickedUserBlackBroadAdapter(pickUserViewModel);
    }

    @Override
    protected void handleHintView(boolean focus) {
        if (focus) {
            hintView.setVisibility(View.GONE);
        } else {
            hintView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void handleEditText() {

    }

    private static class Decoration extends RecyclerView.ItemDecoration {

        private int space;

        public Decoration(int space) {
            this.space = space;
        }

        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view);
            int column = position % SPAN;
            if (column < SPAN - 1) {
                outRect.right = space;
            } else {
                outRect.right = 0;
            }
            outRect.bottom = UIBroadUtils.dip2Px(10);
        }
    }
}
