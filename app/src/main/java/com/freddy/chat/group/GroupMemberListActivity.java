package com.freddy.chat.group;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cboda.intelligent.R;
import com.freddy.chat.im.IMSClientBootstrap;
import com.freddy.im.http.ImGroupHttpExample;
import com.freddy.im.http.model.im.GroupUserVo;
import com.freddy.im.http.model.im.GroupVo;
import com.freddy.im.listener.OnHttpImListener;

import java.util.Collections;
import java.util.List;

/** 群成员列表（§3.11）。 */
public class GroupMemberListActivity extends AppCompatActivity {

    private String groupId;
    @Nullable
    private GroupVo groupVo;
    private boolean isOwner;
    @Nullable
    private String currentUserId;

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private GroupMemberAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_members);
        setTitle(R.string.group_members_title);

        groupId = GroupIntent.getGroupId(getIntent());
        groupVo = GroupIntent.getGroupVo(getIntent());
        isOwner = GroupIntent.isOwner(getIntent());
        currentUserId = IMSClientBootstrap.getInstance().getCurrentUserId();
        if (TextUtils.isEmpty(groupId)) {
            finish();
            return;
        }

        recyclerView = findViewById(R.id.rv_members);
        progressBar = findViewById(R.id.progress);
        tvEmpty = findViewById(R.id.tv_empty);

        adapter = new GroupMemberAdapter();
        adapter.setOwnerContext(groupVo != null ? groupVo.getOwerId() : null, currentUserId, isOwner);
        adapter.setOnMemberActionListener(member -> confirmKick(member));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        loadMembers();
    }

    private void loadMembers() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);
        ImGroupHttpExample.requestGetAllMembers(groupId, new OnHttpImListener<List<GroupUserVo>>() {
            @Override
            public void onHttpImSuccess(@NonNull List<GroupUserVo> result) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    boolean empty = result.isEmpty();
                    tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
                    adapter.setMembers(result);
                });
            }
        }, () -> runOnUiThread(() -> {
            progressBar.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
            Toast.makeText(this, R.string.group_members_load_failed, Toast.LENGTH_SHORT).show();
        }));
    }

    private void confirmKick(@NonNull GroupUserVo member) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.group_member_kick)
                .setMessage(getString(R.string.group_kick_confirm, member.getDisplayName()))
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_confirm, (d, w) -> kickMember(member))
                .show();
    }

    private void kickMember(@NonNull GroupUserVo member) {
        if (TextUtils.isEmpty(member.getUserId())) {
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        ImGroupHttpExample.requestKickOut(groupId, Collections.singletonList(member.getUserId()),
                new OnHttpImListener<String>() {
                    @Override
                    public void onHttpImSuccess(@NonNull String result) {
                        runOnUiThread(() -> {
                            Toast.makeText(GroupMemberListActivity.this,
                                    R.string.group_kick_success, Toast.LENGTH_SHORT).show();
                            loadMembers();
                        });
                    }
                }, () -> runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(GroupMemberListActivity.this,
                            R.string.group_kick_failed, Toast.LENGTH_SHORT).show();
                }));
    }
}
