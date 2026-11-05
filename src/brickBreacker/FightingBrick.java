package brickBreacker;

/** Requests shots; the game owns the shared projectile limit. */
public final class FightingBrick extends GameBrick {
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
