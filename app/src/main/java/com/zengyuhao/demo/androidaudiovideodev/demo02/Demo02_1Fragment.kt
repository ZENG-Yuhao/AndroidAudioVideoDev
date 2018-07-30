package com.zengyuhao.demo.androidaudiovideodev.demo02


import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.zengyuhao.demo.androidaudiovideodev.R
import com.zengyuhao.demo.androidaudiovideodev.demo02.api.AudioRecorder
import kotlinx.android.synthetic.main.fragment_demo02_1.*
import java.io.File

class Demo02_1Fragment : Fragment() {
    companion object {
        const val MAX_PROGRESS = 90
    }

    private lateinit var mRecorder: AudioRecorder

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_demo02_1, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        checkRecordAudioPermission()
        val docDir = context!!.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        Log.d("Demo02_1Fragment", "path = ${docDir.path}")
        val file = File(docDir, "demo02.wav")
        mRecorder = AudioRecorder(file)
        mRecorder.setVolumeCallback {
            Log.d("TAG", "--->volume $it")
            progressBar.progress = if (it < MAX_PROGRESS) it.toInt() else MAX_PROGRESS
        }

        btnRecord.setOnClickListener {
            mRecorder.start()
        }

        btnRecordStop.setOnClickListener {
            mRecorder.stop()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mRecorder.stop()
    }

    private fun checkRecordAudioPermission() {
        val recordPermission = ContextCompat.checkSelfPermission(activity!!, android.Manifest.permission.RECORD_AUDIO)
        val storagePermission = ContextCompat.checkSelfPermission(activity!!, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (PackageManager.PERMISSION_GRANTED != recordPermission ||
                PackageManager.PERMISSION_GRANTED != storagePermission) {
            ActivityCompat.requestPermissions(
                    activity!!,
                    arrayOf(android.Manifest.permission.RECORD_AUDIO, android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    0
            )
        }
    }
}
