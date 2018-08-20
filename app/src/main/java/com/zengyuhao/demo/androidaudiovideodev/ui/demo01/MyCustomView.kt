package com.zengyuhao.demo.androidaudiovideodev.ui.demo01

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.zengyuhao.demo.androidaudiovideodev.R

class MyCustomView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private val paint = Paint().apply {
        this.color = resources.getColor(android.R.color.holo_purple)
    }
    private lateinit var bitmap: Bitmap

    init {
        viewTreeObserver.addOnGlobalLayoutListener {
            bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.super_android_man)
            Bitmap.createScaledBitmap(bitmap, width, height, false)
        }
    }

    override fun onDraw(canvas: Canvas?) {
        canvas?.let {
            it.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
            it.drawBitmap(bitmap, 0f, 0f, paint)
        }
    }
}