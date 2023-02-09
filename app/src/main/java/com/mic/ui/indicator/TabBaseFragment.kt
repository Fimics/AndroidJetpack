package com.mic.ui.indicator

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.mic.R

abstract class TabBaseFragment : Fragment() {

    open lateinit var viewPager: ViewPager2
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(getLayoutId(), container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("a",view.toString())
        viewPager = view.findViewById(R.id.pager2)
        var tabItemAdapter=getTabItemAdapter()
        var dataSource= tabItemAdapter.getDataSource()
        viewPager.adapter=tabItemAdapter
        val tabLayout = view.findViewById<TabLayout>(R.id.tab_layout)
        TabLayoutMediator(tabLayout, viewPager){ tab, position ->
            tab.text = dataSource[position]
        }.attach()
    }


    abstract fun getLayoutId():Int

    abstract fun getTabItemAdapter(): TabBaseAdapter
}



