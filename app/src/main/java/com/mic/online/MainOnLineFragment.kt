package com.mic.online

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mic.R
import com.mic.indicator.TabBaseAdapter
import com.mic.indicator.TabBaseFragment
import com.mic.local.AdapterTabLocal

class MainOnLineFragment : TabBaseFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_on_line, container, false)
    }
    override fun getLayoutId(): Int {
        return R.layout.fragment_on_line
    }

    override fun getTabItemAdapter(): TabBaseAdapter {
        val array = resources.getStringArray(R.array.tab_online)
        return AdapterTabOnline(this, array)
    }
}