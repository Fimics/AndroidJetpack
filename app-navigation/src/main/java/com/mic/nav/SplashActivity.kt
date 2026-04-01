package com.noetix.robotics

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.noetix.libcore.utils.KLog


class SplashActivity : AppCompatActivity() {

    private val tag = "SplashActivity"

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE) //去掉标题栏
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        ) //去掉信息栏
        val params = window.attributes
        params.systemUiVisibility = View.SYSTEM_UI_FLAG_IMMERSIVE
        window.attributes = params
        KLog.d(tag, "setContentView")
        setContentView(R.layout.activity_splash)
        startMainPage()
    }

    private fun startMainPage() {
        val intent = Intent(this@SplashActivity, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

}
