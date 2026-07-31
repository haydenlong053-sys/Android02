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

import com.cboda.intelligent.R;
import com.freddy.chat.utils.WeChatUiHelper;
import com.freddy.im.http.ImGroupHttpExample;
import com.freddy.im.http.model.im.BaseMsgResponse;
import com.freddy.im.http.model.im.GroupNoticeRes;
import com.freddy.im.listener.OnHttpImListener;

/** 群公告查看/编辑（§3.19 / §3.20）。 */
public class GroupNoticeActivity extends AppCompatActivity {

    private String groupId;
    private boolean isOwner;

    private EditText etNotice;
    private TextView tvMeta;
    private Button btnSave;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_notice);
        setTitle(com.cboda.baselibrary.R.string.group_notice_title);

        groupId = GroupIntent.getGroupId(getIntent());
        isOwner = GroupIntent.isOwner(getIntent());
        if (TextUtils.isEmpty(groupId)) {
            finish();
            return;
        }

        etNotice = findViewById(R.id.et_notice);
        tvMeta = findViewById(R.id.tv_notice_meta);
        btnSave = findViewById(R.id.btn_save_notice);
        progressBar = findViewById(R.id.progress);

        etNotice.setEnabled(isOwner);
        btnSave.setVisibility(isOwner ? View.VISIBLE : View.GONE);
        btnSave.setOnClickListener(v -> saveNotice());

        loadNotice();
    }

    private void loadNotice() {
        progressBar.setVisibility(View.VISIBLE);
        ImGroupHttpExample.requestGetGroupNotice(groupId, new OnHttpImListener<GroupNoticeRes>() {
            @Override
            public void onHttpImSuccess(@NonNull GroupNoticeRes result) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    if (result != null && !TextUtils.isEmpty(result.getContent())) {
                        etNotice.setText(result.getContent());
                    }
                    if (result != null && (result.getPublishTime() > 0 || !TextUtils.isEmpty(result.getPublisher()))) {
                        tvMeta.setVisibility(View.VISIBLE);
                        String time = result.getPublishTime() > 0
                                ? WeChatUiHelper.formatMessageTime(result.getPublishTime()) : "";
                        String publisher = result.getPublisher() != null ? result.getPublisher() : "";
                        tvMeta.setText(getString(com.cboda.baselibrary.R.string.group_notice_meta, publisher, time));
                    }
                });
            }
        }, () -> runOnUiThread(() -> progressBar.setVisibility(View.GONE)));
    }

    private void saveNotice() {
        String content = etNotice.getText().toString().trim();
        if (TextUtils.isEmpty(content)) {
            Toast.makeText(this, com.cboda.baselibrary.R.string.group_notice_empty, Toast.LENGTH_SHORT).show();
            return;
        }
        btnSave.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        ImGroupHttpExample.requestUpdateGroupNotice(groupId, content,
                new OnHttpImListener<BaseMsgResponse<Void>>() {
                    @Override
                    public void onHttpImSuccess(@NonNull BaseMsgResponse<Void> result) {
                        runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            btnSave.setEnabled(true);
                            Toast.makeText(GroupNoticeActivity.this,
                                    com.cboda.baselibrary.R.string.group_notice_save_success, Toast.LENGTH_SHORT).show();
                            loadNotice();
                        });
                    }
                }, () -> runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    btnSave.setEnabled(true);
                    Toast.makeText(GroupNoticeActivity.this,
                            com.cboda.baselibrary.R.string.group_notice_save_failed, Toast.LENGTH_SHORT).show();
                }));
    }
}
