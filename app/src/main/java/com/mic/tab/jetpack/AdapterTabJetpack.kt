package com.mic.tab.jetpack

import androidx.fragment.app.Fragment
import com.mic.jetpack.databinding.DataBindingFragment
import com.mic.jetpack.datastore.DataStoreFragment
import com.mic.jetpack.lifecycle.LifecycleFragment
import com.mic.jetpack.livedata.LiveDataFragment
import com.mic.jetpack.paging.PagingFragment
import com.mic.jetpack.room.RoomFragment
import com.mic.jetpack.viewmodel.ViewModelFragment
import com.mic.jetpack.workmanager.WorkFragment
import com.mic.ui.indicator.TabBaseAdapter

class AdapterTabJetpack(fragment: Fragment, arrays: Array<String>) :
    TabBaseAdapter(fragment, arrays) {
    override fun newFragment(position: Int): Fragment {
        return when (arrays[position]) {
            "store"-> DataStoreFragment()
            "work"-> WorkFragment()
            "paging"-> PagingFragment()
            "room"-> RoomFragment()
            "viewmodel"-> ViewModelFragment()
            "databinding"-> DataBindingFragment()
            "livedata"-> LiveDataFragment()
            "lifecycle"-> LifecycleFragment()
            else ->  TabCommonFragment()
        }
    }

}