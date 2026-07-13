package com.freddy.chat.merge;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.cboda.chatui.R;
import com.cboda.chatui.third.utils.TimeConvertBroadUtils;
import com.cboda.chatui.utils.FileBroadUtils;
import com.freddy.chat.utils.MediaMessageContentUtil;
import com.freddy.im.MessageType;
import com.freddy.im.http.model.im.GroupMemberItem;
import com.freddy.im.util.ImUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 合并转发详情列表（对齐野火 {@link com.cboda.chatui.conversation.message.CompositeMessageContentBroadAdapter2}）。
 */
public final class MergeTranspondDetailAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_MESSAGE = 1;

    @NonNull
    private final List<MergeTranspondMessageItem> items;
    @NonNull
    private final Map<String, GroupMemberItem> membersByUserId;
    @Nullable
    private final String currentUserId;
    @Nullable
    private final String peerDisplayName;
    private final boolean isGroupChat;

    public MergeTranspondDetailAdapter(@NonNull List<MergeTranspondMessageItem> items,
                                       @NonNull Map<String, GroupMemberItem> membersByUserId,
                                       @Nullable String currentUserId,
                                       @Nullable String peerDisplayName,
                                       boolean isGroupChat) {
        this.items = items;
        this.membersByUserId = membersByUserId;
        this.currentUserId = currentUserId;
        this.peerDisplayName = peerDisplayName;
        this.isGroupChat = isGroupChat;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_HEADER) {
            return new HeaderViewHolder(inflater.inflate(
                    R.layout.composite_message_item_broad_header, parent, false));
        }
        return new MessageItemViewHolder(inflater.inflate(
                R.layout.composite_message_broad_item2, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (position == 0) {
            ((HeaderViewHolder) holder).bind(items.size());
            return;
        }
        ((MessageItemViewHolder) holder).bind(items, position - 1, this);
    }

    @Override
    public int getItemCount() {
        return items.isEmpty() ? 0 : 1 + items.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? TYPE_HEADER : TYPE_MESSAGE;
    }

    @NonNull
    private Map<String, String> buildDisplayNameMap() {
        Map<String, String> map = new HashMap<>(membersByUserId.size() + 1);
        for (Map.Entry<String, GroupMemberItem> entry : membersByUserId.entrySet()) {
            GroupMemberItem member = entry.getValue();
            if (member != null) {
                String name = member.getDisplayName();
                if (!TextUtils.isEmpty(name)) {
                    map.put(entry.getKey(), name);
                }
            }
        }
        if (!isGroupChat && !TextUtils.isEmpty(peerDisplayName)) {
            for (MergeTranspondMessageItem item : items) {
                if (!TextUtils.isEmpty(item.getFromUserId())) {
                    map.putIfAbsent(item.getFromUserId(), peerDisplayName);
                }
            }
        }
        return map;
    }

    private static final class HeaderViewHolder extends RecyclerView.ViewHolder {
        private final TextView compositeDurationTextView;

        HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            compositeDurationTextView = itemView.findViewById(R.id.compositeDurationTextView);
        }

        void bind(int count) {
            compositeDurationTextView.setText("共 " + count + " 条聊天记录");
        }
    }

    private static final class MessageItemViewHolder extends RecyclerView.ViewHolder {
        private final ImageView portraitImageView;
        private final TextView nameTextView;
        private final TextView timeTextView;
        private final LinearLayout imageContentLayout;
        private final ImageView contentImageView;
        private final LinearLayout textContentLayout;
        private final TextView contentTextView;
        private final LinearLayout fileContentLayout;
        private final ImageView fileIconImageView;
        private final TextView fileNameTextView;
        private final TextView fileSizeTextView;
        private final LinearLayout videoContentLayout;
        private final TextView videoDurationTextView;
        private final ImageView videoThumbnailImageView;
        private final LinearLayout compositeContentLayout;
        private final TextView compositeTitleTextView;
        private final TextView compositeContentTextView;

        MessageItemViewHolder(@NonNull View itemView) {
            super(itemView);
            portraitImageView = itemView.findViewById(R.id.portraitImageView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            timeTextView = itemView.findViewById(R.id.timeTextView);
            imageContentLayout = itemView.findViewById(R.id.imageContentLayout);
            contentImageView = itemView.findViewById(R.id.contentImageView);
            textContentLayout = itemView.findViewById(R.id.textContentLayout);
            contentTextView = itemView.findViewById(R.id.contentTextView);
            fileContentLayout = itemView.findViewById(R.id.fileContentLayout);
            fileIconImageView = itemView.findViewById(R.id.fileIconImageView);
            fileNameTextView = itemView.findViewById(R.id.fileNameTextView);
            fileSizeTextView = itemView.findViewById(R.id.fileSizeTextView);
            videoContentLayout = itemView.findViewById(R.id.videoContentLayout);
            videoDurationTextView = itemView.findViewById(R.id.videoDurationTextView);
            videoThumbnailImageView = itemView.findViewById(R.id.videoThumbnailImageView);
            compositeContentLayout = itemView.findViewById(R.id.compositeContentLayout);
            compositeTitleTextView = itemView.findViewById(R.id.compositeTitleTextView);
            compositeContentTextView = itemView.findViewById(R.id.compositeContentTextView);
        }

        void bind(@NonNull List<MergeTranspondMessageItem> items,
                  int position,
                  @NonNull MergeTranspondDetailAdapter parent) {
            MergeTranspondMessageItem item = items.get(position);
            Map<String, String> nameMap = parent.buildDisplayNameMap();
            String userId = item.getFromUserId();
            nameTextView.setText(MergeTranspondMessageContentUtil.resolveDisplayName(
                    userId, parent.currentUserId, nameMap));
            timeTextView.setVisibility(View.GONE);

            bindPortrait(items, position, parent, userId);
            bindBody(item, parent, nameMap);
        }

        private void bindPortrait(@NonNull List<MergeTranspondMessageItem> items,
                                  int position,
                                  @NonNull MergeTranspondDetailAdapter parent,
                                  @Nullable String userId) {
            if (position == 0) {
                showPortrait(userId, parent);
                return;
            }
            String prevUserId = items.get(position - 1).getFromUserId();
            if (TextUtils.equals(prevUserId, userId)) {
                portraitImageView.setVisibility(View.INVISIBLE);
            } else {
                showPortrait(userId, parent);
            }
        }

        private void showPortrait(@Nullable String userId, @NonNull MergeTranspondDetailAdapter parent) {
            portraitImageView.setVisibility(View.VISIBLE);
            String portrait = MergeTranspondMessageContentUtil.resolvePortrait(
                    userId, parent.membersByUserId);
            Glide.with(itemView)
                    .load(TextUtils.isEmpty(portrait) ? null : portrait)
                    .transforms(new CenterCrop(), new RoundedCorners(10))
                    .placeholder(R.mipmap.avatar_broad_def)
                    .into(portraitImageView);
        }

        private void bindBody(@NonNull MergeTranspondMessageItem item,
                              @NonNull MergeTranspondDetailAdapter parent,
                              @NonNull Map<String, String> nameMap) {
            int contentType = item.getContentType();
            String contentJson = item.getContent();
            hideAllBodies();
            if (MessageType.MessageContentType.isImage(contentType)) {
                imageContentLayout.setVisibility(View.VISIBLE);
                String url = MediaMessageContentUtil.resolveDisplayUrl(contentJson, false);
                Glide.with(itemView).load(url).into(contentImageView);
                return;
            }
            if (MessageType.MessageContentType.isVideo(contentType)) {
                videoContentLayout.setVisibility(View.VISIBLE);
                int durationSec = MediaMessageContentUtil.resolveVoiceDurationSec(contentJson);
                videoDurationTextView.setText(TimeConvertBroadUtils.formatLongTime(durationSec));
                String cover = MediaMessageContentUtil.resolveCoverDisplayUrl(contentJson, false);
                Glide.with(itemView).load(cover).into(videoThumbnailImageView);
                return;
            }
            if (MessageType.MessageContentType.isFile(contentType)) {
                fileContentLayout.setVisibility(View.VISIBLE);
                String fileName = MediaMessageContentUtil.resolveFileName(contentJson);
                fileNameTextView.setText(fileName);
                long sizeBytes = MediaMessageContentUtil.resolveFileSizeBytes(contentJson, false);
                if (sizeBytes > 0L) {
                    fileSizeTextView.setVisibility(View.VISIBLE);
                    fileSizeTextView.setText(ImUtils.getShowFileSize(sizeBytes));
                } else {
                    fileSizeTextView.setVisibility(View.GONE);
                }
                fileIconImageView.setImageResource(FileBroadUtils.getFileTypeImageResId(fileName));
                return;
            }
            if (MessageType.MessageContentType.isMergeTranspond(contentType)) {
                compositeContentLayout.setVisibility(View.VISIBLE);
                List<MergeTranspondMessageItem> nested = MergeTranspondMessageContentUtil.parseMessageList(contentJson);
                compositeTitleTextView.setText(MergeTranspondMessageContentUtil.buildTitle(
                        parent.isGroupChat, parent.peerDisplayName));
                compositeContentTextView.setText(MergeTranspondMessageContentUtil.buildCompositeDigest(
                        nested, parent.currentUserId, nameMap));
                return;
            }
            textContentLayout.setVisibility(View.VISIBLE);
            contentTextView.setText(MergeTranspondMessageContentUtil.formatItemDigest(contentType, contentJson));
        }

        private void hideAllBodies() {
            textContentLayout.setVisibility(View.GONE);
            imageContentLayout.setVisibility(View.GONE);
            fileContentLayout.setVisibility(View.GONE);
            videoContentLayout.setVisibility(View.GONE);
            compositeContentLayout.setVisibility(View.GONE);
        }
    }
}
