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
import com.cboda.chatui.contact.viewholder.UserViewBroadHolder;

public class CheckableUserViewBroadHolder extends UserViewBroadHolder {
    CheckBox checkBox;

    public CheckableUserViewBroadHolder(Fragment fragment, CheckableUserListBroadAdapter adapter, View itemView) {
        super(fragment, adapter, itemView);
        bindViews(itemView);
    }

    private void bindViews(View itemView) {
        checkBox = itemView.findViewById(R.id.checkbox);
    }

    @Override
    public void onBind(UIUserBroadInfo userInfo) {
        super.onBind(userInfo);

        checkBox.setVisibility(View.VISIBLE);
        if (!userInfo.isCheckable()) {
            checkBox.setEnabled(false);
            checkBox.setChecked(true);
        } else {
            checkBox.setEnabled(true);
            checkBox.setChecked(userInfo.isChecked());
        }
        checkBox.setEnabled(userInfo.isCheckable());
    }

    public void setChecked(boolean checked){
        checkBox.setChecked(checked);
    }
}
