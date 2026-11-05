package brickBreacker;

import java.util.List;

/** The limited game services that special bricks need. */
public interface BrickContext {
    Ball getBall();

    List<GameBrick> getBricks();

    void memoryHit(int number);

    void requestProjectile(double x, double y);
}
