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
}
