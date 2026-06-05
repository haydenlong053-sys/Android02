package com.freddy.im.http.model.im;

import com.freddy.im.http.ImHttpConfig;

import java.util.List;

@SuppressWarnings("unused")
public class MessageListContent {

    private String groupId;
    private int anchor;
    private List<ImMessageItem> msgList;

    public String getGroupId() {
        return groupId;
    }

    public int getAnchor() {
        return anchor;
    }

    public List<ImMessageItem> getMsgList() {
        return msgList;
    }

    public boolean isHasNextPage (){
        return anchor == ImHttpConfig.anchorPageSize;
    }
}
