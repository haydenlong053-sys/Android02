package cn.wildfirechat.message;


import static cn.wildfirechat.message.core.MessageContentType.MESSAGE_CONTENT_TYPE_BIAOQIANG;

import android.os.Parcel;

import org.json.JSONException;
import org.json.JSONObject;

import cn.wildfirechat.message.core.ContentTag;
import cn.wildfirechat.message.core.MessagePayload;
import cn.wildfirechat.message.core.PersistFlag;

@ContentTag(type = MESSAGE_CONTENT_TYPE_BIAOQIANG, flag = PersistFlag.Persist_And_Count)
public class BiaoQingMessageContent extends MessageContent {
    public String emoji;

    public BiaoQingMessageContent() {
    }

    public BiaoQingMessageContent(String emoji) {
        this.emoji = emoji;
    }

    @Override
    public MessagePayload encode() {
        MessagePayload payload = super.encode();
        payload.searchableContent = "动态表情";
        payload.mentionedType = mentionedType;
        payload.mentionedTargets = mentionedTargets;
        try {
            JSONObject objWrite = new JSONObject();
            objWrite.put("emoji", emoji);
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
                emoji = jsonObject.optString("emoji");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String digest(Message message) {
        return "[动态表情]";
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(this.emoji);
    }

    protected BiaoQingMessageContent(Parcel in) {
        super(in);
        this.emoji = in.readString();
    }

    public static final Creator<BiaoQingMessageContent> CREATOR = new Creator<BiaoQingMessageContent>() {
        @Override
        public BiaoQingMessageContent createFromParcel(Parcel source) {
            return new BiaoQingMessageContent(source);
        }

        @Override
        public BiaoQingMessageContent[] newArray(int size) {
            return new BiaoQingMessageContent[size];
        }
    };

}
