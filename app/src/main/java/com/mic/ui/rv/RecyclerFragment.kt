package com.mic.ui.rv

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.mic.databinding.FragmentRecyclerBinding


class RecyclerFragment : Fragment() {

    //如果一个类有两个概念上相同的属性，但一个是公共API的一部分，另一个是实现细节，请使用下划线作为私有属性名称的前缀
    private var _binding: FragmentRecyclerBinding? = null
    private val binding get() = _binding!!
    private val starList:MutableList<Star> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRecyclerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initData()
        val recyclerView = binding.rvList
        recyclerView.layoutManager=LinearLayoutManager(activity)
        recyclerView.addItemDecoration(StarDecoration(activity!!))
        recyclerView.adapter=StarAdapter(activity!!,starList)
    }

    private fun initData(){
        for (i in 0 until 5){
            for (j in 0..20){
                when(i){
                    2,4->starList.add(Star("何一华-> $j","快乐一家->$i"))
                    else ->starList.add(Star("小明星->$j","小卫视->$i"))
                }

            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}