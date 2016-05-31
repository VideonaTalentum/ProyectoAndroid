package com.google.android.gms.samples.vision.face.facetracker.Model;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.android.gms.samples.vision.face.facetracker.Presenter.Presenter;
import com.google.android.gms.samples.vision.face.facetracker.R;
import com.google.android.gms.samples.vision.face.facetracker.View.View;

/**
 * Created by Arranque 1 on 24/05/2016.
 */
public class Model {

    public Bitmap hatBitmap;
    public Bitmap eyeBitmap;
    public Bitmap eyeScaledBitmap;
    public Bitmap moustacheBitmap;
    public Bitmap mouthBitmap;
    public Bitmap hatBitmap1;
    public Bitmap hatBitmap2;



    private Context context;

    public Model(Context context){
        this.context = context;
        hatBitmap = BitmapFactory.decodeResource(context.getResources(),
        R.drawable.sombrero_indiana_x3);
        eyeBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.eye_sorpresa_dcha_x3);
        eyeBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.ojo);
        eyeScaledBitmap = Bitmap.createScaledBitmap(eyeBitmap, 200, 200, true);
        mouthBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.boca);
        moustacheBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.moustage_garfio);



    }




}
