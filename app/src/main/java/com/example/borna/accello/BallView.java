package com.example.borna.accello;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.example.borna.accello.obstacles.GameObject;
import com.example.borna.accello.obstacles.PlayerObject;
import com.example.borna.accello.obstacles.PowerUp;
import com.example.borna.accello.util.GeometryUtil;

import java.util.ArrayList;
import java.util.Random;

import static com.example.borna.accello.obstacles.ObjectPower.GROW;
import static com.example.borna.accello.obstacles.ObjectPower.SHRINK;
import static com.example.borna.accello.obstacles.ObjectPower.SLOW_DOWN;
import static com.example.borna.accello.obstacles.ObjectPower.SPEED_UP;

/**
 * Created by bkoruznjak on 16/03/2017.
 */

public class BallView extends SurfaceView implements Runnable, SensorEventListener {

    private static final int TARGET_FPS = 60;
    private static final int POWERUP_MAX_SIZE = 50;
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
    private volatile boolean rising;
    private float[] gyroCoordinates = new float[2];
    private long mRespawnCooldownHolder = 0;
    private ArrayList<PowerUp> gameObjectsList = new ArrayList<>();
    private int mMaxScreenSize;
    private Rect mViewRect;
    private int mObjectSpawnedCounter;
    private Paint mPlayerObjectPaint;
    private PlayerObject mPlayer;
    private boolean readyToRun;

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

        mActualBallRadius = (int) (BALL_RADIUS * mScreenWidthOnePercent);
        mPlayerObjectPaint = new Paint();
        mPlayerObjectPaint.setColor(Color.BLUE);
        mPlayerObjectPaint.setAntiAlias(true);
        mPlayerObjectPaint.setStyle(Paint.Style.FILL);

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
        //random game object spawning
        if (mWidth > 0 && mHeight > 0 && System.currentTimeMillis() - mGameTimeStart > mRespawnCooldownHolder) {
            mObjectSpawnedCounter++;
            mRespawnCooldownHolder += 1000;
            Random rnd = new Random();

            int spawnX = rnd.nextInt(mWidth);
            int spawnY = rnd.nextInt(mHeight);

            //we add a bit of margin equal to the max size of the object
            if (spawnX <= POWERUP_MAX_SIZE) {
                spawnX += POWERUP_MAX_SIZE;
            } else if (spawnX >= mWidth - POWERUP_MAX_SIZE) {
                spawnX -= POWERUP_MAX_SIZE;
            }

            if (spawnY <= POWERUP_MAX_SIZE) {
                spawnY += POWERUP_MAX_SIZE;
            } else if (spawnY >= mHeight - POWERUP_MAX_SIZE) {
                spawnY -= POWERUP_MAX_SIZE;
            }

            PowerUp newObject = new PowerUp(spawnX, spawnY, POWERUP_MAX_SIZE);
            if (mObjectSpawnedCounter % 4 == 0) {
                newObject.setPower(GROW);
            } else if (mObjectSpawnedCounter % 3 == 0) {
                newObject.setPower(SPEED_UP);
            } else if (mObjectSpawnedCounter % 2 == 0) {
                newObject.setPower(SHRINK);
            } else {
                newObject.setPower(SLOW_DOWN);
            }
            gameObjectsList.add(newObject);
            Log.d("bbb", "spawning OBJECT at x:" + spawnX + " y:" + spawnY + ", with power:" + newObject.getPower());
        }

        //loop through and see if you touch them or not
        for (int index = 0; index < gameObjectsList.size(); index++) {
            PowerUp object = gameObjectsList.get(index);
            if (object.isUsable() && GeometryUtil.areCirclesOverlapping(mPlayer.getOriginX(), mPlayer.getOriginY(), mPlayer.getPlayerRadius(), object.getOriginX(), object.getOriginY(), object.getSize())) {
                gameObjectsList.remove(object);
                switch (object.getPower()) {
                    case GROW:
                    case SPEED_UP:
                        mActualBallRadius += (mScreenWidthOnePercent * 5);
                        break;
                    case SLOW_DOWN:
                    case SHRINK:
                        mActualBallRadius -= (mScreenWidthOnePercent * 3);
                        break;
                    default:
                        break;
                }
                //todo inherit the power to the player whetever the object power was
                Log.d("bbb", "overlap CAUGHT, REMOVING OBJECT FROM SCREEN");
            } else {
                object.grow();
            }
        }

        mPlayer.moveWithConstraints(gyroCoordinates[1] * 2, gyroCoordinates[0] * 2);
        //grow
        if (mPlayer.getPlayerRadius() + (mScreenWidthOnePercent / 50.0f) >= mMaxScreenSize) {
            //todo game over
            Log.d("bbb", "game should end");
        } else {
            mPlayer.growNormal();
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
