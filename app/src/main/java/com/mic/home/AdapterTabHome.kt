package com.mic.home

import androidx.fragment.app.Fragment
import com.mic.indicator.TabBaseAdapter
import com.mic.jetpack.lifecycle.LifecycleFragment
import com.mic.rx.RxrFragment
import com.mic.ui.event.EventFragment
import com.mic.ui.flow.FlowFragment
import com.mic.ui.rv.RecyclerFragment
import com.mic.ui.slidecard.SlideCardFragment

class AdapterTabHome(fragment: Fragment, arrays: Array<String>) :
    TabBaseAdapter(fragment, arrays) {
    override fun newFragment(position: Int): Fragment {
        return when (arrays[position]) {
            "lifecycle"->LifecycleFragment()
            "SlideCard"->SlideCardFragment()
            "RecyclerView"-> RecyclerFragment()
            "rxandroid" -> RxrFragment()
            "热点" -> HotFragment()
            "FlowLayout"-> FlowFragment()
            "DispatchEvent" -> EventFragment()
            else -> TabCommonFragment()
        }
    }
}