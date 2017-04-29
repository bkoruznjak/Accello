package com.example.borna.accello.obstacles;

import android.util.Log;

/**
 * Created by bkoruznjak on 22/04/2017.
 */

public class PlayerObject extends GameObject {

    private static final int CHANGE_MULTIPLIER = 2;
    private static final int POWERUP_DURATION_IN_MILLIS = 1000;

    private float mPlayerObjectRadius;

    private long mPowerUpTriggerStart;
    private long mPowerUpTriggerHolder;

    private int growthCoefficient;
    private int fastGrowthCoefficient;
    private int widthBoundary;
    private int heightBoundary;

    private boolean isGrowingRapidly = false;
    private boolean isShrinkingNormaly = false;
    private boolean isShrinkingRadidly = false;


    public PlayerObject(int originX, int originY, float initialRadius, int growthCoefficient) {
        super(originX, originY);
        /*
        * this is a problem since coordinates are ints and growth should be float but for the sake
        * of logic and headache reduction we leave this as is
        * wont be ideal on all phones but it will work
        */
        if (growthCoefficient == 0) {
            this.growthCoefficient = 1;
        } else {
            this.growthCoefficient = growthCoefficient;
        }
        this.fastGrowthCoefficient = this.growthCoefficient * CHANGE_MULTIPLIER;
        this.mPlayerObjectRadius = initialRadius;
    }

    private void resetPowerupTriggers() {
        isGrowingRapidly = false;
        isShrinkingNormaly = false;
        isShrinkingRadidly = false;
    }

    public void live() {
        mPowerUpTriggerHolder = System.currentTimeMillis();
        if (mPowerUpTriggerHolder - mPowerUpTriggerStart > POWERUP_DURATION_IN_MILLIS) {
            resetPowerupTriggers();
        }

        if (isGrowingRapidly) {
            growFast();
        } else if (isShrinkingNormaly) {
            shrinkNormal();
        } else if (isShrinkingRadidly) {
            shrinkFast();
        } else {
            growNormal();
        }
    }

    public void triggerRapidGrowth() {
        resetPowerupTriggers();
        isGrowingRapidly = true;
        mPowerUpTriggerStart = System.currentTimeMillis();
    }

    public void triggerNormalShrink() {
        resetPowerupTriggers();
        isShrinkingNormaly = true;
        mPowerUpTriggerStart = System.currentTimeMillis();
    }

    private void grow(int growthCoefficient) {
        int x = super.getOriginX();
        int y = super.getOriginY();

        if (x + mPlayerObjectRadius + growthCoefficient > widthBoundary) {
            Log.d("bbb", "prvi");
            move(-growthCoefficient, 0);
        }

        if (x - mPlayerObjectRadius - growthCoefficient < 0) {
            Log.d("bbb", "drugi");
            move(growthCoefficient, 0);
        }

        if (y + mPlayerObjectRadius + growthCoefficient > heightBoundary) {
            Log.d("bbb", "treci");
            move(0, -growthCoefficient);
        }

        if (y - mPlayerObjectRadius - growthCoefficient < 0) {
            Log.d("bbb", "cetvrti");
            move(0, growthCoefficient);
        }

        mPlayerObjectRadius += growthCoefficient;
    }

    private void shrink(int shrinkCoefficient) {
        mPlayerObjectRadius -= shrinkCoefficient;
    }

    private void growNormal() {
        grow(growthCoefficient);
    }

    private void growFast() {
        grow(fastGrowthCoefficient);
    }

    private void shrinkNormal() {
        shrink(growthCoefficient);
    }

    private void shrinkFast() {
        shrink(fastGrowthCoefficient);
    }

    public float getPlayerRadius() {
        return mPlayerObjectRadius;
    }

    public int getWidthBoundary() {
        return widthBoundary;
    }

    public void setWidthBoundary(int widthBoundary) {
        this.widthBoundary = widthBoundary;
    }

    public int getHeightBoundary() {
        return heightBoundary;
    }

    public void setHeightBoundary(int heightBoundary) {
        this.heightBoundary = heightBoundary;
    }

    public void move(int speedX, int speedY) {
        super.move(speedX, speedY);
    }

    /**
     * Method shifts coordinates object only if the x is within the rectangle width and height
     * if they have been set.
     *
     * @param speedX
     * @param speedY
     */
    public void moveWithConstraints(float speedX, float speedY) {

        int x = super.getOriginX();
        int y = super.getOriginY();

        if (x + mPlayerObjectRadius + speedX < widthBoundary && x - mPlayerObjectRadius + speedX > 0) {
            move((int) speedX, 0);
        }

        if (y + mPlayerObjectRadius + speedY < heightBoundary && y - mPlayerObjectRadius + speedY > 0) {
            move(0, (int) speedY);
        }
    }
}
