package com.freddy.im.http;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.freddy.im.http.api.AddGroupMembersApi;
import com.freddy.im.http.api.ApplyInGroupApi;
import com.freddy.im.http.api.ApproveInGroupApi;
import com.freddy.im.http.api.CreateGroupApi;
import com.freddy.im.http.api.DisbandGroupApi;
import com.freddy.im.http.api.GetAllGroupMembersApi;
import com.freddy.im.http.api.GetGroupDetailApi;
import com.freddy.im.http.api.GetGroupListApi;
import com.freddy.im.http.api.GetGroupMemberListApi;
import com.freddy.im.http.api.GetGroupNoticeApi;
import com.freddy.im.http.api.GetGroupQrCodeApi;
import com.freddy.im.http.api.GetSpecifyMembersApi;
import com.freddy.im.http.api.GroupFriendsApi;
import com.freddy.im.http.api.InviteInGroupApi;
import com.freddy.im.http.api.KickOutGroupApi;
import com.freddy.im.http.api.LeaveGroupApi;
import com.freddy.im.http.api.ModifyGroupApi;
import com.freddy.im.http.api.SearchGroupAsstApi;
import com.freddy.im.http.api.TransferGroupApi;
import com.freddy.im.http.api.UpdateGroupNoticeApi;
import com.freddy.im.http.model.im.BaseMsgResponse;
import com.freddy.im.http.model.im.GroupApplyInfoRes;
import com.freddy.im.http.model.im.GroupFriendBean;
import com.freddy.im.http.model.im.GroupListResp;
import com.freddy.im.http.model.im.GroupMemberListContent;
import com.freddy.im.http.model.im.GroupNoticeRes;
import com.freddy.im.http.model.im.GroupQrCodeRes;
import com.freddy.im.http.model.im.GroupUserVo;
import com.freddy.im.http.model.im.GroupVo;
import com.freddy.im.http.model.im.InviteMemberItem;
import com.freddy.im.listener.OnHttpImListener;
import com.freddy.im.util.ImUtilKt;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hjq.http.EasyHttp;
import com.hjq.http.lifecycle.ApplicationLifecycle;
import com.hjq.http.listener.OnHttpListener;

import java.util.Collections;
import java.util.List;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * 群组 HTTP（文档 §3），内部统一解析 {@link BaseMsgResponse}。
 */
public final class ImGroupHttpExample {

    public static final String TAG = "group_http";

    private static final Gson GSON = new Gson();

    private ImGroupHttpExample() {
    }

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
                .request(postListener("createGroup", callback, onFail, true, GroupVo.class));
    }

    public static void requestGetGroupList(int anchor,
                                           @NonNull OnHttpImListener<GroupListResp> callback,
                                           @Nullable Runnable onFail) {
        EasyHttp.post(ApplicationLifecycle.getInstance())
                .api(new GetGroupListApi().setAnchor(anchor).setIsContainSingle(0))
                .request(postListener("getGroupList", callback, onFail, true, GroupListResp.class));
    }

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
                .request(getListener("getGroupDetail", callback, onFail, true, GroupVo.class));
    }

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
        GetGroupMemberListApi api = new GetGroupMemberListApi().setGroupId(groupId).setCount(count);
        if (anchor != null) {
            api.setAnchor(anchor);
        }

//        Log.d("zhanglong", "getResumeActivity==" + ImUtilKt.getResumeActivity());
        EasyHttp.post(ImUtilKt.getResumeActivity() != null ? ImUtilKt.getResumeActivity() : ApplicationLifecycle.getInstance())
                .api(api)
                .request(postListener("memberList", callback, onFail, true, GroupMemberListContent.class));
    }

    public static void requestGetAllMembers(@NonNull String groupId,
                                            @NonNull OnHttpImListener<List<GroupUserVo>> callback,
                                            @Nullable Runnable onFail) {
        if (TextUtils.isEmpty(groupId)) {
            if (onFail != null) {
                onFail.run();
            }
            return;
        }
        EasyHttp.get(ApplicationLifecycle.getInstance())
                .api(new GetAllGroupMembersApi().setGroupId(groupId))
                .request(getListener("getAllMembers", callback, onFail, false, listOf(GroupUserVo.class)));
    }

    public static void requestAddMembers(@NonNull String groupId,
                                         @NonNull List<String> members,
                                         @NonNull OnHttpImListener<GroupVo> callback,
                                         @Nullable Runnable onFail) {
        if (TextUtils.isEmpty(groupId) || members.isEmpty()) {
            if (onFail != null) {
                onFail.run();
            }
            return;
        }
        EasyHttp.post(ApplicationLifecycle.getInstance())
                .api(new AddGroupMembersApi().setGroupId(groupId).setMembers(members))
                .request(postListener("addMembers", callback, onFail, true, GroupVo.class));
    }

    public static void requestGroupFriends(@NonNull String groupId,
                                           @Nullable String key,
                                           @NonNull OnHttpImListener<List<GroupFriendBean>> callback,
                                           @Nullable Runnable onFail) {
        if (TextUtils.isEmpty(groupId)) {
            if (onFail != null) {
                onFail.run();
            }
            return;
        }
        EasyHttp.post(ApplicationLifecycle.getInstance())
                .api(new GroupFriendsApi().setGroupId(groupId).setId(groupId).setName(key).setKey(key))
                .request(postListener("groupFriendList", callback, onFail, true, listOf(GroupFriendBean.class)));
    }


    public static void requestModifyGroup(@NonNull String groupId,
                                          @Nullable String name,
                                          @Nullable String icon,
                                          @NonNull OnHttpImListener<GroupVo> callback,
                                          @Nullable Runnable onFail) {
        if (TextUtils.isEmpty(groupId)) {
            if (onFail != null) {
                onFail.run();
            }
            return;
        }
        ModifyGroupApi api = new ModifyGroupApi().setGroupId(groupId);
        if (!TextUtils.isEmpty(name)) {
            api.setName(name);
        }
        if (!TextUtils.isEmpty(icon)) {
            api.setIcon(icon);
        }
        EasyHttp.post(ApplicationLifecycle.getInstance())
                .api(api)
                .request(postListener("modifyGroup", callback, onFail, true, GroupVo.class));
    }

    public static void requestLeaveGroup(@NonNull String groupId,
                                         @NonNull OnHttpImListener<String> callback,
                                         @Nullable Runnable onFail) {
        if (TextUtils.isEmpty(groupId)) {
            if (onFail != null) {
                onFail.run();
            }
            return;
        }
        EasyHttp.post(ApplicationLifecycle.getInstance())
                .api(new LeaveGroupApi().setGroupId(groupId))
                .request(postListener("leaveGroup", callback, onFail, false, String.class));
    }

    public static void requestKickOut(@NonNull String groupId,
                                      @NonNull List<String> members,
                                      @NonNull OnHttpImListener<String> callback,
                                      @Nullable Runnable onFail) {
        if (TextUtils.isEmpty(groupId) || members.isEmpty()) {
            if (onFail != null) {
                onFail.run();
            }
            return;
        }
        EasyHttp.post(ApplicationLifecycle.getInstance())
                .api(new KickOutGroupApi().setGroupId(groupId).setMembers(members))
                .request(postListener("kickOut", callback, onFail, false, String.class));
    }

    public static void requestDisbandGroup(@NonNull String groupId,
                                           @NonNull OnHttpImListener<BaseMsgResponse<Void>> callback,
                                           @Nullable Runnable onFail) {
        if (TextUtils.isEmpty(groupId)) {
            if (onFail != null) {
                onFail.run();
            }
            return;
        }
        EasyHttp.post(ApplicationLifecycle.getInstance())
                .api(new DisbandGroupApi().setGroupId(groupId))
                .request(new OnHttpListener<BaseMsgResponse<Void>>() {
                    @Override
                    public void onHttpSuccess(@NonNull BaseMsgResponse<Void> result) {
                        if (!result.isSuccess()) {
                            logBizError("disbandGroup", result);
                            if (onFail != null) {
                                onFail.run();
                            }
                            return;
                        }
                        callback.onHttpImSuccess(result);
                    }

                    @Override
                    public void onHttpFail(@NonNull Throwable throwable) {
                        logHttpFail("disbandGroup", throwable);
                        if (onFail != null) {
                            onFail.run();
                        }
                    }
                });
    }

    public static void requestTransferOwner(@NonNull String groupId,
                                            @NonNull String newOwnerId,
                                            @NonNull OnHttpImListener<BaseMsgResponse<Void>> callback,
                                            @Nullable Runnable onFail) {
        if (TextUtils.isEmpty(groupId) || TextUtils.isEmpty(newOwnerId)) {
            if (onFail != null) {
                onFail.run();
            }
            return;
        }
        EasyHttp.post(ApplicationLifecycle.getInstance())
                .api(new TransferGroupApi().setGroupId(groupId).setNewOwer(newOwnerId))
                .request(new OnHttpListener<BaseMsgResponse<Void>>() {
                    @Override
                    public void onHttpSuccess(@NonNull BaseMsgResponse<Void> result) {
                        if (!result.isSuccess()) {
                            logBizError("transferOwner", result);
                            if (onFail != null) {
                                onFail.run();
                            }
                            return;
                        }
                        callback.onHttpImSuccess(result);
                    }

                    @Override
                    public void onHttpFail(@NonNull Throwable throwable) {
                        logHttpFail("transferOwner", throwable);
                        if (onFail != null) {
                            onFail.run();
                        }
                    }
                });
    }

    public static void requestGetGroupQrCode(@NonNull String groupId,
                                             @NonNull OnHttpImListener<GroupQrCodeRes> callback,
                                             @Nullable Runnable onFail) {
        if (TextUtils.isEmpty(groupId)) {
            if (onFail != null) {
                onFail.run();
            }
            return;
        }
        EasyHttp.get(ApplicationLifecycle.getInstance())
                .api(new GetGroupQrCodeApi().setGroupId(groupId))
                .request(getListener("getGroupQrCode", callback, onFail, true, GroupQrCodeRes.class));
    }

    public static void requestGetGroupNotice(@NonNull String groupId,
                                             @NonNull OnHttpImListener<GroupNoticeRes> callback,
                                             @Nullable Runnable onFail) {
        if (TextUtils.isEmpty(groupId)) {
            if (onFail != null) {
                onFail.run();
            }
            return;
        }
        EasyHttp.get(ApplicationLifecycle.getInstance())
                .api(new GetGroupNoticeApi().setGroupId(groupId))
                .request(getListener("getGroupNotice", callback, onFail, false, GroupNoticeRes.class));
    }

    public static void requestUpdateGroupNotice(@NonNull String groupId,
                                                @NonNull String content,
                                                @NonNull OnHttpImListener<BaseMsgResponse<Void>> callback,
                                                @Nullable Runnable onFail) {
        if (TextUtils.isEmpty(groupId)) {
            if (onFail != null) {
                onFail.run();
            }
            return;
        }
        EasyHttp.post(ApplicationLifecycle.getInstance())
                .api(new UpdateGroupNoticeApi().setGroupId(groupId).setContent(content))
                .request(new OnHttpListener<BaseMsgResponse<Void>>() {
                    @Override
                    public void onHttpSuccess(@NonNull BaseMsgResponse<Void> result) {
                        if (!result.isSuccess()) {
                            logBizError("updateGroupNotice", result);
                            if (onFail != null) {
                                onFail.run();
                            }
                            return;
                        }
                        callback.onHttpImSuccess(result);
                    }

                    @Override
                    public void onHttpFail(@NonNull Throwable throwable) {
                        logHttpFail("updateGroupNotice", throwable);
                        if (onFail != null) {
                            onFail.run();
                        }
                    }
                });
    }

    public static void requestSearchGroupAsst(@NonNull OnHttpImListener<List<GroupApplyInfoRes>> callback,
                                              @Nullable Runnable onFail) {
        EasyHttp.post(ApplicationLifecycle.getInstance())
                .api(new SearchGroupAsstApi())
                .request(postListener("searchGroupAsst", callback, onFail, false, listOf(GroupApplyInfoRes.class)));
    }

    public static void requestApproveInGroup(@NonNull String applyId,
                                             @NonNull String approveStatus,
                                             @NonNull OnHttpImListener<Object> callback,
                                             @Nullable Runnable onFail) {
        if (TextUtils.isEmpty(applyId)) {
            if (onFail != null) {
                onFail.run();
            }
            return;
        }
        EasyHttp.post(ApplicationLifecycle.getInstance())
                .api(new ApproveInGroupApi().setApplyId(applyId).setApproveStatus(approveStatus))
                .request(postListener("approveInGroup", callback, onFail, false, Object.class));
    }

    public static void requestInviteInGroup(@NonNull String groupId,
                                            @NonNull List<InviteMemberItem> inviteList,
                                            @NonNull OnHttpImListener<Object> callback,
                                            @Nullable Runnable onFail) {
        if (TextUtils.isEmpty(groupId) || inviteList.isEmpty()) {
            if (onFail != null) {
                onFail.run();
            }
            return;
        }
        EasyHttp.post(ApplicationLifecycle.getInstance())
                .api(new InviteInGroupApi().setGroupId(groupId).setInviteMemberList(inviteList))
                .request(postListener("inviteInGroup", callback, onFail, false, Object.class));
    }

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
                .api(new GetSpecifyMembersApi().setGroupId(groupId).setMembers(memberUserIds))
                .request(postListener("getSpecifyMembers", callback, onFail, false, listOf(GroupUserVo.class)));
    }

    private static <T> OnHttpListener<BaseMsgResponse<T>> postListener(
            @NonNull String tag,
            @NonNull OnHttpImListener<T> callback,
            @Nullable Runnable onFail,
            boolean requireContent,
            @NonNull Type contentType) {
        return new OnHttpListener<BaseMsgResponse<T>>() {
            @Override
            public void onHttpSuccess(@NonNull BaseMsgResponse<T> result) {
                if (!result.isSuccess()) {
                    logBizError(tag, result);
                    if (onFail != null) {
                        onFail.run();
                    }
                    return;
                }
                T content = resolveContent(result.getContent(), contentType);
                if (requireContent && content == null) {
                    Log.e(TAG, tag + " content 为空");
                    if (onFail != null) {
                        onFail.run();
                    }
                    return;
                }
                if (content instanceof List) {
                    List<?> list = (List<?>) content;
                    if (isListContentType(contentType)) {
                        callback.onHttpImSuccess((T) list);
                        return;
                    }
                    if (list.isEmpty()) {
                        deliverEmptyContent(callback, contentType, onFail);
                        return;
                    }
                    T single = resolveContent(list.get(0), contentType);
                    if (single != null) {
                        callback.onHttpImSuccess(single);
                    } else {
                        deliverEmptyContent(callback, contentType, onFail);
                    }
                    return;
                }
                if (content != null) {
                    callback.onHttpImSuccess(content);
                } else {
                    deliverEmptyContent(callback, contentType, onFail);
                }
            }

            @Override
            public void onHttpFail(@NonNull Throwable throwable) {
                logHttpFail(tag, throwable);
                if (onFail != null) {
                    onFail.run();
                }
            }
        };
    }

    @SuppressWarnings("unchecked")
    @Nullable
    private static <T> T resolveContent(@Nullable Object raw, @NonNull Type contentType) {
        if (raw == null) {
            return null;
        }
        if (contentType instanceof Class && ((Class<?>) contentType).isInstance(raw)) {
            return (T) raw;
        }
        return GSON.fromJson(GSON.toJson(raw), contentType);
    }

    private static Type listOf(@NonNull Class<?> itemClass) {
        return TypeToken.getParameterized(List.class, itemClass).getType();
    }

    private static boolean isListContentType(@NonNull Type contentType) {
        if (contentType instanceof ParameterizedType) {
            Type raw = ((ParameterizedType) contentType).getRawType();
            return raw instanceof Class && List.class.isAssignableFrom((Class<?>) raw);
        }
        return contentType instanceof Class && List.class.isAssignableFrom((Class<?>) contentType);
    }

    @SuppressWarnings("unchecked")
    private static <T> void deliverEmptyContent(@NonNull OnHttpImListener<T> callback,
                                                @NonNull Type contentType,
                                                @Nullable Runnable onFail) {
        T empty = emptyContent(contentType);
        if (empty != null) {
            callback.onHttpImSuccess(empty);
        } else if (onFail != null) {
            onFail.run();
        }
    }

    @SuppressWarnings("unchecked")
    @Nullable
    private static <T> T emptyContent(@NonNull Type contentType) {
        if (isListContentType(contentType)) {
            return (T) Collections.emptyList();
        }
        if (contentType instanceof Class) {
            try {
                return (T) ((Class<?>) contentType).getDeclaredConstructor().newInstance();
            } catch (ReflectiveOperationException ignored) {
                return null;
            }
        }
        return null;
    }

    private static <T> OnHttpListener<BaseMsgResponse<T>> getListener(
            @NonNull String tag,
            @NonNull OnHttpImListener<T> callback,
            @Nullable Runnable onFail,
            boolean requireContent,
            @NonNull Type contentType) {
        return postListener(tag, callback, onFail, requireContent, contentType);
    }

    private static void logBizError(@NonNull String tag, @NonNull BaseMsgResponse<?> result) {
        Log.e(TAG, tag + " 业务错误: errorCode=" + result.getErrorCode()
                + " msg=" + result.getErrorMsg());
    }

    private static void logHttpFail(@NonNull String tag, @NonNull Throwable throwable) {
        Log.e(TAG, tag + " 请求失败: " + throwable.getMessage());
    }
}
