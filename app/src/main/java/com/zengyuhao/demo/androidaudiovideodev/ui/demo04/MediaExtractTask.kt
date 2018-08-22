package com.zengyuhao.demo.androidaudiovideodev.ui.demo04

import android.media.MediaExtractor
import android.media.MediaFormat
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.channels.FileChannel

/**
 * MediaExtractTask
 */
class MediaExtractTask(
        val input: File,
        val videoFileOut: File,
        val audioFileOut: File
) : Runnable {
    companion object {
        private const val TAG = "MediaExtractTask"
    }

    override fun run() {
        var mediaExtractor: MediaExtractor? = null
        var videoOS: FileOutputStream? = null
        var audioOS: FileOutputStream? = null
        var videoOutChannel: FileChannel? = null
        var audioOutChannel: FileChannel? = null
        try {
            videoOS = FileOutputStream(videoFileOut)
            audioOS = FileOutputStream(audioFileOut)
            videoOutChannel = videoOS.channel
            audioOutChannel = audioOS.channel

            // Step 1: set source file path
            Log.d(TAG, "---> set source file path")
            mediaExtractor = MediaExtractor()
            mediaExtractor.setDataSource(input.absolutePath)

            // Step 2: find video track index and audio track index
            Log.d(TAG, "---> find video track index and audio track index")
            var videoTrackIndex = -1
            var audioTrackIndex = -1
            for (i in 0 until mediaExtractor.trackCount) {
                val trackFormat = mediaExtractor.getTrackFormat(i)
                val mimeType = trackFormat.getString(MediaFormat.KEY_MIME)
                when {
                    mimeType.startsWith("video/") -> {
                        videoTrackIndex = i
                    }
                    mimeType.startsWith("audio/") -> {
                        audioTrackIndex = i
                    }
                }
            }

            val buffer = ByteBuffer.allocate(500 * 1024)
            // Step 3: select video track and output
            Log.d(TAG, "---> select video track and output")
            mediaExtractor.selectTrack(videoTrackIndex)
            while (true) {
                val read = mediaExtractor.readSampleData(buffer, 0)
                if (read < 0) break
                buffer.clear()
                videoOutChannel.write(buffer)
                mediaExtractor.advance()
            }

            // Step 4: select audio track and output
            Log.d(TAG, "---> select audio track and output")
            mediaExtractor.selectTrack(audioTrackIndex)
            while (true) {
                val read = mediaExtractor.readSampleData(buffer, 0)
                if (read < 0) break
                audioOutChannel.write(buffer)
                buffer.clear()
                mediaExtractor.advance()
            }

            Log.d(TAG, "---> finished.")
        } finally {
            videoOutChannel?.close()
            audioOutChannel?.close()
            videoOS?.close()
            audioOS?.close()
            mediaExtractor?.release()
        }
    }
}