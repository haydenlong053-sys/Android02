package com.cboda.chatui.voip.conference;

import static com.cboda.baselibrary.utils.ELogUtilKt.eLogD;

import cn.wildfirechat.model.Conversation;
import cn.wildfirechat.message.Message;
import cn.wildfirechat.remote.ChatManager;
import cn.wildfirechat.remote.GeneralCallback;

import com.cboda.chatui.voip.conference.message.BarrageContent;

/**
 * 基于野火聊天室的礼物（鲜花 / 鼓掌）弹幕通道。
 * 通过独立聊天室发送 BarrageContent，避免群禁言对礼物的影响。
 */
public class GiftChatRoomHelper {

    private static final String TAG = "GiftChatRoomHelper";

    /**
     * 礼物聊天室 ID 规则：gift_ + groupId
     */
    public static String getGiftChatRoomId(String groupId) {
        return "cr" + groupId;
    }

    public static void joinGiftChatRoom(String groupId) {
        String roomId = getGiftChatRoomId(groupId);
        ChatManager.Instance().joinChatRoom(roomId, new GeneralCallback() {
            @Override
            public void onSuccess() {
                eLogD(TAG, "join gift chatroom success, roomId=" + roomId);
            }

            @Override
            public void onFail(int errorCode) {
                eLogD(TAG, "join gift chatroom fail, roomId=" + roomId + ", code=" + errorCode);
            }
        });
    }

    public static void quitGiftChatRoom(String groupId) {
        String roomId = getGiftChatRoomId(groupId);
        ChatManager.Instance().quitChatRoom(roomId, new GeneralCallback() {
            @Override
            public void onSuccess() {
                eLogD(TAG, "quit gift chatroom success, roomId=" + roomId);
            }

            @Override
            public void onFail(int errorCode) {
                eLogD(TAG, "quit gift chatroom fail, roomId=" + roomId + ", code=" + errorCode);
            }
        });
    }

    /**
     * 向礼物聊天室发送礼物弹幕（鲜花 / 鼓掌）。
     *
     * @param groupId  会话所属群 ID
     * @param giftType 礼物类型（1=鲜花，2=鼓掌）
     */
    public static void sendGift(String groupId, int giftType) {
        String roomId = getGiftChatRoomId(groupId);
        Conversation conv = new Conversation(Conversation.ConversationType.ChatRoom, roomId, 0);

        BarrageContent bc = new BarrageContent();
        bc.giftType = giftType;
        Message msg = new Message();
        msg.conversation = conv;
        msg.content = bc;
        msg.sender = ChatManager.Instance().getUserId();

        ChatManager.Instance().sendMessage(msg, null);
    }
}

