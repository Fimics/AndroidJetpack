package com.mic

import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.mic.databinding.ActivityMainBinding
import com.mic.libcore.utils.ExecutorsPoller
import com.mic.libcore.utils.FileServer
import com.mic.libcore.utils.KLog2
import com.mic.libcore.utils.PermissionUtils
import com.mic.libcore.utils.RootShell
import java.util.*


class MainActivity : AppCompatActivity() {
    private val tag = "MainActivity"
    private lateinit var navController: NavController
//    lateinit var user: User
//    lateinit var analyticsAdapter: AnalyticsAdapter
    private val fileServer = FileServer();
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        PermissionUtils.isGrantExternalRW(this, 1)
        PermissionUtils.requestPermissions(this)


        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_container) as NavHostFragment
        navController = navHostFragment.navController

        val bottomNavigationView = binding.bottomNav
        bottomNavigationView.setupWithNavController(navController)
        testRootShell()
//        startServer()

//        user.name = "朱Bony"
//        user.age = 30
//        Log.d("di",user.toString())
//
    }

    private fun testRootShell(){
       val isRooted =  RootShell.isRooted()
        KLog2.d(tag,"isRooted ->$isRooted")
    }

    private fun startServer(){
        ExecutorsPoller.poll(object : TimerTask() {
            override fun run() {
                val isAllGranted = PermissionUtils.isAllGranted()
                if (isAllGranted) {
                    fileServer.start()
                    KLog2.d(tag,"文件已拷贝")
                    ExecutorsPoller.shutdown()
                }else{
                    KLog2.d(tag,"文件正在拷贝")
                }
            }
        })
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            1 -> if (grantResults[0] === PackageManager.PERMISSION_GRANTED) {
                //检验是否获取权限，如果获取权限，外部存储会处于开放状态，会弹出一个toast提示获得授权
                val sdCard = Environment.getExternalStorageState()
                if (sdCard == Environment.MEDIA_MOUNTED) {
                    Toast.makeText(this, "获得授权", Toast.LENGTH_LONG).show()
                }
            } else {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "buxing", Toast.LENGTH_SHORT).show()
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

//        if (PermissionUtils.isAllGranted()){
//            fileServer.start()
//        }else{
//            PermissionUtils.requestPermissions(this)
//        }

    }
}