package com.noetix.robotics.demo

import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.noetix.robotics.R
import com.noetix.robotics.databinding.ActivityDemoBaseBinding

/**
 * 所有 Demo 功能模块的基类 Activity
 * 负责：
 * 1. 设置全屏沉浸模式
 * 2. 绑定公共布局 [R.layout.activity_demo_base]
 * 3. 管理顶部的 Title 和 返回按钮
 * 4. 提供 NavController 实例
 */
abstract class BaseDemoActivity : AppCompatActivity() {

    protected lateinit var binding: ActivityDemoBaseBinding
    protected lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 1. 设置无标题和全屏模式
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        val params = window.attributes
        params.systemUiVisibility = View.SYSTEM_UI_FLAG_IMMERSIVE
        window.attributes = params

        // 2. 绑定通用基础布局
        binding = ActivityDemoBaseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 3. 设置具体的 Navigation Graph 和 Title
        setupNavigationAndTitle()

        // 4. 统一的返回按钮事件（先交由 Navigation 处理）
        binding.ivBack.setOnClickListener {
            if (!navController.popBackStack()) {
                finish() // 若退无可退，则结束当前 Activity 退回主应用界面
            }
        }
    }

    /**
     * 子类需在此方法中：
     * 1. 找到 NavHostFragment 并初始化 [navController]
     * 2. 调用 [navController.setGraph] 绑定自身的 NavGraph
     * 3. 设置 [binding.tvTitle.text] 为模块名称
     */
    abstract fun setupNavigationAndTitle()

    /**
     * 系统返回键也交由 Navigation 优先接管
     */
    @Deprecated("Deprecated in Java", ReplaceWith("navController.popBackStack()"))
    override fun onBackPressed() {
        if (!navController.popBackStack()) {
            super.onBackPressed()
        }
    }
}
