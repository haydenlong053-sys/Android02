package com.cboda.baselibrary.event;

/**
 * Created by kxn on 2025/5/7 星期三.
 */

public class UnreadCountBroadEvent {
    public String allMsgCount;
    public String singleCount;
    public String allGroupCount;
    public String atMeCount;
    public String myGroupCount;
    public String otherGroupCount;
    public  String  socialCount;

    public UnreadCountBroadEvent(String allMsgCount, String singleCount, String allGroupCount, String atMeCount) {
        this.allMsgCount = allMsgCount;
        this.singleCount = singleCount;
        this.allGroupCount = allGroupCount;
        this.atMeCount = atMeCount;
    }

    public UnreadCountBroadEvent(String allMsgCount, String singleCount, String allGroupCount, String atMeCount, String myGroupCount, String otherGroupCount) {
        this.allMsgCount = allMsgCount;
        this.singleCount = singleCount;
        this.allGroupCount = allGroupCount;
        this.atMeCount = atMeCount;
        this.myGroupCount = myGroupCount;
        this.otherGroupCount = otherGroupCount;
    }

    public UnreadCountBroadEvent(String allMsgCount, String singleCount, String allGroupCount, String atMeCount, String myGroupCount, String otherGroupCount, String socialCount) {
        this.allMsgCount = allMsgCount;
        this.singleCount = singleCount;
        this.allGroupCount = allGroupCount;
        this.atMeCount = atMeCount;
        this.myGroupCount = myGroupCount;
        this.otherGroupCount = otherGroupCount;
        this.socialCount = socialCount;
    }
}
