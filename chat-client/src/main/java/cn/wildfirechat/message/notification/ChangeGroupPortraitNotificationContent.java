/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfirechat.message.notification;

import static com.cboda.baselibrary.utils.ViewExpandUtilKt.getAppContext;

import android.os.Parcel;

import com.blankj.utilcode.util.StringUtils;

import org.json.JSONException;
import org.json.JSONObject;

import cn.wildfirechat.message.Message;
import cn.wildfirechat.message.core.ContentTag;
import cn.wildfirechat.message.core.MessagePayload;
import cn.wildfirechat.message.core.PersistFlag;
import cn.wildfirechat.remote.ChatManager;

import static cn.wildfirechat.message.core.MessageContentBroadType.ContentType_CHANGE_GROUP_PORTRAIT;

/**
 * Created by heavyrainlee on 20/12/2017.
 */

@ContentTag(type = ContentType_CHANGE_GROUP_PORTRAIT, flag = PersistFlag.Persist)
public class ChangeGroupPortraitNotificationContent extends GroupNotificationMessageContent {
    public String operateUser;

    public ChangeGroupPortraitNotificationContent() {
    }

    @Override
    public String formatNotification(Message message) {
        if (fromSelf) {
           return getAppContext(null).getString(com.cboda.baselibrary.R.string.base_group_portrait_updated_by_self);
//            return StringUtils.getString(
//                    com.cboda.baselibrary.R.string.base_group_portrait_updated_by_self
//            );
        } else {
            String operatorName = ChatManager.Instance().getGroupMemberDisplayName(groupId, operateUser);
            return String.format(getAppContext(null).getString(com.cboda.baselibrary.R.string.base_group_portrait_updated_by_other),operatorName);
//            return StringUtils.getString(
//                    com.cboda.baselibrary.R.string.base_group_portrait_updated_by_other,
//                    operatorName
//            );
        }
    }

    @Override
    public MessagePayload encode() {
        MessagePayload payload = super.encode();

        try {
            JSONObject objWrite = new JSONObject();
            objWrite.put("g", groupId);
            objWrite.put("o", operateUser);
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
                operateUser = jsonObject.optString("o");
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
        dest.writeString(this.operateUser);
    }

    protected ChangeGroupPortraitNotificationContent(Parcel in) {
        super(in);
        this.operateUser = in.readString();
    }

    public static final Creator<ChangeGroupPortraitNotificationContent> CREATOR = new Creator<ChangeGroupPortraitNotificationContent>() {
        @Override
        public ChangeGroupPortraitNotificationContent createFromParcel(Parcel source) {
            return new ChangeGroupPortraitNotificationContent(source);
        }

        @Override
        public ChangeGroupPortraitNotificationContent[] newArray(int size) {
            return new ChangeGroupPortraitNotificationContent[size];
        }
    };
}
