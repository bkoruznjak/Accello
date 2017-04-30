package com.example.borna.accello.obstacles;

import com.example.borna.accello.util.ColorUtil;

/**
 * Created by bkoruznjak on 19/04/2017.
 */

public class PowerUp extends GameObject {

    private static final int ALPHA = 222;
    private final float MAX_SIZE;
    private boolean isUsable;
    private ObjectPower mPower;
    private float pickupSizeTreshold;

    public PowerUp(int originX, int originY, int maxSize) {
        super(originX, originY);
        mPower = ObjectPower.UNKNOWN;
        MAX_SIZE = maxSize;
        pickupSizeTreshold = maxSize;
        mPaint.setAntiAlias(true);
        mPaint.setAlpha(ALPHA);
    }

    public PowerUp(int originX, int originY, int maxSize, ObjectPower power) {
        super(originX, originY);
        mPower = power;
        MAX_SIZE = maxSize;
    }

    public void grow() {
        if (size == pickupSizeTreshold) {
            switch (mPower) {
                case UNKNOWN:
                    mPaint.setColor(ColorUtil.COLOR_PLAYER);
                    break;
                case GROW:
                    mPaint.setColor(ColorUtil.COLOR_GROW);
                    break;
                case SHRINK:
                    mPaint.setColor(ColorUtil.COLOR_SHRINK);
                    break;
                case SPEED_UP:
                    mPaint.setColor(ColorUtil.COLOR_SPEED_UP);
                    break;
                case INVERT_CONTROL:
                    mPaint.setColor(ColorUtil.COLOR_INVERT_CONTROL);
                    break;
            }

        } else if (!isUsable && size > pickupSizeTreshold) {
            isUsable = true;
        }

        if (size < MAX_SIZE) {
            size++;
        }
    }

    public ObjectPower getPower() {
        return mPower;
    }

    public void setPower(ObjectPower mPower) {
        this.mPower = mPower;
    }

    public float getPickupSizeTreshold() {
        return pickupSizeTreshold;
    }

    public void setPickupSizeTreshold(float pickupSizeTreshold) {
        this.pickupSizeTreshold = pickupSizeTreshold;
    }

    public boolean isUsable() {
        return isUsable;
    }

    public void setUsable(boolean usable) {
        isUsable = usable;
    }

    public float getMaxSize() {
        return MAX_SIZE;
    }


}
