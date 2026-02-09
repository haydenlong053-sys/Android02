/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.conversationlist;

import static com.cboda.baselibrary.utils.ELogUtilKt.eLogD;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.cboda.chatui.R;
import com.cboda.chatui.annotation.ConversationContextMenuBroadItem;
import com.cboda.chatui.annotation.EnableContextBroadMenu;
import com.cboda.chatui.conversationlist.notification.StatusBroadNotification;
import com.cboda.chatui.conversationlist.viewholder.ConversationViewBroadHolder;
import com.cboda.chatui.conversationlist.viewholder.ConversationViewHolderBroadManager;
import com.cboda.chatui.conversationlist.viewholder.StatusNotificationContainerViewBroadHolder;

import cn.wildfirechat.model.Conversation;
import cn.wildfirechat.model.ConversationInfo;

public class ConversationListBroadAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Fragment fragment;

    private List<ConversationInfo> conversationInfos = new ArrayList<>();
    private List<StatusBroadNotification> statusNotifications;
    private boolean isSelectMode = false;
    private final Set<String> selectedTargetIds = new HashSet<>();

    public ConversationListBroadAdapter(Fragment context, boolean isSelectMode) {
        super();
        this.fragment = context;
        this.isSelectMode = isSelectMode;
    }


    public void updateSelectMode(boolean isSelectMode) {
        this.isSelectMode = isSelectMode;
        eLogD("xxxx", "isSelectMode" + isSelectMode);
        notifyDataSetChanged();
    }

    public void selectConversationByList(List<ConversationInfo> infos) {
        selectedTargetIds.clear();
        for (int i = 0; i < infos.size(); i++) {
            selectConversation(infos.get(i), i);
        }

    }

    public void selectConversation(ConversationInfo conversationInfo, int position) {
        String targetId = conversationInfo.conversation.target;


        boolean willBeSelected;
        if (selectedTargetIds.contains(targetId)) {
            selectedTargetIds.remove(targetId);
            willBeSelected = false;
        } else {
            selectedTargetIds.add(targetId);
            willBeSelected = true;
        }

        // 修复：不要写 !conversationInfo.isSelect，要写真实状态！
        conversationInfo.isSelect = !willBeSelected;

        notifyItemChanged(position);
    }

    public void cancelSelectAll() {
        closeSelect();
    }

    public void closeSelect() {
        //关闭选择 清空选择
        selectedTargetIds.clear();
        for (ConversationInfo conversationInfo : conversationInfos) {
            conversationInfo.isSelect = false;
        }
        notifyItemRangeChanged(0, conversationInfos.size());
    }

    // 新增：获取选中列表
    public List<ConversationInfo> getSelectInfos() {
        List<ConversationInfo> result = new ArrayList<>();
        for (ConversationInfo info : conversationInfos) {
            if (selectedTargetIds.contains(info.conversation.target)) {
                result.add(info);
            }
        }
        return result;
    }

    private OnClickConversationItemBroadListener onClickConversationItemListener;

    private boolean isEmpty(List list) {
        return list == null || list.isEmpty();
    }

    public void updateStatusNotification(List<StatusBroadNotification> statusNotifications) {
        submit(statusNotifications, this.conversationInfos);
    }

    public int headerCount() {
        return isEmpty(this.statusNotifications) ? 0 : 1;
    }

    public void setConversationInfos(List<ConversationInfo> conversationInfos) {
        submit(this.statusNotifications, conversationInfos);
    }

    public List<ConversationInfo> getData() {
        return conversationInfos;
    }

    public List<String> getConversationUserInfos() {
        List<String> userIds = new ArrayList<>();
        if (!conversationInfos.isEmpty()) {
            for (int i = 0; i < conversationInfos.size(); i++) {
                userIds.add(conversationInfos.get(i).conversation.target);

            }
        }

        return userIds;

    }

    // 辅助方法：根据 userId 找 position
    public int findPositionByUserId(String userId) {
        for (int i = 0; i < conversationInfos.size(); i++) {
            if (conversationInfos.get(i).conversation.target.equals(userId)) {
                return i;
            }
        }
        return -1;
    }

    public void setOnClickConversationItemListener(OnClickConversationItemBroadListener onClickConversationItemListener) {
        this.onClickConversationItemListener = onClickConversationItemListener;
        if (!isSelectMode) {

            notifyDataSetChanged();
        }
    }

    private void submit(List<StatusBroadNotification> notifications, List<ConversationInfo> conversationInfos) {
        this.statusNotifications = notifications;
        this.conversationInfos = conversationInfos;
        if (!isSelectMode) {
            notifyDataSetChanged();
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == R.layout.conversationlist_item_notification_broad_container) {
            View view = LayoutInflater.from(fragment.getContext()).inflate(R.layout.conversationlist_item_notification_broad_container, parent, false);
            return new StatusNotificationContainerViewBroadHolder(view);
        }
        Class<? extends ConversationViewBroadHolder> viewHolderClazz = ConversationViewHolderBroadManager.getInstance().getConversationContentViewHolder(viewType);

        View itemView;
        itemView = LayoutInflater.from(fragment.getContext()).inflate(R.layout.conversationlist_item_broad_conversation, parent, false);

        try {
            Constructor constructor = viewHolderClazz.getConstructor(Fragment.class, RecyclerView.Adapter.class, View.class);
            ConversationViewBroadHolder viewHolder = (ConversationViewBroadHolder) constructor.newInstance(fragment, this, itemView);
            processConversationClick(viewHolder, itemView);
            processConversationLongClick(viewHolderClazz, viewHolder, itemView);
            return viewHolder;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void processConversationClick(ConversationViewBroadHolder viewHolder, View itemView) {
        if (onClickConversationItemListener != null) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = viewHolder.getAdapterPosition();
                    if (position - headerCount() < conversationInfos.size() && position - headerCount() >= 0) {
                        ConversationInfo conversationInfo = conversationInfos.get(position - headerCount());
//                    onClickConversationItemListener.onClickConversationItem(conversationInfo);
                        onClickConversationItemListener.onClickConversationItemNew(conversationInfo, position - headerCount());

                    }
                }
            });
        } else {

            itemView.setOnClickListener(viewHolder::onClick);
        }

    }

    private static class ContextMenuItemWrapper {
        ConversationContextMenuBroadItem contextMenuItem;
        Method method;

        public ContextMenuItemWrapper(ConversationContextMenuBroadItem contextMenuItem, Method method) {
            this.contextMenuItem = contextMenuItem;
            this.method = method;
        }
    }


    private void processConversationLongClick(Class<? extends ConversationViewBroadHolder> viewHolderClazz, ConversationViewBroadHolder viewHolder, View itemView) {
        if (!viewHolderClazz.isAnnotationPresent(EnableContextBroadMenu.class)) {
            return;
        }
        View.OnLongClickListener listener = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (isSelectMode) return false;
                Method[] allMethods = viewHolderClazz.getDeclaredMethods();
                List<ContextMenuItemWrapper> contextMenus = new ArrayList<>();
                for (final Method method : allMethods) {
                    if (method.isAnnotationPresent(ConversationContextMenuBroadItem.class)) {
                        contextMenus.add(new ContextMenuItemWrapper(method.getAnnotation(ConversationContextMenuBroadItem.class), method));
                    }
                }
                // handle annotated method in ConversationViewBroadHolder
                allMethods = ConversationViewBroadHolder.class.getDeclaredMethods();
                for (final Method method : allMethods) {
                    if (method.isAnnotationPresent(ConversationContextMenuBroadItem.class)) {
                        contextMenus.add(new ContextMenuItemWrapper(method.getAnnotation(ConversationContextMenuBroadItem.class), method));
                    }
                }

                if (contextMenus.isEmpty()) {
                    return false;
                }

                int position = viewHolder.getAdapterPosition();
                if (position - headerCount() < conversationInfos.size() && position - headerCount() >= 0) {
                    ConversationInfo conversationInfo = conversationInfos.get(position - headerCount());
                    Iterator<ContextMenuItemWrapper> iterator = contextMenus.iterator();
                    ConversationContextMenuBroadItem item;
                    while (iterator.hasNext()) {
                        item = iterator.next().contextMenuItem;
                        if (viewHolder.contextMenuItemFilter(conversationInfo, item.tag())) {
                            iterator.remove();
                        }
                    }

                    if (contextMenus.isEmpty()) {
                        return false;
                    }
                    Collections.sort(contextMenus, (o1, o2) -> o1.contextMenuItem.priority() - o2.contextMenuItem.priority());
                    List<String> titles = new ArrayList<>(contextMenus.size());
                    for (ContextMenuItemWrapper itemWrapper : contextMenus) {
                        titles.add(viewHolder.contextMenuTitle(fragment.getContext(), itemWrapper.contextMenuItem.tag()));
                    }
                    new MaterialDialog.Builder(fragment.getContext()).items(titles).itemsCallback(new MaterialDialog.ListCallback() {
                        @Override
                        public void onSelection(MaterialDialog dialog, View v, int position, CharSequence text) {
                            try {
                                ContextMenuItemWrapper menuItem = contextMenus.get(position);
                                if (menuItem.contextMenuItem.confirm()) {
                                    String content;
                                    content = viewHolder.contextConfirmPrompt(fragment.getContext(), menuItem.contextMenuItem.tag());
                                    new MaterialDialog.Builder(fragment.getActivity())
                                            .content(content)
                                            .negativeText(fragment.getString(R.string.quxiao))
                                            .positiveText(fragment.getString(R.string.confirm))
                                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                                @Override
                                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                    try {
                                                        menuItem.method.invoke(viewHolder, itemView, conversationInfo);
                                                    } catch (IllegalAccessException e) {
                                                        e.printStackTrace();
                                                    } catch (InvocationTargetException e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            })
                                            .build()
                                            .show();

                                } else {
                                    contextMenus.get(position).method.invoke(viewHolder, itemView, conversationInfo);
                                }
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            } catch (InvocationTargetException e) {
                                e.printStackTrace();
                            }

                        }
                    }).show();
                }
                return true;
            }
        };
        itemView.setOnLongClickListener(listener);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (isStatusNotificationHeader(position)) {
            ((StatusNotificationContainerViewBroadHolder) holder).onBind(fragment, holder.itemView, statusNotifications);
            return;
        }
        ConversationInfo info = conversationInfos.get(position - headerCount());
        ConversationViewBroadHolder cvh = (ConversationViewBroadHolder) holder;

        // 关键修复：每次绑定都强制同步真实选中状态！
        boolean isSelected = selectedTargetIds.contains(info.conversation.target);
        info.isSelect = isSelected;
        cvh.updateSelectedState(isSelected);  // ← 你必须在 ViewHolder 里加这个方法！

        cvh.setSelectMode(isSelectMode);
        cvh.onBind(info, position);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (isStatusNotificationHeader(position)) {
            ((StatusNotificationContainerViewBroadHolder) holder).onBind(fragment, holder.itemView, statusNotifications);
            return;
        }
        super.onBindViewHolder(holder, position, payloads);
    }

    @Override
    public int getItemViewType(int position) {
        if (isStatusNotificationHeader(position)) {
            return R.layout.conversationlist_item_notification_broad_container;
        }
        Conversation conversation = conversationInfos.get(position - headerCount()).conversation;
        return conversation.type.getValue() << 24 | conversation.line;
    }

    @Override
    public int getItemCount() {
        return headerCount() + (conversationInfos == null ? 0 : conversationInfos.size());
    }

    private boolean isStatusNotificationHeader(int position) {
        return position < headerCount();
    }
}
