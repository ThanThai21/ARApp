package com.esp.arapp.listeners;

import android.os.Handler;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.vuforia.CameraDevice;


public class GustureListener extends GestureDetector.SimpleOnGestureListener {

    private final Handler autoFocusHandler = new Handler();

    @Override
    public boolean onDown(MotionEvent e) {
        return true;
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        boolean result = CameraDevice.getInstance().setFocusMode(CameraDevice.FOCUS_MODE.FOCUS_MODE_TRIGGERAUTO);
        autoFocusHandler.postDelayed(() -> {
            final boolean autoFocusResult = CameraDevice.getInstance().setFocusMode(CameraDevice.FOCUS_MODE.FOCUS_MODE_CONTINUOUSAUTO);
        }, 1000L);
        return true;
    }
}
