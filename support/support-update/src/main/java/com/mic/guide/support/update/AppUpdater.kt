package com.mic.guide.support.update

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import androidx.core.content.FileProvider
import com.mic.guide.lib.log.Logger
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File

/**
 * 应用内升级门面（§9 扩展）：版本比对 + DownloadManager 下载 APK + FileProvider 拉起安装。
 *
 * 用法：
 * 1. 业务从后端拉 [UpdateInfo]，调 [hasUpdate] 判断；
 * 2. `downloadApk(info).collect { ... }` 观察 [DownloadStatus] 进度；
 * 3. 完成后 [installApk] 跳系统安装器。
 *
 * 宿主需在自己的清单声明一个 `FileProvider`，authority = `${applicationId}.fileprovider`，
 * 并配 `res/xml/file_paths.xml`（external-cache-path / files-path）。
 */
class AppUpdater(context: Context) {

    private val appContext = context.applicationContext
    private val downloadManager =
        appContext.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

    /** 是否有新版本（与当前包 versionCode 比较）。 */
    fun hasUpdate(info: UpdateInfo): Boolean = info.latestVersionCode > currentVersionCode()

    @Suppress("DEPRECATION")
    fun currentVersionCode(): Long {
        val pkg = appContext.packageManager.getPackageInfo(appContext.packageName, 0)
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            pkg.longVersionCode
        } else {
            pkg.versionCode.toLong()
        }
    }

    /**
     * 下载 APK 到外部缓存目录，[Flow] 持续吐 [DownloadStatus]（轮询 DownloadManager 进度）。
     */
    fun downloadApk(info: UpdateInfo): Flow<DownloadStatus> = flow {
        val fileName = "aiguide_${info.latestVersionName}.apk"
        val request = DownloadManager.Request(Uri.parse(info.apkUrl))
            .setTitle("AiGuide ${info.latestVersionName}")
            .setDescription("正在下载更新")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalFilesDir(appContext, null, fileName)
            .setMimeType("application/vnd.android.package-archive")

        val downloadId = downloadManager.enqueue(request)
        Logger.d("download enqueued id=$downloadId", tag = "Update")

        var finished = false
        while (!finished) {
            val (status, percent, localUri, reason) = query(downloadId)
            when (status) {
                DownloadManager.STATUS_SUCCESSFUL -> {
                    finished = true
                    val path = localUri?.let { Uri.parse(it).path }
                        ?: File(appContext.getExternalFilesDir(null), fileName).absolutePath
                    emit(DownloadStatus.Success(path))
                }

                DownloadManager.STATUS_FAILED -> {
                    finished = true
                    emit(DownloadStatus.Failed("下载失败，原因码=$reason"))
                }

                else -> {
                    emit(DownloadStatus.Progress(percent))
                    delay(500)
                }
            }
        }
    }

    private data class DownloadState(
        val status: Int,
        val percent: Int,
        val localUri: String?,
        val reason: Int,
    )

    private fun query(downloadId: Long): DownloadState {
        val query = DownloadManager.Query().setFilterById(downloadId)
        downloadManager.query(query)?.use { c: Cursor ->
            if (c.moveToFirst()) {
                val status = c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                val total = c.getLong(c.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                val done = c.getLong(c.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                val uri = c.getString(c.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_URI))
                val reason = c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_REASON))
                val percent = if (total > 0) (done * 100 / total).toInt() else 0
                return DownloadState(status, percent, uri, reason)
            }
        }
        return DownloadState(DownloadManager.STATUS_FAILED, 0, null, -1)
    }

    /** 拉起系统安装器安装 [apkPath]（经 FileProvider 暴露给 PackageInstaller）。 */
    fun installApk(apkPath: String) {
        val file = File(apkPath)
        val uri = FileProvider.getUriForFile(
            appContext,
            "${appContext.packageName}.fileprovider",
            file,
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        appContext.startActivity(intent)
    }
}
