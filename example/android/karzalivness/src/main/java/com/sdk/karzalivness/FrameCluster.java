package com.sdk.karzalivness;

import android.graphics.Bitmap;
import android.graphics.Rect;

class FrameCluster {

    private Bitmap bitmap;
    private float score;
    private Rect rect;
    private float[] landmarks;


    //********************** Setter Functions *********************//

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public void setScore(float score) {
        this.score = score;
    }

    public void setRect(Rect rect) {
        this.rect = rect;
    }

    public void setLandmarks(float[] landmarks) {
        this.landmarks = landmarks;
    }


    //*********************** Getter Functions **********************//

    public Bitmap getBitmap() {
        return bitmap;
    }

    public float getScore() {
        return score;
    }

    public Rect getRect() {
        return rect;
    }

    public float[] getLandmarks() {
        return landmarks;
    }

}
