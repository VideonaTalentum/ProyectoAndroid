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
package com.google.android.gms.samples.vision.face.facetracker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

import com.google.android.gms.samples.vision.face.facetracker.ui.camera.GraphicOverlay;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.Landmark;

import java.util.ArrayList;
import java.util.List;

/**
 * Graphic instance for rendering face position, orientation, and landmarks within an associated
 * graphic overlay view.
 */
class FaceGraphic extends GraphicOverlay.Graphic {
    private static final float FACE_POSITION_RADIUS = 20.0f;
    private static final float ID_TEXT_SIZE = 40.0f;
    private static final float ID_Y_OFFSET = 50.0f;
    private static final float ID_X_OFFSET = -50.0f;
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
    private Paint mDibujoPaint;

    private volatile Face mFace;

    private Boolean sombrero = false;
    private Boolean ojos = false;
    private Boolean boca = false;

    private Bitmap sombreroBitmap;
    private Bitmap ojosBitmap;
    private Bitmap bocaBitmap;

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

        mDibujoPaint = new Paint();
    }

    void setId(int id) {
        mFaceId = id;
    }

    void setmBitmap(Bitmap bitmap){
        sombreroBitmap = bitmap;
    }

    void setOjoBitmap(Bitmap bitmap){
        ojosBitmap = bitmap;
    }

    void setBocaBitmap(Bitmap bitmap){
        bocaBitmap = bitmap;
    }

    void setSombrero(Boolean sombrero){
        this.sombrero=sombrero;
    }

    void setOjos(Boolean ojos){
        this.ojos=ojos;
    }

    void setBoca(Boolean boca){
        this.boca=boca;
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
        RectF rectF = new RectF(left,top-2*(bottom-top)/3,right,bottom-2*(bottom-top)/3);


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

        Paint paint = new Paint();
        paint.setColor(Color.GREEN);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);


        int i=0;
        List<PointF> lista = new ArrayList<>();
        for (Landmark landmark : mFace.getLandmarks()) {
                float cx = translateX(landmark.getPosition().x);
                float cy = translateY(landmark.getPosition().y);
            if (i<2 && ojos) {
                float xOffset1 = (scaleX(face.getWidth() / 2.0f)/3);
                float yOffset1 = (scaleY(face.getHeight() / 2.0f)/3);
                float left1 = cx - xOffset1;
                float top1 = cy - yOffset1;
                float right1 = cx + xOffset1;
                float bottom1 = cy + yOffset1;
                RectF rectF1 = new RectF(left1,top1,right1,bottom1);

                canvas.drawBitmap(ojosBitmap, null,rectF1, mBoxPaint);

            }

            if(i>4 && boca){

                PointF punto = new PointF(cx,cy);

                lista.add(punto);
                if(lista.size()==3) {
                    float xOffset1 = (scaleX(face.getWidth() / 2.0f)/4);
                    float yOffset1 = (scaleY(face.getHeight() / 2.0f)/4);
                    float left1 = cx - xOffset1;
                    float top1 = cy - yOffset1;
                    float right1 = cx + xOffset1;
                    float bottom1 = cy + yOffset1;
                    RectF rectF1 = new RectF(left1,top1,right1,bottom1);
                    canvas.drawBitmap(bocaBitmap,null,rectF1,mBoxPaint);

                }
            }
            i++;
        }


    }





}
