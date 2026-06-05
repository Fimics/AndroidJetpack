package com.mic.guide

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.mic.guide.databinding.ActivityMainBinding
import com.mic.guide.support.router.AppNavigator
import com.mic.guide.support.router.NavigatorProvider


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 注册全局路由门面：业务层经 NavigatorProvider.navigator 跳转（§5.5）
        val navHost = supportFragmentManager
            .findFragmentById(R.id.nav_host) as NavHostFragment
        NavigatorProvider.navigator = AppNavigator(navHost.navController) { uri ->
            // 目标模块被拔掉 / 未集成时的降级（§15 可插拔兜底）
            Toast.makeText(this, "目标暂不可用：$uri", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        NavigatorProvider.navigator = null // 防止静态持有 NavController/Activity 泄漏
    }
}
