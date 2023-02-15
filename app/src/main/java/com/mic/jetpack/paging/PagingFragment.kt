package com.mic.jetpack.paging

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mic.databinding.FragmentPagingBinding
import com.mic.jetpack.paging.adapter.RecyclerPagingAdapter
import com.mic.jetpack.paging.viewmodel.PersonViewModel
import kotlin.properties.Delegates


class PagingFragment : Fragment() {

    //如果一个类有两个概念上相同的属性，但一个是公共API的一部分，另一个是实现细节，请使用下划线作为私有属性名称的前缀
    private var _binding: FragmentPagingBinding? = null
    private val binding get() = _binding!!
    private val tag="paging"
    private var recyclerView by Delegates.notNull<RecyclerView>()
    private var pagingAdapter:RecyclerPagingAdapter?=null
    private var personViewModel by Delegates.notNull<PersonViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPagingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView=binding.recycleView
        pagingAdapter = RecyclerPagingAdapter()

        personViewModel=ViewModelProvider(this,ViewModelProvider.NewInstanceFactory())[PersonViewModel::class.java]

        personViewModel.getLiveData().observe(viewLifecycleOwner) {
            pagingAdapter?.submitList(it)
        }

        recyclerView.adapter=pagingAdapter
        recyclerView.layoutManager=LinearLayoutManager(activity)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}