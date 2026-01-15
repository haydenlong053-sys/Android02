/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.contact.pick;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.List;

import com.cboda.chatui.R;
import com.cboda.chatui.WfcBaseBroadActivity;
import com.cboda.chatui.contact.model.UIUserBroadInfo;
import cn.wildfirechat.model.UserInfo;

public class PickContactBroadActivity extends WfcBaseBroadActivity {
    public static final String PARAM_MAX_COUNT = "maxCount";
    public static final String PARAM_INITIAL_CHECKED_IDS = "initialCheckedIds";
    public static final String PARA_UNCHECKABLE_IDS = "uncheckableIds";
    public static final String RESULT_PICKED_USERS = "pickedUsers";

    private MenuItem menuItem;
    private TextView confirmTv;

    private PickUserViewBroadModel pickUserViewModel;
    private Observer<UIUserBroadInfo> contactCheckStatusUpdateLiveDataObserver = new Observer<UIUserBroadInfo>() {
        @Override
        public void onChanged(@Nullable UIUserBroadInfo userInfo) {
            List<UIUserBroadInfo> list = pickUserViewModel.getCheckedUsers();
            updatePickStatus(list);
        }
    };

    protected void updatePickStatus(List<UIUserBroadInfo> userInfos) {
        if (userInfos == null || userInfos.isEmpty()) {
            confirmTv.setText("确定");
            menuItem.setEnabled(false);
        } else {
            confirmTv.setText("确定(" + userInfos.size() + ")");
            menuItem.setEnabled(true);
        }
    }

    @Override
    protected int contentLayout() {
        return R.layout.fragment_container_broad_activity;
    }

    @Override
    protected void afterViews() {
        pickUserViewModel = new ViewModelProvider(this).get(PickUserViewBroadModel.class);
        pickUserViewModel.userCheckStatusUpdateLiveData().observeForever(contactCheckStatusUpdateLiveDataObserver);
        Intent intent = getIntent();
        int maxCount = intent.getIntExtra(PARAM_MAX_COUNT, 0);
        if (maxCount > 0) {
            pickUserViewModel.setMaxPickCount(maxCount);
        }

        pickUserViewModel.setInitialCheckedIds(intent.getStringArrayListExtra(PARAM_INITIAL_CHECKED_IDS));
        pickUserViewModel.setUncheckableIds(intent.getStringArrayListExtra(PARA_UNCHECKABLE_IDS));

        initView();
    }

    @Override
    protected int menu() {
        return R.menu.contact_broad_pick;
    }

    @Override
    protected void afterMenus(Menu menu) {
        menuItem = menu.findItem(R.id.confirm);
        menuItem.setEnabled(false);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        confirmTv = menuItem.getActionView().findViewById(R.id.confirm_tv);
        confirmTv.setOnClickListener(v -> onOptionsItemSelected(menuItem));
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.confirm) {
            onConfirmClick();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initView() {
        PickContactBroadFragment fragment = new PickContactBroadFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerFrameLayout, fragment)
                .commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        pickUserViewModel.userCheckStatusUpdateLiveData().removeObserver(contactCheckStatusUpdateLiveDataObserver);
    }

    protected void onContactPicked(List<UIUserBroadInfo> newlyCheckedUserInfos) {
        Intent intent = new Intent();
        ArrayList<UserInfo> newlyPickedInfos = new ArrayList<>();
        for (UIUserBroadInfo info : newlyCheckedUserInfos) {
            newlyPickedInfos.add(info.getUserInfo());
        }
        intent.putExtra(RESULT_PICKED_USERS, newlyPickedInfos);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    protected void onConfirmClick() {
        List<UIUserBroadInfo> newlyCheckedUserInfos = pickUserViewModel.getCheckedUsers();
        onContactPicked(newlyCheckedUserInfos);
    }

    public static Intent buildPickIntent(Context context, int maxCount, ArrayList<String> initialChecedIds, ArrayList<String> uncheckableIds) {
        Intent intent = new Intent(context, PickContactBroadActivity.class);
        intent.putExtra(PARAM_MAX_COUNT, maxCount);
        intent.putExtra(PARAM_INITIAL_CHECKED_IDS, initialChecedIds);
        intent.putExtra(PARA_UNCHECKABLE_IDS, uncheckableIds);
        return intent;
    }
}
