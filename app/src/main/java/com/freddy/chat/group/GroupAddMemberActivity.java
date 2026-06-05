package com.freddy.chat.group;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.freddy.chat.GroupMemberSelectAdapter;
import com.cboda.intelligent.R;
import com.freddy.im.http.ImGroupHttpExample;
import com.freddy.im.http.ImMessageHttpExample;
import com.freddy.im.http.model.im.BaseMsgResponse;
import com.freddy.im.http.model.im.GroupVo;
import com.freddy.im.http.model.im.UserInfoItem;
import com.freddy.im.http.model.im.UserInfoListRes;
import com.freddy.im.listener.OnHttpImListener;

import java.util.ArrayList;
import java.util.List;

/** 邀请/添加群成员（§3.4 / §3.17）。 */
public class GroupAddMemberActivity extends AppCompatActivity {

    private static final int USER_LIST_PAGE_SIZE = 30;

    private String groupId;
    private EditText etSearchKeyword;
    private RecyclerView rvMembers;
    private ProgressBar progressMembers;
    private TextView tvMembersEmpty;
    private Button btnConfirm;
    private GroupMemberSelectAdapter memberAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_add_member);
        setTitle(R.string.group_add_member_title);

        groupId = GroupIntent.getGroupId(getIntent());
        if (TextUtils.isEmpty(groupId)) {
            finish();
            return;
        }

        etSearchKeyword = findViewById(R.id.et_search_keyword);
        rvMembers = findViewById(R.id.rv_members);
        progressMembers = findViewById(R.id.progress_members);
        tvMembersEmpty = findViewById(R.id.tv_members_empty);
        btnConfirm = findViewById(R.id.btn_confirm_add);
        findViewById(R.id.btn_search_users).setOnClickListener(v -> loadUserList());

        memberAdapter = new GroupMemberSelectAdapter();
        rvMembers.setLayoutManager(new LinearLayoutManager(this));
        rvMembers.setAdapter(memberAdapter);
        btnConfirm.setOnClickListener(v -> submitAddMembers());

        loadUserList();
    }

    private void loadUserList() {
        String keyword = etSearchKeyword.getText().toString().trim();
        progressMembers.setVisibility(View.VISIBLE);
        tvMembersEmpty.setVisibility(View.GONE);
        rvMembers.setVisibility(View.INVISIBLE);
        btnConfirm.setEnabled(false);

        ImMessageHttpExample.requestGetUserList(
                TextUtils.isEmpty(keyword) ? "用户" : keyword,
                1,
                USER_LIST_PAGE_SIZE,
                new OnHttpImListener<BaseMsgResponse<UserInfoListRes>>() {
                    @Override
                    public void onHttpImSuccess(@NonNull BaseMsgResponse<UserInfoListRes> result) {
                        if (!result.isSuccess() || result.getContent() == null) {
                            runOnUiThread(GroupAddMemberActivity.this::onUserListLoadFailed);
                            return;
                        }
                        runOnUiThread(() -> onUserListLoaded(result.getContent()));
                    }
                },
                () -> runOnUiThread(this::onUserListLoadFailed));
    }

    private void onUserListLoadFailed() {
        progressMembers.setVisibility(View.GONE);
        tvMembersEmpty.setVisibility(View.VISIBLE);
        rvMembers.setVisibility(View.GONE);
        btnConfirm.setEnabled(true);
        Toast.makeText(this, R.string.create_group_load_users_failed, Toast.LENGTH_SHORT).show();
    }

    private void onUserListLoaded(@NonNull UserInfoListRes res) {
        progressMembers.setVisibility(View.GONE);
        btnConfirm.setEnabled(true);
        List<UserInfoItem> list = res.getUserList();
        List<UserInfoItem> display = new ArrayList<>();
        if (list != null) {
            for (UserInfoItem item : list) {
                if (item != null && !TextUtils.isEmpty(item.getUserId())) {
                    display.add(item);
                }
            }
        }
        if (display.isEmpty()) {
            tvMembersEmpty.setVisibility(View.VISIBLE);
            rvMembers.setVisibility(View.GONE);
        } else {
            tvMembersEmpty.setVisibility(View.GONE);
            rvMembers.setVisibility(View.VISIBLE);
            memberAdapter.setItems(display);
        }
    }

    private void submitAddMembers() {
        List<String> selected = memberAdapter.getSelectedUserIds();
        if (selected.isEmpty()) {
            Toast.makeText(this, R.string.group_add_member_empty, Toast.LENGTH_SHORT).show();
            return;
        }
        btnConfirm.setEnabled(false);
        progressMembers.setVisibility(View.VISIBLE);

        ImGroupHttpExample.requestAddMembers(groupId, selected, new OnHttpImListener<GroupVo>() {
            @Override
            public void onHttpImSuccess(@NonNull GroupVo result) {
                runOnUiThread(() -> {
                    progressMembers.setVisibility(View.GONE);
                    Toast.makeText(GroupAddMemberActivity.this,
                            R.string.group_add_member_success, Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        }, () -> runOnUiThread(() -> {
            progressMembers.setVisibility(View.GONE);
            btnConfirm.setEnabled(true);
            Toast.makeText(GroupAddMemberActivity.this,
                    R.string.group_add_member_failed, Toast.LENGTH_SHORT).show();
        }));
    }
}
