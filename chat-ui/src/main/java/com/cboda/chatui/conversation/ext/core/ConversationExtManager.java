/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.conversation.ext.core;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import com.cboda.chatui.bridge.IMCommonResult;
import com.cboda.chatui.conversation.ext.CollectExt;
import com.cboda.chatui.conversation.ext.FileExt;
import com.cboda.chatui.conversation.ext.HongBaoExt;
import com.cboda.chatui.conversation.ext.ImageExt;
import com.cboda.chatui.conversation.ext.MeetingExt;
import com.cboda.chatui.conversation.ext.ShootExt;
import com.cboda.chatui.conversation.ext.UserCardExt;
import com.cboda.chatui.conversation.ext.VoipExt;
import com.cboda.chatui.utils.SharePrefBroadUtil;
import cn.wildfirechat.model.Conversation;

public class ConversationExtManager {
    private static ConversationExtManager instance;
    private final Context context;
    private final Conversation.ConversationType type;
    private List<ConversationExt> conversationExts;

    private ConversationExtManager(Context context, Conversation.ConversationType type) {
        this.context = context;
        this.type = type;
        conversationExts = new ArrayList<>();
        init();
    }

    public static synchronized ConversationExtManager getInstance(Context context, Conversation.ConversationType type) {
//        if (instance == null) {
            instance = new ConversationExtManager(context,type);
//        }
        return instance;
    }

    private Gson gson = new Gson();
    private IMCommonResult imCommonResult;

    private void init() {
        //
        String pingtaipeizhi = SharePrefBroadUtil.getString(context, "pingtaipeizhi", "");
        Log.e("野火", "红包转账配置--> " + pingtaipeizhi);
        if (!TextUtils.isEmpty(pingtaipeizhi)) {
            imCommonResult = gson.fromJson(pingtaipeizhi, IMCommonResult.class);
        }
        registerExt(ImageExt.class);
        registerExt(ShootExt.class);
        registerExt(CollectExt.class);
        if (imCommonResult != null && "1".equals(imCommonResult.getTonghua())) {
            registerExt(VoipExt.class);
        }
        registerExt(FileExt.class);

//        if (imCommonResult != null && "1".equals(imCommonResult.getSendadd())) {
//            registerExt(LocationExt.class);
//        }

        if (imCommonResult != null && "1".equals(imCommonResult.getHongbao())) {
            registerExt(HongBaoExt.class);
        }
//暂时去掉转账功能
//        if (imCommonResult != null && "1".equals(imCommonResult.getZhuanzhang()) && type == Conversation.ConversationType.Single) {
//            registerExt(ZhuanZhangExt.class);
//        }

        registerExt(UserCardExt.class);

        if (type != Conversation.ConversationType.Single) {
            registerExt(MeetingExt.class);
        }
    }

    public void registerExt(Class<? extends ConversationExt> clazz) {
        Constructor constructor;
        try {
            constructor = clazz.getConstructor();
            ConversationExt ext = (ConversationExt) constructor.newInstance();
            conversationExts.add(ext);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void unregisterExt(Class<? extends ConversationExt> clazz) {
        // TODO
    }

    public List<ConversationExt> getConversationExts(Conversation conversation) {
        List<ConversationExt> currentExts = new ArrayList<>();
        for (ConversationExt ext : this.conversationExts) {
            if (!ext.filter(conversation)) {
                currentExts.add(ext);
            }
        }
        return currentExts;
    }
}
