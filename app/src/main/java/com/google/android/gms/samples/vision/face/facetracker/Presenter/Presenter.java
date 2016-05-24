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
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
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

/**
 * Created by Arranque 1 on 24/05/2016.
 */
public class Presenter {

    private Context context;

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

    private static final int RC_HANDLE_GMS = 9001;
    private static final int RC_HANDLE_CAMERA_PERM = 2;


    public Presenter(Context context, GraphicOverlay overlay, CameraSourcePreview preview, Model model, android.app.Activity view) {
        this.model = model;
        this.mPreview = preview;
        this.mGraphicOverlay = overlay;
        this.context = context;
        this.view = view;
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


    public void takePicture() {
        mCameraSourceFront.takePicture(new CameraSource.ShutterCallback() {
            @Override
            public void onShutter() {
                Log.i("we1", "we");
            }
        }, new CameraSource.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] bytes) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                String ruta = guardarImagen(context, "imagen1", bitmap);

                //addImageToGallery(ruta,context);

                MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "COSA" , "cosa");

                Toast.makeText(context, ruta, Toast.LENGTH_LONG).show();
            }
        });
    }

    private String guardarImagen(Context context, String nombre, Bitmap imagen) {
        ContextWrapper cw = new ContextWrapper(context);

        File file = new File (
                Environment.getExternalStoragePublicDirectory (
                        String.valueOf(Environment.getDataDirectory())), "CameraAppDemo");
        if (!file.exists ())
        {
            file.mkdirs();
            Log.i("weee","weeeee");
        }

        File myPath = cw.getDatabasePath("data");
        if (!myPath.exists()) {
        }
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(myPath);
            imagen.compress(Bitmap.CompressFormat.JPEG, 10, fos);
            fos.flush();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return myPath.getAbsolutePath();
    }

    public static void addImageToGallery(final String filePath, final Context context) {

        ContentValues values = new ContentValues();

        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.MediaColumns.DATA, filePath);

        context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
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

        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context);
        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg =
                    GoogleApiAvailability.getInstance().getErrorDialog(view, code, RC_HANDLE_GMS);
            dlg.show();
        }


        return true;
    }




    private boolean requestCameraPermission() {
        Log.w(TAG, "Camera permission is not granted. Requesting permission");

        final String[] permissions = new String[]{Manifest.permission.CAMERA};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(view,
                Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(view, permissions, RC_HANDLE_CAMERA_PERM);
        }

        final Activity thisActivity = view;

        android.view.View.OnClickListener listener = new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View view) {
                ActivityCompat.requestPermissions(thisActivity, permissions,
                        RC_HANDLE_CAMERA_PERM);
            }
        };

        Snackbar.make(mGraphicOverlay, R.string.permission_camera_rationale,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.ok, listener)
                .show();
        return false;
    }



    public boolean onRequestPermissionsResultPresenter(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode != RC_HANDLE_CAMERA_PERM) {
            Log.d(TAG, "Got unexpected permission result: " + requestCode);
            return true;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Camera permission granted - initialize the camera source");
            // we have permission, so create the camerasource
            createCameraSource(mGraphicOverlay);
            return false;
        }

        Log.e(TAG, "Permission not granted: results len = " + grantResults.length +
                " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                view.finish();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(view);
        builder.setTitle("Face Tracker sample")
                .setMessage(R.string.no_camera_permission)
                .setPositiveButton(R.string.ok, listener)
                .show();
        return false;
    }






    public CameraSourcePreview startCameraSource() {

        // check that the device has play services available.
//        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context);
//        if (code != ConnectionResult.SUCCESS) {
//            /*Dialog dlg =
//                    GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS);
//            dlg.show();*/
//        }


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
