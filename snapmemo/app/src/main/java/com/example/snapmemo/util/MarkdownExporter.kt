package com.example.snapmemo.util

import android.content.Context
import com.example.snapmemo.domain.model.Attachment
import com.example.snapmemo.domain.model.Memo
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MarkdownExporter @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    suspend fun exportToFile(
        memo: Memo,
        attachments: List<Attachment>,
        outputDir: File
    ): File = withContext(Dispatchers.IO) {
        outputDir.mkdirs()
        val filename = sanitizeFilename("${dateFormat.format(memo.createTime.toDate())}_${memo.id.take(8)}.md")
        val file = File(outputDir, filename)
        FileWriter(file).use { writer ->
            writer.write(buildMarkdown(memo, attachments))
        }
        file
    }

    suspend fun exportAllToZip(memos: List<Memo>, outputDir: File): File =
        withContext(Dispatchers.IO) {
            outputDir.mkdirs()
            val zipFile = File(
                outputDir,
                "memoflow_export_${System.currentTimeMillis()}.zip"
            )
            ZipOutputStream(zipFile.outputStream()).use { zos ->
                memos.forEach { memo ->
                    val entryName = sanitizeFilename(
                        "${dateFormat.format(memo.createTime.toDate())}_${memo.id.take(8)}.md"
                    )
                    zos.putNextEntry(ZipEntry(entryName))
                    zos.write(buildMarkdown(memo, emptyList()).toByteArray())
                    zos.closeEntry()
                }
            }
            zipFile
        }

    private fun buildMarkdown(memo: Memo, attachments: List<Attachment>): String {
        return buildString {
            appendLine("# 笔记")
            appendLine()
            appendLine("**创建时间**: ${dateFormat.format(memo.createTime.toDate())}")
            appendLine("**更新时间**: ${dateFormat.format(memo.updateTime.toDate())}")
            if (memo.tags.isNotEmpty()) {
                appendLine("**标签**: ${memo.tags.joinToString(" ") { "#$it" }}")
            }
            appendLine()
            appendLine("---")
            appendLine()
            appendLine(memo.content)
            if (attachments.isNotEmpty()) {
                appendLine()
                appendLine("---")
                appendLine()
                appendLine("## 附件")
                appendLine()
                attachments.forEach { att ->
                    val link = att.remoteUrl ?: att.localPath ?: "（本地文件）"
                    appendLine("- [${att.filename}]($link)")
                }
            }
        }
    }

    private fun sanitizeFilename(name: String): String =
        name.replace(Regex("[\\\\/:*?\"<>|]"), "_")
}

private fun Instant.toDate(): Date = Date.from(this)
