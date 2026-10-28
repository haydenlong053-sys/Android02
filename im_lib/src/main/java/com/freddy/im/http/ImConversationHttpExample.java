package com.freddy.im.http;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.freddy.im.http.api.GetConvFoldersApi;
import com.freddy.im.http.api.MuteNotifyApi;
import com.freddy.im.http.api.SaveFoldersApi;
import com.freddy.im.http.api.SessionTopApi;
import com.freddy.im.http.model.im.BaseMsgResponse;
import com.freddy.im.http.model.im.ConvFolderItem;
import com.freddy.im.http.model.im.ConvFoldersContent;
import com.freddy.im.listener.OnHttpImListener;
import com.hjq.http.EasyHttp;
import com.hjq.http.lifecycle.ApplicationLifecycle;
import com.hjq.http.listener.OnHttpListener;

import java.util.Collections;
import java.util.List;

/**
 * 会话设置 HTTP（文档 §2.5 置顶、§2.6 免打扰、§ convFolders）。
 */
public final class ImConversationHttpExample {

    private static final String TAG = "conversation_http";

    /** 带业务 errorCode 的失败回调（用于 saveFolders 3048）。 */
    public interface OnBizFail {
        void onFail(int errorCode, @Nullable String errorMsg);
    }

    private ImConversationHttpExample() {
    }

    /** GET {@code im/conversation/folders} */
    public static void requestGetFolders(@NonNull OnHttpImListener<ConvFoldersContent> callback,
                                         @Nullable Runnable onFail) {
        EasyHttp.get(ApplicationLifecycle.getInstance())
                .api(new GetConvFoldersApi())
                .request(new OnHttpListener<BaseMsgResponse<ConvFoldersContent>>() {
                    @Override
                    public void onHttpSuccess(@NonNull BaseMsgResponse<ConvFoldersContent> result) {
                        if (!result.isSuccess()) {
                            Log.e(TAG, "getFolders 业务错误: errorCode=" + result.getErrorCode()
                                    + " msg=" + result.getErrorMsg());
                            if (onFail != null) {
                                onFail.run();
                            }
                            return;
                        }
                        ConvFoldersContent content = result.getContent();
                        if (content == null) {
                            content = new ConvFoldersContent();
                            content.setVersion(0L);
                            content.setFolders(Collections.emptyList());
                        }
                        callback.onHttpImSuccess(content);
                    }

                    @Override
                    public void onHttpFail(@NonNull Throwable throwable) {
                        Log.e(TAG, "getFolders 请求失败: " + throwable.getMessage());
                        if (onFail != null) {
                            onFail.run();
                        }
                    }
                });
    }

    /** POST {@code im/conversation/saveFolders} */
    public static void requestSaveFolders(long version,
                                          @Nullable List<ConvFolderItem> folders,
                                          @NonNull OnHttpImListener<ConvFoldersContent> callback,
                                          @Nullable OnBizFail onFail) {
        EasyHttp.post(ApplicationLifecycle.getInstance())
                .api(new SaveFoldersApi()
                        .setVersion(version)
                        .setFolders(folders != null ? folders : Collections.emptyList()))
                .request(new OnHttpListener<BaseMsgResponse<ConvFoldersContent>>() {
                    @Override
                    public void onHttpSuccess(@NonNull BaseMsgResponse<ConvFoldersContent> result) {
                        if (!result.isSuccess()) {
                            Log.e(TAG, "saveFolders 业务错误: errorCode=" + result.getErrorCode()
                                    + " msg=" + result.getErrorMsg());
                            if (onFail != null) {
                                onFail.onFail(result.getErrorCode(), result.getErrorMsg());
                            }
                            return;
                        }
                        ConvFoldersContent content = result.getContent();
                        if (content == null) {
                            content = new ConvFoldersContent();
                            content.setVersion(version);
                            content.setFolders(folders != null ? folders : Collections.emptyList());
                        }
                        callback.onHttpImSuccess(content);
                    }

                    @Override
                    public void onHttpFail(@NonNull Throwable throwable) {
                        Log.e(TAG, "saveFolders 请求失败: " + throwable.getMessage());
                        if (onFail != null) {
                            onFail.onFail(-1, throwable.getMessage());
                        }
                    }
                });
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
