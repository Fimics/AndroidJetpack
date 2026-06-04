package com.mic.dagger.demo2.d01_inject_component

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.mic.dagger.databinding.FragmentInjectBinding


class InjectFragment : Fragment() {

    //如果一个类有两个概念上相同的属性，但一个是公共API的一部分，另一个是实现细节，请使用下划线作为私有属性名称的前缀
    private var _binding: FragmentInjectBinding? = null
    private val tag = "DemoFragment"
    private val binding get() = _binding!!
//
//    @Inject
//    lateinit var user: User
//    @Inject
//    lateinit var user2: User
//
//    @Inject
//    lateinit var retrofit: Retrofit
//
//    @Inject
//    lateinit var apiService: ApiService
//    @Inject
//    lateinit var apiService2: ApiService
//
//    @Inject
//    lateinit var applicationContext: Context
//
//    lateinit var usrComponent : UserComponent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentInjectBinding.inflate(inflater, container, false)
//        DaggerApplicationComponent.create().inject(this)
        //全局单例
//        MyApplication.getApplicationComponent().inject(this)
//        usrComponent =DaggerUserComponent.builder().applicationComponent(MyApplication.getApplicationComponent()).build();
//        usrComponent.inject(this)

        binding.btnDagger2.setOnClickListener {
            test_d01_inject();
        }
        return binding.root
    }

    fun test_d01_inject() {
//        KLog.d(tag, "user hashcode -> $user")
//        KLog.d(tag, "user2 hashcode -> $user2")
//        KLog.d(tag, "retrofit hashcode -> $retrofit")
//        KLog.d(tag, "apiService hashcode -> $apiService")
//        KLog.d(tag, "apiService2 hashcode -> $apiService2")
//        KLog.d(tag, "applicationContext hashcode -> $applicationContext")
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