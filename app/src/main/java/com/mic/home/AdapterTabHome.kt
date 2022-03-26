package com.mic.home

import androidx.fragment.app.Fragment
import com.mic.indicator.TabBaseAdapter

class AdapterTabHome(fragment: Fragment, arrays: Array<String>) :
    TabBaseAdapter(fragment, arrays) {
    override fun newFragment(position: Int): Fragment {
        when (arrays[position]) {
            "问答" -> return AnswerFragment()
            "热点" -> return HotFragment()
            else -> return TabCommonFragment()
        }
    }

}