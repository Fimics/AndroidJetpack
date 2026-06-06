package com.mic.guide

import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.mic.guide.databinding.ActivityMainBinding
import com.mic.guide.support.router.AppNavigator
import com.mic.guide.support.router.NavigatorProvider


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)

        // 边到边全屏：内容绘制到状态栏/导航栏之下，再用 insets 避让（系统栏透明见主题）
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(binding.root)

        // 刘海屏：允许内容延伸进刘海区域（摄像头所在短边），配合下方 insets 让 UI 不被遮挡
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes = window.attributes.apply {
                layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }

        // 系统栏图标颜色随日/夜模式：浅色背景用深色图标，深色背景用浅色图标
        val lightBars = !isNightMode()
        WindowCompat.getInsetsController(window, binding.root).apply {
            isAppearanceLightStatusBars = lightBars
            isAppearanceLightNavigationBars = lightBars
        }

        // 把系统栏/刘海的 insets 作为内边距：顶部(含刘海)给内容、底部给 tab 栏、左右防横屏刘海
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout(),
            )
            binding.navHost.updatePadding(top = bars.top, left = bars.left, right = bars.right)
            binding.bottomNav.updatePadding(bottom = bars.bottom, left = bars.left, right = bars.right)
            WindowInsetsCompat.CONSUMED
        }

        val navHost = supportFragmentManager
            .findFragmentById(R.id.nav_host) as NavHostFragment

        // 底部 5 个 tab 与 NavController 绑定：点 tab 切到对应子图、各自保留返回栈（§5.10）
        binding.bottomNav.setupWithNavController(navHost.navController)

        // 注册全局路由门面：业务层经 NavigatorProvider.navigator 跳转（§5.5）
        NavigatorProvider.navigator = AppNavigator(navHost.navController) { uri ->
            // 目标模块被拔掉 / 未集成时的降级（§15 可插拔兜底）
            Toast.makeText(this, "目标暂不可用：$uri", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isNightMode(): Boolean =
        (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_YES

    override fun onDestroy() {
        super.onDestroy()
        NavigatorProvider.navigator = null // 防止静态持有 NavController/Activity 泄漏
    }
}