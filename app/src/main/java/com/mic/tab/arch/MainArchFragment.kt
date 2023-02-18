package com.mic.tab.arch

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mic.R
import com.mic.ui.indicator.TabBaseAdapter
import com.mic.ui.indicator.TabBaseFragment

class MainArchFragment : TabBaseFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_tab_arch, container, false)
    }
    override fun getLayoutId(): Int {
        return R.layout.fragment_tab_arch
    }

    override fun getTabItemAdapter(): TabBaseAdapter {
        val array = resources.getStringArray(R.array.tab_music)
        return AdapterTabArch(this, array)
    }
}