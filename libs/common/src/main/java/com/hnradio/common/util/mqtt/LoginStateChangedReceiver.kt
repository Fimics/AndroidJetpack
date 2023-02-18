package com.hnradio.common.util.mqtt

import android.app.ActivityManager
import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Process
import com.hnradio.common.util.L

class LoginStateChangedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when(intent.action){
            context.packageName + ".ACTION_LOGIN_STATE_CHANGED" ->{
                L.e("收到广播")
                if(isMainProcess(context.applicationContext)){
                    MqttEngine.instance.notifyConfigChanged()
                }else{
                    L.e("不是主进程，不响应")
                }
            }
        }
    }

    private fun isMainProcess(application: Context): Boolean {
        val pid = Process.myPid()
        val activityManager = application.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (appProcess in activityManager.runningAppProcesses) {
            if (appProcess.pid == pid) {
                return application.packageName == appProcess.processName
            }
        }
        return false
    }
}