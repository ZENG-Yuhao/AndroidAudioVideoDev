package com.zengyuhao.demo.androidaudiovideodev.demo03.camera2

import android.content.Context
import android.graphics.ImageFormat
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.util.Log

class CameraInfoExporter(private val context: Context) {
    private val n = "\n"
    private val space = "-----"

    fun log() {
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraInfo = StringBuilder("========== CAMERA INFO =========").append(n)
        for (cameraId in cameraManager.cameraIdList) {
            cameraInfo.append("Camera $cameraId").append(n)
            val characteristics = cameraManager.getCameraCharacteristics(cameraId)
            val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
            if (null != facing) {
                when (facing) {
                    CameraCharacteristics.LENS_FACING_FRONT -> {
                        cameraInfo.append(space).append("Facing : Front").append(n)
                    }
                    CameraCharacteristics.LENS_FACING_BACK -> {
                        cameraInfo.append(space).append("Facing : Back").append(n)
                    }
                    CameraCharacteristics.LENS_FACING_EXTERNAL -> {
                        cameraInfo.append(space).append("Facing : External").append(n)
                    }
                }
            }
            val map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            if (null == map) {
                continue
            }
            val sizes = map.getOutputSizes(ImageFormat.YUV_420_888)
            cameraInfo.append(space).append("Support sizes : ${sizes.size}").append(n)
            for (size in sizes) {
                cameraInfo.append(space).append(space).append("w:${size.width} h:${size.height}").append(n)
            }
        }

        Log.d("CameraInfoExporter", cameraInfo.toString())
    }
}