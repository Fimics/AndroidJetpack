package com.mic.dagger.demo2.d01_inject_component

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.mic.dagger.databinding.FragmentInjectBinding
import com.mic.dagger.demo2.MyApplication
import com.mic.libcore.utils.KLog
import retrofit2.Retrofit
import javax.inject.Inject


class SubComponentFragment : Fragment() {

    //如果一个类有两个概念上相同的属性，但一个是公共API的一部分，另一个是实现细节，请使用下划线作为私有属性名称的前缀
    private var _binding: FragmentInjectBinding? = null
    private val tag = "DemoFragment"
    private val binding get() = _binding!!


    @Inject
    lateinit var student: Student

    @Inject
    lateinit var retrofit: Retrofit

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentInjectBinding.inflate(inflater, container, false)

        MyApplication.getApplicationComponent().studentComponentFactory().create().inject(this)
        binding.btnDagger2.setOnClickListener {
            test_d01_inject();
        }
        return binding.root
    }

    fun test_d01_inject() {
        KLog.d(tag, "student -> $student")
        KLog.d(tag, "retrofit -> $retrofit")
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