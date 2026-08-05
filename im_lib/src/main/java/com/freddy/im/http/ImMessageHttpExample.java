package com.freddy.im.http;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cboda.baselibrary.toast.ToastBroadUtils;
import com.freddy.im.db.ImLocalStore;
import com.freddy.im.http.api.CreateGroupApi;
import com.freddy.im.http.api.FileUploadApi;
import com.freddy.im.http.api.GetConversationListApi;
import com.freddy.im.http.api.GetGroupDetailApi;
import com.freddy.im.http.api.GetGroupMemberListApi;
import com.freddy.im.http.api.GetMessageListApi;
import com.freddy.im.http.api.GetSpecifyMembersApi;
import com.freddy.im.http.api.GetUserListApi;
import com.freddy.im.http.api.RemoveConversationApi;
import com.freddy.im.http.api.RepealMessageApi;
import com.freddy.im.http.api.ReportMessageReadApi;
import com.freddy.im.http.api.TranspondMessageApi;
import com.freddy.im.http.model.im.BaseMsgResponse;
import com.freddy.im.http.model.im.FileUploadParam;
import com.freddy.im.http.model.im.ConversationListContent;
import com.freddy.im.http.model.im.GroupMemberListContent;
import com.freddy.im.http.model.im.GroupUserVo;
import com.freddy.im.http.model.im.GroupVo;
import com.freddy.im.http.model.im.MessageListContent;
import com.freddy.im.http.model.im.ReportReadContent;
import com.freddy.im.http.model.im.SessionItem;
import com.freddy.im.http.model.im.TranspondMessageContent;
import com.freddy.im.http.model.im.TranspondMessageParam;
import com.freddy.im.http.model.im.UploadRes;
import com.freddy.im.http.model.im.UserInfoListRes;
import com.freddy.im.http.model.im.VideoBatchUploadResult;
import com.freddy.im.listener.OnHttpImListener;
import com.freddy.im.util.FileHashUtil;
import com.freddy.im.message.ImMessageManager;
import com.google.gson.reflect.TypeToken;
import com.hjq.http.EasyConfig;
import com.hjq.http.EasyHttp;
import com.hjq.http.lifecycle.ApplicationLifecycle;
import com.hjq.http.listener.OnHttpListener;

import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * IM HTTP 示例：
 * <ul>
 *   <li>POST {@code im/message/getList}</li>
 *   <li>POST {@code im/conversation/getList}（{@code startTime}、{@code anchor}；头由 {@link ImHttpConfig} 注入）</li>
 *   <li>GET {@code im/message/reportRead/{messageId}}</li>
 *   <li>GET {@code im/conversation/remove/{sessionId}}</li>
 *   <li>POST {@code im/group/create}</li>
   *   <li>POST {@code filehub/upload}（multipart 文件上传）</li>
   *   <li>POST {@code filehub/batchUpload}（视频：封面 + 视频批量上传）</li>
   * </ul>
 */
public final class ImMessageHttpExample {

    public static final String MY_TAG = "https_resp";
    private static final Gson GSON = ImGsonHelper.getGson();

    private ImMessageHttpExample() {
    }

    public static void requestGetMessageList(@NonNull String messageId,
                                             int anchor,
                                             int op, OnHttpImListener<MessageListContent> callBack) {
        EasyHttp.post(ApplicationLifecycle.getInstance())
                .api(new GetMessageListApi()
                        .setMessageId(messageId)
                        .setAnchor(anchor)
                        .setOp(op))
                .request(new OnHttpListener<BaseMsgResponse<MessageListContent>>() {

                    @Override
                    public void onHttpSuccess(@NonNull BaseMsgResponse<MessageListContent> result) {
                        if (!result.isSuccess()) {
                            Log.e(MY_TAG, "业务错误: " + result.getErrorCode() + " " + result.getErrorMsg());
                            return;
                        }
                        MessageListContent c = result.getContent();
                        if (c == null || c.getMsgList() == null || c.getMsgList().isEmpty()) {
                            Log.e(MY_TAG, "content 为空或无消息");
                            return;
                        }
                        callBack.onHttpImSuccess(result.getContent());

                    }

                    @Override
                    public void onHttpFail(@NonNull Throwable throwable) {
                        Log.e(MY_TAG, "请求失败: " + throwable.getMessage());
                    }
                });
    }

    /**
     * POST {@code im/conversation/getList}：拉取会话列表。
     */
    public static void requestGetConversationList(long startTime,
                                                  int anchor,
                                                  @NonNull OnHttpImListener<ConversationListContent> callback) {
        requestGetConversationList(startTime, anchor, callback, null);
    }

    /**
     * 同 {@link #(long, int, OnHttpImListener)}；
     * {@code onRequestTerminal} 仅在业务失败、content 为空或网络失败时调用，便于关闭 loading。
     */
    public static void requestGetConversationList(long startTime,
                                                  int anchor,
                                                  @NonNull OnHttpImListener<ConversationListContent> callback,
                                                  @Nullable Runnable onRequestTerminal) {
        EasyHttp.post(ApplicationLifecycle.getInstance())
                .api(new GetConversationListApi()
                        .setStartTime(startTime)
                        .setAnchor(anchor))
                .request(new OnHttpListener<BaseMsgResponse<ConversationListContent>>() {

                    @Override
                    public void onHttpSuccess(@NonNull BaseMsgResponse<ConversationListContent> result) {
                        if (!result.isSuccess()) {
                            Log.e(MY_TAG, "conversation 业务错误: errorCode=" + result.getErrorCode()
                                    + " msg=" + result.getErrorMsg());
                            if (onRequestTerminal != null) {
                                onRequestTerminal.run();
                            }
                            return;
                        }
                        ConversationListContent c = result.getContent();
                        if (c == null) {
                            Log.e(MY_TAG, "conversation content 为空");
                            if (onRequestTerminal != null) {
                                onRequestTerminal.run();
                            }
                            return;
                        }
                        finishConversationList(c, callback);
                    }

                    @Override
                    public void onHttpFail(@NonNull Throwable throwable) {
                        Log.e(MY_TAG, "conversation 请求失败: " + throwable.getMessage());
                        if (onRequestTerminal != null) {
                            onRequestTerminal.run();
                        }
                    }
                });
    }

    /**
     * 会话列表成功后：用本地已有最近消息摘要回填（不再串 {@code getLastMsg}），再回调并落库、同步未读。
     */
    private static void finishConversationList(@NonNull ConversationListContent content,
                                               @NonNull OnHttpImListener<ConversationListContent> callback) {
        SessionLastMsgMerge.applyFromLocal(content.getSessionList());
        dispatchConversationListReady(content, callback);
    }

    private static void dispatchConversationListReady(@NonNull ConversationListContent content,
                                                      @NonNull OnHttpImListener<ConversationListContent> callback) {
        List<SessionItem> sessionList = content.getSessionList();
        callback.onHttpImSuccess(content);
        if (sessionList != null && !sessionList.isEmpty()) {
            ImLocalStore.saveSessions(sessionList);
            ImMessageManager.getInstance().syncUnreadMessagesForSessions(sessionList);
        }
    }

    /**
     * GET {@code im/conversation/remove/{sessionId}}：移除会话。
     */
    public static void requestRemoveConversation(@NonNull String sessionId,
                                                 @Nullable OnHttpImListener<Boolean> callback,
                                                 @Nullable Runnable onFail) {
        if (TextUtils.isEmpty(sessionId)) {
            if (onFail != null) {
                onFail.run();
            }
            return;
        }
        EasyHttp.get(ApplicationLifecycle.getInstance())
                .api(new RemoveConversationApi().setSessionId(sessionId))
                .request(new OnHttpListener<BaseMsgResponse<Boolean>>() {

                    @Override
                    public void onHttpSuccess(@NonNull BaseMsgResponse<Boolean> result) {
                        if (!result.isSuccess()) {
                            Log.e(MY_TAG, "removeConversation 业务错误: errorCode="
                                    + result.getErrorCode() + " msg=" + result.getErrorMsg());
                            if (onFail != null) {
                                onFail.run();
                            }
                            return;
                        }
                        if (callback != null) {
                            callback.onHttpImSuccess(result.getContent());
                        }
                    }

                    @Override
                    public void onHttpFail(@NonNull Throwable throwable) {
                        Log.e(MY_TAG, "removeConversation 请求失败: " + throwable.getMessage());
                        if (onFail != null) {
                            onFail.run();
                        }
                    }
                });
    }

    /**
     * POST {@code im/group/create}：创建群组。
     *
     * @param conversation 会话类型，多人会话一般为 {@code 2}（{@link com.freddy.im.signal.ImSignalProtos.ConversationType#MULTI}）
     */
    public static void requestCreateGroup(@NonNull String name,
                                          @NonNull List<String> members,
                                          int conversation,
                                          @NonNull OnHttpImListener<GroupVo> callback,
                                          @Nullable Runnable onFail) {
        if (TextUtils.isEmpty(name) || members.isEmpty()) {
            if (onFail != null) {
                onFail.run();
            }
            return;
        }
        EasyHttp.post(ApplicationLifecycle.getInstance())
                .api(new CreateGroupApi()
                        .setName(name)
                        .setMembers(members)
                        .setConversation(conversation))
                .request(new OnHttpListener<BaseMsgResponse<GroupVo>>() {

                    @Override
                    public void onHttpSuccess(@NonNull BaseMsgResponse<GroupVo> result) {
                        if (!result.isSuccess()) {
                            Log.e(MY_TAG, "createGroup 业务错误: errorCode=" + result.getErrorCode()
                                    + " msg=" + result.getErrorMsg());
                            if (onFail != null) {
                                onFail.run();
                            }
                            return;
                        }
                        GroupVo group = result.getContent();
                        if (group == null) {
                            Log.e(MY_TAG, "createGroup content 为空");
                            if (onFail != null) {
                                onFail.run();
                            }
                            return;
                        }
                        callback.onHttpImSuccess(group);
                    }

                    @Override
                    public void onHttpFail(@NonNull Throwable throwable) {
                        Log.e(MY_TAG, "createGroup 请求失败: " + throwable.getMessage());
                        if (onFail != null) {
                            onFail.run();
                        }
                    }
                });
    }

    /**
     * GET {@code im/group/detail/{groupId}}：获取群详情（3.6）。
     */
    public static void requestGetGroupDetail(@NonNull String groupId,
                                             @NonNull OnHttpImListener<GroupVo> callback,
                                             @Nullable Runnable onFail) {
        if (TextUtils.isEmpty(groupId)) {
            if (onFail != null) {
                onFail.run();
            }
            return;
        }
        EasyHttp.get(ApplicationLifecycle.getInstance())
                .api(new GetGroupDetailApi().setGroupId(groupId))
                .request(new OnHttpListener<BaseMsgResponse<GroupVo>>() {

                    @Override
                    public void onHttpSuccess(@NonNull BaseMsgResponse<GroupVo> result) {
                        if (!result.isSuccess()) {
                            Log.e(MY_TAG, "getGroupDetail 业务错误: errorCode=" + result.getErrorCode()
                                    + " msg=" + result.getErrorMsg());
                            if (onFail != null) {
                                onFail.run();
                            }
                            return;
                        }
                        GroupVo group = result.getContent();
                        if (group == null || TextUtils.isEmpty(group.getId())) {
                            Log.e(MY_TAG, "getGroupDetail content 无效");
                            if (onFail != null) {
                                onFail.run();
                            }
                            return;
                        }
                        callback.onHttpImSuccess(group);
                    }

                    @Override
                    public void onHttpFail(@NonNull Throwable throwable) {
                        Log.e(MY_TAG, "getGroupDetail 请求失败: " + throwable.getMessage());
                        if (onFail != null) {
                            onFail.run();
                        }
                    }
                });
    }

    /**
     * POST {@code /im/group/memberList}：分页获取群成员（文档 §3）。
     *
     * @param anchor 首页传 {@code null}（不传 anchor 字段）；后续页传上一页响应的 {@code anchor}
     * @param count  每页条数，默认 {@link GroupMemberListContent#DEFAULT_PAGE_SIZE}
     */
    public static void requestGetGroupMemberList(@NonNull String groupId,
                                                 @Nullable Integer anchor,
                                                 int count,
                                                 @NonNull OnHttpImListener<GroupMemberListContent> callback,
                                                 @Nullable Runnable onFail) {
        if (TextUtils.isEmpty(groupId)) {
            if (onFail != null) {
                onFail.run();
            }
            return;
        }
        GetGroupMemberListApi api = new GetGroupMemberListApi()
                .setGroupId(groupId)
                .setCount(count);
        if (anchor != null) {
            api.setAnchor(anchor);
        }
        EasyHttp.post(ApplicationLifecycle.getInstance())
                .api(api)
                .request(new OnHttpListener<BaseMsgResponse<GroupMemberListContent>>() {

                    @Override
                    public void onHttpSuccess(@NonNull BaseMsgResponse<GroupMemberListContent> result) {
                        if (!result.isSuccess()) {
                            Log.e(MY_TAG, "memberList 业务错误: errorCode=" + result.getErrorCode()
                                    + " msg=" + result.getErrorMsg());
                            if (onFail != null) {
                                onFail.run();
                            }
                            return;
                        }
                        GroupMemberListContent content = result.getContent();
                        if (content == null) {
                            Log.e(MY_TAG, "memberList content 为空");
                            if (onFail != null) {
                                onFail.run();
                            }
                            return;
                        }
                        callback.onHttpImSuccess(content);
                    }

                    @Override
                    public void onHttpFail(@NonNull Throwable throwable) {
                        Log.e(MY_TAG, "memberList 请求失败: " + throwable.getMessage());
                        if (onFail != null) {
                            onFail.run();
                        }
                    }
                });
    }

    /**
     * POST {@code /im/group/getSpecifyMembers}：获取指定群成员（文档 §12）。
     */
    public static void requestGetSpecifyMembers(@NonNull String groupId,
                                                @NonNull List<String> memberUserIds,
                                                @NonNull OnHttpImListener<List<GroupUserVo>> callback,
                                                @Nullable Runnable onFail) {
        if (TextUtils.isEmpty(groupId) || memberUserIds.isEmpty()) {
            if (onFail != null) {
                onFail.run();
            }
            return;
        }
        EasyHttp.post(ApplicationLifecycle.getInstance())
                .api(new GetSpecifyMembersApi()
                        .setGroupId(groupId)
                        .setMembers(memberUserIds))
                .request(new OnHttpListener<BaseMsgResponse<List<GroupUserVo>>>() {

                    @Override
                    public void onHttpSuccess(@NonNull BaseMsgResponse<List<GroupUserVo>> result) {
                        if (!result.isSuccess()) {
                            Log.e(MY_TAG, "getSpecifyMembers 业务错误: errorCode="
                                    + result.getErrorCode() + " msg=" + result.getErrorMsg());
                            if (onFail != null) {
                                onFail.run();
                            }
                            return;
                        }
                        List<GroupUserVo> list = result.getContent();
                        callback.onHttpImSuccess(list != null ? list : Collections.emptyList());
                    }

                    @Override
                    public void onHttpFail(@NonNull Throwable throwable) {
                        Log.e(MY_TAG, "getSpecifyMembers 请求失败: " + throwable.getMessage());
                        if (onFail != null) {
                            onFail.run();
                        }
                    }
                });
    }

    /**
     * POST {@code im/user/getUserList}：获取用户列表（5.2）。
     */
    public static void requestGetUserList(@Nullable String keyword,
                                          int pageNo,
                                          int pageSize,
                                          @NonNull OnHttpImListener<BaseMsgResponse<UserInfoListRes>> callback,
                                          @Nullable Runnable onFail) {
        GetUserListApi api = new GetUserListApi()
                .setPageNo(pageNo)
                .setPageSize(pageSize);
        if (!TextUtils.isEmpty(keyword)) {
            api.setKeyword(keyword.trim());
        }
        EasyHttp.post(ApplicationLifecycle.getInstance())
                .api(api)
                .request(new OnHttpListener<BaseMsgResponse<UserInfoListRes>>() {

                    @Override
                    public void onHttpSuccess(@NonNull BaseMsgResponse<UserInfoListRes> result) {
                        if (!result.isSuccess()) {
                            Log.e(MY_TAG, "getUserList 业务错误: errorCode=" + result.getErrorCode()
                                    + " msg=" + result.getErrorMsg());
                        }
                        callback.onHttpImSuccess(result);
                    }

                    @Override
                    public void onHttpFail(@NonNull Throwable throwable) {
                        Log.e(MY_TAG, "getUserList 请求失败: " + throwable.getMessage());
                        if (onFail != null) {
                            onFail.run();
                        }
                    }
                });
    }

    /**
     * POST {@code filehub/upload}：上传文件，返回 CDN 地址（§7）。
     *
     * @param groupId 群聊时传群 ID（{@code sessionId}），单聊可 null
     */
    public static void uploadFile(@NonNull File file,
                                  @Nullable String groupId,
                                  @NonNull OnHttpImListener<UploadRes> callback,
                                  @Nullable Runnable onFail) {
        if (!file.isFile()) {
            if (onFail != null) {
                onFail.run();
            }
            return;
        }
        FileUploadParam param = new FileUploadParam();
        if (!TextUtils.isEmpty(groupId)) {
            param.setGroupId(groupId);
        }
        String hash = FileHashUtil.md5Hex(file);
        if (!TextUtils.isEmpty(hash)) {
            param.setFileHash(hash);
        }
        String paramJson = GSON.toJson(param);
        RequestBody paramBody = RequestBody.create(
                paramJson, MediaType.parse("application/json; charset=utf-8"));

        EasyHttp.post(ApplicationLifecycle.getInstance())
                .api(new FileUploadApi().setFile(file).setParam(paramBody))
                .request(new OnHttpListener<BaseMsgResponse<UploadRes>>() {

                    @Override
                    public void onHttpSuccess(@NonNull BaseMsgResponse<UploadRes> result) {
                        if (!result.isSuccess()) {
                            Log.e(MY_TAG, "filehub/upload 业务错误: errorCode=" + result.getErrorCode()
                                    + " msg=" + result.getErrorMsg());
                            if (onFail != null) {
                                onFail.run();
                            }
                            return;
                        }
                        UploadRes content = result.getContent();
                        if (content == null || TextUtils.isEmpty(content.getUrl())) {
                            Log.e(MY_TAG, "filehub/upload content/url 为空");
                            if (onFail != null) {
                                onFail.run();
                            }
                            return;
                        }
                        callback.onHttpImSuccess(content);
                    }

                    @Override
                    public void onHttpFail(@NonNull Throwable throwable) {
                        Log.e(MY_TAG, "filehub/upload 请求失败: " + throwable.getMessage());
                        if (onFail != null) {
                            onFail.run();
                        }
                    }
                });
    }

    /**
     * POST {@code filehub/batchUpload}：视频消息上传，{@code files[0]} 封面、{@code files[1]} 视频（§7.2.0）。
     */
    public static void uploadVideoBatch(@NonNull File coverFile,
                                        @NonNull File videoFile,
                                        @Nullable String groupId,
                                        @NonNull OnHttpImListener<VideoBatchUploadResult> callback,
                                        @Nullable Runnable onFail) {
        if (!coverFile.isFile() || !videoFile.isFile()) {
            if (onFail != null) {
                onFail.run();
            }
            return;
        }
        if (coverFile.getAbsolutePath().equals(videoFile.getAbsolutePath())) {
            Log.e(MY_TAG, "filehub/batchUpload 封面与视频为同一路径");
            if (onFail != null) {
                onFail.run();
            }
            return;
        }
        // 批量上传勿传 fileHash：param 里只有一个 hash，填视频 MD5 时服务端会按该 hash 去重，
        // 导致 content[0]、content[1] 都返回已存在的视频地址（封面 slot 也被当成同一视频）。
        FileUploadParam param = new FileUploadParam();
        if (!TextUtils.isEmpty(groupId)) {
            param.setGroupId(groupId);
        }
        String paramJson = GSON.toJson(param);
        RequestBody paramBody = RequestBody.create(
                paramJson, MediaType.parse("application/json; charset=utf-8"));
        MultipartBody multipartBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("files", "cover.jpg",
                        RequestBody.create(coverFile, MediaType.parse("image/jpeg")))
                .addFormDataPart("files", videoFile.getName(),
                        RequestBody.create(videoFile, MediaType.parse(guessVideoMimeType(videoFile))))
                .addFormDataPart("param", null, paramBody)
                .build();

        Request.Builder requestBuilder = new Request.Builder()
                .url(new RequestServer().getHost() + "filehub/batchUpload")
                .post(multipartBody);
        applyGlobalHeaders(requestBuilder);

        OkHttpClient client = EasyConfig.getInstance().getClient();
        client.newCall(requestBuilder.build()).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(MY_TAG, "filehub/batchUpload 请求失败: " + e.getMessage());
                if (onFail != null) {
                    onFail.run();
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                try (Response resp = response) {
                    if (!resp.isSuccessful() || resp.body() == null) {
                        Log.e(MY_TAG, "filehub/batchUpload HTTP " + resp.code());
                        if (onFail != null) {
                            onFail.run();
                        }
                        return;
                    }
                    String text = resp.body().string();
                    Log.d("zhanglong","批量文件上传成功--》"+text);
                    Type listType = TypeToken.getParameterized(List.class, UploadRes.class).getType();
                    Type responseType = TypeToken.getParameterized(BaseMsgResponse.class, listType).getType();
                    BaseMsgResponse<List<UploadRes>> result = GSON.fromJson(text, responseType);
                    if (result == null || !result.isSuccess()) {
                        if (result != null) {
                            ImAuthInvalidHandler.handleIfNeeded(result);
                        }
                        Log.e(MY_TAG, "filehub/batchUpload 业务错误");
                        if (onFail != null) {
                            onFail.run();
                        }
                        return;
                    }
                    List<UploadRes> uploads = result.getContent();
                    if (uploads == null || uploads.size() < 2) {
                        Log.e(MY_TAG, "filehub/batchUpload content 数量不足");
                        if (onFail != null) {
                            onFail.run();
                        }
                        return;
                    }
                    String coverUrl = pickCoverUrlFromBatch(uploads);
                    String videoUrl = pickVideoUrlFromBatch(uploads);
                    if (TextUtils.isEmpty(videoUrl)) {
                        Log.e(MY_TAG, "filehub/batchUpload 视频 url 为空");
                        if (onFail != null) {
                            onFail.run();
                        }
                        return;
                    }
                    deliverVideoBatchResult(coverFile, groupId, coverUrl, videoUrl, callback, onFail);
                } catch (Exception e) {
                    Log.e(MY_TAG, "filehub/batchUpload 解析失败: " + e.getMessage());
                    if (onFail != null) {
                        onFail.run();
                    }
                }
            }
        });
    }

    /**
     * 从批量结果中选取封面 URL：优先非视频扩展名的项（文档约定 files[0] 为封面）。
     */
    @Nullable
    private static String pickCoverUrlFromBatch(@NonNull List<UploadRes> uploads) {
        for (UploadRes item : uploads) {
            if (item == null) {
                continue;
            }
            String url = item.getUrl();
            String thumbImage = item.getThumbImage();
            if (!TextUtils.isEmpty(url) && !isVideoResourceUrl(url)) {
                if (TextUtils.isEmpty(thumbImage)){
                    return url;
                }else {
                    return thumbImage;
                }

            }
        }
        return uploads.get(0) != null ? uploads.get(0).getUrl() : null;
    }

    @NonNull
    private static String pickVideoUrlFromBatch(@NonNull List<UploadRes> uploads) {
        for (int i = uploads.size() - 1; i >= 0; i--) {
            UploadRes item = uploads.get(i);
            if (item == null) {
                continue;
            }
            String url = item.getUrl();
            if (!TextUtils.isEmpty(url) && isVideoResourceUrl(url)) {
                return url;
            }
        }
        if (uploads.size() > 1 && uploads.get(1) != null && !TextUtils.isEmpty(uploads.get(1).getUrl())) {
            return uploads.get(1).getUrl();
        }
        return uploads.get(0) != null && uploads.get(0).getUrl() != null
                ? uploads.get(0).getUrl() : "";
    }

    private static void deliverVideoBatchResult(@NonNull File coverFile,
                                                @Nullable String groupId,
                                                @Nullable String coverUrl,
                                                @NonNull String videoUrl,
                                                @NonNull OnHttpImListener<VideoBatchUploadResult> callback,
                                                @Nullable Runnable onFail) {
        if (!TextUtils.isEmpty(coverUrl) && !isVideoResourceUrl(coverUrl)) {
            callback.onHttpImSuccess(new VideoBatchUploadResult(coverUrl, videoUrl));
            return;
        }
        Log.w(MY_TAG, "filehub/batchUpload 封面位返回了视频地址，改用单文件上传补传封面");
        uploadFile(coverFile, groupId, new OnHttpImListener<UploadRes>() {
            @Override
            public void onHttpImSuccess(@NonNull UploadRes coverUpload) {
                String fixedCover = coverUpload.getUrl();
                callback.onHttpImSuccess(new VideoBatchUploadResult(
                        TextUtils.isEmpty(fixedCover) ? coverUrl : fixedCover, videoUrl));
            }
        }, onFail);
    }

    private static boolean isVideoResourceUrl(@Nullable String url) {
        if (TextUtils.isEmpty(url)) {
            return false;
        }
        String path = url.toLowerCase(Locale.US);
        int query = path.indexOf('?');
        if (query >= 0) {
            path = path.substring(0, query);
        }
        return path.endsWith(".mp4")
                || path.endsWith(".mov")
                || path.endsWith(".3gp")
                || path.endsWith(".m4v")
                || path.endsWith(".webm");
    }

    private static void applyGlobalHeaders(@NonNull Request.Builder builder) {
        Map<String, String> headers = EasyConfig.getInstance().getHeaders();
        if (headers == null || headers.isEmpty()) {
            return;
        }
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null) {
                builder.addHeader(entry.getKey(), entry.getValue());
            }
        }
    }

    @NonNull
    private static String guessVideoMimeType(@NonNull File file) {
        String name = file.getName().toLowerCase();
        if (name.endsWith(".mov")) {
            return "video/quicktime";
        }
        if (name.endsWith(".3gp")) {
            return "video/3gpp";
        }
        return "video/mp4";
    }

    /**
     * GET {@code api/im/message/repeal/{messageId}}：撤回消息（§4.4）。
     */
    public static void requestRepealMessage(@NonNull String messageId,
                                          @Nullable OnHttpImListener<Boolean> callback,
                                          @Nullable Runnable onFail) {
        if (TextUtils.isEmpty(messageId)) {
            if (onFail != null) {
                onFail.run();
            }
            return;
        }
        EasyHttp.get(ApplicationLifecycle.getInstance())
                .api(new RepealMessageApi().setMessageId(messageId))
                .request(new OnHttpListener<BaseMsgResponse<Boolean>>() {
                    @Override
                    public void onHttpSuccess(@NonNull BaseMsgResponse<Boolean> result) {
                        if (!result.isSuccess()) {
                            Log.e(MY_TAG, "repeal 业务错误: errorCode=" + result.getErrorCode()
                                    + " msg=" + result.getErrorMsg());
                            if (onFail != null) {
                                ToastBroadUtils.show(result.getErrorMsg());
//                                onFail.run();
                            }
                            return;
                        }
                        if (callback != null) {
                            callback.onHttpImSuccess(Boolean.TRUE);
                        }
                    }

                    @Override
                    public void onHttpFail(@NonNull Throwable throwable) {
                        Log.e(MY_TAG, "repeal 请求失败: " + throwable.getMessage());
                        if (onFail != null) {
                            onFail.run();
                        }
                    }
                });
    }

    /**
     * POST {@code api/im/message/transpond}：转发消息（§4.6）。
     */
    public static void requestTranspondMessage(@NonNull TranspondMessageParam param,
                                               @Nullable OnHttpImListener<TranspondMessageContent> callback,
                                               @Nullable Runnable onFail) {
        if (param == null
                || param.getMsgIdlist() == null
                || param.getMsgIdlist().isEmpty()
                || TextUtils.isEmpty(param.getTo())) {
            if (onFail != null) {
                onFail.run();
            }
            return;
        }
        EasyHttp.post(ApplicationLifecycle.getInstance())
                .api(TranspondMessageApi.from(param))
                .request(new OnHttpListener<BaseMsgResponse<TranspondMessageContent>>() {
                    @Override
                    public void onHttpSuccess(@NonNull BaseMsgResponse<TranspondMessageContent> result) {
                        if (!result.isSuccess()) {
                            Log.e(MY_TAG, "transpond 业务错误: errorCode=" + result.getErrorCode()
                                    + " msg=" + result.getErrorMsg());
                            if (onFail != null) {
                                onFail.run();
                            }
                            return;
                        }
                        TranspondMessageContent content = result.getContent();
                        if (content == null || TextUtils.isEmpty(content.getMessageId())) {
                            Log.e(MY_TAG, "transpond content 为空或缺少 messageId");
                            if (onFail != null) {
                                onFail.run();
                            }
                            return;
                        }
                        if (callback != null) {
                            callback.onHttpImSuccess(content);
                        }
                    }

                    @Override
                    public void onHttpFail(@NonNull Throwable throwable) {
                        Log.e(MY_TAG, "transpond 请求失败: " + throwable.getMessage());
                        if (onFail != null) {
                            onFail.run();
                        }
                    }
                });
    }

    /**
     * GET {@code im/message/reportRead/{messageId}}：上报消息已读。
     */
    public static void requestReportMessageRead(@NonNull String messageId,
                                                @Nullable OnHttpImListener<ReportReadContent> callback) {
        EasyHttp.get(ApplicationLifecycle.getInstance())
                .api(new ReportMessageReadApi().setMessageId(messageId))
                .request(new OnHttpListener<BaseMsgResponse<ReportReadContent>>() {

                    @Override
                    public void onHttpSuccess(@NonNull BaseMsgResponse<ReportReadContent> result) {
                        if (!result.isSuccess()) {
                            Log.e(MY_TAG, "reportRead 业务错误: errorCode=" + result.getErrorCode()
                                    + " msg=" + result.getErrorMsg());
                            return;
                        }
                        ReportReadContent content = result.getContent();
                        if (content == null) {
                            Log.e(MY_TAG, "reportRead content 为空");
                            return;
                        }
                        if (callback != null) {
                            callback.onHttpImSuccess(content);
                        }
                    }

                    @Override
                    public void onHttpFail(@NonNull Throwable throwable) {
                        Log.e(MY_TAG, "reportRead 请求失败: " + throwable.getMessage());
                    }
                });
    }

}
