package com.zengyuhao.demo.androidaudiovideodev.demo02


import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.zengyuhao.demo.androidaudiovideodev.R
import kotlinx.android.synthetic.main.fragment_demo02.*

class Demo02Fragment : Fragment() {
    companion object {
        private const val PRINT_RAW_DATA = 1
    }

    private lateinit var audioRecord: AudioRecord
    private var isRecording = false
    private var buffSize = 0
    private val uiHandler = Handler {
        when (it.what) {
            PRINT_RAW_DATA -> {
                val msg = it.obj as Double
                txtRawData.append(msg.toString() + "\n")
                true
            }
            else -> false
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_demo02, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        btnRecord.setOnClickListener {
            record()
            txtRawData.append("Start Recording...\n")
        }
        btnRecordStop.setOnClickListener {
            stopRecording()
            txtRawData.append("Recording stopped.")
        }
        checkRecordAudioPermission()
        initAudioRecord()
    }

    private fun initAudioRecord() {
        buffSize = AudioRecord.getMinBufferSize(44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
        Log.d("TAG", "minBufferSize = $buffSize")
        audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                44100,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                buffSize
        )
    }

    private fun checkRecordAudioPermission() {
        val permissionCheck = ContextCompat.checkSelfPermission(activity!!, android.Manifest.permission.RECORD_AUDIO)
        if (PackageManager.PERMISSION_GRANTED != permissionCheck) {
            ActivityCompat.requestPermissions(activity!!, arrayOf(android.Manifest.permission.RECORD_AUDIO), 0)
        }
    }

    private fun record() {
        if (audioRecord.state == AudioRecord.STATE_INITIALIZED && audioRecord.state != AudioRecord.RECORDSTATE_RECORDING) {
            RecordThread(uiHandler).start()
        }
    }

    private fun stopRecording() {
        isRecording = false
    }

    inner class RecordThread(private val uiHandler: Handler) : Thread() {
        override fun run() {
            Log.d("TAG", "Start recording...")
            audioRecord.startRecording()
            var buff = ByteArray(buffSize)
            isRecording = true
            while (isRecording) {
                Log.d("TAG", "Recording loop...")
                val read = audioRecord.read(buff, 0, buff.size)
                if (read == AudioRecord.ERROR_INVALID_OPERATION) {
                    Log.d("TAG", "Error Invalid Operation.")
                    Toast.makeText(activity!!, "Error Invalid Operation", Toast.LENGTH_LONG).show()
                    return
                }
                if (read > 0) {
                    var sum = 0.0
                    for (i in 0..(read - 1)) {
                        sum += buff[i] * buff[i]
                    }
                    val amplitude = sum / read
                    Log.d("TAG", "voice read = $amplitude")
                    Message().apply {
                        what = PRINT_RAW_DATA
                        obj = amplitude
                        uiHandler.sendMessage(this)
                    }
                }
            }
            Log.d("TAG", "Stop recording.")
            audioRecord.stop()
        }
    }
}
