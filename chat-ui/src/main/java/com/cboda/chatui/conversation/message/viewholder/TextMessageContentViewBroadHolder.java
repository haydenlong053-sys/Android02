/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.conversation.message.viewholder;

import static com.cboda.baselibrary.utils.ELogUtilKt.eLogD;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.text.Html;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.cboda.baselibrary.utils.SharePrefBroadBroadUtil;
import com.cboda.chatemoji.MoonBroadUtils;

import com.cboda.chatui.*;
import com.cboda.chatui.WfcWebViewBroadActivity;
import com.cboda.chatui.annotation.EnableContextBroadMenu;
import com.cboda.chatui.annotation.MessageContentBroadType;
import com.cboda.chatui.annotation.MessageContextMenuBroadItem;
import com.cboda.chatui.bridge.MyCommonBroadBridge;
import com.cboda.chatui.conversation.ConversationBroadFragment;
import com.cboda.chatui.conversation.message.model.UiBroadMessage;
import com.cboda.chatui.mm.MMPreviewBroadActivity;
import com.cboda.chatui.widget.LinkClickBroadListener;
import com.cboda.chatui.widget.LinkTextViewMovementBroadMethod;
import cn.wildfirechat.message.ImageMessageContent;
import cn.wildfirechat.message.Message;
import cn.wildfirechat.message.MessageContent;
import cn.wildfirechat.message.PTextMessageContent;
import cn.wildfirechat.message.TextMessageContent;
import cn.wildfirechat.message.VideoMessageContent;
import cn.wildfirechat.message.core.MessageDirection;
import cn.wildfirechat.model.QuoteInfo;
import cn.wildfirechat.remote.ChatManager;
import com.cboda.baselibrary.common.BridgeImEnum;


@MessageContentBroadType(value = {
        TextMessageContent.class,
        PTextMessageContent.class

})
@EnableContextBroadMenu
public class TextMessageContentViewBroadHolder extends NormalMessageContentViewBroadHolder {
    TextView contentTextView, fContentTextView;
    TextView refTextView;

    private QuoteInfo quoteInfo;

    public TextMessageContentViewBroadHolder(ConversationBroadFragment fragment, RecyclerView.Adapter adapter, View itemView) {
        super(fragment, adapter, itemView);
        bindViews(itemView);
        bindEvents(itemView);
    }

    private void bindEvents(View itemView) {
        itemView.findViewById(R.id.contentTextView).setOnClickListener(this::onClick);
        itemView.findViewById(R.id.refTextView).setOnClickListener(this::onRefClick);
    }

    private void bindViews(View itemView) {
        contentTextView = itemView.findViewById(R.id.contentTextView);
        refTextView = itemView.findViewById(R.id.refTextView);
        fContentTextView = itemView.findViewById(R.id.fContentTextView);
    }

    @Override
    public void onBind(UiBroadMessage message) {
        TextMessageContent textMessageContent = (TextMessageContent) message.message.content;
        String content = textMessageContent.getContent();
        eLogD("TextMessageContent", content);
        if (content.startsWith("<") && content.endsWith(">")) {
            Spanned spanned = null;
            try {
                spanned = Html.fromHtml(content);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (spanned != null && spanned.length() > 0) {
                contentTextView.setText(spanned);

            } else {
                MoonBroadUtils.identifyFaceExpression(fragment.getContext(), contentTextView, ((TextMessageContent) message.message.content).getContent(), ImageSpan.ALIGN_BOTTOM);
            }
        } else {
            MoonBroadUtils.identifyFaceExpression(fragment.getContext(), contentTextView, ((TextMessageContent) message.message.content).getContent(), ImageSpan.ALIGN_BOTTOM);
        }

        String fy = SharePrefBroadBroadUtil.getString(fragment.getContext(), message.message.messageUid + "", "");

        if (message.message.direction == MessageDirection.Receive) {
            if (SharePrefBroadBroadUtil.getString(fragment.getContext(), "autofanyi", "0").equals("1") &&
                    fy.length() == 0) {
                long time = SharePrefBroadBroadUtil.getLong(fragment.getContext(), "autofanyitime", 0);
                if (time == 0 || message.message.serverTime > time)
                    MyCommonBroadBridge.getInstance().setMyBridgeMethod(fragment.getContext(), BridgeImEnum.FANYI.getValue(), ((TextMessageContent) message.message.content).getContent(), "", message);
            }
        }


        if (null != fy && fy.length() > 0) {
            fContentTextView.setText(fy);
            fContentTextView.setVisibility(View.VISIBLE);
//            MoonUtils.identifyFaceExpression(fragment.getContext(), fContentTextView, ((TextMessageContent) message.message.content).getContent(), ImageSpan.ALIGN_BOTTOM);
        } else
            fContentTextView.setVisibility(View.GONE);

        contentTextView.setMovementMethod(new LinkTextViewMovementBroadMethod(new LinkClickBroadListener() {
            @Override
            public boolean onLinkClick(String link) {
                WfcWebViewBroadActivity.loadUrl(fragment.getContext(), "", link);
                return true;
            }
        }));

        quoteInfo = textMessageContent.getQuoteInfo();
        if (quoteInfo != null && quoteInfo.getMessageUid() > 0) {
            refTextView.setVisibility(View.VISIBLE);
            refTextView.setText(quoteInfo.getUserDisplayName() + ": " + quoteInfo.getMessageDigest());
        } else {
            refTextView.setVisibility(View.GONE);
        }
    }

    public void onClick(View view) {
        String content = ((TextMessageContent) message.message.content).getContent();
//        WfcWebViewBroadActivity.loadHtmlContent(fragment.getActivity(), "消息内容", content);
    }

    public void onRefClick(View view) {
        Message message = ChatManager.Instance().getMessageByUid(quoteInfo.getMessageUid());
        if (message != null) {
            // TODO previewMessageActivity
            MessageContent messageContent = message.content;
            if (messageContent instanceof TextMessageContent) {
//                WfcWebViewBroadActivity.loadHtmlContent(fragment.getActivity(), "消息内容", ((TextMessageContent) messageContent).getContent());
            } else {
                if (messageContent instanceof VideoMessageContent) {
                    MMPreviewBroadActivity.previewVideo(fragment.getActivity(), message);
                } else if (messageContent instanceof ImageMessageContent) {
                    MMPreviewBroadActivity.previewImage(fragment.getActivity(), message);
                }
            }
        }
    }

    @MessageContextMenuBroadItem(tag = MessageContextMenuItemBroadTags.TAG_CLIP, confirm = false, priority = 12)
    public void clip(View itemView, UiBroadMessage message) {
        ClipboardManager clipboardManager = (ClipboardManager) fragment.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboardManager == null) {
            return;
        }
        TextMessageContent content = (TextMessageContent) message.message.content;
        ClipData clipData = ClipData.newPlainText("messageContent", content.getContent());
        clipboardManager.setPrimaryClip(clipData);
    }


    @Override
    public String contextMenuTitle(Context context, String tag) {
        if (MessageContextMenuItemBroadTags.TAG_CLIP.equals(tag)) {
            return "复制";
        }
        return super.contextMenuTitle(context, tag);
    }
}
