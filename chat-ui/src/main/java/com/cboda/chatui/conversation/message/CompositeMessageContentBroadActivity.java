/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.conversation.message;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;

import com.cboda.baselibrary.utils.OnSingleClickBroadListener;
import com.cboda.chatui.BroadConfig;
import com.cboda.chatui.R;
import com.cboda.chatui.WfcBaseBroadActivity;
import com.cboda.chatui.mm.MMPreviewBroadActivity;
import com.cboda.chatui.third.utils.UIBroadUtils;
import com.cboda.chatui.utils.DownloadBroadManager;
import com.cboda.chatui.utils.FileBroadUtils;
import cn.wildfirechat.message.CompositeMessageContent;
import cn.wildfirechat.message.FileMessageContent;
import cn.wildfirechat.message.ImageMessageContent;
import cn.wildfirechat.message.Message;
import cn.wildfirechat.message.MessageContent;
import cn.wildfirechat.message.VideoMessageContent;
import cn.wildfirechat.model.Conversation;
import cn.wildfirechat.remote.ChatManager;

//多选聊天记录合成页
public class CompositeMessageContentBroadActivity extends WfcBaseBroadActivity implements CompositeMessageContentBroadAdapter2.OnMessageClickListener {
    RecyclerView recyclerView;
    TextView mTvTitle;
    private CompositeMessageContentBroadAdapter2 adapter;

    protected void bindViews() {
        super.bindViews();
        recyclerView = findViewById(R.id.recyclerView);
        mTvTitle=findViewById(com.cboda.baselibrary.R.id.mTvTitle);
        findViewById(com.cboda.baselibrary.R.id.mViewBack).setOnClickListener(new OnSingleClickBroadListener() {
            @Override
            public void onSingleClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected int contentLayout() {
        return R.layout.composite_message_broad_activity;
    }

    @Override
    protected void afterViews() {
        Message message = getIntent().getParcelableExtra("message");
        if (message == null || !(message.content instanceof CompositeMessageContent)) {
            finish();
            return;
        }
        CompositeMessageContent content = (CompositeMessageContent) message.content;
        setTitle(content.getTitle());
        if (!((CompositeMessageContent) message.content).isLoaded()) {
            File file = DownloadBroadManager.mediaMessageContentFile(message);
            if (!TextUtils.isEmpty(content.remoteUrl) && !file.exists()) {
                String fileUrl = content.remoteUrl;
                if (message.conversation.type == Conversation.ConversationType.SecretChat) {
                    fileUrl = DownloadBroadManager.buildSecretChatMediaUrl(message);
                }
                Toast.makeText(this, "消息加载中，请稍后", Toast.LENGTH_SHORT).show();
                DownloadBroadManager.download(fileUrl, BroadConfig.FILE_SAVE_DIR, new DownloadBroadManager.OnDownloadListener() {
                    @Override
                    public void onSuccess(File file) {
                        content.localPath = file.getAbsolutePath();
                        ChatManager.Instance().updateMessage(message.messageId, content);
                        UIBroadUtils.postTaskSafely(() -> {
                            adapter.notifyDataSetChanged();
                        });
                    }

                    @Override
                    public void onProgress(int progress) {

                    }

                    @Override
                    public void onFail() {
                        UIBroadUtils.postTaskSafely(() -> {
                            Toast.makeText(CompositeMessageContentBroadActivity.this, "加载消息失败", Toast.LENGTH_SHORT).show();
                        });
                    }
                });
            }
        }
        adapter = new CompositeMessageContentBroadAdapter2(message, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onClickMessage(Message message) {
        //暂不启用，跟ios逻辑保持一致
//        MessageContent content = message.content;
//        if (content instanceof FileMessageContent) {
//            FileBroadUtils.openFile(this, message);
//        } else if (content instanceof VideoMessageContent) {
//            MMPreviewBroadActivity.previewVideo(this, message);
//        } else if (content instanceof ImageMessageContent) {
//            MMPreviewBroadActivity.previewImage(this, message);
//        } else if (content instanceof CompositeMessageContent) {
//            Intent intent = new Intent(this, CompositeMessageContentBroadActivity.class);
//            intent.putExtra("message", message);
//            startActivity(intent);
//        }
    }
}
