package com.mic.guide.module.chat.ui

import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import com.mic.guide.module.chat.databinding.ItemChatMessageBinding
import com.mic.guide.module.chat.domain.model.Message

/** 聊天消息 Adapter（ViewBinding，按 `fromMe` 左右对齐气泡）。 */
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
            binding.tvAuthor.text = if (item.fromMe) "我" else item.author
            binding.tvContent.text = item.content
            // fromMe 右对齐 + 暖色气泡；对方左对齐 + 灰色气泡
            val gravity = if (item.fromMe) Gravity.END else Gravity.START
            (binding.bubble.layoutParams as FrameLayout.LayoutParams).gravity = gravity
            binding.bubble.setBackgroundColor(if (item.fromMe) 0xFFFFE0B2.toInt() else 0xFFECECEC.toInt())
        }
    }
}