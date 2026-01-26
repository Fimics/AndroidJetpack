package com.mic.jetpack.viewmodel

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.mic.databinding.FragmentViewmodelBinding
import com.mic.libcore.utils.KLog2


class ViewModelFragment : Fragment() {

    //如果一个类有两个概念上相同的属性，但一个是公共API的一部分，另一个是实现细节，请使用下划线作为私有属性名称的前缀
    private var _binding: FragmentViewmodelBinding? = null
    private val binding get() = _binding!!
    private lateinit var mViewModel:XViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //当我们执行这句代码的时候  会去ViewModelStore获取MyViewModel对象   如果没有 就创建一个
        mViewModel=ViewModelProvider(this).get(XViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentViewmodelBinding.inflate(inflater, container, false)

        binding.textView.text = mViewModel.getNumber().value

        binding.btnData1.setOnClickListener {
            mViewModel.getNumber().value=mViewModel.getNumber().value+1
        }

        binding.btnData2.setOnClickListener {
            mViewModel.getNumber().value=mViewModel.getNumber().value+1
        }

        mViewModel.getNumber().observe(this){
            KLog2.d("viewmodel","number->$it")
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