/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.group;

import android.view.View;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.List;

import com.cboda.chatui.R;
import com.cboda.chatui.WfcBaseBroadActivity;
import com.cboda.chatui.contact.model.UIUserBroadInfo;
import com.cboda.chatui.contact.pick.PickUserViewBroadModel;
import com.cboda.chatui.user.UserViewBroadModel;
import cn.wildfirechat.model.GroupInfo;

public abstract class BasePickGroupMemberBroadActivity extends WfcBaseBroadActivity {
    protected GroupInfo groupInfo;
    protected List<String> unCheckableMemberIds;
    protected List<String> checkedMemberIds;

    public static final String GROUP_INFO = "groupInfo";
    public static final String UNCHECKABLE_MEMBER_IDS = "unCheckableMemberIds";
    public static final String CHECKED_MEMBER_IDS = "checkedMemberIds";
    public static final String MAX_COUNT = "maxCount";

    protected PickUserViewBroadModel pickUserViewModel;
    private Observer<UIUserBroadInfo> userCheckStatusUpdateLiveDataObserver = new Observer<UIUserBroadInfo>() {
        @Override
        public void onChanged(@Nullable UIUserBroadInfo userInfo) {
            List<UIUserBroadInfo> list = pickUserViewModel.getCheckedUsers();
            onGroupMemberChecked(list);
        }
    };

    /**
     * 当群成员选择情况变化时调用
     *
     * @param checkedUserInfos
     */
    protected abstract void onGroupMemberChecked(List<UIUserBroadInfo> checkedUserInfos);

    @Override
    protected int contentLayout() {
        return R.layout.fragment_container_broad_activity;
    }

    @Override
    protected void afterViews() {
        groupInfo = getIntent().getParcelableExtra("groupInfo");
        unCheckableMemberIds = getIntent().getStringArrayListExtra("unCheckableMemberIds");
        checkedMemberIds = getIntent().getStringArrayListExtra(CHECKED_MEMBER_IDS);
        int maxPickCount = getIntent().getIntExtra("maxCount", Integer.MAX_VALUE);
        if (groupInfo == null) {
            finish();
            return;
        }

        pickUserViewModel = new ViewModelProvider(this).get(PickUserViewBroadModel.class);
        pickUserViewModel.userCheckStatusUpdateLiveData().observeForever(userCheckStatusUpdateLiveDataObserver);
        if (checkedMemberIds != null && !checkedMemberIds.isEmpty()) {
            pickUserViewModel.setInitialCheckedIds(checkedMemberIds);
            pickUserViewModel.setUncheckableIds(checkedMemberIds);
        }

        if (unCheckableMemberIds != null && !unCheckableMemberIds.isEmpty()) {
            pickUserViewModel.setUncheckableIds(unCheckableMemberIds);
        } else {
            UserViewBroadModel userViewModel = new ViewModelProvider(this).get(UserViewBroadModel.class);
            List<String> list = new ArrayList<>();
            list.add(userViewModel.getUserId());
            pickUserViewModel.setUncheckableIds(list);
        }
        pickUserViewModel.setMaxPickCount(maxPickCount);

        initView();
    }

    private View toobarlay;
    private View page_title_layout;
    private void initView() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerFrameLayout, getFragment())
                .commit();
        toobarlay = findViewById(R.id.toobarlay);
        toobarlay.setVisibility(View.VISIBLE);
        page_title_layout = findViewById(R.id.page_title_layout);
        page_title_layout.setVisibility(View.GONE);
    }

    protected Fragment getFragment() {
        return PickGroupMemberBroadFragment.newInstance(groupInfo);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        pickUserViewModel.userCheckStatusUpdateLiveData().removeObserver(userCheckStatusUpdateLiveDataObserver);
    }
}
