/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.conversation.ext;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.cboda.chatui.R;
import com.cboda.chatui.annotation.ExtContextMenuBroadItem;
import com.cboda.chatui.bridge.MyCommonBroadBridge;
import com.cboda.chatui.conversation.ext.core.ConversationBroadExt;
import cn.wildfirechat.model.Conversation;
import com.cboda.baselibrary.common.BridgeImEnum;
import com.cboda.chatui.conversation.ext.core.ConversationMidExt;

public class CollectBroadExt extends ConversationMidExt {

    /**
     * @param containerView 扩展view的container
     * @param conversation
     */
    @ExtContextMenuBroadItem
    public void pickContact(View containerView, Conversation conversation) {
//        Intent intent = new Intent(fragment.getActivity(), ContactListActivity.class);
//        if (conversation.type == Conversation.ConversationType.Single) {
//            ArrayList<String> filterUserList = new ArrayList<>();
//            filterUserList.add(conversation.target);
//            intent.putExtra(ContactListActivity.FILTER_USER_LIST, filterUserList);
//        }
//        startActivityForResult(intent, 100);

        if (conversation.type == Conversation.ConversationType.Group) {
            MyCommonBroadBridge.getInstance().setMyBridgeMethod(fragment.getActivity(), BridgeImEnum.COLLECT_IM.getValue(),"",conversation.target,null);
        } else {
            MyCommonBroadBridge.getInstance().setMyBridgeMethod(fragment.getActivity(), BridgeImEnum.COLLECT_IM.getValue(),conversation.target,"",null);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {

        }
    }


    @Override
    public int priority() {
        return 100;
    }

    @Override
    public int iconResId() {
        return R.mipmap.ic_func_broad_collect;
    }

    @Override
    public String title(Context context) {
        return context.getString(R.string.收藏夹);
    }

    @Override
    public String contextMenuTitle(Context context, String tag) {
        return title(context);
    }
}
