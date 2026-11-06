package brickBreacker;

import java.awt.geom.Rectangle2D;

/** Boss owns health, movement and telegraphed alternating attacks. */
public final class Boss {
    private int health = 100, volley;
    private double x = 290, time, untilAttack = 3;

    public int getHealth() {
        return health;
    }

    public double getX() {
        return x;
    }

    public boolean isWarning() {
        return untilAttack <= .5;
    }

    public boolean isEnraged() {
        return health <= 50;
    }

    public Rectangle2D bounds() {
        return new Rectangle2D.Double(x, 115, 220, 100);
    }

    public void damage(int amount) {
        health = Math.max(0, health - amount);
    }

    public void update(double dt, GameModel game) {
        time += dt;
        x = 290 + Math.sin(time * .8) * 200;
        untilAttack -= dt;
        if (untilAttack > 0)
            return;
        untilAttack = isEnraged() ? 2 : 3;
        double speed = isEnraged() ? 230 : 180;
        boolean aimed = volley++ % 2 == 0;
        double base = aimed
                ? Math.atan2(Paddle.Y - 215,
                        game.getPaddle().getX() + game.getPaddle().width() / 2.0 - (x + 110))
                : Math.PI / 2;
        int count = aimed ? 3 : 5;
        for (int i = 0; i < count; i++) {
            double angle = base + (i - (count - 1) / 2.0) * .20;
            game.addBossShot(x + 110, 215, Math.cos(angle) * speed, Math.sin(angle) * speed);
        }
    }
}
