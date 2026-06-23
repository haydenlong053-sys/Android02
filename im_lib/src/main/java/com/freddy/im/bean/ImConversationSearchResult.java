package com.freddy.im.bean;

import androidx.annotation.Nullable;

import com.freddy.im.http.model.im.SessionItem;

/**
 * 聊天记录搜索结果：每个会话一条，含最近一条命中消息与命中总数。
 */
public class ImConversationSearchResult {

  @Nullable
  public SessionItem sessionItem;

  @Nullable
  public AppMessage latestMatchedMessage;

  public int matchedCount;
}
