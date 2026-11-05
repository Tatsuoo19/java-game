package brickBreacker;

import java.awt.Color;

public enum Level {
    SPRING("Spring", new Color(25, 64, 54), new Color(159, 230, 164)),
    SUMMER("Summer", new Color(21, 73, 100), new Color(255, 211, 100)),
    AUTUMN("Autumn / Halloween", new Color(22, 14, 35), new Color(255, 157, 70)),
    WINTER("Winter", new Color(30, 30, 30), new Color(220, 220, 220));

    public final String title;
    public final Color background, accent;

    Level(String title, Color background, Color accent) {
        this.title = title;
        this.background = background;
        this.accent = accent;
    }
}
