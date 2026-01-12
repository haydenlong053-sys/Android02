package com.cboda.chatui.bridge;

import android.content.Context;

import com.cboda.chatui.conversation.message.model.UiBroadMessage;

public class MyCommonBroadBridge {

    private static MyCommonBroadBridge commonBridge = null;
    private MyImCommonListener imCommonListener;

    private MyCommonBroadBridge() {
    }

    public static MyCommonBroadBridge getInstance() {
        if (commonBridge == null) {
            synchronized (MyCommonBroadBridge.class) {
                if (commonBridge == null) {
                    commonBridge = new MyCommonBroadBridge();
                }
            }
        }
        return commonBridge;
    }

    public void setMyImCommonListener(MyImCommonListener imCommonListener) {
        this.imCommonListener = imCommonListener;
    }

    public void setMyQiaoJie(Context mContext, String type, String params, String params2, UiBroadMessage msg) {
        if (imCommonListener != null) {
            imCommonListener.qiaojie(mContext, type, params, params2,msg);
        }
    }

    public interface MyImCommonListener {
        void qiaojie(Context mContext, String type, String params, String params2, UiBroadMessage msg);
    }

}
