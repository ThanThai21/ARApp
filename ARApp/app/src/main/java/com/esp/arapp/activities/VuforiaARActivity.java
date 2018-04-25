package com.esp.arapp.activities;

import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.esp.arapp.listeners.GustureListener;
import com.esp.arapp.R;
import com.esp.arapp.RefFreeFrame;
import com.esp.arapp.SampleApplication.SampleApplicationControl;
import com.esp.arapp.SampleApplication.SampleApplicationException;
import com.esp.arapp.SampleApplication.SampleApplicationSession;
import com.esp.arapp.SampleApplication.utils.LoadingDialogHandler;
import com.esp.arapp.SampleApplication.utils.SampleApplicationGLView;
import com.esp.arapp.SampleApplication.utils.Texture;
import com.esp.arapp.VuforiaSamples.ui.SampleAppMenu.SampleAppMenu;
import com.esp.arapp.VuforiaSamples.ui.SampleAppMenu.SampleAppMenuGroup;
import com.esp.arapp.VuforiaSamples.ui.SampleAppMenu.SampleAppMenuInterface;
import com.esp.arapp.renderer.VuforiaARRenderer;
import com.vuforia.CameraDevice;
import com.vuforia.DataSet;
import com.vuforia.ImageTargetBuilder;
import com.vuforia.ObjectTracker;
import com.vuforia.State;
import com.vuforia.Trackable;
import com.vuforia.Tracker;
import com.vuforia.TrackerManager;
import com.vuforia.Vuforia;

import java.util.ArrayList;
import java.util.Vector;

public class VuforiaARActivity extends AppCompatActivity implements SampleApplicationControl, SampleAppMenuInterface {

    private static final String TAG = "VuforiaAR";
    private SampleApplicationSession vuforiaAppSession;
    private SampleApplicationGLView mGlView;
    private VuforiaARRenderer mRenderer;
    private Vector<Texture> mTextures;
    private RelativeLayout rootContainer;
    private View mBottomBar;
    private View mCameraButton;

    int targetBuilderCounter = 1;

    DataSet dataSetUserDef = null;

    private GestureDetector mGestureDetector;

    private SampleAppMenu mSampleAppMenu;
    private ArrayList<View> mSettingsAdditionalViews;

    private boolean mExtendedTracking = false;

    private LoadingDialogHandler loadingDialogHandler = new LoadingDialogHandler(this);

    public RefFreeFrame refFreeFrame;
    boolean mIsDroidDevice = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_vuforia_ar);

        vuforiaAppSession = new SampleApplicationSession(this);
        vuforiaAppSession.initAR(this, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mTextures = new Vector<Texture>();
        mTextures.add(Texture.loadTextureFromApk("TextureTeapotBlue.png", getAssets()));
        mGestureDetector = new GestureDetector(this, new GustureListener());
        mIsDroidDevice = Build.MODEL.toLowerCase().startsWith("droid");
        addOverlayLayout(true);

    }

    private void addOverlayLayout(boolean initLayout) {
        rootContainer = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.camera_overlay_udt, null, false);
        rootContainer.setVisibility(View.VISIBLE);
        if (initLayout) {
            rootContainer.setBackgroundColor(Color.BLACK);
        }

        addContentView(rootContainer, new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        mBottomBar = rootContainer.findViewById(R.id.bottom_bar);
        mCameraButton = rootContainer.findViewById(R.id.camera_button);
        loadingDialogHandler.mLoadingDialogContainer = rootContainer.findViewById(R.id.loading_layout);
        mBottomBar.setVisibility(View.VISIBLE);
        mCameraButton.setVisibility(View.VISIBLE);
        rootContainer.bringToFront();
    }

    public void onCameraClick(View view) {
        if (isUserDefinedTargetsRunning()) {
            loadingDialogHandler.sendEmptyMessage(LoadingDialogHandler.SHOW_LOADING_DIALOG);
            startBuild();
        }
    }

    private void startBuild() {
        TrackerManager trackerManager = TrackerManager.getInstance();
        ObjectTracker objectTracker = (ObjectTracker) trackerManager.getTracker(ObjectTracker.getClassType());

        if (objectTracker != null) {
            ImageTargetBuilder targetBuilder = objectTracker.getImageTargetBuilder();
            if (targetBuilder != null) {
                if (targetBuilder.getFrameQuality() == ImageTargetBuilder.FRAME_QUALITY.FRAME_QUALITY_LOW) {
                    new AlertDialog.Builder(this)
                            .setTitle("Low quality image")
                            .setMessage("The image has very little detail, please try another")
                            .setNegativeButton("OK", (dialog, v) -> {
                                dialog.dismiss();
                            })
                            .show();
                }

                String name;
                do {
                    name = "UserTarget-" + targetBuilderCounter;
                    targetBuilderCounter++;
                } while (!targetBuilder.build(name, 320.0f));

                refFreeFrame.setCreating();
            }
        }
    }

    boolean isUserDefinedTargetsRunning() {
        TrackerManager trackerManager = TrackerManager.getInstance();
        ObjectTracker objectTracker = (ObjectTracker) trackerManager
                .getTracker(ObjectTracker.getClassType());

        if (objectTracker != null) {
            ImageTargetBuilder targetBuilder = objectTracker
                    .getImageTargetBuilder();
            if (targetBuilder != null) {
                return targetBuilder.getFrameQuality() != ImageTargetBuilder.FRAME_QUALITY.FRAME_QUALITY_NONE;
            }
        }

        return false;
    }

    public void updateRendering() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        refFreeFrame.initGL(metrics.widthPixels, metrics.heightPixels);
    }

    @Override
    public boolean menuProcess(int command) {
        boolean result = true;

        switch (command) {
            case CMD_BACK:
                finish();
                break;

            case CMD_EXTENDED_TRACKING:
                if (dataSetUserDef.getNumTrackables() > 0) {
                    int lastTrackableCreatedIndex = dataSetUserDef.getNumTrackables() - 1;
                    Trackable trackable = dataSetUserDef.getTrackable(lastTrackableCreatedIndex);

                    if (!mExtendedTracking) {
                        if (!trackable.startExtendedTracking()) {
                            Log.e(TAG, "Failed to start extended tracking target");
                            result = false;
                        } else {
                            Log.d(TAG, "Successfully started extended tracking target");
                        }
                    } else {
                        if (!trackable.stopExtendedTracking()) {
                            Log.e(TAG, "Failed to stop extended tracking target");
                            result = false;
                        } else {
                            Log.d(TAG, "Successfully stopped extended tracking target");
                        }
                    }
                }

                if (result) {
                    mExtendedTracking = !mExtendedTracking;
                }
                break;
        }

        return result;
    }

    @Override
    public boolean doInitTrackers() {
        boolean result = true;
        TrackerManager trackerManager = TrackerManager.getInstance();
        Tracker tracker = trackerManager.initTracker(ObjectTracker
                .getClassType());
        if (tracker == null) {
            Log.d("VuforiaAR", "Failed to initialize ObjectTracker.");
            result = false;
        } else {
            Log.d("VuforiaAR", "Successfully initialized ObjectTracker.");
        }
        return result;
    }

    @Override
    public boolean doLoadTrackersData() {
        TrackerManager trackerManager = TrackerManager.getInstance();
    ObjectTracker objectTracker = (ObjectTracker) trackerManager.getTracker(ObjectTracker.getClassType());
        if (objectTracker == null) {
        Log.d(TAG, "Failed to load tracking data set because the ObjectTracker has not been initialized.");
        return false;
    }

    dataSetUserDef = objectTracker.createDataSet();
        if (dataSetUserDef == null) {
        Log.d(TAG, "Failed to create a new tracking data.");
        return false;
    }

        if (!objectTracker.activateDataSet(dataSetUserDef)) {
        Log.d(TAG, "Failed to activate data set.");
        return false;
    }

        Log.d(TAG, "Successfully loaded and activated data set.");
        return true;
}

    @Override
    public boolean doStartTrackers() {
        boolean result = true;
        Tracker objectTracker = TrackerManager.getInstance().getTracker(ObjectTracker.getClassType());
        if (objectTracker != null) {
            objectTracker.start();
        }
        return result;
    }

    @Override
    public boolean doStopTrackers() {
        boolean result = true;
        Tracker objectTracker = TrackerManager.getInstance().getTracker(ObjectTracker.getClassType());
        if (objectTracker != null) {
            objectTracker.stop();
        }
        return result;
    }

    @Override
    public boolean doUnloadTrackersData() {
        boolean result = true;
        TrackerManager trackerManager = TrackerManager.getInstance();
        ObjectTracker objectTracker = (ObjectTracker) trackerManager.getTracker(ObjectTracker.getClassType());
        if (objectTracker == null) {
            result = false;
            Log.d(TAG, "Failed to destroy the tracking data because the ObjectTracker has not been initialized.");
        }
        if (dataSetUserDef != null) {
            if (objectTracker.getActiveDataSet(0) != null && !objectTracker.deactivateDataSet(dataSetUserDef)) {
                Log.d(TAG, "Failed to destroy the tracking data set because the data set could not be deactivated.");
                result = false;
            }
            if (!objectTracker.destroyDataSet(dataSetUserDef)) {
                Log.d(TAG, "Failed to destroy the tracking data set.");
                result = false;
            }
            Log.d(TAG, "Successfully destroyed the data set.");
            dataSetUserDef = null;
        }
        return result;
    }

    @Override
    public boolean doDeinitTrackers() {
        boolean result = true;

        if (refFreeFrame != null)
            refFreeFrame.deInit();

        TrackerManager tManager = TrackerManager.getInstance();
        tManager.deinitTracker(ObjectTracker.getClassType());

        return result;
    }

    @Override
    public void onInitARDone(SampleApplicationException e) {
        if (e == null) {
            initApplicationAR();
            mRenderer.setActive(true);
            addContentView(mGlView, new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            rootContainer.bringToFront();
            loadingDialogHandler.sendEmptyMessage(LoadingDialogHandler.HIDE_LOADING_DIALOG);
            rootContainer.setBackgroundColor(Color.TRANSPARENT);
            vuforiaAppSession.startAR(CameraDevice.CAMERA_DIRECTION.CAMERA_DIRECTION_BACK);
            setSampleAppMenuAdditionalViews();
            mSampleAppMenu = new SampleAppMenu(this, this,
                    "User Defined Targets", mGlView, rootContainer,
                    mSettingsAdditionalViews);
            setSampleAppMenuSettings();
        } else {
            Log.e(TAG, e.getString());
            showInitializationErrorMessage(e.getString());
        }
    }

    @Override
    public void onVuforiaUpdate(State state) {
        TrackerManager trackerManager = TrackerManager.getInstance();
        ObjectTracker objectTracker = (ObjectTracker) trackerManager.getTracker(ObjectTracker.getClassType());
        if (refFreeFrame.hasNewTrackableSource()) {
            Log.d(TAG, "Attempting to transfer the trackable source to the dataset");

            // Deactivate current dataset
            objectTracker.deactivateDataSet(objectTracker.getActiveDataSet(0));

            // Clear the oldest target if the dataset is full or the dataset
            // already contains five user-defined targets.
            if (dataSetUserDef.hasReachedTrackableLimit()
                    || dataSetUserDef.getNumTrackables() >= 5)
                dataSetUserDef.destroy(dataSetUserDef.getTrackable(0));

            if (mExtendedTracking && dataSetUserDef.getNumTrackables() > 0) {
                // We need to stop the extended tracking for the previous target
                // so we can enable it for the new one
                int previousCreatedTrackableIndex =
                        dataSetUserDef.getNumTrackables() - 1;

                objectTracker.resetExtendedTracking();
                dataSetUserDef.getTrackable(previousCreatedTrackableIndex)
                        .stopExtendedTracking();
            }

            // Add new trackable source
            Trackable trackable = dataSetUserDef
                    .createTrackable(refFreeFrame.getNewTrackableSource());

            // Reactivate current dataset
            objectTracker.activateDataSet(dataSetUserDef);

            if (mExtendedTracking) {
                trackable.startExtendedTracking();
            }

        }
    }

    @Override
    public void onVuforiaResumed() {
        if (mGlView != null) {
            mGlView.setVisibility(View.VISIBLE);
            mGlView.onResume();
        }
    }

    @Override
    public void onVuforiaStarted() {
        startUserDefinedTargets();

        // Set camera focus mode
        if (!CameraDevice.getInstance().setFocusMode(CameraDevice.FOCUS_MODE.FOCUS_MODE_CONTINUOUSAUTO)) {
            // If continuous autofocus mode fails, attempt to set to a different mode
            if (!CameraDevice.getInstance().setFocusMode(CameraDevice.FOCUS_MODE.FOCUS_MODE_TRIGGERAUTO)) {
                CameraDevice.getInstance().setFocusMode(CameraDevice.FOCUS_MODE.FOCUS_MODE_NORMAL);
            }
        }

        showProgressIndicator(false);
    }

    public void showProgressIndicator(boolean show) {
        if (loadingDialogHandler != null) {
            if (show) {
                loadingDialogHandler.sendEmptyMessage(LoadingDialogHandler.SHOW_LOADING_DIALOG);
            } else {
                loadingDialogHandler.sendEmptyMessage(LoadingDialogHandler.HIDE_LOADING_DIALOG);
            }
        }
    }

    public void showInitializationErrorMessage(String message) {
        final String errorMessage = message;
        runOnUiThread(new Runnable() {
            public void run() {
                new AlertDialog.Builder(VuforiaARActivity.this)
                        .setTitle("Error")
                        .setCancelable(false)
                        .setIcon(0)
                        .setPositiveButton("OK", ((dialogInterface, i) -> {
                            dialogInterface.dismiss();
                            finish();
                        }))
                        .setMessage(errorMessage)
                        .create()
                        .show();
            }
        });
    }


    private void initApplicationAR() {
        refFreeFrame = new RefFreeFrame(this, vuforiaAppSession);
        refFreeFrame.init();

        int depthSize = 16;
        int stencilSize = 0;
        boolean translucent = Vuforia.requiresAlpha();

        mGlView = new SampleApplicationGLView(this);
        mGlView.init(translucent, depthSize, stencilSize);

        mRenderer = new VuforiaARRenderer(vuforiaAppSession, this);
        mRenderer.setTextures(mTextures);
        mGlView.setRenderer(mRenderer);
    }

    private void setSampleAppMenuAdditionalViews() {
        mSettingsAdditionalViews = new ArrayList<View>();
        mSettingsAdditionalViews.add(mBottomBar);
    }

    final public static int CMD_BACK = -1;
    final public static int CMD_EXTENDED_TRACKING = 1;

    private void setSampleAppMenuSettings() {
        SampleAppMenuGroup group;

        group = mSampleAppMenu.addGroup("", false);
        group.addTextItem(getString(R.string.menu_back), -1);

        group = mSampleAppMenu.addGroup("", true);
        group.addSelectionItem(getString(R.string.menu_extended_tracking),
                CMD_EXTENDED_TRACKING, false);

        mSampleAppMenu.attachMenu();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mSampleAppMenu != null && mSampleAppMenu.processEvent(event) || mGestureDetector.onTouchEvent(event);
    }

    boolean startUserDefinedTargets() {
        Log.d(TAG, "startUserDefinedTargets");
        TrackerManager trackerManager = TrackerManager.getInstance();
        ObjectTracker objectTracker = (ObjectTracker) (trackerManager
                .getTracker(ObjectTracker.getClassType()));
        if (objectTracker != null) {
            ImageTargetBuilder targetBuilder = objectTracker.getImageTargetBuilder();
            if (targetBuilder != null) {
                // if needed, stop the target builder
                if (targetBuilder.getFrameQuality() != ImageTargetBuilder.FRAME_QUALITY.FRAME_QUALITY_NONE) {
                    targetBuilder.stopScan();
                }
                objectTracker.stop();
                targetBuilder.startScan();
            }
            return true;
        } else {
            return false;
        }
    }

    public Texture createTexture(String nName) {
        return Texture.loadTextureFromApk(nName, getAssets());
    }


    public void targetCreated() {
        // Hides the loading dialog
        loadingDialogHandler
                .sendEmptyMessage(LoadingDialogHandler.HIDE_LOADING_DIALOG);

        if (refFreeFrame != null) {
            refFreeFrame.reset();
        }

    }
}
