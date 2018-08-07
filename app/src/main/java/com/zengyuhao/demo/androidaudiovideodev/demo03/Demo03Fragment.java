package com.zengyuhao.demo.androidaudiovideodev.demo03;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.zengyuhao.demo.androidaudiovideodev.R;
import com.zengyuhao.demo.androidaudiovideodev.common.ErrorDialog;
import com.zengyuhao.demo.androidaudiovideodev.demo03.camera2.Camera2Util;
import com.zengyuhao.demo.androidaudiovideodev.demo03.camera2.CameraInfoExporter;
import com.zengyuhao.demo.androidaudiovideodev.demo03.camera2.SimpleSurfaceTextureListener;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class Demo03Fragment extends Fragment {
    private static final int REQUEST_CAMERA_PERMISSION = 0;
    private TextureView mTextureView;
    private CameraManager mCameraManager;
    private Size mPreviewSize;
    private CameraDevice mCameraDevice;
    private CaptureRequest.Builder mPreviewRequestBuilder;
    private CaptureRequest.Builder mRecordRequestBuilder;
    private CameraCaptureSession mCaptureSession;
    private MediaRecorder mMediaRecorder;
    private HandlerThread mBackgroundThread;
    private Handler mBackgroundHandler;


    public Demo03Fragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.demo03_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mTextureView = view.findViewById(R.id.textureView);
        Button btnCapture = view.findViewById(R.id.btnCapture);
        Button btnStop = view.findViewById(R.id.btnStop);

        btnCapture.setOnClickListener(v -> onBtnCaptureClicked());
        btnStop.setOnClickListener(v -> onBtnStopClicked());

        CameraInfoExporter exporter = new CameraInfoExporter(getActivity());
        exporter.log();
    }

    @Override
    public void onResume() {
        super.onResume();
        startBackgroundThread();
        if (mTextureView.isAvailable()) {
            openCamera(mTextureView.getWidth(), mTextureView.getHeight());
        } else {
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }
    }

    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("Camera2");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    private void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        mBackgroundThread = null;
        mBackgroundHandler = null;
    }

    public void onBtnCaptureClicked() {
        recordVideo();
    }

    public void onBtnStopClicked() {
        if (null != mCaptureSession) {
            mCaptureSession.close();
            mCaptureSession = null;
        }

        if (null != mCameraDevice) {
            mCameraDevice.close();
            mCameraDevice = null;
        }

        if (null != mMediaRecorder) {
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        stopBackgroundThread();
        onBtnStopClicked();
    }

    private final TextureView.SurfaceTextureListener mSurfaceTextureListener = new SimpleSurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            openCamera(width, height);
            Log.d("Demo03", "----> TextureView width:" + width + " height:" + height);
        }
    };

    public CameraManager getCameraManager() {
        if (null == mCameraManager) {
            mCameraManager = (CameraManager) getContext().getSystemService(Context.CAMERA_SERVICE);
        }
        return mCameraManager;
    }

    public String findAvailableCamera() {
        try {
            for (String cameraId : getCameraManager().getCameraIdList()) {
                CameraCharacteristics characteristics = getCameraManager().getCameraCharacteristics(cameraId);
                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (null != facing && CameraCharacteristics.LENS_FACING_FRONT == facing) {
                    continue;
                }

                StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                if (null == map) {
                    continue;
                }
                mPreviewSize = Camera2Util.choosePreviewSize(map.getOutputSizes(mTextureView.getSurfaceTexture().getClass()));
                return cameraId;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("No available camera found.");
    }

    public void openCamera(int width, int height) {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                new ConfirmationDialog().show(getChildFragmentManager(), "ConfirmationDialog");
            } else {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
            }
            return;
        }
        String cameraId = findAvailableCamera();
        try {
            getCameraManager().openCamera(cameraId, mStateCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (REQUEST_CAMERA_PERMISSION == requestCode) {
            if (PackageManager.PERMISSION_GRANTED != grantResults[0]) {
                ErrorDialog.newInstance("Camera permission denied.")
                        .show(getChildFragmentManager(), "ErrorDialog");
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            mCameraDevice = camera;
            createCameraPreviewSession();
            Toast.makeText(getActivity(), "Camera opened.", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            mCameraDevice.close();
            Toast.makeText(getActivity(), "Camera disconnected.", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            mCameraDevice.close();
            ErrorDialog.newInstance("An error occurred on CameraDevice : code = " + error)
                    .show(getChildFragmentManager(), "ErrorDialog");
        }
    };

    private void createCameraPreviewSession() {
        SurfaceTexture texture = mTextureView.getSurfaceTexture();
        texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
        Surface surface = new Surface(texture);
        try {
            mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mPreviewRequestBuilder.addTarget(surface);
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            mCameraDevice.createCaptureSession(Arrays.asList(surface),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession session) {
                            mCaptureSession = session;
                            try {
                                mCaptureSession.setRepeatingRequest(mPreviewRequestBuilder.build(), null, null);
                            } catch (CameraAccessException e) {
                                ErrorDialog.newInstance("CameraAccessException : " + e.toString())
                                        .show(getChildFragmentManager(), "ErrorDialog");
                            }
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                            ErrorDialog.newInstance("CameraCaptureSession onConfigureFailed()")
                                    .show(getChildFragmentManager(), "ErrorDialog");
                        }
                    }, null);
        } catch (CameraAccessException e) {
            ErrorDialog.newInstance("CameraAccessException : " + e.toString())
                    .show(getChildFragmentManager(), "ErrorDialog");
        }
    }

    /**
     * Shows OK/Cancel confirmation dialog about camera permission.
     */
    public static class ConfirmationDialog extends DialogFragment {
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Fragment parent = getParentFragment();

            return new AlertDialog.Builder(getActivity())
                    .setMessage("Camera permission is indispensable for this demo.")
                    .setPositiveButton(android.R.string.ok,
                            (dialog, which) -> parent.requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION)
                    )
                    .setNegativeButton(android.R.string.cancel,
                            (dialog, which) -> {
                                Activity activity = parent.getActivity();
                                if (activity != null) {
                                    activity.finish();
                                }
                            }
                    )
                    .create();
        }
    }


    private void recordVideo() {
        Toast.makeText(getActivity(), "Start recording video...", Toast.LENGTH_LONG).show();
        mCaptureSession.close();
        mMediaRecorder = new MediaRecorder();
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE); // when use Camera2, must be set to SURFACE
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mMediaRecorder.setVideoEncodingBitRate(10000000);
        mMediaRecorder.setVideoFrameRate(30);
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mMediaRecorder.setOutputFile(getVideoFilePath(getActivity()));
        try {
            mMediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        SurfaceTexture texture = mTextureView.getSurfaceTexture();
        texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight()); // same as preview size

        try {
            mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
            Surface recorderSurface = mMediaRecorder.getSurface();
            Surface previewSurface = new Surface(mTextureView.getSurfaceTexture());
            mPreviewRequestBuilder.addTarget(recorderSurface);
            mPreviewRequestBuilder.addTarget(previewSurface);
            mCameraDevice.createCaptureSession(Arrays.asList(recorderSurface, previewSurface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    mCaptureSession = session;
                    try {
                        mCaptureSession.setRepeatingRequest(mPreviewRequestBuilder.build(), null, mBackgroundHandler);
                    } catch (CameraAccessException e) {
                        Log.d("MediaRecorder", "---->createCaptureRequest() failed.");
                    }

                    Log.d("MediaRecorder", "---->mMediaRecorder start.");
                    mMediaRecorder.start();
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    Log.d("MediaRecorder", "---->onConfigureFailed()");
                }
            }, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }

    private String getVideoFilePath(Context context) {
        final File dir = context.getExternalFilesDir(null);
        return (dir == null ? "" : (dir.getAbsolutePath() + "/"))
                + System.currentTimeMillis() + ".mp4";
    }
}
