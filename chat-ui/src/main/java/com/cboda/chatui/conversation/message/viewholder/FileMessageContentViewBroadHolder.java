/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.conversation.message.viewholder;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import java.io.File;

import com.cboda.chatui.*;
import com.cboda.chatui.annotation.EnableContextBroadMenu;
import com.cboda.chatui.annotation.MessageContentBroadType;
import com.cboda.chatui.annotation.MessageContextMenuBroadItem;
import com.cboda.chatui.conversation.ConversationBroadFragment;
import com.cboda.chatui.conversation.message.model.UiBroadMessage;
import com.cboda.chatui.utils.DownloadBroadManager;
import com.cboda.chatui.utils.FileBroadUtils;
import cn.wildfirechat.message.FileMessageContent;

@MessageContentBroadType(FileMessageContent.class)
@EnableContextBroadMenu
public class FileMessageContentViewBroadHolder extends MediaMessageContentViewBroadHolder {
    ImageView fileIconImageView;
    TextView nameTextView;
    TextView sizeTextView;

    private FileMessageContent fileMessageContent;

    public FileMessageContentViewBroadHolder(ConversationBroadFragment fragment, RecyclerView.Adapter adapter, View itemView) {
        super(fragment, adapter, itemView);
        bindViews(itemView);
        bindEvents(itemView);
    }

    private void bindEvents(View itemView) {
       itemView.findViewById(R.id.fileMessageContentItemView).setOnClickListener(this::onClick);
    }

    private void bindViews(View itemView) {
        fileIconImageView =itemView.findViewById(R.id.fileIconImageView);
        nameTextView =itemView.findViewById(R.id.fileNameTextView);
        sizeTextView =itemView.findViewById(R.id.fileSizeTextView);
    }

    @Override
    public void onBind(UiBroadMessage message) {
        super.onBind(message);
        fileMessageContent = (FileMessageContent) message.message.content;
        nameTextView.setText(fileMessageContent.getName());
        sizeTextView.setText(FileBroadUtils.getReadableFileSize(fileMessageContent.getSize()));
        fileIconImageView.setImageResource(FileBroadUtils.getFileTypeImageResId(fileMessageContent.getName()));
    }

    public void onClick(View view) {
        if (message.isDownloading) {
            return;
        }
        FileBroadUtils.openFile(fragment.getContext(), message.message);
    }

    @MessageContextMenuBroadItem(tag = MessageContextMenuItemBroadTags.TAG_SAVE_FILE, confirm = false, priority = 14)
    public void saveFile(View itemView, UiBroadMessage message) {
        File file = DownloadBroadManager.mediaMessageContentFile(message.message);
        if (file == null || !file.exists()) {
            Toast.makeText(fragment.getContext(), "请先点击下载文件", Toast.LENGTH_SHORT).show();
            return;
        }

        File dstFile = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            dstFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/" + file.getName());
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            dstFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/" + file.getName());
        } else {
            dstFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + file.getName());
        }

        boolean result = FileBroadUtils.copyFile(file, dstFile);
        if (result) {
            Toast.makeText(fragment.getContext(), "文件保存成功", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(fragment.getContext(), "文件保存失败", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public String contextMenuTitle(Context context, String tag) {
        if (MessageContextMenuItemBroadTags.TAG_SAVE_FILE.equals(tag)) {
            return "存储到手机";
        }
        return super.contextMenuTitle(context, tag);
    }

}
