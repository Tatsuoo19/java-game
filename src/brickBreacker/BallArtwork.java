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
            // Face-centered square from the supplied 1080 x 1920 portrait.
            int side = (int) (source.getWidth() * .65);
            int x = (int) (source.getWidth() * .285);
            int y = (int) (source.getHeight() * .335);
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
