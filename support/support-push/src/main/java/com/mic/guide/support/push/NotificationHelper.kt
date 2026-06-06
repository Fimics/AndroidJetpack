package com.mic.guide.support.push

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

/**
 * 本地通知封装：创建渠道（Android 8+ 必需）+ 展示通知。
 *
 * 推送消息到达后由 [PushManager] 调用本类落地为系统通知。点击通过 [contentIntent] 跳转。
 */
class NotificationHelper(context: Context) {

    private val appContext = context.applicationContext
    private val manager = NotificationManagerCompat.from(appContext)

    init {
        ensureChannel()
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "默认通知",
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply { description = "AiGuide 推送通知" }
            val sys = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            sys.createNotificationChannel(channel)
        }
    }

    /**
     * 展示一条通知。调用方需确保已获 POST_NOTIFICATIONS 权限（Android 13+）。
     * @param iconRes 小图标资源 id（传业务自己的 mipmap/drawable）。
     */
    fun show(
        title: String?,
        body: String?,
        iconRes: Int,
        notifyId: Int = System.currentTimeMillis().toInt(),
        contentIntent: Intent? = null,
    ) {
        val pending = contentIntent?.let {
            PendingIntent.getActivity(
                appContext,
                notifyId,
                it,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        }
        val notification = NotificationCompat.Builder(appContext, CHANNEL_ID)
            .setSmallIcon(iconRes)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .apply { pending?.let { setContentIntent(it) } }
            .build()

        if (NotificationManagerCompat.from(appContext).areNotificationsEnabled()) {
            try {
                manager.notify(notifyId, notification)
            } catch (_: SecurityException) {
                // 权限被撤销时静默忽略
            }
        }
    }

    companion object {
        const val CHANNEL_ID = "aiguide_default"
    }
}
