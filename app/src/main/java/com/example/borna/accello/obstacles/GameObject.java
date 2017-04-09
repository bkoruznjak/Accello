package com.example.borna.accello.obstacles;

import android.graphics.Color;
import android.graphics.Paint;

/**
 * Created by bkoruznjak on 29/03/2017.
 */

public class GameObject {

    public static final float MAX_SIZE = 50;
    private ObjectPower mPower;
    private Paint mPaint;
    private int originX;
    private int originY;
    private float pickupSizeTreshold = 25;
    private int size;
    private boolean canInteract;


    public GameObject(int originX, int originY) {
        this.originX = originX;
        this.originY = originY;
        this.size = 1;
        this.mPower = ObjectPower.UNKNOWN;
        this.mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.GRAY);
    }

    public int getOriginX() {
        return originX;
    }

    public void setOriginX(int originX) {
        this.originX = originX;
    }

    public int getOriginY() {
        return originY;
    }

    public void setOriginY(int originY) {
        this.originY = originY;
    }

    public Paint getPaint() {
        return mPaint;
    }

    public void setPaint(Paint mPaint) {
        this.mPaint = mPaint;
    }

    public float getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public boolean canInteract() {
        return canInteract;
    }

    public ObjectPower getPower() {
        return mPower;
    }

    public void setPower(ObjectPower mPower) {
        this.mPower = mPower;
    }

    public void grow() {
        if (size == pickupSizeTreshold) {
            switch (mPower) {
                case UNKNOWN:
                    mPaint.setColor(Color.GRAY);
                    break;
                case GROW:
                    mPaint.setColor(Color.RED);
                    break;
                case SHRINK:
                    mPaint.setColor(Color.BLUE);
                    break;
                case SPEED_UP:
                    mPaint.setColor(Color.GREEN);
                    break;
                case SLOW_DOWN:
                    mPaint.setColor(Color.MAGENTA);
                    break;
            }

        } else if (!canInteract && size > pickupSizeTreshold) {
            canInteract = true;
        }

        if (size < MAX_SIZE) {
            size++;
        }
    }
}
