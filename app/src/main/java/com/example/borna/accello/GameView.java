package com.example.borna.accello;

import android.content.Context;
import android.content.SharedPreferences;
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
import com.example.borna.accello.util.GameHints;
import com.example.borna.accello.util.GeometryUtil;
import com.example.borna.accello.util.StringConstants;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

import static android.view.HapticFeedbackConstants.KEYBOARD_TAP;
import static com.example.borna.accello.obstacles.ObjectPower.GROW;
import static com.example.borna.accello.obstacles.ObjectPower.INVERT_CONTROL;
import static com.example.borna.accello.obstacles.ObjectPower.SHRINK;
import static com.example.borna.accello.obstacles.ObjectPower.SPEED_UP;

/**
 * Created by bkoruznjak on 16/03/2017.
 */

public class GameView extends SurfaceView implements Runnable, SensorEventListener, View.OnTouchListener {

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
    private Paint mHUDTextPaint;
    private int mHUDHeight;
    private Paint mHUDPaint;

    private int countPowerShrink;
    private int countPowerGrow;
    private int countPowerSpeed;
    private int countPowerInvert;
    private Paint mPowerUpPaint;
    private float gameTime;

    private Context mContext;
    private SharedPreferences mPrefs;
    private float mHighScore;


    public GameView(Context context) {
        super(context);
        init();
    }

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        this.mTargetFrameDrawTime = 1000f / TARGET_FPS;
        this.mSurfaceHolder = getHolder();
        mHUDTextPaint = new Paint();
        mHUDTextPaint.setAntiAlias(true);
        mHUDTextPaint.setColor(ColorUtil.COLOR_TEXT);
        mHUDTextPaint.setTextAlign(Paint.Align.CENTER);
        mHUDTextPaint.setTextSize(100);

        mPowerUpPaint = new Paint();
        mPowerUpPaint.setAntiAlias(true);
        mPowerUpPaint.setStyle(Paint.Style.FILL);

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


    private void setupNewGame() {
        gameObjectsList.clear();
        mObjectSpawnedCounter = 0;
        mRespawnCooldownHolder = 0;
        mPlayer.resetPlayer();

        countPowerShrink = 0;
        countPowerGrow = 0;
        countPowerSpeed = 0;
        countPowerInvert = 0;
        gameTime = 0;
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
        mHUDHeight = (int) (5 * mScreenWidthOnePercent);
        mHUDPaint = new Paint();
        mHUDPaint.setAntiAlias(true);
        mHUDPaint.setColor(ColorUtil.COLOR_TEXT);
        mHUDPaint.setTextSize(mHUDHeight / 2);
        mHUDPaint.setTextAlign(Paint.Align.CENTER);

        EmbossMaskFilter embossfilter = new EmbossMaskFilter(new float[]{1, 1, 1}, 0.3f, 8f, 20f);
        mPlayerObjectPaint = new Paint();
        mPlayerObjectPaint.setColor(ColorUtil.COLOR_PLAYER);
        mPlayerObjectPaint.setMaskFilter(embossfilter);
        mPlayerObjectPaint.setAntiAlias(true);
        mPlayerObjectPaint.setStyle(Paint.Style.FILL);

        mPowerUpMaxSize = (int) mScreenWidthOnePercent * 5;
        mPlayer = new PlayerObject(mWidth / 2, mHeight / 2, mActualBallRadius, (int) (mScreenWidthOnePercent / 50.0f));
        mPlayer.setPaint(mPlayerObjectPaint);
        mPlayer.setHeightBoundary(mHeight - mHUDHeight);
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
        gameTime = System.currentTimeMillis() - mGameTimeStart;

        if (mWidth > 0 && mHeight > 0 && gameTime > mRespawnCooldownHolder) {
            mObjectSpawnedCounter++;
            mRespawnCooldownHolder += 1000;

            //API lvl 21
            int spawnX = ThreadLocalRandom.current().nextInt(mPowerUpMaxSize, mWidth - mPowerUpMaxSize);
            int spawnY = ThreadLocalRandom.current().nextInt(mPowerUpMaxSize, mHeight - mPowerUpMaxSize - mHUDHeight);

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
            gameObjectsList.add(newObject);
        }

        //loop through and see if you touch them or not
        for (int index = 0; index < gameObjectsList.size(); index++) {
            PowerUp object = gameObjectsList.get(index);
            if (object.isUsable() && GeometryUtil.areCirclesOverlapping(mPlayer.getOriginX(), mPlayer.getOriginY(), mPlayer.getPlayerRadius(), object.getOriginX(), object.getOriginY(), object.getSize())) {
                gameObjectsList.remove(object);
                performHapticFeedback(KEYBOARD_TAP);
                switch (object.getPower()) {
                    case GROW:
                        countPowerGrow++;
                        mPlayer.triggerRapidGrowth();
                        break;
                    case SPEED_UP:
                        countPowerSpeed++;
                        mPlayer.triggerSpeedUp();
                        break;
                    case INVERT_CONTROL:
                        countPowerInvert++;
                        mPlayer.triggerInvertControl();
                        break;
                    case SHRINK:
                        countPowerShrink++;
                        mPlayer.triggerRapidShrink();
                        break;
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
        if (mPlayer.getPlayerRadius() + (mScreenWidthOnePercent / 50.0f) >= mMaxScreenSize - (mHUDHeight / 2)) {
            endGame();
        } else {
            mPlayer.live();
        }
    }

    private void endGame() {

        String scoreText = "SCORE : ";

        if (gameTime > mHighScore && mPrefs != null) {
            mPrefs.edit().putFloat(StringConstants.PREF_HIGHSCORE, gameTime).apply();
            scoreText = "NEW HIGHSCORE : ";
        }

        if (mSurfaceHolder.getSurface().isValid()) {
            //Get start time for FPS calculation
            mTimeStartCurrentFrame = System.nanoTime() / 1000000;
            //First we lock the area of memory we will be drawing to
            mScreenCanvas = mSurfaceHolder.lockCanvas();

            mScreenCanvas.drawColor(ColorUtil.COLOR_DARK_OVERLAY);
            mHUDTextPaint.setTextSize(mHUDHeight * 3);
            mScreenCanvas.drawText("GAME OVER", mWidth / 2, (mHeight / 2) - (mHUDHeight * 3), mHUDTextPaint);
            mHUDTextPaint.setTextSize(mHUDHeight * 2);
            mScreenCanvas.drawText(scoreText + gameTime / 1000, mWidth / 2, mHeight / 2, mHUDTextPaint);
            mHUDTextPaint.setTextSize(mHUDHeight);
            mScreenCanvas.drawText(GameHints.getRandomGameHint(), mWidth / 2, mHeight / 2 + (mHUDHeight * 2), mHUDTextPaint);
            mScreenCanvas.drawText("tap to restart", mWidth / 2, mHeight / 2 + (mHUDHeight * 4), mHUDTextPaint);
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

            //HUD
            mScreenCanvas.drawLine(0, 0, mWidth, 0, mHUDPaint);
            mScreenCanvas.drawLine(0, mHeight - mHUDHeight, mWidth, mHeight - mHUDHeight, mHUDPaint);
            mHUDPaint.setTextAlign(Paint.Align.LEFT);
            mScreenCanvas.drawText("SCORE : " + gameTime / 1000, mWidth / 2, mHeight - (mHUDHeight / 3), mHUDPaint);
            mScreenCanvas.drawText("HIGHSCORE : " + mHighScore / 1000, (int) (mWidth / 1.5), mHeight - (mHUDHeight / 3), mHUDPaint);
            mHUDPaint.setTextAlign(Paint.Align.CENTER);
            mPowerUpPaint.setColor(ColorUtil.COLOR_GROW);
            mScreenCanvas.drawCircle(mHUDHeight, mHeight - (mHUDHeight / 2), mHUDHeight / 3, mPowerUpPaint);
            mScreenCanvas.drawText("" + countPowerGrow, mHUDHeight * 2, mHeight - (mHUDHeight / 3), mHUDPaint);
            mPowerUpPaint.setColor(ColorUtil.COLOR_SHRINK);
            mScreenCanvas.drawCircle(mHUDHeight * 3, mHeight - (mHUDHeight / 2), mHUDHeight / 3, mPowerUpPaint);
            mScreenCanvas.drawText("" + countPowerShrink, mHUDHeight * 4, mHeight - (mHUDHeight / 3), mHUDPaint);
            mPowerUpPaint.setColor(ColorUtil.COLOR_SPEED_UP);
            mScreenCanvas.drawCircle(mHUDHeight * 5, mHeight - (mHUDHeight / 2), mHUDHeight / 3, mPowerUpPaint);
            mScreenCanvas.drawText("" + countPowerSpeed, mHUDHeight * 6, mHeight - (mHUDHeight / 3), mHUDPaint);
            mPowerUpPaint.setColor(ColorUtil.COLOR_INVERT_CONTROL);
            mScreenCanvas.drawCircle(mHUDHeight * 7, mHeight - (mHUDHeight / 2), mHUDHeight / 3, mPowerUpPaint);
            mScreenCanvas.drawText("" + countPowerInvert, mHUDHeight * 8, mHeight - (mHUDHeight / 3), mHUDPaint);

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

    public void start(Context context) {
        this.mContext = context;
        mPrefs = mContext.getSharedPreferences(StringConstants.APP_NAME, Context.MODE_PRIVATE);
        resume();
    }

    public void resume() {
        mGameTimeStart = System.currentTimeMillis();
        mHighScore = mPrefs.getFloat(StringConstants.PREF_HIGHSCORE, 0.0f);
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
            setupNewGame();
            resume();
        }
        return false;
    }
}
