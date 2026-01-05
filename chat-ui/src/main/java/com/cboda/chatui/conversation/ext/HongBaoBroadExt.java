package com.cboda.chatui.conversation.ext;

import android.content.Context;
import android.view.View;

import com.cboda.chatui.R;
import com.cboda.chatui.annotation.ExtContextMenuBroadItem;
import com.cboda.chatui.bridge.YeHuoCommonBroadBridge;
import com.cboda.chatui.conversation.ext.core.ConversationBroadExt;
import cn.wildfirechat.model.Conversation;

public class HongBaoBroadExt extends ConversationBroadExt {

    @ExtContextMenuBroadItem
    public void pickHongBao(View containerView, Conversation conversation) {
        if (conversation != null) {
            if (conversation.type == Conversation.ConversationType.Group) {
                YeHuoCommonBroadBridge.getInstance().setYeHuoQiaoJie(containerView.getContext(), "qunhongbao", conversation.target, "",null);
            } else {
                YeHuoCommonBroadBridge.getInstance().setYeHuoQiaoJie(containerView.getContext(), "hongbao", conversation.target, "",null);
            }
        }
    }

    @Override
    public int priority() {
        return 100;
    }

    @Override
    public int iconResId() {
        return R.mipmap.ic_func_hongbao;
    }

    @Override
    public String title(Context context) {
        return "红包";
    }

    @Override
    public String contextMenuTitle(Context context, String tag) {
        return title(context);
    }
}
