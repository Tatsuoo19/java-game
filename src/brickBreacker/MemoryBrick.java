package brickBreacker;

public final class MemoryBrick extends GameBrick {
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
