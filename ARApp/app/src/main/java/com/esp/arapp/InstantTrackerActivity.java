package com.esp.arapp;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.maxst.ar.CameraDevice;
import com.maxst.ar.MaxstAR;
import com.maxst.ar.ResultCode;
import com.maxst.ar.SensorDevice;
import com.maxst.ar.TrackerManager;

public class InstantTrackerActivity extends ARActivity implements View.OnClickListener, View.OnTouchListener {

    private InstantTrackerRenderer instantTargetRenderer;
    private int preferCameraResolution = 0;
    private Button startTrackingButton;
    private GLSurfaceView glSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instant_tracker);

        startTrackingButton = findViewById(R.id.start_tracking);
        startTrackingButton.setOnClickListener(this);

        instantTargetRenderer = new InstantTrackerRenderer(this);
        glSurfaceView = findViewById(R.id.gl_surface_view);
        glSurfaceView.setEGLContextClientVersion(2);
        glSurfaceView.setRenderer(instantTargetRenderer);
        glSurfaceView.setOnTouchListener(this);

        preferCameraResolution = getSharedPreferences(Constants.PREF_NAME, Activity.MODE_PRIVATE).getInt(Constants.PREF_KEY_CAM_RESOLUTION, 0);
    }

    @Override
    protected void onResume() {
        super.onResume();

        glSurfaceView.onResume();
        SensorDevice.getInstance().start();
        TrackerManager.getInstance().startTracker(TrackerManager.TRACKER_TYPE_INSTANT);

        ResultCode resultCode = ResultCode.Success;

        switch (preferCameraResolution) {
            case 0:
                resultCode = CameraDevice.getInstance().start(0, 640, 480);
                break;

            case 1:
                resultCode = CameraDevice.getInstance().start(0, 1280, 720);
                break;
        }

        if (resultCode != ResultCode.Success) {
            Toast.makeText(this, "Can not open camera", Toast.LENGTH_SHORT).show();
            finish();
        }

        MaxstAR.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        glSurfaceView.onPause();

        TrackerManager.getInstance().quitFindingSurface();
        TrackerManager.getInstance().stopTracker();
        CameraDevice.getInstance().stop();
        SensorDevice.getInstance().stop();

        MaxstAR.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private static final float TOUCH_TOLERANCE = 5;
    private float touchStartX;
    private float touchStartY;
    private float translationX;
    private float translationY;

    @Override
    public boolean onTouch(View v, final MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                touchStartX = x;
                touchStartY = y;

                final float[] screen = new float[2];
                screen[0] = x;
                screen[1] = y;

                final float[] world = new float[3];

                TrackerManager.getInstance().getWorldPositionFromScreenCoordinate(screen, world);
                translationX = world[0];
                translationY = world[1];

                break;
            }

            case MotionEvent.ACTION_MOVE: {
                float dx = Math.abs(x - touchStartX);
                float dy = Math.abs(y - touchStartY);
                if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                    touchStartX = x;
                    touchStartY = y;

                    final float[] screen = new float[2];
                    screen[0] = x;
                    screen[1] = y;

                    final float[] world = new float[3];

                    TrackerManager.getInstance().getWorldPositionFromScreenCoordinate(screen, world);
                    float posX = world[0];
                    float posY = world[1];

                    instantTargetRenderer.setTranslate(posX - translationX, posY - translationY);
                    translationX = posX;
                    translationY = posY;
                }
                break;
            }

            case MotionEvent.ACTION_UP:
                break;
        }

        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start_tracking:
                String text = startTrackingButton.getText().toString();
                if (text.equals("Start")) {
                    TrackerManager.getInstance().findSurface();
                    instantTargetRenderer.resetPosition();
                    startTrackingButton.setText("Stop");
                } else {
                    TrackerManager.getInstance().quitFindingSurface();
                    startTrackingButton.setText("Start");
                }
                break;
        }
    }
}
