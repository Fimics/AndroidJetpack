package com.mic.tab.video

import androidx.fragment.app.Fragment
import com.mic.ui.indicator.TabBaseAdapter

class AdapterTabVideo(fragment: Fragment, arrays: Array<String>) :
    TabBaseAdapter(fragment, arrays) {
    override fun newFragment(position: Int): Fragment {
        return when (arrays[position]) {

            "hot" -> HotFragment()
            else -> TabCommonFragment()
        }
    }
}