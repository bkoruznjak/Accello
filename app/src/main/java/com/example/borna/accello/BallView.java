package com.example.borna.accello;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.example.borna.accello.obstacles.GameObject;

import java.util.ArrayList;
import java.util.Random;

import static com.example.borna.accello.R.drawable.ball;

/**
 * Created by bkoruznjak on 16/03/2017.
 */

public class BallView extends SurfaceView implements Runnable, SensorEventListener {

    private static final int TARGET_FPS = 60;
    private final int BALL_RADIUS = 5;
    Path mTrailPath;
    private int mWidth;
    private int mHeight;
    private Paint mCircleOuterPaint;
    private Paint mCircleInnerPaint;
    private Circle mCircle;
    private float mScreenWidthOnePercent;
    private float mActualBallRadius;
    private float mTargetFrameDrawTime;
    private SurfaceHolder mSurfaceHolder;
    private long mTimeStartCurrentFrame;
    private long mDelta;
    private long mTimeEndCurrentFrame;
    private long mTimeSleepInMillis;
    private long mGameTimeStart;
    private Thread drawingThread = null;
    private Canvas mScreenCanvas;
    private volatile boolean running;
    private volatile int yHolder = -1;
    private volatile int xHolder = -1;
    private int mBallSize;
    private volatile boolean rising;
    private float[] gyroCoordinates = new float[2];
    private BitmapDrawable mBall;
    private Bitmap mBallBitmap;
    private long mRespawnCooldownHolder = 0;
    private ArrayList<GameObject> gameObjectsList = new ArrayList<>();
    private int mMaxScreenSize;
    private Rect mViewRect;

    public BallView(Context context) {
        super(context);
        init();
    }

    public BallView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {


        mCircleOuterPaint = new Paint();
        mCircleOuterPaint.setColor(Color.BLUE);
        mCircleOuterPaint.setAntiAlias(true);
        mCircleOuterPaint.setStyle(Paint.Style.STROKE);
        mCircleOuterPaint.setStrokeCap(Paint.Cap.ROUND);

        mCircleInnerPaint = new Paint();
        mCircleInnerPaint.setColor(Color.CYAN);
        mCircleInnerPaint.setAntiAlias(true);
        mCircleInnerPaint.setStyle(Paint.Style.FILL);

        this.mTargetFrameDrawTime = 1000f / TARGET_FPS;
        this.mSurfaceHolder = getHolder();

        mBall = (BitmapDrawable) getContext().getResources().getDrawable(ball);
        mBallBitmap = mBall.getBitmap();
        mBallSize = mBallBitmap.getWidth();

        mTrailPath = new Path();

        SensorManager manager = (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);
        if (manager.getSensorList(Sensor.TYPE_ACCELEROMETER).size() == 0) {
            Log.e("bbb", " No accelerometer installed");
        } else {
            Sensor accelerometer = manager.getSensorList(
                    Sensor.TYPE_ACCELEROMETER).get(0);
            if (!manager.registerListener(this, accelerometer,
                    SensorManager.SENSOR_DELAY_GAME)) {
                Log.e("bbb", " Couldn't register sensor listener");
            }
        }

        mGameTimeStart = System.currentTimeMillis();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;

        mViewRect = new Rect(0, 0, w, h);
        float radius = (mWidth / 100.0f) * 5.0f;

        mCircle = new Circle(mWidth / 2, mHeight / 2, radius);

        if (mWidth > mHeight) {
            mScreenWidthOnePercent = mHeight / 100.0f;
            mMaxScreenSize = mHeight / 2;
        } else {
            mScreenWidthOnePercent = mWidth / 100.0f;
            mMaxScreenSize = mWidth / 2;
        }

        mCircleOuterPaint.setStrokeWidth(mScreenWidthOnePercent);
        mActualBallRadius = (int) (BALL_RADIUS * mScreenWidthOnePercent);
    }

    @Override
    public void run() {
        while (running) {
            update();
            draw();
            control();
        }
    }

    private void update() {
        //todo spawn random circles

        if (mWidth > 0 && mHeight > 0 && System.currentTimeMillis() - mGameTimeStart > mRespawnCooldownHolder) {
            mRespawnCooldownHolder += 5000;
            Random rnd = new Random();
            int spawnX = rnd.nextInt(mWidth);
            int spawnY = rnd.nextInt(mHeight);
            gameObjectsList.add(new GameObject(spawnX, spawnY));
            Log.d("bbb", "spawning somehting at x:" + spawnX + " y:" + spawnY);
        }

        for (GameObject object : gameObjectsList) {
            object.grow();
        }

        //grow
        if (mActualBallRadius + (mScreenWidthOnePercent / 50.0f) >= mMaxScreenSize) {
            //todo game over
            Log.d("bbb", "veci je stop");

        } else {
            mActualBallRadius += mScreenWidthOnePercent / 50.0f;
        }
//        mTrailPath.lineTo(xHolder,yHolder);

        if (xHolder < 0 && yHolder < 0) {
            xHolder = mWidth / 2;
            yHolder = mHeight / 2;
        } else {
            xHolder += gyroCoordinates[1] * 2; //xVelocity
            yHolder += gyroCoordinates[0] * 2; //yVelocity
            if (xHolder > mWidth - (int) mActualBallRadius) {
                xHolder = mWidth - (int) mActualBallRadius;
            } else if (xHolder - mActualBallRadius < 0) {
                xHolder = (int) mActualBallRadius;
            }

            if (yHolder > mHeight - (int) mActualBallRadius) {
                yHolder = mHeight - (int) mActualBallRadius;
            } else if (yHolder - mActualBallRadius < 0) {
                yHolder = (int) mActualBallRadius;
            }
        }
    }

    private void draw() {
        if (mSurfaceHolder.getSurface().isValid()) {
            //Get start time for FPS calculation
            mTimeStartCurrentFrame = System.nanoTime() / 1000000;
            //First we lock the area of memory we will be drawing to
            mScreenCanvas = mSurfaceHolder.lockCanvas();

            // Rub out the last frame
            mScreenCanvas.drawColor(Color.argb(255, 255, 255, 255));

//            mScreenCanvas.drawPath(mTrailPath, mCircleOuterPaint);
            for (GameObject object : gameObjectsList) {
                mScreenCanvas.drawCircle(object.getOriginX(), object.getOriginY(), object.getSize(), mCircleInnerPaint);
            }


            mScreenCanvas.drawCircle(xHolder, yHolder, mActualBallRadius, mCircleInnerPaint);
            mScreenCanvas.drawCircle(xHolder, yHolder, mActualBallRadius, mCircleOuterPaint);


            // Unlock and draw the scene
            mSurfaceHolder.unlockCanvasAndPost(mScreenCanvas);
            //Get end time for FPS calcualtion
            mTimeEndCurrentFrame = System.nanoTime() / 1000000;
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

    @Override
    public void onSensorChanged(SensorEvent event) {
        gyroCoordinates[0] = (event.values[0]);
        gyroCoordinates[1] = (event.values[1]);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
