package com.cboda.agora.ui.ui.chatList

import java.io.Serializable

data class AUIChatBroadInfo (
    var userId: String,
    var userName: String,
    var content: String?,
    var joined: Boolean
): Serializable