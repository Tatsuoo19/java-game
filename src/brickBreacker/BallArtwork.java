package brickBreacker;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;

/**
 * Loads once per view; crops the supplied portrait without modifying the
 * source.
 */
public final class BallArtwork {
    static final String PATH = "assets/849977873_1441907308138616_6764262475377853956_n.jpg";

    private BallArtwork() {
    }

    public static BufferedImage load() {
        return load(PATH);
    }

    public static BufferedImage loadExpression(int lives) {
        String name = lives >= 3 ? "happy" : lives == 2 ? "sad" : "angry";
        String path = "assets/face-" + name + ".png";
        if (BallArtwork.class.getResource("/" + path) == null && !new File(path).isFile())
            return load();
        return load(path);
    }

    static BufferedImage load(String path) {
        try {
            BufferedImage source;
            try (InputStream resource = BallArtwork.class.getResourceAsStream("/" + path)) {
                source = resource != null ? ImageIO.read(resource) : ImageIO.read(new File(path));
            }
            if (source == null)
                throw new IOException("Unsupported image");
            BufferedImage result = new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = result.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.setClip(new Ellipse2D.Double(3, 3, 122, 122));
            // Zoom toward the expression, trimming extra hat and background.
            boolean variant = path.contains("face-");
            int side = variant ? (int) (Math.min(source.getWidth(), source.getHeight()) * .80)
                    : (int) (source.getWidth() * .60);
            int x = variant ? (source.getWidth() - side) / 2 : (int) (source.getWidth() * .31);
            int y = variant ? (int) (source.getHeight() * .18) : (int) (source.getHeight() * .36);
            side = Math.min(side, Math.min(source.getWidth() - x, source.getHeight() - y));
            g.drawImage(source, 3, 3, 125, 125, x, y, x + side, y + side, null);
            g.setClip(null);
            g.setColor(new Color(255, 225, 125));
            g.setStroke(new BasicStroke(4));
            g.drawOval(3, 3, 122, 122);
            g.dispose();
            return result;
        } catch (IOException | SecurityException e) {
            System.err.println("Ball photo unavailable; using drawn face: " + e.getMessage());
            if (path.contains("face-"))
                return load();
            return fallback();
        }
    }

    private static BufferedImage fallback() {
        BufferedImage image = new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setColor(new Color(255, 221, 126));
        g.fillOval(3, 3, 122, 122);
        g.setColor(new Color(36, 41, 56));
        g.fillOval(35, 45, 14, 20);
        g.fillOval(79, 45, 14, 20);
        g.setStroke(new BasicStroke(5));
        g.drawArc(42, 68, 42, 30, 180, 180);
        g.dispose();
        return image;
    }
}
