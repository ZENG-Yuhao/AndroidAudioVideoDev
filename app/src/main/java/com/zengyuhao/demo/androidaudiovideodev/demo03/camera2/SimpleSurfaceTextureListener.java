package com.zengyuhao.demo.androidaudiovideodev.demo03.camera2;

import android.graphics.SurfaceTexture;
import android.view.TextureView;

public class SimpleSurfaceTextureListener implements TextureView.SurfaceTextureListener {
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }
}
