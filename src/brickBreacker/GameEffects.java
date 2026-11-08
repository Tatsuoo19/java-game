package brickBreacker;

public final class GameEffects {
    private int charge;
    private boolean shield;
    private double freeze, piercing, lantern, doubleScore;

    public int getCharge() {
        return charge;
    }

    public boolean hasShield() {
        return shield;
    }

    public double getFreeze() {
        return freeze;
    }

    public double getPiercing() {
        return piercing;
    }

    public double getLantern() {
        return lantern;
    }

    public double getDoubleScore() {
        return doubleScore;
    }

    void charge() {
        charge = Math.min(100, charge + 10);
    }

    boolean consumeCharge() {
        if (charge < 100)
            return false;
        charge = 0;
        return true;
    }

    void rescue() {
        shield = true;
    }

    boolean consumeShield() {
        if (!shield)
            return false;
        shield = false;
        return true;
    }

    void freeze(double seconds) {
        freeze = Math.max(freeze, seconds);
    }

    void sunburst() {
        piercing = 6;
    }

    void lantern() {
        lantern = 8;
    }

    void winter() {
        freeze(8);
        doubleScore = 8;
    }

    void update(double dt) {
        freeze = Math.max(0, freeze - dt);
        piercing = Math.max(0, piercing - dt);
        lantern = Math.max(0, lantern - dt);
        doubleScore = Math.max(0, doubleScore - dt);
    }

    void clear() {
        charge = 0;
        shield = false;
        freeze = piercing = lantern = doubleScore = 0;
    }
}
