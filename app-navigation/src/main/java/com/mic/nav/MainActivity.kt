package com.noetix.robotics

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.content.Intent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.noetix.libcore.utils.KLog
import com.noetix.robotics.databinding.ActivityMainBinding
import com.noetix.robotics.demo.camera.CameraActivity
import com.noetix.robotics.demo.explanation.ExplanationActivity
import com.noetix.robotics.demo.interaction.InteractionActivity

class MainActivity : AppCompatActivity() {

    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
    }

    private val tag = "MainActivity"
    private lateinit var context: Context
    private lateinit var binding: ActivityMainBinding

    @SuppressLint("NewApi")
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        KLog.d(tag, "onCreate")

        requestWindowFeature(Window.FEATURE_NO_TITLE) // 去掉标题栏
        window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        val params = window.attributes
        params.systemUiVisibility = View.SYSTEM_UI_FLAG_IMMERSIVE
        window.attributes = params

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        context = this
        initSDK()
        setupButtons()

        KLog.d(tag, "onCreate end $this")
    }

    private fun setupButtons() {
        binding.btnInteraction.setOnClickListener {
            startActivity(Intent(this, InteractionActivity::class.java))
        }

        binding.btnExplanation.setOnClickListener {
            startActivity(Intent(this, ExplanationActivity::class.java))
        }

        binding.btnCamera.setOnClickListener {
            startActivity(Intent(this, CameraActivity::class.java))
        }
    }

    private fun initSDK() {
        // SDK 初始化相关逻辑保持原样（原本就是注释掉的）
    }

    override fun onResume() {
        super.onResume()
        KLog.d(tag, "onResume end $this")
    }

    override fun onPause() {
        super.onPause()
        KLog.d(tag, "onPause end $this")
    }

    override fun onStop() {
        super.onStop()
        KLog.d(tag, "onStop end $this")
    }

    override fun onDestroy() {
        super.onDestroy()
        KLog.d(tag, "onDestroy end $this")
    }

    override fun onBackPressed() {
        super.onBackPressed()
        KLog.d(tag, "onBackPressed end $this")
    }
}
