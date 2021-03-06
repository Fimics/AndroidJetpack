package com.mic.local

import androidx.fragment.app.Fragment
import com.mic.indicator.TabBaseAdapter

class AdapterTabLocal(fragment: Fragment, arrays: Array<String>) :
    TabBaseAdapter(fragment, arrays) {
    override fun newFragment(position: Int): Fragment {
        when (arrays[position]) {
            "图片" -> return TabPictureFragment()
            "文档" -> return TabDocFragment()
            else -> return TabCommonFragment()
        }
    }

}