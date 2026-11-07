# BRICK BLOCK

BRICK BLOCK is a 2D Java game where you control a paddle, bounce a ball, and break bricks across four seasonal stages. Each season has its own appearance and ability. The adventure ends with a battle against the Pumpkin King.

## How to play

Move the paddle to keep the ball in play and clear the bricks. You begin with three lives. Missing the last active ball costs one life, and losing all your lives ends the game.

Choose **Start Adventure** to begin in Winter, or use **Select Level** to start at any stage. Selecting a stage starts a new run with three lives and zero score, then continues through the remaining levels. Clearing a seasonal stage restores one life, up to a maximum of three.

The ball gradually gets faster as you play, reaching twice its starting speed after 50 seconds of active play. Speed resets at the start of each stage.

## Stages

- **Winter:** Snow-themed scenery with sleeping, shy, fighting, and memory bricks.
- **Spring:** Cherry blossoms, colorful bricks, and a healing ability.
- **Summer:** Tropical scenery, dodging bricks, and a piercing-ball ability.
- **Autumn / Halloween:** A dark playfield, spooky decorations, and attacking bricks. Light around the ball helps you follow it.
- **Pumpkin King:** A final boss battle using the ball and a laser-equipped paddle.

Each seasonal stage has 24 bricks. All five stages are available through the level selection menu.

## Special bricks

Some bricks do more than stay in place:

- **Sleeping bricks** need two hits to break.
- **Shy bricks** move away when a ball gets close.
- **Fighting bricks** shoot projectiles that temporarily slow the paddle.
- **Memory bricks** award a 100-point bonus when destroyed in the order 1, 2, 3, 4.

Each destroyed brick normally awards 10 points.

## Power-ups

Destroyed bricks have a chance to drop a power-up. Catch it with the paddle to use it.

| Pickup | Effect |
| --- | --- |
| **W — Wide Paddle** | Makes the paddle wider for eight seconds. |
| **S — Slow Ball** | Slows active balls for eight seconds. |
| **R — Rescue Shield** | Saves one ball that would otherwise be missed. |
| **F — Freeze** | Stops enemy movement, firing, and projectiles for five seconds. |
| **M — Multiball** | Adds balls, up to three at a time. |
| **+ — Extra Life** | Restores one life, or gives 50 points if lives are already full. |

During multiball, you only lose a life when the last ball is missed.

## Seasonal abilities

Breaking bricks fills the Season Power meter. Once it reaches 100%, press **E** or click the ready power button.

| Season | Ability |
| --- | --- |
| Winter | **Deep Freeze:** Freezes enemies and doubles brick points for eight seconds. |
| Spring | **Second Bloom:** Restores a life, or grants a rescue shield at full lives. |
| Summer | **Sunburst:** Lets the ball pass through bricks for six seconds. |
| Autumn | **Lantern:** Lights the field, prevents ball invisibility, clears enemy shots, and stops firing for eight seconds. |

Temporary powers and charge reset when you lose a life or move to another stage.

## Boss battle

The Pumpkin King has a health bar and moves across the top of the playfield. Hit it with the ball or fire lasers from the paddle to deal damage.

The boss alternates between aimed shots and a spread of projectiles. Its attacks become faster when its health reaches half. A warning appears before each attack.

Lasers have limited energy. Release the fire control to let them recharge. Defeating the boss completes the game.

## Face expressions and sound

The ball displays a group member's face. Its expression follows the remaining lives: happy with three, sad with two, and angry with one. Gaining a life changes the expression back.

The game includes background music and sound effects, with a Sound button to mute them. Separate music files can be assigned to each stage; these tracks are still pending, so the game currently uses its built-in melody.

## Controls

| Control | Action |
| --- | --- |
| Left/Right arrows or A/D | Move the paddle in Keyboard mode |
| Mouse movement | Move the paddle in Mouse mode |
| Space | Launch the ball |
| Click the playfield | Launch in Mouse mode |
| P or Escape | Pause or resume |
| E | Activate a charged seasonal ability |
| Hold X or Fire Laser | Shoot during the boss battle |

Choose Keyboard or Mouse mode on the welcome screen. The pause menu also lets you restart, select a level, or return to the main menu. Switching away from the window pauses the game.

## Running the game

Requires JDK 8 or later. From the project folder:

```sh
javac -d out -sourcepath src src/brickBreacker/Brick.java
java -cp out brickBreacker.Brick
```