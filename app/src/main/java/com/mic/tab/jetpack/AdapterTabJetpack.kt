package com.mic.tab.jetpack

import androidx.fragment.app.Fragment
import com.mic.ui.indicator.TabBaseAdapter

class AdapterTabJetpack(fragment: Fragment, arrays: Array<String>) :
    TabBaseAdapter(fragment, arrays) {
    override fun newFragment(position: Int): Fragment {
        when (arrays[position]) {
            else -> return TabCommonFragment()
        }
    }

}