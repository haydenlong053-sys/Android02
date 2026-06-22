package com.freddy.im.util

import android.text.TextUtils
import androidx.appcompat.app.AppCompatActivity
import com.blankj.utilcode.util.ActivityUtils
import com.freddy.im.db.repository.ImSessionRepository
import com.freddy.im.http.GroupVoSessionMapper
import com.freddy.im.http.model.im.GroupVo
import com.freddy.im.http.model.im.SessionItem

//group转session
fun groupToSession(group: GroupVo?): SessionItem? {
    return group?.let {
        if (!TextUtils.isEmpty(group.id)) {
            val existing = ImSessionRepository.getInstance().queryBySessionId(group.id)
            GroupVoSessionMapper.toSessionItem(group, existing, group.id)
        } else {
            null
        }
    }

}
fun getResumeActivity(): AppCompatActivity? {
    return try {
        val activity = ActivityUtils.getTopActivity()
        if (null != activity && activity is AppCompatActivity && !activity.isFinishing && !activity.isDestroyed) activity else null
    } catch (e: Exception) {
        null
    }
}
