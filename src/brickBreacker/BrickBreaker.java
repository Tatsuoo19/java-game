package brickBreacker;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import javax.swing.*;

/** Swing view/controller; all gameplay lives in GameModel. */
@SuppressWarnings("serial")
public final class BrickBreaker extends JPanel {
    private final GameModel game = new GameModel();
    private final GameAudio audio = new GameAudio();
    private final JPanel menu = new JPanel();
    private final JButton pause = new JButton("Pause"), sound = new JButton("Sound: On");
    private final BufferedImage scene = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB),
            face = BallArtwork.load();
    private final BufferedImage[] faces = { BallArtwork.loadExpression(1), BallArtwork.loadExpression(2),
            BallArtwork.loadExpression(3) };
    private Stage selected = Stage.WINTER;
    private final JButton ability = new JButton("Season Power [E]");
    private final Timer timer;
    private GameModel.State shown;
    private boolean left, right, mouseMode, muted;
    private Double mouseX;
    private long last = System.nanoTime();

    public BrickBreaker() {
        setPreferredSize(new Dimension(800, 600));
        setLayout(null);
        setBackground(new Color(13, 20, 31));
        game.setSoundListener(audio::play);
        menu.setOpaque(false);
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setBounds(100, 250, 600, 195);
        add(menu);
        pause.setBounds(584, 16, 90, 32);
        pause.addActionListener(e -> togglePause());
        pause.setFocusable(false);
        add(pause);
        sound.setBounds(680, 16, 108, 32);
        sound.setFocusable(false);
        sound.addActionListener(e -> {
            muted = !muted;
            sound.setText(muted ? "Sound: Off" : "Sound: On");
            syncAudio();
        });
        add(sound);
        bind("LEFT", () -> left = true);
        bind("released LEFT", () -> left = false);
        bind("A", () -> left = true);
        bind("released A", () -> left = false);
        bind("RIGHT", () -> right = true);
        bind("released RIGHT", () -> right = false);
        bind("D", () -> right = true);
        bind("released D", () -> right = false);
        ability.setBounds(414, 8, 160, 28);
        ability.setFocusable(false);
        ability.addActionListener(e -> game.activateAbility());
        add(ability);
        bind("E", game::activateAbility);
        bind("X", () -> game.setFiring(true));
        bind("released X", () -> game.setFiring(false));
        ability.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (game.season().isBoss())
                    game.setFiring(true);
            }

            public void mouseReleased(MouseEvent e) {
                game.setFiring(false);
            }
        });
        bind("SPACE", game::launch);
        bind("P", this::togglePause);
        bind("ESCAPE", this::togglePause);
        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseMoved(MouseEvent e) {
                mouseX = (double) e.getX();
            }

            public void mouseDragged(MouseEvent e) {
                mouseMoved(e);
            }
        });
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (mouseMode)
                    game.launch();
            }
        });
        timer = new Timer(8, e -> {
            long now = System.nanoTime();
            double dt = Math.min(.05, (now - last) / 1e9);
            last = now;
            game.update(dt, mouseMode ? 0 : (right ? 1 : 0) - (left ? 1 : 0), mouseMode ? mouseX : null);
            refresh();
            repaint();
        });
        refresh();
        timer.start();
    }

    private void bind(String key, Runnable action) {
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(key), key);
        getActionMap().put(key, new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                action.run();
            }
        });
    }

    private void togglePause() {
        if (game.getState() == GameModel.State.PAUSED)
            game.resume();
        else
            game.pause();
        left = right = false;
        game.setFiring(false);
        refresh();
    }

    public void pauseForFocusLoss() {
        game.pause();
        left = right = false;
        refresh();
    }

    public void close() {
        timer.stop();
        audio.close();
    }

    private void syncAudio() {
        audio.sync(!muted, game.getState() != GameModel.State.PAUSED, game.season());
    }

    private void button(String text, Runnable action) {
        JButton b = new JButton(text);
        b.setFont(new Font("SansSerif", Font.BOLD, 15));
        b.setAlignmentX(.5f);
        b.setMaximumSize(new Dimension(290, 39));
        b.setFocusable(false);
        b.addActionListener(e -> {
            action.run();
            refresh();
            requestFocusInWindow();
        });
        menu.add(b);
        menu.add(Box.createVerticalStrut(9));
    }

    private void refresh() {
        ability.setVisible(game.getState() == GameModel.State.PLAYING);
        ability.setEnabled(game.season().isBoss()
                ? game.getState() == GameModel.State.PLAYING && game.getBall().getLaunched()
                : game.canActivateAbility());
        ability.setText(game.season().isBoss() ? "Fire Laser [X]"
                : game.canActivateAbility() ? "READY! [E]"
                        : "Season Power " + game.getEffects().getCharge() + "%");
        ability.setToolTipText(game.getAbilityName());
        if (shown == game.getState())
            return;
        shown = game.getState();
        left = right = false;
        mouseX = null;
        menu.removeAll();
        pause.setVisible(shown == GameModel.State.PLAYING);
        menu.setVisible(shown != GameModel.State.PLAYING);
        syncAudio();
        switch (shown) {
        case WELCOME:
            button("Start Adventure", () -> game.startAt(Stage.WINTER));
            button("Select Level", game::showLevelSelection);
            button(mouseMode ? "Controls: Mouse" : "Controls: Keyboard", () -> {
                mouseMode = !mouseMode;
                shown = null;
            });
            break;
        case SELECTING:
            JComboBox<Stage> choices = new JComboBox<>(Stage.values());
            choices.setSelectedItem(selected);
            choices.setMaximumSize(new Dimension(290, 35));
            choices.addActionListener(e -> {
                selected = (Stage) choices.getSelectedItem();
                repaint();
            });
            menu.add(choices);
            menu.add(Box.createVerticalStrut(9));
            button("Start Selected Level", () -> game.startAt(selected));
            button("Main Menu", game::returnToMenu);
            break;
        case PAUSED:
            button("Resume", game::resume);
            button("Restart Game", game::start);
            button("Select Level", game::showLevelSelection);
            button("Main Menu", game::returnToMenu);
            break;
        case TRANSITION:
            button("Continue to " + Level.values()[game.getLevel() + 1].title, game::nextLevel);
            button("Main Menu", game::returnToMenu);
            break;
        case GAME_OVER:
        case VICTORY:
            button("Play Again", game::start);
            button("Main Menu", game::returnToMenu);
            break;
        default:
            break;
        }
        menu.revalidate();
        menu.repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D s = scene.createGraphics();
        s.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        drawField(s);
        s.dispose();
        Graphics2D g = (Graphics2D) graphics.create();
        g.drawImage(scene, 0, 0, null);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        drawHud(g);
        if (game.getState() != GameModel.State.PLAYING)
            drawScreen(g);
        g.dispose();
    }

    private void drawField(Graphics2D g) {
        Level season = game.season();
        g.setColor(season.background);
        g.fillRect(0, 0, 800, 600);
        g.setColor(new Color(255, 255, 255, 25));
        for (int i = 0; i < 22; i++) {
            int x = (i * 137) % 800, y = 80 + (i * 67) % 450;
            if (game.season() == Level.SPRING) {
                g.setColor(new Color(255, 180, 210, 130));
                g.drawLine(x, y, x + 12, y + 25);
                for (int petal = 0; petal < 5; petal++)
                    g.fillOval(x + (int) (Math.cos(petal * 1.256) * 7),
                            y + (int) (Math.sin(petal * 1.256) * 7), 7, 7);
                g.drawLine(x + 6, y + 12, x + 6, y + 28);
            } else if (game.season() == Level.SUMMER) {
                g.setColor(new Color(90, 210, 220, 90));
                g.drawArc(x, y, 100, 30, 0, 180);
                g.drawLine(x + 20, y, x + 12, y - 30);
                g.drawArc(x - 10, y - 45, 55, 30, 0, 180);
            } else if (game.season() == Level.AUTUMN) {
                g.drawLine(x, y, x + 8, y + 10);
                g.drawLine(x + 8, y + 10, x + 16, y);
            } else {
                g.drawLine(x - 5, y, x + 5, y);
                g.drawLine(x, y - 5, x, y + 5);
            }
        }
        for (GameBrick b : game.getBricks())
            if (b.alive())
                drawBrick(g, b);
        for (GameModel.Falling f : game.getFalling()) {
            g.setColor(f.isEnemy() ? new Color(255, 100, 100) : f.getType().color);
            g.fillRoundRect((int) f.getX() - 9, (int) f.getY() - 9, 18, 18, 6, 6);
            g.setColor(Color.BLACK);
            g.setFont(new Font("SansSerif", Font.BOLD, 12));
            g.drawString(f.isEnemy() ? "!" : f.getType().label, (int) f.getX() - 5, (int) f.getY() + 5);
        }
        g.setColor(game.getPaddle().getSlowTime() > 0 ? new Color(255, 130, 120) : season.accent);
        g.fillRoundRect((int) game.getPaddle().getX(), Paddle.Y, game.getPaddle().width(), 14, 14, 14);
        BufferedImage expression = faces[Math.max(0, Math.min(2, game.getLives() - 1))];
        for (Ball ball : game.getBalls()) {
            if (game.getInvisibleTime() <= 0)
                g.drawImage(expression, (int) ball.getX(), (int) ball.getY(), Ball.SIZE, Ball.SIZE, null);
        }
        if (game.getBoss() != null)
            drawBoss(g);
        g.setColor(Color.CYAN);
        for (GameModel.Falling shot : game.getLasers())
            g.fillRect((int) shot.getX() - 2, (int) shot.getY() - 10, 4, 20);
        if (game.getEffects().getLantern() <= 0 && game.season() == Level.AUTUMN && game.getPreview() <= 0
                && game.getState() != GameModel.State.WELCOME) {
            Shape clip = g.getClip();
            g.clipRect(0, 64, 800, 536);
            g.setPaint(new RadialGradientPaint(
                    new Point2D.Double(game.getBall().getX() + Ball.SIZE / 2.0,
                            game.getBall().getY() + Ball.SIZE / 2.0),
                    165, new float[] { 0, .55f, 1 },
                    new Color[] { new Color(0, 0, 0, 0), new Color(0, 0, 0, 25), Color.BLACK }));
            g.fillRect(0, 64, 800, 536);
            g.setClip(clip);
            if (game.getInvisibleTime() > 0) {
                g.setColor(new Color(255, 255, 255, 100));
                g.drawOval((int) game.getBall().getX(), (int) game.getBall().getY(), Ball.SIZE, Ball.SIZE);
            }
        }
        if (game.getEffects().hasShield()) {
            g.setColor(PowerUpType.RESCUE.color);
            g.fillRoundRect(8, 582, 784, 5, 5, 5);
        }
        if (game.getState() == GameModel.State.PLAYING) {
            if (game.getPreview() > 0) {
                g.setColor(new Color(0, 0, 0, 190));
                g.fillRoundRect(140, 310, 520, 60, 18, 18);
                center(g, "Remember: 1  >  2  >  3  >  4", 337, 20, Color.WHITE);
                center(g, "Break them in order for +100 points", 358, 13, Color.LIGHT_GRAY);
            } else if (!game.getBall().getLaunched())
                center(g, mouseMode ? "Click or press SPACE to launch" : "Press SPACE to launch", 490, 18,
                        Color.WHITE);
        }
    }

    private void drawBrick(Graphics2D g, GameBrick b) {
        int x = (int) b.getX(), y = (int) b.getY();
        Color color = game.season().accent;
        if (b.getKind() == GameBrick.Kind.SLEEPING)
            color = b.getHealth() == 2 ? new Color(143, 153, 184) : new Color(255, 202, 113);
        if (b.getKind() == GameBrick.Kind.SHY)
            color = new Color(239, 153, 183);
        if (b.getKind() == GameBrick.Kind.FIGHTING)
            color = new Color(240, 117, 86);
        if (b.getKind() == GameBrick.Kind.MEMORY)
            color = new Color(160, 182, 248);
        g.setColor(color);
        g.fillRoundRect(x, y, 74, 28, game.season() == Level.AUTUMN ? 20 : 9, 12);
        g.setColor(new Color(255, 255, 255, 70));
        g.drawRoundRect(x, y, 74, 28, 9, 12);
        g.setColor(new Color(25, 29, 39));
        g.setFont(new Font("SansSerif", Font.BOLD, 13));
        if (b.getKind() == GameBrick.Kind.MEMORY) {
            g.drawString(Integer.toString(b.getNumber()), x + 37, y + 19);
            if (game.getPreview() > 0 && (int) ((3 - game.getPreview()) / .75) + 1 == b.getNumber()) {
                g.setColor(Color.WHITE);
                g.drawRoundRect(x - 2, y - 2, 78, 32, 10, 10);
            }
        } else if (b.getKind() == GameBrick.Kind.SLEEPING && b.getHealth() == 2)
            g.drawString("-  -  z", x + 24, y + 19);
        else {
            g.fillOval(x + 27, y + 8, 5, 6);
            g.fillOval(x + 49, y + 8, 5, 6);
            g.drawArc(x + 33, y + 14, 14, 7, 180, 180);
            if (b.getKind() == GameBrick.Kind.FIGHTING) {
                g.drawLine(x + 25, y + 4, x + 34, y + 8);
                g.drawLine(x + 47, y + 8, x + 56, y + 4);
            }
            if (b.getKind() == GameBrick.Kind.SHY) {
                g.setColor(new Color(190, 70, 110));
                g.fillOval(x + 20, y + 16, 8, 3);
                g.fillOval(x + 55, y + 16, 8, 3);
            }
        }
        if (game.season() == Level.AUTUMN) {
            if (b.getKind() == GameBrick.Kind.SLEEPING) {
                // A tiny ghost accompanies sleeping Halloween bricks.
                g.setColor(Color.WHITE);
                g.fillOval(x + 4, y + 5, 15, 16);
                g.fillRect(x + 4, y + 13, 15, 9);
                g.setColor(Color.BLACK);
                g.fillOval(x + 7, y + 10, 3, 4);
                g.fillOval(x + 13, y + 10, 3, 4);
            } else if (b.getKind() == GameBrick.Kind.SHY) {
                g.setColor(new Color(45, 25, 60));
                g.fillPolygon(new int[] { x + 4, x + 13, x + 22, x + 16, x + 10 },
                        new int[] { y + 6, y + 12, y + 6, y + 22, y + 22 }, 5);
            } else {
                g.setColor(new Color(50, 85, 60));
                g.fillRect(x + 35, y - 4, 4, 5);
            }
        }
        if (b.getPower() != PowerUpType.NONE) {
            g.setColor(Color.BLACK);
            g.setFont(new Font("SansSerif", Font.BOLD, 10));
            g.drawString(b.getPower().label, x + 5, y + 18);
        }
    }

    private void drawHud(Graphics2D g) {
        if (game.getState() == GameModel.State.PLAYING) {
            g.setColor(new Color(13, 20, 31, 220));
            g.fillRoundRect(12, 69, 190, 24, 8, 8);
            g.setColor(Color.WHITE);
            g.setFont(new Font("SansSerif", Font.BOLD, 12));
            double multiplier = game.getBallSpeedMultiplier();
            String speed = multiplier >= 2 ? "MAX 2.00\u00d7"
                    : String.format(java.util.Locale.ROOT, "%.2f\u00d7", multiplier);
            g.drawString("Ball speed: " + speed, 20, 86);
        }
        g.setColor(new Color(13, 20, 31));
        g.fillRect(0, 0, 800, 64);
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 15));
        g.drawString(game.season().title + "  |  Score " + game.getScore() + "  |  Lives " + game.getLives(),
                18, 27);
        g.setColor(new Color(173, 190, 207));
        g.setFont(new Font("SansSerif", Font.PLAIN, 12));
        String effects = (game.getPaddle().getWideTime() > 0
                ? "Wide " + (int) Math.ceil(game.getPaddle().getWideTime()) + "s  "
                : "")
                + (game.getSlowBallTime() > 0 ? "Slow ball " + (int) Math.ceil(game.getSlowBallTime()) + "s  "
                        : "")
                + (game.getPaddle().getSlowTime() > 0 ? "Paddle slowed  " : "");
        GameEffects powers = game.getEffects();
        String active = (powers.hasShield() ? "Shield  " : "")
                + (powers.getFreeze() > 0 ? "Freeze " + (int) Math.ceil(powers.getFreeze()) + "s  " : "")
                + (powers.getPiercing() > 0 ? "Pierce " + (int) Math.ceil(powers.getPiercing()) + "s  " : "")
                + (powers.getLantern() > 0 ? "Light " + (int) Math.ceil(powers.getLantern()) + "s  " : "")
                + (powers.getDoubleScore() > 0 ? "2x " + (int) Math.ceil(powers.getDoubleScore()) + "s" : "");
        g.drawString(game.season().isBoss()
                ? "Laser energy: "
                        + String.format(java.util.Locale.ROOT, "%.1f / 3", game.getLaser().getEnergy())
                : game.getAbilityName() + " | " + active, 414, 52);
        g.drawString(
                effects.isEmpty()
                        ? "Level " + (game.getLevel() + 1)
                                + (game.season().isBoss() ? " / 5 | Defeat the boss"
                                        : " / 5   |   Memory bonus: "
                                                + (!game.getMemoryValid() ? "missed"
                                                        : game.getMemoryNext() == 5 ? "earned"
                                                                : "next " + game.getMemoryNext()))
                        : effects,
                18, 48);
    }

    private void drawScreen(Graphics2D g) {
        g.setColor(new Color(9, 15, 25));
        g.fillRect(0, 64, 800, 536);
        String title = "", subtitle = "";
        switch (game.getState()) {
        case WELCOME:
            title = "BRICK BLOCK";
            subtitle = "FOUR SEASONS. ONE FINAL BATTLE.";
            break;
        case SELECTING:
            title = "Choose Your Stage";
            subtitle = (selected.ordinal() + 1) + ". " + selected.theme.title + " / Start with 3 lives";
            break;
        case PAUSED:
            title = "Take a breather";
            subtitle = "PAUSED / Your adventure will wait";
            break;
        case TRANSITION:
            title = game.season().title + " cleared!";
            subtitle = "Score " + game.getScore() + " / Lives " + game.getLives();
            break;
        case GAME_OVER:
            title = "Game Over";
            subtitle = "Final score: " + game.getScore() + " / Season: " + game.season().title;
            break;
        case VICTORY:
            title = "The Pumpkin King is defeated!";
            subtitle = "VICTORY / Final score: " + game.getScore();
            break;
        default:
            break;
        }
        g.drawImage(faces[Math.max(0, Math.min(2, game.getLives() - 1))], 372, 106, 56, 56, null);
        center(g, title, 210, 30, Color.WHITE);
        center(g, subtitle, 246, 14, new Color(166, 207, 185));
        if (game.getState() == GameModel.State.SELECTING) {
            g.setColor(selected.theme.accent);
            g.fillRoundRect(180, 455, 440, 50, 16, 16);
            center(g,
                    selected == Stage.BOSS ? "Boss: bouncing ball + laser paddle"
                            : selected.theme.title + " themed bricks and environment",
                    486, 16, selected.theme.background);
        }
        if (game.getState() == GameModel.State.WELCOME) {
            center(g, "Arrows / A-D or mouse to move. Space to launch. P / Esc to pause.", 435, 14,
                    Color.LIGHT_GRAY);
            center(g, "Random drops: W wide | S slow | R rescue | F freeze | M multiball | + life", 466, 13,
                    Color.LIGHT_GRAY);
            center(g, "Numbered bricks: break 1-2-3-4 | Catch W: wider paddle, S: slower ball", 491, 13,
                    Color.LIGHT_GRAY);
            center(g, "Catch R: rescue shield | F: freeze enemies | E: charged season power", 530, 13,
                    Color.LIGHT_GRAY);
            center(g, "Boss fight: hold X or Fire Laser. Faces change with remaining lives.", 555, 12,
                    Color.LIGHT_GRAY);
        }
    }

    private void drawBoss(Graphics2D g) {
        Boss boss = game.getBoss();
        int x = (int) boss.getX();
        g.setColor(new Color(255, 140, 40));
        g.fillOval(x, 115, 220, 100);
        g.setColor(new Color(130, 60, 15));
        g.drawOval(x + 50, 115, 120, 100);
        g.setColor(new Color(70, 120, 60));
        g.fillRect(x + 100, 100, 20, 18);
        g.setColor(Color.YELLOW);
        g.fillPolygon(new int[] { x + 35, x + 70, x + 65 }, new int[] { 140, 140, 162 }, 3);
        g.fillPolygon(new int[] { x + 150, x + 185, x + 155 }, new int[] { 140, 140, 162 }, 3);
        g.fillPolygon(new int[] { x + 50, x + 85, x + 110, x + 135, x + 170, x + 160, x + 60 },
                new int[] { 177, 185, 175, 185, 177, 199, 199 }, 7);
        g.setColor(Color.DARK_GRAY);
        g.fillRoundRect(250, 72, 300, 16, 8, 8);
        g.setColor(boss.isEnraged() ? Color.RED : Color.ORANGE);
        g.fillRoundRect(250, 72, boss.getHealth() * 3, 16, 8, 8);
        center(g, "PUMPKIN KING " + boss.getHealth() + " / 100", 100, 12, Color.WHITE);
        if (boss.isWarning())
            center(g, "INCOMING! Move your paddle", 250, 20, Color.ORANGE);
    }

    private void center(Graphics2D g, String text, int y, int size, Color color) {
        g.setFont(new Font("SansSerif", Font.BOLD, size));
        g.setColor(color);
        g.drawString(text, (800 - g.getFontMetrics().stringWidth(text)) / 2, y);
    }

}
