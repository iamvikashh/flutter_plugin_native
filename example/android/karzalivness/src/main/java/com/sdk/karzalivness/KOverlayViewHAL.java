package com.sdk.karzalivness;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;

public class KOverlayViewHAL extends View {

    private static final String TAG = KOverlayViewHAL.class.getName();
    //private int margin = 10;
    //private SurfaceHolder mHolder;

    private Paint paintBackground;
    private Paint transparentPaint;
    private Paint paintCircle;
    private Paint paintLine;
    private Paint faceRectPaint;
    private Paint faceRectPaintParent;
    private Paint paintLandmark;
    private int overLayColor = Color.WHITE;
    private int strokeWidth = 5;

    private boolean mIsCanDraw;
    private Handler mHandler;
    private int lastAngle;
    private Rect debugRect;
    private Rect debugRectParent;
    private float[] landMarks;

    private Canvas canvas;
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (!mIsCanDraw) return;
            //Invalidate the view so that it gets re-drawn using the onDraw callback function of View class.
            //Requests a layout pass of the view tree. Called when view has been invalidated.
            invalidate();
            requestLayout();
        }
    };

    public KOverlayViewHAL(Context context) {
        super(context);
        init(context, null);
    }

    public KOverlayViewHAL(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
//        init(context, attrs);
    }

    public KOverlayViewHAL(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }


    //**************************************************************************//
    //************************* Init Method ************************************//

    private void init(Context context, final AttributeSet attributeSet) {

        //Setting if view is being drawn by it's own or not (not, in this case, as we are drawing it)
        //Setting Layer Type for drawing the view as Hardware for smoother, faster
        //animation/view creation and transitions + providing support for almost all types of phones
        //(as Hardware pipeline is present in all phones).
        setWillNotDraw(false);
        setLayerType(LAYER_TYPE_HARDWARE, null);

        //Background Paint.
        paintBackground = new Paint();
        paintBackground.setColor(overLayColor);
        transparentPaint = new Paint();
        transparentPaint.setAntiAlias(true);
        transparentPaint.setColor(getResources().getColor(android.R.color.transparent));
        transparentPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        //Circle Paint.
        paintCircle = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintCircle.setStyle(Paint.Style.STROKE);
        paintCircle.setColor(Color.RED);
        paintCircle.setStrokeWidth(strokeWidth);

//        HandlerThread
//        = new HandlerThread("CircleAnimatorThread");
//        handlerThread.start();

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
        mHandler = new Handler(Looper.getMainLooper());
        bringToFront();
//        margin = getResources().getDimensionPixelSize(R.dimen.view_margin);
//        mHolder = getHolder();
//        mHolder.setFormat(PixelFormat.TRANSPARENT);
//        setZOrderMediaOverlay(true);
    }


    //**************************************************************************//
    //************************** Draw Method ***********************************//

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        this.canvas = canvas;
        if (BuildConfig.DEBUG) Log.e(TAG, "onDraw: Called");
        myDraw();
//        mHandler.postDelayed(runnable,20);
    }

    protected void myDraw() {
//        if (!mIsCanDraw || isStopped) return;
        if (!mIsCanDraw) return;
//        mHandler.removeCallbacksAndMessages(null);
//        canvas = canvas;
        if (canvas == null) return;

        try {
            //We draw the background sheet first (which is a rectangle).
            canvas.drawRect(0, 0, getWidth(), getHeight(), paintBackground);

            //We first draw a transparent paint circle. Then we draw another circle over it, which will
            //have a strokeWidth as well. This is the circle having border color, that is visible to us.
            float radius = getWidth() / 2.0f;
            canvas.drawCircle(getWidth() / 2.0f, getHeight() / 2.0f, (radius - 10), transparentPaint);
            canvas.drawCircle(getWidth() / 2.0f, getHeight() / 2.0f, (radius - 10), paintCircle);

            //Logic for scan line animation.
            if (BuildConfig.DEBUG) Log.e(TAG, "stopScanning: ");
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
            if (BuildConfig.DEBUG)
                Log.e(TAG, "lineStartX  lineStartY: " + lineStartX + " " + lineStartY);
            canvas.drawLine(lineStartX, lineStartY, (float) (lineStartX + baseLength), lineStartY, paintLine);

            //For drawing all different rects for debug purposes.
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

        } catch (Exception e) {
//            mHolder.unlockCanvasAndPost(canvas);
            if (BuildConfig.DEBUG) Log.e(TAG, "Draw Error");
            e.printStackTrace();
        }

        //We keep on showing this renewed value animation, until we get stopScanning = false.
        mHandler.postDelayed(runnable, 20);
    }


    //**************************************************************************//
    //********************* Other Important Methods ****************************//

    void setOverLayColor(@ColorInt int overLayColor) {
        paintBackground.setColor(overLayColor);
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

    void setDebugRect(Rect rect, Rect parentRect, float[] landmarks) {
        this.debugRect = rect;
        this.debugRectParent = parentRect;
        this.landMarks = landmarks;
    }


    //**************************************************************************//
    //********************* SurfaceHolder Callback Methods *********************//

//    @Override
//    public void surfaceCreated(@NonNull SurfaceHolder holder) {
//        mIsCanDraw = true;
//    }
//
//    @Override
//    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
//        if (mHandler != null) {
//            mHandler.postDelayed(this::myDraw, 10);
//        }
//    }
//
//    @Override
//    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
//        mIsCanDraw = false;
//    }


    //**************************************************************************//
    //******************* Other View Override Methods ***************************//

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (BuildConfig.DEBUG) Log.e(TAG, "onAttachedToWindow: called");
        mIsCanDraw = true;
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
    }


    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (BuildConfig.DEBUG) Log.e(TAG, "onDetachedFromWindow: called");
        mIsCanDraw = false;
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
    }


    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
    }
}
