package com.google.android.gms.samples.vision.face.facetracker.Model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.android.gms.samples.vision.face.facetracker.R;

/**
 * Created by Arranque 1 on 24/05/2016.
 */
public class Model {

    public Bitmap hatBitmap;
    public Bitmap eyeBitmapLeft;
    public Bitmap eyeBitmapRight;
    public Bitmap moustacheBitmap;



    private Context context;

    public Model(Context context){
        this.context = context;
        hatBitmap = BitmapFactory.decodeResource(context.getResources(),
        R.drawable.hat_senior);
        eyeBitmapLeft = BitmapFactory.decodeResource(context.getResources(), R.drawable.eye_left_dollar);
        eyeBitmapRight = BitmapFactory.decodeResource(context.getResources(), R.drawable.eye_right_dollar);
        moustacheBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.bigotes);

    }
}
