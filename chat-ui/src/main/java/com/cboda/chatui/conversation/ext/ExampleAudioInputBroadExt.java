/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.conversation.ext;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.cboda.chatui.R;
import com.cboda.chatui.annotation.ExtContextMenuBroadItem;
import com.cboda.chatui.conversation.ext.core.ConversationBroadExt;
import cn.wildfirechat.model.Conversation;

//用来演示类似微信语音输入那种扩展
public class ExampleAudioInputBroadExt extends ConversationBroadExt {

    /**
     * @param containerView 扩展view的container
     * @param conversation
     */
    @ExtContextMenuBroadItem
    public void image(View containerView, Conversation conversation) {
        FrameLayout frameLayout = (FrameLayout) containerView;
        View view = LayoutInflater.from(activity).inflate(R.layout.conversatioin_ext_example_layout, frameLayout, false);
        frameLayout.addView(view);
        extension.disableHideOnScroll();

        view.findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                extension.reset();
            }
        });
    }

    @Override
    public int priority() {
        return 100;
    }

    @Override
    public int iconResId() {
        return R.mipmap.ic_func_voice;
    }

    @Override
    public String title(Context context) {
        return "Example";
    }

    @Override
    public String contextMenuTitle(Context context, String tag) {
        return title(context);
    }
}
