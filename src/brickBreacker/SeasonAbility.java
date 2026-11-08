package brickBreacker;


public interface SeasonAbility {
    String name();

    void activate(GameModel game, GameEffects effects);

    static SeasonAbility forLevel(Level level) {
        switch (level) {
        case SPRING:
            return new SecondBloom();
        case SUMMER:
            return new Sunburst();
        case AUTUMN:
            return new Lantern();
        case WINTER:
            return new DeepFreeze();
        default:
            throw new IllegalArgumentException("Unknown season");
        }
    }

    final class SecondBloom implements SeasonAbility {
        public String name() {
            return "Second Bloom";
        }

        public void activate(GameModel game, GameEffects effects) {
            game.restoreLifeOrShield();
        }
    }

    final class Sunburst implements SeasonAbility {
        public String name() {
            return "Sunburst";
        }

        public void activate(GameModel game, GameEffects effects) {
            effects.sunburst();
        }
    }

    final class Lantern implements SeasonAbility {
        public String name() {
            return "Lantern";
        }

        public void activate(GameModel game, GameEffects effects) {
            effects.lantern();
            game.clearEnemies();
        }
    }

    final class DeepFreeze implements SeasonAbility {
        public String name() {
            return "Deep Freeze";
        }

        public void activate(GameModel game, GameEffects effects) {
            effects.winter();
        }
    }
}
