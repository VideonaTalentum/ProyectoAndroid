package com.google.android.gms.samples.vision.face.facetracker.View;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.github.fafaldo.fabtoolbar.widget.FABToolbarLayout;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.samples.vision.face.facetracker.Model.Model;
import com.google.android.gms.samples.vision.face.facetracker.Presenter.Presenter;
import com.google.android.gms.samples.vision.face.facetracker.R;
import com.google.android.gms.samples.vision.face.facetracker.ui.camera.CameraSourcePreview;
import com.google.android.gms.samples.vision.face.facetracker.ui.camera.GraphicOverlay;
import com.google.android.gms.vision.CameraSource;

public class View extends Activity implements android.view.View.OnClickListener {

    private static final String TAG = "FaceTracker";

    private CameraSourcePreview mPreview;
    private GraphicOverlay mGraphicOverlay;

    private Presenter presenter;
    private Model model;

    private FABToolbarLayout fabToolbarLayout;
    private FloatingActionButton fab;

    private static final int RC_HANDLE_CAMERA_PERM = 2;



    private ImageView buttonOne, buttonTwo, buttonThree, buttonFour;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view);

        mPreview = (CameraSourcePreview) findViewById(R.id.preview);
        mGraphicOverlay = (GraphicOverlay) findViewById(R.id.faceOverlay);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fabToolbarLayout = (FABToolbarLayout) findViewById(R.id.fabtoolbar);

        model = new Model(getApplicationContext());
        presenter = new Presenter(getApplicationContext(),mGraphicOverlay,mPreview,model,this);


        buttonOne = (ImageView) findViewById(R.id.buttonOne);
        buttonTwo = (ImageView) findViewById(R.id.buttonTwo);
        buttonThree = (ImageView) findViewById(R.id.buttonThree);
        buttonFour = (ImageView) findViewById(R.id.buttonFour);

        fab.setOnClickListener(this);
        buttonOne.setOnClickListener(this);
        buttonTwo.setOnClickListener(this);
        buttonThree.setOnClickListener(this);
        buttonFour.setOnClickListener(this);
        mGraphicOverlay.setOnClickListener(this);

        presenter.checkCameraPermissions(mGraphicOverlay);


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        if(presenter.onRequestPermissionsResultPresenter(requestCode,permissions,grantResults)) {

            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
//
//        if (requestCode != RC_HANDLE_CAMERA_PERM) {
//            Log.d(TAG, "Got unexpected permission result: " + requestCode);
//            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//            return;
//        }
//
//        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//            Log.d(TAG, "Camera permission granted - initialize the camera source");
//            // we have permission, so create the camerasource
//            presenter.createCameraSource(mGraphicOverlay);
//            return;
//        }
//
//        Log.e(TAG, "Permission not granted: results len = " + grantResults.length +
//                " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));
//
//        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int id) {
//                finish();
//            }
//        };
//
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle("Face Tracker sample")
//                .setMessage(R.string.no_camera_permission)
//                .setPositiveButton(R.string.ok, listener)
//                .show();
    }

    /**
     * Restarts the camera.
     */
    @Override
    protected void onResume() {
        super.onResume();
        presenter.startCameraSource();
    }

    /**
     * Stops the camera.
     */
    @Override
    protected void onPause() {
        super.onPause();
        mPreview.stop();
    }

    /**
     * Releases the resources associated with the camera source, the associated detector, and the
     * rest of the processing pipeline.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPreview != null) {
            mPreview.release();
        }

    }


    @Override
    public void onClick(android.view.View v) {

        switch (v.getId()){
            case R.id.fab:
                fabToolbarLayout.show();
                break;
            case R.id.buttonOne:
                presenter.changeCamera();
                break;
            case R.id.buttonTwo:
                presenter.changeMouth();
                break;
            case R.id.buttonThree:
                presenter.changeEyes();
                break;
            case R.id.buttonFour:
                presenter.takePicture();
                break;
        }
        fabToolbarLayout.hide();
    }



}
