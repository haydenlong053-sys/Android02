package com.freddy.im.http;

import static com.cboda.baselibrary.utils.GsonUtilsKt.fromJson2;
import static com.cboda.baselibrary.utils.MMKVBroadUtils.LOGIN_USER_DATA;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cboda.baselibrary.bean.UserInfoBean;
import com.cboda.baselibrary.toast.ToastBroadUtils;
import com.cboda.baselibrary.utils.ELogUtilKt;
import com.cboda.baselibrary.utils.MMKVBroadUtils;
import com.freddy.im.http.api.MeetingCreateApi;
import com.freddy.im.http.api.MeetingEndApi;
import com.freddy.im.http.api.MeetingInfoApi;
import com.freddy.im.http.api.MeetingInfoByGroupApi;
import com.freddy.im.http.api.MeetingJoinApi;
import com.freddy.im.http.api.MeetingLeaveApi;
import com.freddy.im.http.api.MeetingSpeakApi;
import com.freddy.im.http.model.im.BaseMsgListResponse;
import com.freddy.im.http.model.im.BaseMsgResponse;
import com.freddy.im.http.model.im.MeetingEndContent;
import com.freddy.im.http.model.im.MeetingSessionVO;
import com.freddy.im.listener.OnHttpImListener;
import com.google.gson.Gson;
import com.hjq.http.EasyHttp;
import com.hjq.http.lifecycle.ApplicationLifecycle;
import com.hjq.http.listener.OnHttpListener;

import java.util.List;

/**
 * 群会议 / 直播 HTTP：{@code /im/meeting/*}（LiveKit SFU）。
 */
public final class ImMeetingHttpExample {

    private static final String TAG = "meeting_http";
    private static final Gson GSON = ImGsonHelper.getGson();

    private ImMeetingHttpExample() {
    }

    /** 创建群语音会议室：mode=live、mediaType=audio、默认 4 麦。 */
    public static void requestCreateLiveAudio(@Nullable String title,
                                              @Nullable String groupId,
                                              int maxSpeakers,
                                              @NonNull OnHttpImListener<MeetingSessionVO> callback,
                                              @Nullable Runnable onFail) {
        UserInfoBean user = fromJson2(
                MMKVBroadUtils.Companion.getSqlStrData(LOGIN_USER_DATA, ""),
                UserInfoBean.class
        );
        MeetingCreateApi api = new MeetingCreateApi()
                .setMode("live")
                .setMediaType("audio")
                .setMaxSpeakers(maxSpeakers > 0 ? maxSpeakers : 4)
                .setTitle(title)
                .setGroupId(groupId);
        if (user != null) {
            api.setFromUsername(user.getName());
            api.setFromUserIcon(user.getUserImg());
        }
        EasyHttp.post(ApplicationLifecycle.getInstance())
                .api(api)
                .request(sessionListener("create", callback, onFail, true));
    }

    public static void requestJoin(@Nullable String meetingId,
                                   @Nullable String displayName,
                                   @NonNull OnHttpImListener<MeetingSessionVO> callback,
                                   @Nullable Runnable onFail) {
        if (TextUtils.isEmpty(meetingId) ) {
            if (onFail != null) {
                onFail.run();
            }
            return;
        }
        MeetingJoinApi api = new MeetingJoinApi()
                .setMeetingId(meetingId)
                .setName(displayName)
                .setMediaType("audio");
        EasyHttp.post(ApplicationLifecycle.getInstance())
                .api(api)
                .request(sessionListener("join", callback, onFail, true));
    }

    public static void requestLiveByGroup(@NonNull String groupId,
                                   @NonNull OnHttpImListener<MeetingSessionVO> callback,
                                   @Nullable Runnable onFail) {
        EasyHttp.get(ApplicationLifecycle.getInstance())
                .api(new MeetingInfoByGroupApi().setGroupId(groupId))
                .request(  new OnHttpListener<BaseMsgListResponse<MeetingSessionVO>>() {
            @Override
            public void onHttpSuccess(@NonNull BaseMsgListResponse<MeetingSessionVO> result) {
                if (!result.isSuccess()) {
                    String msg = result.getErrorMsg();
                    ToastBroadUtils.show(TextUtils.isEmpty(msg) ? "会议请求开小差了" : msg);
                    if (onFail != null) {
                        onFail.run();
                    }
                    return;
                }
                List<MeetingSessionVO> content = result.getContent();
                if (content == null ||content.isEmpty()) {
                    if (onFail != null) {
                        onFail.run();
                    }
                    return;
                }
                ELogUtilKt.eLogD(ELogUtilKt.APP_IM_TAG,"会议查询成功1---"+content.get(0).getMeetingId());
                callback.onHttpImSuccess(content.get(0));
            }

            @Override
            public void onHttpFail(@NonNull Throwable throwable) {
                String msg = throwable.getMessage();
                ToastBroadUtils.show(TextUtils.isEmpty(msg) ? "会议请求失败" : msg);
                if (onFail != null) {
                    onFail.run();
                }
            }
        });
    }


    public static void requestLeave(@Nullable String meetingId,
                                    @Nullable String roomName,
                                    @Nullable OnHttpImListener<Boolean> callback,
                                    @Nullable Runnable onFail) {
        if (TextUtils.isEmpty(meetingId) && TextUtils.isEmpty(roomName)) {
            if (onFail != null) {
                onFail.run();
            }
            return;
        }
        EasyHttp.post(ApplicationLifecycle.getInstance())
                .api(new MeetingLeaveApi().setMeetingId(meetingId).setRoomName(roomName))
                .request(new OnHttpListener<BaseMsgResponse<Object>>() {
                    @Override
                    public void onHttpSuccess(@NonNull BaseMsgResponse<Object> result) {
                        if (!result.isSuccess()) {
                            Log.e(TAG, "leave 业务错误: " + result.getErrorMsg());
                            if (onFail != null) {
                                onFail.run();
                            }
                            return;
                        }
                        if (callback != null) {
                            callback.onHttpImSuccess(true);
                        }
                    }

                    @Override
                    public void onHttpFail(@NonNull Throwable throwable) {
                        Log.e(TAG, "leave 失败: " + throwable.getMessage());
                        if (onFail != null) {
                            onFail.run();
                        }
                    }
                });
    }

    public static void requestEnd(@Nullable String meetingId,
                                  @Nullable String roomName,
                                  @Nullable OnHttpImListener<MeetingEndContent> callback,
                                  @Nullable Runnable onFail) {
        if (TextUtils.isEmpty(meetingId) && TextUtils.isEmpty(roomName)) {
            if (onFail != null) {
                onFail.run();
            }
            return;
        }
        EasyHttp.post(ApplicationLifecycle.getInstance())
                .api(new MeetingEndApi().setMeetingId(meetingId).setRoomName(roomName))
                .request(new OnHttpListener<BaseMsgResponse<MeetingEndContent>>() {
                    @Override
                    public void onHttpSuccess(@NonNull BaseMsgResponse<MeetingEndContent> result) {
                        if (!result.isSuccess()) {
                            Log.e(TAG, "end 业务错误: " + result.getErrorMsg());
                            String msg = result.getErrorMsg();
                            ToastBroadUtils.show(TextUtils.isEmpty(msg) ? "结束会议失败" : msg);
                            if (onFail != null) {
                                onFail.run();
                            }
                            return;
                        }
                        if (callback != null) {
                            callback.onHttpImSuccess(resolveEnd(result.getContent()));
                        }
                    }

                    @Override
                    public void onHttpFail(@NonNull Throwable throwable) {
                        Log.e(TAG, "end 失败: " + throwable.getMessage());
                        ToastBroadUtils.show(TextUtils.isEmpty(throwable.getMessage())
                                ? "结束会议失败" : throwable.getMessage());
                        if (onFail != null) {
                            onFail.run();
                        }
                    }
                });
    }

    public static void requestInfo(@Nullable String meetingId,
                                   @Nullable String roomName,
                                   @NonNull OnHttpImListener<MeetingSessionVO> callback,
                                   @Nullable Runnable onFail) {
        if (TextUtils.isEmpty(meetingId) && TextUtils.isEmpty(roomName)) {
            if (onFail != null) {
                onFail.run();
            }
            return;
        }
        EasyHttp.get(ApplicationLifecycle.getInstance())
                .api(new MeetingInfoApi().setMeetingId(meetingId).setRoomName(roomName))
                .request(sessionListener("info", callback, onFail, false));
    }

    public static void requestSpeakApply(@Nullable String meetingId,
                                         @Nullable String roomName,
                                         @NonNull OnHttpImListener<MeetingSessionVO> callback,
                                         @Nullable Runnable onFail) {
        requestSpeak(MeetingSpeakApi.APPLY, meetingId, roomName, null, callback, onFail, false);
    }

    public static void requestSpeakCancel(@Nullable String meetingId,
                                          @Nullable String roomName,
                                          @NonNull OnHttpImListener<MeetingSessionVO> callback,
                                          @Nullable Runnable onFail) {
        requestSpeak(MeetingSpeakApi.CANCEL, meetingId, roomName, null, callback, onFail, false);
    }

    public static void requestSpeakApprove(@Nullable String meetingId,
                                           @Nullable String roomName,
                                           @NonNull String targetUserId,
                                           @NonNull OnHttpImListener<MeetingSessionVO> callback,
                                           @Nullable Runnable onFail) {
        requestSpeak(MeetingSpeakApi.APPROVE, meetingId, roomName, targetUserId, callback, onFail, true);
    }

    public static void requestSpeakReject(@Nullable String meetingId,
                                          @Nullable String roomName,
                                          @NonNull String targetUserId,
                                          @NonNull OnHttpImListener<MeetingSessionVO> callback,
                                          @Nullable Runnable onFail) {
        requestSpeak(MeetingSpeakApi.REJECT, meetingId, roomName, targetUserId, callback, onFail, true);
    }

    public static void requestSpeakLeave(@Nullable String meetingId,
                                         @Nullable String roomName,
                                         @NonNull OnHttpImListener<MeetingSessionVO> callback,
                                         @Nullable Runnable onFail) {
        requestSpeak(MeetingSpeakApi.LEAVE, meetingId, roomName, null, callback, onFail, false);
    }

    public static void requestSpeakKick(@Nullable String meetingId,
                                        @Nullable String roomName,
                                        @NonNull String targetUserId,
                                        @NonNull OnHttpImListener<MeetingSessionVO> callback,
                                        @Nullable Runnable onFail) {
        requestSpeak(MeetingSpeakApi.KICK, meetingId, roomName, targetUserId, callback, onFail, true);
    }

    private static void requestSpeak(@NonNull String path,
                                     @Nullable String meetingId,
                                     @Nullable String roomName,
                                     @Nullable String targetUserId,
                                     @NonNull OnHttpImListener<MeetingSessionVO> callback,
                                     @Nullable Runnable onFail,
                                     boolean toastOnFail) {
        if (TextUtils.isEmpty(meetingId) && TextUtils.isEmpty(roomName)) {
            if (onFail != null) {
                onFail.run();
            }
            return;
        }
        UserInfoBean user = fromJson2(
                MMKVBroadUtils.Companion.getSqlStrData(LOGIN_USER_DATA, ""),
                UserInfoBean.class
        );
        MeetingSpeakApi api = new MeetingSpeakApi(path)
                .setMeetingId(meetingId)
                .setRoomName(roomName)
                .setTargetUserId(targetUserId);
        if (user != null) {
            api.setFromUsername(user.getName());
            api.setFromUserIcon(user.getUserImg());
        }
        EasyHttp.post(ApplicationLifecycle.getInstance())
                .api(api)
                .request(sessionListener("speak:" + path, callback, onFail, toastOnFail));
    }

    @NonNull
    private static OnHttpListener<BaseMsgResponse<MeetingSessionVO>> sessionListener(
            @NonNull String tag,
            @NonNull OnHttpImListener<MeetingSessionVO> callback,
            @Nullable Runnable onFail,
            boolean toastOnFail
    ) {
        return new OnHttpListener<BaseMsgResponse<MeetingSessionVO>>() {
            @Override
            public void onHttpSuccess(@NonNull BaseMsgResponse<MeetingSessionVO> result) {
                if (!result.isSuccess()) {
                    Log.e(TAG, tag + " 业务错误: errorCode=" + result.getErrorCode()
                            + " msg=" + result.getErrorMsg());
                    if (toastOnFail) {
                        String msg = result.getErrorMsg();
                        ToastBroadUtils.show(TextUtils.isEmpty(msg) ? "会议请求开小差了" : msg);
                    }
                    if (onFail != null) {
                        onFail.run();
                    }
                    return;
                }
                MeetingSessionVO content = resolveSession(result.getContent());
                if (content == null) {
                    Log.e(TAG, tag + " content 为空");
                    if (onFail != null) {
                        onFail.run();
                    }
                    return;
                }
                callback.onHttpImSuccess(content);
            }

            @Override
            public void onHttpFail(@NonNull Throwable throwable) {
                Log.e(TAG, tag + " 请求失败: " + throwable.getMessage());
                if (toastOnFail) {
                    String msg = throwable.getMessage();
                    ToastBroadUtils.show(TextUtils.isEmpty(msg) ? "会议请求失败" : msg);
                }
                if (onFail != null) {
                    onFail.run();
                }
            }
        };
    }

    @Nullable
    private static MeetingSessionVO resolveSession(@Nullable Object raw) {
        if (raw == null) {
            return null;
        }
        if (raw instanceof MeetingSessionVO) {
            return (MeetingSessionVO) raw;
        }
        return GSON.fromJson(GSON.toJson(raw), MeetingSessionVO.class);
    }

    @Nullable
    private static MeetingEndContent resolveEnd(@Nullable Object raw) {
        if (raw == null) {
            return null;
        }
        if (raw instanceof MeetingEndContent) {
            return (MeetingEndContent) raw;
        }
        return GSON.fromJson(GSON.toJson(raw), MeetingEndContent.class);
    }
}
