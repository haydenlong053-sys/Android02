package com.cboda.chatui.conversation.ext;

import android.content.Context;
import android.view.View;

import com.cboda.baselibrary.common.BridgeImEnum;
import com.cboda.chatui.R;
import com.cboda.chatui.annotation.ExtContextMenuBroadItem;
import com.cboda.chatui.bridge.MyCommonBroadBridge;
import com.cboda.chatui.conversation.ext.core.ConversationMidExt;

import cn.wildfirechat.model.Conversation;

public class AiAntiFraudBroadExt extends ConversationMidExt {

    @ExtContextMenuBroadItem
    public void openAntiFraud(View containerView, Conversation conversation) {
        MyCommonBroadBridge.getInstance().setMyBridgeMethod(
                fragment.getActivity(),
                BridgeImEnum.AI_ANTI_FRAUD.getValue(),
                "",
                "",
                null
        );
    }

    @Override
    public int priority() {
        return 100;
    }

    @Override
    public int iconResId() {
        return R.mipmap.ic_func_broad_anti_fraud;
    }

    @Override
    public String title(Context context) {
        return context.getString(R.string.msg_anti_fraud);
    }

    @Override
    public String contextMenuTitle(Context context, String tag) {
        return title(context);
    }
}
