/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfirechat.message.notification;

import static com.cboda.baselibrary.utils.ViewExpandUtilKt.getAppContext;
import static cn.wildfirechat.message.core.MessageContentBroadType.ContentType_CREATE_GROUP;

import android.os.Parcel;

import androidx.core.content.ContextCompat;

import com.blankj.utilcode.util.StringUtils;

import org.json.JSONException;
import org.json.JSONObject;

import cn.wildfirechat.message.Message;
import cn.wildfirechat.message.core.ContentTag;
import cn.wildfirechat.message.core.MessagePayload;
import cn.wildfirechat.message.core.PersistFlag;
import cn.wildfirechat.remote.ChatManager;

/**
 * Created by heavyrainlee on 20/12/2017.
 */

@ContentTag(type = ContentType_CREATE_GROUP, flag = PersistFlag.Persist)
public class CreateGroupNotificationContent extends GroupNotificationMessageContent {
    public String creator;
    public String groupName;

    public CreateGroupNotificationContent() {
    }

    @Override
    public String formatNotification(Message message) {
        if (fromSelf) {
            return String.format(getAppContext(null).getString(com.cboda.baselibrary.R.string.base_group_created_by_self),groupName);
//            return StringUtils.getString(
//                    com.cboda.baselibrary.R.string.base_group_created_by_self,
//                    groupName
//            );
        } else {
            String creatorName = ChatManager.Instance().getUserDisplayName(creator);
            return String.format(getAppContext(null).getString(com.cboda.baselibrary.R.string.base_group_created_by_other),creatorName,groupName);
//            return StringUtils.getString(
//                    com.cboda.baselibrary.R.string.base_group_created_by_other,
//                    creatorName,
//                    groupName
//            );
        }
    }

    @Override
    public MessagePayload encode() {
        MessagePayload payload = super.encode();

        try {
            JSONObject objWrite = new JSONObject();
            objWrite.put("g", groupId);
            objWrite.put("o", creator);
            objWrite.put("n", groupName);
            payload.binaryContent = objWrite.toString().getBytes();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return payload;
    }

    @Override
    public void decode(MessagePayload payload) {
        try {
            if (payload.binaryContent != null) {
                JSONObject jsonObject = new JSONObject(new String(payload.binaryContent));
                groupId = jsonObject.optString("g");
                creator = jsonObject.optString("o");
                groupName = jsonObject.optString("n");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(this.creator);
        dest.writeString(this.groupName);
    }

    protected CreateGroupNotificationContent(Parcel in) {
        super(in);
        this.creator = in.readString();
        this.groupName = in.readString();
    }

    public static final Creator<CreateGroupNotificationContent> CREATOR = new Creator<CreateGroupNotificationContent>() {
        @Override
        public CreateGroupNotificationContent createFromParcel(Parcel source) {
            return new CreateGroupNotificationContent(source);
        }

        @Override
        public CreateGroupNotificationContent[] newArray(int size) {
            return new CreateGroupNotificationContent[size];
        }
    };
}
