package hr.from.bkoruznjak.accello.obstacles;

import android.graphics.Color;
import android.graphics.Paint;

/**
 * Created by bkoruznjak on 29/03/2017.
 */

public class GameObject {

    protected Paint mPaint;
    protected int size;
    private int originX;
    private int originY;

    public GameObject(int originX, int originY) {
        this.originX = originX;
        this.originY = originY;
        this.size = 1;
        this.mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAntiAlias(false);
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

    public void move(int xMove, int yMove) {
        this.originX += xMove;
        this.originY += yMove;
    }

}
