package com.freddy.im.http;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.freddy.im.http.api.MuteNotifyApi;
import com.freddy.im.http.api.SessionTopApi;
import com.freddy.im.http.model.im.BaseMsgResponse;
import com.freddy.im.listener.OnHttpImListener;
import com.hjq.http.EasyHttp;
import com.hjq.http.lifecycle.ApplicationLifecycle;
import com.hjq.http.listener.OnHttpListener;

/**
 * 会话设置 HTTP（文档 §2.5 置顶、§2.6 免打扰）。
 */
public final class ImConversationHttpExample {

    private static final String TAG = "conversation_http";

    private ImConversationHttpExample() {
    }

    public static void requestSessionTop(@NonNull String sessionId,
                                         boolean top,
                                         @NonNull OnHttpImListener<Boolean> callback,
                                         @Nullable Runnable onFail) {
        if (TextUtils.isEmpty(sessionId)) {
            if (onFail != null) {
                onFail.run();
            }
            return;
        }
        EasyHttp.post(ApplicationLifecycle.getInstance())
                .api(new SessionTopApi().setSessionId(sessionId).setFlag(top))
                .request(boolListener("sessionTop", callback, onFail));
    }

    public static void requestMuteNotify(@NonNull String sessionId,
                                         boolean mute,
                                         @NonNull OnHttpImListener<Boolean> callback,
                                         @Nullable Runnable onFail) {
        if (TextUtils.isEmpty(sessionId)) {
            if (onFail != null) {
                onFail.run();
            }
            return;
        }
        EasyHttp.post(ApplicationLifecycle.getInstance())
                .api(new MuteNotifyApi().setSessionId(sessionId).setFlag(mute))
                .request(boolListener("muteNotify", callback, onFail));
    }

    private static OnHttpListener<BaseMsgResponse<Boolean>> boolListener(
            @NonNull String tag,
            @NonNull OnHttpImListener<Boolean> callback,
            @Nullable Runnable onFail) {
        return new OnHttpListener<BaseMsgResponse<Boolean>>() {
            @Override
            public void onHttpSuccess(@NonNull BaseMsgResponse<Boolean> result) {
                if (!result.isSuccess()) {
                    Log.e(TAG, tag + " 业务错误: errorCode=" + result.getErrorCode()
                            + " msg=" + result.getErrorMsg());
                    if (onFail != null) {
                        onFail.run();
                    }
                    return;
                }
                Boolean content = result.getContent();
                callback.onHttpImSuccess(content != null && content);
            }

            @Override
            public void onHttpFail(@NonNull Throwable throwable) {
                Log.e(TAG, tag + " 请求失败: " + throwable.getMessage());
                if (onFail != null) {
                    onFail.run();
                }
            }
        };
    }
}
