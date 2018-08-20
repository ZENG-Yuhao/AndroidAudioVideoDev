package com.zengyuhao.demo.androidaudiovideodev.ui.demo02.api;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.IntDef;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class AudioRecorder {
    public final static String TAG = "AudioRecorder";
    public final static int STATE_READY = 0;
    public final static int STATE_RECORDING = 1;
    public final static int STATE_STOPPED = 2;

    @IntDef({STATE_READY, STATE_RECORDING, STATE_STOPPED})
    @Retention(RetentionPolicy.SOURCE)
    public @interface RecordState {
    }

    private final static int SAMPLE_RATE_IN_HZ = 44100;
    private final int byteBuffSize;
    private File fileOut;
    private @RecordState volatile int state = STATE_READY;
    /**
     * Call {@link #getAudioRecord()} instead of using directly the instance.
     */
    private AudioRecord audioRecord;
    /**
     * Call {@link #getHandler()} instead of using directly the instance.
     */
    private Handler mainThreadHandler;
    /**
     * Call {@link #getRecordingTask()} instead of using directly the instance.
     */
    private Runnable recordingTask;
    private Thread recordingThread;
    private VolumeCallback volumeCallback;

    @MainThread
    public AudioRecorder(@NonNull File fileOut) {
        byteBuffSize = AudioRecord.getMinBufferSize(
                SAMPLE_RATE_IN_HZ,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        this.fileOut = fileOut;
    }

    private AudioRecord getAudioRecord() {
        if (null == audioRecord) {
            audioRecord = new AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    SAMPLE_RATE_IN_HZ,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    byteBuffSize);
        }
        return audioRecord;
    }

    private Handler getHandler() {
        if (null == mainThreadHandler) {
            mainThreadHandler = new Handler(Looper.getMainLooper());
        }
        return mainThreadHandler;
    }

    private Runnable getRecordingTask() {
        if (null == recordingTask) {
            recordingTask = new RecordingTask();
        }
        return recordingTask;
    }

    public void start() {
        if (STATE_READY == state) {
            state = STATE_RECORDING;
            recordingThread = new Thread(getRecordingTask());
            recordingThread.start();
        } else {
            throw new RuntimeException("Trying to start a recorder that is running or closed.");
        }
    }

    public void stop() {
        if (STATE_RECORDING == state) {
            state = STATE_STOPPED;
            if (null != audioRecord) {
                audioRecord.stop();
                audioRecord.release();
                audioRecord = null;
            }
        }
    }

    public void setVolumeCallback(@Nullable VolumeCallback callback) {
        this.volumeCallback = callback;
    }


    /**
     * VolumeCallback
     */
    public interface VolumeCallback {
        void onUpdateVolume(long volume);
    }


    /**
     * RecordingTask
     */
    private class RecordingTask implements Runnable {

        @Override
        public void run() {
            FileOutputStream fos = null;
            FileChannel channel = null;
            try {
                fos = new FileOutputStream(fileOut);
                channel = fos.getChannel();

                // Wav file header occupy the first 44 bytes (from 0 to 43), data starts from 44.
                channel.position(44);
                final ByteBuffer buff = ByteBuffer.allocate(byteBuffSize);
                long totalBytes = 0;
                getAudioRecord().startRecording();
                while (STATE_RECORDING == state) {
                    int read = getAudioRecord().read(buff.array(), 0, buff.capacity());
                    buff.clear();
                    if (AudioRecord.ERROR_INVALID_OPERATION != read && read > 0) {
                        totalBytes += read;
                        channel.write(buff);
                        updateVolume(calcVolume(buff.array(), read));
                    }
                }

                // Recording stopped, Write .wav file header, byte[0] to byte[43]
                ByteBuffer headerBuff = ByteBuffer.wrap(
                        wavFileHeaderBytes(totalBytes, SAMPLE_RATE_IN_HZ, getAudioRecord().getChannelCount())
                );
                channel.write(headerBuff, 0);
            } catch (IOException e) {
                Log.d(TAG, "I/O | " + e.toString());
            } finally {
                try {
                    if (null != channel) channel.close();
                    if (null != fos) fos.close();
                } catch (IOException e) {
                    Log.d(TAG, "Close channel/stream | " + e.toString());
                }
            }
        }

        private long calcVolume(byte[] buff, int readSize) {
            int len = Math.min(buff.length, readSize);
            long pow = 0;
            for (int i = 0; i < len; i = i + 2) {
                int data = buff[i] << 8 + buff[i + 1];
                pow += data * data;
            }
            long mean = pow / (len / 2);
            return (long) (10 * Math.log10(mean));
        }

        private void updateVolume(long volume) {
            if (null != volumeCallback) {
                getHandler().post(
                        () -> volumeCallback.onUpdateVolume(volume)
                );
            }
        }

        /**
         * 任何一种文件在头部添加相应的头文件才能够确定的表示这种文件的格式，
         * wave是RIFF文件结构，每一部分为一个chunk，其中有RIFF WAVE chunk，
         * FMT Chunk，Fact chunk,Data chunk,其中Fact chunk是可以选择的.
         *
         * <a href="http://www.cnblogs.com/renhui/p/7457321.html">Reference</a>
         *
         * @param pcmAudioLen    不包括header的音频数据总长度
         * @param sampleRateInHz 采样率,也就是录制时使用的频率
         * @param numChannel     audioRecord的频道数量
         */
        private byte[] wavFileHeaderBytes(long pcmAudioLen, long sampleRateInHz, int numChannel) {
            long totalDataLen = pcmAudioLen + 36; // 不包含前8个字节的WAV文件总长度
            long byteRate = sampleRateInHz * 2 * numChannel;
            byte[] header = new byte[44];
            header[0] = 'R'; // RIFF
            header[1] = 'I';
            header[2] = 'F';
            header[3] = 'F';

            header[4] = (byte) (totalDataLen & 0xff);//数据大小
            header[5] = (byte) ((totalDataLen >> 8) & 0xff);
            header[6] = (byte) ((totalDataLen >> 16) & 0xff);
            header[7] = (byte) ((totalDataLen >> 24) & 0xff);

            header[8] = 'W';//WAVE
            header[9] = 'A';
            header[10] = 'V';
            header[11] = 'E';
            //FMT Chunk
            header[12] = 'f'; // 'fmt '
            header[13] = 'm';
            header[14] = 't';
            header[15] = ' ';//过渡字节
            //数据大小
            header[16] = 16; // 4 bytes: size of 'fmt ' chunk
            header[17] = 0;
            header[18] = 0;
            header[19] = 0;
            //编码方式 10H为PCM编码格式
            header[20] = 1; // format = 1
            header[21] = 0;
            //通道数
            header[22] = (byte) numChannel;
            header[23] = 0;
            //采样率，每个通道的播放速度
            header[24] = (byte) (sampleRateInHz & 0xff);
            header[25] = (byte) ((sampleRateInHz >> 8) & 0xff);
            header[26] = (byte) ((sampleRateInHz >> 16) & 0xff);
            header[27] = (byte) ((sampleRateInHz >> 24) & 0xff);
            //音频数据传送速率,采样率*通道数*采样深度/8
            header[28] = (byte) (byteRate & 0xff);
            header[29] = (byte) ((byteRate >> 8) & 0xff);
            header[30] = (byte) ((byteRate >> 16) & 0xff);
            header[31] = (byte) ((byteRate >> 24) & 0xff);
            // 确定系统一次要处理多少个这样字节的数据，确定缓冲区，通道数*采样位数
            header[32] = (byte) (2 * numChannel);
            header[33] = 0;
            //每个样本的数据位数
            header[34] = 16;
            header[35] = 0;
            //Data chunk
            header[36] = 'd';//data
            header[37] = 'a';
            header[38] = 't';
            header[39] = 'a';
            header[40] = (byte) (pcmAudioLen & 0xff);
            header[41] = (byte) ((pcmAudioLen >> 8) & 0xff);
            header[42] = (byte) ((pcmAudioLen >> 16) & 0xff);
            header[43] = (byte) ((pcmAudioLen >> 24) & 0xff);
            return header;
        }
    }
}
