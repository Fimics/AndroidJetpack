package com.mic.jetpack.dagger2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.mic.databinding.FragmentDagger2Binding
import com.mic.jetpack.dagger2.`object`.DatabaseObject
import com.mic.jetpack.dagger2.`object`.HttpObject
import javax.inject.Inject


class Dagger2Fragment : Fragment() {

    //如果一个类有两个概念上相同的属性，但一个是公共API的一部分，另一个是实现细节，请使用下划线作为私有属性名称的前缀
    private var _binding: FragmentDagger2Binding? = null
    private val binding get() = _binding!!

    @Inject
    var httpObject:HttpObject?=null

    @Inject
    var databaseObject:DatabaseObject?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDagger2Binding.inflate(inflater, container, false)
        binding.btnDagger2.setOnClickListener {

        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}