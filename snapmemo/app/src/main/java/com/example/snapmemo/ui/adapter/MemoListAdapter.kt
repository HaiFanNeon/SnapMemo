package com.example.snapmemo.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.example.snapmemo.databinding.ItemMemoCardBinding
import com.example.snapmemo.domain.model.Memo

class MemoListAdapter(
    private val onMemoClick: (Memo) -> Unit
) : ListAdapter<Memo, MemoViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemoViewHolder {
        val binding = ItemMemoCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return MemoViewHolder(binding, onMemoClick)
    }

    override fun onBindViewHolder(holder: MemoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<Memo>() {
        override fun areItemsTheSame(oldItem: Memo, newItem: Memo): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Memo, newItem: Memo): Boolean =
            oldItem == newItem
    }
}
