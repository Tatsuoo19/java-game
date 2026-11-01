package brickBreacker;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class MapGenerator {
    public int map[][];
    public int brickWidth;
    public int brickHeight;

    // Example texture for the bricks (optional)
    private BufferedImage brickTexture;

    public MapGenerator(int row, int col) {
        map = new int[row][col];
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[0].length; j++) {
                map[i][j] = 1;
            }
        }

        brickWidth = 540 / col;
        brickHeight = 150 / row;

        // Load or create a brick texture if desired (optional)
        // brickTexture = loadTexture("/resources/brickTexture.png");
    }

    public void draw(Graphics2D g) {
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[0].length; j++) {
                if (map[i][j] > 0) {
                    // Set brick position
                    int brickX = j * brickWidth + 80;
                    int brickY = i * brickHeight + 50;

                    // Create a gradient effect for the brick
                    GradientPaint gradient = new GradientPaint(brickX, brickY, Color.BLUE, brickX + brickWidth, brickY + brickHeight, Color.CYAN);
                    g.setPaint(gradient);
                    g.fillRect(brickX, brickY, brickWidth, brickHeight);

                    // Optionally, draw a texture on the brick
                    if (brickTexture != null) {
                        TexturePaint texture = new TexturePaint(brickTexture, new Rectangle(brickX, brickY, brickWidth, brickHeight));
                        g.setPaint(texture);
                        g.fillRect(brickX, brickY, brickWidth, brickHeight);
                    }

                    // Draw brick border
                    g.setStroke(new BasicStroke(3));
                    g.setColor(Color.BLACK);
                    g.drawRect(brickX, brickY, brickWidth, brickHeight);
                }
            }
        }
    }

    public void setBrickValue(int value, int row, int col) {
        map[row][col] = value;
    }

    // Method to load a texture (optional)
    private BufferedImage loadTexture(String path) {
        try {
            return ImageIO.read(getClass().getResource(path));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}

