package com.freddy.chat;

import android.os.Handler;
import android.os.Looper;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.cboda.baselibrary.widge.VpGradeView;
import com.cboda.intelligent.R;
import com.freddy.chat.mention.AtMentionChecker;
import com.freddy.chat.utils.MessageAvatarBinder;
import com.freddy.chat.utils.WeChatUiHelper;
import com.freddy.im.http.model.im.SessionItem;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 主会话列表：未读/预览用 payload 局部刷新；DiffUtil 在后台计算，避免主线程卡顿。
 */
public final class ConversationListAdapter extends RecyclerView.Adapter<ConversationListAdapter.VH> {

    public interface OnSessionClickListener {
        void onSessionClick(SessionItem sessionItem);
    }

    public interface OnSessionLongClickListener {
        boolean onSessionLongClick(SessionItem sessionItem);
    }

    /** 仅未读角标。 */
    private static final Object PAYLOAD_UNREAD = new Object();
    /** 未读 + 最近消息预览（不含头像/昵称）。 */
    private static final Object PAYLOAD_PREVIEW = new Object();
    /** 仅勾选框。 */
    private static final Object PAYLOAD_SELECT = new Object();

    private static final ExecutorService DIFF_EXECUTOR = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "session-list-diff");
        t.setDaemon(true);
        t.setPriority(Thread.NORM_PRIORITY - 1);
        return t;
    });

    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    private final List<SessionItem> items = new ArrayList<>();
    private final Set<String> selectedSessionIds = new HashSet<>();
    private final AtomicInteger dataGeneration = new AtomicInteger(0);
    private boolean selectMode;

    @Nullable
    private String currentUserId;
    @Nullable
    private OnSessionClickListener onSessionClickListener;
    @Nullable
    private OnSessionLongClickListener onSessionLongClickListener;

    public void setOnSessionClickListener(@Nullable OnSessionClickListener listener) {
        this.onSessionClickListener = listener;
    }

    public void setCurrentUserId(@Nullable String currentUserId) {
        this.currentUserId = currentUserId;
    }

    public void setOnSessionLongClickListener(@Nullable OnSessionLongClickListener listener) {
        this.onSessionLongClickListener = listener;
    }

    public boolean isSelectMode() {
        return selectMode;
    }

    /** 进入/退出多选管理模式。清空勾选请走 {@link #closeSelect()}。 */
    public void updateSelectMode(boolean selectMode) {
        this.selectMode = selectMode;
        notifyDataSetChanged();
    }

    @NonNull
    public List<SessionItem> getData() {
        return new ArrayList<>(items);
    }

    @NonNull
    public List<SessionItem> getSelectInfos() {
        List<SessionItem> result = new ArrayList<>();
        for (SessionItem item : items) {
            if (item == null) {
                continue;
            }
            String sessionId = item.getSessionId();
            if (sessionId != null && selectedSessionIds.contains(sessionId)) {
                result.add(item);
            }
        }
        return result;
    }

    public int getSelectedCount() {
        return selectedSessionIds.size();
    }

    public boolean isSelected(@Nullable String sessionId) {
        return sessionId != null && selectedSessionIds.contains(sessionId);
    }

    /** 全选当前列表。 */
    public void selectAll() {
        selectedSessionIds.clear();
        for (SessionItem item : items) {
            if (item != null && !TextUtils.isEmpty(item.getSessionId())) {
                selectedSessionIds.add(item.getSessionId());
            }
        }
        notifyDataSetChanged();
    }

    /** 切换单项勾选。 */
    public void toggleSelect(@Nullable SessionItem sessionItem, int position) {
        if (sessionItem == null || TextUtils.isEmpty(sessionItem.getSessionId())) {
            return;
        }
        String sessionId = sessionItem.getSessionId();
        if (selectedSessionIds.contains(sessionId)) {
            selectedSessionIds.remove(sessionId);
        } else {
            selectedSessionIds.add(sessionId);
        }
        if (position >= 0 && position < items.size()) {
            notifyItemChanged(position, PAYLOAD_SELECT);
        } else {
            notifyDataSetChanged();
        }
    }

    public void cancelSelectAll() {
        closeSelect();
    }

    public void closeSelect() {
        selectedSessionIds.clear();
        if (!items.isEmpty()) {
            notifyItemRangeChanged(0, items.size(), PAYLOAD_SELECT);
        }
    }

    public int findPositionBySessionId(@Nullable String sessionId) {
        if (TextUtils.isEmpty(sessionId)) {
            return -1;
        }
        for (int i = 0; i < items.size(); i++) {
            SessionItem item = items.get(i);
            if (item != null && sessionId.equals(item.getSessionId())) {
                return i;
            }
        }
        return -1;
    }

    public void setNewData(@Nullable List<SessionItem> sessionList) {
        List<SessionItem> next = new ArrayList<>();
        if (sessionList != null) {
            next.addAll(sessionList);
        }
        sortSessions(next);
        submitListAsync(next);
    }

    /**
     * 从列表中移除会话（仅 UI 层内存列表，不触发网络刷新）。
     */
    public boolean removeBySessionId(@Nullable String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) {
            return false;
        }
        for (int i = 0; i < items.size(); i++) {
            SessionItem s = items.get(i);
            if (s != null && sessionId.equals(s.getSessionId())) {
                items.remove(i);
                notifyItemRemoved(i);
                return true;
            }
        }
        return false;
    }

    /**
     * 仅清除指定会话未读角标，避免整表刷新闪烁。
     */
    public boolean clearUnread(@Nullable String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) {
            return false;
        }
        for (int i = 0; i < items.size(); i++) {
            SessionItem s = items.get(i);
            if (s != null && sessionId.equals(s.getSessionId())) {
                if (s.getUnreadCount() <= 0) {
                    return false;
                }
                s.setUnreadCount(0);
                notifyItemChanged(i, PAYLOAD_UNREAD);
                return true;
            }
        }
        return false;
    }

    /**
     * 合并/更新部分会话；排序变化时走 Diff，预览/未读变化走 payload。
     */
    public void mergeSessions(@Nullable List<SessionItem> updates) {
        if (updates == null || updates.isEmpty()) {
            return;
        }
        List<SessionItem> next = new ArrayList<>(items);
        for (SessionItem update : updates) {
            if (update == null || update.getSessionId() == null) {
                continue;
            }
            int index = -1;
            for (int i = 0; i < next.size(); i++) {
                if (update.getSessionId().equals(next.get(i).getSessionId())) {
                    index = i;
                    break;
                }
            }
            if (index >= 0) {
                next.set(index, update);
            } else {
                next.add(0, update);
            }
        }
        sortSessions(next);
        submitListAsync(next);
    }

    private void submitListAsync(@NonNull List<SessionItem> next) {
        final int gen = dataGeneration.incrementAndGet();
        final List<SessionItem> oldSnapshot = new ArrayList<>(items);
        final List<SessionItem> newSnapshot = new ArrayList<>(next);
        DIFF_EXECUTOR.execute(() -> {
            DiffUtil.DiffResult diff = DiffUtil.calculateDiff(
                    new SessionDiffCallback(oldSnapshot, newSnapshot), false);
            MAIN.post(() -> {
                if (gen != dataGeneration.get()) {
                    return;
                }
                items.clear();
                items.addAll(newSnapshot);
                diff.dispatchUpdatesTo(ConversationListAdapter.this);
            });
        });
    }

    private static void sortSessions(@NonNull List<SessionItem> list) {
        list.sort((a, b) -> {
            int topCmp = Long.compare(b.getTopNum(), a.getTopNum());
            if (topCmp != 0) {
                return topCmp;
            }
            return Long.compare(b.getLastMessageTime(), a.getLastMessageTime());
        });
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_conversation, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position, @NonNull List<Object> payloads) {
        if (!payloads.isEmpty()) {
            boolean unreadOnly = false;
            boolean previewOnly = false;
            boolean selectOnly = false;
            for (Object payload : payloads) {
                if (payload == PAYLOAD_UNREAD) {
                    unreadOnly = true;
                } else if (payload == PAYLOAD_PREVIEW) {
                    previewOnly = true;
                } else if (payload == PAYLOAD_SELECT) {
                    selectOnly = true;
                } else {
                    unreadOnly = false;
                    previewOnly = false;
                    selectOnly = false;
                    break;
                }
            }
            if (selectOnly && !previewOnly && !unreadOnly) {
                bindSelect(holder, items.get(position));
                return;
            }
            if (previewOnly || unreadOnly) {
                SessionItem s = items.get(position);
                if (previewOnly) {
                    bindPreview(holder, s);
                }
                bindUnread(holder, s);
                if (selectOnly) {
                    bindSelect(holder, s);
                }
                return;
            }
        }
        onBindViewHolder(holder, position);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        SessionItem s = items.get(position);
        String name = WeChatUiHelper.resolveSessionListDisplayName(s, currentUserId);
        String displayName = name.isEmpty()
                ? holder.itemView.getContext().getString(com.cboda.baselibrary.R.string.conversation_name_fallback)
                : name;
        holder.tvName.setText(displayName);
        MessageAvatarBinder.bindSessionListAvatar(holder.avatar, s, currentUserId);
        //目前不需要展示，注释掉注册天数
//        MessageAvatarBinder.bindSessionRegisterDay(holder.tvRegisterDay, s, currentUserId);

        bindPreview(holder, s);
        bindUnread(holder, s);
        bindSelect(holder, s);

        holder.itemView.setBackgroundResource(
                s.getTopNum() > 0 ? R.drawable.bg_item_pinned : R.drawable.bg_item_white);

        holder.itemView.setOnClickListener(v -> {
            if (onSessionClickListener != null) {
                onSessionClickListener.onSessionClick(s);
            }
        });
        holder.itemView.setOnLongClickListener(v -> {
            if (selectMode) {
                return true;
            }
            if (onSessionLongClickListener != null) {
                return onSessionLongClickListener.onSessionLongClick(s);
            }
            return false;
        });
    }

    private void bindSelect(@NonNull VH holder, @NonNull SessionItem s) {
        if (!selectMode) {
            holder.ivCheck.setVisibility(View.GONE);
            return;
        }
        holder.ivCheck.setVisibility(View.VISIBLE);
        boolean selected = isSelected(s.getSessionId());
        holder.ivCheck.setImageResource(selected
                ? com.cboda.chatui.R.mipmap.ic_check_cnv_broad_select
                : com.cboda.chatui.R.mipmap.ic_check_cnv_broad_normal);
    }

    private void bindPreview(@NonNull VH holder, @NonNull SessionItem s) {
        holder.tvSubtitle.setText(buildSubtitle(holder, s));
        if (WeChatUiHelper.isVoiceSessionPreview(s)) {
            holder.ivPreviewType.setVisibility(View.VISIBLE);
            holder.ivPreviewType.setImageResource(R.drawable.ic_msg_voice);
        } else if (WeChatUiHelper.isImageSessionPreview(s)) {
            holder.ivPreviewType.setVisibility(View.VISIBLE);
            holder.ivPreviewType.setImageResource(R.drawable.ic_msg_image);
        } else {
            holder.ivPreviewType.setVisibility(View.GONE);
        }
    }

    private static void bindUnread(@NonNull VH holder, @NonNull SessionItem s) {
        int unread = s.getUnreadCount();
        boolean muted = s.isMuteNotify();
        if (unread > 0) {
            if (muted) {
                holder.tvUnread.setVisibility(View.GONE);
                holder.muteDot.setVisibility(View.VISIBLE);
            } else {
                holder.muteDot.setVisibility(View.GONE);
                holder.tvUnread.setVisibility(View.VISIBLE);
                holder.tvUnread.setText(unread > 99 ? "99+" : String.valueOf(unread));
            }
        } else {
            holder.tvUnread.setVisibility(View.GONE);
            holder.muteDot.setVisibility(View.GONE);
        }
    }

    @NonNull
    private CharSequence buildSubtitle(@NonNull VH holder, @NonNull SessionItem session) {
        String preview = WeChatUiHelper.formatSessionPreview(session, currentUserId);
        if (AtMentionChecker.isMentioningMe(session, currentUserId)) {
            String prefix = holder.itemView.getContext().getString(com.cboda.chatui.R.string.someone_mentioned_me);
            String text = prefix + preview;
            SpannableString spannable = new SpannableString(text);
            int blue = ContextCompat.getColor(holder.itemView.getContext(), R.color.color_3B47DD);
            spannable.setSpan(
                    new ForegroundColorSpan(blue),
                    0,
                    prefix.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            return spannable;
        }
        return preview;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private static boolean sameIdentityFields(@NonNull SessionItem a, @NonNull SessionItem b) {
        return a.getTopNum() == b.getTopNum()
                && a.getCreateTime() == b.getCreateTime()
                && TextUtils.equals(a.getSessionName(), b.getSessionName())
                && TextUtils.equals(a.getIcon(), b.getIcon())
                && TextUtils.equals(a.getUsername(), b.getUsername())
                && Objects.equals(a.getOppositeUserId(), b.getOppositeUserId());
    }

    private static boolean samePreviewFields(@NonNull SessionItem a, @NonNull SessionItem b) {
        return a.getUnreadCount() == b.getUnreadCount()
                && a.isMuteNotify() == b.isMuteNotify()
                && a.getLastMessageTime() == b.getLastMessageTime()
                && a.getLastMessageStatus() == b.getLastMessageStatus()
                && a.getLastMsgType() == b.getLastMsgType()
                && TextUtils.equals(a.getLastMessageId(), b.getLastMessageId())
                && TextUtils.equals(a.getLastMessageContent(), b.getLastMessageContent())
                && TextUtils.equals(a.getLastContentType(), b.getLastContentType())
                && TextUtils.equals(a.getLastCustomInfo(), b.getLastCustomInfo());
    }

    private static final class SessionDiffCallback extends DiffUtil.Callback {
        @NonNull
        private final List<SessionItem> oldList;
        @NonNull
        private final List<SessionItem> newList;

        SessionDiffCallback(@NonNull List<SessionItem> oldList, @NonNull List<SessionItem> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() {
            return oldList.size();
        }

        @Override
        public int getNewListSize() {
            return newList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            SessionItem oldItem = oldList.get(oldItemPosition);
            SessionItem newItem = newList.get(newItemPosition);
            return TextUtils.equals(oldItem.getSessionId(), newItem.getSessionId());
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            SessionItem a = oldList.get(oldItemPosition);
            SessionItem b = newList.get(newItemPosition);
            return sameIdentityFields(a, b) && samePreviewFields(a, b);
        }

        @Nullable
        @Override
        public Object getChangePayload(int oldItemPosition, int newItemPosition) {
            SessionItem a = oldList.get(oldItemPosition);
            SessionItem b = newList.get(newItemPosition);
            if (!sameIdentityFields(a, b)) {
                return null;
            }
            if (samePreviewFields(a, b)) {
                return null;
            }
            boolean onlyUnread = a.getUnreadCount() != b.getUnreadCount()
                    && a.isMuteNotify() == b.isMuteNotify()
                    && a.getLastMessageTime() == b.getLastMessageTime()
                    && a.getLastMessageStatus() == b.getLastMessageStatus()
                    && a.getLastMsgType() == b.getLastMsgType()
                    && TextUtils.equals(a.getLastMessageId(), b.getLastMessageId())
                    && TextUtils.equals(a.getLastMessageContent(), b.getLastMessageContent())
                    && TextUtils.equals(a.getLastContentType(), b.getLastContentType())
                    && TextUtils.equals(a.getLastCustomInfo(), b.getLastCustomInfo());
            return onlyUnread ? PAYLOAD_UNREAD : PAYLOAD_PREVIEW;
        }
    }

    static final class VH extends RecyclerView.ViewHolder {
        final ImageView ivCheck;
        final VpGradeView avatar;
        final TextView tvName;
        final TextView tvRegisterDay;
        final ImageView ivPreviewType;
        final TextView tvSubtitle;
        final TextView tvUnread;
        final View muteDot;

        VH(@NonNull View itemView) {
            super(itemView);
            ivCheck = itemView.findViewById(R.id.iv_check);
            avatar = itemView.findViewById(R.id.avatar);
            tvName = itemView.findViewById(R.id.tv_name);
            tvRegisterDay = itemView.findViewById(R.id.tv_register_day);
            ivPreviewType = itemView.findViewById(R.id.iv_preview_type);
            tvSubtitle = itemView.findViewById(R.id.tv_subtitle);
            tvUnread = itemView.findViewById(R.id.tv_unread);
            muteDot = itemView.findViewById(R.id.v_mute_dot);
        }
    }
}
