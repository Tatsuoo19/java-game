package brickBreacker;

import java.awt.Color;

public enum PowerUpType {
    NONE("", Color.WHITE), WIDE("W", new Color(120, 230, 150)), SLOW("S", new Color(130, 190, 255)),
    RESCUE("R", new Color(255, 215, 90)), FREEZE("F", new Color(120, 245, 245));

    public final String label;
    public final Color color;

    PowerUpType(String label, Color color) {
        this.label = label;
        this.color = color;
    }
}
