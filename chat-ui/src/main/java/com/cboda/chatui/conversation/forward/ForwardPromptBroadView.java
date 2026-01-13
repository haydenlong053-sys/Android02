/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.conversation.forward;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import com.cboda.chatui.R;
import com.cboda.chatui.third.utils.UIBroadUtils;
import com.cboda.chatui.utils.WfcTextBroadUtils;

public class ForwardPromptBroadView extends LinearLayout {
    ImageView portraitImageView;
    TextView nameTextView;
    TextView contentTextView;
    ImageView contentImageView;
    EditText editText;

    public ForwardPromptBroadView(Context context) {
        super(context);
        init();
    }

    public ForwardPromptBroadView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ForwardPromptBroadView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public ForwardPromptBroadView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        View view = LayoutInflater.from(getContext())
            .inflate(R.layout.forward_prompt_broad_dialog, this, true);
        bindViews(view);
    }

    private void bindViews(View view) {
        portraitImageView = view.findViewById(R.id.portraitImageView);
        nameTextView = view.findViewById(R.id.nameTextView);
        contentTextView = view.findViewById(R.id.contentTextView);
        contentImageView = view.findViewById(R.id.contentImageView);
        editText = view.findViewById(R.id.editText);
    }

    public void bind(String targetName, String targetPortrait, String contentText) {
        bind(targetName, targetPortrait, contentText, null);
    }

    public void bind(String targetName, String targetPortrait, Bitmap contentImage) {
        bind(targetName, targetPortrait, null, contentImage);
    }

    private void bind(String targetName, String targetPortrait, String contentText, Bitmap contentImage) {
        nameTextView.setText(targetName);
        Glide.with(getContext()).load(targetPortrait).apply(new RequestOptions().placeholder(R.mipmap.ic_group_broad_chat).centerCrop()).into(portraitImageView);
        if (!TextUtils.isEmpty(contentText)) {
            contentImageView.setVisibility(GONE);
            contentTextView.setVisibility(VISIBLE);
            contentTextView.setText(WfcTextBroadUtils.htmlToText(contentText));
        } else if (contentImage != null) {
            contentTextView.setVisibility(GONE);
            contentImageView.setVisibility(VISIBLE);
            contentImageView.getLayoutParams().width = UIBroadUtils.dip2Px(contentImage.getWidth());
            contentImageView.getLayoutParams().height = UIBroadUtils.dip2Px(contentImage.getHeight());
            contentImageView.setImageBitmap(contentImage);
        }
        invalidate();
    }

    public String getEditText() {
        return editText.getText().toString().trim();
    }
}
