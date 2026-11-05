package brickBreacker;

/** An ordinary, stationary brick. */
public final class NormalBrick extends GameBrick {
    public NormalBrick(double x, double y, PowerUpType power) {
        super(x, y, Kind.NORMAL, 0, power, 1);
    }

    @Override
    public void update(double dt, BrickContext context) {
        // Stationary bricks have no timed behavior.
    }
}
