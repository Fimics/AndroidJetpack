package com.mic.hilt.demo.hilt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.mic.hilt.KLog
import com.mic.hilt.databinding.FragmentHiltBinding
import com.mic.hilt.demo.hilt.di.IInterface
import com.mic.hilt.demo.hilt.`object`.HttpObject
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class HiltFragment : Fragment() {

    //如果一个类有两个概念上相同的属性，但一个是公共API的一部分，另一个是实现细节，请使用下划线作为私有属性名称的前缀
    private var _binding: FragmentHiltBinding? = null
    private val binding get() = _binding!!

    @Inject
    @JvmField
    var httpObject: HttpObject?=null

    @Inject
    @JvmField
    var ii: IInterface?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHiltBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnHilt.setOnClickListener {
            KLog.d("hilt","http object code ${httpObject.hashCode()}")
            KLog.d("hilt","ii method ${ii?.method()}")
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