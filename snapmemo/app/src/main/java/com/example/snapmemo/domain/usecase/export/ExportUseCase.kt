package com.example.snapmemo.domain.usecase.export

import com.example.snapmemo.domain.model.Memo
import com.example.snapmemo.domain.repository.AttachmentRepository
import com.example.snapmemo.domain.repository.MemoRepository
import com.example.snapmemo.util.MarkdownExporter
import kotlinx.coroutines.flow.firstOrNull
import java.io.File
import javax.inject.Inject

class ExportUseCase @Inject constructor(
    private val memoRepository: MemoRepository,
    private val attachmentRepository: AttachmentRepository,
    private val markdownExporter: MarkdownExporter
) {
    /**
     * 导出指定 ID 的笔记为 Markdown 文件
     */
    suspend fun exportMemo(memoId: String, outputDir: File): Result<File> = runCatching {
        val memo = memoRepository.getMemoById(memoId).firstOrNull()
            ?: throw IllegalArgumentException("Memo not found: $memoId")
        val attachments = attachmentRepository.getAttachmentsByMemo(memoId).firstOrNull()
            ?: emptyList()
        markdownExporter.exportToFile(memo, attachments, outputDir)
    }

    /**
     * 导出全部笔记为 ZIP（内含各自的 Markdown 文件）
     */
    suspend fun exportAll(outputDir: File): Result<File> = runCatching {
        val memos = memoRepository.getAllMemosOnce()
        markdownExporter.exportAllToZip(memos, outputDir)
    }
}
