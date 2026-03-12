package com.example.snapmemo.ui.sync

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult

class ConflictResolutionDialogFragment : DialogFragment() {

    companion object {
        const val TAG = "ConflictResolutionDialog"
        const val REQUEST_KEY = "conflict_resolution"
        const val RESULT_ACTION = "action"
        const val ACTION_KEEP_LOCAL = "keep_local"
        const val ACTION_KEEP_REMOTE = "keep_remote"
        const val ACTION_MERGE = "merge"

        private const val ARG_MEMO_ID = "memo_id"
        private const val ARG_LOCAL_CONTENT = "local_content"
        private const val ARG_REMOTE_CONTENT = "remote_content"

        fun newInstance(
            memoId: String,
            localContent: String,
            remoteContent: String
        ) = ConflictResolutionDialogFragment().apply {
            arguments = bundleOf(
                ARG_MEMO_ID to memoId,
                ARG_LOCAL_CONTENT to localContent,
                ARG_REMOTE_CONTENT to remoteContent
            )
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val memoId = arguments?.getString(ARG_MEMO_ID) ?: ""
        val localContent = arguments?.getString(ARG_LOCAL_CONTENT) ?: ""
        val remoteContent = arguments?.getString(ARG_REMOTE_CONTENT) ?: ""

        val localPreview = localContent.take(80).let { if (localContent.length > 80) "$it…" else it }
        val remotePreview = remoteContent.take(80).let { if (remoteContent.length > 80) "$it…" else it }

        val message = """检测到笔记冲突 (ID: $memoId)

📱 本地版本：
$localPreview

☁️ 服务器版本：
$remotePreview

请选择如何处理此冲突："""

        return AlertDialog.Builder(requireContext())
            .setTitle("同步冲突")
            .setMessage(message)
            .setPositiveButton("保留本地") { _, _ ->
                setFragmentResult(
                    REQUEST_KEY,
                    bundleOf(RESULT_ACTION to ACTION_KEEP_LOCAL, ARG_MEMO_ID to memoId)
                )
            }
            .setNegativeButton("使用服务器版本") { _, _ ->
                setFragmentResult(
                    REQUEST_KEY,
                    bundleOf(RESULT_ACTION to ACTION_KEEP_REMOTE, ARG_MEMO_ID to memoId)
                )
            }
            .setNeutralButton("智能合并") { _, _ ->
                setFragmentResult(
                    REQUEST_KEY,
                    bundleOf(RESULT_ACTION to ACTION_MERGE, ARG_MEMO_ID to memoId)
                )
            }
            .create()
    }
}
