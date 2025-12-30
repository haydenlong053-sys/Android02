package cn.wildfirechat.event;

import androidx.annotation.NonNull;

public class MsgUnReadCountEvent {
    public  int singleCount;
    public  int groupCount;
    /**
     * 群聊@数
     */
    public int unreadMention;
    /**
     * 群聊@All数
     */
    public int unreadMentionAll;
    public int myGroupCount;
    public int otherGroupCount;

    public MsgUnReadCountEvent() {
    }

    public MsgUnReadCountEvent(int singleCount, int groupCount, int unreadMention, int unreadMentionAll) {
        this.singleCount = singleCount;
        this.groupCount = groupCount;
        this.unreadMention = unreadMention;
        this.unreadMentionAll = unreadMentionAll;
    }

    @NonNull
    @Override
    public String toString() {
        return "MsgUnReadCountEvent{" +
                "singleCount=" + singleCount +
                ", groupCount=" + groupCount +
                ", unreadMention=" + unreadMention +
                ", unreadMentionAll=" + unreadMentionAll +
                ", myGroupCount=" + myGroupCount +
                ", otherGroupCount=" + otherGroupCount +
                '}';
    }
}
