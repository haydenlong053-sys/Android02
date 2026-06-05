package com.freddy.im;

import com.freddy.im.listener.OnEventListener;
import com.freddy.im.signal.ImSignalProtos;

/**
 * 将下行 {@link ImSignalProtos} 回调到应用层。
 */
public class MsgDispatcher {

    private OnEventListener mOnEventListener;

    public MsgDispatcher() {
    }

    public void setOnEventListener(OnEventListener listener) {
        this.mOnEventListener = listener;
    }

    public void dispatchSignalResponse(ImSignalProtos.SignalResponse response) {
        if (mOnEventListener == null) {
            return;
        }
        mOnEventListener.onSignalResponse(response);
    }

    public void dispatchSignalPush(ImSignalProtos.SignalPush push) {
        if (mOnEventListener == null) {
            return;
        }
        mOnEventListener.onSignalPush(push);
    }
}
