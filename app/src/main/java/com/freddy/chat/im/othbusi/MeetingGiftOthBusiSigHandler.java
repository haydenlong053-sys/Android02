package com.freddy.chat.im.othbusi;

import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.cboda.baselibrary.bean.BarrageBean;
import com.cboda.baselibrary.utils.BaseLiveDataEventKt;
import com.cboda.baselibrary.utils.ELogUtilKt;
import com.freddy.im.signal.ImSignalProtos;
import com.freddy.im.signal.OthBusiSigCodes;
import com.freddy.im.signal.othbusi.OthBusiSigHandler;
import com.freddy.im.signal.othbusi.OthBusiSigJson;

/**
 * 会议礼物弹幕：code={@link OthBusiSigCodes#MEETING_GIFT} / 2001。
 * <p>
 * data 示例：
 * {@code {"type":"1","groupId":"...","fromUserId":"...","giftType":"meeting_gift","toUserId":"...","timestamp":0}}
 * type=1 鲜花，type=2 鼓掌。
 */
public final class MeetingGiftOthBusiSigHandler implements OthBusiSigHandler {

    private static final String TAG = "OthBusiSig.MeetingGift";

    @Override
    public int code() {
        return OthBusiSigCodes.MEETING_GIFT;
    }

    @Override
    public boolean handle(@Nullable String data) {
        String groupId = OthBusiSigJson.readGroupId(data);
        String fromUserId = OthBusiSigJson.readString(data, "fromUserId");
        if (TextUtils.isEmpty(groupId) || TextUtils.isEmpty(fromUserId)) {
            return false;
        }
        int giftType = OthBusiSigJson.readInt(data, "type", 0);
        if (giftType <= 0) {
            String typeStr = OthBusiSigJson.readString(data, "type");
            if (!TextUtils.isEmpty(typeStr)) {
                try {
                    giftType = Integer.parseInt(typeStr.trim());
                } catch (NumberFormatException ignored) {
                    giftType = 0;
                }
            }
        }
        if (giftType <= 0) {
            return false;
        }

        BarrageBean barrageBean = new BarrageBean();
        barrageBean.setSender(fromUserId);
        barrageBean.setConversationId(groupId);
        barrageBean.setConversationType(ImSignalProtos.ConversationType.MULTI_VALUE);
        barrageBean.setGiftType(giftType);
        ELogUtilKt.eLogD(TAG, "groupId=" + groupId + ", from=" + fromUserId + ", type=" + giftType);
        BaseLiveDataEventKt.getUpdateBarrageDataLiveData().postValue(barrageBean);
        return true;
    }
}
