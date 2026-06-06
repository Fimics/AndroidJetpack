package com.mic.guide.support.update

/** 版本检查结果（由业务从自家后端拉取后构造）。 */
data class UpdateInfo(
    val latestVersionCode: Long,
    val latestVersionName: String,
    val apkUrl: String,
    val releaseNotes: String,
    val forceUpdate: Boolean = false,
)

/** 下载进度状态。 */
sealed interface DownloadStatus {
    data class Progress(val percent: Int) : DownloadStatus
    data class Success(val apkPath: String) : DownloadStatus
    data class Failed(val reason: String) : DownloadStatus
}
