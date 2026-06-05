package com.mic.guide.module.settings.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mic.guide.module.settings.databinding.ItemSettingBinding
import com.mic.guide.module.settings.domain.model.SettingItem

/** 设置列表 Adapter（ViewBinding，两行：标题 + 摘要）。 */
class SettingsAdapter : RecyclerView.Adapter<SettingsAdapter.SettingViewHolder>() {

    private val items = mutableListOf<SettingItem>()

    fun submit(list: List<SettingItem>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SettingViewHolder {
        val binding = ItemSettingBinding.inflate(
            LayoutInflater.from(parent.context), parent, false,
        )
        return SettingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SettingViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class SettingViewHolder(
        private val binding: ItemSettingBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SettingItem) {
            binding.tvTitle.text = item.title
            binding.tvSummary.text = item.summary
        }
    }
}