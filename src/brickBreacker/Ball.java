package brickBreacker;

import java.awt.geom.Rectangle2D;

public class Ball {
    public static final int SIZE = 48;
    private double x, y, vx, vy;
    private boolean launched;

    public Rectangle2D bounds() {
        return new Rectangle2D.Double(x, y, SIZE, SIZE);
    }

    public void dock(Paddle p) {
        launched = false;
        x = p.getX() + p.width() / 2 - SIZE / 2;
        y = Paddle.Y - SIZE - 2;
    }

    public void launch(int level) {
        if (!launched) {
            launched = true;
            vx = 125 + level * 15;
            vy = -245 - level * 20;
        }
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getVx() {
        return vx;
    }

    public double getVy() {
        return vy;
    }

    public boolean getLaunched() {
        return launched;
    }

    public Ball split(double angle) {
        Ball copy = new Ball();
        copy.x = x;
        copy.y = y;
        copy.launched = true;
        copy.vx = vx * Math.cos(angle) - vy * Math.sin(angle);
        copy.vy = vx * Math.sin(angle) + vy * Math.cos(angle);
        return copy;
    }

    public void advance(double dt) {
        x += vx * dt;
        y += vy * dt;
    }

    public void bounceHorizontal(double contactX) {
        x = contactX;
        vx = -vx;
    }

    public void bounceVertical(double contactY) {
        y = contactY;
        vy = -vy;
    }

    public void bounceOff(Paddle paddle) {
        y = Paddle.Y - SIZE;
        double offset = (x + SIZE / 2.0 - paddle.getX() - paddle.width() / 2.0) / (paddle.width() / 2.0);
        offset = Math.max(-1, Math.min(1, offset));
        double speed = Math.hypot(vx, vy);
        vx = offset * speed * .8;
        vy = -Math.sqrt(speed * speed - vx * vx);
    }

    public void constrainToWalls() {
        if (x < 8) {
            x = 8;
            vx = Math.abs(vx);
        }
        if (x > 792 - SIZE) {
            x = 792 - SIZE;
            vx = -Math.abs(vx);
        }
        if (y < 66) {
            y = 66;
            vy = Math.abs(vy);
        }
    }

}
