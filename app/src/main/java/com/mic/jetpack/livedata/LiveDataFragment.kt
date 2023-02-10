package com.mic.jetpack.livedata

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import com.mic.databinding.FragmentLivedataBinding
import com.mic.libcore.utils.KLog
import kotlin.concurrent.thread


class LiveDataFragment : Fragment() {

    //如果一个类有两个概念上相同的属性，但一个是公共API的一部分，另一个是实现细节，请使用下划线作为私有属性名称的前缀
    private var _binding: FragmentLivedataBinding? = null
    private val binding get() = _binding!!
    companion object{
        //mVersion怎么控制的？
        @JvmStatic
        val mLiveData = MutableLiveData<String>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mLiveData.observe(this){
            KLog.d("livedata ",it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLivedataBinding.inflate(inflater, container, false)
        binding.btnMain.setOnClickListener {
           mLiveData.value="main"
        }
        var value = "thread-> "
        binding.btnThread.setOnClickListener {
            thread(start = true){
                mLiveData.postValue(value)
                value+=value
            }
        }
        binding.btnActivity.setOnClickListener {
           val intent = Intent(activity,LivedataActivity::class.java)
           startActivity(intent)
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