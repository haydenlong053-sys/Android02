/*
 * Copyright (c) 2023 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.contact.viewholder.header;

import android.view.View;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.cboda.chatui.R;
import com.cboda.chatui.contact.UserListBroadAdapter;
import com.cboda.chatui.contact.model.OrganizationBroadValue;
import com.cboda.chatui.organization.model.Organization;

public class DepartViewBroadHolder extends HeaderViewBroadHolder<OrganizationBroadValue> {
    private Organization organization;
    private TextView nameTextView;

    public DepartViewBroadHolder(Fragment fragment, UserListBroadAdapter adapter, View itemView) {
        super(fragment, adapter, itemView);
        this.nameTextView = itemView.findViewById(R.id.nameTextView);
    }

    @Override
    public void onBind(OrganizationBroadValue value) {
        Organization organization = (Organization) value.getValue();
        this.nameTextView.setText(organization.name);
        this.organization = organization;
    }

    public Organization getOrganization() {
        return organization;
    }
}
