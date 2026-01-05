package com.cboda.chatui.bridge;

public class YeHuoBroadEvent {
    public String type;
    public String params1;
    public String params2;

    public YeHuoBroadEvent(String type, String params1, String params2){
        this.type = type;
        this.params1 = params1;
        this.params2 = params2;
    }
}
