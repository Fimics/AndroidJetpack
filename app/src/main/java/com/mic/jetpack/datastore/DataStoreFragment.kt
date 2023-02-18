package com.mic.jetpack.datastore

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.mic.databinding.FragmentDatastoreBinding
import com.mic.libcore.utils.KLog
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch


class DataStoreFragment : Fragment() {

    //如果一个类有两个概念上相同的属性，但一个是公共API的一部分，另一个是实现细节，请使用下划线作为私有属性名称的前缀
    private var _binding: FragmentDatastoreBinding? = null
    private val binding get() = _binding!!
    private val tag = "store"
    private val mainScope by lazy { MainScope() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDatastoreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.put.setOnClickListener {
            mainScope.launch {
                DataStoreUtils2.put("int", 1)
            }
        }

        binding.get.setOnClickListener {
            val flow:Flow<Int> =DataStoreUtils2.get("int", 3)
            flow.map {
                KLog.d(tag,"value ->$it")
            }

        }

        binding.saveBoolean.setOnClickListener {
            mainScope.launch {
                DataStoreUtils2.put("b", true)
            }
        }

        binding.getBoolean.setOnClickListener {
            DataStoreUtils2.get("b", false).map {
                KLog.d(tag, "value->$it")
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