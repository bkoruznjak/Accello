package com.example.borna.accello.obstacles;

/**
 * Created by bkoruznjak on 22/04/2017.
 */

public class PlayerObject extends GameObject {

    private static final int CHANGE_MULTIPLIER = 1;
    private float growthCoefficient;
    private float mPlayerObjectRadius;
    private float fastGrowthCoefficient;
    private float minimalRadius;
    private int widthBoundary;
    private int heightBoundary;

    public PlayerObject(int originX, int originY, float initialRadius, float growthCoefficient) {
        super(originX, originY);
        this.growthCoefficient = growthCoefficient;
        this.fastGrowthCoefficient = growthCoefficient * CHANGE_MULTIPLIER;
        this.mPlayerObjectRadius = initialRadius;
        this.minimalRadius = initialRadius;
    }

    public void growNormal() {

        int x = super.getOriginX();
        int y = super.getOriginY();

        if (x + mPlayerObjectRadius + growthCoefficient > widthBoundary) {
            move((int) -1, 0);
        }

        if (x - mPlayerObjectRadius - growthCoefficient < 0) {
            move((int) 1, 0);
        }

        if (y + mPlayerObjectRadius + growthCoefficient > heightBoundary) {
            move(0, (int) -1);
        }

        if (y - mPlayerObjectRadius - growthCoefficient < 0) {
            move(0, (int) 1);
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
//            Log.d("bbb","prvi:"+(xWithRadius + speedX));
            move((int) speedX, 0);
        }

        if (y + mPlayerObjectRadius + speedY < heightBoundary && y - mPlayerObjectRadius + speedY > 0) {
//            Log.d("bbb","drugi:"+(yWithRadius + speedY));
            move(0, (int) speedY);
        }
    }
}
