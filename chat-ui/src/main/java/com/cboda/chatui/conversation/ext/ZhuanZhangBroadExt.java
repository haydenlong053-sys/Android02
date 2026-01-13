package com.cboda.chatui.conversation.ext;

import android.content.Context;
import android.view.View;

import com.cboda.chatui.R;
import com.cboda.chatui.annotation.ExtContextMenuBroadItem;
import com.cboda.chatui.bridge.MyCommonBroadBridge;
import com.cboda.chatui.conversation.ext.core.ConversationBroadExt;
import cn.wildfirechat.model.Conversation;
import com.cboda.baselibrary.common.BridgeImEnum;

public class ZhuanZhangBroadExt extends ConversationBroadExt {
    @ExtContextMenuBroadItem
    public void pickZhuanZhang(View containerView, Conversation conversation) {
        MyCommonBroadBridge.getInstance().setMyBridgeMethod(containerView.getContext(), BridgeImEnum.ZHUANZHANG.getValue(),conversation.target,"",null);
    }
    @Override
    public int priority() {
        return 100;
    }

    @Override
    public int iconResId() {
        return R.mipmap.ic_func_zhuanzhang;
    }

    @Override
    public String title(Context context) {
        return "转账";
    }

    @Override
    public String contextMenuTitle(Context context, String tag) {
        return title(context);
    }
}
