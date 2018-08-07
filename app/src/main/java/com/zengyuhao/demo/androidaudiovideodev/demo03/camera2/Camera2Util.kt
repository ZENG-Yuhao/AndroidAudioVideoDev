package com.zengyuhao.demo.androidaudiovideodev.demo03.camera2

import android.content.Context
import android.os.Environment
import android.util.Size

object Camera2Util {

    @JvmStatic
    fun chooseVideoSize(choices: Array<Size>): Size {
        return choices.firstOrNull {
            it.height <= 1080 && it.width <= 1920
        } ?: choices[choices.size - 1]
    }

    @JvmStatic
    fun choosePreviewSize(choices: Array<Size>): Size {
        return choices.firstOrNull {
            it.height <= 1080 && it.width <= 1920
        } ?: choices[choices.size - 1]
    }


    @JvmStatic
    fun getVideoFilePath(context: Context?): String {
        val filename = "${System.currentTimeMillis()}.mp4"
        val dir = context?.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)

        return if (dir == null) {
            filename
        } else {
            "${dir.absolutePath}/$filename"
        }
    }
//    fun calculateTransform(previewSize: Size, w: Int, h: Int): Matrix {
//
//    }

}