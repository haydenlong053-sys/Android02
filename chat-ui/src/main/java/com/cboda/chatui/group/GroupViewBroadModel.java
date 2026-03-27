/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.group;

import static com.cboda.baselibrary.utils.ELogUtilKt.eLogD;
import static com.cboda.baselibrary.utils.GsonUtilsKt.toJson2;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.bumptech.glide.Glide;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.cboda.baselibrary.bean.BaseBroadResponse;
import com.cboda.baselibrary.bean.CommonResultBroadBean;
import com.cboda.baselibrary.common.UrlBroadConstants;
import com.cboda.baselibrary.livedata.SingleLiveBroadData;
import com.cboda.baselibrary.network.NetworkBroadUtils;
import com.cboda.baselibrary.network.model.NetworkBroadResponse;
import com.cboda.baselibrary.network.repository.NetworkBroadRepository;
import com.cboda.baselibrary.utils.HttpsBroadConfig;
import com.cboda.baselibrary.utils.MMKVBroadUtils;
import com.cboda.chatui.ChatManagerBroadHolder;
import com.cboda.chatui.R;
import com.cboda.chatui.common.OperateBroadResult;
import com.cboda.chatui.contact.model.UIUserBroadInfo;
import com.cboda.chatui.net.OKHttpBroadHelper;
import com.cboda.chatui.net.SimpleCallback;
import com.cboda.chatui.third.utils.FileBroadUtils;
import com.cboda.chatui.user.UserViewBroadModel;
import com.cboda.chatui.utils.PinyinBroadUtils;
import com.cboda.chatui.utils.portrait.CombineBitmapBroadTools;

import org.jetbrains.annotations.NotNull;

import cn.wildfirechat.message.Message;
import cn.wildfirechat.message.MessageContent;
import cn.wildfirechat.message.MessageContentMediaType;
import cn.wildfirechat.message.notification.ModifyGroupSettingsNotificationContent;
import cn.wildfirechat.message.notification.NotificationMessageContent;
import cn.wildfirechat.model.GroupInfo;
import cn.wildfirechat.model.GroupMember;
import cn.wildfirechat.model.ModifyGroupInfoType;
import cn.wildfirechat.model.UserInfo;
import cn.wildfirechat.remote.ChatManager;
import cn.wildfirechat.remote.GeneralCallback;
import cn.wildfirechat.remote.GeneralCallback2;
import cn.wildfirechat.remote.GetGroupMembersCallback;
import cn.wildfirechat.remote.GetGroupsCallback;
import cn.wildfirechat.remote.OnGroupInfoUpdateListener;
import cn.wildfirechat.remote.OnGroupMembersUpdateListener;
import cn.wildfirechat.remote.OnReceiveMessageListener;
import cn.wildfirechat.remote.UploadMediaCallback;

public class GroupViewBroadModel extends ViewModel implements OnGroupInfoUpdateListener, OnGroupMembersUpdateListener, OnReceiveMessageListener {
    private MutableLiveData<List<GroupInfo>> groupInfoUpdateLiveData;
    private MutableLiveData<List<GroupMember>> groupMembersUpdateLiveData;

    public MutableLiveData<Boolean> setIsGag(String groupID, boolean isGag) {
        eLogD("GroupViewBroadModelClass", "getGagData");
        MutableLiveData<Boolean> result = new MutableLiveData<>();
//        String url = HttpsBroadConfig.INSTANCE.getAppBaseUrl() + "/groupInfo";
        Map<String, Object> params = new HashMap<>(2);
        params.put("id", groupID);
        params.put("allJy", isGag ? "1" : "0");
        NetworkBroadUtils.INSTANCE.postEncrypted("api/editGroup", params, new NetworkBroadUtils.NetworkCallback<>() {
            @Override
            public void onFailure(int code, @NotNull String message, @org.jetbrains.annotations.Nullable Object data) {
                eLogD("GroupViewBroadModelClass", "onFailure:" + code + "," + message);
            }

            @Override
            public void onSuccess(@org.jetbrains.annotations.Nullable Object data) {
                result.postValue(isGag);
//                eLogD("GroupViewBroadModelClass", "onSuccess:" + toJson2(data));
            }

            @Override
            public void onError(@NotNull Throwable throwable) {
//                eLogD("GroupViewBroadModelClass", "onError");
            }


        });
//                UrlBroadBroadConstants.SIGN_INFO,
//                SignInfoViewBroadModel::class.java, HashMap<String, Any>()
//                    )
        return result;
    }

    public GroupViewBroadModel() {
        super();
        ChatManager.Instance().addGroupInfoUpdateListener(this);
        ChatManager.Instance().addGroupMembersUpdateListener(this);
        ChatManager.Instance().addOnReceiveMessageListener(this);
    }

    @Override
    protected void onCleared() {
        ChatManager.Instance().removeGroupInfoUpdateListener(this);
        ChatManager.Instance().removeGroupMembersUpdateListener(this);
        ChatManager.Instance().removeOnReceiveMessageListener(this);
    }

    public MutableLiveData<List<GroupInfo>> groupInfoUpdateLiveData() {
        if (groupInfoUpdateLiveData == null) {
            groupInfoUpdateLiveData = new MutableLiveData<>();
        }
        return groupInfoUpdateLiveData;
    }

    public MutableLiveData<List<GroupMember>> groupMembersUpdateLiveData() {
        if (groupMembersUpdateLiveData == null) {
            groupMembersUpdateLiveData = new MutableLiveData<>();
        }
        return groupMembersUpdateLiveData;
    }

    public MutableLiveData<List<UIUserBroadInfo>> getGroupMemberUIUserInfosLiveData(String groupId, boolean refresh) {
        MutableLiveData<List<UIUserBroadInfo>> groupMemberLiveData = new MutableLiveData<>();
        ChatManager.Instance().getWorkHandler().post(() -> {
            List<GroupMember> members = ChatManager.Instance().getGroupMembers(groupId, refresh);
            List<String> memberIds = new ArrayList<>(members.size());
            for (GroupMember member : members) {
                memberIds.add(member.memberId);
            }
            List<UserInfo> userInfos = ChatManager.Instance().getUserInfos(memberIds, groupId);
            List<UIUserBroadInfo> users = UIUserBroadInfo.fromUserInfos(userInfos);
            groupMemberLiveData.postValue(users);
        });
        return groupMemberLiveData;
    }

    public MutableLiveData<List<UserInfo>> getGroupMemberUserInfosLiveData(String groupId, boolean refresh) {

        return getGroupMemberUserInfosLiveData(groupId, refresh, Long.MAX_VALUE);
    }

    public MutableLiveData<List<UserInfo>> getGroupMemberUserInfosLiveData(String groupId, boolean refresh, long joinBeforeDt) {
        MutableLiveData<List<UserInfo>> groupMemberLiveData = new MutableLiveData<>();
        ChatManager.Instance().getWorkHandler().post(() -> {
            List<GroupMember> members = ChatManager.Instance().getGroupMembers(groupId, refresh);
            List<String> memberIds = new ArrayList<>(members.size());
            for (GroupMember member : members) {
                if (member.createDt <= joinBeforeDt) {
                    memberIds.add(member.memberId);
                }
            }
            List<UserInfo> userInfos = ChatManager.Instance().getUserInfos(memberIds, groupId);
            groupMemberLiveData.postValue(userInfos);
        });
        return groupMemberLiveData;
    }

    @Override
    public void onGroupInfoUpdate(List<GroupInfo> groupInfos) {
        if (groupInfoUpdateLiveData != null) {
            groupInfoUpdateLiveData.setValue(groupInfos);
        }
    }

    public MutableLiveData<OperateBroadResult<String>> createGroup(Context context, List<UserInfo> checkedUsers, MessageContent notifyMsg, List<Integer> lines) {
        List<String> selectedIds = new ArrayList<>(checkedUsers.size());
        List<UserInfo> selectedUsers = new ArrayList<>();
        for (UserInfo userInfo : checkedUsers) {
            selectedIds.add(userInfo.uid);
            selectedUsers.add(userInfo);
        }
        String id = ChatManager.Instance().getUserId();
        if (!selectedIds.contains(id)) {
            selectedIds.add(id);
            selectedUsers.add(ChatManager.Instance().getUserInfo(id, false));
        }
        String groupName = "";
        for (int i = 0; i < 3 && i < selectedUsers.size(); i++) {
            groupName += selectedUsers.get(i).displayName + "、";
        }
        groupName = groupName.substring(0, groupName.length() - 1);
        if (selectedUsers.size() > 3) {
            groupName += " ...";
        }

        groupName = groupName.substring(0, groupName.length() - 1);

        MutableLiveData<OperateBroadResult<String>> groupLiveData = new MutableLiveData<>();
        String finalGroupName = groupName;
        ChatManager.Instance().createGroup(null, finalGroupName, null, GroupInfo.GroupType.Restricted, null, selectedIds, null, lines, notifyMsg, new GeneralCallback2() {
            @Override
            public void onSuccess(String groupId) {
                groupLiveData.setValue(new OperateBroadResult<>(groupId, 0));
            }

            @Override
            public void onFail(int errorCode) {
                groupLiveData.setValue(new OperateBroadResult<>(errorCode));
            }
        });
        return groupLiveData;
    }

    public MutableLiveData<Boolean> addGroupMember(GroupInfo groupInfo, List<String> memberIds, MessageContent notifyMsg, List<Integer> notifyLines) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        // TODO need update group portrait or not?
        ChatManager.Instance().addGroupMembers(groupInfo.target, memberIds, null, notifyLines, notifyMsg, new GeneralCallback() {
            @Override
            public void onSuccess() {
                result.setValue(true);
            }

            @Override
            public void onFail(int errorCode) {
                result.setValue(false);
            }
        });

        return result;
    }

    public MutableLiveData<Boolean> removeGroupMember(GroupInfo groupInfo, List<String> memberIds, MessageContent notifyMsg, List<Integer> notifyLines) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        ChatManagerBroadHolder.gChatManager.removeGroupMembers(groupInfo.target, memberIds, notifyLines, notifyMsg, new GeneralCallback() {
            @Override
            public void onSuccess() {
                result.setValue(true);
            }

            @Override
            public void onFail(int errorCode) {
                result.setValue(false);
            }
        });

        return result;
    }

    public MutableLiveData<OperateBroadResult<Boolean>> setGroupManager(String groupId, boolean isSet, List<String> memberIds, NotificationMessageContent notifyMsg, List<Integer> lines) {
        MutableLiveData<OperateBroadResult<Boolean>> result = new MutableLiveData<>();
        ChatManager.Instance().setGroupManager(groupId, isSet, memberIds, lines, notifyMsg, new GeneralCallback() {
            @Override
            public void onSuccess() {
                result.setValue(new OperateBroadResult<>(0));
            }

            @Override
            public void onFail(int errorCode) {
                result.setValue(new OperateBroadResult<>(errorCode));
            }
        });
        return result;
    }

    public MutableLiveData<OperateBroadResult<Boolean>> muteGroupMember(String groupId, boolean mute, List<String> memberIds, NotificationMessageContent notifyMsg, List<Integer> lines) {
        MutableLiveData<OperateBroadResult<Boolean>> result = new MutableLiveData<>();
        ChatManager.Instance().muteGroupMember(groupId, mute, memberIds, lines, notifyMsg, new GeneralCallback() {
            @Override
            public void onSuccess() {
                result.setValue(new OperateBroadResult<>(true, 0));
            }

            @Override
            public void onFail(int errorCode) {
                result.setValue(new OperateBroadResult<>(false, errorCode));
            }
        });
        return result;
    }

    public MutableLiveData<OperateBroadResult<Boolean>> allowGroupMember(String groupId, boolean allow, List<String> memberIds, NotificationMessageContent notifyMsg, List<Integer> lines) {
        MutableLiveData<OperateBroadResult<Boolean>> result = new MutableLiveData<>();
        ChatManager.Instance().allowGroupMember(groupId, allow, memberIds, lines, notifyMsg, new GeneralCallback() {
            @Override
            public void onSuccess() {
                result.setValue(new OperateBroadResult<>(true, 0));
            }

            @Override
            public void onFail(int errorCode) {
                result.setValue(new OperateBroadResult<>(false, errorCode));
            }
        });
        return result;
    }

    public MutableLiveData<OperateBroadResult<Boolean>> muteAll(String groupId, boolean mute, MessageContent notifyMsg, List<Integer> notifyLines) {
        MutableLiveData<OperateBroadResult<Boolean>> result = new MutableLiveData<>();
        ChatManager.Instance().modifyGroupInfo(groupId, ModifyGroupInfoType.Modify_Group_Mute, mute ? "1" : "0", notifyLines, notifyMsg, new GeneralCallback() {
            @Override
            public void onSuccess() {
                result.setValue(new OperateBroadResult<>(0));
            }

            @Override
            public void onFail(int errorCode) {
                result.setValue(new OperateBroadResult<>(errorCode));
            }
        });
        return result;
    }

    public MutableLiveData<OperateBroadResult<Boolean>> enablePrivateChat(String groupId, boolean enablePrivateChat, MessageContent notifyMsg, List<Integer> notifyLines) {
        MutableLiveData<OperateBroadResult<Boolean>> result = new MutableLiveData<>();
        ChatManager.Instance().modifyGroupInfo(groupId, ModifyGroupInfoType.Modify_Group_PrivateChat, enablePrivateChat ? "0" : "1", notifyLines, notifyMsg, new GeneralCallback() {
            @Override
            public void onSuccess() {
                result.setValue(new OperateBroadResult<>(0));
            }

            @Override
            public void onFail(int errorCode) {
                result.setValue(new OperateBroadResult<>(errorCode));
            }
        });
        return result;
    }

    public MutableLiveData<OperateBroadResult<Boolean>> setGroupJoinType(String groupId, int joinType, MessageContent notifyMsg, List<Integer> notifyLines) {
        MutableLiveData<OperateBroadResult<Boolean>> result = new MutableLiveData<>();
        ChatManager.Instance().modifyGroupInfo(groupId, ModifyGroupInfoType.Modify_Group_JoinType, joinType + "", notifyLines, notifyMsg, new GeneralCallback() {
            @Override
            public void onSuccess() {
                result.setValue(new OperateBroadResult<>(0));
            }

            @Override
            public void onFail(int errorCode) {
                result.setValue(new OperateBroadResult<>(errorCode));
            }
        });
        return result;
    }

    public MutableLiveData<OperateBroadResult<Boolean>> setGroupSearchType(String groupId, int searchType, MessageContent notifyMsg, List<Integer> notifyLines) {
        MutableLiveData<OperateBroadResult<Boolean>> result = new MutableLiveData<>();
        ChatManager.Instance().modifyGroupInfo(groupId, ModifyGroupInfoType.Modify_Group_Searchable, searchType + "", notifyLines, notifyMsg, new GeneralCallback() {
            @Override
            public void onSuccess() {
                result.setValue(new OperateBroadResult<>(0));
            }

            @Override
            public void onFail(int errorCode) {
                result.setValue(new OperateBroadResult<>(errorCode));
            }
        });
        return result;
    }

    public MutableLiveData<OperateBroadResult<Boolean>> setGroupHistoryMessage(String groupId, int searchType, MessageContent notifyMsg, List<Integer> notifyLines) {
        MutableLiveData<OperateBroadResult<Boolean>> result = new MutableLiveData<>();
        ChatManager.Instance().modifyGroupInfo(groupId, ModifyGroupInfoType.Modify_Group_History_Message, searchType + "", notifyLines, notifyMsg, new GeneralCallback() {
            @Override
            public void onSuccess() {
                result.setValue(new OperateBroadResult<>(0));
            }

            @Override
            public void onFail(int errorCode) {
                result.setValue(new OperateBroadResult<>(errorCode));
            }
        });
        return result;
    }

    public @Nullable
    GroupInfo getGroupInfo(String groupId, boolean refresh) {
        eLogD(this.getClass().getSimpleName(), "getGroupInfos65");
        return ChatManager.Instance().getGroupInfo(groupId, refresh);
    }

    public List<GroupMember> getGroupMembers(String groupId, boolean forceRefresh) {
        return ChatManager.Instance().getGroupMembers(groupId, forceRefresh);
    }

    public MutableLiveData<List<GroupMember>> getGroupMembersLiveData(String groupId, boolean refresh) {
        MutableLiveData<List<GroupMember>> data = new MutableLiveData<>();
        ChatManager.Instance().getGroupMembers(groupId, refresh, new GetGroupMembersCallback() {
            @Override
            public void onSuccess(List<GroupMember> groupMembers) {
                data.postValue(groupMembers);
            }

            @Override
            public void onFail(int errorCode) {
                data.postValue(new ArrayList<>());
            }
        });

        return data;
    }

    public MutableLiveData<List<UIUserBroadInfo>> getGroupManagerUIUserInfosLiveData(String groupId, boolean refresh) {
        MutableLiveData<List<UIUserBroadInfo>> data = new MutableLiveData<>();
        ChatManager.Instance().getWorkHandler().post(() -> {
            List<GroupMember> managers = getGroupManagers(groupId);
            List<UIUserBroadInfo> userInfos = managerMemberToUIUserInfo(groupId, managers);
            data.postValue(userInfos);
        });

        return data;
    }

    public MutableLiveData<List<UIUserBroadInfo>> getMutedOrAllowedMemberUIUserInfosLiveData(String groupId, boolean muted, boolean refresh) {
        MutableLiveData<List<UIUserBroadInfo>> data = new MutableLiveData<>();
        ChatManager.Instance().getWorkHandler().post(() -> {
            List<GroupMember> mutedMembers = getMutedOrAllowedMembers(groupId, muted);
            List<UIUserBroadInfo> userInfos = mutedOrAllowedMemberToUIUserInfo(groupId, muted, mutedMembers);
            data.postValue(userInfos);
        });

        return data;
    }

    public List<GroupMember> getGroupManagers(String groupId) {
        List<GroupMember> members = ChatManager.Instance().getGroupMembers(groupId, false);
        List<GroupMember> managers = new ArrayList<>();
        if (members != null) {
            for (GroupMember member : members) {
                if (member.type == GroupMember.GroupMemberType.Manager || member.type == GroupMember.GroupMemberType.Owner) {
                    managers.add(member);
                }
            }
        }
        return managers;
    }

    public List<String> getGroupManagerIds(String groupId) {
        List<GroupMember> managers = getGroupManagers(groupId);
        List<String> mangerIds = new ArrayList<>();
        if (managers != null) {
            for (GroupMember manager : managers) {
                mangerIds.add(manager.memberId);
            }
        }
        return mangerIds;
    }


    public List<GroupMember> getMutedOrAllowedMembers(String groupId, boolean muted) {
        List<GroupMember> members = ChatManager.Instance().getGroupMembers(groupId, false);
        List<GroupMember> managers = new ArrayList<>();
        if (members != null) {
            for (GroupMember member : members) {
                if ((muted && member.type == GroupMember.GroupMemberType.Allowed)
                        || !muted && member.type == GroupMember.GroupMemberType.Muted) {
                    managers.add(member);
                }
            }
        }
        return managers;
    }


    public List<String> getMutedOrAllowedMemberIds(String groupId, boolean muted) {
        List<GroupMember> mutedMembers = getMutedOrAllowedMembers(groupId, muted);
        List<String> mutedIds = new ArrayList<>();
        if (mutedMembers != null) {
            for (GroupMember manager : mutedMembers) {
                mutedIds.add(manager.memberId);
            }
        }
        return mutedIds;

    }

    private List<UIUserBroadInfo> managerMemberToUIUserInfo(String groupId, List<GroupMember> members) {
        if (members == null || members.isEmpty()) {
            return null;
        }

        List<String> memberIds = new ArrayList<>(members.size());
        for (GroupMember member : members) {
            memberIds.add(member.memberId);
        }

        List<UIUserBroadInfo> uiUserInfos = new ArrayList<>();
        List<UserInfo> userInfos = UserViewBroadModel.getUsers(memberIds, groupId);
        boolean showManagerCategory = false;
        for (UserInfo userInfo : userInfos) {
            UIUserBroadInfo info = new UIUserBroadInfo(userInfo);
            String name = ChatManager.Instance().getGroupMemberDisplayName(userInfo);
            if (!TextUtils.isEmpty(name)) {
                String pinyin = PinyinBroadUtils.getPinyin(name);
                char c = pinyin.toUpperCase().charAt(0);
                if (c >= 'A' && c <= 'Z') {
                    info.setSortName(pinyin);
                } else {
                    // 为了让排序排到最后
                    info.setSortName("{" + pinyin);
                }
            } else {
                info.setSortName("");
            }

            for (GroupMember member : members) {
                if (userInfo.uid.equals(member.memberId)) {
                    if (member.type == GroupMember.GroupMemberType.Manager) {
                        info.setCategory("管理员");
                        if (!showManagerCategory) {
                            showManagerCategory = true;
                            info.setShowCategory(true);
                        }
                        uiUserInfos.add(info);
                    } else {
                        info.setCategory("群主");
                        info.setShowCategory(true);
                        uiUserInfos.add(0, info);
                    }
                    break;
                }
            }
        }
        return uiUserInfos;
    }

    private List<UIUserBroadInfo> mutedOrAllowedMemberToUIUserInfo(String groupId, boolean muted, List<GroupMember> members) {
        if (members == null || members.isEmpty()) {
            return null;
        }

        List<String> memberIds = new ArrayList<>(members.size());
        for (GroupMember member : members) {
            memberIds.add(member.memberId);
        }

        List<UIUserBroadInfo> uiUserInfos = new ArrayList<>();
        List<UserInfo> userInfos = UserViewBroadModel.getUsers(memberIds, groupId);
        boolean showManagerCategory = false;
        for (UserInfo userInfo : userInfos) {
            UIUserBroadInfo info = new UIUserBroadInfo(userInfo);
            String name = ChatManager.Instance().getGroupMemberDisplayName(userInfo);
            if (!TextUtils.isEmpty(name)) {
                String pinyin = PinyinBroadUtils.getPinyin(name);
                char c = pinyin.toUpperCase().charAt(0);
                if (c >= 'A' && c <= 'Z') {
                    info.setSortName(pinyin);
                } else {
                    // 为了让排序排到最后
                    info.setSortName("{" + pinyin);
                }
            } else {
                info.setSortName("");
            }
            info.setCategory(muted ? "白名单列表" : "禁言列表");
            if (!showManagerCategory) {
                showManagerCategory = true;
                info.setShowCategory(true);
            }
            uiUserInfos.add(info);
        }
        return uiUserInfos;
    }

    public GroupMember getGroupMember(String groupId, String memberId) {
        return ChatManager.Instance().getGroupMember(groupId, memberId);
    }

    public String getGroupMemberDisplayName(String groupId, String memberId) {
        return ChatManager.Instance().getGroupMemberDisplayName(groupId, memberId);
    }

    public MutableLiveData<OperateBroadResult<List<GroupInfo>>> getFavGroups() {
        MutableLiveData<OperateBroadResult<List<GroupInfo>>> result = new MutableLiveData<>();
        ChatManager.Instance().getFavGroups(new GetGroupsCallback() {
            @Override
            public void onSuccess(List<GroupInfo> groupInfos) {
                result.setValue(new OperateBroadResult<>(groupInfos, 0));
            }

            @Override
            public void onFail(int errorCode) {
                result.setValue(new OperateBroadResult<>(null, 0));
            }
        });
        return result;
    }

    public MutableLiveData<OperateBroadResult<Boolean>> modifyGroupInfo(String groupId, ModifyGroupInfoType modifyType, String newValue, MessageContent notifyMsg, List<Integer> notifyLines) {
        MutableLiveData<OperateBroadResult<Boolean>> result = new MutableLiveData<>();
        ChatManager.Instance().modifyGroupInfo(groupId, modifyType, newValue, notifyLines, notifyMsg, new GeneralCallback() {
            @Override
            public void onSuccess() {
                result.setValue(new OperateBroadResult<>(true, 0));
            }

            @Override
            public void onFail(int errorCode) {
                result.setValue(new OperateBroadResult<>(false, errorCode));
            }
        });
        return result;
    }

    public MutableLiveData<OperateBroadResult<Boolean>> updateGroupPortrait(String groupId, String localImagePath) {
        MutableLiveData<OperateBroadResult<Boolean>> resultLiveData = new MutableLiveData<>();
        byte[] content = FileBroadUtils.readFile(localImagePath);
        if (content != null) {
            ChatManager.Instance().uploadMediaFile(localImagePath, MessageContentMediaType.PORTRAIT.getValue(), new UploadMediaCallback() {
                @Override
                public void onSuccess(String result) {
                    ChatManager.Instance().modifyGroupInfo(groupId, ModifyGroupInfoType.Modify_Group_Portrait, result, Collections.singletonList(0), null, new GeneralCallback() {
                        @Override
                        public void onSuccess() {
                            resultLiveData.setValue(new OperateBroadResult<>(true, 0));
                        }

                        @Override
                        public void onFail(int errorCode) {
                            resultLiveData.setValue(new OperateBroadResult<>(false, errorCode));
                        }
                    });
                }

                @Override
                public void onProgress(long uploaded, long total) {

                }


                @Override
                public void onFail(int errorCode) {
                    resultLiveData.setValue(new OperateBroadResult<>(errorCode));
                }
            });
        }
        return resultLiveData;
    }

    public MutableLiveData<OperateBroadResult<Boolean>> setGroupRemark(String groupId, String remark) {
        MutableLiveData<OperateBroadResult<Boolean>> result = new MutableLiveData<>();
        ChatManager.Instance().setGroupRemark(groupId, remark, new GeneralCallback() {
            @Override
            public void onSuccess() {
                result.setValue(new OperateBroadResult<>(true, 0));
            }

            @Override
            public void onFail(int errorCode) {
                result.setValue(new OperateBroadResult<>(false, errorCode));
            }
        });
        return result;
    }

    public MutableLiveData<OperateBroadResult> modifyMyGroupAlias(String groupId, String alias, MessageContent notifyMsg, List<Integer> notifyLines) {
        MutableLiveData<OperateBroadResult> result = new MutableLiveData<>();
        ChatManager.Instance().modifyGroupAlias(groupId, alias, notifyLines, notifyMsg, new GeneralCallback() {
            @Override
            public void onSuccess() {
                result.setValue(new OperateBroadResult<>(0));
            }

            @Override
            public void onFail(int errorCode) {
                result.setValue(new OperateBroadResult<>(errorCode));
            }
        });
        return result;
    }

    public MutableLiveData<OperateBroadResult<Boolean>> setFavGroup(String groupId, boolean fav) {
        MutableLiveData<OperateBroadResult<Boolean>> result = new MutableLiveData<>();
        ChatManager.Instance().setFavGroup(groupId, fav, new GeneralCallback() {
            @Override
            public void onSuccess() {
                result.setValue(new OperateBroadResult<>(0));
            }

            @Override
            public void onFail(int errorCode) {
                result.setValue(new OperateBroadResult<>(errorCode));
            }
        });
        return result;
    }

    public MutableLiveData<Boolean> quitGroup(String groupId, List<Integer> lines, MessageContent notifyMsg) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        ChatManager.Instance().quitGroup(groupId, lines, notifyMsg, new GeneralCallback() {
            @Override
            public void onSuccess() {
                result.setValue(true);
            }

            @Override
            public void onFail(int errorCode) {
                result.setValue(false);
            }
        });
        return result;
    }

    public MutableLiveData<Boolean> dismissGroup(String groupId, List<Integer> lines, MessageContent notifyMsg) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        ChatManager.Instance().dismissGroup(groupId, lines, notifyMsg, new GeneralCallback() {
            @Override
            public void onSuccess() {
                result.setValue(true);
            }

            @Override
            public void onFail(int errorCode) {
                result.setValue(false);
            }
        });
        return result;
    }

    private @Nullable
    String generateGroupPortrait(Context context, List<UserInfo> userInfos) throws Exception {
        List<Bitmap> bitmaps = new ArrayList<>();
        for (UserInfo userInfo : userInfos) {
            Drawable drawable;
            try {
                drawable = Glide.with(context).load(userInfo.portrait).placeholder(R.mipmap.avatar_broad_def).submit(60, 60).get();
            } catch (Exception e) {
                e.printStackTrace();
                drawable = Glide.with(context).load(R.mipmap.avatar_broad_def).submit(60, 60).get();
            }
            if (drawable instanceof BitmapDrawable) {
                bitmaps.add(((BitmapDrawable) drawable).getBitmap());
            }
        }
        Bitmap bitmap = CombineBitmapBroadTools.combimeBitmap(context, 60, 60, bitmaps);
        if (bitmap == null) {
            return null;
        }
        //create a file to write bitmap data
        File f = new File(context.getCacheDir(), System.currentTimeMillis() + ".png");
        f.createNewFile();

        //Convert bitmap to byte array
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
        byte[] bitmapData = bos.toByteArray();

        //write the bytes in file
        FileOutputStream fos = new FileOutputStream(f);
        fos.write(bitmapData);
        fos.flush();
        fos.close();

        return f.getAbsolutePath();
    }

    @Override
    public void onGroupMembersUpdate(String groupId, List<GroupMember> groupMembers) {
        if (groupMembersUpdateLiveData != null && groupMembers != null && !groupMembers.isEmpty()) {
            groupMembersUpdateLiveData.setValue(groupMembers);
        }
    }

    @Override
    public void onReceiveMessage(List<Message> messages, boolean hasMore) {
        Set<String> groupIds = new HashSet<>();
        for (Message msg : messages) {
            if (msg.content instanceof ModifyGroupSettingsNotificationContent) {
                groupIds.add(((ModifyGroupSettingsNotificationContent) msg.content).groupId);
            }
        }
        if (groupIds.size() > 0) {
            ChatManager.Instance().getGroupInfos(new ArrayList<>(groupIds), true);
        }
    }


    //上麦申请接口
    public SingleLiveBroadData<Boolean> goMeetUpMicApply(String groupID) {
        SingleLiveBroadData<Boolean> result = new SingleLiveBroadData<>();
        Map<String, Object> params = new HashMap<>(2);
        params.put("uid", MMKVBroadUtils.Companion.getRealUserId());
        params.put("groupIdNo", groupID);
        NetworkBroadUtils.INSTANCE.postEncrypted("api/quick/live/applyMic", params, new NetworkBroadUtils.NetworkCallback<>() {
            @Override
            public void onFailure(int code, @NotNull String message, @org.jetbrains.annotations.Nullable Object data) {
                eLogD("GroupViewBroadModelClass", "onFailure:" + code + "," + message);
            }

            @Override
            public void onSuccess(@org.jetbrains.annotations.Nullable Object data) {
                result.postValue(true);
            }

            @Override
            public void onError(@NotNull Throwable throwable) {
            }

        });
        return result;
    }


    public void goMeetDownMicApply(String target) {

    }
}
