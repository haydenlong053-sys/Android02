/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfire.chat.kit.contact;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cn.wildfire.chat.kit.Config;
import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.bridge.FriendDataListBean;
import cn.wildfire.chat.kit.bridge.YeHuoCommonBridge;
import cn.wildfire.chat.kit.contact.model.FriendRequestValue;
import cn.wildfire.chat.kit.contact.model.GroupValue;
import cn.wildfire.chat.kit.contact.model.HeaderValue;
import cn.wildfire.chat.kit.contact.model.OrganizationValue;
import cn.wildfire.chat.kit.contact.model.UIUserInfo;
import cn.wildfire.chat.kit.contact.viewholder.header.ChannelViewHolder;
import cn.wildfire.chat.kit.contact.viewholder.header.DepartViewHolder;
import cn.wildfire.chat.kit.contact.viewholder.header.FriendRequestViewHolder;
import cn.wildfire.chat.kit.contact.viewholder.header.GroupViewHolder;
import cn.wildfire.chat.kit.contact.viewholder.header.HeaderViewHolder;
import cn.wildfire.chat.kit.contact.viewholder.header.OrganizationViewHolder;
import cn.wildfire.chat.kit.organization.model.Organization;
import cn.wildfire.chat.kit.utils.PinyinUtils;
import cn.wildfire.chat.kit.widget.QuickIndexBar;
import cn.wildfirechat.model.ChannelInfo;
import cn.wildfirechat.model.UserInfo;
import cn.wildfirechat.model.UserOnlineState;
import cn.wildfirechat.remote.ChatManager;

public class ContactListFragment extends BaseUserListFragment implements QuickIndexBar.OnLetterUpdateListener {
    private boolean pick = false;
    private boolean showChannel = true;
    // 临时方案，如果本地缓存了组织结构信息，应当更新
    private boolean isRootOrganizationLoaded = false;
    private boolean isMyOrganizationLoaded = false;
    private List<String> filterUserList;
    private static final int REQUEST_CODE_PICK_CHANNEL = 100;

    private OrganizationServiceViewModel organizationServiceViewModel;

    private boolean isVisibleToUser = false;
    private ContactViewModel contactViewModel;


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
        organizationServiceViewModel = new ViewModelProvider(this).get(OrganizationServiceViewModel.class);
        contactViewModel = new ViewModelProvider(this).get(ContactViewModel.class);
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
        Log.e("野火","通讯录好友数据--> "+json);
        FriendDataListBean friendDataListBean = gson.fromJson(json, FriendDataListBean.class);
        if (friendDataListBean != null && friendDataListBean.getDataList() != null){
            List<UIUserInfo> userInfos = new ArrayList<>();
            for (int i = 0; i < friendDataListBean.getDataList().size(); i++) {
                UserInfo userInfo = new UserInfo();
                userInfo.portrait = friendDataListBean.getDataList().get(i).getIcon();
                userInfo.uid = friendDataListBean.getDataList().get(i).getUserId();
                if (!TextUtils.isEmpty(friendDataListBean.getDataList().get(i).getBei())){
                    userInfo.lixinUserName =friendDataListBean.getDataList().get(i).getBei();
                } else {
                    userInfo.lixinUserName =friendDataListBean.getDataList().get(i).getName();
                }
                UIUserInfo uiUserInfo = new UIUserInfo(userInfo);


                userInfos.add(uiUserInfo);
            }

//            List<UserInfo> paixiuserInfos = new ArrayList<>();
//            for (int i = 0; i < userInfos.size(); i++) {
//                paixiuserInfos.add(userInfos.get(i).getUserInfo());
//            }
//
//            List<UIUserInfo> uiUserInfos = UIUserInfo.fromUserInfos(paixiuserInfos, false);
            if (userInfos != null ){
                String preIndexLetter = null;
                for (int i = 0; i < userInfos.size(); i++) {
                    String indexLetter;
                    String displayName = friendDataListBean.getDataList().get(i).getZimu();
                    if (!TextUtils.isEmpty(displayName)) {
                        String pinyin = PinyinUtils.getPinyin(displayName);
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
        contactViewModel.friendRequestUpdatedLiveData().observe(getActivity(), integer -> userListAdapter.updateHeader(0, new FriendRequestValue(integer)));
        contactViewModel.favContactListLiveData().observe(getActivity(), uiUserInfos -> {
            if (filterUserList != null) {
                uiUserInfos.removeIf(uiUserInfo -> filterUserList.indexOf(uiUserInfo.getUserInfo().uid) > -1);
            }
            patchUserOnlineState(uiUserInfos);
            userListAdapter.setFavUsers(uiUserInfos);
        });

    }

    private void patchUserOnlineState(List<UIUserInfo> userInfos) {
        if (userInfos == null) {
            return;
        }
        Map<String, UserOnlineState> userOnlineStateMap = ChatManager.Instance().getUserOnlineStateMap();
        for (UIUserInfo userInfo : userInfos) {
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
            addHeaderViewHolder(FriendRequestViewHolder.class, R.layout.contact_header_friend, new FriendRequestValue(contactViewModel.getUnreadFriendRequestCount()));
            addHeaderViewHolder(GroupViewHolder.class, R.layout.contact_header_group, new GroupValue());
        }
        if (showChannel) {
            addHeaderViewHolder(ChannelViewHolder.class, R.layout.contact_header_channel, new HeaderValue());
        }
//        if (!TextUtils.isEmpty(Config.ORG_SERVER_ADDRESS)) {
//            organizationServiceViewModel.rootOrganizationLiveData().observe(this, new Observer<List<Organization>>() {
//                @Override
//                public void onChanged(List<Organization> organizations) {
//                    if (isRootOrganizationLoaded) {
//                        return;
//                    }
//                    isRootOrganizationLoaded = true;
//                    if (!organizations.isEmpty()) {
//                        for (Organization org : organizations) {
//                            OrganizationValue value = new OrganizationValue();
//                            value.setValue(org);
//                            addHeaderViewHolder(OrganizationViewHolder.class, R.layout.contact_header_organization, value);
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
//                            OrganizationValue value = new OrganizationValue();
//                            value.setValue(org);
//                            addHeaderViewHolder(DepartViewHolder.class, R.layout.contact_header_department, value);
//                        }
//                    }
//
//                }
//            });
//        }
    }

    @Override
    public void initFooterViewHolders() {
//        addFooterViewHolder(ContactCountViewHolder.class, R.layout.contact_item_footer, new ContactCountFooterValue());
    }

    @Override
    public void onUserClick(UIUserInfo userInfo) {
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
//        Intent intent = new Intent(getContext(), ConversationActivity.class);
//        intent.putExtra("conversation", conversation);
//        startActivity(intent);
        YeHuoCommonBridge.getInstance().setYeHuoQiaoJie(getContext(),"danliaogengduo",userInfo.getUserInfo().uid,"",null);
    }

    @Override
    public void onHeaderClick(HeaderViewHolder holder) {
        if (pick) {
            showChannelList();
            return;
        }
        if (holder instanceof FriendRequestViewHolder) {
            showFriendRequest();
        } else if (holder instanceof GroupViewHolder) {
            showGroupList();
        } else if (holder instanceof ChannelViewHolder) {
            showChannelList();
        }
    }

    private void showFriendRequest() {
//        FriendRequestValue value = new FriendRequestValue(0);
//        userListAdapter.updateHeader(0, value);
//
//        Intent intent = new Intent(getActivity(), FriendRequestListActivity.class);
//        startActivity(intent);
        YeHuoCommonBridge.getInstance().setYeHuoQiaoJie(getContext(),"xindepengyou","","",null);

    }

    private void showGroupList() {
//        Intent intent = new Intent(getActivity(), GroupListActivity.class);
//        startActivity(intent);
        YeHuoCommonBridge.getInstance().setYeHuoQiaoJie(getContext(),"ruqunyaoqing","","",null);
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
        YeHuoCommonBridge.getInstance().setYeHuoQiaoJie(getContext(),"qunliaoliebiao","","",null);
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
