package com.example.snapmemo.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.snapmemo.MainActivity
import com.example.snapmemo.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val CHANNEL_SYNC = "sync_channel"
        const val CHANNEL_REMINDER = "reminder_channel"
        const val CHANNEL_EXPORT = "export_channel"

        private const val NOTIFY_SYNC_ID = 1001
        private const val NOTIFY_EXPORT_ID = 1002
    }

    /** 在 Application 启动时调用，注册所有通知渠道 */
    fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannels(
            listOf(
                NotificationChannel(
                    CHANNEL_SYNC, "同步通知",
                    NotificationManager.IMPORTANCE_LOW
                ).apply { description = "笔记云同步完成时的通知" },
                NotificationChannel(
                    CHANNEL_REMINDER, "笔记提醒",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply { description = "笔记定时提醒" },
                NotificationChannel(
                    CHANNEL_EXPORT, "导出通知",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply { description = "笔记导出完成后的通知" }
            )
        )
    }

    fun notifySyncComplete(totalSynced: Int) {
        val intent = Intent(context, MainActivity::class.java)
        val pi = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_SYNC)
            .setSmallIcon(R.drawable.ic_sync_success)
            .setContentTitle("同步完成")
            .setContentText("已同步 $totalSynced 条笔记")
            .setAutoCancel(true)
            .setContentIntent(pi)
            .build()
        NotificationManagerCompat.from(context).notify(NOTIFY_SYNC_ID, notification)
    }

    fun notifySyncFailed(reason: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_SYNC)
            .setSmallIcon(R.drawable.ic_sync_failed)
            .setContentTitle("同步失败")
            .setContentText(reason)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(NOTIFY_SYNC_ID, notification)
    }

    fun notifyMemoReminder(memoId: String, memoContent: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("memo_id", memoId)
        }
        val pi = PendingIntent.getActivity(
            context, memoId.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val preview = memoContent.take(60).let { if (memoContent.length > 60) "$it…" else it }
        val notification = NotificationCompat.Builder(context, CHANNEL_REMINDER)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("笔记提醒")
            .setContentText(preview)
            .setAutoCancel(true)
            .setContentIntent(pi)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        NotificationManagerCompat.from(context).notify(memoId.hashCode(), notification)
    }

    fun notifyExportComplete(filePath: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_EXPORT)
            .setSmallIcon(R.drawable.ic_attachment)
            .setContentTitle("导出完成")
            .setContentText("文件已保存至 $filePath")
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(NOTIFY_EXPORT_ID, notification)
    }
}
