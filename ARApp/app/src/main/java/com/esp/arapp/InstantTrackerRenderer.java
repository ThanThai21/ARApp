/*
 * Copyright 2017 Maxst, Inc. All Rights Reserved.
 */

package com.esp.arapp;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.Matrix;
import android.util.Log;

import com.esp.arapp.models.ColorCube;
import com.esp.arapp.models.ObjModel;
import com.maxst.ar.CameraDevice;
import com.maxst.ar.MaxstAR;
import com.maxst.ar.MaxstARUtil;
import com.maxst.ar.Trackable;
import com.maxst.ar.TrackerManager;
import com.maxst.ar.TrackingResult;
import com.maxst.ar.TrackingState;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


class InstantTrackerRenderer implements Renderer {

    public static final String TAG = InstantTrackerRenderer.class.getSimpleName();

    private int surfaceWidth;
    private int surfaceHeight;
    private BackgroundRenderHelper backgroundRenderHelper;

    private TexturedCube texturedCube;
    private float posX;
    private float posY;
    private Activity activity;

    private ObjModel objectModel;
    private ColorCube colorCube;
    private ColoredCube coloredCube;


    InstantTrackerRenderer(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        GLES20.glViewport(0, 0, surfaceWidth, surfaceHeight);

        TrackingState state = TrackerManager.getInstance().updateTrackingState();
        TrackingResult trackingResult = state.getTrackingResult();

        backgroundRenderHelper.drawBackground();

        if (trackingResult.getCount() == 0) {
            return;
        }

        float[] projectionMatrix = CameraDevice.getInstance().getProjectionMatrix();

        Trackable trackable = trackingResult.getTrackable(0);

        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

//        object3DImp.draw();
        texturedCube.setTransform(trackable.getPoseMatrix());
        texturedCube.setTranslate(posX, posY, -0.05f);

        texturedCube.setProjectionMatrix(projectionMatrix);
        if (objectModel != null) {
            objectModel.draw(gl);
        }

//        colorCube.draw(gl);

        coloredCube.setTransform(trackable.getPoseMatrix());
        coloredCube.setTranslate(posX, posY, -0.05f);
        coloredCube.setScale(0.5f, 0.5f, 0.5f);
        coloredCube.setProjectionMatrix(projectionMatrix);
        coloredCube.draw();
//        texturedCube.draw();


    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {

        surfaceWidth = width;
        surfaceHeight = height;

        texturedCube.setScale(0.3f, 0.3f, 0.1f);
        MaxstAR.onSurfaceChanged(width, height);
    }

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        backgroundRenderHelper = new BackgroundRenderHelper();
        backgroundRenderHelper.init();

        texturedCube = new TexturedCube();
        Bitmap bitmap = MaxstARUtil.getBitmapFromAsset("HUST_logo.jpg", activity.getAssets());
//        texturedCube.setTextureBitmap(bitmap);

        AssetFileDescriptor afd = null;
        FileInputStream fis = null;
        InputStream is = null;
        try {
            is = activity.getAssets().open("ToyPlane.obj", AssetManager.ACCESS_BUFFER);
//            objectModel = ObjModel.loadFromStream(is, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        colorCube = new ColorCube(40.0f, 0.0f, 0.0f, 20.0f);
        coloredCube = new ColoredCube();
        MaxstAR.onSurfaceCreated();
    }

    void setTranslate(float x, float y) {
        posX += x;
        posY += y;
    }

    void resetPosition() {
        posX = 0;
        posY = 0;
    }

    void setCubeColor(float r, float g, float b, float a) {
        coloredCube = new ColoredCube(r, g, b, a);
    }
}

