package com.mic.tab.arch

import androidx.fragment.app.Fragment
import com.mic.rx.RxrFragment
import com.mic.ui.indicator.TabBaseAdapter

class AdapterTabArch(fragment: Fragment, arrays: Array<String>) :
    TabBaseAdapter(fragment, arrays) {
    override fun newFragment(position: Int): Fragment {
        return when (arrays[position]) {
            "rxandroid" -> RxrFragment()
            else ->  TabCommonFragment()
        }
    }

}