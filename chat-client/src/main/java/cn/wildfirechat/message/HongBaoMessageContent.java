package cn.wildfirechat.message;


import static cn.wildfirechat.message.core.MessageContentBroadType.MESSAGE_CONTENT_TYPE_HONGBAO;

import android.os.Parcel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cn.wildfirechat.message.core.ContentTag;
import cn.wildfirechat.message.core.MessagePayload;
import cn.wildfirechat.message.core.PersistFlag;

@ContentTag(type = MESSAGE_CONTENT_TYPE_HONGBAO, flag = PersistFlag.Persist_And_Count)
public class HongBaoMessageContent extends MessageContent {
    public String hongbaoId;
    public String price;
    public String hongbaoDesc;
    public String lingquStatus;
    public String hongbaoLeixing;
    public String zhuanshuUserId;
    public String zhuanshuUserName;
    public String zhuanshuUserAvatar;
    public List<String> lqUsers;

    public HongBaoMessageContent() {
    }

    public HongBaoMessageContent(
            String hongbaoId,
            String price,
            String hongbaoDesc,
            String lingquStatus, 
            String hongbaoLeixing,
            String zhuanshuUserId,
            String zhuanshuUserName,
            String zhuanshuUserAvatar,
            List<String> lqUsers
    ) {
        this.hongbaoId = hongbaoId;
        this.price = price;
        this.hongbaoDesc = hongbaoDesc;
        this.lingquStatus = lingquStatus;
        this.hongbaoLeixing = hongbaoLeixing;
        this.zhuanshuUserId = zhuanshuUserId;
        this.zhuanshuUserName = zhuanshuUserName;
        this.zhuanshuUserAvatar = zhuanshuUserAvatar;
        this.lqUsers = lqUsers;
    }

    @Override
    public MessagePayload encode() {
        MessagePayload payload = super.encode();
        payload.searchableContent = "红包";
        payload.mentionedType = mentionedType;
        payload.mentionedTargets = mentionedTargets;
        try {
            JSONObject objWrite = new JSONObject();
            objWrite.put("hongbaoId", hongbaoId);
            objWrite.put("price", price);
            objWrite.put("hongbaoDesc", hongbaoDesc);
            objWrite.put("lingquStatus", lingquStatus);
            objWrite.put("hongbaoLeixing", hongbaoLeixing);
            objWrite.put("zhuanshuUserId", zhuanshuUserId);
            objWrite.put("zhuanshuUserName", zhuanshuUserName);
            objWrite.put("zhuanshuUserAvatar", zhuanshuUserAvatar);
            if (lqUsers != null){
                JSONArray objArray = new JSONArray();
                for (int i = 0; i < lqUsers.size(); i++) {
                    objArray.put(i, lqUsers.get(i));
                }
                objWrite.put("lqUsers", objArray);
            }
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
                hongbaoId = jsonObject.optString("hongbaoId");
                price = jsonObject.optString("price");
                hongbaoDesc = jsonObject.optString("hongbaoDesc");
                lingquStatus = jsonObject.optString("lingquStatus");
                hongbaoLeixing = jsonObject.optString("hongbaoLeixing");
                zhuanshuUserId = jsonObject.optString("zhuanshuUserId");
                zhuanshuUserName = jsonObject.optString("zhuanshuUserName");
                zhuanshuUserAvatar = jsonObject.optString("zhuanshuUserAvatar");
                JSONArray jsonArray =jsonObject.optJSONArray("lqUsers");
                lqUsers = new ArrayList<>();
                if (jsonArray != null) {
                    for (int i = 0; i < jsonArray.length(); i++) {
                        lqUsers.add(jsonArray.getString(i));
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String digest(Message message) {
        return "[红包]";
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(this.hongbaoId);
        dest.writeString(this.price);
        dest.writeString(this.hongbaoDesc);
        dest.writeString(this.lingquStatus);
        dest.writeString(this.hongbaoLeixing);
        dest.writeString(this.zhuanshuUserId);
        dest.writeString(this.zhuanshuUserName);
        dest.writeString(this.zhuanshuUserAvatar);
        dest.writeStringList(this.lqUsers);

    }

    protected HongBaoMessageContent(Parcel in) {
        super(in);
        this.hongbaoId = in.readString();
        this.price = in.readString();
        this.hongbaoDesc = in.readString();
        this.lingquStatus = in.readString();
        this.hongbaoLeixing = in.readString();
        this.zhuanshuUserId = in.readString();
        this.zhuanshuUserName = in.readString();
        this.zhuanshuUserAvatar = in.readString();
        this.lqUsers = in.createStringArrayList();

    }

    public static final Creator<HongBaoMessageContent> CREATOR = new Creator<HongBaoMessageContent>() {
        @Override
        public HongBaoMessageContent createFromParcel(Parcel source) {
            return new HongBaoMessageContent(source);
        }

        @Override
        public HongBaoMessageContent[] newArray(int size) {
            return new HongBaoMessageContent[size];
        }
    };
}
