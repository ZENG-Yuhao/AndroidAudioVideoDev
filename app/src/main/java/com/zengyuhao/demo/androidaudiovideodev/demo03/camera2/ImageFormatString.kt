package com.zengyuhao.demo.androidaudiovideodev.demo03.camera2

import android.graphics.ImageFormat

object ImageFormatString {
    fun from(format: Int) = when (format) {
        ImageFormat.YUV_420_888 -> "YUV_420_888"
        ImageFormat.DEPTH16 -> "DEPTH16"
        ImageFormat.DEPTH_POINT_CLOUD -> "DEPTH_POINT_CLOUD"
        ImageFormat.FLEX_RGBA_8888 -> "FLEX_RGBA_8888"
        ImageFormat.JPEG -> "JPEG"
        ImageFormat.NV16 -> "NV16"
        ImageFormat.NV21 -> "NV21"
        ImageFormat.YUV_422_888 -> "YUV_422_888"
        ImageFormat.YUV_444_888 -> "YUV_444_888"
        ImageFormat.RGB_565 -> "RGB_565"
        ImageFormat.YUY2 -> "YUY2"
        ImageFormat.YV12 -> "YV12"
        else -> "UNKNOWN"
    }
}