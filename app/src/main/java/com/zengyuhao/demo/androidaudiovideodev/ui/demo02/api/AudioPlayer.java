package com.zengyuhao.demo.androidaudiovideodev.ui.demo02.api;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import java.io.File;

public class AudioPlayer {
    private File file;
    private AudioTrack audioTrack;
    private int buffSize;

    public AudioPlayer(File file) {
        this.file = file;
        buffSize = AudioTrack.getMinBufferSize(
                44100,
                AudioFormat.CHANNEL_OUT_STEREO,
                AudioFormat.ENCODING_PCM_16BIT);
    }

    private AudioTrack getAudioTrack() {
        if (null == audioTrack) {
            audioTrack = new AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    44100,
                    AudioFormat.CHANNEL_OUT_STEREO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    buffSize,
                    AudioTrack.MODE_STREAM);
        }
        return audioTrack;
    }

    public void play() {

    }

    public void pause() {

    }

    public void stop() {

    }

    private class PlayThread extends Thread {
        public final static int STATE_READY = 0;
        public final static int STATE_PLAYING = 1;
        public final static int STATE_PAUSED = 2;
        public final static int STATE_STOPPED = 3;
        private volatile int state;

        public void changeState(int newState) {

        }

        @Override
        public void run() {

        }
    }
}
