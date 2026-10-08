package com.cboda.chatui.bridge

import android.content.Context
import android.content.Context.VIBRATOR_SERVICE
import android.os.Vibrator
import com.cboda.baselibrary.utils.eLogD
import com.cboda.baselibrary.utils.toLog
import com.cboda.chatui.quickmeet.MeetCallManager

class TiXingBroadUtils {
    companion object {
        private var vibrator: Vibrator? = null

        const val RING_FOUND = "item_found.mp3"

        //野火消息提醒（仅震动；提示音已全局关闭）
        fun imMsgReminder(context: Context?) {
            try {
                val isCallingState = MeetCallManager.getInstance().isCallingState()
                eLogD("TiXingBroadUtilsClass", "收到信息：$isCallingState")
                if (!isCallingState) {
                    if (vibrator == null) {
                        vibrator = context?.getSystemService(VIBRATOR_SERVICE) as Vibrator
                    }
                    if (vibrator?.hasVibrator() == true) {
                        vibrator?.vibrate(longArrayOf(100, 200), -1)
                    }
                    // 全局关闭消息提示音，不再播放铃声
                }
            } catch (e: Exception) {
                eLogD("野火", "提醒异常--> ${e.toLog()}")
            }
        }
    }

}
