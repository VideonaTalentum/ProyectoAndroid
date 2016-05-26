package com.google.android.gms.samples.vision.face.facetracker.Presenter;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.samples.vision.face.facetracker.Model.Model;
import com.google.android.gms.samples.vision.face.facetracker.R;
import com.google.android.gms.samples.vision.face.facetracker.ui.camera.CameraSourcePreview;
import com.google.android.gms.samples.vision.face.facetracker.ui.camera.GraphicOverlay;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.Permission;

/**
 * Created by Arranque 1 on 24/05/2016.
 */
public class Presenter {

    private Context context;

    private android.hardware.Camera camera;

    private static final String TAG = "FaceTracker";

    private CameraSourcePreview mPreview;
    private GraphicOverlay mGraphicOverlay;

    private android.app.Activity view;
    private Model model;

    private CameraSource mCameraSourceBack = null;
    private CameraSource mCameraSourceFront = null;

    private Boolean usingCameraBack = false;

    private boolean hat = false;
    private boolean eyes = false;
    private boolean mouth = false;
    private boolean moustache = false;

    private static final int RC_HANDLE_CAMERA_PERM = 2;
    private static final int RC_HANDLE_WRITE_EXTERNAL_STORAGE = 3;

    public MediaRecorder recorder;
    SurfaceHolder holder;
    public boolean recording = false;
    SurfaceView cameraView;


    public Presenter(Context context, GraphicOverlay overlay, CameraSourcePreview preview, Model model, android.app.Activity view) {
        this.model = model;
        this.mPreview = preview;
        this.mGraphicOverlay = overlay;
        this.context = context;
        this.view = view;
        //camera = android.hardware.Camera.open();

    }

    public void prepareRecorder() {
        recorder.setPreviewDisplay(holder.getSurface());

        try {
            recorder.prepare();
        } catch (IllegalStateException e) {
            e.printStackTrace();
            view.finish();
        } catch (IOException e) {
            e.printStackTrace();
            view.finish();
        }
    }


    public void setRecorderSettings(){

        recorder = new MediaRecorder();
        initRecorder();

        cameraView = (SurfaceView) mPreview.getChildAt(0);
        holder = cameraView.getHolder();
        holder.addCallback((SurfaceHolder.Callback) view);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        cameraView.setClickable(true);
        cameraView.setOnClickListener((View.OnClickListener) view);

    }

    public void initRecorder() {
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        CamcorderProfile cpHigh = CamcorderProfile
                .get(CamcorderProfile.QUALITY_HIGH);
        recorder.setProfile(cpHigh);
        recorder.setOutputFile("/sdcard/videocapture_example.mp4");
        recorder.setMaxDuration(50000); // 50 seconds
        recorder.setMaxFileSize(5000000); // Approximately 5 megabytes
    }

    public boolean startRecording() {
        try {
            camera.unlock();
            recorder = new MediaRecorder();
            recorder.setOnErrorListener(new MediaRecorder.OnErrorListener() {

                @Override
                public void onError(MediaRecorder mr, int what, int extra) {
                    Log.i(TAG, "Error");
                }
            });

            recorder.setCamera(camera);
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            Log.i(TAG, "a");

            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
            recorder.setVideoEncoder(MediaRecorder.VideoEncoder.H263);
            Log.i(TAG, "b");

            recorder.setMaxDuration(200); // set to 20000

            String path = Environment.getExternalStorageDirectory().toString();
            String uniqueOutFile = path+ System.currentTimeMillis() + ".mp4";
            File outFile = new File(uniqueOutFile);
            java.io.File inFile = new java.io.File(view.getApplicationContext().getFileStreamPath("FileName.mp4").getPath());
            if (inFile.exists()) {
                inFile.delete();
            }
            recorder.setOutputFile(inFile.getPath());
            recorder.setVideoFrameRate(20); // set to 20
            //recorder.setVideoSize(cameraView.getWidth(), cameraView.getHeight());
            Log.i(TAG, "c");

            recorder.setPreviewDisplay(holder.getSurface());
            recorder.setMaxFileSize(5000); // set to 50000
            recorder.prepare();
            Log.i(TAG, "d");

            recorder.start();
            Log.i(TAG, "e");

            return true;
        } catch (IllegalStateException e) {
            Log.i(TAG, "f");
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
            camera.lock();
            return false;
        } catch (IOException e) {
            Log.i(TAG, "g");
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
            camera.lock();
            return false;
        } catch (RuntimeException e) {
            Log.i(TAG, "h");
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
            camera.lock();
            return false;
        }
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


    public void changeMouth() {
        if (mouth == false) {
            mouth = true;
        } else {
            mouth = false;
        }
    }

    public void changeEyes() {
        if (eyes == false) {
            eyes = true;
        } else {
            eyes = false;
        }
    }

    public void changeHat() {
        if (hat == false) {
            hat = true;
        } else {
            hat = false;
        }
    }

    public void changeMoustache() {
        if (moustache == false) {
            moustache = true;
        } else {
            moustache = false;
        }
    }





    public void takePicture() {
        mCameraSourceFront.takePicture(new CameraSource.ShutterCallback() {
            @Override
            public void onShutter() {
                Log.i("we1", "we");
            }
        }, new CameraSource.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] bytes) {

                mPreview.setDrawingCacheEnabled(true);
                mGraphicOverlay.setDrawingCacheEnabled(true);


                mPreview.buildDrawingCache();
                mGraphicOverlay.buildDrawingCache();

                Bitmap bm = mPreview.getDrawingCache();
                Bitmap bm1 = mGraphicOverlay.getDrawingCache();

                Bitmap bm2 = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                Bitmap cs = null;

                int height = 1920;
                int width = 1080;
/*
                if(bm1.getWidth() > bm2.getWidth()) {
                    width = bm1.getWidth() + bm2.getWidth();
                    height = bm1.getHeight();
                } else {
                    width = bm2.getWidth() + bm2.getWidth();
                    height = bm1.getHeight();
                }
*/
                Log.i("height: ",String.valueOf(height));
                Log.i("width: ",String.valueOf(width));

                cs = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

                Canvas comboImage = new Canvas(cs);

                comboImage.drawBitmap(bm2, 0f, 0f, null);
                comboImage.drawBitmap(bm1, bm2.getWidth(), 0f, null);

                String path = null;
                try {
                    path = saveImage(context, "imagen1", cs);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Toast.makeText(context, path, Toast.LENGTH_LONG).show();
            }
        });
    }

    private String saveImage(Context context, String name, Bitmap bitmapImage) throws IOException {

        String path = Environment.getExternalStorageDirectory().toString();
        OutputStream fOut = null;
        File file = new File(path, name+".jpg");
        fOut = new FileOutputStream(file);

        bitmapImage.compress(Bitmap.CompressFormat.JPEG, 85, fOut);
        fOut.flush();
        fOut.close();

        MediaStore.Images.Media.insertImage(context.getContentResolver(), file.getAbsolutePath(), file.getName(), file.getName());


        return file.getAbsolutePath();
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


//        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context);
//        if (code != ConnectionResult.SUCCESS) {
//            Dialog dlg =
//                    GoogleApiAvailability.getInstance().getErrorDialog(view, code, RC_HANDLE_GMS);
//            dlg.show();
//        }
        return true;
    }

    public boolean checkWriteExternalStoragePermissions(){

        int rc1 = ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if(rc1 == PackageManager.PERMISSION_DENIED){
            requestExternalStoragePermission();
        }
        return true;
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
            checkCameraPermissions(mGraphicOverlay);
            return false;
        }


        if (requestCode == RC_HANDLE_CAMERA_PERM && grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Camera permission granted - initialize the camera source");
            createCameraSource(mGraphicOverlay);
            return false;
        }


//        Log.e(TAG, "Permission not granted: results len = " + grantResults.length +
//                " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));
//
//        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int id) {
//                view.finish();
//            }
//        };
//
//        AlertDialog.Builder builder = new AlertDialog.Builder(view);
//        builder.setTitle("Face Tracker sample")
//                .setMessage(R.string.no_camera_permission)
//                .setPositiveButton(R.string.ok, listener)
//                .show();
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
            mFaceGraphic.setOjoBitmap(model.eyeScaledBitmap);
            mFaceGraphic.setBocaBitmap(model.mouthBitmap);
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
            mFaceGraphic.setSombrero(hat);
            mFaceGraphic.setOjos(eyes);
            mFaceGraphic.setBoca(mouth);
            mFaceGraphic.setMoustache(moustache);
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
