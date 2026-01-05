/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.contact.pick;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import java.util.ArrayList;
import java.util.List;

import com.cboda.chatui.BroadConfig;
import com.cboda.chatui.R;
import com.cboda.chatui.contact.ContactViewBroadModel;
import com.cboda.chatui.contact.OrganizationServiceViewBroadModel;
import com.cboda.chatui.contact.model.GroupBroadValue;
import com.cboda.chatui.contact.model.OrganizationBroadValue;
import com.cboda.chatui.contact.model.UIUserBroadInfo;
import com.cboda.chatui.contact.pick.viewholder.PickGroupViewBroadHolder;
import com.cboda.chatui.contact.viewholder.header.DepartViewBroadHolder;
import com.cboda.chatui.contact.viewholder.header.HeaderViewBroadHolder;
import com.cboda.chatui.contact.viewholder.header.OrganizationViewBroadHolder;
import com.cboda.chatui.organization.model.Employee;
import com.cboda.chatui.organization.model.Organization;
import cn.wildfirechat.model.GroupInfo;

public class PickConversationTargetBroadFragment extends PickUserBroadFragment {
    private final static int REQUEST_CODE_PICK_GROUP = 100;
    private final static int REQUEST_CODE_PICK_ORGANIZATION_MEMBER = 101;
    private boolean pickGroupForResult;
    private boolean multiGroupMode;
    private OnGroupPickListener groupPickListener;

    private OrganizationServiceViewBroadModel organizationServiceViewModel;

    /**
     * @param pickGroupForResult 为true时，点击group item不直接启动群会话；否则直接启动群会话
     * @param multiGroupMode     {@code pickGroupForResult} 为{@code true}时，才生效，表示群多选
     * @return
     */
    public static PickConversationTargetBroadFragment newInstance(boolean pickGroupForResult, boolean multiGroupMode) {
        PickConversationTargetBroadFragment fragment = new PickConversationTargetBroadFragment();
        Bundle args = new Bundle();
        args.putBoolean("pickGroupForResult", pickGroupForResult);
        args.putBoolean("multiGroupMode", multiGroupMode);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            pickGroupForResult = args.getBoolean("pickGroupForResult", false);
            multiGroupMode = args.getBoolean("multiGroupMode", false);
        }
        organizationServiceViewModel = new ViewModelProvider(this).get(OrganizationServiceViewBroadModel.class);
    }

    @Override
    protected void setupPickFromUsers() {
        ContactViewBroadModel contactViewModel = ViewModelProviders.of(getActivity()).get(ContactViewBroadModel.class);
        contactViewModel.contactListLiveData().observe(this, userInfos -> {
            showContent();
            pickUserViewModel.setUsers(userInfos);
            userListAdapter.setUsers(userInfos);
        });
    }

    public void setOnGroupPickListener(OnGroupPickListener listener) {
        this.groupPickListener = listener;
    }

    @Override
    public void initHeaderViewHolders() {
        // 选择一个群
        addHeaderViewHolder(PickGroupViewBroadHolder.class, R.layout.contact_header_broad_group, new GroupBroadValue());
        // 选取组织成员
        if (!TextUtils.isEmpty(BroadConfig.ORG_SERVER_ADDRESS)) {
            organizationServiceViewModel.rootOrganizationLiveData().observe(this, new Observer<List<Organization>>() {
                @Override
                public void onChanged(List<Organization> organizations) {
                    if (!organizations.isEmpty()) {
                        for (Organization org : organizations) {
                            OrganizationBroadValue value = new OrganizationBroadValue();
                            value.setValue(org);
                            addHeaderViewHolder(OrganizationViewBroadHolder.class, R.layout.contact_header_broad_organization, value);
                        }
                    }
                }
            });
            organizationServiceViewModel.myOrganizationLiveData().observe(this, new Observer<List<Organization>>() {
                @Override
                public void onChanged(List<Organization> organizations) {
                    if (!organizations.isEmpty()) {
                        for (Organization org : organizations) {
                            OrganizationBroadValue value = new OrganizationBroadValue();
                            value.setValue(org);
                            addHeaderViewHolder(DepartViewBroadHolder.class, R.layout.contact_header_broad_department, value);
                        }
                    }

                }
            });
        }
    }

    @Override
    public void onHeaderClick(HeaderViewBroadHolder holder) {
//        // 选择一个群
//        if (holder instanceof PickGroupViewBroadHolder) {
//            Intent intent = new Intent(getActivity(), GroupListActivity.class);
//            if (pickGroupForResult) {
//                intent.putExtra(GroupListActivity.INTENT_FOR_RESULT, true);
//            }
//            startActivityForResult(intent, REQUEST_CODE_PICK_GROUP);
//        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == REQUEST_CODE_PICK_GROUP) {
            // TODO 多选
            ArrayList<GroupInfo> groupInfos = data.getParcelableArrayListExtra("groupInfos");
            if (groupPickListener != null) {
                groupPickListener.onGroupPicked(groupInfos);
            }
        } else if (requestCode == REQUEST_CODE_PICK_ORGANIZATION_MEMBER) {
            ArrayList<Organization> organizations = data.getParcelableArrayListExtra("organizations");
            ArrayList<Employee> employees = data.getParcelableArrayListExtra("employees");
            if (employees != null) {
                for (Employee employee : employees) {
                    UIUserBroadInfo uiUserInfo = new UIUserBroadInfo(employee.toUserInfo());
                    pickUserViewModel.checkUser(uiUserInfo, true);
                }
            }
            if (organizations != null) {
                pickedUserAdapter.setOrganizations(organizations);

                // 没有的话，搜索提示框布局会错乱
                handleHintView(false);
                handleEditText();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public List<Organization> getCheckedOrganizations() {
        return pickedUserAdapter.organizations;
    }

    public interface OnGroupPickListener {
        void onGroupPicked(List<GroupInfo> groupInfos);
    }
}
