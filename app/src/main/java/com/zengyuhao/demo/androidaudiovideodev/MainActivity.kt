package com.zengyuhao.demo.androidaudiovideodev

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.zengyuhao.demo.androidaudiovideodev.demo01.Demo01Activity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun onBtnClick(view: View) {
        when (view.id) {
            R.id.btn_demo01 -> {
                startActivity(Intent(this, Demo01Activity::class.java))
            }
        }
    }
}
