package cn.wildfirechat.message;



import static cn.wildfirechat.message.core.MessageContentType.MESSAGE_CONTENT_TYPE_ZHUANGZHANGJIESHOU;

import android.os.Parcel;

import org.json.JSONException;
import org.json.JSONObject;

import cn.wildfirechat.message.core.ContentTag;
import cn.wildfirechat.message.core.MessagePayload;
import cn.wildfirechat.message.core.PersistFlag;

@ContentTag(type = MESSAGE_CONTENT_TYPE_ZHUANGZHANGJIESHOU, flag = PersistFlag.Persist_And_Count)
public class ZhuanZhangJieShouMessageContent extends MessageContent {
    public String zhuanzhangId;
    public String price;
    public String hongbaoDesc;
    public String lingquStatus;

    public ZhuanZhangJieShouMessageContent() {
    }

    public ZhuanZhangJieShouMessageContent(String zhuanzhangId, String price, String hongbaoDesc, String lingquStatus) {
        this.zhuanzhangId = zhuanzhangId;
        this.price = price;
        this.hongbaoDesc = hongbaoDesc;
        this.lingquStatus = lingquStatus;
    }

    @Override
    public MessagePayload encode() {
        MessagePayload payload = super.encode();
        payload.searchableContent = "转账";
        payload.mentionedType = mentionedType;
        payload.mentionedTargets = mentionedTargets;
        try {
            JSONObject objWrite = new JSONObject();
            objWrite.put("zhuanzhangId", zhuanzhangId);
            objWrite.put("price", price);
            objWrite.put("hongbaoDesc", hongbaoDesc);
            objWrite.put("lingquStatus", lingquStatus);
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
                zhuanzhangId = jsonObject.optString("zhuanzhangId");
                price = jsonObject.optString("price");
                hongbaoDesc = jsonObject.optString("hongbaoDesc");
                lingquStatus = jsonObject.optString("lingquStatus");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String digest(Message message) {
        return "[转账]";
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(this.zhuanzhangId);
        dest.writeString(this.price);
        dest.writeString(this.hongbaoDesc);
        dest.writeString(this.lingquStatus);

    }

    protected ZhuanZhangJieShouMessageContent(Parcel in) {
        super(in);
        this.zhuanzhangId = in.readString();
        this.price = in.readString();
        this.hongbaoDesc = in.readString();
        this.lingquStatus = in.readString();
    }

    public static final Creator<ZhuanZhangJieShouMessageContent> CREATOR = new Creator<ZhuanZhangJieShouMessageContent>() {
        @Override
        public ZhuanZhangJieShouMessageContent createFromParcel(Parcel source) {
            return new ZhuanZhangJieShouMessageContent(source);
        }

        @Override
        public ZhuanZhangJieShouMessageContent[] newArray(int size) {
            return new ZhuanZhangJieShouMessageContent[size];
        }
    };
}
