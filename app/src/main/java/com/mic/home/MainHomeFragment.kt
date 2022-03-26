package com.mic.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mic.R
import com.mic.indicator.TabBaseAdapter
import com.mic.indicator.TabBaseFragment
import com.mic.local.AdapterTabLocal


class MainHomeFragment : TabBaseFragment() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_home
    }

    override fun getTabItemAdapter(): TabBaseAdapter {
        val array = resources.getStringArray(R.array.tab_home)
        return AdapterTabHome(this, array)
    }
}