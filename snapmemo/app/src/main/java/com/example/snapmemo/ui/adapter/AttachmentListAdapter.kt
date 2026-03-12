package com.example.snapmemo.ui.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.snapmemo.domain.model.Attachment
import com.example.snapmemo.ui.customview.AttachmentPreviewView

class AttachmentListAdapter(
    private val onAttachmentClick: (Attachment) -> Unit,
    private val onPlayClick: ((Attachment) -> Unit)? = null,
    private val onLongClick: ((Attachment) -> Unit)? = null
) : ListAdapter<Attachment, AttachmentListAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = AttachmentPreviewView(parent.context).apply {
            layoutParams = ViewGroup.LayoutParams(200, 120)
        }
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val previewView: AttachmentPreviewView) :
        RecyclerView.ViewHolder(previewView) {

        fun bind(attachment: Attachment) {
            previewView.bind(attachment, onPlayClick)
            previewView.setOnClickListener { onAttachmentClick(attachment) }
            previewView.setOnLongClickListener {
                onLongClick?.invoke(attachment)
                true
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Attachment>() {
        override fun areItemsTheSame(oldItem: Attachment, newItem: Attachment) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Attachment, newItem: Attachment) =
            oldItem == newItem
    }
}
