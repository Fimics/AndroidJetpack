package com.mic.tab.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mic.R
import com.mic.ui.indicator.TabBaseFragment
import com.mic.ui.indicator.TabBaseAdapter

class MainViewFragment : TabBaseFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_tab_view
    }

    override fun getTabItemAdapter(): TabBaseAdapter {
        val array = resources.getStringArray(R.array.tab_local)
        return AdapterTabView(this, array)
    }
}