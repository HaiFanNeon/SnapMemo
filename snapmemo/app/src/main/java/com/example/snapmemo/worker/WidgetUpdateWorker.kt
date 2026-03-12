package com.example.snapmemo.worker

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.example.snapmemo.domain.repository.MemoRepository
import com.example.snapmemo.widget.DailyReviewWidget
import com.example.snapmemo.widget.HeatmapWidget
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.firstOrNull
import java.util.concurrent.TimeUnit

@HiltWorker
class WidgetUpdateWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val memoRepository: MemoRepository
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "WidgetUpdateWorker"
        private const val WORK_NAME = "widget_update_periodic"

        fun enqueue(workManager: WorkManager) {
            val request = PeriodicWorkRequestBuilder<WidgetUpdateWorker>(30, TimeUnit.MINUTES)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiresBatteryNotLow(true)
                        .build()
                )
                .build()
            workManager.enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }
    }

    override suspend fun doWork(): Result {
        return try {
            updateDailyReviewWidgets()
            updateHeatmapWidgets()
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Widget update failed", e)
            Result.retry()
        }
    }

    private suspend fun updateDailyReviewWidgets() {
        val manager = AppWidgetManager.getInstance(applicationContext)
        val component = ComponentName(applicationContext, DailyReviewWidget::class.java)
        val widgetIds = manager.getAppWidgetIds(component)
        if (widgetIds.isEmpty()) return

        val todayMemos = memoRepository.getRecentMemos(1).firstOrNull() ?: emptyList()
        val content = todayMemos.firstOrNull()?.content?.take(100) ?: "今天暂无笔记"

        widgetIds.forEach { id ->
            DailyReviewWidget.updateWidget(applicationContext, manager, id, content)
        }
    }

    private fun updateHeatmapWidgets() {
        val manager = AppWidgetManager.getInstance(applicationContext)
        val component = ComponentName(applicationContext, HeatmapWidget::class.java)
        val widgetIds = manager.getAppWidgetIds(component)
        if (widgetIds.isEmpty()) return
        widgetIds.forEach { id ->
            HeatmapWidget.updateWidget(applicationContext, manager, id)
        }
    }
}
