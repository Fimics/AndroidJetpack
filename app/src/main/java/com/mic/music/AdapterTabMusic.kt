package com.mic.music

import androidx.fragment.app.Fragment
import com.mic.indicator.TabBaseAdapter

class AdapterTabMusic(fragment: Fragment, arrays: Array<String>) :
    TabBaseAdapter(fragment, arrays) {
    override fun newFragment(position: Int): Fragment {
        when (arrays[position]) {
            "听故事" -> return StoryFragment()
            "唱儿歌" -> return SongFragment()
            else -> return TabCommonFragment()
        }
    }

}