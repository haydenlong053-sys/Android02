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
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.cboda.baselibrary.utils.SharePrefBroadUtil;
import com.google.gson.Gson;
import com.lqr.emoji.MoonUtils;

import com.cboda.chatui.*;
import com.cboda.chatui.WfcWebViewActivity;
import com.cboda.chatui.annotation.EnableContextMenu;
import com.cboda.chatui.annotation.MessageContentType;
import com.cboda.chatui.annotation.MessageContextMenuItem;
import com.cboda.chatui.bridge.YeHuoCommonBridge;
import com.cboda.chatui.conversation.ConversationFragment;
import com.cboda.chatui.conversation.message.model.UiMessage;
import com.cboda.chatui.mm.MMPreviewActivity;
import com.cboda.chatui.widget.LinkClickListener;
import com.cboda.chatui.widget.LinkTextViewMovementMethod;
import cn.wildfirechat.message.ImageMessageContent;
import cn.wildfirechat.message.Message;
import cn.wildfirechat.message.MessageContent;
import cn.wildfirechat.message.PTextMessageContent;
import cn.wildfirechat.message.TextMessageContent;
import cn.wildfirechat.message.VideoMessageContent;
import cn.wildfirechat.message.core.MessageDirection;
import cn.wildfirechat.model.QuoteInfo;
import cn.wildfirechat.remote.ChatManager;


@MessageContentType(value = {
        TextMessageContent.class,
        PTextMessageContent.class

})
@EnableContextMenu
public class TextMessageContentViewHolder extends NormalMessageContentViewHolder {
    TextView contentTextView, fContentTextView;
    TextView refTextView;

    private QuoteInfo quoteInfo;

    public TextMessageContentViewHolder(ConversationFragment fragment, RecyclerView.Adapter adapter, View itemView) {
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
    public void onBind(UiMessage message) {
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
                MoonUtils.identifyFaceExpression(fragment.getContext(), contentTextView, ((TextMessageContent) message.message.content).getContent(), ImageSpan.ALIGN_BOTTOM);
            }
        } else {
            MoonUtils.identifyFaceExpression(fragment.getContext(), contentTextView, ((TextMessageContent) message.message.content).getContent(), ImageSpan.ALIGN_BOTTOM);
        }

        String fy = SharePrefBroadUtil.getString(fragment.getContext(), message.message.messageUid + "", "");

        if (message.message.direction == MessageDirection.Receive) {
            if (SharePrefBroadUtil.getString(fragment.getContext(), "autofanyi", "0").equals("1") &&
                    fy.length() == 0) {
                long time = SharePrefBroadUtil.getLong(fragment.getContext(), "autofanyitime", 0);
                if (time == 0 || message.message.serverTime > time)
                    YeHuoCommonBridge.getInstance().setYeHuoQiaoJie(fragment.getContext(), "fanyi", ((TextMessageContent) message.message.content).getContent(), "", message);
            }
        }


        if (null != fy && fy.length() > 0) {
            fContentTextView.setText(fy);
            fContentTextView.setVisibility(View.VISIBLE);
//            MoonUtils.identifyFaceExpression(fragment.getContext(), fContentTextView, ((TextMessageContent) message.message.content).getContent(), ImageSpan.ALIGN_BOTTOM);
        } else
            fContentTextView.setVisibility(View.GONE);

        contentTextView.setMovementMethod(new LinkTextViewMovementMethod(new LinkClickListener() {
            @Override
            public boolean onLinkClick(String link) {
                WfcWebViewActivity.loadUrl(fragment.getContext(), "", link);
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
//        WfcWebViewActivity.loadHtmlContent(fragment.getActivity(), "消息内容", content);
    }

    public void onRefClick(View view) {
        Message message = ChatManager.Instance().getMessageByUid(quoteInfo.getMessageUid());
        if (message != null) {
            // TODO previewMessageActivity
            MessageContent messageContent = message.content;
            if (messageContent instanceof TextMessageContent) {
//                WfcWebViewActivity.loadHtmlContent(fragment.getActivity(), "消息内容", ((TextMessageContent) messageContent).getContent());
            } else {
                if (messageContent instanceof VideoMessageContent) {
                    MMPreviewActivity.previewVideo(fragment.getActivity(), message);
                } else if (messageContent instanceof ImageMessageContent) {
                    MMPreviewActivity.previewImage(fragment.getActivity(), message);
                }
            }
        }
    }

    @MessageContextMenuItem(tag = MessageContextMenuItemTags.TAG_CLIP, confirm = false, priority = 12)
    public void clip(View itemView, UiMessage message) {
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
        if (MessageContextMenuItemTags.TAG_CLIP.equals(tag)) {
            return "复制";
        }
        return super.contextMenuTitle(context, tag);
    }
}
