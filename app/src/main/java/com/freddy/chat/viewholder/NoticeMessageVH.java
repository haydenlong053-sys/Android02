package com.freddy.chat.viewholder;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cboda.baselibrary.utils.ELogUtilKt;
import com.cboda.chatui.R;
import com.cboda.intelligent.activity.NoticeDetailBroadActivity;
import com.cboda.intelligent.model.NoticeInfoBroadBean;
import com.freddy.chat.utils.NoticeMessageContentUtil;
import com.freddy.im.bean.AppMessage;

/**
 * 系统公告卡片消息 UI（对齐野火 {@link com.cboda.chatui.conversation.message.viewholder.NoticeContentViewBroadHolder}）。
 */
public final class NoticeMessageVH extends BaseMessageVH {

    private int boundContentLayoutRes;
    private View noticeContentLayout;
    private TextView titleTextView;
    private TextView descTextView;
    @NonNull
    private NoticeInfoBroadBean noticeInfo = new NoticeInfoBroadBean();

    public NoticeMessageVH(@NonNull View itemView) {
        super(itemView, R.layout.conversation_item_msg_notice_broad_receive, false);
        cacheNoticeViews();
    }

    public void bind(@NonNull AppMessage message, int position, @NonNull MessageViewHolderContext context) {
        ELogUtilKt.eLogD("zhanglong", "系统消息卡getContent--->" + message.getContent());
        boolean sentByMe = message.isSender(context.currentUserId);
        int layoutRes = sentByMe
                ? R.layout.conversation_item_msg_notice_broad_send
                : R.layout.conversation_item_msg_notice_broad_receive;
        if (boundContentLayoutRes != layoutRes) {
            inflateContentLayout(layoutRes);
            boundContentLayoutRes = layoutRes;
            cacheNoticeViews();
        }
        bindCommon(message, position, context);

        String content = message.getContent();
        noticeInfo = buildNoticeInfo(content);
        titleTextView.setText(NoticeMessageContentUtil.resolveNoticeTitle(content));
        descTextView.setText(NoticeMessageContentUtil.resolveCreateTime(content));
    }

    private void cacheNoticeViews() {
        noticeContentLayout = contentView.findViewById(R.id.contentLayout);
        titleTextView = contentView.findViewById(R.id.titleTextView);
        descTextView = contentView.findViewById(R.id.descTextView);
        if (noticeContentLayout != null) {
            noticeContentLayout.setOnClickListener(v -> onNoticeClick());
        }
    }

    private void onNoticeClick() {
        if (TextUtils.isEmpty(noticeInfo.getId()) && TextUtils.isEmpty(noticeInfo.getContentUrl())) {
            return;
        }
        Intent intent = new Intent(itemView.getContext(), NoticeDetailBroadActivity.class);
        intent.putExtra("data", noticeInfo);
        itemView.getContext().startActivity(intent);
    }

    @NonNull
    private static NoticeInfoBroadBean buildNoticeInfo(@Nullable String content) {
        NoticeInfoBroadBean bean = new NoticeInfoBroadBean();
        bean.setId(NoticeMessageContentUtil.resolveId(content));
        bean.setContentUrl(NoticeMessageContentUtil.resolveContentUrl(content));
        bean.setCreateTime(NoticeMessageContentUtil.resolveCreateTime(content));
        bean.setNoticeTitle(NoticeMessageContentUtil.resolveNoticeTitle(content));
        bean.setPreviewContent(NoticeMessageContentUtil.resolvePreviewContent(content));
        bean.setStatus(NoticeMessageContentUtil.resolveStatus(content) > 0);
        return bean;
    }
}
