package com.mic.guide.support.camera

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.mic.guide.lib.log.Logger
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * CameraX 封装门面（§2.2 硬件能力下沉 support）。
 *
 * 基于 [LifecycleCameraController] 一站式管理「预览 + 拍照 + 分析」，业务只需：
 * 1. `manager.bind(lifecycleOwner, previewView)`；
 * 2. `manager.takePicture(file)`（suspend）拿到照片；
 * 3. `switchCamera()` / `enableTorch()` 控制前后摄/闪光。
 *
 * 注意：调用方需先经 `support-permission` 申请到 `CAMERA` 运行期权限再 [bind]。
 */
class CameraManager(context: Context) {

    private val appContext = context.applicationContext
    private val mainExecutor = ContextCompat.getMainExecutor(appContext)

    private val controller: LifecycleCameraController =
        LifecycleCameraController(appContext).apply {
            // 同时开启预览 + 拍照用例（如需实时分析可加 IMAGE_ANALYSIS 并 setImageAnalysisAnalyzer）
            setEnabledUseCases(CameraController.IMAGE_CAPTURE)
            cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        }

    /** 把相机绑定到生命周期与预览控件；离开页面时随 owner 自动解绑。 */
    fun bind(owner: LifecycleOwner, previewView: PreviewView) {
        controller.bindToLifecycle(owner)
        previewView.controller = controller
        Logger.d("camera bound", tag = "Camera")
    }

    /** 解绑（释放相机），通常交给生命周期自动处理，必要时手动调。 */
    fun unbind() {
        controller.unbind()
    }

    /** 前后摄切换。 */
    fun switchCamera() {
        controller.cameraSelector =
            if (controller.cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
                CameraSelector.DEFAULT_FRONT_CAMERA
            } else {
                CameraSelector.DEFAULT_BACK_CAMERA
            }
    }

    /** 开关闪光灯（需当前摄像头支持）。 */
    fun enableTorch(enabled: Boolean) {
        controller.enableTorch(enabled)
    }

    /** 拍照并保存到 [outputFile]，挂起到回调返回；失败抛异常。 */
    suspend fun takePicture(outputFile: File): File = suspendCancellableCoroutine { cont ->
        val options = ImageCapture.OutputFileOptions.Builder(outputFile).build()
        controller.takePicture(
            options,
            mainExecutor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(results: ImageCapture.OutputFileResults) {
                    Logger.d("picture saved: ${outputFile.absolutePath}", tag = "Camera")
                    if (cont.isActive) cont.resume(outputFile)
                }

                override fun onError(exception: ImageCaptureException) {
                    Logger.e("takePicture error", exception, tag = "Camera")
                    if (cont.isActive) cont.resumeWithException(exception)
                }
            },
        )
    }
}
