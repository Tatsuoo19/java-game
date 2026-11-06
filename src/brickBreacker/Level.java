package brickBreacker;

import java.awt.Color;

public enum Level {
    WINTER("Winter", new Color(20, 48, 75), new Color(180, 225, 250)),
    SPRING("Spring", new Color(40, 65, 55), new Color(255, 180, 210)),
    SUMMER("Summer", new Color(15, 90, 115), new Color(255, 215, 100)),
    AUTUMN("Autumn / Halloween", new Color(22, 14, 35), new Color(255, 157, 70)),
    BOSS("Pumpkin King", new Color(28, 12, 30), new Color(255, 130, 55));

    public final String title;
    public final Color background, accent;

    Level(String title, Color background, Color accent) {
        this.title = title;
        this.background = background;
        this.accent = accent;
    }

    public boolean isBoss() {
        return this == BOSS;
    }

    public boolean hasShy() {
        return this == SUMMER || this == AUTUMN || this == WINTER;
    }

    public boolean hasFighters() {
        return this == AUTUMN || this == WINTER;
    }
}
