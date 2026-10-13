package com.freddy.chat.menu;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cboda.chatui.bridge.ChatPopBroadMenu;
import com.freddy.im.bean.AppMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * 野火式长按气泡菜单（{@link ChatPopBroadMenu}）。
 */
public final class MessageContextMenuHelper {

    private MessageContextMenuHelper() {
    }

    public static void show(@NonNull View anchor,
                            int anchorTopOnScreen,
                            @NonNull AppMessage message,
                            @Nullable String currentUserId,
                            boolean skipRecallTimeLimit,
                            @NonNull MessageListActions actions) {
        if (!MessageMenuPolicy.supportsContextMenu(message)) {
            return;
        }
        List<ChatPopBroadMenu.ChatPopMenuAction> menuActions = new ArrayList<>();
        Context context = anchor.getContext();

        // 撤回入口已隐藏；对所有人删除走 deleteForAll，0x0202 统一本地删库
//        if (MessageMenuPolicy.canRecall(message, currentUserId, skipRecallTimeLimit)) {
//            menuActions.add(action(context, com.cboda.chatui.R.string.message_recall,
//                    () -> actions.onRecallMessage(message)));
//        }
        if (MessageMenuPolicy.canEdit(message, currentUserId)) {
            menuActions.add(action(context, com.cboda.chatui.R.string.message_edit,
                    () -> actions.onEditMessage(message)));
        }
        if (MessageMenuPolicy.canCopy(message)) {
            menuActions.add(action(context, com.cboda.chatui.R.string.message_copy,
                    () -> actions.onCopyMessage(message)));
        }
        if (MessageMenuPolicy.canCollect(message)) {
            menuActions.add(action(context, com.cboda.chatui.R.string.message_collect,
                    () -> actions.onCollectMessage(message)));
        }
        if (MessageMenuPolicy.canMultiSelect(message)) {
            menuActions.add(action(context, com.cboda.chatui.R.string.message_select_more,
                    () -> actions.onEnterMultiSelectMode(message)));
        }
        if (MessageMenuPolicy.canDelete(message)) {
            menuActions.add(action(context, com.cboda.chatui.R.string.message_delete,
                    () -> actions.onDeleteMessage(message)));
        }
        if (MessageMenuPolicy.canForward(message)) {
            menuActions.add(action(context, com.cboda.chatui.R.string.message_forward,
                    () -> actions.onForwardMessages(java.util.Collections.singletonList(message))));
        }
        if (menuActions.isEmpty()) {
            return;
        }
        ChatPopBroadMenu menu = new ChatPopBroadMenu(context);
        menu.setChatPopMenuActionList(menuActions);
        menu.show(anchor, anchorTopOnScreen);
    }

    public static void copyTextToClipboard(@NonNull Context context, @NonNull CharSequence text) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard == null) {
            return;
        }
        clipboard.setPrimaryClip(ClipData.newPlainText("messageContent", text));
        Toast.makeText(context, com.cboda.chatui.R.string.message_copy_success, Toast.LENGTH_SHORT).show();
    }

    @NonNull
    private static ChatPopBroadMenu.ChatPopMenuAction action(@NonNull Context context,
                                                             int titleRes,
                                                             @NonNull Runnable onClick) {
        ChatPopBroadMenu.ChatPopMenuAction action = new ChatPopBroadMenu.ChatPopMenuAction();
        action.setActionName(context.getString(titleRes));
        action.setActionClickListener(onClick::run);
        return action;
    }
}
