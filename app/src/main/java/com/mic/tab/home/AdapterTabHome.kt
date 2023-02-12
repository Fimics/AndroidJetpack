package com.mic.tab.home

import androidx.fragment.app.Fragment
import com.mic.jetpack.databinding.DataBindingFragment
import com.mic.ui.indicator.TabBaseAdapter
import com.mic.jetpack.lifecycle.LifecycleFragment
import com.mic.jetpack.livedata.LiveDataFragment
import com.mic.jetpack.viewmodel.ViewModelFragment
import com.mic.rx.RxrFragment
import com.mic.ui.event.EventFragment
import com.mic.ui.flow.FlowFragment
import com.mic.ui.rv.RecyclerFragment
import com.mic.ui.slidecard.SlideCardFragment

class AdapterTabHome(fragment: Fragment, arrays: Array<String>) :
    TabBaseAdapter(fragment, arrays) {
    override fun newFragment(position: Int): Fragment {
        return when (arrays[position]) {
//            "hilt"->HiltFragment()
            "viewmodel"->ViewModelFragment()
//            "dagger2"->Dagger2Fragment()
            "databinding"->DataBindingFragment()
            "livedata"->LiveDataFragment()
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