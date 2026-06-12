package com.freddy.im;

import com.freddy.im.signal.ImSignalProtos;

/**
 * IM 消息类型定义（会话 {@link #msgType}、内容 {@link MessageContentType}），全局统一使用。
 * <p>
 * 内容类型编号对齐 IM 文档 §10：100 文本、101 转发、102 合并转发、200 图片、300 文件、400 语音、500 视频、600 地图、700 个人名片。
 */
public enum MessageType {

    /** 单聊 */
    SINGLE_CHAT(ImSignalProtos.ConversationType.PAIR_VALUE),

    /** 群聊 */
    GROUP_CHAT(ImSignalProtos.ConversationType.MULTI_VALUE);

    private final int msgType;

    MessageType(int msgType) {
        this.msgType = msgType;
    }

    public int getMsgType() {
        return msgType;
    }

    public static boolean isGroupChat(int msgType) {
        return msgType == GROUP_CHAT.msgType;
    }

    public static boolean isSingleChat(int msgType) {
        return msgType == SINGLE_CHAT.msgType;
    }

    /**
     * 消息内容类型（{@code contentType} / {@code msgContentType}）。
     */
    public enum MessageContentType {
        /** 文本 text/ */
        TEXT(100),
        /** 转发 text/transpond */
        TRANSPOND(101),
        /** 合并转发 text/mergeTranspond */
        MERGE_TRANSPOND(102),
        /** 图片 img/ */
        IMAGE(200),
        /** 文件 file/* */
        FILE(300),
        /** 语音 audio/* */
        VOICE(400),
        /** 视频 video/* */
        VIDEO(500),
        /** 地图 text/map */
        MAP(600),
        /** 个人名片 card */
        CARD(700);

        private final int msgContentType;

        MessageContentType(int msgContentType) {
            this.msgContentType = msgContentType;
        }

        public int getMsgContentType() {
            return msgContentType;
        }

        public static boolean isText(int type) {
            return type == TEXT.msgContentType || type == TRANSPOND.msgContentType;
        }

        public static boolean isImage(int type) {
            return type == IMAGE.msgContentType;
        }

        public static boolean isVoice(int type) {
            return type == VOICE.msgContentType;
        }

        public static boolean isVideo(int type) {
            return type == VIDEO.msgContentType;
        }

        public static boolean isFile(int type) {
            return type == FILE.msgContentType;
        }

        public static boolean isMap(int type) {
            return type == MAP.msgContentType;
        }

        public static boolean isMergeTranspond(int type) {
            return type == MERGE_TRANSPOND.msgContentType;
        }

        public static boolean isCard(int type) {
            return type == CARD.msgContentType;
        }
    }
}
