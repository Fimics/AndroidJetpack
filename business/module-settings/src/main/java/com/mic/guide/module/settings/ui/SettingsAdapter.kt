package com.mic.guide.module.settings.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.mic.guide.module.settings.databinding.ItemSettingBinding
import com.mic.guide.module.settings.domain.model.SettingItem

/**
 * 设置列表 Adapter（卡片行）：普通项显示「›」并上抛点击；开关项显示 [SwitchMaterial]，
 * 当前状态由 [isChecked] 读取、变更经 [onCheckedChange] 上抛（深色模式即走此路接 SettingsApi）。
 */
class SettingsAdapter(
    private val onItemClick: (SettingItem) -> Unit,
    private val isChecked: (SettingItem) -> Boolean,
    private val onCheckedChange: (SettingItem, Boolean) -> Unit,
) : RecyclerView.Adapter<SettingsAdapter.SettingViewHolder>() {

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
            val ctx = binding.root.context
            binding.ivIcon.setImageResource(item.iconRes)
            binding.tvTitle.text = ctx.getString(item.titleRes)
            if (item.summaryRes != 0) {
                binding.tvSummary.isVisible = true
                binding.tvSummary.text = ctx.getString(item.summaryRes)
            } else {
                binding.tvSummary.isVisible = false
            }

            if (item.isSwitch) {
                binding.switchWidget.isVisible = true
                binding.ivChevron.isVisible = false
                // 先解绑监听再设初值，避免复用/回填触发回调
                binding.switchWidget.setOnCheckedChangeListener(null)
                binding.switchWidget.isChecked = isChecked(item)
                binding.switchWidget.setOnCheckedChangeListener { _, checked ->
                    onCheckedChange(item, checked)
                }
                // 点整行 = 拨动开关
                binding.root.setOnClickListener { binding.switchWidget.toggle() }
            } else {
                binding.switchWidget.isVisible = false
                binding.ivChevron.isVisible = true
                binding.root.setOnClickListener { onItemClick(item) }
            }
        }
    }
}