package com.example.snapmemo.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.snapmemo.R
import com.example.snapmemo.data.local.db.entity.OutboxEntry
import com.example.snapmemo.data.local.db.entity.OutboxOperation
import com.example.snapmemo.data.local.db.entity.OutboxStatus
import com.example.snapmemo.databinding.ItemSyncEntryBinding

class SyncEntryListAdapter(
    private val onRetry: (Long) -> Unit,
    private val onCancel: (Long) -> Unit
) : ListAdapter<OutboxEntry, SyncEntryListAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSyncEntryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemSyncEntryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(entry: OutboxEntry) {
            binding.tvOperation.text = when (entry.operation) {
                OutboxOperation.CREATE -> "创建"
                OutboxOperation.UPDATE -> "更新"
                OutboxOperation.DELETE -> "删除"
            }

            binding.tvEntitySummary.text = "${entry.entityType.name}: ${entry.entityId.take(8)}..."

            val (iconRes, color) = when (entry.status) {
                OutboxStatus.PENDING -> R.drawable.ic_sync_pending to R.color.sync_pending
                OutboxStatus.PROCESSING -> R.drawable.ic_sync to R.color.sync_pending
                OutboxStatus.COMPLETED -> R.drawable.ic_sync_success to R.color.sync_success
                OutboxStatus.FAILED -> R.drawable.ic_sync_failed to R.color.sync_failed
                OutboxStatus.CANCELLED -> R.drawable.ic_sync_failed to R.color.text_secondary
            }
            binding.ivStatus.setImageResource(iconRes)

            if (entry.retryCount > 0) {
                binding.tvRetryCount.visibility = View.VISIBLE
                binding.tvRetryCount.text = "重试 ${entry.retryCount}/${entry.maxRetries} 次"
            } else {
                binding.tvRetryCount.visibility = View.GONE
            }

            if (entry.lastError != null) {
                binding.tvLastError.visibility = View.VISIBLE
                binding.tvLastError.text = entry.lastError
            } else {
                binding.tvLastError.visibility = View.GONE
            }

            binding.btnRetry.visibility =
                if (entry.status == OutboxStatus.FAILED) View.VISIBLE else View.GONE
            binding.btnCancel.visibility =
                if (entry.status == OutboxStatus.PENDING || entry.status == OutboxStatus.FAILED) View.VISIBLE else View.GONE

            binding.btnRetry.setOnClickListener { onRetry(entry.id) }
            binding.btnCancel.setOnClickListener { onCancel(entry.id) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<OutboxEntry>() {
        override fun areItemsTheSame(oldItem: OutboxEntry, newItem: OutboxEntry) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: OutboxEntry, newItem: OutboxEntry) =
            oldItem == newItem
    }
}
