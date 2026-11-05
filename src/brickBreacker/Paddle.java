package brickBreacker;

import java.awt.geom.Rectangle2D;

public class Paddle {
    public static final int Y = 550;
    private double x = 335, wideTime, slowTime;

    public int width() {
        return wideTime > 0 ? 180 : 130;
    }

    public void moveTo(double target) {
        x = Math.max(8, Math.min(792 - width(), target));
    }

    public Rectangle2D bounds() {
        return new Rectangle2D.Double(x, Y, width(), 14);
    }

    public void update(double dt) {
        wideTime = Math.max(0, wideTime - dt);
        slowTime = Math.max(0, slowTime - dt);
        moveTo(x);
    }

    public void clearEffects() {
        wideTime = 0;
        slowTime = 0;
    }

    public double getX() {
        return x;
    }

    public double getWideTime() {
        return wideTime;
    }

    public double getSlowTime() {
        return slowTime;
    }

    public void widen() {
        wideTime = 8;
        moveTo(x);
    }

    public void slow() {
        slowTime = 2;
    }

}
