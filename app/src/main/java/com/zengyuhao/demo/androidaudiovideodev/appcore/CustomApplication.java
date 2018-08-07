package com.zengyuhao.demo.androidaudiovideodev.appcore;

import android.app.Application;

public class CustomApplication extends Application implements Thread.UncaughtExceptionHandler {
    @Override
    public void onCreate() {
        super.onCreate();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {

    }
}
