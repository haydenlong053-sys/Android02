/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.conversation.ext.core;

import android.content.Context;
import android.content.Intent;
import android.widget.FrameLayout;

import androidx.annotation.IntRange;
import androidx.fragment.app.Fragment;

import com.afollestad.materialdialogs.MaterialDialog;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.cboda.chatui.annotation.ExtContextMenuBroadItem;
import com.cboda.chatui.viewmodel.MessageViewBroadModel;
import com.cboda.chatui.widget.ViewPagerBroadFixed;
import cn.wildfirechat.model.Conversation;

public class ConversationBroadExtension {
    private Context context;
    private Fragment fragment;
    private Conversation conversation;
    private FrameLayout containerLayout;
    private ViewPagerBroadFixed extViewPager;
    private List<ConversationBroadExt> exts;

    private boolean hideOnScroll = true;

    /**
     * @param fragment
     * @param inputContainerLayout 包含整个输入区域的framelayout
     * @param extViewPager         用于展示{@link ConversationExtPageBroadView}, 每个ConversationExtPageView包含8个{@link ConversationBroadExt}
     */
    public ConversationBroadExtension(Fragment fragment, FrameLayout inputContainerLayout, ViewPagerBroadFixed extViewPager) {
        this.fragment = fragment;
        this.context = fragment.getActivity();
        this.containerLayout = inputContainerLayout;
        this.extViewPager = extViewPager;
    }

    private void onConversationExtClick(ConversationBroadExt ext) {
        List<ExtMenuItemWrapper> extMenuItems = new ArrayList<>();
        Method[] allMethods = ext.getClass().getDeclaredMethods();
        for (final Method method : allMethods) {
            if (method.isAnnotationPresent(ExtContextMenuBroadItem.class)) {
                ExtContextMenuBroadItem item = method.getAnnotation(ExtContextMenuBroadItem.class);
                extMenuItems.add(new ExtMenuItemWrapper(item, method));
            }
        }
        if (extMenuItems.size() > 0) {
            if (extMenuItems.size() == 1) {
                try {
                    extMenuItems.get(0).method.invoke(ext, containerLayout, conversation);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            } else {
                List<String> titles = new ArrayList<>(extMenuItems.size());
                for (ExtMenuItemWrapper itemWrapper : extMenuItems) {
                    titles.add(ext.contextMenuTitle(context, itemWrapper.extContextMenuItem.tag()));
                }
                // TODO sort
                new MaterialDialog.Builder(context).items(titles).itemsCallback((dialog, v, position, text) -> {
                    try {
                        extMenuItems.get(position).method.invoke(ext, containerLayout, conversation);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }

                }).show();
            }
        }
    }


    public void bind(MessageViewBroadModel messageViewModel, Conversation conversation) {
        this.conversation = conversation;
        setupExtViewPager(extViewPager);

        for (int i = 0; i < exts.size(); i++) {
            exts.get(i).onBind(fragment, messageViewModel, conversation, this, i);
        }
    }

    public void onDestroy() {
        for (int i = 0; i < exts.size(); i++) {
            exts.get(i).onDestroy();
        }
    }

    private void setupExtViewPager(ViewPagerBroadFixed viewPager) {
        exts = ConversationExtBroadManager.getInstance(viewPager.getContext(),conversation.type).getConversationExts(conversation);
        if (exts.isEmpty()) {
            return;
        }
        viewPager.setAdapter(new ConversationExtPagerBroadAdapter(exts, index -> {
            onConversationExtClick(exts.get(index));
        }));
    }


    public void reset() {
        int childCount = containerLayout.getChildCount();
        // 不删除最下层的layout，最下层是咱们的input panel
        while (--childCount > 0) {
            containerLayout.removeViewAt(childCount);
        }
        hideOnScroll = true;
    }


    public boolean canHideOnScroll() {
        return hideOnScroll;
    }

    public void disableHideOnScroll() {
        this.hideOnScroll = false;
    }

    public static final int REQUEST_CODE_MIN = 0x8000;


    /**
     * 低16位是合法的request code
     * <p>
     * 第15位强制置1，表示从ConversationExtension发起的
     * <p>
     * 第14-7位，共8个位，是{@link ConversationBroadExt}可用所有request code, 即request code只能在0-256
     * <p>
     * 第6-0位，共7个位，是index
     *
     * @param intent
     * @param requestCode
     * @param index
     */
    public void startActivityForResult(Intent intent, @IntRange(from = 0, to = 256) int requestCode, int index) {
        int extRequestCode = (requestCode << 7) | ConversationBroadExtension.REQUEST_CODE_MIN;
        extRequestCode += index;
        fragment.startActivityForResult(intent, extRequestCode);
    }

    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        if(exts == null || exts.isEmpty()){
            return false;
        }
        int index = requestCode & 0x7F;
        ConversationBroadExt conversationExt = exts.get(index);
        if(conversationExt == null){
            return false;
        }
        conversationExt.onActivityResult((requestCode >> 7) & 0xFF, resultCode, data);
        return true;
    }

    private static class ExtMenuItemWrapper {
        ExtContextMenuBroadItem extContextMenuItem;
        Method method;

        ExtMenuItemWrapper(ExtContextMenuBroadItem extContextMenuItem, Method method) {
            this.extContextMenuItem = extContextMenuItem;
            this.method = method;
        }
    }
}
