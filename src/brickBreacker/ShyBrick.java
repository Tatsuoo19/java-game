package brickBreacker;

/** Dodges nearby balls without crossing walls or another live brick. */
public final class ShyBrick extends GameBrick {
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
