package com.example.borna.accello;

/**
 * Created by bkoruznjak on 12/03/2017.
 */

public class Circle {

    private int mCenterX;
    private int mCenterY;
    private float mRadius;

    public Circle(int mCenterX, int mCenterY, float mRadius) {
        this.mCenterX = mCenterX;
        this.mCenterY = mCenterY;
        this.mRadius = mRadius;
    }

    public int getCenterX() {
        return mCenterX;
    }

    public void setCenterX(int mCenterX) {
        this.mCenterX = mCenterX;
    }

    public int getCenterY() {
        return mCenterY;
    }

    public void setCenterY(int mCenterY) {
        this.mCenterY = mCenterY;
    }

    public float getRadius() {
        return mRadius;
    }

    public void setRadius(float mRadius) {
        this.mRadius = mRadius;
    }

    public void move(int xMove, int yMove) {
        this.mCenterX += xMove;
        this.mCenterY += yMove;
    }
}
