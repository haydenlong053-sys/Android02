package com.freddy.chat.group;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.cboda.intelligent.R;
import com.freddy.chat.im.IMSClientBootstrap;
import com.freddy.chat.utils.AvatarViews;
import com.freddy.im.db.ImLocalStore;
import com.freddy.im.http.ImGroupHttpExample;
import com.freddy.im.http.model.im.BaseMsgResponse;
import com.freddy.im.http.model.im.GroupQrCodeRes;
import com.freddy.im.http.model.im.GroupUserVo;
import com.freddy.im.http.model.im.GroupVo;
import com.freddy.im.listener.OnHttpImListener;

import java.util.List;

/** 群聊资料 / 管理页（微信风格）。
 * ui不完整，暂不使用，仅作自研参考
 * */
@Deprecated
public class GroupInfoActivity extends AppCompatActivity {

    private String groupId;
    @Nullable
    private GroupVo groupVo;
    @Nullable
    private String currentUserId;
    private boolean isOwner;

    private View avatarRoot;
    private TextView tvGroupName;
    private TextView tvGroupId;
    private ProgressBar progressBar;

    private GroupSettingRow rowMembers;
    private GroupSettingRow rowNotice;
    private GroupSettingRow rowQrcode;
    private GroupSettingRow rowAddMember;
    private GroupSettingRow rowRename;
    private GroupSettingRow rowTransfer;
    private GroupSettingRow rowApplyList;
    private GroupSettingRow rowLeave;
    private GroupSettingRow rowDisband;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_info);
        setTitle(R.string.group_info_title);

        groupId = GroupIntent.getGroupId(getIntent());
        groupVo = GroupIntent.getGroupVo(getIntent());
        currentUserId = IMSClientBootstrap.getInstance().getCurrentUserId();
        if (TextUtils.isEmpty(groupId)) {
            Toast.makeText(this, R.string.group_id_invalid, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        avatarRoot = findViewById(R.id.avatar);
        tvGroupName = findViewById(R.id.tv_group_name);
        tvGroupId = findViewById(R.id.tv_group_id);
        progressBar = findViewById(R.id.progress);

        rowMembers = new GroupSettingRow(findViewById(R.id.row_members));
        rowNotice = new GroupSettingRow(findViewById(R.id.row_notice));
        rowQrcode = new GroupSettingRow(findViewById(R.id.row_qrcode));
        rowAddMember = new GroupSettingRow(findViewById(R.id.row_add_member));
        rowRename = new GroupSettingRow(findViewById(R.id.row_rename));
        rowTransfer = new GroupSettingRow(findViewById(R.id.row_transfer));
        rowApplyList = new GroupSettingRow(findViewById(R.id.row_apply_list));
        rowLeave = new GroupSettingRow(findViewById(R.id.row_leave));
        rowDisband = new GroupSettingRow(findViewById(R.id.row_disband));

        bindSettingRows();
        bindHeaderFromCache();
        loadGroupDetail();
    }

    private void bindSettingRows() {
        rowMembers.bind(getString(R.string.group_row_members), null, true);
        rowNotice.bind(getString(R.string.group_row_notice), null, true);
        rowQrcode.bind(getString(R.string.group_row_qrcode), null, true);
        rowAddMember.bind(getString(R.string.group_row_add_member), null, true);
        rowRename.bind(getString(R.string.group_row_rename), null, true);
        rowTransfer.bind(getString(R.string.group_row_transfer), null, true);
        rowApplyList.bind(getString(R.string.group_row_apply_list), null, true);
        rowLeave.bind(getString(R.string.group_row_leave), null, false);
        rowDisband.bind(getString(R.string.group_row_disband), null, false);
        rowDisband.label.setTextColor(ContextCompat.getColor(this, R.color.wechat_unread_badge));
        rowLeave.label.setTextColor(ContextCompat.getColor(this, R.color.wechat_unread_badge));
        rowDisband.hideDivider();

        rowMembers.root.setOnClickListener(v -> openMembers());
        rowNotice.root.setOnClickListener(v -> openNotice());
        rowQrcode.root.setOnClickListener(v -> showQrCode());
        rowAddMember.root.setOnClickListener(v -> openAddMember());
        rowRename.root.setOnClickListener(v -> showRenameDialog());
        rowTransfer.root.setOnClickListener(v -> openTransferOwner());
        rowApplyList.root.setOnClickListener(v -> openApplyList());
        rowLeave.root.setOnClickListener(v -> confirmLeave());
        rowDisband.root.setOnClickListener(v -> confirmDisband());
    }

    private void bindHeaderFromCache() {
        if (groupVo == null) {
            tvGroupId.setText(groupId);
            return;
        }
        applyGroupVo(groupVo);
    }

    private void loadGroupDetail() {
        progressBar.setVisibility(View.VISIBLE);
        ImGroupHttpExample.requestGetGroupDetail(groupId, new OnHttpImListener<GroupVo>() {
            @Override
            public void onHttpImSuccess(@NonNull GroupVo result) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    groupVo = result;
                    applyGroupVo(result);
                });
            }
        }, () -> runOnUiThread(() -> {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(GroupInfoActivity.this, R.string.group_detail_load_failed, Toast.LENGTH_SHORT).show();
        }));
    }

    private void applyGroupVo(@NonNull GroupVo vo) {
        String name = TextUtils.isEmpty(vo.getName())
                ? getString(R.string.conversation_name_fallback) : vo.getName();
        tvGroupName.setText(name);
        tvGroupId.setText(vo.getId());
        AvatarViews.bind(avatarRoot, vo.getIcon(), name, name);
        isOwner = !TextUtils.isEmpty(currentUserId)
                && currentUserId.equals(vo.getOwerId());
        String countText = vo.getMemCount() > 0 ? vo.getMemCount() + "人" : null;
        rowMembers.bind(getString(R.string.group_row_members), countText, true);
        updateOwnerOnlyRows();
    }

    private void updateOwnerOnlyRows() {
        int ownerVis = isOwner ? View.VISIBLE : View.GONE;
        rowAddMember.root.setVisibility(ownerVis);
        rowRename.root.setVisibility(ownerVis);
        rowTransfer.root.setVisibility(ownerVis);
        rowApplyList.root.setVisibility(ownerVis);
        rowDisband.root.setVisibility(isOwner ? View.VISIBLE : View.GONE);
        rowLeave.root.setVisibility(isOwner ? View.GONE : View.VISIBLE);
    }

    private void openMembers() {
        Intent intent = new Intent(this, GroupMemberListActivity.class);
        GroupIntent.put(intent, groupId, groupVo);
        GroupIntent.setOwner(intent, isOwner);
        startActivity(intent);
    }

    private void openNotice() {
        Intent intent = new Intent(this, GroupNoticeActivity.class);
        GroupIntent.put(intent, groupId);
        GroupIntent.setOwner(intent, isOwner);
        startActivity(intent);
    }

    private void showQrCode() {
        progressBar.setVisibility(View.VISIBLE);
        ImGroupHttpExample.requestGetGroupQrCode(groupId, new OnHttpImListener<GroupQrCodeRes>() {
            @Override
            public void onHttpImSuccess(@NonNull GroupQrCodeRes result) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    String url = result.getCodeUrl();
                    new AlertDialog.Builder(GroupInfoActivity.this)
                            .setTitle(R.string.group_row_qrcode)
                            .setMessage(TextUtils.isEmpty(url) ? getString(R.string.group_qrcode_empty) : url)
                            .setPositiveButton(android.R.string.ok, null)
                            .show();
                });
            }
        }, () -> runOnUiThread(() -> {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(GroupInfoActivity.this, R.string.group_qrcode_load_failed, Toast.LENGTH_SHORT).show();
        }));
    }

    private void openAddMember() {
        Intent intent = new Intent(this, GroupAddMemberActivity.class);
        GroupIntent.put(intent, groupId);
        startActivity(intent);
    }

    private void showRenameDialog() {
        EditText input = new EditText(this);
        input.setHint(R.string.create_group_name_hint);
        if (groupVo != null && !TextUtils.isEmpty(groupVo.getName())) {
            input.setText(groupVo.getName());
            input.setSelection(groupVo.getName().length());
        }
        int pad = getResources().getDimensionPixelSize(R.dimen.wechat_list_item_padding_h);
        input.setPadding(pad, pad / 2, pad, pad / 2);
        new AlertDialog.Builder(this)
                .setTitle(R.string.group_row_rename)
                .setView(input)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_confirm, (d, w) -> {
                    String name = input.getText().toString().trim();
                    if (TextUtils.isEmpty(name)) {
                        return;
                    }
                    renameGroup(name);
                })
                .show();
    }

    private void renameGroup(@NonNull String name) {
        progressBar.setVisibility(View.VISIBLE);
        ImGroupHttpExample.requestModifyGroup(groupId, name, null, new OnHttpImListener<GroupVo>() {
            @Override
            public void onHttpImSuccess(@NonNull GroupVo result) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    groupVo = result;
                    applyGroupVo(result);
                    Toast.makeText(GroupInfoActivity.this, R.string.group_rename_success, Toast.LENGTH_SHORT).show();
                });
            }
        }, () -> runOnUiThread(() -> {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(GroupInfoActivity.this, R.string.group_rename_failed, Toast.LENGTH_SHORT).show();
        }));
    }

    private void openTransferOwner() {
        progressBar.setVisibility(View.VISIBLE);
        ImGroupHttpExample.requestGetAllMembers(groupId, new OnHttpImListener<List<GroupUserVo>>() {
            @Override
            public void onHttpImSuccess(@NonNull List<GroupUserVo> members) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    showTransferPicker(members);
                });
            }
        }, () -> runOnUiThread(() -> {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(GroupInfoActivity.this, R.string.group_members_load_failed, Toast.LENGTH_SHORT).show();
        }));
    }

    private void showTransferPicker(@NonNull List<GroupUserVo> members) {
        List<GroupUserVo> candidates = new java.util.ArrayList<>();
        List<String> labels = new java.util.ArrayList<>();
        for (GroupUserVo m : members) {
            if (m == null || TextUtils.isEmpty(m.getUserId())) {
                continue;
            }
            if (m.getUserId().equals(currentUserId)) {
                continue;
            }
            candidates.add(m);
            labels.add(m.getDisplayName() + " (" + m.getUserId() + ")");
        }
        if (candidates.isEmpty()) {
            Toast.makeText(this, R.string.group_transfer_no_candidate, Toast.LENGTH_SHORT).show();
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.group_row_transfer)
                .setItems(labels.toArray(new String[0]), (d, which) -> {
                    GroupUserVo target = candidates.get(which);
                    confirmTransfer(target.getUserId(), target.getDisplayName());
                })
                .show();
    }

    private void confirmTransfer(@NonNull String newOwnerId, @NonNull String displayName) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.group_row_transfer)
                .setMessage(getString(R.string.group_transfer_confirm, displayName))
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_confirm, (d, w) -> doTransfer(newOwnerId))
                .show();
    }

    private void doTransfer(@NonNull String newOwnerId) {
        progressBar.setVisibility(View.VISIBLE);
        ImGroupHttpExample.requestTransferOwner(groupId, newOwnerId,
                new OnHttpImListener<BaseMsgResponse<Void>>() {
                    @Override
                    public void onHttpImSuccess(@NonNull BaseMsgResponse<Void> result) {
                        runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(GroupInfoActivity.this,
                                    R.string.group_transfer_success, Toast.LENGTH_SHORT).show();
                            loadGroupDetail();
                        });
                    }
                }, () -> runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(GroupInfoActivity.this,
                            R.string.group_transfer_failed, Toast.LENGTH_SHORT).show();
                }));
    }

    private void openApplyList() {
        startActivity(new Intent(this, GroupApplyListActivity.class));
    }

    private void confirmLeave() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.group_row_leave)
                .setMessage(R.string.group_leave_confirm)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_confirm, (d, w) -> doLeave())
                .show();
    }

    private void onGroupSessionRemoved() {
        ImLocalStore.removeSessionCompletely(groupId, () -> {
            progressBar.setVisibility(View.GONE);
            setResult(RESULT_OK);
            finish();
        });
    }

    private void doLeave() {
        progressBar.setVisibility(View.VISIBLE);
        ImGroupHttpExample.requestLeaveGroup(groupId, new OnHttpImListener<String>() {
            @Override
            public void onHttpImSuccess(@NonNull String result) {
                runOnUiThread(() -> {
                    Toast.makeText(GroupInfoActivity.this, R.string.group_leave_success, Toast.LENGTH_SHORT).show();
                    onGroupSessionRemoved();
                });
            }
        }, () -> runOnUiThread(() -> {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(GroupInfoActivity.this, R.string.group_leave_failed, Toast.LENGTH_SHORT).show();
        }));
    }

    private void confirmDisband() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.group_row_disband)
                .setMessage(R.string.group_disband_confirm)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_confirm, (d, w) -> doDisband())
                .show();
    }

    private void doDisband() {
        progressBar.setVisibility(View.VISIBLE);
        ImGroupHttpExample.requestDisbandGroup(groupId, new OnHttpImListener<BaseMsgResponse<Void>>() {
            @Override
            public void onHttpImSuccess(@NonNull BaseMsgResponse<Void> result) {
                runOnUiThread(() -> {
                    Toast.makeText(GroupInfoActivity.this, R.string.group_disband_success, Toast.LENGTH_SHORT).show();
                    onGroupSessionRemoved();
                });
            }
        }, () -> runOnUiThread(() -> {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(GroupInfoActivity.this, R.string.group_disband_failed, Toast.LENGTH_SHORT).show();
        }));
    }
}
