package brickBreacker;

/** A brick that survives its first hit. */
public final class SleepingBrick extends GameBrick {
    public SleepingBrick(double x, double y, PowerUpType power) {
        super(x, y, Kind.SLEEPING, 0, power, 2);
    }

    @Override
    public void update(double dt, BrickContext context) {
        // Stationary bricks have no timed behavior.
    }
}
