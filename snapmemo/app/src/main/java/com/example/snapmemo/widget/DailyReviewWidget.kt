package com.example.snapmemo.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.snapmemo.MainActivity
import com.example.snapmemo.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DailyReviewWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { id ->
            updateWidget(context, appWidgetManager, id)
        }
    }

    companion object {
        fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            widgetId: Int,
            content: String? = null
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_daily_review)
            val dateStr = SimpleDateFormat("M月d日", Locale.CHINESE).format(Date())
            views.setTextViewText(R.id.tv_widget_date, dateStr)
            views.setTextViewText(
                R.id.tv_widget_content,
                content ?: "今天暂无笔记，点击查看历史"
            )
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context, widgetId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.tv_widget_content, pendingIntent)
            appWidgetManager.updateAppWidget(widgetId, views)
        }
    }
}
