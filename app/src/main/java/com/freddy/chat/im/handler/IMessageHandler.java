package com.freddy.chat.im.handler;

import com.freddy.im.bean.AppMessage;

/**
 * <p>@ProjectName:     NettyChat</p>
 * <p>@ClassName:       IMessageHandler.java</p>
 * <p>@PackageName:     com.freddy.chat.im.handler</p>
 * <b>
 * <p>@Description:     类描述</p>
 * </b>
 */
@Deprecated
public interface IMessageHandler {

    void execute(AppMessage message);
}
