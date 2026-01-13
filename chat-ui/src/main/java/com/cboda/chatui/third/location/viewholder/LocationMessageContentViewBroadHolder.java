/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.third.location.viewholder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import com.cboda.chatui.R;
import com.cboda.chatui.annotation.EnableContextBroadMenu;
import com.cboda.chatui.annotation.MessageContentBroadType;
import com.cboda.chatui.bridge.MyCommonBroadBridge;
import com.cboda.chatui.conversation.ConversationBroadFragment;
import com.cboda.chatui.conversation.message.model.UiBroadMessage;
import com.cboda.chatui.conversation.message.viewholder.NormalMessageContentViewBroadHolder;
import com.cboda.chatui.third.utils.UIBroadUtils;
import cn.wildfirechat.message.LocationMessageContent;
import com.cboda.baselibrary.common.BridgeImEnum;

@MessageContentBroadType(LocationMessageContent.class)
@EnableContextBroadMenu
public class LocationMessageContentViewBroadHolder extends NormalMessageContentViewBroadHolder {

    TextView locationTitleTextView;
    ImageView locationImageView;

    public LocationMessageContentViewBroadHolder(ConversationBroadFragment fragment, RecyclerView.Adapter adapter, View itemView) {
        super(fragment, adapter, itemView);
        bindViews(itemView);
        bindEvents(itemView);
    }

    private void bindEvents(View itemView) {
       itemView.findViewById(R.id.locationLinearLayout).setOnClickListener(this::onClick);
    }

    private void bindViews(View itemView) {
        locationTitleTextView =itemView.findViewById(R.id.locationTitleTextView);
        locationImageView =itemView.findViewById(R.id.locationImageView);
    }

    @Override
    public void onBind(UiBroadMessage message) {
        LocationMessageContent locationMessage = (LocationMessageContent) message.message.content;
        locationTitleTextView.setText(locationMessage.getTitle());

        if (locationMessage.getThumbnail() != null && locationMessage.getThumbnail().getWidth() > 0) {
            int width = locationMessage.getThumbnail().getWidth();
            int height = locationMessage.getThumbnail().getHeight();
            locationImageView.getLayoutParams().width = UIBroadUtils.dip2Px(width > 200 ? 200 : width);
            locationImageView.getLayoutParams().height = UIBroadUtils.dip2Px(height > 200 ? 200 : height);
            locationImageView.setImageBitmap(locationMessage.getThumbnail());
        } else {
            Glide.with(fragment).load(R.mipmap.default_location)
                .apply(new RequestOptions().override(UIBroadUtils.dip2Px(200), UIBroadUtils.dip2Px(200)).centerCrop()).into(locationImageView);
        }
    }

    public void onClick(View view) {
        LocationMessageContent content = (LocationMessageContent) message.message.content;
        MyCommonBroadBridge.getInstance().setMyBridgeMethod(fragment.getContext(), BridgeImEnum.DAOHANG.getValue(), content.getTitle(), content.getLocation().getLatitude() + "|" + content.getLocation().getLongitude(), null);

    }
}
