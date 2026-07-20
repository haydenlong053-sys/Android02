package com.freddy.im.http;

import static com.cboda.baselibrary.utils.GsonUtilsKt.fromJson2;
import static com.cboda.baselibrary.utils.MMKVBroadUtils.LOGIN_USER_DATA;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cboda.baselibrary.bean.UserInfoBean;
import com.cboda.baselibrary.utils.MMKVBroadUtils;
import com.freddy.im.http.api.CallStartApi;
import com.freddy.im.http.api.CallTokenApi;
import com.freddy.im.http.model.im.BaseMsgResponse;
import com.freddy.im.http.model.im.CallTokenContent;
import com.freddy.im.listener.OnHttpImListener;
import com.google.gson.Gson;
import com.hjq.http.EasyHttp;
import com.hjq.http.lifecycle.ApplicationLifecycle;
import com.hjq.http.listener.OnHttpListener;

import java.util.ArrayList;
import java.util.List;

/**
 * 通话 HTTP：POST {@code call/token}、{@code call/start}（基址见 {@link RequestServer}）。
 */
public final class ImCallHttpExample {

    private static final String TAG = "call_http";
    private static final Gson GSON = new Gson();

    private ImCallHttpExample() {
    }

    public static void requestCallToken(@NonNull String identity,
                                        @NonNull String roomName,
                                        boolean video,
                                        @Nullable String name,
                                        @NonNull OnHttpImListener<CallTokenContent> callback,
                                        @Nullable Runnable onFail) {
        if (TextUtils.isEmpty(identity) || TextUtils.isEmpty(roomName)) {
            if (onFail != null) {
                onFail.run();
            }
            return;
        }
        CallTokenApi api = new CallTokenApi()
                .setIdentity(identity)
                .setRoomName(roomName)
                .setVideo(video);
        if (!TextUtils.isEmpty(name)) {
            api.setName(name);
        }
        EasyHttp.post(ApplicationLifecycle.getInstance())
                .api(api)
                .request(new OnHttpListener<BaseMsgResponse<CallTokenContent>>() {
                    @Override
                    public void onHttpSuccess(@NonNull BaseMsgResponse<CallTokenContent> result) {
                        if (!result.isSuccess()) {
                            Log.e(TAG, "getCallToken 业务错误: errorCode=" + result.getErrorCode()
                                    + " msg=" + result.getErrorMsg());
                            if (onFail != null) {
                                onFail.run();
                            }
                            return;
                        }
                        CallTokenContent content = resolveContent(result.getContent());
                        if (content == null || TextUtils.isEmpty(content.getToken())) {
                            Log.e(TAG, "getCallToken content 或 token 为空");
                            if (onFail != null) {
                                onFail.run();
                            }
                            return;
                        }
                        callback.onHttpImSuccess(content);
                    }

                    @Override
                    public void onHttpFail(@NonNull Throwable throwable) {
                        Log.e(TAG, "getCallToken 请求失败: " + throwable.getMessage());
                        if (onFail != null) {
                            onFail.run();
                        }
                    }
                });
    }

    public static void requestCallStart(@NonNull String identity,
                                        boolean video,
                                        @NonNull OnHttpImListener<CallTokenContent> callback,
                                        @Nullable Runnable onFail) {
        List<String> idList = new ArrayList<>();
        idList.add(identity);
        requestCallStart(idList, video, callback, onFail);
    }

    public static void requestCallStart(@NonNull List<String> toUserIds,
                                        boolean video,
                                        @NonNull OnHttpImListener<CallTokenContent> callback,
                                        @Nullable Runnable onFail) {
        List<String> idList = new ArrayList<>();
        for (String id : toUserIds) {
            if (!TextUtils.isEmpty(id)) {
                idList.add(id);
            }
        }
        if (idList.isEmpty()) {
            if (onFail != null) {
                onFail.run();
            }
            return;
        }
        UserInfoBean userInfoBean = fromJson2(MMKVBroadUtils.Companion.getSqlStrData(LOGIN_USER_DATA, ""), UserInfoBean.class);
        CallStartApi api = new CallStartApi()
                .setMediaType(video ? "video" : "audio")
                .setToUserIds(idList)
                .setFromUsername(userInfoBean.getName())
                .setFromUserIcon(userInfoBean.getUserImg());
//        if (!TextUtils.isEmpty(name)) {
//            api.setName(name);
//        }
        EasyHttp.post(ApplicationLifecycle.getInstance())
                .api(api)
                .request(new OnHttpListener<BaseMsgResponse<CallTokenContent>>() {
                    @Override
                    public void onHttpSuccess(@NonNull BaseMsgResponse<CallTokenContent> result) {
                        if (!result.isSuccess()) {
                            Log.e(TAG, "startCall 业务错误: errorCode=" + result.getErrorCode()
                                    + " msg=" + result.getErrorMsg());
                            if (onFail != null) {
                                onFail.run();
                            }
                            return;
                        }
                        CallTokenContent content = resolveContent(result.getContent());
                        if (content == null || TextUtils.isEmpty(content.getToken())) {
                            Log.e(TAG, "startCall content 或 token 为空");
                            if (onFail != null) {
                                onFail.run();
                            }
                            return;
                        }
                        callback.onHttpImSuccess(content);
                    }

                    @Override
                    public void onHttpFail(@NonNull Throwable throwable) {
                        Log.e(TAG, "startCall 请求失败: " + throwable.getMessage());
                        if (onFail != null) {
                            onFail.run();
                        }
                    }
                });
    }

    @Nullable
    private static CallTokenContent resolveContent(@Nullable Object raw) {
        if (raw == null) {
            return null;
        }
        if (raw instanceof CallTokenContent) {
            return (CallTokenContent) raw;
        }
        return GSON.fromJson(GSON.toJson(raw), CallTokenContent.class);
    }
}
