package com.mic.guide.support.permission

import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.contract.ActivityResultContracts

/**
 * 运行时权限请求封装（§9）：基于 ActivityResult API，避免 `onRequestPermissionsResult` 的请求码样板。
 *
 * 用法（必须在 STARTED 之前创建，通常作为 Activity/Fragment 字段）：
 * ```
 * private val cameraPermission = PermissionLauncher(this) { granted -> ... }
 * // 点击时：cameraPermission.launch(Manifest.permission.CAMERA)
 * ```
 *
 * @param caller `ComponentActivity` 或 `Fragment`（均实现 [ActivityResultCaller]）
 */
class PermissionLauncher(
    caller: ActivityResultCaller,
    private val onResult: (granted: Boolean) -> Unit,
) {

    private val launcher = caller.registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted -> onResult(granted) }

    fun launch(permission: String) = launcher.launch(permission)
}