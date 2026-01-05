/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.contact.pick.viewholder;

import android.view.View;
import android.widget.CheckBox;

import androidx.fragment.app.Fragment;

import com.cboda.chatui.*;
import com.cboda.chatui.contact.model.UIUserBroadInfo;
import com.cboda.chatui.contact.pick.CheckableUserListBroadAdapter;

public class CheckableUserBlackViewBroadHolder extends CheckableUserViewBroadHolder {
    CheckBox checkBox;

    public CheckableUserBlackViewBroadHolder(Fragment fragment, CheckableUserListBroadAdapter adapter, View itemView) {
        super(fragment, adapter, itemView);
        bindViews(itemView);
    }

    private void bindViews(View itemView) {
        checkBox = itemView.findViewById(R.id.checkbox);
    }

    @Override
    public void onBind(UIUserBroadInfo userInfo) {
        super.onBind(userInfo);
        categoryTextView.setVisibility(View.GONE);
    }
}
