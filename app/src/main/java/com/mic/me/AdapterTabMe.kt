package com.mic.me

import androidx.fragment.app.Fragment
import com.mic.indicator.TabBaseAdapter
import com.mic.me.user.InfoFragment

class AdapterTabMe(fragment: Fragment, arrays: Array<String>) :
    TabBaseAdapter(fragment, arrays) {
    override fun newFragment(position: Int): Fragment {
        when (arrays[position]) {
            "信息" -> return InfoFragment()
            "下载" -> return DownloadFragment()
            "设置" -> return SettingFragment()
            else -> return TabCommonFragment()
        }
    }

}