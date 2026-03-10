package com.cboda.chatui.voip.conference.message;

import static com.cboda.baselibrary.utils.ELogUtilKt.toLog;
import static cn.wildfirechat.message.core.MessageContentBroadType.MESSAGE_CONTENT_TYPE_GROUP_BARRAGE;

import android.annotation.SuppressLint;


import cn.wildfirechat.message.Message;
import cn.wildfirechat.message.MessageContent;
import cn.wildfirechat.message.core.ContentTag;
import cn.wildfirechat.message.core.MessagePayload;
import cn.wildfirechat.message.core.PersistFlag;

@SuppressLint("ParcelCreator")
@ContentTag(type = MESSAGE_CONTENT_TYPE_GROUP_BARRAGE, flag = PersistFlag.Transparent)
public class BarrageContent extends MessageContent {
    private String TAG = "BarrageContentClass";

    public int giftType = 0;
    private MessagePayload orignalPayload;

    public MessagePayload getOrignalPayload() {
        return orignalPayload;
    }

    @Override
    public void decode(MessagePayload payload) {
        orignalPayload = payload;
        try {
            giftType = Integer.parseInt(payload.content);
        } catch (Exception e) {
            toLog(e, TAG);
        }
    }

    @Override
    public MessagePayload encode() {
        MessagePayload payload = super.encode();
        payload.content = String.valueOf(giftType);
        return payload;
    }

    @Override
    public String digest(Message message) {
        return "[自定义弹幕]";
    }
}
