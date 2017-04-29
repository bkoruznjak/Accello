package com.example.borna.accello.obstacles;

import android.util.Log;

/**
 * Created by bkoruznjak on 22/04/2017.
 */

public class PlayerObject extends GameObject {

    private static final int CHANGE_MULTIPLIER = 2;
    private float mPlayerObjectRadius;
    private float fastGrowthCoefficient;
    private int growthCoefficient;
    private int widthBoundary;
    private int heightBoundary;

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
        this.fastGrowthCoefficient = growthCoefficient * CHANGE_MULTIPLIER;
        this.mPlayerObjectRadius = initialRadius;
    }

    public void growNormal() {

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

    public void shrinkNormal() {
        mPlayerObjectRadius -= growthCoefficient;
    }

    public void growFast() {
        mPlayerObjectRadius += fastGrowthCoefficient;
    }

    public void shrinkFast() {
        mPlayerObjectRadius -= fastGrowthCoefficient;
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
