package com.esp.arapp.activities;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.esp.arapp.ARActivity;
import com.esp.arapp.CodeScanRenderer;
import com.esp.arapp.Constants;
import com.esp.arapp.R;
import com.maxst.ar.CameraDevice;
import com.maxst.ar.MaxstAR;
import com.maxst.ar.ResultCode;
import com.maxst.ar.TrackerManager;
import com.maxst.ar.TrackingState;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.Locale;

public class QRCodeActivity extends ARActivity {

    private GLSurfaceView glSurfaceView;
    private CodeScanResultHandler resultHandler;
    private AutoFocusHandler autoFocusHandler;
    private int preferCameraResolution = 0;

    private TextView qrContentTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode);


        CodeScanRenderer renderer = new CodeScanRenderer();
        glSurfaceView = findViewById(R.id.gl_surface_view);
        glSurfaceView.setEGLContextClientVersion(2);
        glSurfaceView.setRenderer(renderer);
        qrContentTextView = findViewById(R.id.qr_content);


        resultHandler = new CodeScanResultHandler(this);
        autoFocusHandler = new AutoFocusHandler();

        preferCameraResolution = getSharedPreferences(Constants.PREF_NAME, Activity.MODE_PRIVATE).getInt(Constants.PREF_KEY_CAM_RESOLUTION, 0);
    }

    @Override
    protected void onResume() {
        super.onResume();

        glSurfaceView.onResume();
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

        //CameraDevice.getInstance().setAutoWhiteBalanceLock(true); // For ODG-R7 preventing camera flickering

        TrackerManager.getInstance().startTracker(TrackerManager.TRACKER_TYPE_CODE_SCANNER);

        resultHandler.sendEmptyMessageDelayed(0, 33);
        autoFocusHandler.start();
    }

    @Override
    protected void onPause() {
        super.onPause();

        glSurfaceView.onPause();

        resultHandler.removeCallbacksAndMessages(null);
        TrackerManager.getInstance().stopTracker();
        CameraDevice.getInstance().stop();
        autoFocusHandler.stop();

        MaxstAR.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        resultHandler = null;
    }

    private static class CodeScanResultHandler extends Handler {

        WeakReference<QRCodeActivity> activityWeakReference;
        private String TAG = "CodeScanResultHandler";

        CodeScanResultHandler(QRCodeActivity activity) {
            activityWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {

            final QRCodeActivity activity = activityWeakReference.get();

            if (activity == null) {
                return;
            }

            TrackingState state = TrackerManager.getInstance().updateTrackingState();
            String code = state.getCodeScanResult();

            if (code != null && code.length() > 0) {
                try {
                    JSONObject jsonObject = new JSONObject(code);
                    Log.d(TAG, "handleMessage: " + jsonObject.toString());
                    activity.qrContentTextView.setText(jsonObject.getString("Value"));
                    Log.d(TAG, "handleMessage: " + jsonObject.getString("Value"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                TrackerManager.getInstance().stopTracker();
                TrackerManager.getInstance().destroyTracker();
                new CountDownTimer(1000, 1) {

                    @Override
                    public void onTick(long l) {

                    }

                    @Override
                    public void onFinish() {

                        TrackerManager.getInstance().startTracker(TrackerManager.TRACKER_TYPE_CODE_SCANNER);
                        sendEmptyMessageDelayed(0, 33);
                        activity.qrContentTextView.setText("");
                    }
                }.start();
            } else {
                sendEmptyMessageDelayed(0, 30);
                activity.qrContentTextView.setText("");
            }
        }
    }

    private static class AutoFocusHandler extends Handler {

        void start() {
            sendEmptyMessage(0);
        }

        void stop() {
            removeCallbacksAndMessages(null);
        }

        @Override
        public void handleMessage(Message msg) {
            CameraDevice.getInstance().setFocusMode(CameraDevice.FocusMode.FOCUS_MODE_AUTO);
            sendEmptyMessageDelayed(0, 3000);
        }
    }
}
