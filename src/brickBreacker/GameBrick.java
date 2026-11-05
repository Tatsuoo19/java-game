package brickBreacker;

import java.awt.geom.Rectangle2D;

/** Common brick state and damage rules; subclasses supply special behavior. */
public abstract class GameBrick {
    public enum Kind {
        NORMAL, SLEEPING, MEMORY, SHY, FIGHTING
    }

    public static final int WIDTH = 74, HEIGHT = 28;
    private final Kind kind;
    private final int number;
    private final PowerUpType power;
    private double x;
    private final double y;
    private int health;

    protected GameBrick(double x, double y, Kind kind, int number, PowerUpType power, int health) {
        this.x = x;
        this.y = y;
        this.kind = kind;
        this.number = number;
        this.power = power;
        this.health = health;
    }

    public final double getX() {
        return x;
    }

    public final double getY() {
        return y;
    }

    public final Kind getKind() {
        return kind;
    }

    public final int getNumber() {
        return number;
    }

    public final PowerUpType getPower() {
        return power;
    }

    public final int getHealth() {
        return health;
    }

    public final boolean alive() {
        return health > 0;
    }

    public final Rectangle2D bounds() {
        return new Rectangle2D.Double(x, y, WIDTH, HEIGHT);
    }

    protected final void moveTo(double target) {
        x = Math.max(8, Math.min(710, target));
    }

    /** Returns true only on the hit that destroys this brick. */
    public final boolean hit(BrickContext context) {
        if (!alive())
            return false;
        health--;
        if (alive())
            return false;
        onDestroyed(context);
        return true;
    }

    protected void onDestroyed(BrickContext context) {
    }

    public abstract void update(double dt, BrickContext context);
}
