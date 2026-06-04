package com.mic.netconfig

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mic.netconfig.softap.WiFiConfigService


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        this.supportFragmentManager.beginTransaction().add(R.id.root, DependenciesFragment()).commit()

//        this.supportFragmentManager.beginTransaction().add(R.id.root, MainFragment()).commit()
          // 用于测试@Singleton 测试
//        this.supportFragmentManager.beginTransaction().add(R.id.root, Demo2FragmentTest()).commit()


        // 启动配网服务
        val serviceIntent = Intent(this, WiFiConfigService::class.java)
        this.startService(serviceIntent)
    }
}
