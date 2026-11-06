package brickBreacker;

/** Bounded laser energy with delayed recharge and a fixed firing cadence. */
public final class LaserPaddle {
    private double energy = 3, cooldown, idle;

    public double getEnergy() {
        return energy;
    }

    public void reset() {
        energy = 3;
        cooldown = idle = 0;
    }

    public boolean update(double dt, boolean held) {
        cooldown = Math.max(0, cooldown - dt);
        if (held) {
            idle = 0;
            if (energy <= 0)
                return false;
            energy = Math.max(0, energy - dt);
            if (cooldown <= 0) {
                cooldown = .25;
                return true;
            }
        } else {
            double before = idle;
            idle += dt;
            energy = Math.min(3, energy + .5 * (Math.max(0, idle - 1) - Math.max(0, before - 1)));
        }
        return false;
    }
}
