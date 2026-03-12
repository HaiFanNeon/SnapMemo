package com.example.snapmemo.ui.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.example.snapmemo.R
import com.example.snapmemo.data.local.db.entity.SyncStatus
import com.example.snapmemo.databinding.ItemMemoCardBinding
import com.example.snapmemo.domain.model.Memo
import com.google.android.material.chip.Chip
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class MemoViewHolder(
    private val binding: ItemMemoCardBinding,
    private val onMemoClick: (Memo) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    private val timeFormatter = DateTimeFormatter.ofPattern("MM-dd HH:mm", Locale.getDefault())
        .withZone(ZoneId.systemDefault())

    fun bind(memo: Memo) {
        binding.tvContent.text = memo.snippet?.takeIf { it.isNotBlank() } ?: memo.content
        binding.tvTime.text = timeFormatter.format(memo.displayTime)

        binding.ivPinned.visibility = if (memo.pinned) View.VISIBLE else View.GONE

        setupSyncStatus(memo.syncStatus)
        setupTags(memo.tags)

        binding.root.setOnClickListener { onMemoClick(memo) }
    }

    private fun setupSyncStatus(status: SyncStatus) {
        val (visible, iconRes) = when (status) {
            SyncStatus.SYNCED -> Pair(false, 0)
            SyncStatus.PENDING_CREATE, SyncStatus.PENDING_UPDATE, SyncStatus.PENDING_DELETE ->
                Pair(true, R.drawable.ic_sync_pending)
            SyncStatus.FAILED -> Pair(true, R.drawable.ic_sync_failed)
            SyncStatus.CONFLICT -> Pair(true, R.drawable.ic_sync_failed)
        }
        binding.ivSyncStatus.visibility = if (visible) View.VISIBLE else View.GONE
        if (visible && iconRes != 0) {
            binding.ivSyncStatus.setImageResource(iconRes)
        }
    }

    private fun setupTags(tags: List<String>) {
        binding.chipGroupTags.removeAllViews()
        tags.take(3).forEach { tag ->
            val chip = Chip(binding.root.context).apply {
                text = "#$tag"
                isClickable = false
                setChipBackgroundColorResource(R.color.heatmap_level_1)
                setTextColor(binding.root.context.getColor(R.color.primary))
                textSize = 11f
            }
            binding.chipGroupTags.addView(chip)
        }
        binding.chipGroupTags.visibility =
            if (tags.isEmpty()) View.GONE else View.VISIBLE
    }
}
