package com.mic.guide.module.home.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import com.mic.guide.arch.base.BaseFragment
import com.mic.guide.module.home.databinding.FragmentHomeDetailBinding

/**
 * 详情页：无需 ViewModel，直接继承 [BaseFragment]。
 * 从 `arguments` 读取列表页传入的参数，演示模块内 nav 子图的目的地 + 传参。
 */
class HomeDetailFragment : BaseFragment<FragmentHomeDetailBinding>() {

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentHomeDetailBinding.inflate(inflater, container, false)

    override fun initView() {
        binding.tvTitle.text = arguments?.getString("title").orEmpty()
        binding.tvSubtitle.text = arguments?.getString("subtitle").orEmpty()
    }
}
