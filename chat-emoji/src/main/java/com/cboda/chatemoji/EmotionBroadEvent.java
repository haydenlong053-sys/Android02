package com.cboda.chatemoji;

public class EmotionBroadEvent {
    public String params;
    public String type;
    public EmotionBroadEvent(String type, String params){
        this.type = type;
        this.params = params;
    }
}
