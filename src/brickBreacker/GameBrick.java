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

    // Static nested subclasses keep related brick behaviors together; each is still a distinct class.
    /** An ordinary, stationary brick. */
    public static final class NormalBrick extends GameBrick {
        public NormalBrick(double x, double y, PowerUpType power) {
            super(x, y, Kind.NORMAL, 0, power, 1);
        }

        @Override
        public void update(double dt, BrickContext context) {
            // Stationary bricks have no timed behavior.
        }
    }

    /** A brick that survives its first hit. */
    public static final class SleepingBrick extends GameBrick {
        public SleepingBrick(double x, double y, PowerUpType power) {
            super(x, y, Kind.SLEEPING, 0, power, 2);
        }

        @Override
        public void update(double dt, BrickContext context) {
            // Stationary bricks have no timed behavior.
        }
    }

    public static final class MemoryBrick extends GameBrick {
        public MemoryBrick(double x, double y, int number, PowerUpType power) {
            super(x, y, Kind.MEMORY, number, power, 1);
        }

        @Override
        protected void onDestroyed(BrickContext context) {
            context.memoryHit(getNumber());
        }

        @Override
        public void update(double dt, BrickContext context) {
            // The model coordinates the sequence shared by all memory bricks.
        }
    }

    /** Dodges nearby balls without crossing walls or another live brick. */
    public static final class ShyBrick extends GameBrick {
        private double cooldown;

        public ShyBrick(double x, double y, PowerUpType power) {
            super(x, y, Kind.SHY, 0, power, 1);
            cooldown = 2 + x / 250;
        }

        @Override
        public void update(double dt, BrickContext context) {
            if (!alive())
                return;
            cooldown -= dt;
            Ball ball = context.nearestBall(getX(), getY());
            if (cooldown > 0 || Math.abs(ball.getX() - getX()) >= 95 || Math.abs(ball.getY() - getY()) >= 90)
                return;
            double original = getX();
            moveTo(getX() + (ball.getX() < getX() + 37 ? 12 : -12));
            for (GameBrick other : context.getBricks()) {
                if (other != this && other.alive() && other.bounds().intersects(bounds())) {
                    moveTo(original);
                    break;
                }
            }
            cooldown = 2;
        }
    }

    /** Requests shots; the game owns the shared projectile limit. */
    public static final class FightingBrick extends GameBrick {
        private double cooldown;

        public FightingBrick(double x, double y, PowerUpType power) {
            super(x, y, Kind.FIGHTING, 0, power, 1);
            cooldown = 2 + x / 250;
        }

        @Override
        public void update(double dt, BrickContext context) {
            if (!alive())
                return;
            cooldown -= dt;
            if (cooldown <= 0) {
                context.requestProjectile(getX() + 41, getY() + 30);
                cooldown = 3.5;
            }
        }
    }
}
