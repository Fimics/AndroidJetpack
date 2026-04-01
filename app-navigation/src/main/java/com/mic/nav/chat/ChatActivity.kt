package com.noetix.robotics.chat

import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.bytedance.speech.speechengine.SpeechEngineGenerator
import com.noetix.robotics.AppConfig
import com.noetix.libcore.utils.KLog
import com.noetix.libcore.utils.P
import com.noetix.robotics.R
import com.noetix.robotics.chat.fragments.MainFragment

class ChatActivity : AppCompatActivity() {

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
        val resId =   R.layout.activity_chat
        setContentView(resId)

        //使用 火山tts 完成网络环境等相关依赖配置。
        SpeechEngineGenerator.PrepareEnvironment(applicationContext, application)
        KLog.d(TAG,"ChatActivity onCreate()")

        // Add fragments to their respective containers
        if (savedInstanceState == null) {
            addFragments()
        }
    }

    override fun onStart() {
        super.onStart()
        KLog.d(TAG,"ChatActivity onStart()")
    }

    override fun onResume() {
        super.onResume()
        KLog.d(TAG,"ChatActivity onResume()")
    }

    private fun addFragments() {
        // Create fragment instances
        val mainFragment = MainFragment()
        // Begin fragment transaction using getSupportFragmentManager()
        val transaction = supportFragmentManager.beginTransaction()

//        if (isStateModal) {
//            val cameraFragment = SpeechRecManager.instance().gerCameraFragment()
//            if (cameraFragment!=null){
//                KLog.d(TAG,"add cameraFragment")
//                transaction.replace(R.id.testFragmentContainer, cameraFragment)
//            }
//        }

        transaction.replace(R.id.mainFragmentContainer, mainFragment)

        // Commit the transaction
        transaction.commit()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    companion object {
        private const val TAG = "ChatActivity"
    }
}
