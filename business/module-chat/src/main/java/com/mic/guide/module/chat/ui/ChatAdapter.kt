package com.mic.guide.module.chat.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.mic.guide.module.chat.R
import com.mic.guide.module.chat.databinding.ItemChatMessageBinding
import com.mic.guide.module.chat.domain.model.Message

/**
 * 聊天消息 Adapter（ViewBinding）：按 `fromMe` 左右对齐 + 配色。
 *
 * 配色全部走 day/night 颜色资源 + 圆角气泡 drawable（见 res/values{,-night}/colors.xml、
 * drawable/bg_bubble_*），因此浅色/深色模式都正确；左右对齐用 `horizontalBias`。
 */
class ChatAdapter : RecyclerView.Adapter<ChatAdapter.MessageViewHolder>() {

    private val items = mutableListOf<Message>()

    fun submit(list: List<Message>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val binding = ItemChatMessageBinding.inflate(
            LayoutInflater.from(parent.context), parent, false,
        )
        return MessageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class MessageViewHolder(
        private val binding: ItemChatMessageBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Message) {
            val ctx = binding.root.context
            binding.tvAuthor.text = if (item.fromMe) "我" else item.author
            binding.tvContent.text = item.content

            // 左右对齐（ConstraintLayout 用 horizontalBias；重设 layoutParams 触发重新布局）
            val lp = binding.bubble.layoutParams as ConstraintLayout.LayoutParams
            lp.horizontalBias = if (item.fromMe) 1f else 0f
            binding.bubble.layoutParams = lp

            // 气泡背景 + 文字颜色：均为 day/night 资源，深浅模式自动适配（气泡 drawable 自带 padding）
            if (item.fromMe) {
                binding.bubble.setBackgroundResource(R.drawable.bg_bubble_me)
                binding.tvAuthor.setTextColor(ctx.getColor(R.color.chat_text_on_me))
                binding.tvContent.setTextColor(ctx.getColor(R.color.chat_text_on_me))
            } else {
                binding.bubble.setBackgroundResource(R.drawable.bg_bubble_other)
                binding.tvAuthor.setTextColor(ctx.getColor(R.color.chat_text_secondary))
                binding.tvContent.setTextColor(ctx.getColor(R.color.chat_text_primary))
            }
        }
    }
}