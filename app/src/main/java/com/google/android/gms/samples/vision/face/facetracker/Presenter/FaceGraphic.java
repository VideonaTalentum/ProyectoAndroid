/*
 * Copyright (C) The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.android.gms.samples.vision.face.facetracker.Presenter;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;

import com.google.android.gms.samples.vision.face.facetracker.ui.camera.GraphicOverlay;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.Landmark;

import java.util.ArrayList;
import java.util.List;

class FaceGraphic extends GraphicOverlay.Graphic {
    private static final float ID_TEXT_SIZE = 40.0f;
    private static final float BOX_STROKE_WIDTH = 5.0f;

    private static final int COLOR_CHOICES[] = {
            Color.BLUE,
            Color.CYAN,
            Color.GREEN,
            Color.MAGENTA,
            Color.RED,
            Color.WHITE,
            Color.YELLOW
    };
    private static int mCurrentColorIndex = 0;

    private Paint mFacePositionPaint;
    private Paint mIdPaint;
    private Paint mBoxPaint;

    private volatile Face mFace;

    private Boolean sombrero = false;
    private Boolean ojos = false;
    private Boolean boca = false;
    private Boolean moustache = false;

    private Bitmap sombreroBitmap;
    private Bitmap eyeLeftBitmap;
    private Bitmap eyeRightBitmap;
    private Bitmap bocaBitmap;
    private Bitmap moustacheBitmap;
    List<PointF> lista = new ArrayList<>();

    private int mFaceId;
    private float mFaceHappiness;

    private Boolean mCameraOrientation;

    private  GraphicOverlay mOverlay;

    FaceGraphic(GraphicOverlay overlay) {
        super(overlay);
        mOverlay = overlay;

        mCurrentColorIndex = (mCurrentColorIndex + 1) % COLOR_CHOICES.length;
        final int selectedColor = COLOR_CHOICES[mCurrentColorIndex];

        mFacePositionPaint = new Paint();
        mFacePositionPaint.setColor(selectedColor);

        mIdPaint = new Paint();
        mIdPaint.setColor(selectedColor);
        mIdPaint.setTextSize(ID_TEXT_SIZE);

        mBoxPaint = new Paint();
        mBoxPaint.setColor(selectedColor);
        mBoxPaint.setStyle(Paint.Style.STROKE);
        mBoxPaint.setStrokeWidth(BOX_STROKE_WIDTH);
    }

    void setId(int id) {
        mFaceId = id;
    }

    void setHatBitmap(Bitmap bitmap){
        sombreroBitmap = bitmap;
    }

    void setEyeLeftBitmap(Bitmap bitmap){
        eyeLeftBitmap = bitmap;
    }

    void setEyeRightBitmap(Bitmap bitmap){
        eyeRightBitmap = bitmap;
    }



    void setMoustacheBitmap(Bitmap bitmap){
        moustacheBitmap = bitmap;
    }

    void setHatBoolean(Boolean sombrero){
        this.sombrero=sombrero;
    }

    void setMoustacheBoolean(Boolean moustache){
        this.moustache=moustache;
    }

    void setEyesBoolean(Boolean ojos){
        this.ojos=ojos;
    }


    void setCameraOrientation(Boolean cameraOrientation){
        mCameraOrientation = cameraOrientation;
    }


    /**
     * Updates the face instance from the detection of the most recent frame.  Invalidates the
     * relevant portions of the overlay to trigger a redraw.
     */
    void updateFace(Face face) {
        mFace = face;
        postInvalidate();
    }

    /**
     * Draws the face annotations for position on the supplied canvas.
     */
    @Override
    public void draw(Canvas canvas) {
        Face face = mFace;
        if (face == null) {
            return;
        }
        // Draws a circle at the position of the detected face, with the face's track id below.
        float x = translateX(face.getPosition().x + face.getWidth() / 2);
        float y = translateY(face.getPosition().y + face.getHeight() / 2);

        float xOffset = scaleX(face.getWidth() / 2.0f);
        float yOffset = scaleY(face.getHeight() / 2.0f);
        float left = x - xOffset;
        float top = y - yOffset;
        float right = x + xOffset;
        float bottom = y + yOffset;
        RectF rectF = new RectF(left+(right-left)/2,(top-2*(bottom-top)/5),right+(right-left)/4,bottom-2*(bottom-top)/3);


        canvas.save(Canvas.MATRIX_SAVE_FLAG);
        Matrix m = new Matrix();

        if(mCameraOrientation){
            m.setRotate(-face.getEulerZ(), rectF.centerX(), rectF.centerY() + rectF.height());
        }else{
            m.setRotate(face.getEulerZ(), rectF.centerX(), rectF.centerY() + rectF.height());
        }

        canvas.concat(m);
        if(sombrero) {
            canvas.drawBitmap(sombreroBitmap, null, rectF, mBoxPaint);
        }

        canvas.restore();




        int i=0;

        List<PointF> lista2 = new ArrayList<>();
        for (Landmark landmark : mFace.getLandmarks()) {
            float cx = translateX(landmark.getPosition().x);
            float cy = translateY(landmark.getPosition().y);

            lista2.add(new PointF(cx, cy));

            if(lista2.size()==8){
                lista=lista2;
            }

            if (lista.size() == 8) {

                for (int j = 0; j < lista.size(); j++) {
                    if (j == 0 && ojos ) {
                        float xOffset1 = (scaleX(face.getWidth() / 2.0f) / 3);
                        float yOffset1 = (scaleY(face.getHeight() / 2.0f) / 3);
                        float left1 = lista.get(j).x - xOffset1;
                        float top1 = lista.get(j).y - yOffset1;
                        float right1 = lista.get(j).x + xOffset1;
                        float bottom1 = lista.get(j).y + yOffset1;
                        RectF rectF1 = new RectF(left1, top1, right1, bottom1);

                        canvas.save(Canvas.MATRIX_SAVE_FLAG);
                        Matrix m1 = new Matrix();

                        if(mCameraOrientation){
                            m1.setRotate(-face.getEulerZ(), rectF1.centerX(), rectF1.centerY());
                        }else{
                            m1.setRotate(face.getEulerZ(), rectF1.centerX(), rectF1.centerY());
                        }

                        canvas.concat(m1);

                        canvas.drawBitmap(eyeLeftBitmap, null, rectF1, mBoxPaint);

                        canvas.restore();

                    }

                    if (j == 1 && ojos) {
                        float xOffset1 = (scaleX(face.getWidth() / 2.0f) / 3);
                        float yOffset1 = (scaleY(face.getHeight() / 2.0f) / 3);
                        float left1 = lista.get(j).x - xOffset1;
                        float top1 = lista.get(j).y - yOffset1;
                        float right1 = lista.get(j).x + xOffset1;
                        float bottom1 = lista.get(j).y + yOffset1;
                        RectF rectF1 = new RectF(left1, top1, right1, bottom1);

                        canvas.save(Canvas.MATRIX_SAVE_FLAG);
                        Matrix m1 = new Matrix();

                        if(mCameraOrientation){
                            m1.setRotate(-face.getEulerZ(), rectF1.centerX(), rectF1.centerY());
                        }else{
                            m1.setRotate(face.getEulerZ(), rectF1.centerX(), rectF1.centerY());
                        }

                        canvas.concat(m1);

                        canvas.drawBitmap(eyeRightBitmap, null, rectF1, mBoxPaint);

                        canvas.restore();

                    }


                    if(j==2 && moustache){

                        float xOffset1 = (scaleX(face.getWidth() / 2.0f));
                        float yOffset1 = (scaleY(face.getHeight() / 2.0f) / 3);
                        float left1 = lista.get(j).x - xOffset1;
                        float top1 = lista.get(j).y - yOffset1;
                        float right1 = lista.get(j).x + xOffset1;
                        float bottom1 = lista.get(j).y + 2*yOffset1;
                        RectF rectF1 = new RectF(left1, top1, right1, bottom1);

                        canvas.save(Canvas.MATRIX_SAVE_FLAG);
                        Matrix m1 = new Matrix();


                        if(mCameraOrientation){
                            m1.setRotate(-face.getEulerZ(), rectF1.centerX(), rectF1.centerY());
                        }else{
                            m1.setRotate(face.getEulerZ(), rectF1.centerX(), rectF1.centerY());
                        }

                        canvas.concat(m1);

                        canvas.drawBitmap(moustacheBitmap, null, rectF1, mBoxPaint);

                        canvas.restore();


                    }

                    if (j ==7 && boca) {

                        float xOffset1 = (scaleX(face.getWidth() / 2.0f) / 4);
                        float yOffset1 = (scaleY(face.getHeight() / 2.0f) / 4);
                        float left1 = lista.get(j).x - xOffset1;
                        float top1 = lista.get(j).y - yOffset1;
                        float right1 = lista.get(j).x + xOffset1;
                        float bottom1 = lista.get(j).y + yOffset1;
                        RectF rectF1 = new RectF(left1, top1, right1, bottom1);

                        canvas.save(Canvas.MATRIX_SAVE_FLAG);
                        Matrix m2 = new Matrix();

                        if(mCameraOrientation){
                            m2.setRotate(-face.getEulerZ(), rectF1.centerX(), rectF1.centerY());
                        }else{
                            m2.setRotate(face.getEulerZ(), rectF1.centerX(), rectF1.centerY());
                        }

                        canvas.concat(m2);

                        canvas.drawBitmap(bocaBitmap, null, rectF1, mBoxPaint);

                        canvas.restore();
                    }
                }

                i++;

            }
        }
    }









}
