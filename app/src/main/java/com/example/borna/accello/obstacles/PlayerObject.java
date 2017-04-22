package com.example.borna.accello.obstacles;

/**
 * Created by bkoruznjak on 22/04/2017.
 */

public class PlayerObject extends GameObject {

    private static final int CHANGE_MULTIPLIER = 2;
    private float growthCoefficient;
    private float mPlayerObjectRadius;
    private float fastGrowthCoefficient;
    private float minimalRadius;

    public PlayerObject(int originX, int originY, float initialRadius, float growthCoefficient) {
        super(originX, originY);
        this.growthCoefficient = growthCoefficient;
        this.fastGrowthCoefficient = growthCoefficient * CHANGE_MULTIPLIER;
        this.mPlayerObjectRadius = initialRadius;
        this.minimalRadius = initialRadius;
    }

    public void growNormal() {
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
}
