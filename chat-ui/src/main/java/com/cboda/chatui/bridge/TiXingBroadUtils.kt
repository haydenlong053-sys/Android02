package com.cboda.chatui.bridge

import android.content.Context
import android.content.Context.VIBRATOR_SERVICE
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Vibrator
import com.cboda.baselibrary.utils.MMKVBroadUtils
import com.cboda.baselibrary.utils.SharePrefBroadBroadUtil
import com.cboda.baselibrary.utils.eLogD
import com.cboda.baselibrary.utils.toLog
import com.cboda.chatui.quickmeet.MeetCallManager

class TiXingBroadUtils {
    companion object {
        private var vibrator: Vibrator? = null
        private var mediaPlayer: MediaPlayer? = null

        const val RING_FOUND = "item_found.mp3"

        //野火消息提醒（声音+震动）
        fun imMsgReminder(context: Context?) {
            try {
                val isCallingState = MeetCallManager.getInstance().isCallingState()
//                eLogD("TiXingBroadUtilsClass", "收到信息：$isCallingState")
                if (!isCallingState) {
//                val ringFileName = SharePrefBroadBroadUtil.getString(
//                    context,
//                    SharePrefBroadBroadUtil.RING_FILE_NAME_KEY,
//                    RING_FOUND
//                )
                    val ringFileName = MMKVBroadUtils.getSqlStrData(SharePrefBroadBroadUtil.RING_FILE_NAME_KEY, RING_FOUND)
                    //震动
                    if (vibrator == null) {
                        vibrator = context?.getSystemService(VIBRATOR_SERVICE) as Vibrator
                    }
                    if (vibrator?.hasVibrator() == true) {
                        vibrator?.vibrate(longArrayOf(100, 200), -1)
                    }

                    if (mediaPlayer != null) {
                        mediaPlayer?.release()
                        mediaPlayer = null
                    }
                    mediaPlayer = MediaPlayer()
                    mediaPlayer?.setAudioStreamType(AudioManager.STREAM_MUSIC)
                    val afd = context?.assets?.openFd(ringFileName)
                    mediaPlayer?.setDataSource(afd?.fileDescriptor, afd!!.startOffset, afd.length)
                    mediaPlayer?.prepare()
                    mediaPlayer?.start()
//                mediaPlayer?.setOnCompletionListener {
//
//                }
                }
            } catch (e: Exception) {
                eLogD("野火", "提醒异常--> ${e.toLog()}")
            }
        }
    }

}