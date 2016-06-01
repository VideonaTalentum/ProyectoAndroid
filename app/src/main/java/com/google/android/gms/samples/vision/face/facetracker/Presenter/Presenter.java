package com.google.android.gms.samples.vision.face.facetracker.Presenter;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Surface;
import android.widget.Toast;

import com.google.android.gms.samples.vision.face.facetracker.Model.Model;
import com.google.android.gms.samples.vision.face.facetracker.ui.camera.CameraSourcePreview;
import com.google.android.gms.samples.vision.face.facetracker.ui.camera.GraphicOverlay;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Arranque 1 on 24/05/2016.
 */
public class Presenter {

    private Context context;


    private static final int REQUEST_CODE = 1000;
    private int mScreenDensity;
    private MediaProjectionManager mProjectionManager;
    private static final int DISPLAY_WIDTH = 720;
    private static final int DISPLAY_HEIGHT = 1280;
    private MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay;
    private MediaProjectionCallback mMediaProjectionCallback;
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();


    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }



    private MediaRecorder mMediaRecorder;

    public boolean isRecording = false;
    private static final String TAG = "Recorder";


    private CameraSourcePreview mPreview;
    private GraphicOverlay mGraphicOverlay;

    private android.app.Activity view;
    private Model model;

    private CameraSource mCameraSourceBack = null;
    private CameraSource mCameraSourceFront = null;

    private Boolean usingCameraBack = false;

    private boolean hatBoolean = false;
    private boolean eyesBoolean = false;
    private boolean moustacheBoolean = false;

    private static final int RC_HANDLE_CAMERA_PERM = 2;
    private static final int RC_HANDLE_WRITE_EXTERNAL_STORAGE = 3;
    private static final int RC_HANDLE_RECORD_AUDIO = 4;

    private GraphicOverlay mOverlay;
    private FaceGraphic mFaceGraphic;

    private DisplayMetrics metrics;


    public Presenter(Context context, GraphicOverlay overlay, CameraSourcePreview preview, Model model, android.app.Activity view) {
        this.model = model;
        this.mPreview = preview;
        this.mGraphicOverlay = overlay;
        this.context = context;
        this.view = view;

    }
    public void prepareVideo(){
        metrics = new DisplayMetrics();
        view.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mScreenDensity = metrics.densityDpi;

        mMediaRecorder = new MediaRecorder();

        mProjectionManager = (MediaProjectionManager) view.getSystemService
                (Context.MEDIA_PROJECTION_SERVICE);
    }


    public void onToggleScreenShare() {
        if (!isRecording) {
            prepareVideo();
            initRecorder();
            shareScreen();
            isRecording=true;
            Toast.makeText(view, "Grabación iniciada. Vuelve a pulsar para detener.", Toast.LENGTH_SHORT).show();
        } else {
            mMediaRecorder.stop();
            mMediaRecorder.reset();
            Log.v(TAG, "Stopping Recording");
            Toast.makeText(view, "Grabación acabada.", Toast.LENGTH_SHORT).show();
            stopScreenSharing();
            isRecording=false;
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void onActivityResultPresent(int resultCode, Intent data){
        mMediaProjectionCallback = new MediaProjectionCallback();
        mMediaProjection = mProjectionManager.getMediaProjection(resultCode, data);
        mMediaProjection.registerCallback(mMediaProjectionCallback, null);
        mVirtualDisplay = createVirtualDisplay();
        mMediaRecorder.start();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void shareScreen() {
        if (mMediaProjection == null) {
            view.startActivityForResult(mProjectionManager.createScreenCaptureIntent(), REQUEST_CODE);
            return;
        }
        mVirtualDisplay = createVirtualDisplay();
        mMediaRecorder.start();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private VirtualDisplay createVirtualDisplay() {
        return mMediaProjection.createVirtualDisplay("MainActivity",
                DISPLAY_WIDTH, DISPLAY_HEIGHT, mScreenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mMediaRecorder.getSurface(), null /*Callbacks*/, null
                /*Handler*/);
    }

    private void initRecorder() {
        try {
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String fileName = "VID_" + timeStamp + ".mp4";
            mMediaRecorder.setOutputFile(Environment
                    .getExternalStoragePublicDirectory(Environment
                            .DIRECTORY_DOWNLOADS) + File.separator + fileName);
            mMediaRecorder.setVideoSize(DISPLAY_WIDTH, DISPLAY_HEIGHT);
            mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mMediaRecorder.setVideoEncodingBitRate(2048 * 1000);
            mMediaRecorder.setAudioEncodingBitRate(128*1000);
            mMediaRecorder.setVideoFrameRate(30);
            int rotation = view.getWindowManager().getDefaultDisplay().getRotation();
            int orientation = ORIENTATIONS.get(rotation + 90);
            mMediaRecorder.setOrientationHint(orientation);
            mMediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private class MediaProjectionCallback extends MediaProjection.Callback {
        @Override
        public void onStop() {
            if (isRecording) {
                isRecording=false;
                mMediaRecorder.stop();
                mMediaRecorder.reset();
                Log.v(TAG, "Recording Stopped");
            }
            mMediaProjection = null;
            stopScreenSharing();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void destroyMediaProjection() {
        if (mMediaProjection != null) {
            mMediaProjection.unregisterCallback(mMediaProjectionCallback);
            mMediaProjection.stop();
            mMediaProjection = null;
        }
        Log.i(TAG, "MediaProjection Stopped");
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void stopScreenSharing() {
        if (mVirtualDisplay == null) {
            return;
        }
        mVirtualDisplay.release();
        mMediaRecorder.release(); //If used: mMediaRecorder object cannot
        // be reused again
        destroyMediaProjection();
    }


    public void changeCamera() {
        if (usingCameraBack) {
            usingCameraBack = false;
            mPreview.stop();
            startCameraSource();
        } else {
            usingCameraBack = true;
            mPreview.stop();
            startCameraSource();
        }
    }

    public void changeEyes() {
        if (eyesBoolean == false) {
            eyesBoolean = true;
        } else {
            eyesBoolean = false;
        }
    }

    public void changeHat() {
        if (hatBoolean == false) {
            hatBoolean = true;
        } else {
            hatBoolean = false;
        }
    }

    public void changeMoustache() {
        if (moustacheBoolean == false) {
            moustacheBoolean = true;
        } else {
            moustacheBoolean = false;
        }
    }


    public void createCameraSource(GraphicOverlay overlay) {

        mGraphicOverlay = overlay;
        FaceDetector detector = new FaceDetector.Builder(context)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .build();

        detector.setProcessor(
                new MultiProcessor.Builder<>(new GraphicFaceTrackerFactory())
                        .build());

        if (!detector.isOperational()) {
            // Note: The first time that an app using face API is installed on a device, GMS will
            // download a native library to the device in order to do detection.  Usually this
            // completes before the app is run for the first time.  But if that download has not yet
            // completed, then the above call will not detect any faces.
            //
            // isOperational() can be used to check if the required native library is currently
            // available.  The detector will automatically become operational once the library
            // download completes on device.
            Log.w(TAG, "Face detector dependencies are not yet available.");
        }

        mCameraSourceBack = new CameraSource.Builder(context, detector)
                .setRequestedPreviewSize(1980, 1080)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedFps(30.0f)
                .build();
        mCameraSourceFront = new CameraSource.Builder(context, detector)
                .setRequestedPreviewSize(1980, 1080)
                .setFacing(CameraSource.CAMERA_FACING_FRONT)
                .setRequestedFps(30.0f)
                .build();
    }



    public boolean checkCameraPermissions(GraphicOverlay overlay){
        mGraphicOverlay = overlay;
        int rc = ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA);

        if (rc == PackageManager.PERMISSION_GRANTED) {
            createCameraSource(mGraphicOverlay);
        } else {
            return requestCameraPermission();
        }


        return true;
    }




    public boolean checkWriteExternalStoragePermissions(){

        int rc1 = ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if(rc1 == PackageManager.PERMISSION_DENIED){
            requestExternalStoragePermission();
        }
        return true;
    }
    public boolean checkAudioPermissions(){

        int rc1 = ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO);

        if(rc1 == PackageManager.PERMISSION_DENIED){
            requestAudioPermission();
        }
        return true;
    }



    public void requestAudioPermission(){
        final String[] permissions = new String[]{Manifest.permission.RECORD_AUDIO};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(view,
                Manifest.permission.RECORD_AUDIO)) {
            ActivityCompat.requestPermissions(view, permissions, RC_HANDLE_RECORD_AUDIO);
        }

    }

    public void requestExternalStoragePermission(){

        final String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(view,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(view, permissions, RC_HANDLE_WRITE_EXTERNAL_STORAGE);
        }


    }


    private boolean requestCameraPermission() {
        Log.w(TAG, "Camera permission is not granted. Requesting permission");

        final String[] permissions = new String[]{Manifest.permission.CAMERA};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(view,
                Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(view, permissions, RC_HANDLE_CAMERA_PERM);
        }

        return false;
    }



    public boolean onRequestPermissionsResultPresenter(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode != RC_HANDLE_CAMERA_PERM && requestCode != RC_HANDLE_WRITE_EXTERNAL_STORAGE) {
            Log.d(TAG, "Got unexpected permission result: " + requestCode);
            return true;
        }



        if (requestCode == RC_HANDLE_WRITE_EXTERNAL_STORAGE && grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Write External Storage permission granted");
            checkAudioPermissions();
            return false;
        }

        if (requestCode == RC_HANDLE_RECORD_AUDIO && grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Write External Storage permission granted");
            checkCameraPermissions(mGraphicOverlay);
            return false;
        }



        if (requestCode == RC_HANDLE_CAMERA_PERM && grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Camera permission granted - initialize the camera source");
            createCameraSource(mGraphicOverlay);
            return false;
        }

        return false;
    }






    public CameraSourcePreview startCameraSource() {


        if (mCameraSourceBack != null && usingCameraBack == true) {
            try {
                mPreview.start(mCameraSourceBack, mGraphicOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                mCameraSourceBack.release();
                mCameraSourceBack = null;
            }
        }
        if (mCameraSourceFront != null && usingCameraBack == false) {
            try {
                mPreview.start(mCameraSourceFront, mGraphicOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                mCameraSourceFront.release();
                mCameraSourceFront = null;
            }
        }
        return mPreview;
    }

    //==============================================================================================
    // Graphic Face Tracker
    //==============================================================================================

    /**
     * Factory for creating a face tracker to be associated with a new face.  The multiprocessor
     * uses this factory to create face trackers as needed -- one for each individual.
     */
    private class GraphicFaceTrackerFactory implements MultiProcessor.Factory<Face> {
        @Override
        public Tracker<Face> create(Face face) {
            return new GraphicFaceTracker(mGraphicOverlay);
        }
    }

    /**
     * Face tracker for each detected individual. This maintains a face graphic within the app's
     * associated face overlay.
     */
    private class GraphicFaceTracker extends Tracker<Face> {
        private GraphicOverlay mOverlay;
        private FaceGraphic mFaceGraphic;

        GraphicFaceTracker(GraphicOverlay overlay) {
            mOverlay = overlay;
            mFaceGraphic = new FaceGraphic(overlay);
            mFaceGraphic.setmBitmap(model.hatBitmap);
            mFaceGraphic.setEyeLeftBitmap(model.eyeBitmapLeft);
            mFaceGraphic.setEyeRightBitmap(model.eyeBitmapRight);
            mFaceGraphic.setMoustacheBitmap(model.moustacheBitmap);

        }

        /**
         * Start tracking the detected face instance within the face overlay.
         */
        @Override
        public void onNewItem(int faceId, Face item) {
            mFaceGraphic.setId(faceId);
            mFaceGraphic.setCameraOrientation(usingCameraBack);
        }

        /**
         * Update the position/characteristics of the face within the overlay.
         */
        @Override
        public void onUpdate(FaceDetector.Detections<Face> detectionResults, Face face) {
            mOverlay.add(mFaceGraphic);
            mFaceGraphic.updateFace(face);
            mFaceGraphic.setSombrero(hatBoolean);
            mFaceGraphic.setOjos(eyesBoolean);
            mFaceGraphic.setMoustache(moustacheBoolean);
        }

        /**
         * Hide the graphic when the corresponding face was not detected.  This can happen for
         * intermediate frames temporarily (e.g., if the face was momentarily blocked from
         * view).
         */
        @Override
        public void onMissing(FaceDetector.Detections<Face> detectionResults) {
            mOverlay.remove(mFaceGraphic);
        }

        /**
         * Called when the face is assumed to be gone for good. Remove the graphic annotation from
         * the overlay.
         */
        @Override
        public void onDone() {
            mOverlay.remove(mFaceGraphic);
        }
    }



}
