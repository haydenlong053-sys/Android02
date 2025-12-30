package cn.wildfire.chat.kit.conversation.ext;

import android.content.Context;
import android.view.View;

import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.annotation.ExtContextMenuItem;
import cn.wildfire.chat.kit.bridge.YeHuoCommonBridge;
import cn.wildfire.chat.kit.conversation.ext.core.ConversationExt;
import cn.wildfirechat.model.Conversation;

public class ZhuanZhangExt extends ConversationExt {
    @ExtContextMenuItem
    public void pickZhuanZhang(View containerView, Conversation conversation) {
        YeHuoCommonBridge.getInstance().setYeHuoQiaoJie(containerView.getContext(),"zhuanzhang",conversation.target,"",null);
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
