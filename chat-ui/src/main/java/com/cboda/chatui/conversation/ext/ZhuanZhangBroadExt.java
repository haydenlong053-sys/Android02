package com.cboda.chatui.conversation.ext;

import android.content.Context;
import android.view.View;

import com.cboda.chatui.R;
import com.cboda.chatui.annotation.ExtContextMenuBroadItem;
import com.cboda.chatui.bridge.MyCommonBroadBridge;
import com.cboda.chatui.conversation.ext.core.ConversationBroadExt;
import cn.wildfirechat.model.Conversation;
import com.cboda.baselibrary.common.BridgeImEnum;
import com.cboda.chatui.conversation.ext.core.ConversationMidExt;

public class ZhuanZhangBroadExt extends ConversationMidExt {
    @ExtContextMenuBroadItem
    public void pickZhuanZhang(View containerView, Conversation conversation) {
        MyCommonBroadBridge.getInstance().setMyBridgeMethod(containerView.getContext(), BridgeImEnum.TRANSFER.getValue(),conversation.target,"",null);
    }
    @Override
    public int priority() {
        return 100;
    }

    @Override
    public int iconResId() {
        return R.mipmap.ic_func_broad_zhuanzhang;
    }

    @Override
    public String title(Context context) {
        return context.getString(R.string.msg_transfer);
    }

    @Override
    public String contextMenuTitle(Context context, String tag) {
        return title(context);
    }
}
