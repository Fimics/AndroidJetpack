package com.mic.tab.flutter

import androidx.fragment.app.Fragment
import com.mic.ui.indicator.TabBaseAdapter

class AdapterTabFlutter(fragment: Fragment, arrays: Array<String>) :
    TabBaseAdapter(fragment, arrays) {
    override fun newFragment(position: Int): Fragment {
        when (arrays[position]) {
            else -> return TabCommonFragment()
        }
    }

}