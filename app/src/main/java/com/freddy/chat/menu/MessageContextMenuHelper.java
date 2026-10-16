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

    @Nullable
    private static ChatPopBroadMenu sShowingMenu;

    private MessageContextMenuHelper() {
    }

    /** Activity/Fragment 销毁时调用，避免 PopupWindow 泄漏。 */
    public static void dismissShowing() {
        ChatPopBroadMenu menu = sShowingMenu;
        sShowingMenu = null;
        if (menu != null) {
            menu.hide();
        }
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
        if (actions.isPinnedMessageListMode()) {
            // 置顶列表：仅群主/管理员显示「取消置顶」
            menuActions.clear();
            if (!actions.canManageGroupPins()) {
                return;
            }
            menuActions.add(action(context, com.cboda.baselibrary.R.string.cancel_zhiding,
                    () -> actions.onUnpinMessage(message)));
        } else if (actions.canManageGroupPins() && MessageMenuPolicy.isPinnableMessage(message)) {
            if (actions.isMessagePinned(message)) {
                menuActions.add(action(context, com.cboda.baselibrary.R.string.cancel_zhiding,
                        () -> actions.onUnpinMessage(message)));
            } else {
                menuActions.add(action(context, com.cboda.intelligent.R.string.zhiding,
                        () -> actions.onPinMessage(message)));
            }
        }
        if (menuActions.isEmpty()) {
            return;
        }
        dismissShowing();
        ChatPopBroadMenu menu = new ChatPopBroadMenu(context);
        sShowingMenu = menu;
        menu.setOnDismissListener(() -> {
            if (sShowingMenu == menu) {
                sShowingMenu = null;
            }
        });
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
