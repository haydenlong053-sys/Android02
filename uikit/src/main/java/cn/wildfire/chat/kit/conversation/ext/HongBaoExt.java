package cn.wildfire.chat.kit.conversation.ext;

import android.content.Context;
import android.view.View;

import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.annotation.ExtContextMenuItem;
import cn.wildfire.chat.kit.bridge.YeHuoCommonBridge;
import cn.wildfire.chat.kit.conversation.ext.core.ConversationExt;
import cn.wildfirechat.model.Conversation;

public class HongBaoExt extends ConversationExt {

    @ExtContextMenuItem
    public void pickHongBao(View containerView, Conversation conversation) {
        if (conversation != null) {
            if (conversation.type == Conversation.ConversationType.Group) {
                YeHuoCommonBridge.getInstance().setYeHuoQiaoJie(containerView.getContext(), "qunhongbao", conversation.target, "",null);
            } else {
                YeHuoCommonBridge.getInstance().setYeHuoQiaoJie(containerView.getContext(), "hongbao", conversation.target, "",null);
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
