/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.conversation.ext.core;

import static com.cboda.baselibrary.utils.ELogUtilKt.eLogD;

import android.content.Context;
import android.text.TextUtils;


import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import com.blankj.utilcode.util.GsonUtils;
import com.cboda.chatui.bridge.IMCommonBroadResult;
import com.cboda.chatui.conversation.ext.AiAntiFraudBroadExt;
import com.cboda.chatui.conversation.ext.CollectBroadExt;
import com.cboda.chatui.conversation.ext.FileBroadExt;
import com.cboda.chatui.conversation.ext.HongBaoBroadExt;
import com.cboda.chatui.conversation.ext.ImageBroadExt;
import com.cboda.chatui.conversation.ext.MeetingBroadExt;
import com.cboda.chatui.conversation.ext.ShootBroadExt;
import com.cboda.chatui.conversation.ext.UserCardBroadExt;
import com.cboda.chatui.conversation.ext.VoipBroadExt;
import com.cboda.chatui.utils.SharePrefBroadBroadUtil;
import com.google.gson.Gson;

import cn.wildfirechat.model.Conversation;

public class ConversationExtBroadManager {
    private static ConversationExtBroadManager instance;
    private final Context context;
    private final Conversation.ConversationType type;
    private List<ConversationBroadExt> conversationExts;

    private ConversationExtBroadManager(Context context, Conversation.ConversationType type) {
        this.context = context;
        this.type = type;
        conversationExts = new ArrayList<>();
        init();
    }

    public static synchronized ConversationExtBroadManager getInstance(Context context, Conversation.ConversationType type) {
//        if (instance == null) {
        instance = new ConversationExtBroadManager(context, type);
//        }
        return instance;
    }

    private Gson gson = GsonUtils.getGson();
    private IMCommonBroadResult imCommonResult;

    private void init() {
        //
        String pingtaipeizhi = SharePrefBroadBroadUtil.getString(context, "pingtaipeizhi", "");
        eLogD("野火", "红包转账配置--> " + pingtaipeizhi);
        if (!TextUtils.isEmpty(pingtaipeizhi)) {
            imCommonResult = gson.fromJson(pingtaipeizhi, IMCommonBroadResult.class);
        }
        registerExt(ImageBroadExt.class);
        registerExt(ShootBroadExt.class);
        registerExt(CollectBroadExt.class);
        if (imCommonResult != null && "1".equals(imCommonResult.getTonghua())) {
            registerExt(VoipBroadExt.class);
        }
        registerExt(FileBroadExt.class);

//        if (imCommonResult != null && "1".equals(imCommonResult.getSendadd())) {
//            registerExt(LocationBroadExt.class);
//        }

        if (imCommonResult != null && "1".equals(imCommonResult.getHongbao())) {
            registerExt(HongBaoBroadExt.class);
        }
//暂时去掉转账功能
//        if (imCommonResult != null && "1".equals(imCommonResult.getZhuanzhang()) && type == Conversation.ConversationType.Single) {
//            registerExt(ZhuanZhangBroadExt.class);
//        }

        registerExt(UserCardBroadExt.class);

        //暂时隐藏
        if (type != Conversation.ConversationType.Single) {
            registerExt(MeetingBroadExt.class);
        }

        registerExt(AiAntiFraudBroadExt.class);
    }

    public void registerExt(Class<? extends ConversationBroadExt> clazz) {
        Constructor constructor;
        try {
            constructor = clazz.getConstructor();
            ConversationBroadExt ext = (ConversationBroadExt) constructor.newInstance();
            conversationExts.add(ext);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void unregisterExt(Class<? extends ConversationBroadExt> clazz) {
        // TODO
    }

    public List<ConversationBroadExt> getConversationExts(Conversation conversation) {
        List<ConversationBroadExt> currentExts = new ArrayList<>();
        for (ConversationBroadExt ext : this.conversationExts) {
            if (!ext.filter(conversation)) {
                currentExts.add(ext);
            }
        }
        return currentExts;
    }
}
