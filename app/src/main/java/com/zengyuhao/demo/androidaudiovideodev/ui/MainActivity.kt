package com.zengyuhao.demo.androidaudiovideodev.ui

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.zengyuhao.demo.androidaudiovideodev.R
import com.zengyuhao.demo.androidaudiovideodev.ui.demo01.Demo01Activity
import com.zengyuhao.demo.androidaudiovideodev.ui.demo02.Demo02Activity
import com.zengyuhao.demo.androidaudiovideodev.ui.demo03.Demo03Activity
import com.zengyuhao.demo.androidaudiovideodev.ui.demo04.Demo04Activity
import com.zengyuhao.demo.androidaudiovideodev.ui.demo05.OpenGLESActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
    }

    fun onBtnClick(view: View) {
        when (view.id) {
            R.id.btn_demo01 -> {
                startActivity(Intent(this, Demo01Activity::class.java))
            }
            R.id.btn_demo02 -> {
                startActivity(Intent(this, Demo02Activity::class.java))
            }
            R.id.btn_demo03 -> {
                startActivity(Intent(this, Demo03Activity::class.java))
            }
            R.id.btn_demo04 -> {
                startActivity(Intent(this, Demo04Activity::class.java))
            }
            R.id.btn_demo05 -> {
                startActivity(Intent(this, OpenGLESActivity::class.java))
            }
        }
    }
}
