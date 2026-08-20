package com.translator.offline

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.translator.offline.databinding.ItemLanguageModelBinding

class LanguageAdapter(
    private val items: List<LanguageModelItem>,
    private val onActionClick: (LanguageModelItem) -> Unit
) : RecyclerView.Adapter<LanguageAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemLanguageModelBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLanguageModelBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.tvLangName.text = item.name

        when {
            item.isDownloading -> {
                holder.binding.btnDownloadOrDelete.text = holder.itemView.context.getString(R.string.downloading)
                holder.binding.btnDownloadOrDelete.isEnabled = false
            }
            item.isDownloaded -> {
                holder.binding.btnDownloadOrDelete.text = holder.itemView.context.getString(R.string.delete)
                holder.binding.btnDownloadOrDelete.isEnabled = true
            }
            else -> {
                holder.binding.btnDownloadOrDelete.text = holder.itemView.context.getString(R.string.download)
                holder.binding.btnDownloadOrDelete.isEnabled = true
            }
        }

        holder.binding.btnDownloadOrDelete.setOnClickListener {
            onActionClick(item)
        }
    }

    override fun getItemCount(): Int = items.size
}
