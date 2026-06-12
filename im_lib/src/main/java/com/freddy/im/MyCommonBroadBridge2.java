package com.freddy.im;

import android.content.Context;

import com.freddy.im.bean.AppMessage;


public class MyCommonBroadBridge2 {

    private static MyCommonBroadBridge2 commonBridge = null;
    private MyImCommonListener2 imCommonListener;

    private MyCommonBroadBridge2() {
    }

    public static MyCommonBroadBridge2 getInstance() {
        if (commonBridge == null) {
            synchronized (MyCommonBroadBridge2.class) {
                if (commonBridge == null) {
                    commonBridge = new MyCommonBroadBridge2();
                }
            }
        }
        return commonBridge;
    }

    public void setMyImCommonListener2(MyImCommonListener2 imCommonListener) {
        this.imCommonListener = imCommonListener;
    }

    public void setMyBridgeMethod2(Context mContext, String type, String params, String params2, AppMessage msg) {
        if (imCommonListener != null) {
            imCommonListener.goBridgeMethod(mContext, type, params, params2,msg);
        }
    }

    public interface MyImCommonListener2 {
        void goBridgeMethod(Context mContext, String type, String params, String params2, AppMessage msg);
    }

}
