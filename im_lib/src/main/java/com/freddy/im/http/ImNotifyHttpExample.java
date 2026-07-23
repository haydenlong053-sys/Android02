package com.freddy.im.http;

import static com.cboda.baselibrary.utils.GsonUtilsKt.fromJson2;
import static com.cboda.baselibrary.utils.MMKVBroadUtils.LOGIN_USER_DATA;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cboda.baselibrary.bean.UserInfoBean;
import com.cboda.baselibrary.utils.MMKVBroadUtils;
import com.freddy.im.http.api.BusiPushApi;
import com.freddy.im.http.model.im.BaseMsgResponse;
import com.freddy.im.listener.OnHttpImListener;
import com.freddy.im.signal.OthBusiSigCodes;
import com.google.gson.JsonObject;
import com.hjq.http.EasyHttp;
import com.hjq.http.lifecycle.ApplicationLifecycle;
import com.hjq.http.listener.OnHttpListener;

/**
 * IM 业务推送 HTTP（{@code api/im/notify/busiPush}）。
 */
public final class ImNotifyHttpExample {

    private static final String TAG = "notify_http";

    /**
     * 鲜花
     */
    public static final int GIFT_FLOWER = 1;
    /**
     * 鼓掌
     */
    public static final int GIFT_ACCLAMATION = 2;

    private ImNotifyHttpExample() {
    }

    /**
     * 会议礼物弹幕推送：code=2001，giftType 1=鲜花 / 2=鼓掌。
     */
    public static void requestMeetingGift(int giftType,
                                          @Nullable String groupId,
                                          @Nullable String fromUserName,
                                          @Nullable OnHttpImListener<Boolean> callback,
                                          @Nullable Runnable onFail) {
        if (giftType != GIFT_FLOWER && giftType != GIFT_ACCLAMATION) {
            Log.e(TAG, "非法 giftType=" + giftType + "，仅支持 1=鲜花 / 2=鼓掌");
            if (onFail != null) {
                onFail.run();
            }
            return;
        }
        if (TextUtils.isEmpty(groupId)) {
            Log.e(TAG, "groupId 为空，跳过 busiPush");
            if (onFail != null) {
                onFail.run();
            }
            return;
        }
        String fromUserId = MMKVBroadUtils.Companion.getRealUserId();
        String uid = fromUserId == null ? "" : fromUserId;
        JsonObject data = new JsonObject();
        data.addProperty("groupId", groupId);
        data.addProperty("fromUserId", uid);
        data.addProperty("fromUserName", fromUserName);
        data.addProperty("giftType", giftType);
        requestBusiPush(groupId, OthBusiSigCodes.MEETING_GIFT, data.toString(), callback, onFail);
    }

    /**
     * 通用业务推送。
     *
     * @param groupId  群 ID
     * @param code     业务 code（如 2001）
     * @param dataJson data JSON 字符串
     */
    public static void requestBusiPush(@NonNull String groupId,
                                       int code,
                                       @Nullable String dataJson,
                                       @Nullable OnHttpImListener<Boolean> callback,
                                       @Nullable Runnable onFail) {
        if (TextUtils.isEmpty(groupId)) {
            if (onFail != null) {
                onFail.run();
            }
            return;
        }
        EasyHttp.post(ApplicationLifecycle.getInstance())
                .api(new BusiPushApi()
                        .setGroupId(groupId)
                        .setCode(code)
                        .setData(dataJson))
                .request(new OnHttpListener<BaseMsgResponse<Boolean>>() {
                    @Override
                    public void onHttpSuccess(@NonNull BaseMsgResponse<Boolean> result) {
                        if (!result.isSuccess()) {
                            Log.e(TAG, "busiPush 业务错误: errorCode=" + result.getErrorCode()
                                    + " msg=" + result.getErrorMsg());
                            if (onFail != null) {
                                onFail.run();
                            }
                            return;
                        }
                        if (callback != null) {
                            Boolean content = result.getContent();
                            callback.onHttpImSuccess(content != null && content);
                        }
                    }

                    @Override
                    public void onHttpFail(@NonNull Throwable throwable) {
                        Log.e(TAG, "busiPush 请求失败: " + throwable.getMessage());
                        if (onFail != null) {
                            onFail.run();
                        }
                    }
                });
    }
}
