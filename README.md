# BRICK BLOCK

A Java 8+ desktop game built with Swing, Java2D and Java Sound. No database or game engine. All game progress is held in memory and is lost on exit.

## Run

From the project directory:

```sh
javac -d out -sourcepath src src/brickBreacker/Brick.java
java -cp out brickBreacker.Brick
```

Keep the package spelling `brickBreacker`. In VS Code you can also run `Brick.main`.

## Play and stage selection

Choose **Start Adventure** to begin in Winter, or **Select Level** to choose any stage and click **Start Selected Level**. Every selection starts with three lives and zero score, then continues through the remaining stages. **Restart Game** and **Play Again** restart from the selected starting stage.

1. Winter: snowy blue scenery and all special brick types.
2. Spring: cherry blossoms, memory bricks and sleeping bricks.
3. Summer: tropical waves and shy bricks.
4. Autumn / Halloween: spooky bricks, fighting bricks and darkness around the ball.
5. Pumpkin King: a boss battle without ordinary bricks.

The four seasonal stages each have 24 bricks. Clear them to continue; defeating the boss wins the game. Score and lives appear at the top. Clearing a season restores one life, capped at three.

## Controls

- Arrow keys or A/D: move the paddle in Keyboard mode.
- Mouse movement: steer in Mouse mode (choose it on the welcome screen).
- Space or a playfield click in Mouse mode: launch.
- P, Escape or **Pause**: pause. **Resume** continues; **Main Menu** returns to the welcome screen.
- E or **Season Power**: activate a fully charged seasonal ability.
- Hold X or hold **Fire Laser**: fire in the boss stage.
- **Sound**: mute/unmute music and effects.
- Close the window to exit. Switching away pauses automatically.

All active gameplay, attack and power timers freeze while paused or waiting to launch. Laser energy also waits while docked. Memory previews last three seconds before seasonal stages.

## Bricks, balls and scoring

Sleeping bricks take two hits. Shy bricks dodge the nearest ball. Fighting bricks shoot slowing projectiles (up to three at once); shots slow the paddle for two seconds without removing lives.

Destroy numbered memory bricks in order 1-2-3-4 for a 100-point bonus. A wrong order cancels only that stage's bonus. Ordinary destruction awards 10 points. Each destroyed brick adds 10 ability charge, capped at 100.

Ball speed rises smoothly by 10% every five seconds of launched play, capped at 2x after 50 seconds. The timer advances once for the whole game, including multiball. Losing a life preserves speed; a new stage or restart resets it.

Lose a life only when the last ball is missed. A replacement ball docks for relaunch; zero lives ends the game. A rescue shield saves one missed ball.

## Random power-ups

Each destroyed brick has a 25% chance to drop one of six equally likely pickups. Catch it with the paddle. There are no guaranteed drop positions.

| Label | Effect |
| --- | --- |
| W | Wide paddle for eight seconds |
| S | Slow ball for eight seconds: 0.65 times current speed, including acceleration |
| R | One-use rescue shield; maximum one |
| F | Freeze shy movement, enemy firing and enemy projectiles for five seconds |
| M | Add two balls at different angles, capped at three; replenish missing balls |
| + | One extra life, capped at three; at full lives award 50 points |

Timed effects refresh rather than stack. Freeze keeps the longer duration. Ball effects apply to all active balls. Losing the final ball clears powers, pickups and projectiles. Level changes, restart and returning to the menu also clear temporary effects.

## Seasonal abilities

| Season | Charged ability |
| --- | --- |
| Winter | Deep Freeze: eight-second enemy freeze and double brick points |
| Spring | Second Bloom: restore one life, or grant a shield at full lives |
| Summer | Sunburst: six-second piercing; one damage per separate brick contact |
| Autumn | Lantern: eight seconds of light, no invisibility, cleared enemy shots and blocked firing |

Memory bonuses stay at 100 during double scoring. Charge is consumed on activation and resets after life loss or a level change. The boss uses the Laser Paddle instead of seasonal abilities.

## Pumpkin King

The boss has 100 health. Each ball contact deals five damage and bounces the ball. It alternates three aimed shots and five-shot fans, with a visible warning before firing. Below half health it attacks every two seconds instead of three and uses faster projectiles.

Boss attacks slow the paddle, with a one-second shared hit cooldown. Losing the final ball costs one life, clears shots and restores laser energy; boss health and speed progression remain.

Lasers deal one damage per hit, firing every 0.25 seconds while held. Energy lasts three seconds. Release fire for one second to begin recharging at half an energy second per second. Energy and firing are shown in the HUD. Combat starts after launch.

## Face expressions

All balls use the member portrait: happy at three lives, sad at two, angry at one. Healing updates expressions immediately. Edited assets are `assets/face-happy.png`, `assets/face-sad.png` and `assets/face-angry.png`; the original JPG is preserved. Missing variants use the original portrait, and missing originals use a drawn face. Image-edit prompts and provenance are in `assets/EXPRESSION-ART.md`.

## Your stage music

Add Java Sound-compatible PCM WAV tracks to:

- `assets/music/winter.wav`
- `assets/music/spring.wav`
- `assets/music/summer.wav`
- `assets/music/autumn.wav`
- `assets/music/boss.wav`

These tracks have not yet been supplied. Missing or unsupported music uses the original synthesized melody and prints a diagnostic. Stage changes stop the old track and start the selected one. Audio-device failure allows silent gameplay. Run from the project directory or package the assets as classpath resources at the same paths.

## OOP structure and checks

The five concrete brick subclasses are grouped as static nested classes inside GameBrick.java, reducing navigation while preserving inheritance and polymorphism. See [VIDEO-GUIDE.md](VIDEO-GUIDE.md) for a timed presentation outline and explanations of all required OOP concepts.

GameBrick is abstract; its five subclasses implement behavior polymorphically through BrickContext. SeasonAbility supplies four implementations. GameModel composes balls, paddle, boss, effects and laser controller; state is private and collections are exposed as unmodifiable views. Boss and LaserPaddle own their timers and rules. Randomness is injectable for deterministic tests. GameAudio implements AutoCloseable and handles missing/device failures.

```sh
javac -Xlint:all -d out -sourcepath src src/brickBreacker/Brick.java tests/brickBreacker/GameModelTest.java
java "-Djava.awt.headless=true" -cp out brickBreacker.GameModelTest
```

Tests cover gameplay, selected-stage progression, powers, speed, multiball, drops, boss/laser rules, portrait loading and headless rendering. Preview images are written under ignored `out/`. Interactive controls, actual music and boss difficulty should also be checked by playing.

Original project credited to Sagar Mishra.
