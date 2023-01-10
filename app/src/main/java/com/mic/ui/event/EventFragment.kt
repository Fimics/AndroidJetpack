package com.mic.ui.event

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.mic.R
import com.mic.databinding.FragmentEventBinding
import com.mic.utils.KLog


class EventFragment : Fragment() {

    //如果一个类有两个概念上相同的属性，但一个是公共API的一部分，另一个是实现细节，请使用下划线作为私有属性名称的前缀
    private var _binding: FragmentEventBinding? = null
    private val binding get() = _binding!!
    val TAG: String = "event"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEventBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnClick.setOnClickListener {
            KLog.d(TAG, "setOnClickListener")
        }
        binding.btnClick.setOnLongClickListener {
            KLog.d(TAG, "setOnLongClickListener")
            false
        }
        binding.btnClick.setOnTouchListener { v, event ->
            KLog.d(TAG, "setOnTouchListener")
            false
        }

        binding.btnDispatch.setOnClickListener{
            val intent = Intent(activity,EventActivity::class.java)
            startActivity(intent)
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