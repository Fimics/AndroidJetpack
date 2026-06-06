package com.mic.guide.module.settings.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.mic.guide.arch.base.BaseFragment
import com.mic.guide.module.settings.data.local.SettingsLocalSource
import com.mic.guide.module.settings.databinding.FragmentSettingsDetailBinding

/**
 * 设置二级页面：展示某一级项的「功能列表」（每个功能即一个 item）。
 *
 * 入参（bundle）：`sectionKey`（决定展示哪组功能）、`sectionTitle`（标题栏）。
 * 复用 [SettingsAdapter]：普通功能行点击 toast、开关行本地记忆勾选态（演示，无持久化）。
 */
class SettingsDetailFragment : BaseFragment<FragmentSettingsDetailBinding>() {

    private val checkedKeys = mutableSetOf<String>()

    private val adapter = SettingsAdapter(
        onItemClick = { toast(getString(it.titleRes)) },
        isChecked = { it.key in checkedKeys },
        onCheckedChange = { item, checked ->
            if (checked) checkedKeys.add(item.key) else checkedKeys.remove(item.key)
        },
    )

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentSettingsDetailBinding.inflate(inflater, container, false)

    override fun initView() {
        val sectionKey = arguments?.getString(ARG_KEY).orEmpty()
        binding.toolbar.title = arguments?.getString(ARG_TITLE).orEmpty()
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        binding.rvDetail.layoutManager = LinearLayoutManager(requireContext())
        binding.rvDetail.adapter = adapter
        adapter.submit(SettingsLocalSource.detailItems(sectionKey))
    }

    companion object {
        const val ARG_KEY = "sectionKey"
        const val ARG_TITLE = "sectionTitle"
    }
}