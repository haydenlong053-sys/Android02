package com.freddy.chat;

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

import com.freddy.chat.im.IMSClientBootstrap;
import com.freddy.im.http.ImMessageHttpExample;
import com.freddy.im.http.model.im.BaseMsgResponse;
import com.freddy.im.http.model.im.GroupVo;
import com.freddy.im.http.model.im.UserInfoItem;
import com.freddy.im.http.model.im.UserInfoListRes;
import com.freddy.im.listener.OnHttpImListener;
import com.freddy.im.signal.ImSignalProtos;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import com.cboda.intelligent.R;

/**
 * 创建群组：成员来自 5.2 {@code im/user/getUserList}，RecyclerView 多选。
 */
public class CreateGroupActivity extends AppCompatActivity {

    private static final int USER_LIST_PAGE_SIZE = 30;

    private EditText etGroupName;
    private EditText etSearchKeyword;
    private RecyclerView rvMembers;
    private ProgressBar progressMembers;
    private TextView tvMembersEmpty;
    private Button btnConfirmCreate;
    private GroupMemberSelectAdapter memberAdapter;

    @Nullable
    private String currentUserId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);
        setTitle(com.cboda.baselibrary.R.string.create_group);

        currentUserId = IMSClientBootstrap.getInstance().getCurrentUserId();

        etGroupName = findViewById(R.id.et_group_name);
        etSearchKeyword = findViewById(R.id.et_search_keyword);
        rvMembers = findViewById(R.id.rv_members);
        progressMembers = findViewById(R.id.progress_members);
        tvMembersEmpty = findViewById(R.id.tv_members_empty);
        btnConfirmCreate = findViewById(R.id.btn_confirm_create);
        Button btnSearchUsers = findViewById(R.id.btn_search_users);

        memberAdapter = new GroupMemberSelectAdapter();
        rvMembers.setLayoutManager(new LinearLayoutManager(this));
        rvMembers.setAdapter(memberAdapter);

        btnSearchUsers.setOnClickListener(v -> loadUserList());
        btnConfirmCreate.setOnClickListener(v -> submitCreateGroup());

        loadUserList();
    }

    private void loadUserList() {
        String keyword = etSearchKeyword.getText().toString().trim();
        progressMembers.setVisibility(View.VISIBLE);
        tvMembersEmpty.setVisibility(View.GONE);
        rvMembers.setVisibility(View.INVISIBLE);
        btnConfirmCreate.setEnabled(false);

        ImMessageHttpExample.requestGetUserList(
                TextUtils.isEmpty(keyword) ? "用户" : keyword,
                1,
                USER_LIST_PAGE_SIZE,
                new OnHttpImListener<BaseMsgResponse<UserInfoListRes>>() {
                    @Override
                    public void onHttpImSuccess(@NonNull BaseMsgResponse<UserInfoListRes> result) {
                        if (!result.isSuccess() || result.getContent() == null) {
                            runOnUiThread(CreateGroupActivity.this::onUserListLoadFailed);
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
        btnConfirmCreate.setEnabled(true);
        Toast.makeText(this, com.cboda.baselibrary.R.string.create_group_load_users_failed, Toast.LENGTH_SHORT).show();
    }

    private void onUserListLoaded(@NonNull UserInfoListRes res) {
        progressMembers.setVisibility(View.GONE);
        btnConfirmCreate.setEnabled(true);

        List<UserInfoItem> list = res.getUserList();
        List<UserInfoItem> display = new ArrayList<>();
        if (list != null) {
            for (UserInfoItem item : list) {
                if (item == null || TextUtils.isEmpty(item.getUserId())) {
                    continue;
                }
                if (!TextUtils.isEmpty(currentUserId)
                        && currentUserId.equals(item.getUserId())) {
                    continue;
                }
                display.add(item);
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

    private void submitCreateGroup() {
        String name = etGroupName.getText().toString().trim();
        List<String> members = buildMemberIds();
        if (TextUtils.isEmpty(name) || members.isEmpty()) {
            Toast.makeText(this, com.cboda.baselibrary.R.string.create_group_failed, Toast.LENGTH_SHORT).show();
            return;
        }

        btnConfirmCreate.setEnabled(false);
        progressMembers.setVisibility(View.VISIBLE);

        ImMessageHttpExample.requestCreateGroup(
                name,
                members,
                ImSignalProtos.ConversationType.MULTI_VALUE,
                new OnHttpImListener<GroupVo>() {
                    @Override
                    public void onHttpImSuccess(@NonNull GroupVo group) {
                        runOnUiThread(() -> {
                            progressMembers.setVisibility(View.GONE);
                            Toast.makeText(CreateGroupActivity.this,
                                    getString(com.cboda.baselibrary.R.string.create_group_success) + " " + group.getName(),
                                    Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
                            finish();
                        });
                    }
                },
                () -> runOnUiThread(() -> {
                    progressMembers.setVisibility(View.GONE);
                    btnConfirmCreate.setEnabled(true);
                    Toast.makeText(CreateGroupActivity.this,
                            com.cboda.baselibrary.R.string.create_group_failed, Toast.LENGTH_SHORT).show();
                }));
    }

    @NonNull
    private List<String> buildMemberIds() {
        Set<String> set = new LinkedHashSet<>();
        if (!TextUtils.isEmpty(currentUserId)) {
            set.add(currentUserId.trim());
        }
        for (String id : memberAdapter.getSelectedUserIds()) {
            if (!TextUtils.isEmpty(id)) {
                set.add(id.trim());
            }
        }
        return new ArrayList<>(set);
    }
}
