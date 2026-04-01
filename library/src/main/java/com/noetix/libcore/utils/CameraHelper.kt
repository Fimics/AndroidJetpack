package com.noetix.libcore.utils

import android.content.Context
import android.graphics.ImageFormat
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.util.Size

class CameraHelper {
    private val tag="nx_app"
    private val size = Size(160,120)

    fun getTargetCameraId():String{
        var cid =""
        val cameraManager = AppGlobals.getApplication().getSystemService(Context.CAMERA_SERVICE) as CameraManager

        // 获取所有摄像头ID
        val cameraIds = cameraManager.cameraIdList


        // 打印所有摄像头ID
        for (cameraId in cameraIds) {


            // 获取摄像头特性
            val characteristics = cameraManager.getCameraCharacteristics(cameraId)


            // 获取摄像头厂商信息（推测）
            val manufacturer =
                if (characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK
                ) "Back Camera" else "Front Camera" // 这里只是简单的分类


            // 获取摄像头的其他信息
            val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
            val orientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)
            val hardwareLevel =
                characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL).toString()


            // 打印获取到的摄像头信息
            KLog.d(tag, "Camera ID: $cameraId")
            KLog.d(tag, "Manufacturer: $manufacturer")
            KLog.d(tag, "Facing: " + (if (facing == CameraCharacteristics.LENS_FACING_BACK) "Back" else "Front"))
            KLog.d(tag, "Sensor Orientation: $orientation") // 摄像头传感器方向
            KLog.d(tag, "Hardware Level: $hardwareLevel") // 硬件级别


            // 检查分辨率支持
            val streamMap = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)



            for (s in streamMap!!.getOutputSizes(ImageFormat.YUV_420_888)) {
                KLog.d(tag,s.toString())
                if (s.width==size.width && s.height==size.height){
                    cid =cameraId
                    break
                }
            }
        }
        return cid
    }
}