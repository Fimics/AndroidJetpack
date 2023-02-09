package com.mic.tab.online

import androidx.fragment.app.Fragment
import com.mic.ui.indicator.TabBaseAdapter

class AdapterTabOnline(fragment: Fragment, arrays: Array<String>) :
    TabBaseAdapter(fragment, arrays) {
    override fun newFragment(position: Int): Fragment {
        when (arrays[position]) {
            "家庭" -> return LoveFragment()
            "爱情" -> return FimilyFragment()
            else -> return TabCommonFragment()
        }
    }

}