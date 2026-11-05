package brickBreacker;

import java.util.ArrayList;
import java.util.List;

public class LevelFactory {
    public List<GameBrick> create(int level) {
        if (level < 0 || level >= Level.values().length) {
            throw new IllegalArgumentException("Unknown level: " + level);
        }
        List<GameBrick> bricks = new ArrayList<>();
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 8; col++) {
                double x = 44 + col * 89, y = 100 + row * 38;
                PowerUpType power = row != 2 ? PowerUpType.NONE
                        : col == 0 ? PowerUpType.RESCUE
                                : col == 2 ? PowerUpType.WIDE
                                        : col == 5 ? PowerUpType.SLOW
                                                : col == 7 ? PowerUpType.FREEZE : PowerUpType.NONE;
                GameBrick brick;
                if (row == 0 && col < 4)
                    brick = new MemoryBrick(x, y, col + 1, power);
                else if (row == 1 && (col == 0 || col == 4))
                    brick = new SleepingBrick(x, y, power);
                else if (level >= 1 && row == 1 && (col == 1 || col == 5))
                    brick = new ShyBrick(x, y, power);
                else if (level >= 2 && row == 1 && (col == 3 || col == 7))
                    brick = new FightingBrick(x, y, power);
                else
                    brick = new NormalBrick(x, y, power);
                bricks.add(brick);
            }
        }
        return bricks;
    }
}
