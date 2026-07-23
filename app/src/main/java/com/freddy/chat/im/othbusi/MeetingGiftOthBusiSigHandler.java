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
 * {@code {"giftType":1,"type":1,"groupId":"...","groupIdNo":"...","fromUserId":"...","toUserId":"...","timestamp":0}}
 * giftType / type：1=鲜花，2=鼓掌。
 * <p>
 * 接收路径：IMSEventListener(OTHER_BUSI) → {@link OthBusiSigHandlers} → 本 Handler → updateBarrageDataLiveData。
 */
public final class MeetingGiftOthBusiSigHandler implements OthBusiSigHandler {

    private static final String TAG = "OthBusiSig.MeetingGift";

    @Override
    public int code() {
        return OthBusiSigCodes.MEETING_GIFT;
    }

    @Override
    public boolean handle(@Nullable String data) {
        String groupId = OthBusiSigJson.readGroupIdNo(data);
        if (TextUtils.isEmpty(groupId)) {
            groupId = OthBusiSigJson.readGroupId(data);
        }
        String fromUserId = OthBusiSigJson.readString(data, "fromUserId");
        String fromUserName = OthBusiSigJson.readString(data, "fromUserName");
        if (TextUtils.isEmpty(fromUserId)) {
            fromUserId = OthBusiSigJson.readString(data, "operator");
        }
        if (TextUtils.isEmpty(groupId)) {
            return false;
        }
        // 优先 giftType（1=鲜花 2=鼓掌），兼容旧字段 type
        int giftType = OthBusiSigJson.readInt(data, "giftType", 0);
        if (giftType <= 0) {
            giftType = OthBusiSigJson.readInt(data, "type", 0);
        }
        if (giftType <= 0) {
            String typeStr = OthBusiSigJson.readString(data, "giftType");
            if (TextUtils.isEmpty(typeStr)) {
                typeStr = OthBusiSigJson.readString(data, "type");
            }
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
        barrageBean.setSender(fromUserId == null ? "" : fromUserId);
        barrageBean.setConversationId(groupId);
        barrageBean.setConversationType(ImSignalProtos.ConversationType.MULTI_VALUE);
        barrageBean.setGiftType(giftType);
        barrageBean.setSendName(fromUserName);
        ELogUtilKt.eLogD(TAG, "groupId=" + groupId + ", from=" + fromUserId + ", giftType=" + giftType);
        BaseLiveDataEventKt.getUpdateBarrageDataLiveData().postValue(barrageBean);
        return true;
    }
}
