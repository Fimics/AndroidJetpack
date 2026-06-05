package com.mic.guide.module.home.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mic.guide.lib.image.ImageLoader
import com.mic.guide.module.home.databinding.ItemHomeFeedBinding
import com.mic.guide.module.home.domain.model.FeedItem

/**
 * 首页列表 Adapter（ViewBinding，点击回调上抛由 Fragment 处理导航）。
 * 列表项缩略图经 `lib-image` 的 [ImageLoader]（Glide 门面）加载（posts 无图，按 id 取占位图源）。
 */
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
            // jsonplaceholder posts 无图，按 id 取一张稳定的占位图源演示真实加载（带占位图 + 8dp 圆角）
            ImageLoader.load(binding.ivThumb, "https://picsum.photos/seed/${item.id}/120", cornerRadiusDp = 8)
            binding.root.setOnClickListener { onItemClick(item) }
        }
    }
}
