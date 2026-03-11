package cn.wildfirechat.message;


import static com.cboda.baselibrary.utils.ViewExpandUtilKt.getAppContext;
import static cn.wildfirechat.message.core.MessageContentBroadType.MESSAGE_CONTENT_TYPE_MEET_START;

import android.os.Parcel;

import androidx.core.content.ContextCompat;

import org.json.JSONException;
import org.json.JSONObject;

import cn.wildfirechat.message.core.ContentTag;
import cn.wildfirechat.message.core.MessagePayload;
import cn.wildfirechat.message.core.PersistFlag;
import cn.wildfirechat.message.notification.GroupNotificationMessageContent;

@ContentTag(type = MESSAGE_CONTENT_TYPE_MEET_START, flag = PersistFlag.Persist_And_Count)
public class MeetStartMessageContent extends GroupNotificationMessageContent {
    public MeetStartMessageContent() {
    }


    @Override
    public MessagePayload encode() {
        MessagePayload payload = super.encode();
        payload.searchableContent = "";
        return payload;
    }

    @Override
    public void decode(MessagePayload payload) {
        try {
            if (payload.content != null) {
                JSONObject jsonObject = new JSONObject(payload.content);

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String formatNotification(Message message) {
        String content = "会议中";

        return content;
    }

    @Override
    public String digest(Message message) {
        return ContextCompat.getString(getAppContext(null), com.cboda.baselibrary.R.string.base_digest_meeting);

    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
    }

    protected MeetStartMessageContent(Parcel in) {
        super(in);

    }

    public static final Creator<MeetStartMessageContent> CREATOR = new Creator<MeetStartMessageContent>() {
        @Override
        public MeetStartMessageContent createFromParcel(Parcel source) {
            return new MeetStartMessageContent(source);
        }

        @Override
        public MeetStartMessageContent[] newArray(int size) {
            return new MeetStartMessageContent[size];
        }
    };
}
