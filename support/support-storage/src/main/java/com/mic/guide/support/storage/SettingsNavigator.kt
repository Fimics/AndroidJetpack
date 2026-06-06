package com.mic.guide.support.storage

import android.content.ComponentName
import android.content.Context
import android.content.Intent

/** 跳转系统设置页（迁移自 libcore `SettingsUtils`，转 Kotlin）。 */
object SettingsNavigator {

    /** 打开系统设置主页。 */
    fun goSystemSettings(context: Context) {
        runCatching {
            val intent = Intent().apply {
                component = ComponentName("com.android.settings", "com.android.settings.Settings")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }.onFailure {
            // 兜底：用标准 action
            runCatching {
                context.startActivity(
                    Intent(android.provider.Settings.ACTION_SETTINGS)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                )
            }
        }
    }
}
