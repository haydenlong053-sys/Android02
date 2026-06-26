package com.freddy.chat;

import androidx.annotation.Nullable;

/** 消息列表页群公告展示与交互，由 {@link MessageListActivity} 实现。 */
public interface GroupNoticeHost {

    /** 从本地缓存刷新顶栏群公告。 */
    void applyCachedGroupNotice();

    /** 拉取服务端群公告并更新顶栏。 */
    void fetchGroupNoticeFromServer();

    /** 更新顶栏群公告；{@code persist} 为 true 时写入 MMKV。 */
    void updateGroupNoticeUi(@Nullable String notice, boolean persist);

    /** 打开群公告详情页。 */
    void openGroupNoticeDetail();
}
