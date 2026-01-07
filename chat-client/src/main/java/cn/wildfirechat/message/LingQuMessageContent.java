package cn.wildfirechat.message;


import static com.cboda.baselibrary.utils.ELogUtilKt.eLogD;
import static cn.wildfirechat.message.core.MessageContentBroadType.MESSAGE_CONTENT_TYPE_LINGQU;

import android.os.Parcel;


import org.json.JSONException;
import org.json.JSONObject;

import cn.wildfirechat.message.core.ContentTag;
import cn.wildfirechat.message.core.MessagePayload;
import cn.wildfirechat.message.core.PersistFlag;
import cn.wildfirechat.message.notification.GroupNotificationMessageContent;
import cn.wildfirechat.remote.ChatManager;

@ContentTag(type = MESSAGE_CONTENT_TYPE_LINGQU, flag = PersistFlag.Persist_And_Count)
public class LingQuMessageContent extends GroupNotificationMessageContent {
    public String tipType;
    public String sendUserId;
    public String sendUserName;
    public String lingquUserId;
    public String lingquUserName;

    public LingQuMessageContent() {
    }

    public LingQuMessageContent(String tipType, String sendUserId, String sendUserName, String lingquUserId, String lingquUserName) {
        this.tipType = tipType;
        this.sendUserId = sendUserId;
        this.sendUserName = sendUserName;
        this.lingquUserId = lingquUserId;
        this.lingquUserName = lingquUserName;
    }

    @Override
    public MessagePayload encode() {
        MessagePayload payload = super.encode();
        payload.searchableContent = "";
        payload.mentionedType = mentionedType;
        payload.mentionedTargets = mentionedTargets;
        try {
            JSONObject objWrite = new JSONObject();
            objWrite.put("tipType", tipType);
            objWrite.put("sendUserId", sendUserId);
            objWrite.put("sendUserName", sendUserName);
            objWrite.put("lingquUserId", lingquUserId);
            objWrite.put("lingquUserName", lingquUserName);
            payload.content = objWrite.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return payload;
    }

    @Override
    public void decode(MessagePayload payload) {
        try {
            if (payload.content != null) {
                JSONObject jsonObject = new JSONObject(payload.content);
                tipType = jsonObject.optString("tipType");
                sendUserId = jsonObject.optString("sendUserId");
                sendUserName = jsonObject.optString("sendUserName");
                lingquUserId = jsonObject.optString("lingquUserId");
                lingquUserName = jsonObject.optString("lingquUserName");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String formatNotification(Message message) {
        String content = "";
        LingQuMessageContent lqMessage = (LingQuMessageContent) message.content;
        if (lqMessage.sendUserId.equals(ChatManager.Instance().getUserId())) {
            eLogD("formatNotificationClass", "sendUserId:" + lqMessage.sendUserId +",login:"+ChatManager.Instance().getUserId());
            if (message.sender.equals(ChatManager.Instance().getUserId())) {
                content = "你领取了自己的红包";
            } else {
                content = lqMessage.lingquUserName + "领取了你的红包";
            }
        } else {
            eLogD("formatNotificationClass", "lingquUserId:" + lqMessage.lingquUserId);
            if (lqMessage.lingquUserId.equals(ChatManager.Instance().getUserId())) {
                content = ("您领取了" + lqMessage.sendUserName + "红包");
            } else
                content = (lqMessage.lingquUserName + "领取了" + lqMessage.sendUserName + "的红包");
        }
        return content;
    }

    @Override
    public String digest(Message message) {
        String content = "";
        LingQuMessageContent lqMessage = (LingQuMessageContent) message.content;
        if (lqMessage.sendUserId.equals(ChatManager.Instance().getUserId())) {
            eLogD("formatNotificationClass3", "lingquUserName:" + lqMessage.lingquUserName);
            if (message.sender.equals(ChatManager.Instance().getUserId())) {
                content = "你领取了自己的红包";
            } else {
                content = lqMessage.lingquUserName + "领取了你的红包";
            }
        } else {
            eLogD("formatNotificationClass3", "lingquUserId:" + lqMessage.lingquUserId);
            if (lqMessage.lingquUserId.equals(ChatManager.Instance().getUserId())) {
                content = ("您领取了" + lqMessage.sendUserName + "红包");
            } else
                content = (lqMessage.lingquUserName + "领取了" + lqMessage.sendUserName + "的红包");
        }
        return content;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(this.tipType);
        dest.writeString(this.sendUserId);
        dest.writeString(this.sendUserName);
        dest.writeString(this.lingquUserId);
        dest.writeString(this.lingquUserName);

    }

    protected LingQuMessageContent(Parcel in) {
        super(in);
        this.tipType = in.readString();
        this.sendUserId = in.readString();
        this.sendUserName = in.readString();
        this.lingquUserId = in.readString();
        this.lingquUserName = in.readString();
    }

    public static final Creator<LingQuMessageContent> CREATOR = new Creator<LingQuMessageContent>() {
        @Override
        public LingQuMessageContent createFromParcel(Parcel source) {
            return new LingQuMessageContent(source);
        }

        @Override
        public LingQuMessageContent[] newArray(int size) {
            return new LingQuMessageContent[size];
        }
    };
}
