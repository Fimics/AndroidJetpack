package com.mic.guide.module.music.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mic.guide.module.music.databinding.ItemMusicBinding
import com.mic.guide.module.music.domain.model.Track

/** 歌单列表 Adapter（ViewBinding，两行：标题 + 歌手）。 */
class MusicAdapter : RecyclerView.Adapter<MusicAdapter.TrackViewHolder>() {

    private val items = mutableListOf<Track>()

    fun submit(list: List<Track>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val binding = ItemMusicBinding.inflate(
            LayoutInflater.from(parent.context), parent, false,
        )
        return TrackViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class TrackViewHolder(
        private val binding: ItemMusicBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Track) {
            binding.tvTitle.text = item.title
            binding.tvSubtitle.text = item.artist
        }
    }
}