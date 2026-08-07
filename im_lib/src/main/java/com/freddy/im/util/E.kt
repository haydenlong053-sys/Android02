package com.freddy.im.util

import com.freddy.im.signal.ImSignalProtos

object E {
    fun getImConversationType(imConversationType: ImSignalProtos.ConversationType?): Int {
        return if (null != imConversationType && null != imConversationType.number) {
            imConversationType.number
        } else {
            0
        }
    }
}