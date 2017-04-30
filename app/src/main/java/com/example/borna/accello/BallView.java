package com.example.borna.accello;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.EmbossMaskFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.example.borna.accello.obstacles.GameObject;
import com.example.borna.accello.obstacles.PlayerObject;
import com.example.borna.accello.obstacles.PowerUp;
import com.example.borna.accello.util.ColorUtil;
import com.example.borna.accello.util.GeometryUtil;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

import static com.example.borna.accello.obstacles.ObjectPower.GROW;
import static com.example.borna.accello.obstacles.ObjectPower.INVERT_CONTROL;
import static com.example.borna.accello.obstacles.ObjectPower.SHRINK;
import static com.example.borna.accello.obstacles.ObjectPower.SPEED_UP;

/**
 * Created by bkoruznjak on 16/03/2017.
 */

public class BallView extends SurfaceView implements Runnable, SensorEventListener, View.OnTouchListener {

    private static final int TARGET_FPS = 60;
    private final int BALL_RADIUS = 5;
    private int mPowerUpMaxSize;
    private int mWidth;
    private int mHeight;
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
    private float[] gyroCoordinates = new float[2];
    private long mRespawnCooldownHolder = 0;
    private ArrayList<PowerUp> gameObjectsList = new ArrayList<>();
    private int mMaxScreenSize;
    private Rect mViewRect;
    private int mObjectSpawnedCounter;
    private Paint mPlayerObjectPaint;
    private PlayerObject mPlayer;
    private boolean readyToRun;
    private Paint mUiPaint;

    public BallView(Context context) {
        super(context);
        init();
    }

    public BallView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        this.mTargetFrameDrawTime = 1000f / TARGET_FPS;
        this.mSurfaceHolder = getHolder();
        mUiPaint = new Paint();
        mUiPaint.setAntiAlias(true);
        mUiPaint.setColor(ColorUtil.COLOR_TEXT);
        mUiPaint.setTextAlign(Paint.Align.CENTER);
        mUiPaint.setTextSize(100);

        setOnTouchListener(this);

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

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;

        mViewRect = new Rect(0, 0, w, h);
        if (mWidth > mHeight) {
            mScreenWidthOnePercent = mHeight / 100.0f;
            mMaxScreenSize = mHeight / 2;
        } else {
            mScreenWidthOnePercent = mWidth / 100.0f;
            mMaxScreenSize = mWidth / 2;
        }

        mActualBallRadius = (int) (BALL_RADIUS * mScreenWidthOnePercent);
        EmbossMaskFilter embossfilter = new EmbossMaskFilter(new float[]{1, 1, 1}, 0.3f, 8f, 20f);
        mPlayerObjectPaint = new Paint();
        mPlayerObjectPaint.setColor(ColorUtil.COLOR_PLAYER);
        mPlayerObjectPaint.setMaskFilter(embossfilter);
        mPlayerObjectPaint.setAntiAlias(true);
        mPlayerObjectPaint.setStyle(Paint.Style.FILL);

        mPowerUpMaxSize = (int) mScreenWidthOnePercent * 5;
        mPlayer = new PlayerObject(mWidth / 2, mHeight / 2, mActualBallRadius, (int) (mScreenWidthOnePercent / 50.0f));
        mPlayer.setPaint(mPlayerObjectPaint);
        mPlayer.setHeightBoundary(mHeight);
        mPlayer.setWidthBoundary(mWidth);
        readyToRun = true;
    }

    @Override
    public void run() {
        while (running) {
            if (readyToRun) {
                update();
                draw();
                control();
            }
        }
    }

    private void update() {
        if (mWidth > 0 && mHeight > 0 && System.currentTimeMillis() - mGameTimeStart > mRespawnCooldownHolder) {
            mObjectSpawnedCounter++;
            mRespawnCooldownHolder += 1000;

            //API lvl 21
            int spawnX = ThreadLocalRandom.current().nextInt(mPowerUpMaxSize, mWidth - mPowerUpMaxSize);
            int spawnY = ThreadLocalRandom.current().nextInt(mPowerUpMaxSize, mHeight - mPowerUpMaxSize);


            PowerUp newObject = new PowerUp(spawnX, spawnY, mPowerUpMaxSize);
            /*
            balance consideration:

            on a 1000 item spawn we will have:
                    250 Speed ups
                    250 Control inversions
                    167 Rapid grows
                    333 Shrinks
             */
            if (mObjectSpawnedCounter % 4 == 0) {
                newObject.setPower(SPEED_UP);
            } else if (mObjectSpawnedCounter % 3 == 0) {
                newObject.setPower(INVERT_CONTROL);
            } else if (mObjectSpawnedCounter % 2 == 0) {
                newObject.setPower(GROW);
            } else {
                newObject.setPower(SHRINK);
            }
            gameObjectsList.add(0, newObject);
        }

        //loop through and see if you touch them or not
        for (int index = 0; index < gameObjectsList.size(); index++) {
            PowerUp object = gameObjectsList.get(index);
            if (object.isUsable() && GeometryUtil.areCirclesOverlapping(mPlayer.getOriginX(), mPlayer.getOriginY(), mPlayer.getPlayerRadius(), object.getOriginX(), object.getOriginY(), object.getSize())) {
                gameObjectsList.remove(object);
                switch (object.getPower()) {
//                    case GROW:
//                        mPlayer.triggerRapidGrowth();
//                        break;
//                    case SPEED_UP:
//                        mPlayer.triggerSpeedUp();
//                        break;
//                    case INVERT_CONTROL:
//                        mPlayer.triggerInvertControl();
//                        break;
//                    case SHRINK:
//                        mPlayer.triggerRapidShrink();
//                        break;
                    default:
                        //FOR FAST END GAME UI DEBUG
                        mPlayer.triggerRapidGrowth();
                        break;
                }
            } else {
                object.grow();
            }
        }

        mPlayer.moveWithConstraints(gyroCoordinates[1] * 2, gyroCoordinates[0] * 2);
        if (mPlayer.getPlayerRadius() + (mScreenWidthOnePercent / 50.0f) >= mMaxScreenSize) {
            endGame();
        } else {
            mPlayer.live();
        }
    }

    private void endGame() {
        if (mSurfaceHolder.getSurface().isValid()) {
            //Get start time for FPS calculation
            mTimeStartCurrentFrame = System.nanoTime() / 1000000;
            //First we lock the area of memory we will be drawing to
            mScreenCanvas = mSurfaceHolder.lockCanvas();

            mScreenCanvas.drawColor(ColorUtil.COLOR_DARK_OVERLAY);
            mScreenCanvas.drawText("GAME OVER", mWidth / 2, mHeight / 2, mUiPaint);
            mScreenCanvas.drawText("tap to restart", mWidth / 2, mHeight / 2 + 100, mUiPaint);
            mSurfaceHolder.unlockCanvasAndPost(mScreenCanvas);
        }
        pause();
    }

    private void draw() {
        if (mSurfaceHolder.getSurface().isValid()) {
            //Get start time for FPS calculation
            mTimeStartCurrentFrame = System.nanoTime() / 1000000;
            //First we lock the area of memory we will be drawing to
            mScreenCanvas = mSurfaceHolder.lockCanvas();

            // Rub out the last frame
            mScreenCanvas.drawColor(ColorUtil.COLOR_BACKGROUND);

            for (int index = 0; index < gameObjectsList.size(); index++) {
                GameObject object = gameObjectsList.get(index);
                mScreenCanvas.drawCircle(object.getOriginX(), object.getOriginY(), object.getSize(), object.getPaint());
            }
            mScreenCanvas.drawCircle(mPlayer.getOriginX(), mPlayer.getOriginY(), mPlayer.getPlayerRadius(), mPlayer.getPaint());
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
        mGameTimeStart = System.currentTimeMillis();
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

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN && !running) {
            gameObjectsList.clear();
            mObjectSpawnedCounter = 0;
            mRespawnCooldownHolder = 0;
            mPlayer.resetPlayer();
            resume();
        }
        return false;
    }
}
