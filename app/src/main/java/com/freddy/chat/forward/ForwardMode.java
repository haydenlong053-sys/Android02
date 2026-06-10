package com.freddy.chat.forward;

/**
 * 转发方式：逐条转发（每条走 SEND_MSG 单条发送）或合并转发（transpond contentType=102）。
 */
public enum ForwardMode {
    ONE_BY_ONE,
    MERGE
}
