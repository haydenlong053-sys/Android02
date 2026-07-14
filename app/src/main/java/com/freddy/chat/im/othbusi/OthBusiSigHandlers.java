package com.freddy.chat.im.othbusi;

import com.freddy.im.signal.othbusi.OthBusiSigDispatcher;

/**
 * 应用层 OthBusiSig 处理器注册表；新增业务时在此 {@link #create()} 中 register 即可。
 */
public final class OthBusiSigHandlers {

    private static final OthBusiSigDispatcher DISPATCHER = create();

    private OthBusiSigHandlers() {
    }

    public static OthBusiSigDispatcher get() {
        return DISPATCHER;
    }

    private static OthBusiSigDispatcher create() {
        OthBusiSigDispatcher dispatcher = new OthBusiSigDispatcher();
        dispatcher.register(new GroupNoticeOthBusiSigHandler());
        dispatcher.register(new GroupAllMuteOthBusiSigHandler());
        dispatcher.register(new GroupMemberMuteOthBusiSigHandler());
        dispatcher.register(new GroupManagerRoleOthBusiSigHandler());
        dispatcher.register(new MeetStartOthBusiSigHandler());
        dispatcher.register(new MeetEndOthBusiSigHandler());
        dispatcher.register(new MeetUpMicApplyOthBusiSigHandler());
        dispatcher.register(new MeetSeatChangeOthBusiSigHandler());
        dispatcher.register(new MeetingGiftOthBusiSigHandler());
        return dispatcher;
    }
}
