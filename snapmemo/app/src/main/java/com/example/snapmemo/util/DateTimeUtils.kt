package com.example.snapmemo.util

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale


@RequiresApi(Build.VERSION_CODES.O)
object DateTimeUtils {


    private val relativeFormatter = DateTimeFormatter
        .ofPattern("MM-dd HH:mm", Locale.getDefault())
        .withZone(ZoneId.systemDefault())

    private val fullFormatter = DateTimeFormatter
        .ofPattern("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        .withZone(ZoneId.systemDefault())

    fun formatRelative(instant: Instant): String {
        val now = Instant.now()
        val diff = now.epochSecond - instant.epochSecond

        return when {
            diff < 60 -> "刚刚"
            diff < 3600 -> "${diff / 60} 分钟前"
            diff < 86400 -> "${diff / 3600} 小时前"
            diff < 86400 * 7 -> "${diff / 86400} 天前"
            else -> relativeFormatter.format(instant)
        }
    }

    fun formatFull(instant: Instant): String = fullFormatter.format(instant)
}
