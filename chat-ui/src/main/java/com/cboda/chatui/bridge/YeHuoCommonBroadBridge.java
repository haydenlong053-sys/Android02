package com.cboda.chatui.bridge;

import android.content.Context;

import com.cboda.chatui.conversation.message.model.UiBroadMessage;

public class YeHuoCommonBroadBridge {

    private static YeHuoCommonBroadBridge commonBridge = null;
    private YeHuoImCommonListener imCommonListener;

    private YeHuoCommonBroadBridge() {
    }

    public static YeHuoCommonBroadBridge getInstance() {
        if (commonBridge == null) {
            synchronized (YeHuoCommonBroadBridge.class) {
                if (commonBridge == null) {
                    commonBridge = new YeHuoCommonBroadBridge();
                }
            }
        }
        return commonBridge;
    }

    public void setYeHuoImCommonListener(YeHuoImCommonListener imCommonListener) {
        this.imCommonListener = imCommonListener;
    }

    public void setYeHuoQiaoJie(Context mContext, String type, String params, String params2, UiBroadMessage msg) {
        if (imCommonListener != null) {
            imCommonListener.qiaojie(mContext, type, params, params2,msg);
        }
    }

    public interface YeHuoImCommonListener {
        void qiaojie(Context mContext, String type, String params, String params2, UiBroadMessage msg);
    }

}
