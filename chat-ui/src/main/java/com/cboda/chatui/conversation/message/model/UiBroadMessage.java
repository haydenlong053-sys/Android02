/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.conversation.message.model;

import cn.wildfirechat.message.Message;

public class UiBroadMessage {
    public boolean isSelectTouSu;
    public boolean isPlaying;
    public boolean isDownloading;
    public boolean isFocus;
    public boolean isChecked;
    /**
     * media类型消息，上传或下载的进度
     */
    public int progress;
    public Message message;
    public Object extra;

    public boolean continuousPlayAudio;
    public boolean audioPlayCompleted;

    public UiBroadMessage(Message message) {
        this.message = message;
    }

    public UiBroadMessage(Message message, Object extra) {
        this.message = message;
        this.extra = extra;
    }
}
