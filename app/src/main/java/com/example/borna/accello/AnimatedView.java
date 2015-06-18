package com.example.borna.accello;

/**
 * Created by Borna on 18.6.15.
 */

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import java.util.Arrays;

public class AnimatedView extends ImageView implements SensorEventListener {
    private Context mContext;
    int x = -1;
    int y = -1;
    private int xVelocity = 15;
    private int yVelocity = 15;
    private Handler h;
    private final int FRAME_RATE = 30;
    private final int BALL_SHADE_LENGTH = 30;
    private static final String TAG = "AnimatedView";
    private float[] gyroCoordinates = new float[3];

    public AnimatedView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        h = new Handler();

    }

    private Runnable r = new Runnable() {
        @Override
        public void run() {
            invalidate();
        }
    };

    protected void onDraw(Canvas c) {
        BitmapDrawable ball = (BitmapDrawable) mContext.getResources().getDrawable(R.drawable.ball);

        SensorManager manager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        if (manager.getSensorList(Sensor.TYPE_ACCELEROMETER).size() == 0) {
            Log.e(TAG, " No accelerometer installed");
        } else {
            Sensor accelerometer = manager.getSensorList(
                    Sensor.TYPE_ACCELEROMETER).get(0);
            if (!manager.registerListener(this, accelerometer,
                    SensorManager.SENSOR_DELAY_GAME)) {
                Log.e(TAG, " Couldn't register sensor listener");
            }
        }


        if (x < 0 && y < 0) {
            x = this.getWidth() / 2;
            y = this.getHeight() / 2;
        } else {
            x += gyroCoordinates[1]; //xVelocity
            y += gyroCoordinates[0]; //yVelocity
            if (x > this.getWidth() - ball.getBitmap().getWidth()) {
                x = this.getWidth() - ball.getBitmap().getWidth();
            } else if (x < 0) {
                x = 0;
            }

            if (y > this.getHeight() - ball.getBitmap().getHeight() + BALL_SHADE_LENGTH) {
                y = this.getHeight() - ball.getBitmap().getHeight() + BALL_SHADE_LENGTH;
            } else if (y < 0) {
                y = 0;
            }
        }
        c.drawBitmap(ball.getBitmap(), x, y, null);
        h.postDelayed(r, FRAME_RATE);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        gyroCoordinates[0] = (event.values[0]);
        gyroCoordinates[1] = (event.values[1]);
        gyroCoordinates[2] = (event.values[2]);

        Log.d(TAG, " " + Arrays.toString(gyroCoordinates));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}