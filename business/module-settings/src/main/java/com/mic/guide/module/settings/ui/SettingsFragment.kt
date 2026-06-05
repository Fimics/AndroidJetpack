package com.mic.guide.module.settings.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.mic.guide.api.settings.SettingsApi
import com.mic.guide.arch.api.ApiRegistry
import com.mic.guide.arch.base.collectIn
import com.mic.guide.arch.mvvm.MvvmFragment
import com.mic.guide.module.settings.databinding.FragmentSettingsBinding

/**
 * 设置首页（MVVM 端到端示范，与 [com.mic.guide.module.home.ui.HomeFragment] 同构，数据走本地）：
 * 逻辑写在 `initView()/observe()` 钩子；`items` 是 StateFlow，`collectIn(viewLifecycleOwner)` 收集。
 */
class SettingsFragment : MvvmFragment<FragmentSettingsBinding, SettingsViewModel>() {

    override val viewModel: SettingsViewModel by viewModels()

    private val adapter = SettingsAdapter()

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentSettingsBinding.inflate(inflater, container, false)

    override fun initView() {
        binding.rvSettings.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSettings.adapter = adapter

        // 经 SettingsApi（本模块实现、support-storage 持久化）读写深色模式；走注册表，亦印证 api 解耦
        val settings = ApiRegistry.get(SettingsApi::class.java)
        binding.switchDarkMode.isChecked = settings?.isDarkMode() ?: false
        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            settings?.setDarkMode(isChecked)
        }
    }

    override fun observe() {
        viewModel.items.collectIn(viewLifecycleOwner) { adapter.submit(it) }
    }

    override fun onLoading(loading: Boolean) {
        binding.progress.isVisible = loading
    }
}