package com.freddy.im.http.model.im;

import java.util.HashMap;

/**
 * {@code getLastMsg} 响应 {@code content}：key 为 sessionId，value 为最后一条消息。
 */
public final class ConversationLastMsgMap extends HashMap<String, ImMessageItem> {
}
