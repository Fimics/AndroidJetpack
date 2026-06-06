package com.mic.guide.support.permission

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.mic.guide.lib.common.AppGlobals

/**
 * 运行时权限静态工具（**合并自 libcore `PermissionUtils` + `PermissionRWUtil`**，转 Kotlin）。
 *
 * 与 [PermissionLauncher]（基于 ActivityResult 的回调式申请）互补：
 * - 一次性、命令式校验/申请用本类；
 * - 需要结果回调用 [PermissionLauncher]。
 */
object PermissionUtils {

    /** 默认通用权限组（网络/存储）。 */
    val DEFAULT_PERMISSIONS = arrayOf(
        android.Manifest.permission.ACCESS_NETWORK_STATE,
        android.Manifest.permission.ACCESS_WIFI_STATE,
        android.Manifest.permission.READ_EXTERNAL_STORAGE,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
    )

    /** 相机 + 存储（合并自 PermissionRWUtil）。 */
    val CAMERA_AND_STORAGE = arrayOf(
        android.Manifest.permission.READ_EXTERNAL_STORAGE,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
        android.Manifest.permission.CAMERA,
    )

    /** 返回未授予的权限列表。 */
    @JvmStatic
    fun missingPermissions(context: Context, permissions: Array<String>): List<String> =
        permissions.filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }

    /** 是否全部已授予。 */
    @JvmStatic
    @JvmOverloads
    fun isAllGranted(
        context: Context = AppGlobals.getApplication(),
        permissions: Array<String> = DEFAULT_PERMISSIONS,
    ): Boolean = missingPermissions(context, permissions).isEmpty()

    /** 申请权限（仅申请未授予的），requestCode 默认 0x10。 */
    @JvmStatic
    @JvmOverloads
    fun requestPermissions(
        activity: Activity,
        permissions: Array<String> = DEFAULT_PERMISSIONS,
        requestCode: Int = 0x10,
    ) {
        val missing = missingPermissions(activity, permissions)
        if (missing.isNotEmpty()) {
            ActivityCompat.requestPermissions(activity, missing.toTypedArray(), requestCode)
        }
    }

    /** 相机+存储一次性申请（合并自 PermissionRWUtil.isGrantExternalRW），返回是否已全部授予。 */
    @JvmStatic
    fun ensureCameraAndStorage(activity: Activity, requestCode: Int = 0x10): Boolean {
        val missing = missingPermissions(activity, CAMERA_AND_STORAGE)
        if (missing.isNotEmpty()) {
            ActivityCompat.requestPermissions(activity, missing.toTypedArray(), requestCode)
            return false
        }
        return true
    }

    /** 悬浮窗权限：未授予则跳系统授权页。 */
    @RequiresApi(Build.VERSION_CODES.M)
    @JvmStatic
    fun requestOverlayPermission() {
        val context = AppGlobals.getApplication()
        if (!Settings.canDrawOverlays(context)) {
            context.startActivity(
                Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:${context.packageName}"),
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
        }
    }
}
