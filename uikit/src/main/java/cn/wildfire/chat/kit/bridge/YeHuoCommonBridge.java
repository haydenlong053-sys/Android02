package cn.wildfire.chat.kit.bridge;

import android.content.Context;

import cn.wildfire.chat.kit.conversation.message.model.UiMessage;

public class YeHuoCommonBridge {

    private static YeHuoCommonBridge commonBridge = null;
    private YeHuoImCommonListener imCommonListener;

    private YeHuoCommonBridge() {
    }

    public static YeHuoCommonBridge getInstance() {
        if (commonBridge == null) {
            synchronized (YeHuoCommonBridge.class) {
                if (commonBridge == null) {
                    commonBridge = new YeHuoCommonBridge();
                }
            }
        }
        return commonBridge;
    }

    public void setYeHuoImCommonListener(YeHuoImCommonListener imCommonListener) {
        this.imCommonListener = imCommonListener;
    }

    public void setYeHuoQiaoJie(Context mContext, String type, String params, String params2, UiMessage msg) {
        if (imCommonListener != null) {
            imCommonListener.qiaojie(mContext, type, params, params2,msg);
        }
    }

    public interface YeHuoImCommonListener {
        void qiaojie(Context mContext, String type, String params, String params2, UiMessage msg);
    }

}
