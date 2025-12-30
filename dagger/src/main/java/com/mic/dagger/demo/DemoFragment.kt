package com.mic.dagger.demo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.mic.dagger.databinding.FragmentDemoBinding


class DemoFragment : Fragment() {

    //如果一个类有两个概念上相同的属性，但一个是公共API的一部分，另一个是实现细节，请使用下划线作为私有属性名称的前缀
    private var _binding: FragmentDemoBinding? = null
    private val tag = "DemoFragment"
    private val binding get() = _binding!!


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDemoBinding.inflate(inflater, container, false)
        binding.btnDagger2.setOnClickListener {
            testDIComputer()
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    fun testDIComputer(){
        // 创建一个爱玩游戏的张三
        val person = Person("xxx")
       // 创建依赖注入器
        val ztoExpress = DaggerZTQExpress.builder()
            .taoBao(TaoBao())
            .build()

      // 通过中通这个依赖注入器，为张三提供一台电脑
        ztoExpress.deliverTo(person)

      // 现在张三可以玩游戏了
        person.playGame("赛博朋克2077")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}