package brickBreacker;

/** Explicit adventure order, independent of seasonal mechanics. */
public enum Stage {
    WINTER(Level.WINTER), SPRING(Level.SPRING), SUMMER(Level.SUMMER), AUTUMN(Level.AUTUMN), BOSS(Level.BOSS);

    public final Level theme;

    Stage(Level theme) {
        this.theme = theme;
    }

    @Override
    public String toString() {
        return (ordinal() + 1) + ". " + theme.title;
    }
}
