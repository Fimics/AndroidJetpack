package com.mic.guide.module.video.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mic.guide.module.video.databinding.ItemLoadStateBinding

/**
 * Paging 页脚 Adapter（§9.2）：列表底部展示「加载更多(转圈) / 失败(提示 + 重试)」，
 * 与列表 [VideoAdapter] 经 `withLoadStateFooter` 拼接。重试回调上抛由 Fragment 调 `adapter.retry()`。
 */
class VideoLoadStateAdapter(
    private val onRetry: () -> Unit,
) : LoadStateAdapter<VideoLoadStateAdapter.LoadStateViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): LoadStateViewHolder {
        val binding = ItemLoadStateBinding.inflate(
            LayoutInflater.from(parent.context), parent, false,
        )
        return LoadStateViewHolder(binding, onRetry)
    }

    override fun onBindViewHolder(holder: LoadStateViewHolder, loadState: LoadState) {
        holder.bind(loadState)
    }

    class LoadStateViewHolder(
        private val binding: ItemLoadStateBinding,
        onRetry: () -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.btnRetry.setOnClickListener { onRetry() }
        }

        fun bind(loadState: LoadState) {
            binding.progress.isVisible = loadState is LoadState.Loading
            binding.btnRetry.isVisible = loadState is LoadState.Error
            binding.tvError.isVisible = loadState is LoadState.Error
        }
    }
}