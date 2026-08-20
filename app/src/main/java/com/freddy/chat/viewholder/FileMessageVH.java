package com.freddy.chat.viewholder;

import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import com.cboda.baselibrary.utils.ELogUtilKt;
import com.cboda.chatui.utils.FileBroadUtils;
import com.cboda.intelligent.R;
import com.freddy.chat.utils.MediaMessageContentUtil;
import com.freddy.im.bean.AppMessage;
import com.freddy.im.util.ImUtils;

import java.io.File;

public final class FileMessageVH extends BaseMessageVH {
    private final View fileRoot;
    private final ImageView fileIconImageView;
    private final TextView fileNameTextView;
    private final TextView fileSizeTextView;

    public FileMessageVH(@NonNull View itemView) {
        super(itemView, R.layout.item_message_content_file, false);
        fileRoot = contentView.findViewById(R.id.fileMessageContentItemView);
        fileIconImageView = contentView.findViewById(R.id.fileIconImageView);
        fileNameTextView = contentView.findViewById(R.id.fileNameTextView);
        fileSizeTextView = contentView.findViewById(R.id.fileSizeTextView);
    }

    public void bind(@NonNull AppMessage message, int position, @NonNull MessageViewHolderContext context) {
        ELogUtilKt.eLogD(ELogUtilKt.APP_IM_TAG, "文件消息getContent--->" + message.getContent());
        bindCommon(message, position, context);
        boolean sentByMe = message.isSender(context.currentUserId);
        applyWhiteBubbleBackground(fileRoot, sentByMe);

        String fileName = MediaMessageContentUtil.resolveFileName(message.getContent());
        long sizeBytes = MediaMessageContentUtil.resolveFileSizeBytes(message.getContent(), sentByMe);
        fileNameTextView.setText(fileName);
        if (sizeBytes > 0L) {
            fileSizeTextView.setVisibility(View.VISIBLE);
            fileSizeTextView.setText(ImUtils.getShowFileSize(sizeBytes));
        } else {
            fileSizeTextView.setVisibility(View.GONE);
        }
        fileIconImageView.setImageResource(FileBroadUtils.getFileTypeImageResId(fileName));

        fileRoot.setOnClickListener(v -> openFile(v, message, sentByMe));
        refreshContextMenuLongClickOnContent();
    }

    private void openFile(@NonNull View anchor, @NonNull AppMessage message, boolean sentByMe) {
        String path = MediaMessageContentUtil.resolveDisplayUrl(message.getContent(), sentByMe);
        if (path.isEmpty()) {
            Toast.makeText(anchor.getContext(), com.cboda.baselibrary.R.string.file_pick_failed, Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            Uri uri;
            if (path.startsWith("http://") || path.startsWith("https://")) {
                uri = Uri.parse(path);
            } else {
                File file = new File(path);
                if (!file.isFile()) {
                    Toast.makeText(anchor.getContext(), com.cboda.baselibrary.R.string.file_pick_failed, Toast.LENGTH_SHORT).show();
                    return;
                }
                uri = FileProvider.getUriForFile(
                        anchor.getContext(),
                        anchor.getContext().getPackageName() + ".provider",
                        file);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
            intent.setDataAndType(uri, "*/*");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            anchor.getContext().startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(anchor.getContext(), com.cboda.baselibrary.R.string.file_pick_failed, Toast.LENGTH_SHORT).show();
        }
    }
}
