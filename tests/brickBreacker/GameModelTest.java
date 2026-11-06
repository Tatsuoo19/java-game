package brickBreacker;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;

/** Dependency-free regression checks; run with the commands in README.md. */
public class GameModelTest {
    private static int checks;

    private static void check(boolean condition, String message) {
        checks++;
        if (!condition)
            throw new AssertionError(message);
    }

    private static GameModel active(int level) {
        GameModel game = new GameModel(new java.util.Random(1));
        game.start();
        set(game, "level", new int[] { 1, 2, 3, 0 }[level]);
        game.loadLevel();
        set(game, "preview", 0);
        game.launch();
        return game;
    }

    // Test-only scenario setup keeps production state private.
    private static void set(Object target, String name, Object value) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(name);
            field.setAccessible(true);
            if (field.getType() == double.class)
                field.setDouble(target, ((Number) value).doubleValue());
            else
                field.set(target, value);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    @SuppressWarnings("unchecked")
    private static void addFalling(GameModel model, GameModel.Falling item) {
        try {
            java.lang.reflect.Field field = GameModel.class.getDeclaredField("falling");
            field.setAccessible(true);
            ((java.util.List<GameModel.Falling>) field.get(model)).add(item);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    public static void main(String[] args) throws Exception {
        checkSpeedProgression();
        checkNewFeatures();
        checkSeasonPowers();
        checkOopContracts();
        GameModel game = active(0);
        check(game.getBricks().size() == 24, "24 bricks per season");
        for (int i = 0; i < 4; i++)
            game.hit(game.getBricks().get(i));
        check(game.getScore() == 140 && game.getMemoryNext() == 5, "Ordered memory bonus");
        game = active(0);
        game.hit(game.getBricks().get(1));
        check(!game.getMemoryValid() && game.getScore() == 10, "Wrong order only loses bonus");
        GameBrick sleeper = game.getBricks().get(8);
        game.hit(sleeper);
        check(sleeper.getHealth() == 1 && game.getScore() == 10, "Sleeping brick wakes first");
        game.hit(sleeper);
        check(!sleeper.alive() && game.getScore() == 20, "Awake brick breaks");

        set(game.getPaddle(), "wideTime", 8);
        set(game, "slowBallTime", 8);
        double x = game.getBall().getX();
        game.pause();
        game.update(1, 1, null);
        check(game.getBall().getX() == x && game.getPaddle().getWideTime() == 8
                && game.getSlowBallTime() == 8, "Pause freezes state");
        game.resume();
        game.update(.02, 1, null);
        check(game.getBall().getX() != x && game.getPaddle().getWideTime() < 8, "Resume advances state");
        game.getPaddle().moveTo(9999);
        check(game.getPaddle().getX() + game.getPaddle().width() <= 792, "Wide paddle stays inside walls");

        game = active(1);
        GameBrick shy = game.getBricks().get(9);
        double brickX = shy.getX();
        set(shy, "cooldown", 0);
        set(game.getBall(), "x", shy.getX());
        set(game.getBall(), "y", shy.getY() + 60);
        game.update(.008, 0, null);
        check(shy.getX() != brickX, "Shy brick dodges in a full row");
        for (GameBrick other : game.getBricks())
            if (other != shy)
                check(!other.bounds().intersects(shy.bounds()), "Shy brick avoids overlap");

        game = active(2);
        set(game.getBall(), "x", 400);
        set(game.getBall(), "y", 350);
        set(game, "glitchCountdown", 0);
        for (GameBrick b : game.getBricks())
            if (b instanceof ShyBrick || b instanceof FightingBrick)
                set(b, "cooldown", 0);
        game.update(.008, 0, null);
        check(game.getInvisibleTime() > 0, "Halloween invisibility starts");
        check(game.getFalling().stream().filter(f -> f.isEnemy()).count() == 2, "Two fighting bricks fire");
        set(game.getBall(), "y", 460);
        game.update(.008, 0, null);
        check(game.getInvisibleTime() == 0, "Ball visible near paddle");

        game = active(0);
        addFalling(game, new GameModel.Falling(game.getPaddle().getX() + 40, 547, PowerUpType.WIDE));
        addFalling(game, new GameModel.Falling(game.getPaddle().getX() + 40, 547, PowerUpType.SLOW));
        addFalling(game, new GameModel.Falling(game.getPaddle().getX() + 40, 547, PowerUpType.NONE));
        game.update(.008, 0, null);
        check(game.getPaddle().getWideTime() > 7 && game.getSlowBallTime() > 7
                && game.getPaddle().getSlowTime() > 1, "Pickups and projectile debuff");
        check(game.getLives() == 3, "Projectiles do not remove lives");
        game.getPaddle().update(9);
        check(game.getPaddle().width() == 130 && game.getPaddle().getSlowTime() == 0,
                "Paddle effects expire");
        set(game, "slowBallTime", .001);
        game.update(.008, 0, null);
        check(game.getSlowBallTime() == 0, "Slow ball expires");

        game = active(0);
        set(game.getBall(), "x", game.getPaddle().getX() + 120);
        set(game.getBall(), "y", 531);
        set(game.getBall(), "vy", 250);
        game.update(.008, 0, null);
        check(game.getBall().getVy() < 0, "Full drawn paddle has collision coverage");
        set(game.getBall(), "x", 7);
        set(game.getBall(), "vx", -100);
        game.update(.008, 0, null);
        check(game.getBall().getX() >= 8 && game.getBall().getVx() > 0, "Wall bounce");
        for (int life = 2; life >= 0; life--) {
            game.launch();
            set(game.getBall(), "y", 620);
            set(game.getBall(), "vy", 250);
            game.update(.008, 0, null);
            check(game.getLives() == life && !game.getBall().getLaunched() && game.getFalling().isEmpty(),
                    "Life loss and cleanup");
        }
        check(game.getState() == GameModel.State.GAME_OVER, "Game over at zero lives");
        game.start();
        check(game.getScore() == 0 && game.getLives() == 3 && game.getLevel() == 0,
                "Restart resets adventure");
        for (int level = 0; level < 4; level++) {
            set(game, "preview", 0);
            game.launch();
            for (GameBrick b : game.getBricks())
                while (b.alive())
                    game.hit(b);
            game.update(.008, 0, null);
            check(game.getState() == (GameModel.State.TRANSITION), "Season completion");
            check(game.getEffects().getCharge() == 0 && game.getEffects().getFreeze() == 0
                    && !game.getEffects().hasShield(), "Season completion resets powers");
            if (level < 4)
                game.nextLevel();
        }
        game.launch();
        game.getBoss().damage(100);
        game.update(.008, 0, null);
        check(game.getState() == GameModel.State.VICTORY, "Boss completes adventure");
        check(game.getScore() == 1360, "All-season score including ordered bonuses");

        SwingUtilities.invokeAndWait(() -> {
            BrickBreaker panel = new BrickBreaker();
            try {
                panel.setSize(800, 600);
                panel.doLayout();
                java.lang.reflect.Field field = BrickBreaker.class.getDeclaredField("game");
                field.setAccessible(true);
                GameModel model = (GameModel) field.get(panel);
                java.lang.reflect.Method refresh = BrickBreaker.class.getDeclaredMethod("refresh");
                refresh.setAccessible(true);
                for (int level = -1; level < 5; level++) {
                    if (level >= 0) {
                        set(model, "level", level);
                        model.loadLevel();
                        set(model, "preview", 0);
                        if (level == 2)
                            set(model, "elapsed", 50);
                    }
                    refresh.invoke(panel);
                    for (java.awt.Component child : panel.getComponents())
                        if (child instanceof java.awt.Container)
                            ((java.awt.Container) child).doLayout();
                    BufferedImage image = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
                    Graphics2D g = image.createGraphics();
                    panel.paint(g);
                    g.dispose();
                    ImageIO.write(image, "png", new File("out/preview-" + (level + 1) + ".png"));

                }
                for (GameModel.State state : GameModel.State.values()) {
                    set(model, "level", 2);
                    set(model, "state", state);
                    refresh.invoke(panel);
                    for (java.awt.Component child : panel.getComponents())
                        if (child instanceof java.awt.Container)
                            ((java.awt.Container) child).doLayout();
                    BufferedImage image = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
                    Graphics2D g = image.createGraphics();
                    panel.paint(g);
                    g.dispose();
                    if (state == GameModel.State.SELECTING)
                        ImageIO.write(image, "png", new File("out/preview-selection.png"));
                }
                check(true, "All screens render headlessly");
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                panel.close();
            }
        });
        System.out.println("Passed " + checks + " checks.");
    }

    private static void checkSpeedProgression() {
        GameModel game = active(0);
        // Keep the ball in clear space while measuring the game's own active timer.
        set(game.getBall(), "x", 300);
        set(game.getBall(), "y", 350);
        set(game.getBall(), "vx", 0);
        set(game.getBall(), "vy", 0);
        double[] times = { 0, 5, 25, 50, 90 };
        double[] speeds = { 1, 1.1, 1.5, 2, 2 };
        double previous = 0;
        for (int i = 0; i < times.length; i++) {
            game.update(times[i] - previous, 0, null);
            check(Math.abs(game.getBallSpeedMultiplier() - speeds[i]) < 1e-9,
                    "Active speed multiplier at " + times[i] + " seconds");
            previous = times[i];
        }
        double elapsed = game.getElapsed();
        game.pause();
        game.update(10, 0, null);
        check(game.getElapsed() == elapsed, "Pause freezes speed progression");
        game.resume();
        set(game.getBall(), "y", 620);
        set(game.getBall(), "vy", 250);
        game.update(.008, 0, null);
        check(game.getLives() == 2 && game.getBallSpeedMultiplier() == 2, "Life loss retains season speed");
        elapsed = game.getElapsed();
        game.update(10, 0, null);
        check(game.getElapsed() == elapsed, "Waiting to relaunch freezes speed timer");
        game.launch();
        check(game.getBallSpeedMultiplier() == 2, "Relaunch retains speed");
        for (GameBrick brick : game.getBricks()) {
            while (brick.alive())
                game.hit(brick);
        }
        game.update(.008, 0, null);
        game.nextLevel();
        check(game.getLevel() == 2 && game.getBallSpeedMultiplier() == 1, "New season resets speed");
        game.update(1, 0, null);
        check(game.getPreview() > 0 && game.getElapsed() == 0, "Memory preview does not build speed");
        set(game, "elapsed", 30);
        game.start();
        check(game.getBallSpeedMultiplier() == 1, "Restart resets speed");
        game.returnToMenu();
        game.update(10, 0, null);
        check(game.getElapsed() == 0, "Menu does not build speed");

        game = active(0);
        Ball ball = game.getBall();
        set(ball, "x", 300);
        set(ball, "y", 350);
        double vx = ball.getVx(), vy = ball.getVy();
        set(game, "elapsed", 50);
        game.update(.008, 0, null);
        check(Math.abs(ball.getX() - (300 + vx * .008 * 2)) < 1e-9
                && Math.abs(ball.getY() - (350 + vy * .008 * 2)) < 1e-9,
                "Maximum speed scales movement on both axes");
        check(ball.getVx() == vx && ball.getVy() == vy, "Speed ramp does not compound base velocity");
        set(ball, "x", 300);
        set(ball, "y", 350);
        set(game, "slowBallTime", 8);
        game.update(.008, 0, null);
        check(Math.abs(ball.getX() - (300 + vx * .008 * 1.3)) < 1e-9
                && Math.abs(ball.getY() - (350 + vy * .008 * 1.3)) < 1e-9,
                "Slow pickup combines with maximum speed");
        set(game, "slowBallTime", .001);
        double x = ball.getX();
        game.update(.008, 0, null);
        check(Math.abs(ball.getX() - (x + vx * .008 * 2)) < 1e-9,
                "Expired slow effect restores accelerated speed");

        for (int level = 0; level < 4; level++) {
            game = active(level);
            set(game, "elapsed", 50);
            ball = game.getBall();
            double baseSpeed = Math.hypot(ball.getVx(), ball.getVy());
            set(ball, "x", 9);
            set(ball, "y", 350);
            set(ball, "vx", -ball.getVx());
            game.update(.008, 0, null);
            check(ball.getX() >= 8 && ball.getVx() > 0, "Maximum-speed wall collision");
            set(ball, "x", game.getPaddle().getX() + 40);
            set(ball, "y", Paddle.Y - Ball.SIZE - 1);
            set(ball, "vy", Math.abs(ball.getVy()));
            game.update(.008, 0, null);
            check(ball.getVy() < 0 && Math.abs(Math.hypot(ball.getVx(), ball.getVy()) - baseSpeed) < 1e-9,
                    "Maximum-speed paddle bounce preserves base speed");
            GameBrick brick = game.getBricks().get(16);
            set(ball, "x", brick.getX() + 10);
            set(ball, "y", brick.getY() + GameBrick.HEIGHT + 1);
            set(ball, "vx", 0);
            set(ball, "vy", -baseSpeed);
            game.update(.008, 0, null);
            check(!brick.alive() && ball.getVy() > 0, "Maximum-speed brick collision");
        }
    }

    private static void pickup(GameModel game, PowerUpType type) {
        addFalling(game, new GameModel.Falling(game.getPaddle().getX() + 40, 547, type));
        game.update(.008, 0, null);
    }

    private static void checkNewFeatures() throws Exception {
        for (Stage stage : Stage.values()) {
            GameModel g = new GameModel(new java.util.Random(4));
            g.startAt(stage);
            check(g.season() == stage.theme && g.getLives() == 3 && g.getScore() == 0, "Direct stage entry");
            check(g.getBricks().size() == (stage == Stage.BOSS ? 0 : 24), "Boss has no bricks");
            g.start();
            check(g.season() == stage.theme, "Restart preserves selected start");
        }
        GameModel g = active(0);
        pickup(g, PowerUpType.MULTIBALL);
        check(g.getBalls().size() == 3, "Multiball creates three balls");
        double speed = Math.hypot(g.getBall().getVx(), g.getBall().getVy());
        for (Ball b : g.getBalls())
            check(Math.abs(Math.hypot(b.getVx(), b.getVy()) - speed) < 1e-9, "Split preserves base speed");
        pickup(g, PowerUpType.MULTIBALL);
        check(g.getBalls().size() == 3, "Multiball capped");
        GameBrick shared = g.getBricks().get(20);
        for (Ball b : g.getBalls()) {
            set(b, "x", shared.getX() + 10);
            set(b, "y", shared.getY() + GameBrick.HEIGHT + 1);
            set(b, "vx", 0);
            set(b, "vy", -250);
        }
        int beforeScore = g.getScore();
        g.update(.008, 0, null);
        check(!shared.alive() && g.getScore() == beforeScore + 10,
                "Simultaneous balls score destruction once");
        for (Ball b : g.getBalls()) {
            set(b, "y", 350);
            set(b, "vy", -250);
        }
        set(g.getBalls().get(0), "y", 620);
        g.update(.008, 0, null);
        check(g.getLives() == 3 && g.getBalls().size() == 2, "Losing one ball costs no life");
        pickup(g, PowerUpType.MULTIBALL);
        check(g.getBalls().size() == 3, "Pickup replenishes missing ball");
        for (Ball b : g.getBalls())
            set(b, "y", 620);
        g.update(.008, 0, null);
        check(g.getLives() == 2 && g.getBalls().size() == 1 && !g.getBall().getLaunched(),
                "Last missed ball costs one life");
        g.launch();
        pickup(g, PowerUpType.EXTRA_LIFE);
        check(g.getLives() == 3, "Extra life heals");
        int score = g.getScore();
        pickup(g, PowerUpType.EXTRA_LIFE);
        check(g.getLives() == 3 && g.getScore() == score + 50, "Full-life pickup scores instead");
        pickup(g, PowerUpType.MULTIBALL);
        g.getEffects().rescue();
        for (Ball b : g.getBalls()) {
            set(b, "x", 10);
            set(b, "y", 620);
            set(b, "vy", 250);
        }
        g.update(.008, 0, null);
        check(g.getBalls().size() == 1 && g.getLives() == 3 && !g.getEffects().hasShield(),
                "Shield saves only one ball");
        double elapsed = g.getElapsed();
        g.update(.008, 0, null);
        check(Math.abs(g.getElapsed() - elapsed - .008) < 1e-9, "Timer advances once per step");
        g.showLevelSelection();
        elapsed = g.getElapsed();
        g.update(1, 0, null);
        check(g.getElapsed() == elapsed, "Selection freezes simulation");

        // Controlled random sources cover both outcomes and all six pickup choices.
        for (int index = 0; index < 6; index++) {
            final int choice = index;
            GameModel drops = new GameModel(new java.util.Random(0) {
                private static final long serialVersionUID = 1;

                @Override
                public double nextDouble() {
                    return 0;
                }

                @Override
                public int nextInt(int bound) {
                    return choice;
                }
            });
            drops.startAt(Stage.SPRING);
            GameBrick sleeper = drops.getBricks().get(8);
            drops.hit(sleeper);
            check(drops.getFalling().isEmpty(), "No drop before destruction");
            drops.hit(sleeper);
            check(drops.getFalling().size() == 1, "One randomized drop on destruction");
            drops.hit(sleeper);
            check(drops.getFalling().size() == 1, "No repeated destruction drop");
        }
        GameModel none = new GameModel(new java.util.Random(0) {
            private static final long serialVersionUID = 1;

            @Override
            public double nextDouble() {
                return .9;
            }
        });
        none.start();
        none.hit(none.getBricks().get(4));
        check(none.getFalling().isEmpty(), "Failed drop roll produces no pickup");

        g = new GameModel();
        g.startAt(Stage.BOSS);
        g.setFiring(true);
        g.update(1, 0, null);
        check(g.getBoss().getHealth() == 100 && g.getLasers().isEmpty(), "Docked boss combat is idle");
        g.launch();
        g.setFiring(true);
        g.update(.008, 0, null);
        check(g.getLasers().size() == 1 && g.getLaser().getEnergy() < 3, "Boss laser fires");
        GameModel.Falling laserShot = g.getLasers().get(0);
        set(laserShot, "x", g.getBoss().getX() + 50);
        set(laserShot, "y", 218);
        g.update(.008, 0, null);
        check(g.getBoss().getHealth() == 99 && g.getLasers().isEmpty(),
                "Laser hit deals one damage and is consumed");
        double energy = g.getLaser().getEnergy();
        g.pause();
        g.update(1, 0, null);
        check(g.getLaser().getEnergy() == energy, "Pause freezes laser energy");
        g.resume();
        set(g.getBall(), "x", g.getBoss().getX() + 30);
        set(g.getBall(), "y", 216);
        set(g.getBall(), "vy", -250);
        g.update(.008, 0, null);
        check(g.getBoss().getHealth() == 94 && g.getBall().getVy() > 0, "Ball damages boss and bounces");
        int health = g.getBoss().getHealth();
        set(g.getBall(), "y", 620);
        g.update(.008, 0, null);
        check(g.getBoss().getHealth() == health && g.getLaser().getEnergy() == 3 && g.getLasers().isEmpty(),
                "Life loss preserves boss and resets weapons");
        g.launch();
        g.getBoss().update(2.5, g);
        check(g.getBoss().isWarning(), "Boss telegraphs attacks");
        g.getBoss().update(.6, g);
        check(g.getFalling().size() == 3, "Boss aimed volley");
        g.getBoss().damage(50);
        g.getBoss().update(3.1, g);
        check(g.getFalling().size() == 8 && g.getBoss().isEnraged(), "Boss fan and phase two");
        g.getBoss().damage(100);
        set(g.getBall(), "y", 620);
        g.update(.008, 0, null);
        check(g.getState() == GameModel.State.VICTORY, "Boss victory precedes final ball loss");
        LaserPaddle weapon = new LaserPaddle();
        int shots = 0;
        for (int i = 0; i < 500; i++)
            if (weapon.update(.008, true))
                shots++;
        check(shots <= 13 && weapon.getEnergy() == 0, "Laser duration and cadence bounded");
        weapon.update(1, false);
        check(weapon.getEnergy() == 0, "Recharge delay");
        weapon.update(2, false);
        check(Math.abs(weapon.getEnergy() - 1) < 1e-9, "Recharge rate");
        for (int lives = 1; lives <= 3; lives++) {
            BufferedImage face = BallArtwork.loadExpression(lives);
            check((face.getRGB(0, 0) >>> 24) == 0 && (face.getRGB(64, 64) >>> 24) > 0,
                    "Expression circular and visible");
        }
    }

    private static void charge(GameModel game) {
        for (int i = 0; i < 12; i++)
            game.getEffects().charge();
        check(game.getEffects().getCharge() == 100, "Charge capped at 100");
    }

    private static void checkSeasonPowers() {
        for (int level = 0; level < 4; level++) {
            java.util.List<GameBrick> layout = new LevelFactory().create(level);
            check(layout.size() == 24, "24 bricks in every season");
            check(layout.stream().filter(b -> b instanceof SleepingBrick).count() == 2, "Two sleepers");
            check(layout.stream().filter(b -> b instanceof ShyBrick)
                    .count() == (Level.values()[level].hasShy() ? 2 : 0), "Shy layout");
            check(layout.stream().filter(b -> b instanceof FightingBrick)
                    .count() == (Level.values()[level].hasFighters() ? 2 : 0), "Fighter layout");

        }
        GameModel game = active(0);
        game.hit(game.getBricks().get(4));
        check(game.getEffects().getCharge() == 10, "Destroyed brick charges meter");
        charge(game);
        game.pause();
        game.activateAbility();
        check(game.getEffects().getCharge() == 100, "Cannot activate while paused");
        game.resume();
        game.activateAbility();
        check(game.getEffects().hasShield() && game.getEffects().getCharge() == 0, "Spring full-life shield");
        set(game.getBall(), "x", 10);
        set(game.getBall(), "y", 560);
        set(game.getBall(), "vy", 250);
        game.update(.008, 0, null);
        check(game.getLives() == 3 && game.getBall().getVy() < 0 && !game.getEffects().hasShield(),
                "Shield saves once");
        set(game, "lives", 2);
        charge(game);
        game.activateAbility();
        check(game.getLives() == 3 && !game.getEffects().hasShield(), "Spring restores a life");
        addFalling(game, new GameModel.Falling(game.getPaddle().getX() + 40, 547, PowerUpType.RESCUE));
        game.update(.008, 0, null);
        check(game.getEffects().hasShield(), "Rescue pickup collected");

        game = active(1);
        charge(game);
        game.activateAbility();
        GameBrick sleeper = game.getBricks().get(8);
        set(game.getBall(), "x", sleeper.getX());
        set(game.getBall(), "y", sleeper.getY());
        set(game.getBall(), "vx", 0);
        set(game.getBall(), "vy", 0);
        game.update(.016, 0, null);
        check(sleeper.getHealth() == 1 && game.getEffects().getPiercing() > 5,
                "Piercing hits once per contact");
        set(game.getBall(), "y", 400);
        game.update(.008, 0, null);
        set(game.getBall(), "y", sleeper.getY());
        game.update(.008, 0, null);
        check(!sleeper.alive(), "Piercing can hit again after leaving");

        game = active(2);
        game.requestProjectile(200, 300);
        charge(game);
        game.activateAbility();
        game.requestProjectile(200, 300);
        check(game.getFalling().isEmpty() && game.getInvisibleTime() == 0
                && game.getEffects().getLantern() == 8, "Lantern clears and blocks projectiles");
        set(game, "glitchCountdown", 0);
        set(game.getBall(), "y", 350);
        game.update(.008, 0, null);
        check(game.getInvisibleTime() == 0, "Lantern prevents invisibility");

        game = active(3);
        game.requestProjectile(200, 300);
        GameModel.Falling shot = game.getFalling().get(0);
        charge(game);
        game.activateAbility();
        game.getEffects().freeze(5);
        check(game.getEffects().getFreeze() == 8, "Freeze keeps longer duration");
        game.hit(game.getBricks().get(4));
        check(game.getScore() == 20, "Winter doubles destruction score");
        for (int i = 0; i < 4; i++)
            game.hit(game.getBricks().get(i));
        check(game.getScore() == 200, "Memory bonus is not doubled");
        double freeze = game.getEffects().getFreeze();
        game.pause();
        game.update(1, 0, null);
        check(game.getEffects().getFreeze() == freeze, "Pause preserves ability timers");
        game.resume();
        GameBrick shy = game.getBricks().get(9);
        set(shy, "cooldown", 0);
        set(game.getBall(), "x", shy.getX());
        set(game.getBall(), "y", shy.getY() + 60);
        double shyX = shy.getX();
        game.update(.008, 0, null);
        check(shot.getY() == 300 && shy.getX() == shyX, "Freeze stops enemy movement");
        game.getEffects().update(10);
        check(game.getEffects().getFreeze() == 0 && game.getEffects().getDoubleScore() == 0,
                "Effects expire");
        addFalling(game, new GameModel.Falling(game.getPaddle().getX() + 40, 547, PowerUpType.FREEZE));
        game.update(.008, 0, null);
        check(game.getEffects().getFreeze() > 4, "Freeze pickup collected");
        game.returnToMenu();
        check(game.getEffects().getCharge() == 0 && game.getEffects().getFreeze() == 0,
                "Menu clears effects");
        game = active(1);
        charge(game);
        game.activateAbility();
        set(game.getBall(), "y", 620);
        game.update(.008, 0, null);
        check(game.getEffects().getPiercing() == 0 && game.getEffects().getCharge() == 0,
                "Life loss clears powers");
        charge(game);
        game.activateAbility();
        check(game.getEffects().getCharge() == 100, "Docked ball cannot activate");
        game.start();
        check(game.getEffects().getCharge() == 0, "Restart clears charge");

        BufferedImage photo = BallArtwork.load();
        BufferedImage fallback = BallArtwork.load("assets/missing-test-image.jpg");
        check(photo.getWidth() == 128 && (photo.getRGB(0, 0) >>> 24) == 0, "Photo has transparent corners");
        check(photo.getRGB(64, 64) != fallback.getRGB(64, 64), "Photo loaded rather than fallback");
        check((fallback.getRGB(64, 64) >>> 24) == 255, "Missing image has visible fallback");
        Ball ball = new Ball();
        set(ball, "x", 790);
        ball.constrainToWalls();
        check(ball.getX() + Ball.SIZE == 792 && ball.bounds().getWidth() == 32,
                "Enlarged ball respects walls");
    }

    private static void checkOopContracts() {
        GameModel context = new GameModel(new java.util.Random(1));
        context.start();
        GameBrick normal = new NormalBrick(44, 100, PowerUpType.NONE);
        GameBrick sleeping = new SleepingBrick(133, 100, PowerUpType.NONE);
        check(normal.hit(context), "Normal brick breaks through base API");
        check(!sleeping.hit(context) && sleeping.getHealth() == 1,
                "Sleeping brick survives through base API");
        check(sleeping.hit(context) && !sleeping.hit(context), "Destroyed brick cannot score another hit");
        GameBrick memory = new MemoryBrick(44, 100, 1, PowerUpType.NONE);
        context.hit(memory);
        check(context.getMemoryNext() == 2, "Memory override reports sequence hit");
        for (int i = 0; i < 5; i++) {
            GameBrick fighter = new FightingBrick(44 + i * 89, 200, PowerUpType.NONE);
            fighter.update(10, context);
        }
        check(context.getFalling().size() == 3, "Polymorphic fighters share projectile limit");
        try {
            context.getBricks().clear();
            throw new AssertionError("Brick collection must be read-only");
        } catch (UnsupportedOperationException expected) {
            check(context.getBricks().size() == 24, "Brick collection protected");
        }
        try {
            context.getFalling().clear();
            throw new AssertionError("Falling collection must be read-only");
        } catch (UnsupportedOperationException expected) {
            check(context.getFalling().size() == 3, "Falling collection protected");
        }
        try {
            new LevelFactory().create(-1);
            throw new AssertionError("Invalid level accepted");
        } catch (IllegalArgumentException expected) {
            check(true, "Invalid level rejected clearly");
        }
        context.returnToMenu();
        check(context.getState() == GameModel.State.WELCOME && context.getFalling().isEmpty(),
                "Returning to menu clears temporary effects");
    }
}
