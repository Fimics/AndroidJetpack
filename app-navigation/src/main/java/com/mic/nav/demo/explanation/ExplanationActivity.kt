package com.noetix.robotics.demo.explanation

import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import com.noetix.robotics.R
import com.noetix.robotics.demo.BaseDemoActivity

class ExplanationActivity : BaseDemoActivity() {

    private lateinit var viewModel: ExplanationViewModel

    override fun setupNavigationAndTitle() {
        // 绑定 ViewModel
        viewModel = ViewModelProvider(this)[ExplanationViewModel::class.java]

        // 设置当前界面的主标题
        binding.tvTitle.text = "讲解 (Explanation)"

        // 获取 NavHostFragment 并设置 Graph
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        navController.setGraph(R.navigation.nav_explanation)
    }
}
