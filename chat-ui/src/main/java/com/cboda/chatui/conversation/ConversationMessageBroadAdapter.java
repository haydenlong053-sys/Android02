/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.conversation;

import static com.cboda.baselibrary.utils.ELogUtilKt.eLogD;
import static com.cboda.baselibrary.utils.ELogUtilKt.toLog;
import static com.cboda.baselibrary.utils.GsonUtilsKt.fromJson2;
import static com.cboda.baselibrary.utils.MMKVBroadUtils.USER_REGISTRATION_DAY_SCORE;
import static com.cboda.baselibrary.utils.ViewExpandUtilKt.anyToInt;
import static com.cboda.baselibrary.utils.ViewExpandUtilKt.setBackColor;
import static com.cboda.baselibrary.utils.ViewExpandUtilKt.setTxtColor;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.cboda.baselibrary.bean.GlobalBroadInfo;
import com.cboda.baselibrary.bean.MsgBroadExtra;
import com.cboda.baselibrary.utils.MMKVBroadUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.cboda.baselibrary.view.AppRoundBroadImage;
import com.google.gson.Gson;
import com.cboda.select.picture.utils.ToastBroadUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.cboda.chatui.R;
import com.cboda.chatui.annotation.EnableContextBroadMenu;
import com.cboda.chatui.annotation.MessageContextMenuBroadItem;
import com.cboda.chatui.bridge.ChatPopBroadMenu;
import com.cboda.chatui.bridge.IMCommonBroadResult;
import com.cboda.chatui.bridge.MyCommonBroadBridge;
import com.cboda.chatui.conversation.forward.ForwardBroadActivity;
import com.cboda.chatui.conversation.message.MessageItemBroadView;
import com.cboda.chatui.conversation.message.model.UiBroadMessage;
import com.cboda.chatui.conversation.message.viewholder.ContextableNotificationMessageContentViewBroadHolder;
import com.cboda.chatui.conversation.message.viewholder.LoadingViewBroadHolder;
import com.cboda.chatui.conversation.message.viewholder.MessageContentViewBroadHolder;
import com.cboda.chatui.conversation.message.viewholder.MessageContextMenuItemBroadTags;
import com.cboda.chatui.conversation.message.viewholder.MessageViewHolderBroadManager;
import com.cboda.chatui.conversation.message.viewholder.NormalMessageContentViewBroadHolder;
import com.cboda.chatui.conversation.message.viewholder.NotificationMessageContentViewBroadHolder;
import com.cboda.chatui.utils.SharePrefBroadBroadUtil;
import com.cboda.chatui.viewmodel.MessageViewBroadModel;

import cn.wildfirechat.message.BiaoQingMessageContent;
import cn.wildfirechat.message.CallStartMessageContent;
import cn.wildfirechat.message.ImageMessageContent;
import cn.wildfirechat.message.LocationMessageContent;
import cn.wildfirechat.message.MediaMessageContent;
import cn.wildfirechat.message.Message;
import cn.wildfirechat.message.SoundMessageContent;
import cn.wildfirechat.message.StickerMessageContent;
import cn.wildfirechat.message.TextMessageContent;
import cn.wildfirechat.message.VideoMessageContent;
import cn.wildfirechat.message.core.MessageDirection;
import cn.wildfirechat.model.UserInfo;
import cn.wildfirechat.remote.ChatManager;

import com.cboda.baselibrary.common.BridgeImEnum;
import com.hjq.shape.view.ShapeTextView;

public class ConversationMessageBroadAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private ConversationBroadFragment fragment;

    public static int MODE_NORMAL = 0;
    public static int MODE_CHECKABLE = 1;

    // check or normal
    private int mode;
    private List<UiBroadMessage> messages = new ArrayList<>();
    private Map<String, Long> readEntries;
    private OnPortraitClickListener onPortraitClickListener;
    private OnMessageCheckListener onMessageCheckListener;
    private OnPortraitLongClickListener onPortraitLongClickListener;
    private OnMessageReceiptClickListener onMessageReceiptClickListener;

    long oldestMessageUid = Long.MAX_VALUE;
    long oldestMessageId = Long.MAX_VALUE;
    protected MessageViewBroadModel messageViewModel;
    public String istousu;


    public ConversationMessageBroadAdapter(ConversationBroadFragment fragment) {
        super();
        this.fragment = fragment;
        messageViewModel = new ViewModelProvider(fragment).get(MessageViewBroadModel.class);
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public void clearMessageCheckStatus() {
        if (messages == null) {
            return;
        }
        for (UiBroadMessage message : messages) {
            message.isChecked = false;
        }
    }

    public List<UiBroadMessage> getCheckedMessages() {
        List<UiBroadMessage> checkedMessages = new ArrayList<>();
        if (this.messages != null) {
            for (UiBroadMessage msg : this.messages) {
                if (msg.isChecked) {
                    checkedMessages.add(msg);
                }
            }
        }
        return checkedMessages;
    }

    public List<UiBroadMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<UiBroadMessage> messages) {
        if (messages != null && !messages.isEmpty()) {
            oldestMessageUid = messages.get(0).message.messageUid;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                messages = messages.stream().filter(m -> m.message.messageId != 0).collect(Collectors.toList());
                if (!messages.isEmpty()) {
                    oldestMessageId = messages.get(0).message.messageId;
                }
                this.messages = messages;
            } else {
                for (UiBroadMessage uiMsg : messages) {
                    if (uiMsg.message.messageId != 0) {
                        this.messages.add(uiMsg);
                    }
                }
                if (!this.messages.isEmpty()) {
                    oldestMessageId = this.messages.get(0).message.messageId;
                }
            }
        } else {
            this.messages = new ArrayList<>();
        }
    }

    public void setReadEntries(Map<String, Long> readEntries) {
        // TODO diff
        this.readEntries = readEntries;
        notifyDataSetChanged();
    }

    public Map<String, Long> getReadEntries() {
        return readEntries;
    }

    public void setOnPortraitClickListener(OnPortraitClickListener onPortraitClickListener) {
        this.onPortraitClickListener = onPortraitClickListener;
    }

    public void setOnMessageCheckListener(OnMessageCheckListener onMessageCheckListener) {
        this.onMessageCheckListener = onMessageCheckListener;
    }

    public void setOnPortraitLongClickListener(OnPortraitLongClickListener onPortraitLongClickListener) {
        this.onPortraitLongClickListener = onPortraitLongClickListener;
    }

    public void setOnMessageReceiptClickListener(OnMessageReceiptClickListener onMessageReceiptClickListener) {
        this.onMessageReceiptClickListener = onMessageReceiptClickListener;
    }

    public void addNewMessage(UiBroadMessage message) {
        if (message == null) {
            return;
        }
        if (contains(message)) {
            updateMessage(message);
            return;
        }
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    public void addMessagesAtHead(List<UiBroadMessage> newMessages) {
        if (newMessages == null || newMessages.isEmpty()) {
            return;
        }
        oldestMessageUid = newMessages.get(0).message.messageUid;
        List<UiBroadMessage> filteredMsgs = new ArrayList<>();
        for (UiBroadMessage m : newMessages) {
            if (m.message.messageId != 0) {
                filteredMsgs.add(m);
            }
        }
        newMessages = filteredMsgs;
        if (newMessages.isEmpty()) {
            return;
        } else {
            oldestMessageId = newMessages.get(0).message.messageId;
        }
        this.messages.addAll(0, newMessages);
        notifyItemRangeInserted(0, newMessages.size());
    }

    public void addMessagesAtTail(List<UiBroadMessage> newMessages) {
        if (newMessages == null || newMessages.isEmpty()) {
            return;
        }
        int insertStartPosition = this.messages.size();
        this.messages.addAll(newMessages);
        notifyItemRangeInserted(insertStartPosition, newMessages.size());
    }

    public void updateMessage(UiBroadMessage message) {
        int index = -1;
        for (int i = messages.size() - 1; i >= 0; i--) {
            if (message.message.messageUid > 0) {
                // 聊天室消息收到的消息
                if (messages.get(i).message.messageUid == message.message.messageUid) {
                    messages.set(i, message);
                    index = i;
                    break;
                }
            }
            if (message.message.messageId > 0) {
                if (messages.get(i).message.messageId == message.message.messageId) {
                    messages.set(i, message);
                    index = i;
                    break;
                }
            }
        }
        if (index > -1) {
            notifyItemChanged(index);
        }
    }

    public void removeMessage(UiBroadMessage message) {
        if (message == null || messages == null || messages.isEmpty()) {
            return;
        }
        UiBroadMessage msg;
        int position = -1;
        for (int i = 0; i < messages.size(); i++) {
            msg = messages.get(i);

            if (msg.message.messageUid > 0 || message.message.messageUid > 0) {
                if (msg.message.messageUid == message.message.messageUid) {
                    messages.remove(msg);
                    position = i;
                    break;
                }
            } else {
                if (msg.message.messageId == message.message.messageId) {
                    messages.remove(msg);
                    position = i;
                    break;
                }
            }
        }
        if (position >= 0) {
            notifyItemRemoved(position);
        }
    }

    public void removeMessageById(long messageId) {
        int position = -1;
        UiBroadMessage msg;
        for (int i = 0; i < messages.size(); i++) {
            msg = messages.get(i);
            if (msg.message.messageId == messageId) {
                messages.remove(msg);
                position = i;
                break;
            }
        }
        if (position >= 0) {
            notifyItemRemoved(position);
        }
    }

    public void removeMessageByUid(long messageUid) {
        int position = -1;
        UiBroadMessage msg;
        for (int i = 0; i < messages.size(); i++) {
            msg = messages.get(i);
            if (msg.message.messageUid == messageUid) {
                messages.remove(msg);
                position = i;
                break;
            }
        }
        if (position >= 0) {
            notifyItemRemoved(position);
        }
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder instanceof MessageContentViewBroadHolder) {
            ((MessageContentViewBroadHolder) holder).onViewRecycled();
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == R.layout.conversation_item_broad_loading) {
            View view = LayoutInflater.from(fragment.getContext()).inflate(R.layout.conversation_item_broad_loading, parent, false);
            return new LoadingViewBroadHolder(view);
        }

        int direction = viewType >> 24;
        int messageType = viewType & 0x7FFFFF;
        Class<? extends MessageContentViewBroadHolder> viewHolderClazz = MessageViewHolderBroadManager.getInstance().getMessageContentViewHolder(messageType);

        int sendResId = MessageViewHolderBroadManager.getInstance().sendLayoutResId(messageType);
        int receiveResId = MessageViewHolderBroadManager.getInstance().receiveLayoutResId(messageType);

        View itemView;
        ViewStub viewStub;
        if (NotificationMessageContentViewBroadHolder.class.isAssignableFrom(viewHolderClazz)) {
            itemView = LayoutInflater.from(fragment.getContext()).inflate(R.layout.conversation_item_notification_broad_containr, parent, false);
            viewStub = itemView.findViewById(R.id.contentViewStub);
            viewStub.setLayoutResource(direction == 0 ? sendResId : receiveResId);
        } else {
            if (direction == 0) {
                itemView = LayoutInflater.from(fragment.getContext()).inflate(R.layout.conversation_item_message_container_broad_send, parent, false);
                viewStub = itemView.findViewById(R.id.contentViewStub);
                viewStub.setLayoutResource(sendResId);
            } else {
                itemView = LayoutInflater.from(fragment.getContext()).inflate(R.layout.conversation_item_message_container_broad_receive, parent, false);
                viewStub = itemView.findViewById(R.id.contentViewStub);
                viewStub.setLayoutResource(receiveResId);
            }
        }
        try {
            View view = viewStub.inflate();
            if (view instanceof ImageView) {
                ((ImageView) view).setImageDrawable(null);
            }
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("webview")) {
                Toast.makeText(fragment.getContext(), fragment.getString(R.string.message_install_webview), Toast.LENGTH_SHORT).show();
            }
        }

        try {
            Constructor constructor = viewHolderClazz.getConstructor(ConversationBroadFragment.class, RecyclerView.Adapter.class, View.class);
            MessageContentViewBroadHolder viewHolder = (MessageContentViewBroadHolder) constructor.newInstance(fragment, this, itemView);
            if (viewHolder instanceof NotificationMessageContentViewBroadHolder) {
                return viewHolder;
            }
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

    private static class ContextMenuItemWrapper {
        MessageContextMenuBroadItem contextMenuItem;
        Method method;

        public ContextMenuItemWrapper(MessageContextMenuBroadItem contextMenuItem, Method method) {
            this.contextMenuItem = contextMenuItem;
            this.method = method;
        }
    }

    private void setOnLongClickListenerForAllClickableChildView(View view, View.OnLongClickListener listener) {
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                setOnLongClickListenerForAllClickableChildView(((ViewGroup) view).getChildAt(i), listener);
            }
        }
        if (view.isClickable()) {
            view.setOnLongClickListener(listener);
        }
    }

    private void processPortraitClick(MessageContentViewBroadHolder viewHolder, View itemView) {
        itemView.findViewById(R.id.portraitImageView).setOnClickListener(v -> {
            if (onPortraitClickListener != null) {
                int position = viewHolder.getAdapterPosition();
                UiBroadMessage message = getItem(position);
                // FIXME: 2019/2/15 getUserInfo可能返回null
                String keFuId = MMKVBroadUtils.Companion.getKeFuId();
                if (!TextUtils.equals(message.message.sender, keFuId)) {
                    onPortraitClickListener.onPortraitClick(ChatManager.Instance().getUserInfo(message.message.sender, false));
                }
            }
        });
        int position = viewHolder.getAdapterPosition();
        UiBroadMessage message = getItem(position);
        View portraitImageLayout = itemView.findViewById(R.id.portraitImageLayout);
        ShapeTextView portraitTv = itemView.findViewById(R.id.portraitTv);
        ImageView vipMirrorCredit = itemView.findViewById(R.id.vipMirrorCredit);
        AppRoundBroadImage portraitImageView = itemView.findViewById(R.id.portraitImageView);
        GlobalBroadInfo.INSTANCE.queryMyFriendInfo(message.message.sender, null, friendSmailListBean -> {
            if (null != friendSmailListBean) {
                vipMirrorCredit.setVisibility(friendSmailListBean.isVip ? View.VISIBLE : View.GONE);
                if(friendSmailListBean.isVip){
                    portraitImageView.setBorderWidthAndColor(com.cboda.baselibrary.R.color.color_FAD27C, 1f);
                }else{
                    portraitImageView.setBorderWidthAndColor(com.cboda.baselibrary.R.color.transparent, 0f);
                }
                if (friendSmailListBean.isNiceNum) {
                    setBackColor(portraitTv, com.cboda.baselibrary.R.color.black);
                    setTxtColor(portraitTv, com.cboda.baselibrary.R.color.color_FAE77C);
                } else {
                    int registrationDayScore = anyToInt(MMKVBroadUtils.Companion.getSqlStrData(USER_REGISTRATION_DAY_SCORE, ""));
                    int registrationDay = friendSmailListBean.registerDay;
                    if (registrationDay < registrationDayScore) {
                        setBackColor(portraitTv, com.cboda.baselibrary.R.color.color_D5271E);
                    } else {
                        setBackColor(portraitTv, com.cboda.baselibrary.R.color.color_7A66FA);
                    }
                    setTxtColor(portraitTv, com.cboda.baselibrary.R.color.white);
                }
            } else {
                vipMirrorCredit.setVisibility(View.GONE);
                setBackColor(portraitTv, com.cboda.baselibrary.R.color.color_D5271E);
                setTxtColor(portraitTv, com.cboda.baselibrary.R.color.white);
                portraitImageView.setBorderWidthAndColor(com.cboda.baselibrary.R.color.transparent, 0f);
            }
            return null;
        });
        if (message.message.sender.equals(MMKVBroadUtils.Companion.getRealUserId())) {
            if (!TextUtils.isEmpty(message.message.content.extra)) {
                MsgBroadExtra msgExtra = fromJson2(message.message.content.extra, MsgBroadExtra.class);
                if (msgExtra.isMeetingImage().equals("1")) {
                    portraitImageLayout.setVisibility(View.GONE);
                    portraitTv.setVisibility(View.GONE);
                } else {
                    portraitImageLayout.setVisibility(View.VISIBLE);
                    portraitTv.setVisibility(View.VISIBLE);
                    portraitTv.setText(msgExtra.getGrade() + fragment.getString(R.string.message_grade_unit));
                }
            } else if (messageViewModel.getUserGroupData() != null && messageViewModel.getUserGroupData().getGrade() != null) {
                portraitTv.setVisibility(View.VISIBLE);
                portraitTv.setText(messageViewModel.getUserGroupData().getGrade() + fragment.getString(R.string.message_grade_unit));
            } else {
                portraitTv.setVisibility(View.GONE);
            }
        } else {
            TextView joinTimeTv = itemView.findViewById(R.id.joinTimeTv);
            if (!TextUtils.isEmpty(message.message.content.extra)) {
                MsgBroadExtra msgExtra = fromJson2(message.message.content.extra, MsgBroadExtra.class);
                if (msgExtra.isMeetingImage().equals("1")) {
                    portraitImageLayout.setVisibility(View.GONE);
                    portraitTv.setVisibility(View.GONE);
                    joinTimeTv.setVisibility(View.GONE);
                } else {
                    portraitImageLayout.setVisibility(View.VISIBLE);
                    portraitTv.setVisibility(View.VISIBLE);
                    String joinGroupTime = msgExtra.getJoinGroupTime();
                    portraitTv.setText(msgExtra.getGrade() + fragment.getString(R.string.message_grade_unit));
                    joinTimeTv.setVisibility(TextUtils.isEmpty(joinGroupTime) ? View.GONE : View.VISIBLE);
                    try {
                        long timeLong = TimeUtils.string2Millis(joinGroupTime, "yyyy-MM-dd HH:mm:ss");
                        String joinGroupTimeBefore = TimeUtils.millis2String(timeLong, "yyyy-MM-dd");
                        joinTimeTv.setText(joinGroupTimeBefore + fragment.getString(R.string.message_joined_group));
                    } catch (Exception e) {
                        toLog(e, "joinGroupTime");
                        joinTimeTv.setVisibility(View.GONE);
                    }
                }


            } else {
                joinTimeTv.setVisibility(View.GONE);
                portraitTv.setVisibility(View.GONE);
            }
        }

    }

    public void onGroupMessageReceiptClick(Message message) {
        if (onMessageReceiptClickListener != null) {
            onMessageReceiptClickListener.onMessageReceiptCLick(message);
        }
    }


    private void processTousuCheckClick(MessageContentViewBroadHolder holder, View itemView) {
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                UiBroadMessage message = getItem(position);
                message.isSelectTouSu = !message.isSelectTouSu;
                CheckBox tousucheckbox = itemView.findViewById(R.id.tousucheckbox);
                tousucheckbox.setChecked(message.isSelectTouSu);
                notifyItemChanged(position);
            }
        });
    }

    private void processCheckClick(MessageContentViewBroadHolder holder, View itemView) {
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                UiBroadMessage message = getItem(position);
                message.isChecked = !message.isChecked;
                CheckBox checkBox = itemView.findViewById(R.id.checkbox);
                checkBox.setChecked(message.isChecked);
                if (onMessageCheckListener != null) {
                    onMessageCheckListener.onMessageCheck(message, message.isChecked);
                }
                notifyItemChanged(position);
            }
        });
    }

    private void processPortraitLongClick(MessageContentViewBroadHolder viewHolder, View itemView) {
        itemView.findViewById(R.id.portraitImageView).setOnLongClickListener(v -> {
                    if (onPortraitLongClickListener != null) {
                        int position = viewHolder.getAdapterPosition();
                        UiBroadMessage message = getItem(position);
                        onPortraitLongClickListener.onPortraitLongClick(ChatManager.Instance().getUserInfo(message.message.sender, false));
                        return true;
                    }
                    return false;
                }
        );
    }

    /**
     * 和{@link Class#getDeclaredMethods()}类似，但包括父类方法
     *
     * @param clazz
     * @return
     */
    private List<Method> getDeclaredMethodsEx(Class clazz) {
        List<Method> methods = new ArrayList<>();
        if (MessageContentViewBroadHolder.class.isAssignableFrom(clazz)) {
            Method[] m = clazz.getDeclaredMethods();
            methods.addAll(Arrays.asList(m));

            methods.addAll(getDeclaredMethodsEx(clazz.getSuperclass()));
        }
        return methods;
    }

    private Gson gson = new Gson();
    private IMCommonBroadResult imCommonResult;
    private ChatPopBroadMenu mChatPopMenu;
    protected List<ChatPopBroadMenu.ChatPopMenuAction> mPopActions = new ArrayList<>();

    // refer to https://stackoverflow.com/questions/21217397/android-issue-with-onclicklistener-and-onlongclicklistener?noredirect=1&lq=1
    private void processContentLongClick(Class<? extends MessageContentViewBroadHolder> viewHolderClazz, MessageContentViewBroadHolder viewHolder, View itemView) {
        if (!viewHolderClazz.isAnnotationPresent(EnableContextBroadMenu.class)) {
            return;
        }
        View.OnLongClickListener listener = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // 野火长按事件
//                List<Method> allMethods = getDeclaredMethodsEx(viewHolderClazz);
//                List<ContextMenuItemWrapper> contextMenus = new ArrayList<>();
//                for (final Method method : allMethods) {
//                    if (method.isAnnotationPresent(MessageContextMenuBroadItem.class)) {
//                        contextMenus.add(new ContextMenuItemWrapper(method.getAnnotation(MessageContextMenuBroadItem.class), method));
//                    }
//                }
//
//                if (contextMenus.isEmpty()) {
//                    return false;
//                }
                String pingtaipeizhi = SharePrefBroadBroadUtil.getString(v.getContext(), "pingtaipeizhi", "");
                eLogD("野火长按配置", "--> " + pingtaipeizhi);
                if (!TextUtils.isEmpty(pingtaipeizhi)) {
                    imCommonResult = gson.fromJson(pingtaipeizhi, IMCommonBroadResult.class);
                }
                int position = viewHolder.getAdapterPosition();
                UiBroadMessage message = getItem(position);
                Message message1 = message.message;
                boolean canChehui = viewHolder.contextMenuItemFilter(message, MessageContextMenuItemBroadTags.TAG_RECALL);
                eLogD("野火", "长按--> " + message1.direction + "  " + message1.content.getMessageContentType() + "  是否可撤回--> " + canChehui);
                List<String> titles = new ArrayList<>();
                mPopActions.clear();

                switch (message1.content.getMessageContentType()) {
                    case 1:
                        // 文本
                        if (imCommonResult != null && "1".equals(imCommonResult.getOpenBj()) && message1.direction == MessageDirection.Send) {
                            titles.add(fragment.getString(R.string.message_edit));
                            ChatPopBroadMenu.ChatPopMenuAction bianjiAction = new ChatPopBroadMenu.ChatPopMenuAction();
                            bianjiAction.setActionName(fragment.getString(R.string.message_edit));
                            bianjiAction.setActionClickListener(new ChatPopBroadMenu.ChatPopMenuAction.OnClickListener() {
                                @Override
                                public void onClick() {
                                    MyCommonBroadBridge.getInstance().setMyBridgeMethod(v.getContext(), BridgeImEnum.EDIT.getValue(), "", "", message);
                                }
                            });
                            mPopActions.add(bianjiAction);
                        }
                        break;
                    case 3:
                        //图片
                        titles.add(fragment.getString(R.string.message_add));
                        ChatPopBroadMenu.ChatPopMenuAction tianjia = new ChatPopBroadMenu.ChatPopMenuAction();
                        tianjia.setActionName(fragment.getString(R.string.message_add));
                        tianjia.setActionClickListener(new ChatPopBroadMenu.ChatPopMenuAction.OnClickListener() {
                            @Override
                            public void onClick() {
                                MyCommonBroadBridge.getInstance().setMyBridgeMethod(v.getContext(), BridgeImEnum.ADD_ANIMATED_EMOJI.getValue(), ((ImageMessageContent) message.message.content).remoteUrl, "", message);
                            }
                        });
                        mPopActions.add(tianjia);
                        break;
                    case 7:
                        //表情
                        titles.add(fragment.getString(R.string.message_add));
                        ChatPopBroadMenu.ChatPopMenuAction tianjia2 = new ChatPopBroadMenu.ChatPopMenuAction();
                        tianjia2.setActionName(fragment.getString(R.string.message_add));
                        tianjia2.setActionClickListener(new ChatPopBroadMenu.ChatPopMenuAction.OnClickListener() {
                            @Override
                            public void onClick() {
                                MyCommonBroadBridge.getInstance().setMyBridgeMethod(v.getContext(), BridgeImEnum.ADD_ANIMATED_EMOJI.getValue(), ((StickerMessageContent) message.message.content).remoteUrl, "", message);
                            }
                        });
                        mPopActions.add(tianjia2);
                        break;
                    case 2002:
                        //表情
                        titles.add(fragment.getString(R.string.message_add));
                        ChatPopBroadMenu.ChatPopMenuAction tianjia3 = new ChatPopBroadMenu.ChatPopMenuAction();
                        tianjia3.setActionName(fragment.getString(R.string.message_add));
                        tianjia3.setActionClickListener(new ChatPopBroadMenu.ChatPopMenuAction.OnClickListener() {
                            @Override
                            public void onClick() {
                                MyCommonBroadBridge.getInstance().setMyBridgeMethod(v.getContext(), BridgeImEnum.ADD_ANIMATED_EMOJI.getValue(), ((BiaoQingMessageContent) message.message.content).emoji, "", message);
                            }
                        });
                        mPopActions.add(tianjia3);
                        break;
                }

                if (!canChehui) {
                    titles.add(fragment.getString(R.string.message_recall));
                    ChatPopBroadMenu.ChatPopMenuAction cehhui = new ChatPopBroadMenu.ChatPopMenuAction();
                    cehhui.setActionName(fragment.getString(R.string.message_recall));
                    cehhui.setActionClickListener(new ChatPopBroadMenu.ChatPopMenuAction.OnClickListener() {
                        @Override
                        public void onClick() {
                            messageViewModel.recallMessage(message.message);
                        }
                    });
                    mPopActions.add(cehhui);
                }

                if (message1.content.getMessageContentType() == 1) {
                    titles.add(fragment.getString(R.string.message_copy));
                    ChatPopBroadMenu.ChatPopMenuAction fuzhi = new ChatPopBroadMenu.ChatPopMenuAction();
                    fuzhi.setActionName(fragment.getString(R.string.message_copy));
                    fuzhi.setActionClickListener(new ChatPopBroadMenu.ChatPopMenuAction.OnClickListener() {
                        @Override
                        public void onClick() {
                            ClipboardManager clipboardManager = (ClipboardManager) fragment.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                            if (clipboardManager == null) {
                                return;
                            }
                            TextMessageContent content = (TextMessageContent) message.message.content;
                            ClipData clipData = ClipData.newPlainText("messageContent", content.getContent());
                            clipboardManager.setPrimaryClip(clipData);
                            ToastBroadUtils.showToast(fragment.getContext(), fragment.getString(R.string.message_copy_success));
                        }
                    });
                    mPopActions.add(fuzhi);
                }
                boolean isMeetingImage = false;
                if (!TextUtils.isEmpty(message1.content.extra)) {
                    MsgBroadExtra msgExtra = fromJson2(message1.content.extra, MsgBroadExtra.class);
                    if (msgExtra.isMeetingImage().equals("1")) {
                        isMeetingImage = true;
                    }
                }
                if (!isMeetingImage) {
                    titles.add(fragment.getString(R.string.message_select_more));
                    ChatPopBroadMenu.ChatPopMenuAction selectMore = new ChatPopBroadMenu.ChatPopMenuAction();
                    selectMore.setActionName(fragment.getString(R.string.message_select_more));
                    selectMore.setActionClickListener(new ChatPopBroadMenu.ChatPopMenuAction.OnClickListener() {
                        @Override
                        public void onClick() {
                            fragment.toggleMultiMessageMode(message);
                        }
                    });
                    mPopActions.add(selectMore);
                }


                titles.add(fragment.getString(R.string.message_delete));
                ChatPopBroadMenu.ChatPopMenuAction shanchu = new ChatPopBroadMenu.ChatPopMenuAction();
                shanchu.setActionName(fragment.getString(R.string.message_delete));
                shanchu.setActionClickListener(new ChatPopBroadMenu.ChatPopMenuAction.OnClickListener() {
                    @Override
                    public void onClick() {
//                        List<String> items = new ArrayList();
//                        items.add("删除本地消息");
//                        boolean isSuperGroup = false;
//                        if (message.message.conversation.type == Conversation.ConversationType.Group) {
//                            GroupInfo groupInfo = ChatManager.Instance().getGroupInfo(message.message.conversation.target, false);
//                            if (groupInfo != null && groupInfo.superGroup == 1) {
//                                isSuperGroup = true;
//                            }
//                        }
//                        // 超级群组不支持远端删除
//                        if ((message.message.conversation.type == Conversation.ConversationType.Group && !isSuperGroup) || message.message.conversation.type == Conversation.ConversationType.Single) {
//                            items.add("删除远程消息");
//                        } else if (message.message.conversation.type == Conversation.ConversationType.SecretChat) {
//                            items.add("删除自己及对方消息");
//                        }
//
//                        new MaterialDialog.Builder(fragment.getContext())
//                                .items(items)
//                                .itemsCallback(new MaterialDialog.ListCallback() {
//                                    @Override
//                                    public void onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {
//                                        if (position == 0) {
                        messageViewModel.deleteMessage(message.message);
//                                        } else {
//                                            messageViewModel.deleteRemoteMessage(message.message);
//                                        }
//                                    }
//                                })
//                                .show();
                    }
                });
                mPopActions.add(shanchu);


//                titles.add("双向删除");
//                ChatPopBroadMenu.ChatPopMenuAction shanchusx = new ChatPopBroadMenu.ChatPopMenuAction();
//                shanchusx.setActionName("双向删除");
//                shanchusx.setActionClickListener(new ChatPopBroadMenu.ChatPopMenuAction.OnClickListener() {
//                    @Override
//                    public void onClick() {
//                        messageViewModel.deleteRemoteMessage(message.message);
//                    }
//                });
//                mPopActions.add(shanchusx);


                if (message1.content.getMessageContentType() != 2
                        && message1.content.getMessageContentType() != 2000
                        && message1.content.getMessageContentType() != 2001
                        && message1.content.getMessageContentType() != 2002
                        && !(message1.content instanceof CallStartMessageContent)) {
                    titles.add(fragment.getString(R.string.message_forward));

                    ChatPopBroadMenu.ChatPopMenuAction zhuanfa = new ChatPopBroadMenu.ChatPopMenuAction();
                    zhuanfa.setActionName(fragment.getString(R.string.message_forward));
                    zhuanfa.setActionClickListener(new ChatPopBroadMenu.ChatPopMenuAction.OnClickListener() {
                        @Override
                        public void onClick() {
                            Intent intent = new Intent(fragment.getContext(), ForwardBroadActivity.class);
                            intent.putExtra("message", message.message);
                            fragment.startActivity(intent);
                        }
                    });
                    mPopActions.add(zhuanfa);
                }

                // MessageContentBroadType MessageContextMenuItemBroadTags
                if (message1.content.getMessageContentType() == 1
                        || message1.content.getMessageContentType() == 3
//                        || message1.content.getMessageContentType() == 2
                        || message1.content.getMessageContentType() == 6
//                        || message1.content.getMessageContentType() == 4
                ) {
                    if ((message1.content.getMessageContentType() == 3 || message1.content.getMessageContentType() == 6) && !TextUtils.isEmpty(((MediaMessageContent) message1.content).remoteUrl)) {
                        titles.add(fragment.getString(R.string.message_collect));
                        ChatPopBroadMenu.ChatPopMenuAction shoucang = new ChatPopBroadMenu.ChatPopMenuAction();
                        shoucang.setActionName(fragment.getString(R.string.message_collect));
                        shoucang.setActionClickListener(new ChatPopBroadMenu.ChatPopMenuAction.OnClickListener() {
                            @Override
                            public void onClick() {
                                String params2 = "";
                                if (message1.content.getMessageContentType() == 3) {
                                    params2 = ((ImageMessageContent) message1.content).remoteUrl + "|" + "2";
                                } else if (message1.content.getMessageContentType() == 2) {
                                    params2 = ((SoundMessageContent) message1.content).remoteUrl + "|" + "4" + "|" + ((SoundMessageContent) message1.content).getDuration();
                                } else if (message1.content.getMessageContentType() == 6) {
                                    params2 = ((VideoMessageContent) message1.content).remoteUrl + "|" + "3" + "|" + ((VideoMessageContent) message1.content).getDuration();
                                    ;
                                } else if (message1.content.getMessageContentType() == 4) {
                                    params2 = ((LocationMessageContent) message1.content).getTitle() + "|" + "5";
                                }
                                MyCommonBroadBridge.getInstance().setMyBridgeMethod(v.getContext(), BridgeImEnum.COLLECT.getValue(), message1.sender, params2, message);
                            }
                        });
                        mPopActions.add(shoucang);
                    } else if (message1.content.getMessageContentType() == 1) {
                        titles.add(fragment.getString(R.string.message_collect));
                        ChatPopBroadMenu.ChatPopMenuAction shoucang = new ChatPopBroadMenu.ChatPopMenuAction();
                        shoucang.setActionName(fragment.getString(R.string.message_collect));
                        shoucang.setActionClickListener(new ChatPopBroadMenu.ChatPopMenuAction.OnClickListener() {
                            @Override
                            public void onClick() {
                                String params2 = ((TextMessageContent) message1.content).getContent() + "|" + "1";
                                MyCommonBroadBridge.getInstance().setMyBridgeMethod(v.getContext(), BridgeImEnum.COLLECT.getValue(), message1.sender, params2, message);
                            }
                        });
                        mPopActions.add(shoucang);
                    }
                }
                if (message1.content.getMessageContentType() == 1
                        && MMKVBroadUtils.Companion.getUserVip()) {
                    titles.add(fragment.getString(R.string.message_translate));
                    ChatPopBroadMenu.ChatPopMenuAction fanyi = new ChatPopBroadMenu.ChatPopMenuAction();
                    fanyi.setActionName(fragment.getString(R.string.message_translate));
                    fanyi.setActionClickListener(new ChatPopBroadMenu.ChatPopMenuAction.OnClickListener() {
                        @Override
                        public void onClick() {
                            MyCommonBroadBridge.getInstance().setMyBridgeMethod(v.getContext(), BridgeImEnum.TRANSLATE.getValue(), ((TextMessageContent) message1.content).getContent(), "", message);
                        }
                    });
                    mPopActions.add(fanyi);
                }

                if (mChatPopMenu != null) {
                    mChatPopMenu.hide();
                    mChatPopMenu = null;
                }
                mChatPopMenu = new ChatPopBroadMenu(fragment.getContext());
                mChatPopMenu.setChatPopMenuActionList(mPopActions);
                int[] location = new int[2];
                v.getLocationOnScreen(location);

                mChatPopMenu.show(v, location[1]);


//                new MaterialDialog.Builder(fragment.getContext()).items(titles).itemsCallback(new MaterialDialog.ListCallback() {
//                    @Override
//                    public void onSelection(MaterialDialog dialog, View v, int position, CharSequence text) {
//                        Log.e("野火点击", "长按内容--> " + text);
//                        switch (text.toString()) {
//                            case "编辑":
//                                MyCommonBroadBridge.getInstance().setMyQiaoJie(v.getContext(), "bianji", "", "", message);
//                                break;
//                            case "添加":
//                                if (message1.content.getMessageContentType() == 3){
//                                    MyCommonBroadBridge.getInstance().setMyQiaoJie(v.getContext(), "tianjiadonghuabiaoqing", ((ImageMessageContent) message.message.content).remoteUrl, "", message);
//                                } else  if (message1.content.getMessageContentType() == 7){
//                                    MyCommonBroadBridge.getInstance().setMyQiaoJie(v.getContext(), "tianjiadonghuabiaoqing", ((StickerMessageContent) message.message.content).remoteUrl, "", message);
//                                }else  if (message1.content.getMessageContentType() == 2002){
//                                    MyCommonBroadBridge.getInstance().setMyQiaoJie(v.getContext(), "tianjiadonghuabiaoqing", ((BiaoQingMessageContent) message.message.content).emoji, "", message);
//                                }
//                                break;
//                            case "撤回":
//                                messageViewModel.recallMessage(message.message);
//                                break;
//                            case "复制":
//                                ClipboardManager clipboardManager = (ClipboardManager) fragment.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
//                                if (clipboardManager == null) {
//                                    return;
//                                }
//                                TextMessageContent content = (TextMessageContent) message.message.content;
//                                ClipData clipData = ClipData.newPlainText("messageContent", content.getContent());
//                                clipboardManager.setPrimaryClip(clipData);
//                                break;
//                            case "删除":
//                                List<String> items = new ArrayList();
//                                items.add("删除本地消息");
//                                boolean isSuperGroup = false;
//                                if (message.message.conversation.type == Conversation.ConversationType.Group) {
//                                    GroupInfo groupInfo = ChatManager.Instance().getGroupInfo(message.message.conversation.target, false);
//                                    if (groupInfo != null && groupInfo.superGroup == 1) {
//                                        isSuperGroup = true;
//                                    }
//                                }
//                                // 超级群组不支持远端删除
//                                if ((message.message.conversation.type == Conversation.ConversationType.Group && !isSuperGroup) || message.message.conversation.type == Conversation.ConversationType.Single) {
//                                    items.add("删除远程消息");
//                                } else if (message.message.conversation.type == Conversation.ConversationType.SecretChat) {
//                                    items.add("删除自己及对方消息");
//                                }
//
//                                new MaterialDialog.Builder(fragment.getContext())
//                                        .items(items)
//                                        .itemsCallback(new MaterialDialog.ListCallback() {
//                                            @Override
//                                            public void onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {
//                                                if (position == 0) {
//                                                    messageViewModel.deleteMessage(message.message);
//                                                } else {
//                                                    messageViewModel.deleteRemoteMessage(message.message);
//                                                }
//                                            }
//                                        })
//                                        .show();
//                                break;
//                            case "转发":
//                                Intent intent = new Intent(fragment.getContext(), ForwardBroadActivity.class);
//                                intent.putExtra("message", message.message);
//                                fragment.startActivity(intent);
//                                break;
//                            case "收藏":
//                                String params2 = "";
//                                if (message1.content.getMessageContentType() == 1) {
//                                    params2 = ((TextMessageContent)message1.content).getContent()+"|"+"1";
//                                } else if (message1.content.getMessageContentType() == 3) {
//                                    params2 = ((ImageMessageContent)message1.content).remoteUrl+"|"+"2";
//                                } else if (message1.content.getMessageContentType() == 2) {
//                                    params2 = ((SoundMessageContent)message1.content).remoteUrl+"|"+"4";
//                                } else if (message1.content.getMessageContentType() == 6) {
//                                    params2 = ((VideoMessageContent)message1.content).remoteUrl+"|"+"3";
//                                } else if (message1.content.getMessageContentType() == 4) {
//                                    params2 = ((LocationMessageContent)message1.content).getTitle()+"|"+"5";
//                                }
//                                MyCommonBroadBridge.getInstance().setMyQiaoJie(v.getContext(), "shoucang",message1.sender, params2, message);
//                                break;
//                        }
//                    }
//                }).show();

//                Iterator<ContextMenuItemWrapper> iterator = contextMenus.iterator();
//                MessageContextMenuBroadItem item;
//                while (iterator.hasNext()) {
//                    item = iterator.next().contextMenuItem;
//                    if (viewHolder.contextMenuItemFilter(message, item.tag())) {
//                        iterator.remove();
//                    }
//                }
//
//                if (contextMenus.isEmpty()) {
//                    return false;
//                }
//
//                Collections.sort(contextMenus, (o1, o2) -> o1.contextMenuItem.priority() - o2.contextMenuItem.priority());
//                List<String> titles = new ArrayList<>(contextMenus.size());
//                for (ContextMenuItemWrapper itemWrapper : contextMenus) {
//                    titles.add(viewHolder.contextMenuTitle(fragment.getContext(), itemWrapper.contextMenuItem.tag()));
//                }
//                new MaterialDialog.Builder(fragment.getContext()).items(titles).itemsCallback(new MaterialDialog.ListCallback() {
//                    @Override
//                    public void onSelection(MaterialDialog dialog, View v, int position, CharSequence text) {
//                        try {
//                            ContextMenuItemWrapper menuItem = contextMenus.get(position);
//                            if (menuItem.contextMenuItem.confirm()) {
//                                String content;
//                                content = viewHolder.contextConfirmPrompt(fragment.getContext(), menuItem.contextMenuItem.tag());
//                                new MaterialDialog.Builder(fragment.getContext())
//                                        .content(content)
//                                        .negativeText("取消")
//                                        .positiveText("确认")
//                                        .onPositive(new MaterialDialog.SingleButtonCallback() {
//                                            @Override
//                                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
//                                                try {
//                                                    menuItem.method.invoke(viewHolder, itemView, message);
//                                                } catch (IllegalAccessException e) {
//                                                    e.printStackTrace();
//                                                } catch (InvocationTargetException e) {
//                                                    e.printStackTrace();
//                                                }
//                                            }
//                                        })
//                                        .build()
//                                        .show();
//
//                            } else {
//                                contextMenus.get(position).method.invoke(viewHolder, itemView, message);
//                            }
//                        } catch (IllegalAccessException e) {
//                            e.printStackTrace();
//                        } catch (InvocationTargetException e) {
//                            e.printStackTrace();
//                        }
//
//                    }
//                }).show();
                return true;
            }
        };
        View contentLayout = itemView.findViewById(R.id.contentFrameLayout);
        contentLayout.setOnLongClickListener(listener);
        setOnLongClickListenerForAllClickableChildView(contentLayout, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MessageContentViewBroadHolder) {
            MessageContentViewBroadHolder viewHolder = (MessageContentViewBroadHolder) holder;
            ((MessageContentViewBroadHolder) holder).onBind(getItem(position), position);
            MessageItemBroadView itemView = (MessageItemBroadView) holder.itemView;
            CheckBox checkBox = itemView.findViewById(R.id.checkbox);
            if (checkBox != null) {
                if (holder instanceof NotificationMessageContentViewBroadHolder && !(holder instanceof ContextableNotificationMessageContentViewBroadHolder)) {
                    checkBox.setVisibility(View.GONE);
                } else {
                    itemView.setCheckable(getMode() == MODE_CHECKABLE);
                    if (getMode() == MODE_CHECKABLE) {
                        checkBox.setVisibility(View.VISIBLE);
                        UiBroadMessage message = getItem(position);
                        checkBox.setChecked(message.isChecked);
                    } else {
                        checkBox.setVisibility(View.GONE);
                    }
                }
            }

            CheckBox tousucheckbox = itemView.findViewById(R.id.tousucheckbox);
            if (tousucheckbox != null) {
                if ("1".equals(istousu)) {
                    tousucheckbox.setVisibility(View.VISIBLE);
                    UiBroadMessage message = getItem(position);
                    tousucheckbox.setChecked(message.isSelectTouSu);
                    processTousuCheckClick(viewHolder, itemView);
                } else {
                    tousucheckbox.setVisibility(View.GONE);
                }
            }


            if (getMode() == MODE_CHECKABLE) {
                processCheckClick(viewHolder, itemView);
            } else {
                processContentLongClick(viewHolder.getClass(), viewHolder, itemView);
                if (holder instanceof NormalMessageContentViewBroadHolder) {
                    processPortraitClick(viewHolder, itemView);
                    processPortraitLongClick(viewHolder, itemView);
                }
            }
        } else {
            // bottom loading progress bar, do nothing
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);
    }

    // 返回message type + message direction
    @Override
    public int getItemViewType(int position) {
        if (getItem(position) == null) {
            return R.layout.conversation_item_broad_loading;
        }
        Message msg = getItem(position).message;
        return msg.direction.value() << 24 | msg.content.getMessageContentType();
    }

    @Override
    public int getItemCount() {
        return messages == null ? 0 : messages.size();
    }

    public void showLoadingNewMessageProgressBar() {
        if (messages == null) {
            return;
        }

        messages.add(null);
        notifyItemInserted(messages.size() - 1);
    }

    public void dismissLoadingNewMessageProgressBar() {
        if (messages == null || messages.isEmpty() || messages.get(messages.size() - 1) != null) {
            return;
        }
        int position = messages.size() - 1;
        messages.remove(position);
        notifyItemRemoved(position);
    }

    public int getMessagePosition(long messageId) {
        if (messages == null) {
            return -1;
        }
        for (int i = 0; i < messages.size(); i++) {
            if (messages.get(i).message.messageId == messageId) {
                return i;
            }
        }
        return -1;
    }

    public UiBroadMessage getItem(int position) {
        return messages.get(position);
    }

    public void highlightFocusMessage(int position) {
        //野火闪烁
        messages.get(position).isFocus = true;
        notifyItemChanged(position);
    }

    private boolean contains(UiBroadMessage message) {
        for (UiBroadMessage msg : messages) {
            // 消息发送成功之前，messageUid都是0
            if (message.message.messageId > 0) {
                if (msg.message.messageId == message.message.messageId) {
                    return true;
                }
            }
            // 聊天室里面，由于消息不存储，messageId都是0，被远程更新的消息，也会走这儿
            if (message.message.messageUid > 0) {
                if (msg.message.messageUid == message.message.messageUid) {
                    return true;
                }
            }
        }
        return false;
    }

    public interface OnPortraitClickListener {
        void onPortraitClick(UserInfo userInfo);
    }

    public interface OnPortraitLongClickListener {
        void onPortraitLongClick(UserInfo userInfo);
    }

    public interface OnMessageCheckListener {
        void onMessageCheck(UiBroadMessage uiMessage, boolean checked);
    }

    public interface OnMessageReceiptClickListener {
        void onMessageReceiptCLick(Message message);
    }
}
