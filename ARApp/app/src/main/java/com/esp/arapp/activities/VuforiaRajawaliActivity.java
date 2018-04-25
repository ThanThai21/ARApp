package com.esp.arapp.activities;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ToggleButton;

import com.esp.arapp.R;
import com.esp.arapp.renderer.SimpleRajawaliVuforiaRenderer;

import org.rajawali3d.vuforia.RajawaliVuforiaActivity;

public class VuforiaRajawaliActivity extends RajawaliVuforiaActivity {

    private static final String TAG = "VuforiaRajawaliActivity";
    private RajawaliVuforiaActivity mUILayout;
    private SimpleRajawaliVuforiaRenderer mRenderer;
    private Button mStartScanButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_rajawali_vuforia);
        setScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setCloudRecoDatabase("a75960aa97c3b72a76eb997f9e40d210d5e40bf2",
                "aac883379f691a2550e80767ccd445ffbaa520ca");

        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setGravity(Gravity.CENTER);

        ImageView logoView = new ImageView(this);
        logoView.setImageResource(R.drawable.ar_app);
        ll.addView(logoView);

        addContentView(ll, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));

        startVuforia();
    }

    @Override
    protected void setupTracker() {
        int result = initTracker(TRACKER_TYPE_MARKER);
        if (result == 1) {
            result = initTracker(TRACKER_TYPE_IMAGE);
            if (result == 1) {
                super.setupTracker();
            } else {
                Log.e(TAG, "Couldn't initialize image tracker.");
            }
        } else {
            Log.e(TAG, "Couldn't initialize marker tracker.");
        }
    }

    @Override
    protected void initApplicationAR() {
        createFrameMarker(1, "Marker1", 50, 50);
        createFrameMarker(2, "Marker2", 50, 50);

        createImageMarker("StonesAndChips.xml");
    }

    public void showStartScanButton() {
        this.runOnUiThread(new Runnable() {
            public void run() {
                if (mStartScanButton != null)
                    mStartScanButton.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    protected void initRajawali() {
        mRenderer = new SimpleRajawaliVuforiaRenderer(this);
        setRenderer(mRenderer);
        super.initRajawali();

        // Add button for Cloud Reco:
        mStartScanButton = new Button(this);
        mStartScanButton.setText("Start Scanning CloudReco");
        mStartScanButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                enterScanningModeNative();
                mStartScanButton.setVisibility(View.GONE);
            }
        });

        ToggleButton extendedTrackingButton = new ToggleButton(this);
        extendedTrackingButton.setTextOn("Extended Tracking On");
        extendedTrackingButton.setTextOff("Extended Tracking Off");
        extendedTrackingButton.setChecked(false);
        extendedTrackingButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (((ToggleButton) v).isChecked()) {
                    if (!startExtendedTracking())
                        Log.e(TAG, "Could not start extended tracking");
                } else {
                    if (!stopExtendedTracking())
                        Log.e(TAG, "Could not stop extended tracking");
                }
            }
        });

        mUILayout = this;
        LinearLayout ll = new LinearLayout(this);
        ll.addView(mStartScanButton);
        ll.addView(extendedTrackingButton);
        mUILayout.addContentView(ll, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
    }
}
