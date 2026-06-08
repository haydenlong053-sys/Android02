package com.cboda.intelligent.bean

data class Duote(
    val refId: String = "",
    val content: String = "",
    val name: String = "",
    val lostTip: String = "",
    val imageUrls: Array<String>? = null,
)