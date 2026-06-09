package com.cboda.intelligent.utils

object SquarePostMediaBroadHelper {

    private val videoExtensions = setOf(
        "mp4", "mov", "m4v", "3gp", "3gpp", "3g2", "wmv", "mkv", "avi", "webm",
    )

    fun isVideoUrl(url: String?): Boolean {
        val path = url?.trim().orEmpty()
        if (path.isEmpty()) return false
        val pathWithoutQuery = path.substringBefore('?').substringBefore('#')
        if (MediaBroadFile.isVideoFileType(pathWithoutQuery)) {
            return true
        }
        val extension = pathWithoutQuery.substringAfterLast('.', "").lowercase()
        return extension in videoExtensions
    }
}
