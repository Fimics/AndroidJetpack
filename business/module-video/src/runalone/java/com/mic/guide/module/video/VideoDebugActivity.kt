package com.mic.guide.module.video

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/** 组件模式占位入口页：替换为本模块真实首页 Fragment 即可。 */
class VideoDebugActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(TextView(this).apply { text = "module-video 组件模式独立运行" })
    }
}
