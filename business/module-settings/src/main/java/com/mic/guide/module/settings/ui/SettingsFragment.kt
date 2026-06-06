package com.mic.guide.module.settings.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.mic.guide.api.settings.SettingsApi
import com.mic.guide.arch.api.ApiRegistry
import com.mic.guide.arch.base.collectIn
import com.mic.guide.arch.mvvm.MvvmFragment
import com.mic.guide.lib.image.ImageLoader
import com.mic.guide.module.settings.R
import com.mic.guide.module.settings.data.local.SettingsLocalSource
import com.mic.guide.module.settings.databinding.FragmentSettingsBinding

/**
 * 设置首页（MVVM + 卡片化 UI）：资料头卡 + 卡片式设置行。
 *
 * - 普通行点击 → 进入二级页面（[SettingsDetailFragment]，展示该项的功能列表）。
 * - 深色模式 = 列表内开关行；多语言 = 弹选择框；均经 [SettingsApi]（`ApiRegistry` 取）读写。
 * - 头像加载真实图片（圆形）。
 */
class SettingsFragment : MvvmFragment<FragmentSettingsBinding, SettingsViewModel>() {

    override val viewModel: SettingsViewModel by viewModels()

    private val settingsApi get() = ApiRegistry.get(SettingsApi::class.java)

    private val adapter = SettingsAdapter(
        onItemClick = { item ->
            when (item.key) {
                SettingsLocalSource.KEY_LANGUAGE -> showLanguageDialog()
                else -> openDetail(item.key, getString(item.titleRes))
            }
        },
        isChecked = { item ->
            item.key == SettingsLocalSource.KEY_DARK_MODE && (settingsApi?.isDarkMode() ?: false)
        },
        onCheckedChange = { item, checked ->
            if (item.key == SettingsLocalSource.KEY_DARK_MODE) settingsApi?.setDarkMode(checked)
        },
    )

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentSettingsBinding.inflate(inflater, container, false)

    override fun initView() {
        binding.rvSettings.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSettings.adapter = adapter

        // 头像：加载真实图片（圆形）。背景置空避免方角露出底色，padding 归零让图填满。
        binding.ivAvatar.background = null
        binding.ivAvatar.setPadding(0, 0, 0, 0)
        ImageLoader.load(binding.ivAvatar, AVATAR_URL, cornerRadiusDp = 100)

        // 头像卡 → 个人资料二级页
        binding.headerCard.setOnClickListener {
            openDetail(SettingsLocalSource.KEY_PROFILE, getString(R.string.settings_profile_title))
        }
        binding.btnLogout.setOnClickListener { toast(getString(R.string.settings_signed_out)) }
    }

    private fun openDetail(sectionKey: String, title: String) {
        findNavController().navigate(
            R.id.action_settings_to_detail,
            bundleOf(
                SettingsDetailFragment.ARG_KEY to sectionKey,
                SettingsDetailFragment.ARG_TITLE to title,
            ),
        )
    }

    /** 语言选择：跟随系统 / 中文 / English；选中即经 SettingsApi 应用（AppCompat 自动重建界面）。 */
    private fun showLanguageDialog() {
        val tags = arrayOf("", "zh", "en")
        val labels = arrayOf(
            getString(R.string.lang_follow_system),
            getString(R.string.lang_chinese),
            getString(R.string.lang_english),
        )
        val current = settingsApi?.getLanguage().orEmpty()
        val checked = tags.indexOf(current).takeIf { it >= 0 } ?: 0
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.lang_dialog_title)
            .setSingleChoiceItems(labels, checked) { dialog, which ->
                settingsApi?.setLanguage(tags[which])
                dialog.dismiss()
            }
            .show()
    }

    override fun observe() {
        viewModel.items.collectIn(viewLifecycleOwner) { adapter.submit(it) }
    }

    override fun onLoading(loading: Boolean) {
        binding.progress.isVisible = loading
    }

    private companion object {
        const val AVATAR_URL = "https://picsum.photos/seed/aiguide-avatar/200"
    }
}