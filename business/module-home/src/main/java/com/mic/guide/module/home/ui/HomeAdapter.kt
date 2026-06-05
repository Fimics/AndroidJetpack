package com.mic.guide.module.home.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mic.guide.module.home.databinding.ItemHomeFeedBinding
import com.mic.guide.module.home.domain.model.FeedItem

/** 首页列表 Adapter（ViewBinding，点击回调上抛由 Fragment 处理导航）。 */
class HomeAdapter(
    private val onItemClick: (FeedItem) -> Unit,
) : RecyclerView.Adapter<HomeAdapter.FeedViewHolder>() {

    private val items = mutableListOf<FeedItem>()

    fun submit(list: List<FeedItem>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedViewHolder {
        val binding = ItemHomeFeedBinding.inflate(
            LayoutInflater.from(parent.context), parent, false,
        )
        return FeedViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FeedViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class FeedViewHolder(
        private val binding: ItemHomeFeedBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: FeedItem) {
            binding.tvTitle.text = item.title
            binding.tvSubtitle.text = item.subtitle
            binding.root.setOnClickListener { onItemClick(item) }
        }
    }
}
