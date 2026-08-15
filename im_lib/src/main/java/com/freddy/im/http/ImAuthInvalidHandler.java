package com.freddy.im.http;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;

import com.freddy.im.http.model.im.BaseMsgResponse;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 自研 IM HTTP 鉴权/权限失效统一处理：
 * <ul>
 *   <li>{@code 1001007} 无效 token</li>
 *   <li>{@code 1001004} 无权操作</li>
 * </ul>
 * 由宿主在 Application 中注册 {@link Listener}，触发 Toast + 失效重登。
 */
public final class ImAuthInvalidHandler {

    private static final String TAG = "ImAuthInvalid";

    /** 无权操作（与无效 token 相同：Toast + 失效重登） */
    public static final int ERROR_NO_PERMISSION = 1001004;
    /** 无效的 token（需重新登录） */
    public static final int ERROR_INVALID_TOKEN = 1001007;

    public interface Listener {
        void onImTokenInvalid(int errorCode, @Nullable String errorMsg);
    }

    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    private static final AtomicBoolean HANDLING = new AtomicBoolean(false);

    @Nullable
    private static volatile Listener listener;

    private ImAuthInvalidHandler() {
    }

    public static void setListener(@Nullable Listener l) {
        listener = l;
        HANDLING.set(false);
    }

    /** 是否走登录失效流程（无效 token / 无权操作）。 */
    public static boolean isInvalidToken(int errorCode) {
        return errorCode == ERROR_INVALID_TOKEN || errorCode == ERROR_NO_PERMISSION;
    }

    /**
     * 业务失败时调用；若为鉴权/权限失效则回调宿主（主线程、防重复）。
     *
     * @return {@code true} 表示已识别为鉴权失效
     */
    public static boolean handleIfNeeded(int errorCode, @Nullable String errorMsg) {
        if (!isInvalidToken(errorCode)) {
            return false;
        }
        Log.w(TAG, "IM auth invalid errorCode=" + errorCode + " msg=" + errorMsg);
        if (!HANDLING.compareAndSet(false, true)) {
            return true;
        }
        final Listener l = listener;
        final int code = errorCode;
        final String msg = errorMsg;
        MAIN.post(() -> {
            try {
                if (l != null) {
                    l.onImTokenInvalid(code, msg);
                } else {
                    Log.w(TAG, "ImAuthInvalidHandler listener 未注册");
                    HANDLING.set(false);
                }
            } catch (Exception e) {
                Log.e(TAG, "onImTokenInvalid failed", e);
                HANDLING.set(false);
            }
        });
        return true;
    }

    /** 宿主完成 exit 后可重置，便于再次登录后还能再次触发。 */
    public static void reset() {
        HANDLING.set(false);
    }

    public static boolean handleIfNeeded(@Nullable BaseMsgResponse<?> result) {
        if (result == null || result.isSuccess()) {
            return false;
        }
        return handleIfNeeded(result.getErrorCode(), result.getErrorMsg());
    }
}
