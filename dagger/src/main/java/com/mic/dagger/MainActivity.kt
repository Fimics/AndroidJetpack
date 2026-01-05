package com.mic.dagger

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mic.dagger.demo2.Demo2Fragment


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        this.supportFragmentManager.beginTransaction().add(R.id.root, DependenciesFragment()).commit()

        this.supportFragmentManager.beginTransaction().add(R.id.root, Demo2Fragment()).commit()
          // 用于测试@Singleton 测试
//        this.supportFragmentManager.beginTransaction().add(R.id.root, Demo2FragmentTest()).commit()
    }
}
