package hr.from.bkoruznjak.accello.obstacles;

import android.graphics.EmbossMaskFilter;

import hr.from.bkoruznjak.accello.util.ColorUtil;

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
        EmbossMaskFilter embossfilter = new EmbossMaskFilter(new float[]{1, 1, 1}, 0.5f, 0.6f, 4f);
        mPaint.setAntiAlias(true);
        mPaint.setAlpha(ALPHA);
        mPaint.setMaskFilter(embossfilter);
    }

    public PowerUp(int originX, int originY, int maxSize, ObjectPower power) {
        super(originX, originY);
        mPower = power;
        MAX_SIZE = maxSize;
    }

    public void grow() {
        if (!isUsable && size >= pickupSizeTreshold) {
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
