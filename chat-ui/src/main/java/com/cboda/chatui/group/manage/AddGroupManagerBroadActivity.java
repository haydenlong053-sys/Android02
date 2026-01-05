/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.group.manage;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProviders;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.cboda.chatui.R;
import com.cboda.chatui.contact.model.UIUserBroadInfo;
import com.cboda.chatui.group.BasePickGroupMemberBroadActivity;
import com.cboda.chatui.group.GroupViewBroadModel;

public class AddGroupManagerBroadActivity extends BasePickGroupMemberBroadActivity {
    private MenuItem menuItem;
    private List<UIUserBroadInfo> checkedGroupMembers;

    @Override
    protected void onGroupMemberChecked(List<UIUserBroadInfo> checkedUserInfos) {
        this.checkedGroupMembers = checkedUserInfos;
        if (checkedUserInfos == null || checkedUserInfos.isEmpty()) {
            menuItem.setTitle("确定");
            menuItem.setEnabled(false);
        } else {
            menuItem.setTitle("确定(" + checkedUserInfos.size() + ")");
            menuItem.setEnabled(true);
        }
    }

    @Override
    protected int menu() {
        return R.menu.group_manage_add_manager;
    }

    @Override
    protected void afterMenus(Menu menu) {
        menuItem = menu.findItem(R.id.confirm);
        menuItem.setEnabled(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.confirm) {
            setGroupManager();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setGroupManager() {
        GroupViewBroadModel groupViewModel = ViewModelProviders.of(this).get(GroupViewBroadModel.class);
        List<String> memberIds = new ArrayList<>(checkedGroupMembers.size());
        for (UIUserBroadInfo info : checkedGroupMembers) {
            memberIds.add(info.getUserInfo().uid);
        }
        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .content("添加中...")
                .progress(true, 100)
                .cancelable(false)
                .build();
        dialog.show();
        groupViewModel.setGroupManager(groupInfo.target, true, memberIds, null, Collections.singletonList(0))
                .observe(this, booleanOperateResult -> {
                    dialog.dismiss();
                    if (booleanOperateResult.isSuccess()) {
                        finish();
                    } else {
                        Toast.makeText(this, "设置管理员错误 " + booleanOperateResult.getErrorCode(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
