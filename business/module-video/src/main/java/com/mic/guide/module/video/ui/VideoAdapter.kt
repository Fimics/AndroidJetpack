package com.mic.guide.module.video.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.mic.guide.lib.image.ImageLoader
import com.mic.guide.module.video.databinding.ItemVideoBinding
import com.mic.guide.module.video.domain.model.VideoItem

/**
 * 视频列表 [PagingDataAdapter]（Paging 3）：缩略图经 `lib-image` 的 [ImageLoader]（带占位 + 圆角）加载。
 * 点击回调上抛由 Fragment 处理（经 PlayerApi 播放，§6.6）。
 */
class VideoAdapter(
    private val onItemClick: (VideoItem) -> Unit,
) : PagingDataAdapter<VideoItem, VideoAdapter.VideoViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val binding = ItemVideoBinding.inflate(
            LayoutInflater.from(parent.context), parent, false,
        )
        return VideoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }

    inner class VideoViewHolder(
        private val binding: ItemVideoBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: VideoItem) {
            binding.tvTitle.text = item.title
            binding.tvSubtitle.text = item.thumbnail
            ImageLoader.load(binding.ivThumb, item.thumbnail, cornerRadiusDp = 8)
            binding.root.setOnClickListener { onItemClick(item) }
        }
    }

    private companion object {
        val DIFF = object : DiffUtil.ItemCallback<VideoItem>() {
            override fun areItemsTheSame(old: VideoItem, new: VideoItem) = old.id == new.id
            override fun areContentsTheSame(old: VideoItem, new: VideoItem) = old == new
        }
    }
}