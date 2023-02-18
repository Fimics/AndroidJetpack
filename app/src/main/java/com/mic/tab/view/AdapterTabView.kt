package com.mic.tab.view

import androidx.fragment.app.Fragment
import com.mic.ui.indicator.TabBaseAdapter

class AdapterTabView(fragment: Fragment, arrays: Array<String>) :
    TabBaseAdapter(fragment, arrays) {
    override fun newFragment(position: Int): Fragment {
        when (arrays[position]) {
            else -> return TabCommonFragment()
        }
    }

}