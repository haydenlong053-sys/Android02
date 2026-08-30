package com.freddy.chat.viewholder;

import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.cboda.chatui.R;
import com.cboda.intelligent.utils.JumpBroadUtils;
import com.freddy.chat.utils.CardMessageContentUtil;
import com.freddy.im.bean.AppMessage;

/**
 * 个人名片消息 UI（contentType=700）。
 */
public final class CardMessageVH extends BaseMessageVH {

    private int boundContentLayoutRes;
    private View cardContentLayout;
    private ImageView portraitImageView;
    private TextView nameTextView;
    private TextView userIdTextView;
    @NonNull
    private String cardTargetUserId = "";

    public CardMessageVH(@NonNull View itemView) {
        super(itemView, R.layout.conversation_item_user_card_broad_receive, false);
        cacheCardViews();
    }

    public void bind(@NonNull AppMessage message, int position, @NonNull MessageViewHolderContext context) {
        boolean sentByMe = message.isSender(context.currentUserId);
        int layoutRes = sentByMe
                ? R.layout.conversation_item_user_card_broad_send
                : R.layout.conversation_item_user_card_broad_receive;
        if (boundContentLayoutRes != layoutRes) {
            inflateContentLayout(layoutRes);
            boundContentLayoutRes = layoutRes;
            cacheCardViews();
            bindCardClick();
        }
        bindCommon(message, position, context);

        String content = message.getContent();
        cardTargetUserId = CardMessageContentUtil.resolveUserId(content);
        nameTextView.setText(CardMessageContentUtil.resolveDisplayName(content));
        String idno = CardMessageContentUtil.resolveIdno(content);
        if (TextUtils.isEmpty(idno)) {
            userIdTextView.setText("");
        } else {
            userIdTextView.setText("ID:" + idno);
        }
        Glide.with(portraitImageView.getContext())
                .load(CardMessageContentUtil.resolvePortrait(content))
                .placeholder(R.mipmap.avatar_broad_def)
                .into(portraitImageView);
    }

    private void cacheCardViews() {
        cardContentLayout = contentView.findViewById(R.id.contentLayout);
        portraitImageView = contentView.findViewById(R.id.userCardPortraitImageView);
        nameTextView = contentView.findViewById(R.id.userCardNameTextView);
        userIdTextView = contentView.findViewById(R.id.userIdTextView);
        bindCardClick();
    }

    private void bindCardClick() {
        if (cardContentLayout == null) {
            return;
        }
        cardContentLayout.setOnClickListener(v -> onUserCardClick());
    }

    private void onUserCardClick() {
        if (TextUtils.isEmpty(cardTargetUserId)) {
            return;
        }
        JumpBroadUtils.Companion.startHaoYouAddActivity(itemView.getContext(), cardTargetUserId, "2");
    }
}
