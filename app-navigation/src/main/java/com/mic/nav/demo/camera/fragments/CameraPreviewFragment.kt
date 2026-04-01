package com.noetix.robotics.demo.camera.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.noetix.robotics.R
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraPreviewFragment : Fragment(R.layout.fragment_camera_preview) {

    private lateinit var viewFinder: PreviewView
    private lateinit var tvResolution: TextView
    private lateinit var tvFps: TextView

    private lateinit var cameraExecutor: ExecutorService

    private var frameCount = 0
    private var lastFpsTimestamp = System.currentTimeMillis()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        viewFinder = view.findViewById(R.id.view_finder)
        tvResolution = view.findViewById(R.id.tv_resolution)
        tvFps = view.findViewById(R.id.tv_fps)

        cameraExecutor = Executors.newSingleThreadExecutor()

        startCamera()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            // 把 cameraProvider 绑定到生命周期上
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // 预览用例
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewFinder.surfaceProvider)
                }

            // 图像分析用例（用于计算 FPS）
            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor) { imageProxy ->
                        calculateFps()
                        imageProxy.close() // 记得关闭 imageProxy，否则阻塞下一帧
                    }
                }

            // 默认选择后置摄像头（可以根据实际情况改为前置）
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // 在重新绑定前解绑所有用例
                cameraProvider.unbindAll()

                // 绑定用例到相机
                val camera = cameraProvider.bindToLifecycle(
                    viewLifecycleOwner, cameraSelector, preview, imageAnalyzer
                )

                // 从 Camera 获取当前选择的分辨率信息
                // CameraX 的 Preview 分辨率取决于其所绑定的 Surface，可以从 resolutionInfo 获取
                preview.resolutionInfo?.let { resInfo ->
                    val resolutionText = "分辨率: ${resInfo.resolution.width} x ${resInfo.resolution.height}"
                    requireActivity().runOnUiThread {
                        tvResolution.text = resolutionText
                    }
                }

            } catch (exc: Exception) {
                Log.e("CameraPreview", "用例绑定失败", exc)
                requireActivity().runOnUiThread {
                    tvResolution.text = "摄像头加载失败"
                    tvFps.text = ""
                }
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun calculateFps() {
        frameCount++
        val currentTimestamp = System.currentTimeMillis()
        val difference = currentTimestamp - lastFpsTimestamp

        // 每隔 1 秒更新一次 FPS 显示
        if (difference >= 1000) {
            val fpsStr = "帧率: $frameCount FPS"
            activity?.runOnUiThread {
                tvFps.text = fpsStr
            }
            lastFpsTimestamp = currentTimestamp
            frameCount = 0
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor.shutdown()
    }
}
