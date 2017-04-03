package com.example.borna.accello.obstacles;

import android.graphics.Paint;

/**
 * Created by bkoruznjak on 29/03/2017.
 */

public class GameObject {

    private int originX;
    private int originY;
    private Paint mPaint;
    private float maxSize = 50;
    private int size;

    public GameObject(int originX, int originY) {
        this.originX = originX;
        this.originY = originY;
        this.size = 1;
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

    public Paint getmPaint() {
        return mPaint;
    }

    public void setmPaint(Paint mPaint) {
        this.mPaint = mPaint;
    }

    public float getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(float maxSize) {
        this.maxSize = maxSize;
    }

    public float getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void grow() {
        if (size < maxSize) {
            size++;
        }
    }

}
