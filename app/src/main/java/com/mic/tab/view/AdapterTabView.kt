package com.mic.tab.view

import androidx.fragment.app.Fragment
import com.mic.ui.event.EventFragment
import com.mic.ui.flow.FlowFragment
import com.mic.ui.indicator.TabBaseAdapter
import com.mic.ui.rv.RecyclerFragment
import com.mic.ui.slidecard.SlideCardFragment

class AdapterTabView(fragment: Fragment, arrays: Array<String>) :
    TabBaseAdapter(fragment, arrays) {
    override fun newFragment(position: Int): Fragment {
        return when (arrays[position]) {
            "SlideCard"-> SlideCardFragment()
            "RecyclerView"-> RecyclerFragment()
            "DispatchEvent" -> EventFragment()
            "FlowLayout"-> FlowFragment()
            else -> TabCommonFragment()
        }
    }

}