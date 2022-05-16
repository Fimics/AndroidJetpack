package com.mic.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat

object PermissionUtils {

    private val PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_NETWORK_STATE,
        Manifest.permission.ACCESS_WIFI_STATE,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
    )

    @JvmStatic
    fun requestPermissions(activity: Activity?) {
        val list:List<String> = checkPermission(activity, PERMISSIONS)
        if (list.isEmpty()){
            //已申请权限
        }else{
            requestPermission(activity,list)
        }
    }

    private fun requestPermission(activity: Activity?,needRequestList:List<String>) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                activity?.let { ActivityCompat.requestPermissions(it, needRequestList.toTypedArray(), 0x0010) }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun checkPermission(context: Context?, checkList: Array<String>): List<String> {
        val list: MutableList<String> = ArrayList()
        for (s in checkList) {
            if (PackageManager.PERMISSION_GRANTED != context?.let {
                    ActivityCompat.checkSelfPermission(
                        it, s)
                }) {
                list.add(s)
            }
        }
        return list
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun requestPermissionsOverlay(){
        val context = AppGlobals.getApplication()
        if (!Settings.canDrawOverlays(context)) {
            val intent = Intent()
            intent.action = Settings.ACTION_MANAGE_OVERLAY_PERMISSION
            context.startActivity(intent)
        }
    }

    fun isAllGranted():Boolean{
        val list:List<String> = checkPermission(AppGlobals.getApplication(), PERMISSIONS)
        return list.isEmpty()
    }
}