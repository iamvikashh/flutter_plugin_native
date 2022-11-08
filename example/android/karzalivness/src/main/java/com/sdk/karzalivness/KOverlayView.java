package com.sdk.karzalivness;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sdk.karzalivness.R;

class KOverlayView extends SurfaceView implements SurfaceHolder.Callback {

    private Paint painBackground;
    private Paint transparentPaint;
    private Paint paintCircle;
    private Paint paintLine;
    private Paint faceRectPaint;
    private Paint faceRectPaintParent;
    private Paint paintLandmark;
    //private int margin = 10;
    private int overLayColor = Color.WHITE;
    private int strokeWidth = 5;
    private boolean mIsCanDraw;
    private SurfaceHolder mHolder;
    private Handler mHandler;
    private int lastAngle;
    private boolean isStopped;
    private Rect debugRect;
    private Rect debugRectParent;
    private boolean stopScanning;
    private float[] landMarks;
    private final Runnable runnable = this::myDraw;

    public KOverlayView(Context context) {
        super(context);
        init(context, null);
    }

    public KOverlayView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public KOverlayView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }


    //**************************************************************************//
    //************************* Init Method ************************************//

    private void init(Context context, final AttributeSet attributeSet) {
        painBackground = new Paint();
        painBackground.setColor(overLayColor);
        transparentPaint = new Paint();
        transparentPaint.setAntiAlias(true);
        transparentPaint.setColor(getResources().getColor(android.R.color.transparent));
        transparentPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        //Circle Paint.
        paintCircle = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintCircle.setStyle(Paint.Style.STROKE);
        paintCircle.setColor(Color.RED);
        paintCircle.setStrokeWidth(5);
        HandlerThread handlerThread = new HandlerThread("CircleAnimatorThread");
        handlerThread.start();

        //Scan Line Paint.
        paintLine = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintLine.setStyle(Paint.Style.STROKE);
        paintLine.setColor(Color.WHITE);
        paintLine.setStrokeWidth(2);

        //Child/Red Rect Paint.
        faceRectPaint = new Paint();
        faceRectPaint.setAntiAlias(true);
        faceRectPaint.setColor(Color.RED);
        faceRectPaint.setStyle(Paint.Style.STROKE);
        faceRectPaint.setStrokeWidth(5);

        //Parent/Yellow Rect Paint.
        faceRectPaintParent = new Paint();
        faceRectPaintParent.setAntiAlias(true);
        faceRectPaintParent.setColor(Color.YELLOW);
        faceRectPaintParent.setStyle(Paint.Style.STROKE);
        faceRectPaintParent.setStrokeWidth(3);

        //Landmarks Paint.
        paintLandmark = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintLandmark.setStyle(Paint.Style.STROKE);
        paintLandmark.setColor(Color.RED);
        paintLandmark.setStrokeWidth(3);

        //Handler calls.
        mHandler = new Handler(handlerThread.getLooper());
        //margin = getResources().getDimensionPixelSize(R.dimen.view_margin);
        mHolder = getHolder();
        mHolder.setFormat(PixelFormat.TRANSPARENT);
        setZOrderMediaOverlay(true);
        mHolder.addCallback(this);
    }


    //**************************************************************************//
    //************************** Draw Method ***********************************//

    protected void myDraw() {
        if (!mIsCanDraw || isStopped) return;
        mHandler.removeCallbacksAndMessages(null);
        Canvas canvas = mHolder.lockCanvas();
        if (canvas == null) return;

        try {
            canvas.drawRect(0, 0, getWidth(), getHeight(), painBackground);
            float radius = getWidth() / 2.0f;

            //We first draw a transparent paint circle. Then we draw another circle over it, which will
            //have a strokeWidth as well. This is the circle having border color, that is visible to us.
            //canvas.drawCircle(getWidth()/2.0f,getHeight()/2.0f, radius - margin, transparentPaint);
            canvas.drawCircle(getWidth() / 2.0f, getHeight() / 2.0f, radius - 10, transparentPaint);
            //canvas.drawCircle(getWidth()/2.0f,getHeight()/2.0f, (radius - margin), paintCircle);
            canvas.drawCircle(getWidth() / 2.0f, getHeight() / 2.0f, (radius - 10), paintCircle);
//            Log.i("ABCD", "Radius overlay : " + (radius - margin) + " Margin : " + margin);

            //Logic for scan line animation.
            if (!stopScanning) {

                lastAngle += 5;
                if (lastAngle > 360) {
                    lastAngle = 0;
                }
                float centerX = getWidth() / 2.0f;
                float centerY = getHeight() / 2.0f;
                //double circleRadius = radius - margin - strokeWidth;
                double circleRadius = radius - strokeWidth;
                double angle = Math.toRadians(lastAngle);
                double radiusSqr = circleRadius * circleRadius;
                // use cosine formula to calculate arc line length
                double baseLength = Math.sqrt(radiusSqr + radiusSqr - (2 * radiusSqr * Math.cos(angle)));
                // pythagoras theorem to find height of bottom of rectangle
                double height = Math.sqrt(radiusSqr - ((baseLength / 2.0) * (baseLength / 2.0)));
                float lineStartX = (float) (centerX - (baseLength / 2.0f));
                // If angle is more than 180, its bottom part of circle where y is negative.
                // So to map it with android screen coordinates, we need to add it to existing height.
                float lineStartY = lastAngle > 180 ? (float) (centerY + height) : (float) (centerY - height);

                canvas.drawLine(lineStartX, lineStartY, (float) (lineStartX + baseLength), lineStartY, paintLine);

            }

            if (debugRect != null) {
                canvas.drawRect(debugRect, faceRectPaint);
            }
            if (debugRectParent != null) {
                canvas.drawRect(debugRectParent, faceRectPaintParent);
            }
            if (landMarks != null && landMarks.length > 0) {
                for (int i = 0; i < landMarks.length; ) {
                    canvas.drawCircle(landMarks[i++], landMarks[i++], 3.0f, paintLandmark);
                }
            }

            mHolder.unlockCanvasAndPost(canvas);

        } catch (Exception e) {
            mHolder.unlockCanvasAndPost(canvas);
        }

        //We keep on showing this renewed value animation, until we get stopScanning = false.
        mHandler.postDelayed(runnable, 20);
    }


    //**************************************************************************//
    //********************* Other Important Methods ****************************//

    void setOverLayColor(@ColorInt int overLayColor) {
        painBackground.setColor(overLayColor);
        //This does immediate change reflection, as next cycle is called immediately.
        postInvalidate();
    }

    void setBoundaryColor(@ColorInt int overLayColor, int strokeWidth) {
        this.overLayColor = overLayColor;
        this.strokeWidth = strokeWidth;
        paintCircle.setColor(overLayColor);
        paintCircle.setStrokeWidth(strokeWidth);
        postInvalidate();
    }

    void stopAnimation() {
        this.isStopped = true;
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
    }

    void stopScanning() {
        this.stopScanning = true;
    }

    void setDebugRect(Rect rect, Rect parentRect, float[] landmarks) {
        this.debugRect = rect;
        this.debugRectParent = parentRect;
        this.landMarks = landmarks;
    }



    //**************************************************************************//
    //********************* SurfaceHolder Callback Methods *********************//

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        mIsCanDraw = true;
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        if (mHandler != null) {
            mHandler.postDelayed(this::myDraw, 10);
        }
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        mIsCanDraw = false;
    }
}
