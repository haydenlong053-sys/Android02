package io.agora.auikit.model

import java.io.Serializable

data class AUIChatBroadEntity (

    /**
     * chatId : string
     * user : AUIUserThumbnailBroadInfo
     * content: String
     * joined : Boolean
     */
    var user: AUIUserThumbnailBroadInfo?,
    var content: String?,
    var joined: Boolean

): Serializable