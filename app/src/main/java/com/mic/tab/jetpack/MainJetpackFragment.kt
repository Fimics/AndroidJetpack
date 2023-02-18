package com.mic.tab.jetpack

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mic.R
import com.mic.ui.indicator.TabBaseAdapter
import com.mic.ui.indicator.TabBaseFragment

class MeFragment : TabBaseFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_tab_jetpack, container, false)
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_tab_jetpack
    }

    override fun getTabItemAdapter(): TabBaseAdapter {
        val array = resources.getStringArray(R.array.tab_me)
        return AdapterTabJetpack(this, array)
    }
}