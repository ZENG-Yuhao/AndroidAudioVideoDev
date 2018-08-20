package com.zengyuhao.demo.androidaudiovideodev.ui.demo01

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.zengyuhao.demo.androidaudiovideodev.R
import kotlin.concurrent.thread

class MySurfaceView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : SurfaceView(context, attrs, defStyleAttr), SurfaceHolder.Callback {
    companion object {
        const val TAG = "MySurfaceView"
    }

    init {
        holder.addCallback(this)
    }

    @Volatile
    private var surfaceAvailable = false
    private val paint = Paint().apply {
                this.color = resources.getColor(android.R.color.holo_blue_dark)
    }
    private lateinit var bitmap: Bitmap

    init {
        viewTreeObserver.addOnGlobalLayoutListener {
            bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.super_android_man)
            Bitmap.createScaledBitmap(bitmap, width, height, false)
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        Log.d(TAG, "Demo01|$TAG - surfaceCreated()")
        surfaceAvailable = true
        thread(true) {
            drawBitmap()
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
        Log.d(TAG, "Demo01|$TAG - surfaceChanged(format: $format, width: $width, height: $height)")
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        Log.d(TAG, "Demo01|$TAG - surfaceDestroyed()")
        surfaceAvailable = false
    }

    fun drawBitmap() {
        if (!surfaceAvailable) return
        val canvas = holder.lockCanvas()
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        holder.unlockCanvasAndPost(canvas)
    }
}