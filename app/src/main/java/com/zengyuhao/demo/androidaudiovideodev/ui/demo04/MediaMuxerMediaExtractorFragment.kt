package com.zengyuhao.demo.androidaudiovideodev.ui.demo04


import android.os.Bundle
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.zengyuhao.demo.androidaudiovideodev.R
import kotlinx.android.synthetic.main.mediamuxer_mediaextractor_fragment.*
import java.io.File
import java.security.Permission
import java.util.jar.Manifest
import kotlin.concurrent.thread

class MediaMuxerMediaExtractorFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.mediamuxer_mediaextractor_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val docDir = view.context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        val sampleVideo = File(docDir, "SampleVideo_1280x720_5mb.mp4")
//        val sampleVideo = File(docDir, "sample.mp4")
        val videoOutput = File(docDir, "video_output")
        val audioOutput = File(docDir, "audio_output")
        val muxedVideo = File(docDir, "MuxedVideo_1280x720_10mb.mp4")

        val extractTask = MediaExtractTask(sampleVideo, videoOutput, audioOutput)
        val muxTask = MediaMuxTask(sampleVideo, sampleVideo, muxedVideo)
        btnExtract.setOnClickListener {
            Thread(extractTask).start()
        }

        btnMux.setOnClickListener {
            Thread(muxTask).start()
        }

        ActivityCompat.requestPermissions(activity!!, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 110)
    }
}
