package brickBreacker;

import javax.swing.JPanel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.Timer;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.net.URL;

public class BrickBreaker extends JPanel implements KeyListener, ActionListener {
    private boolean play = false;
    private int score = 0;

    private int totalBricks = 50;

    private Timer timer;
    private int delay = 8;

    private int playerX = 310;

    private int ballposX = 370;
    private int ballposY = 525;
    private int ballXdir = -1;
    private int ballYdir = -2;

    private MapGenerator map;

    private Image backgroundImage; // Variable to hold the background image

    public BrickBreaker() {
        map = new MapGenerator(5,10);
        addKeyListener(this);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
        timer = new Timer(delay, this);
        timer.start();

        // Load the background image
        setBackground(Color.BLACK);
        backgroundImage = loadBackground();
    }

    private Image loadBackground() {
        URL resource = getClass().getResource("image.jpg");
        try {
            if (resource != null) {
                return ImageIO.read(resource);
            }
            File file = new File("image.jpg");
            if (file.isFile()) {
                return ImageIO.read(file);
            }
        } catch (IOException e) {
            System.err.println("Could not load background image: " + e.getMessage());
        }
        return null; // The game can still run with a plain background.
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g = g.create();
        // Draw the background image
        g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);

        // Draw the bricks
        map.draw((Graphics2D) g);

        // Scores
        g.setColor(Color.cyan);
        g.setFont(new Font("Times New Roman", Font.BOLD, 30));
        g.drawString("Score: " + score, 630, 30);

        // Borders
        g.setColor(Color.yellow);
        g.fillRect(0, 0, 3, getHeight());
        g.fillRect(0, 0, getWidth(), 3);
        g.fillRect(getWidth() - 3, 0, 3, getHeight());

        // The paddle
        g.setColor(Color.green);
        g.fillRect(playerX, 550, 150, 10);

        // The ball
        g.setColor(Color.red);
        g.fillOval(ballposX, ballposY, 25, 25);

        // Winning and game over messages
        if (totalBricks <= 0) {
            play = false;
            ballXdir = 0;
            ballYdir = 0;
            g.setColor(Color.red);
            g.setFont(new Font("Times New Roman", Font.BOLD, 30));
            g.drawString("Congrats You Won", 250, 300);

            g.setFont(new Font("Times New Roman", Font.BOLD, 20));
            g.drawString("Press Enter To Restart", 200, 350);
        }
        if (ballposY > 570) {
            play = false;
            ballXdir = 0;
            ballYdir = 0;
            g.setColor(Color.red);
            g.setFont(new Font("Times New Roman", Font.BOLD, 30));
            g.drawString("Game Over, score: " + score, 190, 300);

            g.setFont(new Font("Times New Roman", Font.BOLD, 20));
            g.drawString("Press Enter To Restart", 240, 350);
        }

        g.dispose();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        timer.start();

        if (play) {
            // Ball and paddle collision
            if (new Rectangle(ballposX, ballposY, 25, 25).intersects(new Rectangle(playerX, 550, 120, 8))) {
                ballYdir = -ballYdir;
            }

            // Ball and bricks collision
            A: for (int i = 0; i < map.map.length; i++) {
                for (int j = 0; j < map.map[0].length; j++) {
                    if (map.map[i][j] > 0) {
                        int brickX = j * map.brickWidth + 80;
                        int brickY = i * map.brickHeight + 50;
                        Rectangle rect = new Rectangle(brickX, brickY, map.brickWidth, map.brickHeight);
                        Rectangle ballRect = new Rectangle(ballposX, ballposY, 25, 25);

                        if (ballRect.intersects(rect)) {
                            map.setBrickValue(0, i, j);
                            totalBricks--;
                            score += 10;

                            // Ball collision logic
                            if (ballposX + 25 <= rect.x || ballposX >= rect.x + rect.width) {
                                ballXdir = -ballXdir; // Change direction on horizontal collision
                            } else {
                                ballYdir = -ballYdir; // Change direction on vertical collision
                            }

                            break A;
                        }
                    }
                }
            }

            // Move the ball
            ballposX += ballXdir;
            ballposY += ballYdir;

            // Wall collision
            if (ballposX < 0) {
                ballXdir = -ballXdir;
            }
            if (ballposY < 0) {
                ballYdir = -ballYdir;
            }
            if (ballposX > 740) {
                ballXdir = -ballXdir;
            }
        }

        repaint();
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            if (playerX >= 600) {
                playerX = 630; // Prevent going out of bounds
            } else {
                moveRight();
            }
        }

        if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            if (playerX < 10) {
                playerX = 10; // Prevent going out of bounds
            } else {
                moveLeft();
            }
        }

        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            if (!play) {
                play = true;
                resetGame();
                repaint();
            }
        }
    }

    private void resetGame() {
        ballposX = 120;
        ballposY = 350;
        ballXdir = -1;
        ballYdir = -2;
        playerX = 310;
        score = 0;
        totalBricks = 50; // Set total bricks for your game
        map = new MapGenerator(5, 10); // Re-initialize the map
    }

    public void moveRight() {
        play = true;
        playerX += 20; // Move right
    }

    public void moveLeft() {
        play = true;
        playerX -= 20; // Move left
    }
}
