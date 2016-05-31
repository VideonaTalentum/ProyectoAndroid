package com.google.android.gms.samples.vision.face.facetracker.View;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Camera;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.SurfaceHolder;
import android.view.Window;
import android.widget.ImageView;

import com.google.android.gms.samples.vision.face.facetracker.Model.Model;
import com.google.android.gms.samples.vision.face.facetracker.Presenter.Presenter;
import com.google.android.gms.samples.vision.face.facetracker.R;
import com.google.android.gms.samples.vision.face.facetracker.ui.camera.CameraSourcePreview;
import com.google.android.gms.samples.vision.face.facetracker.ui.camera.GraphicOverlay;

public class View extends Activity implements android.view.View.OnClickListener {

    private CameraSourcePreview mPreview;
    private GraphicOverlay mGraphicOverlay;

    private Presenter presenter;
    private Model model;

    private com.getbase.floatingactionbutton.FloatingActionButton hatIcon;
    private com.getbase.floatingactionbutton.FloatingActionButton eyesIcon;
    private com.getbase.floatingactionbutton.FloatingActionButton mouthIcon;
    private com.getbase.floatingactionbutton.FloatingActionButton moustacheIcon;
    private com.getbase.floatingactionbutton.FloatingActionButton recordIcon;
    private com.getbase.floatingactionbutton.FloatingActionButton changeCameraIcon;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view);

        mPreview = (CameraSourcePreview) findViewById(R.id.preview);
        mGraphicOverlay = (GraphicOverlay) findViewById(R.id.faceOverlay);

        model = new Model(getApplicationContext());
        presenter = new Presenter(getApplicationContext(),mGraphicOverlay,mPreview,model,this);


        hatIcon = (com.getbase.floatingactionbutton.FloatingActionButton) findViewById(R.id.hatIcon);
        eyesIcon = (com.getbase.floatingactionbutton.FloatingActionButton) findViewById(R.id.eyesIcon);
        mouthIcon = (com.getbase.floatingactionbutton.FloatingActionButton) findViewById(R.id.mouthIcon);
        moustacheIcon = (com.getbase.floatingactionbutton.FloatingActionButton) findViewById(R.id.moustacheIcon);
        recordIcon = (com.getbase.floatingactionbutton.FloatingActionButton) findViewById(R.id.recordIcon);
        changeCameraIcon = (com.getbase.floatingactionbutton.FloatingActionButton) findViewById(R.id.changeCameraIcon);

        hatIcon.setOnClickListener(this);
        eyesIcon.setOnClickListener(this);
        mouthIcon.setOnClickListener(this);
        moustacheIcon.setOnClickListener(this);
        recordIcon.setOnClickListener(this);
        changeCameraIcon.setOnClickListener(this);
        mGraphicOverlay.setOnClickListener(this);


        presenter.checkWriteExternalStoragePermissions();
        presenter.checkCameraPermissions(mGraphicOverlay);

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if(presenter.onRequestPermissionsResultPresenter(requestCode,permissions,grantResults)) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
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
        presenter.releaseMediaRecorder();
        presenter.releaseCamera();
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
            case R.id.hatIcon:
                presenter.changeHat();
                break;
            case R.id.eyesIcon:
                presenter.changeEyes();
                break;
            case R.id.mouthIcon:
                presenter.changeMouth();
                break;
            case R.id.moustacheIcon:
                presenter.changeMoustache();
                break;
            case R.id.recordIcon:
                presenter.onCaptureClick();
                break;
            case R.id.changeCameraIcon:
                presenter.changeCamera();
                break;

        }
    }
}
