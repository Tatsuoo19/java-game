package brickBreacker;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Objects;
import java.util.function.Consumer;

/** Independent simulation; rendering never mutates gameplay. */
public class GameModel implements BrickContext {
    public enum State {
        WELCOME, PLAYING, PAUSED, TRANSITION, GAME_OVER, VICTORY
    }

    public static class Falling {
        private double x, y;
        private final PowerUpType type;
        private final boolean enemy;

        Falling(double x, double y, PowerUpType type) {
            this.x = x;
            this.y = y;
            this.type = type;
            this.enemy = type == PowerUpType.NONE;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

        public PowerUpType getType() {
            return type;
        }

        public boolean isEnemy() {
            return enemy;
        }

        private void advance(double dt) {
            y += (enemy ? 165 : 115) * dt;
        }

        Rectangle2D bounds() {
            return new Rectangle2D.Double(x - 8, y - 8, 16, 16);
        }
    }

    private final GameEffects effects = new GameEffects();
    private final java.util.Set<GameBrick> contacts = new java.util.HashSet<>();

    public GameEffects getEffects() {
        return effects;
    }

    public String getAbilityName() {
        return SeasonAbility.forLevel(season()).name();
    }

    public boolean canActivateAbility() {
        return state == State.PLAYING && preview <= 0 && ball.getLaunched() && effects.getCharge() == 100;
    }

    public void activateAbility() {
        if (!canActivateAbility() || !effects.consumeCharge())
            return;
        SeasonAbility.forLevel(season()).activate(this, effects);
        sound.accept("bonus");
    }

    void restoreLifeOrShield() {
        if (lives < 3)
            lives++;
        else
            effects.rescue();
    }

    void clearEnemies() {
        falling.removeIf(Falling::isEnemy);
        invisibleTime = 0;
    }

    private void collect(PowerUpType type) {
        switch (type) {
        case WIDE:
            paddle.widen();
            break;
        case SLOW:
            slowBallTime = 8;
            break;
        case RESCUE:
            effects.rescue();
            break;
        case FREEZE:
            effects.freeze(5);
            break;
        default:
            break;
        }
    }

    private State state = State.WELCOME;
    private final Ball ball = new Ball();
    private final Paddle paddle = new Paddle();
    private List<GameBrick> bricks = new ArrayList<>();
    private final List<Falling> falling = new ArrayList<>();
    private int score, lives = 3, level, memoryNext = 1;
    private boolean memoryValid = true;
    private double preview, elapsed, invisibleTime, glitchCountdown = 15, slowBallTime;
    private Consumer<String> sound = event -> {
    };

    public Level season() {
        return Level.values()[level];
    }

    public void start() {
        score = 0;
        lives = 3;
        level = 0;
        loadLevel();
    }

    public void loadLevel() {
        bricks = new LevelFactory().create(level);
        memoryNext = 1;
        memoryValid = true;
        clearEffects();
        paddle.moveTo(335);
        ball.dock(paddle);
        preview = 3;
        elapsed = 0;
        state = State.PLAYING;
    }

    public void nextLevel() {
        if (state == State.TRANSITION) {
            level++;
            loadLevel();
        }
    }

    public void launch() {
        if (state == State.PLAYING && preview <= 0)
            ball.launch(level);
    }

    public void pause() {
        if (state == State.PLAYING)
            state = State.PAUSED;
    }

    public void resume() {
        if (state == State.PAUSED)
            state = State.PLAYING;
    }

    private void clearEffects() {
        falling.clear();
        effects.clear();
        contacts.clear();
        paddle.clearEffects();
        slowBallTime = invisibleTime = 0;
        glitchCountdown = 15;
    }

    public void hit(GameBrick b) {
        if (!b.alive())
            return;
        sound.accept("hit");
        if (!b.hit(this))
            return;
        score += effects.getDoubleScore() > 0 ? 20 : 10;
        effects.charge();
        if (b.getPower() != PowerUpType.NONE)
            falling.add(new Falling(b.getX() + 41, b.getY() + 14, b.getPower()));
    }

    @Override
    public void memoryHit(int number) {
        if (!memoryValid)
            return;
        if (number != memoryNext)
            memoryValid = false;
        else if (++memoryNext == 5) {
            score += 100;
            sound.accept("bonus");
        }
    }

    @Override
    public void requestProjectile(double x, double y) {
        if (effects.getFreeze() <= 0 && effects.getLantern() <= 0
                && falling.stream().filter(Falling::isEnemy).count() < 3) {
            falling.add(new Falling(x, y, PowerUpType.NONE));
        }
    }

    public void update(double dt, int direction, Double mouseX) {
        if (state != State.PLAYING)
            return;
        int steps = Math.max(1, (int) Math.ceil(dt / .008));
        for (int i = 0; i < steps && state == State.PLAYING; i++)
            step(dt / steps, direction, mouseX);
    }

    private void step(double dt, int direction, Double mouseX) {
        paddle.update(dt);
        double speed = paddle.getSlowTime() > 0 ? 270 : 540;
        double target = mouseX == null ? paddle.getX() + direction * speed * dt
                : mouseX - paddle.width() / 2.0;
        paddle.moveTo(paddle.getX() + Math.max(-speed * dt, Math.min(speed * dt, target - paddle.getX())));
        preview = Math.max(0, preview - dt);
        if (!ball.getLaunched()) {
            ball.dock(paddle);
            return;
        }
        effects.update(dt);
        elapsed += dt;
        slowBallTime = Math.max(0, slowBallTime - dt);
        invisibleTime = Math.max(0, invisibleTime - dt);
        if (level == 2 && effects.getLantern() <= 0) {
            glitchCountdown -= dt;
            if (glitchCountdown <= 0 && ball.getY() < 410) {
                invisibleTime = 1;
                glitchCountdown = 15;
            }
            if (ball.getY() >= 450)
                invisibleTime = 0;
        }
        for (GameBrick brick : bricks)
            if (brick.alive() && effects.getFreeze() <= 0)
                brick.update(dt, this);
        double oldX = ball.getX(), oldY = ball.getY(), factor = slowBallTime > 0 ? .65 : 1;
        ball.advance(dt * factor * getBallSpeedMultiplier());
        ball.constrainToWalls();
        if (ball.getVy() > 0 && ball.bounds().intersects(paddle.bounds())) {
            ball.bounceOff(paddle);
            sound.accept("bounce");
        }
        contacts.removeIf(b -> !b.alive() || !b.bounds().intersects(ball.bounds()));
        for (GameBrick b : bricks) {
            if (!b.alive() || !b.bounds().intersects(ball.bounds()) || contacts.contains(b))
                continue;
            contacts.add(b);
            hit(b);
            if (effects.getPiercing() > 0)
                continue;
            if (oldY + Ball.SIZE <= b.getY() || oldY >= b.getY() + GameBrick.HEIGHT)
                ball.bounceVertical(oldY);
            else
                ball.bounceHorizontal(oldX);
            break;
        }
        for (int i = falling.size() - 1; i >= 0; i--) {
            Falling f = falling.get(i);
            if (f.enemy && effects.getFreeze() > 0)
                continue;
            f.advance(dt);
            if (f.bounds().intersects(paddle.bounds())) {
                if (f.enemy)
                    paddle.slow();
                else
                    collect(f.type);
                sound.accept(f.enemy ? "hit" : "bonus");
                falling.remove(i);
            } else if (f.y > 610)
                falling.remove(i);
        }
        if (effects.hasShield() && ball.getVy() > 0 && ball.getY() + Ball.SIZE >= 582) {
            effects.consumeShield();
            ball.bounceVertical(582 - Ball.SIZE);
            sound.accept("bounce");
        }
        if (bricks.stream().noneMatch(GameBrick::alive)) {
            clearEffects();
            lives = Math.min(3, lives + 1);
            state = level == 3 ? State.VICTORY : State.TRANSITION;
            sound.accept("win");
        } else if (ball.getY() > 600) {
            lives--;
            clearEffects();
            ball.dock(paddle);
            sound.accept("lose");
            if (lives == 0)
                state = State.GAME_OVER;
        }
    }

    public State getState() {
        return state;
    }

    public Ball getBall() {
        return ball;
    }

    public Paddle getPaddle() {
        return paddle;
    }

    public int getScore() {
        return score;
    }

    public int getLives() {
        return lives;
    }

    public int getLevel() {
        return level;
    }

    public int getMemoryNext() {
        return memoryNext;
    }

    public boolean getMemoryValid() {
        return memoryValid;
    }

    public double getPreview() {
        return preview;
    }

    public double getElapsed() {
        return elapsed;
    }

    /** Speed builds during launched play and resets with the season's elapsed timer. */
    public double getBallSpeedMultiplier() {
        return 1 + Math.min(elapsed / 50.0, 1);
    }

    public double getInvisibleTime() {
        return invisibleTime;
    }

    public double getSlowBallTime() {
        return slowBallTime;
    }

    public List<GameBrick> getBricks() {
        return Collections.unmodifiableList(bricks);
    }

    public List<Falling> getFalling() {
        return Collections.unmodifiableList(falling);
    }

    public void setSoundListener(Consumer<String> listener) {
        sound = Objects.requireNonNull(listener);
    }

    public void returnToMenu() {
        clearEffects();
        ball.dock(paddle);
        state = State.WELCOME;
    }

}
