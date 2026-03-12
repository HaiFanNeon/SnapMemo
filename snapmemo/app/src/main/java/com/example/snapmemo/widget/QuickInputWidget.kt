package com.example.snapmemo.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.snapmemo.MainActivity
import com.example.snapmemo.R

class QuickInputWidget : AppWidgetProvider() {

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
        const val ACTION_QUICK_INPUT = "com.example.snapmemo.QUICK_INPUT"

        fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            widgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_quick_input)
            val intent = Intent(context, MainActivity::class.java).apply {
                action = ACTION_QUICK_INPUT
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            }
            val pendingIntent = PendingIntent.getActivity(
                context, widgetId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btn_quick_add, pendingIntent)
            views.setOnClickPendingIntent(R.id.tv_quick_hint, pendingIntent)
            appWidgetManager.updateAppWidget(widgetId, views)
        }
    }
}
