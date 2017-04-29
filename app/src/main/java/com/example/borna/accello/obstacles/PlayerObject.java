package com.example.borna.accello.obstacles;

/**
 * Created by bkoruznjak on 22/04/2017.
 */

public class PlayerObject extends GameObject {

    private static final int CHANGE_MULTIPLIER = 2;
    private static final int POWERUP_DURATION_IN_MILLIS = 1000;
    private final float PLAYER_MIN_RADIUS;

    private float mPlayerObjectRadius;


    private long mPowerUpTriggerStart;
    private long mPowerUpTriggerHolder;

    private int growthCoefficient;
    private int fastGrowthCoefficient;
    private int widthBoundary;
    private int heightBoundary;

    private boolean isGrowingRapidly = false;
    private boolean isShrinkingRadidly = false;
    private boolean isShrinkingNormally = false;
    private boolean isFast = false;
    private boolean areControlsInverted = false;

    //stuff for re-init;
    private int restartOriginX;
    private int restartOriginY;
    private float restartRadius;


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
        this.PLAYER_MIN_RADIUS = initialRadius / 2;
    }

    public void resetPlayer() {
        super.move(restartOriginX, restartOriginY);
        mPlayerObjectRadius = restartRadius;
    }

    private void resetPowerupTriggers(ObjectPower objectPower) {
        isGrowingRapidly = false;
        isShrinkingRadidly = false;
        isShrinkingNormally = false;
        isFast = false;
        areControlsInverted = false;
        switch (objectPower) {
            case GROW:
                isGrowingRapidly = true;
                break;
            case SHRINK:
                isShrinkingRadidly = true;
                break;
            case SPEED_UP:
                isShrinkingNormally = true;
                isShrinkingRadidly = true;
                isFast = true;
                break;
            case INVERT_CONTROL:
                isShrinkingNormally = true;
                areControlsInverted = true;
                break;
            case RESET:
                isShrinkingNormally = false;
                isShrinkingRadidly = false;
                isFast = false;
                areControlsInverted = false;
                break;
        }
    }

    public void live() {
        mPowerUpTriggerHolder = System.currentTimeMillis();
        if (mPowerUpTriggerHolder - mPowerUpTriggerStart > POWERUP_DURATION_IN_MILLIS) {
            resetPowerupTriggers(ObjectPower.RESET);
        }

        if (isGrowingRapidly) {
            growFast();
        } else if (isShrinkingRadidly) {
            shrinkFast();
        } else if (isShrinkingNormally) {
            shrinkNormal();
        } else {
            growNormal();
        }
    }

    public void triggerRapidGrowth() {
        resetPowerupTriggers(ObjectPower.GROW);
        mPowerUpTriggerStart = System.currentTimeMillis();
    }

    public void triggerRapidShrink() {
        resetPowerupTriggers(ObjectPower.SHRINK);
        mPowerUpTriggerStart = System.currentTimeMillis();
    }

    public void triggerSpeedUp() {
        resetPowerupTriggers(ObjectPower.SPEED_UP);
        mPowerUpTriggerStart = System.currentTimeMillis();
    }

    public void triggerInvertControl() {
        resetPowerupTriggers(ObjectPower.INVERT_CONTROL);
        mPowerUpTriggerStart = System.currentTimeMillis();
    }

    private void grow(int growthCoefficient) {
        int x = super.getOriginX();
        int y = super.getOriginY();

        if (x + mPlayerObjectRadius + growthCoefficient > widthBoundary) {
            move(-growthCoefficient, 0);
        }

        if (x - mPlayerObjectRadius - growthCoefficient < 0) {
            move(growthCoefficient, 0);
        }

        if (y + mPlayerObjectRadius + growthCoefficient > heightBoundary) {
            move(0, -growthCoefficient);
        }

        if (y - mPlayerObjectRadius - growthCoefficient < 0) {
            move(0, growthCoefficient);
        }

        mPlayerObjectRadius += growthCoefficient;
    }

    private void shrink(int shrinkCoefficient) {
        if (mPlayerObjectRadius - shrinkCoefficient > PLAYER_MIN_RADIUS) {
            mPlayerObjectRadius -= shrinkCoefficient;
        }
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

        if (isFast) {
            speedX *= CHANGE_MULTIPLIER;
            speedY *= CHANGE_MULTIPLIER;
            if (x + mPlayerObjectRadius + speedX < widthBoundary && x - mPlayerObjectRadius + speedX > 0) {
                move((int) speedX, 0);
            }

            if (y + mPlayerObjectRadius + speedY < heightBoundary && y - mPlayerObjectRadius + speedY > 0) {
                move(0, (int) speedY);
            }
        } else if (areControlsInverted) {
            if (x + mPlayerObjectRadius + speedY < widthBoundary && x - mPlayerObjectRadius + speedY > 0) {
                move((int) speedY, 0);
            }

            if (y + mPlayerObjectRadius + speedX < heightBoundary && y - mPlayerObjectRadius + speedX > 0) {
                move(0, (int) speedX);
            }
        } else {
            if (x + mPlayerObjectRadius + speedX < widthBoundary && x - mPlayerObjectRadius + speedX > 0) {
                move((int) speedX, 0);
            }

            if (y + mPlayerObjectRadius + speedY < heightBoundary && y - mPlayerObjectRadius + speedY > 0) {
                move(0, (int) speedY);
            }
        }
    }
}
