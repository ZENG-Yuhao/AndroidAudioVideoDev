package com.zengyuhao.demo.androidaudiovideodev.ui.demo04

import android.media.MediaExtractor
import android.media.MediaFormat
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.channels.FileChannel

/**
 * MediaExtractTask
 */
class MediaExtractTask(
        val sourcePath: String,
        val outVideoFile: File,
        val outAudioFile: File
) : Runnable {

    override fun run() {
        var mediaExtractor: MediaExtractor? = null
        var videoOS: FileOutputStream? = null
        var audioOS: FileOutputStream? = null
        var videoOutChannel: FileChannel? = null
        var audioOutChannel: FileChannel? = null
        try {
            videoOS = FileOutputStream(outVideoFile)
            audioOS = FileOutputStream(outAudioFile)
            videoOutChannel = videoOS.channel
            audioOutChannel = audioOS.channel

            // Step 1: set source file path
            mediaExtractor = MediaExtractor()
            mediaExtractor.setDataSource(sourcePath)

            // Step 2: find video track index and audio track index
            var videoTrackIndex = -1
            var audioTrackIndex = -1
            for (i in 0..mediaExtractor.trackCount) {
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
            mediaExtractor.selectTrack(videoTrackIndex)
            while (true) {
                val read = mediaExtractor.readSampleData(buffer, 0)
                if (read < 0) break
                videoOutChannel.write(buffer)
                buffer.clear()
                mediaExtractor.advance()
            }

            // Step 4: select audio track and output
            mediaExtractor.selectTrack(audioTrackIndex)
            while (true) {
                val read = mediaExtractor.readSampleData(buffer, 0)
                if (read < 0) break
                audioOutChannel.write(buffer)
                buffer.clear()
                mediaExtractor.advance()
            }

        } finally {
            videoOutChannel?.close()
            audioOutChannel?.close()
            videoOS?.close()
            audioOS?.close()
            mediaExtractor?.release()
        }
    }
}