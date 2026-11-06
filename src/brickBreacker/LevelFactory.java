package brickBreacker;

import java.util.ArrayList;
import java.util.List;

public class LevelFactory {
    public List<GameBrick> create(int level) {
        if (level < 0 || level >= Level.values().length) {
            throw new IllegalArgumentException("Unknown level: " + level);
        }
        Level theme = Level.values()[level];
        List<GameBrick> bricks = new ArrayList<>();
        if (theme.isBoss())
            return bricks;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 8; col++) {
                double x = 44 + col * 89, y = 100 + row * 38;
                PowerUpType power = PowerUpType.NONE;
                GameBrick brick;
                if (row == 0 && col < 4)
                    brick = new MemoryBrick(x, y, col + 1, power);
                else if (row == 1 && (col == 0 || col == 4))
                    brick = new SleepingBrick(x, y, power);
                else if (theme.hasShy() && row == 1 && (col == 1 || col == 5))
                    brick = new ShyBrick(x, y, power);
                else if (theme.hasFighters() && row == 1 && (col == 3 || col == 7))
                    brick = new FightingBrick(x, y, power);
                else
                    brick = new NormalBrick(x, y, power);
                bricks.add(brick);
            }
        }
        return bricks;
    }
}
