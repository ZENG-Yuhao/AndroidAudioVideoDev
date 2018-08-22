package com.zengyuhao.demo.androidaudiovideodev.ui.demo04

import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.nio.ByteBuffer

class MediaMuxTask(
        val videoFileIn: File,
        val audioFileIn: File,
        val output: File
) : Runnable {
    companion object {
        private const val TAG = "MediaMuxTask"
    }

    override fun run() {
        var videoExtractor: MediaExtractor? = null
        var audioExtractor: MediaExtractor? = null
        var mediaMuxer: MediaMuxer? = null
        try {
            // setup video extractor
            Log.d(TAG, "---> setup video extractor")
            videoExtractor = MediaExtractor()
            videoExtractor.setDataSource(videoFileIn.absolutePath)
            var videoTrackIndex = -1
            var videoFormat: MediaFormat
            for (i in 0 until videoExtractor.trackCount) {
                videoFormat = videoExtractor.getTrackFormat(i)
                val mimeType = videoFormat.getString(MediaFormat.KEY_MIME)
                if (mimeType.startsWith("video/")) {
                    videoTrackIndex = i
                    break
                }
            }

            // setup audio extractor
//            Log.d(TAG, "---> setup audio extractor")
//            audioExtractor = MediaExtractor()
//            audioExtractor.setDataSource(audioFileIn.absolutePath)
//            var audioTrackIndex = -1
//            var audioFormat: MediaFormat
//            for (i in 0 until audioExtractor.trackCount) {
//                audioFormat = audioExtractor.getTrackFormat(i)
//                val mimeType = audioFormat.getString(MediaFormat.KEY_MIME)
//                if (mimeType.startsWith("audio/")) {
//                    audioTrackIndex = i
//                    break
//                }
//            }

            // create muxer and add tracks
            Log.d(TAG, "---> create muxer and add tracks")
            mediaMuxer = MediaMuxer(output.path, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            val muxerVideoTrackIndex = mediaMuxer.addTrack(videoExtractor.getTrackFormat(videoTrackIndex))
//            val muxerAudioTrackIndex = mediaMuxer.addTrack(audioExtractor.getTrackFormat(audioTrackIndex))
            mediaMuxer.start()

            val maxInputSize = videoExtractor.getTrackFormat(videoTrackIndex).getInteger(MediaFormat.KEY_MAX_INPUT_SIZE)
            val byteBuffer = ByteBuffer.allocate(maxInputSize)
            val sampleTime = getVideoSampleTime(videoExtractor, videoTrackIndex, byteBuffer)

            // mux video track
            videoExtractor = MediaExtractor()
            videoExtractor.setDataSource(videoFileIn.absolutePath)
            Log.d(TAG, "---> mux video track")
            videoExtractor.selectTrack(videoTrackIndex)
            val videoBufferInfo = MediaCodec.BufferInfo()
            while (true) {
                val read = videoExtractor.readSampleData(byteBuffer, 0)
                if (read < 0) break
                videoBufferInfo.size = read
                videoBufferInfo.presentationTimeUs += sampleTime
                videoBufferInfo.offset = 0
                videoBufferInfo.flags = videoExtractor.sampleFlags
                mediaMuxer.writeSampleData(muxerVideoTrackIndex, byteBuffer, videoBufferInfo)
                videoExtractor.advance()
            }

            // mux audio track
//            Log.d(TAG, "---> mux audio track")
//            audioExtractor.selectTrack(audioTrackIndex)
//            val audioBufferInfo = MediaCodec.BufferInfo()
//            while (true) {
//                val read = audioExtractor.readSampleData(byteBuffer, 0)
//                if (read < 0) break
//                audioBufferInfo.size = read
//                audioBufferInfo.presentationTimeUs += sampleTime
//                audioBufferInfo.offset = 0
//                audioBufferInfo.flags = audioExtractor.sampleFlags
//                mediaMuxer.writeSampleData(muxerAudioTrackIndex, byteBuffer, audioBufferInfo)
//                audioExtractor.advance()
//            }

            Log.d(TAG, "---> finished.")
        } finally {
            videoExtractor?.release()
            audioExtractor?.release()
            mediaMuxer?.stop()
            mediaMuxer?.release()
        }
    }

    private fun getVideoSampleTime(mediaExtractor: MediaExtractor, trackIndex: Int, buffer: ByteBuffer): Long {
        mediaExtractor.selectTrack(trackIndex)
        mediaExtractor.readSampleData(buffer, 0)
        if (MediaExtractor.SAMPLE_FLAG_SYNC == mediaExtractor.sampleFlags) {
            mediaExtractor.advance()
        }
        mediaExtractor.readSampleData(buffer, 0)
        val firstVideoPts = mediaExtractor.sampleTime
        mediaExtractor.advance()
        mediaExtractor.readSampleData(buffer, 0)
        val secondVideoPts = mediaExtractor.sampleTime
        mediaExtractor.unselectTrack(trackIndex)
        return Math.abs(secondVideoPts - firstVideoPts)
    }
}