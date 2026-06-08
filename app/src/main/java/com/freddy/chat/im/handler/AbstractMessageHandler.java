package com.freddy.chat.im.handler;

import com.freddy.im.bean.AppMessage;

/**
 * <p>@ProjectName:     NettyChat</p>
 * <p>@ClassName:       AbstractMessageHandler.java</p>
 * <p>@PackageName:     com.freddy.chat.im.handler</p>
 * <b>
 * <p>@Description:     抽象的MessageHandler</p>
 * </b>
 */
@Deprecated
public abstract class AbstractMessageHandler implements IMessageHandler {

    @Override
    public void execute(AppMessage message) {
        action(message);
    }

    protected abstract void action(AppMessage message);
}
