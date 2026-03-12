package com.example.snapmemo.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import android.view.ViewGroup
import android.widget.RemoteViews
import com.example.snapmemo.R
import com.example.snapmemo.ui.customview.HeatmapCalendarView

class HeatmapWidget : AppWidgetProvider() {

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
            widgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_heatmap)

            // 1. 创建自定义 View 实例
            val heatmapView = HeatmapCalendarView(context)

            // 2. 获取 Widget 的尺寸（需要处理横竖屏等情况，这里简化处理）
            val options = appWidgetManager.getAppWidgetOptions(widgetId)
            val minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)
            val minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)

            // 将 dp 转换为 px
            val width = minWidth * context.resources.displayMetrics.density.toInt()
            val height = minHeight * context.resources.displayMetrics.density.toInt()

            // 3. 测量并布局自定义 View
            heatmapView.layoutParams = ViewGroup.LayoutParams(width, height)
            heatmapView.measure(
                View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)
            )
            heatmapView.layout(0, 0, width, height)

            // 4. 将 View 绘制到 Bitmap
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            heatmapView.draw(canvas)

            // 5. 将 Bitmap 设置给 ImageView
            views.setImageViewBitmap(R.id.heatmap_image, bitmap)

            appWidgetManager.updateAppWidget(widgetId, views)
        }
    }
}