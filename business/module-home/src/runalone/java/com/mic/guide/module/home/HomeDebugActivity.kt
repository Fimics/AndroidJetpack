package com.mic.guide.module.home

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/** 组件模式占位入口页：替换为本模块真实首页 Fragment 即可。 */
class HomeDebugActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(TextView(this).apply { text = "module-home 组件模式独立运行" })
    }
}
