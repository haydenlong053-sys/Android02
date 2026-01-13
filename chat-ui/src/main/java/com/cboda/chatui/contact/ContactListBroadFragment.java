/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.contact;

import static com.cboda.baselibrary.utils.ELogUtilKt.eLogD;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.cboda.chatui.R;
import com.cboda.chatui.bridge.FriendDataListBroadBean;
import com.cboda.chatui.bridge.MyCommonBroadBridge;
import com.cboda.chatui.contact.model.FriendRequestBroadValue;
import com.cboda.chatui.contact.model.GroupBroadValue;
import com.cboda.chatui.contact.model.HeaderBroadValue;
import com.cboda.chatui.contact.model.UIUserBroadInfo;
import com.cboda.chatui.contact.viewholder.header.ChannelViewBroadHolder;
import com.cboda.chatui.contact.viewholder.header.FriendRequestViewBroadHolder;
import com.cboda.chatui.contact.viewholder.header.GroupViewBroadHolder;
import com.cboda.chatui.contact.viewholder.header.HeaderViewBroadHolder;
import com.cboda.chatui.utils.PinyinBroadUtils;
import com.cboda.chatui.widget.QuickIndexBroadBar;
import cn.wildfirechat.model.ChannelInfo;
import cn.wildfirechat.model.UserInfo;
import cn.wildfirechat.model.UserOnlineState;
import cn.wildfirechat.remote.ChatManager;
import com.cboda.baselibrary.common.BridgeImEnum;

public class ContactListBroadFragment extends BaseUserListBroadFragment implements QuickIndexBroadBar.OnLetterUpdateListener {
    private boolean pick = false;
    private boolean showChannel = true;
    // 临时方案，如果本地缓存了组织结构信息，应当更新
    private boolean isRootOrganizationLoaded = false;
    private boolean isMyOrganizationLoaded = false;
    private List<String> filterUserList;
    private static final int REQUEST_CODE_PICK_CHANNEL = 100;

    private OrganizationServiceViewBroadModel organizationServiceViewModel;

    private boolean isVisibleToUser = false;
    private ContactViewBroadModel contactViewModel;


    @Override
    public void setMenuVisibility(boolean isvisible) {
        super.setMenuVisibility(isvisible);
        this.isVisibleToUser = isvisible;
        if (isvisible && contactViewModel != null) {
            contactViewModel.reloadContact();
            contactViewModel.reloadFriendRequestStatus();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showQuickIndexBar(true);
        Bundle bundle = getArguments();
        if (bundle != null) {
            pick = bundle.getBoolean("pick", false);
            showChannel = bundle.getBoolean("showChannel", true);
            filterUserList = bundle.getStringArrayList("filterUserList");
        }
        organizationServiceViewModel = new ViewModelProvider(this).get(OrganizationServiceViewBroadModel.class);
        contactViewModel = new ViewModelProvider(this).get(ContactViewBroadModel.class);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isVisibleToUser) {
            contactViewModel.reloadContact();
            contactViewModel.reloadFriendRequestStatus();
            contactViewModel.reloadFavContact();
        }
    }

    private Gson gson = new Gson();
    public void setFriendData(String json){
        eLogD("野火","通讯录好友数据--> "+json);
        FriendDataListBroadBean friendDataListBean = gson.fromJson(json, FriendDataListBroadBean.class);
        if (friendDataListBean != null && friendDataListBean.getDataList() != null){
            List<UIUserBroadInfo> userInfos = new ArrayList<>();
            for (int i = 0; i < friendDataListBean.getDataList().size(); i++) {
                UserInfo userInfo = new UserInfo();
                userInfo.portrait = friendDataListBean.getDataList().get(i).getIcon();
                userInfo.uid = friendDataListBean.getDataList().get(i).getUserId();
                if (!TextUtils.isEmpty(friendDataListBean.getDataList().get(i).getBei())){
                    userInfo.lixinUserName =friendDataListBean.getDataList().get(i).getBei();
                } else {
                    userInfo.lixinUserName =friendDataListBean.getDataList().get(i).getName();
                }
                UIUserBroadInfo uiUserInfo = new UIUserBroadInfo(userInfo);


                userInfos.add(uiUserInfo);
            }

//            List<UserInfo> paixiuserInfos = new ArrayList<>();
//            for (int i = 0; i < userInfos.size(); i++) {
//                paixiuserInfos.add(userInfos.get(i).getUserInfo());
//            }
//
//            List<UIUserBroadInfo> uiUserInfos = UIUserBroadInfo.fromUserInfos(paixiuserInfos, false);
            if (userInfos != null ){
                String preIndexLetter = null;
                for (int i = 0; i < userInfos.size(); i++) {
                    String indexLetter;
                    String displayName = friendDataListBean.getDataList().get(i).getZimu();
                    if (!TextUtils.isEmpty(displayName)) {
                        String pinyin = PinyinBroadUtils.getPinyin(displayName);
                        char c = pinyin.toUpperCase().charAt(0);
                        if (c >= 'A' && c <= 'Z') {
                            indexLetter = c + "";
                            userInfos.get(i).setSortName(pinyin);
                        } else {
                            indexLetter = "#";
                            // 为了让排序排到最后
                            userInfos.get(i).setSortName("{" + pinyin);
                        }
                        userInfos.get(i).setCategory(indexLetter);
                    } else {
                        userInfos.get(i).setSortName("");
                    }

                    indexLetter =  userInfos.get(i).getCategory();
                    if (preIndexLetter == null || !preIndexLetter.equals(indexLetter)) {
                        userInfos.get(i).setShowCategory(true);
                    }
                    preIndexLetter = indexLetter;
                }
                userListAdapter.setUsers(userInfos);
            }
        }
    }

    public void updateShuLiang(Long xinepngy,Long qunshu){
        ChatManager.Instance().haoyoushenqing = xinepngy;
        ChatManager.Instance().qunshenqing = qunshu;
        if (userListAdapter != null){
            userListAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void afterViews(View view) {
        super.afterViews(view);
        contactViewModel.contactListLiveData().observe(this, userInfos -> {
            showContent();
            if (filterUserList != null) {
                userInfos.removeIf(uiUserInfo -> filterUserList.indexOf(uiUserInfo.getUserInfo().uid) > -1);
            }
            patchUserOnlineState(userInfos);
//            userListAdapter.setUsers(userInfos);
        });
        contactViewModel.friendRequestUpdatedLiveData().observe(getActivity(), integer -> userListAdapter.updateHeader(0, new FriendRequestBroadValue(integer)));
        contactViewModel.favContactListLiveData().observe(getActivity(), uiUserInfos -> {
            if (filterUserList != null) {
                uiUserInfos.removeIf(uiUserInfo -> filterUserList.indexOf(uiUserInfo.getUserInfo().uid) > -1);
            }
            patchUserOnlineState(uiUserInfos);
            userListAdapter.setFavUsers(uiUserInfos);
        });

    }

    private void patchUserOnlineState(List<UIUserBroadInfo> userInfos) {
        if (userInfos == null) {
            return;
        }
        Map<String, UserOnlineState> userOnlineStateMap = ChatManager.Instance().getUserOnlineStateMap();
        for (UIUserBroadInfo userInfo : userInfos) {
            UserOnlineState userOnlineState = userOnlineStateMap.get(userInfo.getUserInfo().uid);
            if (userOnlineState != null) {
                String userOnlineDesc = userOnlineState.desc();
                if (!TextUtils.isEmpty(userOnlineDesc)) {
                    userInfo.setDesc(userOnlineDesc);
                }
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void initHeaderViewHolders() {
        if (!pick) {
            addHeaderViewHolder(FriendRequestViewBroadHolder.class, R.layout.contact_header_broad_friend, new FriendRequestBroadValue(contactViewModel.getUnreadFriendRequestCount()));
            addHeaderViewHolder(GroupViewBroadHolder.class, R.layout.contact_header_broad_group, new GroupBroadValue());
        }
        if (showChannel) {
            addHeaderViewHolder(ChannelViewBroadHolder.class, R.layout.contact_header_broad_channel, new HeaderBroadValue());
        }
//        if (!TextUtils.isEmpty(BroadConfig.ORG_SERVER_ADDRESS)) {
//            organizationServiceViewModel.rootOrganizationLiveData().observe(this, new Observer<List<Organization>>() {
//                @Override
//                public void onChanged(List<Organization> organizations) {
//                    if (isRootOrganizationLoaded) {
//                        return;
//                    }
//                    isRootOrganizationLoaded = true;
//                    if (!organizations.isEmpty()) {
//                        for (Organization org : organizations) {
//                            OrganizationBroadValue value = new OrganizationBroadValue();
//                            value.setValue(org);
//                            addHeaderViewHolder(OrganizationViewBroadHolder.class, R.layout.contact_header_broad_organization, value);
//                        }
//                    }
//                }
//            });
//            organizationServiceViewModel.myOrganizationLiveData().observe(this, new Observer<List<Organization>>() {
//                @Override
//                public void onChanged(List<Organization> organizations) {
//                    if (isMyOrganizationLoaded) {
//                        return;
//                    }
//                    isMyOrganizationLoaded = true;
//                    if (!organizations.isEmpty()) {
//                        for (Organization org : organizations) {
//                            OrganizationBroadValue value = new OrganizationBroadValue();
//                            value.setValue(org);
//                            addHeaderViewHolder(DepartViewBroadHolder.class, R.layout.contact_header_broad_department, value);
//                        }
//                    }
//
//                }
//            });
//        }
    }

    @Override
    public void initFooterViewHolders() {
//        addFooterViewHolder(ContactCountViewBroadHolder.class, R.layout.contact_item_broad_footer, new ContactCountFooterBroadValue());
    }

    @Override
    public void onUserClick(UIUserBroadInfo userInfo) {
//        if (pick) {
//            Intent intent = new Intent();
//            intent.putExtra("userInfo", userInfo.getUserInfo());
//            getActivity().setResult(Activity.RESULT_OK, intent);
//            getActivity().finish();
//        } else {
//            Intent intent = new Intent(getActivity(), UserInfoActivity.class);
//            intent.putExtra("userInfo", userInfo.getUserInfo());
//            startActivity(intent);
//        }

//        Conversation conversation = new Conversation();
//        conversation.type = Conversation.ConversationType.Single;
//        conversation.target =userInfo.getUserInfo().uid;
//        Intent intent = new Intent(getContext(), ConversationBroadActivity.class);
//        intent.putExtra("conversation", conversation);
//        startActivity(intent);
        MyCommonBroadBridge.getInstance().setMyBridgeMethod(getContext(), BridgeImEnum.SINGLE_CHAT_MORE.getValue(),userInfo.getUserInfo().uid,"",null);
    }

    @Override
    public void onHeaderClick(HeaderViewBroadHolder holder) {
        if (pick) {
            showChannelList();
            return;
        }
        if (holder instanceof FriendRequestViewBroadHolder) {
            showFriendRequest();
        } else if (holder instanceof GroupViewBroadHolder) {
            showGroupList();
        } else if (holder instanceof ChannelViewBroadHolder) {
            showChannelList();
        }
    }

    private void showFriendRequest() {
//        FriendRequestBroadValue value = new FriendRequestBroadValue(0);
//        userListAdapter.updateHeader(0, value);
//
//        Intent intent = new Intent(getActivity(), FriendRequestListActivity.class);
//        startActivity(intent);
        MyCommonBroadBridge.getInstance().setMyBridgeMethod(getContext(), BridgeImEnum.NEW_FRIEND.getValue(),"","",null);

    }

    private void showGroupList() {
//        Intent intent = new Intent(getActivity(), GroupListActivity.class);
//        startActivity(intent);
        MyCommonBroadBridge.getInstance().setMyBridgeMethod(getContext(), BridgeImEnum.JOIN_GROUP_INVITATION.getValue(),"","",null);
    }

    private void showChannelList() {
//        Intent intent = new Intent(getActivity(), ChannelListActivity.class);
//        if (pick) {
//            intent.putExtra("pick", true);
//            startActivityForResult(intent, REQUEST_CODE_PICK_CHANNEL);
//        } else {
//            startActivity(intent);
//        }
//        Intent intent = new Intent(getActivity(), GroupListActivity.class);
//        startActivity(intent);
        MyCommonBroadBridge.getInstance().setMyBridgeMethod(getContext(), BridgeImEnum.GROUP_CHAT_LIST.getValue(),"","",null);
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CODE_PICK_CHANNEL && resultCode == Activity.RESULT_OK) {
            Intent intent = new Intent();
            ChannelInfo channelInfo = data.getParcelableExtra("channelInfo");
            intent.putExtra("channelInfo", channelInfo);
            getActivity().setResult(Activity.RESULT_OK, intent);
            getActivity().finish();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
