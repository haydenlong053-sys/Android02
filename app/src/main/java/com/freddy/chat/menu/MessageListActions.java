package com.freddy.chat.menu;

import androidx.annotation.NonNull;

import com.freddy.im.bean.AppMessage;

import java.util.List;

public interface MessageListActions {

    int getListMode();

    boolean isMessageChecked(@NonNull AppMessage message);

    void onRecallMessage(@NonNull AppMessage message);

    void onEditMessage(@NonNull AppMessage message);

    void onCopyMessage(@NonNull AppMessage message);

    void onEnterMultiSelectMode(@NonNull AppMessage message);

    void onDeleteMessage(@NonNull AppMessage message);

    void onForwardMessages(@NonNull List<AppMessage> messages);

    void onCollectMessage(@NonNull AppMessage message);

    void onToggleMessageChecked(@NonNull AppMessage message, boolean checked);

    void onExitMultiSelectMode();

    /** 置顶列表页：长按菜单仅「取消置顶」。 */
    boolean isPinnedMessageListMode();

    /** 当前用户是否可置顶/取消置顶（群主/管理员）。 */
    boolean canManageGroupPins();

    boolean isMessagePinned(@NonNull AppMessage message);

    void onPinMessage(@NonNull AppMessage message);

    void onUnpinMessage(@NonNull AppMessage message);
}
