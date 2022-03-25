package com.mic.indicator

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.mic.R

class ItemFragment : Fragment(){
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_indicator, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        arguments?.takeIf { it.containsKey(ARG_OBJECT) }?.apply {
            val textView: TextView = view.findViewById(R.id.text_item)
            textView.text = getInt(ARG_OBJECT).toString()
        }
    }
}

private const val ARG_OBJECT = "object"

class Adapter(fragment: BaseFragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int {
        return 5
    }

    override fun createFragment(position: Int): Fragment {
        val fragment = BaseFragment()
        fragment.arguments= Bundle().apply {
            putInt(ARG_OBJECT,position+1)
        }
        return fragment;
    }
}
