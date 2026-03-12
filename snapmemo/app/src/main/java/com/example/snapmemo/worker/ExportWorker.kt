package com.example.snapmemo.worker

import android.content.Context
import android.os.Environment
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.example.snapmemo.domain.usecase.export.ExportUseCase
import com.example.snapmemo.util.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.io.File

@HiltWorker
class ExportWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val exportUseCase: ExportUseCase,
    private val notificationHelper: NotificationHelper
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "ExportWorker"
        const val KEY_MEMO_ID = "memo_id"
        const val KEY_EXPORT_ALL = "export_all"

        fun buildRequest(memoId: String? = null, exportAll: Boolean = false): OneTimeWorkRequest {
            val data = Data.Builder()
                .apply {
                    memoId?.let { putString(KEY_MEMO_ID, it) }
                    putBoolean(KEY_EXPORT_ALL, exportAll)
                }
                .build()
            return OneTimeWorkRequestBuilder<ExportWorker>()
                .setInputData(data)
                .build()
        }
    }

    override suspend fun doWork(): Result {
        val outputDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
            "MemoFlow"
        )
        return try {
            val exportAll = inputData.getBoolean(KEY_EXPORT_ALL, false)
            val memoId = inputData.getString(KEY_MEMO_ID)
            val file = if (exportAll) {
                exportUseCase.exportAll(outputDir).getOrThrow()
            } else {
                requireNotNull(memoId) { "Need memo_id for single export" }
                exportUseCase.exportMemo(memoId, outputDir).getOrThrow()
            }
            notificationHelper.notifyExportComplete(file.absolutePath)
            Result.success(workDataOf("output_path" to file.absolutePath))
        } catch (e: Exception) {
            Log.e(TAG, "Export failed", e)
            Result.failure(workDataOf("error" to e.message))
        }
    }
}
