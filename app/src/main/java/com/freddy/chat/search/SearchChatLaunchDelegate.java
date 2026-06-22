package com.freddy.chat.search;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.freddy.im.http.model.im.SessionItem;

/**
 * 自研 IM 搜索页跳转（app 层，可依赖 im_lib）。
 */
public interface SearchChatLaunchDelegate {

    void openSingleChat(@NonNull Fragment fragment,
                        @NonNull String userId,
                        @Nullable String displayName);

    void openGroupChat(@NonNull Fragment fragment, @NonNull SessionItem sessionItem);
}
