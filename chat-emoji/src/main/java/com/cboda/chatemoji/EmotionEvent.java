package com.cboda.chatemoji;

public class EmotionEvent {
    public String params;
    public String type;
    public EmotionEvent(String type, String params){
        this.type = type;
        this.params = params;
    }
}
