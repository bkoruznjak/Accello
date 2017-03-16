package com.example.borna.accello;

/**
 * Created by bkoruznjak on 16/03/2017.
 */

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by bkoruznjak on 12/03/2017.
 */

public class MainView extends SurfaceView implements Runnable {

    private static final int TARGET_FPS = 60;
    private int mWidth;
    private int mHeight;
    private Paint mCirclePaint;

    private Circle mCircle;

    private float mTargetFrameDrawTime;
    private SurfaceHolder mSurfaceHolder;
    private long mTimeStartCurrentFrame;
    private long mDelta;
    private long mTimeEndCurrentFrame;
    private long mTimeSleepInMillis;
    private Thread drawingThread = null;
    private Canvas mScreenCanvas;
    private volatile boolean running;
    private volatile int yHolder = 1;
    private volatile int xHolder = 1;
    private volatile boolean rising;

    private Rect mViewRect;

    public MainView(Context context) {
        super(context);
        init();
    }

    public MainView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {

        mCirclePaint = new Paint();
        mCirclePaint.setColor(Color.BLUE);
        mCirclePaint.setAntiAlias(true);
        mCirclePaint.setStyle(Paint.Style.FILL);

        this.mTargetFrameDrawTime = 1000f / TARGET_FPS;
        this.mSurfaceHolder = getHolder();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;

        mViewRect = new Rect(0, 0, w, h);
        float radius = (mWidth / 100.0f) * 5.0f;

        mCircle = new Circle(mWidth / 2, mHeight / 2, radius);
    }

    @Override
    public void run() {
        while (running) {
            update();
            draw();
            control();
        }
    }

    private void draw() {
        if (mSurfaceHolder.getSurface().isValid()) {
            //Get start time for FPS calcualtion
            mTimeStartCurrentFrame = System.nanoTime() / 1000000;
            //First we lock the area of memory we will be drawing to
            mScreenCanvas = mSurfaceHolder.lockCanvas();

            // Rub out the last frame
            mScreenCanvas.drawColor(Color.argb(255, 255, 255, 255));

            mScreenCanvas.drawCircle(mCircle.getCenterX(), mCircle.getCenterY(), mCircle.getRadius(), mCirclePaint);

            // Unlock and draw the scene
            mSurfaceHolder.unlockCanvasAndPost(mScreenCanvas);
            //Get end time for FPS calcualtion
            mTimeEndCurrentFrame = System.nanoTime() / 1000000;
        }
    }

    private void update() {
        if (mCircle != null) {

            if (yHolder > 10) {
                rising = false;
            } else if (yHolder < -10) {
                rising = true;
            }

            if (rising) {
                yHolder += 1;
            } else {
                yHolder -= 1;
            }

            if (mCircle.getCenterX() + mCircle.getRadius() >= mWidth) {
                xHolder = -1;
            } else if (mCircle.getCenterX() - mCircle.getRadius() <= 0) {
                xHolder = 1;
            }

            mCircle.move(xHolder, yHolder);
        }
    }

    private void control() {
        try {
            //calculate FPS
            mDelta = mTimeEndCurrentFrame - mTimeStartCurrentFrame;
            mTimeSleepInMillis = (long) (mTargetFrameDrawTime - mDelta);
            if (mTimeSleepInMillis > 0) {
                drawingThread.sleep(mTimeSleepInMillis);
            }
        } catch (InterruptedException e) {
            Log.e("bbb", "InterruptedException:" + e);
        }

    }

    public void start() {
        running = true;
        drawingThread = new Thread(this);
        drawingThread.start();
    }

    public void pause() {
        running = false;
        try {
            drawingThread.join();
        } catch (InterruptedException e) {
            Log.e("bbb", "InterruptedException:" + e);
        }
    }

    public void resume() {
        Log.d("bbb", "resuming");
        running = true;
        drawingThread = new Thread(this);
        drawingThread.start();
    }

}
