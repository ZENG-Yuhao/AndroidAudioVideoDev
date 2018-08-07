package com.zengyuhao.demo.androidaudiovideodev.demo03

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.media.ImageReader
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.util.Log
import android.util.Size
import android.util.SparseIntArray
import android.view.*
import android.widget.Button
import android.widget.Toast
import com.example.android.camera2video.AutoFitTextureView

import com.zengyuhao.demo.androidaudiovideodev.R
import com.zengyuhao.demo.androidaudiovideodev.common.ErrorDialog
import com.zengyuhao.demo.androidaudiovideodev.demo03.camera2.Camera2Util
import com.zengyuhao.demo.androidaudiovideodev.demo03.camera2.CameraInfoExporter
import com.zengyuhao.demo.androidaudiovideodev.demo03.camera2.ImageFormatString
import java.util.*

class MediaRecorderFragment : Fragment() {
    companion object {
        private const val TAG = "MediaRecorderFragment"
        private const val REQUEST_CAMERA_PERMISSION = 0
        private const val FRAGMENT_DIALOG = "Dialog"

        private const val SENSOR_ORIENTATION_DEFAULT_DEGREES = 90
        private const val SENSOR_ORIENTATION_INVERSE_DEGREES = 270

        @JvmStatic
        private val DEFAULT_ORIENTATIONS = SparseIntArray().apply {
            append(Surface.ROTATION_0, 90)
            append(Surface.ROTATION_90, 0)
            append(Surface.ROTATION_180, 270)
            append(Surface.ROTATION_270, 180)
        }
        private val INVERSE_ORIENTATIONS = SparseIntArray().apply {
            append(Surface.ROTATION_0, 270)
            append(Surface.ROTATION_90, 180)
            append(Surface.ROTATION_180, 90)
            append(Surface.ROTATION_270, 0)
        }

        @JvmStatic
        private val VIDEO_PERMISSIONS = arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.RECORD_AUDIO)
    }

    private lateinit var btnRecord: Button
    private lateinit var btnStop: Button
    private lateinit var textureVw: AutoFitTextureView
    private lateinit var cameraManager: CameraManager
    private lateinit var previewSize: Size
    private lateinit var videoSize: Size
    private lateinit var previewRequestBuilder: CaptureRequest.Builder

    private var cameraSensorOrientation = 0
    private var backgroundHandler: Handler? = null
    private var mediaRecorder: MediaRecorder? = null
    private var backgroundThread: HandlerThread? = null
    private var cameraDevice: CameraDevice? = null
    private var captureSession: CameraCaptureSession? = null
    private var imageReader: ImageReader? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.media_recorder_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        CameraInfoExporter(activity!!).log()
        textureVw = view.findViewById(R.id.MediaRecordFragment_textureVw)
        btnRecord = view.findViewById(R.id.MediaRecordFragment_btnRecord)
        btnStop = view.findViewById(R.id.MediaRecordFragment_btnStop)

        btnRecord.setOnClickListener {
            startRecording()
            Toast.makeText(activity!!, "Start recording...", Toast.LENGTH_SHORT).show()
        }
        btnStop.setOnClickListener {
            stopRecording()
            Toast.makeText(activity!!, "Recording stopped.", Toast.LENGTH_SHORT).show()
        }

        activity!!.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }

    override fun onResume() {
        super.onResume()
        startBackgroundThread()
        if (textureVw.isAvailable) {
            openCamera(textureVw.width, textureVw.height)
        } else {
            textureVw.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {
                }

                override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {
                }

                override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
                    return true
                }

                override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
                    openCamera(width, height)
                }

            }
        }
    }

    override fun onPause() {
        closeCamera()
        stopBackgroundThread()
        super.onPause()
    }

    private fun startBackgroundThread() {
        backgroundThread = HandlerThread("Camera2Background")
        backgroundThread!!.start()
        backgroundHandler = Handler(backgroundThread!!.looper)
    }

    private fun stopBackgroundThread() {
        backgroundThread?.quitSafely()
        backgroundThread?.join()
        backgroundThread = null
        backgroundHandler = null
    }


    @SuppressLint("MissingPermission")
    private fun openCamera(w: Int, h: Int) {
        if (!hasPermissionGranted(VIDEO_PERMISSIONS)) {
            if (shouldShowRequestPermissionRationale(VIDEO_PERMISSIONS)) {
                ConfirmationDialog().show(childFragmentManager, FRAGMENT_DIALOG)
            } else {
                requestPermissions(VIDEO_PERMISSIONS, REQUEST_CAMERA_PERMISSION)
            }
            return
        }
        cameraManager = context!!.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraId = findAvailableCamera(cameraManager)
        textureVw.setAspectRatio(previewSize.height, previewSize.width)
        cameraManager.openCamera(cameraId,
                object : CameraDevice.StateCallback() {
                    override fun onOpened(camera: CameraDevice?) {
                        cameraDevice = camera
                        startPreview()
                    }

                    override fun onDisconnected(camera: CameraDevice?) {
                        cameraDevice?.close()
                        ErrorDialog.newInstance("Error while camera openin: disconnected.")
                    }

                    override fun onError(camera: CameraDevice?, error: Int) {
                        cameraDevice?.close()
                        ErrorDialog.newInstance("Error while camera opening: $error")
                                .show(childFragmentManager, FRAGMENT_DIALOG)
                    }

                },
                backgroundHandler)
    }

    private fun findAvailableCamera(cameraManager: CameraManager): String {
        for (id in cameraManager.cameraIdList) {
            val characteristics = cameraManager.getCameraCharacteristics(id);
            val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
            if (null != facing && CameraCharacteristics.LENS_FACING_FRONT == facing) {
                continue
            }
            val map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP) ?: continue
            cameraSensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)
            videoSize = Camera2Util.chooseVideoSize(
                    map.getOutputSizes(MediaRecorder::class.java)
            )
            Log.d(TAG, "videoSize (${videoSize.width}, ${videoSize.height})")
            previewSize = Camera2Util.choosePreviewSize(
                    map.getOutputSizes(textureVw.surfaceTexture::class.java)
            )
            Log.d(TAG, "previewSize (${previewSize.width}, ${previewSize.height})")
            return id
        }
        throw RuntimeException("No available camera found.")
    }

    private fun startPreview() {
        if (null == cameraDevice || !textureVw.isAvailable) {
            return
        }
        val surfaceTexture = textureVw.surfaceTexture
        surfaceTexture.setDefaultBufferSize(previewSize?.width, previewSize.height)
        val previewSurface = Surface(surfaceTexture)
        previewRequestBuilder = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
        previewRequestBuilder.addTarget(previewSurface)
        cameraDevice!!.createCaptureSession(listOf(previewSurface),
                object : CameraCaptureSession.StateCallback() {
                    override fun onConfigureFailed(session: CameraCaptureSession?) {
                        ErrorDialog.newInstance("Create session failed.")
                                .show(childFragmentManager, FRAGMENT_DIALOG)
                    }

                    override fun onConfigured(session: CameraCaptureSession?) {
                        captureSession = session
                        updatePreview()
                    }
                }, backgroundHandler)
    }

    private fun updatePreview() {
        // auto-exposure, auto-white-balance, auto-focus
        previewRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
        captureSession!!.setRepeatingRequest(previewRequestBuilder.build(), null, backgroundHandler)
    }

    private fun closePreviewSession() {
        captureSession?.stopRepeating()
        captureSession?.abortCaptures()
        captureSession?.close()
        captureSession = null
    }

    private fun prepareMediaRecorder() {
        mediaRecorder = MediaRecorder()

        val rotation = activity!!.windowManager.defaultDisplay.rotation
        when (cameraSensorOrientation) {
            SENSOR_ORIENTATION_DEFAULT_DEGREES ->
                mediaRecorder?.setOrientationHint(DEFAULT_ORIENTATIONS.get(rotation))
            SENSOR_ORIENTATION_INVERSE_DEGREES ->
                mediaRecorder?.setOrientationHint(INVERSE_ORIENTATIONS.get(rotation))
        }
        with(mediaRecorder!!) {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setVideoSource(MediaRecorder.VideoSource.SURFACE)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setOutputFile(Camera2Util.getVideoFilePath(activity!!))
            setVideoEncodingBitRate(10000000)
            setVideoFrameRate(30)
            setVideoSize(videoSize.width, videoSize.height)
            setVideoEncoder(MediaRecorder.VideoEncoder.H264)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            prepare()
        }
    }

    private fun startRecording() {
        closePreviewSession()
        prepareMediaRecorder()
        val previewTexture = textureVw.surfaceTexture
        previewTexture.setDefaultBufferSize(previewSize.width, previewSize.height)

        val previewSurface = Surface(previewTexture)
        val recordSurface = mediaRecorder!!.surface

        previewRequestBuilder = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_RECORD)
        previewRequestBuilder.addTarget(previewSurface)
        previewRequestBuilder.addTarget(recordSurface)

        imageReader = ImageReader.newInstance(previewSize.width, previewSize.height, ImageFormat.YUV_420_888, 2)
        imageReader!!.setOnImageAvailableListener({ reader ->
            val image = reader!!.acquireNextImage()
            Log.d(TAG, "image received : format=${ImageFormatString.from(image.format)} planes.size=${image.planes.size} w=${image.width} h=${image.height} timestamp=${image.timestamp}")
            image.close()
        }, backgroundHandler)
        val imageReaderSurface = imageReader!!.surface
        previewRequestBuilder.addTarget(imageReaderSurface)


        cameraDevice!!.createCaptureSession(Arrays.asList(previewSurface, recordSurface, imageReaderSurface),
                object : CameraCaptureSession.StateCallback() {
                    override fun onConfigureFailed(session: CameraCaptureSession?) {
                        ErrorDialog.newInstance("Configure recording session failed.")
                                .show(childFragmentManager, FRAGMENT_DIALOG)
                    }

                    override fun onConfigured(session: CameraCaptureSession?) {
                        captureSession = session
                        updatePreview()
                        mediaRecorder!!.start()
                    }

                }, backgroundHandler)
    }

    private fun stopRecording() {
        imageReader?.close()
        imageReader = null


        mediaRecorder?.stop()
        mediaRecorder?.reset()
        startPreview()
    }

    private fun closeCamera() {
        closePreviewSession()
        mediaRecorder?.release()
        mediaRecorder = null
        cameraDevice?.close()
        cameraDevice = null
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (REQUEST_CAMERA_PERMISSION == requestCode) {
            if (grantResults.size == VIDEO_PERMISSIONS.size) {
                for ((index, result) in grantResults.withIndex()) {
                    if (PackageManager.PERMISSION_GRANTED != result) {
                        ErrorDialog.newInstance("This demo need ${permissions[index]}")
                                .show(childFragmentManager, FRAGMENT_DIALOG)
                    }
                }
            } else {
                ErrorDialog.newInstance("Wrong permission array size.")
                        .show(childFragmentManager, FRAGMENT_DIALOG)
            }
        } else
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun hasPermissionGranted(permissions: Array<String>) = permissions.all {
        ContextCompat.checkSelfPermission(activity!!, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun shouldShowRequestPermissionRationale(permissions: Array<String>) = permissions.any {
        shouldShowRequestPermissionRationale(it)
    }

    /**
     * Shows OK/Cancel confirmation dialog about camera permission.
     */
    class ConfirmationDialog : DialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val parent = parentFragment

            return AlertDialog.Builder(activity)
                    .setMessage("Camera permission is indispensable for this demo.")
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        parent!!.requestPermissions(arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
                    }
                    .setNegativeButton(android.R.string.cancel) { _, _ ->
                        val activity = parent!!.activity
                        activity?.finish()
                    }
                    .create()
        }
    }
}
