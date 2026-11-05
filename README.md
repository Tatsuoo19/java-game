# Brick Breaker - Four Seasons

A local single-player game built with object-oriented Java, Swing, Java2D, and Java Sound. No external libraries or downloaded assets are required. A JDK 8 or later is required.

## Run

From the project directory, in PowerShell or Git Bash:

```sh
javac -d out -sourcepath src src/brickBreacker/Brick.java
java -cp out brickBreacker.Brick
```

In VS Code, open `src/brickBreacker/Brick.java` and click Run above `main`, or use the existing **Run Brick Breaker** launch configuration. Keep the spelling `brickBreacker`: it is the package name.

## Controls and screens

- Choose Keyboard or Mouse on the welcome screen, then select Start Adventure.
- Keyboard: Left/Right arrows or A/D move the paddle; Space launches the ball.
- Mouse: move the pointer to steer; click the playfield or press Space to launch.
- P, Escape, or the Pause button pauses. Resume, restart, and return to the main menu from the pause menu.
- Switching away from the window pauses automatically. Click Resume when you return.
- Sound toggles the original synthesized background melody and game effects. The game remains playable if no audio device is available.
- A three-second memory preview appears before each season. Launch after the preview ends.

## Four seasonal levels

1. **Spring:** flowers, sleeping bricks, and numbered memory bricks.
2. **Summer:** beach colors and shy bricks that dodge nearby balls.
3. **Autumn / Halloween:** pumpkin, ghost, and bat details; fighting bricks; darkness illuminated by the ball. One-second invisible-ball events retain a faint locator and stop near the paddle.
4. **Winter:** a grayscale finale combining every special brick type.

Each stage has 24 bricks in three rows, making it easier to explore all four seasons. Clear all four to reach the victory screen. Start with three lives; missing the paddle costs one life unless a rescue shield saves the ball. Clearing a stage restores one life, up to three. Scores carry between stages. Restarting resets the adventure.

The ball speeds up smoothly by 10% every five seconds of launched play, reaching 2× speed after 50 seconds. The speed indicator above the bricks shows the current multiplier. Losing a life preserves the increase; each new season and restart reset it. Pausing, memory previews, menus, and waiting to launch do not advance the speed timer. The slow-ball pickup applies its 0.65 factor on top of this increase (1.3× effective speed at the maximum).

Sleeping bricks require two hits. Destroy numbered memory bricks in order 1-2-3-4 for a 100-point bonus; a wrong order only cancels that stage's bonus. Every destroyed brick awards 10 points. Fighting bricks fire at most three projectiles at once; hits slow the paddle for two seconds without taking a life.

Catch **W** for a wider paddle or **S** for a slower ball. Each lasts eight seconds; another pickup refreshes the duration. Losing a life or changing seasons clears all temporary effects, projectiles, and pickups. Pausing freezes gameplay and audio.

Catch **R** for a one-use rescue shield or **F** to freeze enemies for five seconds. Freeze stops shy bricks, fighting-brick firing, and enemy projectiles; your ball, paddle, and beneficial pickups keep moving. Each season guarantees one of each pickup in the bottom row. Shields do not stack; freeze retains the longer remaining duration.

Destroying a brick earns 10 Season Power charge, capped at 100. Press **E** or click the **Season Power** button when ready, after launching the ball:

| Season | Power |
| --- | --- |
| Spring | Second Bloom restores one life, or grants a rescue shield at full lives. |
| Summer | Sunburst pierces bricks for six seconds. Sleeping bricks still need two separate contacts. |
| Autumn | Lantern lights the field, prevents invisibility, clears enemy shots, and prevents firing for eight seconds. |
| Winter | Deep Freeze freezes enemies and doubles brick points for eight seconds. Memory bonuses remain 100 points. |

Activation consumes the meter. Charge and effects reset on life loss, level changes, restart, or return to menu. Pausing freezes timers. Clearing all four seasons with every memory bonus and no scoring power gives 1,360 points.

## OOP structure

- `Brick`: creates the Swing window and handles its lifecycle.
- `BrickBreaker`: Swing input, menus, and Java2D rendering.
- `GameEffects`, `SeasonAbility`: encapsulated effect state and four polymorphic seasonal abilities.
- `PowerUpType`: named pickup types, labels, and colors.
- `BallArtwork`: loads the supplied photo, crops it into a circle, and provides a drawn fallback.
- `GameModel`: independent game simulation, screen state, score, lives, collisions, and timed effects.
- `Ball`, `Paddle`, `GameBrick`: game entities with their own state and operations.
- `Level`, `LevelFactory`: season definitions and deterministic layouts, replacing the old integer-only MapGenerator.
- `GameAudio`: asynchronous music and effect generation with Java Sound.

The 32-pixel ball uses the portrait in `assets/849977873_1441907308138616_6764262475377853956_n.jpg`. Keep the assets folder with the project and run from the project directory. A packaged resource at the same path is also supported; missing images fall back to a drawn face. The original photo is unchanged. Other artwork and music are generated in Java. There is no saved progress or online leaderboard.

## Regression checks

```sh
javac -Xlint:all -d out -sourcepath src src/brickBreacker/Brick.java tests/brickBreacker/GameModelTest.java
java "-Djava.awt.headless=true" -cp out brickBreacker.GameModelTest
```

The checks cover scoring, special bricks, pause/resume, power-ups, collisions, lives, seasonal progression, and offscreen Swing rendering. They save preview images under the ignored `out/` directory. Interactive input and actual sound output should also be checked by running the game normally.

Original project credited to Sagar Mishra.

## OOP concepts for the code walkthrough

- **Encapsulation:** Ball, Paddle, GameBrick, GameModel, and falling objects keep their state private. Methods such as paddle movement, ball bouncing, and brick hits control changes. Rendering reads getters; collection getters return unmodifiable views (the contained game objects still have behavior).
- **Abstraction and inheritance:** GameBrick is an abstract base class containing shared bounds, health, and damage rules. NormalBrick, SleepingBrick, MemoryBrick, ShyBrick, and FightingBrick extend it.
- **Polymorphism:** GameModel updates a collection of GameBrick references. Each concrete brick supplies its own update behavior; MemoryBrick also overrides the destruction hook. SleepingBrick reuses the shared damage algorithm with two health points. Brick type identifiers are only used for appearance.
- **Interfaces:** BrickContext exposes the services special bricks need without exposing the model's fields. GameModel implements this interface; GameAudio also implements AutoCloseable for resource cleanup.
- **Composition:** GameModel owns its ball, paddle, bricks, and falling objects. BrickBreaker owns the model and audio service.
- **Exception handling:** Audio initialization catches device/access failures, reports the problem, releases resources, and allows silent gameplay. LevelFactory rejects invalid level indices.

Regression tests use test-only reflection to arrange precise collision and timer scenarios, avoiding production setters that would bypass game rules. The tests also verify shared brick behavior and protected collection access.
