package com.mic.ui.indicator

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

abstract class TabBaseAdapter(fragment: Fragment, val arrays:Array<String>) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int {
        return arrays.size
    }

    override fun createFragment(position: Int): Fragment {
        return newFragment(position)
    }

    fun getDataSource():Array<String>{
        return arrays
    }

    abstract fun newFragment(position: Int): Fragment
}