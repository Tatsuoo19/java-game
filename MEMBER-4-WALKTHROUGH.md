# Member 4: Power-ups, Seasonal Abilities, and Boss Fight

Use this guide for a **2½–3 minute video segment**. The spoken script is the recording portion; the explanations and questions afterward are preparation notes.

Your main OOP examples are **interfaces, polymorphism, encapsulation, and composition**. Describe your actual contribution accurately: “I will explain this system” is appropriate for the walkthrough; say “I implemented…” only for work you personally completed.

## 1. Prepare before recording

Open these files in this order:

1. [SeasonAbility.java](src/brickBreacker/SeasonAbility.java) — interface and two implementations.
2. [GameModel.java](src/brickBreacker/GameModel.java) — search for `activateAbility()`.
3. [GameEffects.java](src/brickBreacker/GameEffects.java) — private fields, charge methods, and timers.
4. [Boss.java](src/brickBreacker/Boss.java) — health and attack behavior.
5. [LaserPaddle.java](src/brickBreacker/LaserPaddle.java) — energy and firing limits.

Keep [PowerUpType.java](src/brickBreacker/PowerUpType.java) available if asked about pickups; you do not need to explain every enum constant during the video.

Enlarge your editor font. Use Ctrl+F to jump to the named methods instead of scrolling through the whole project.

### Prepare two short gameplay clips

**Seasonal ability clip**

1. Choose **Select Level → Summer → Start Selected Level**.
2. Wait for the memory preview, then press Space to launch.
3. Destroy ten bricks to fill the meter. Each destroyed brick adds 10 charge; the maximum is 100.
4. Press **E** or click the ready Season Power button.
5. Record the ball passing through bricks during Sunburst.

Keep this clip short in the final edit. Getting ten destroyed bricks may take longer than your speaking segment, so record beforehand. Random pickups do not fill the ability meter; brick destruction does.

**Boss clip**

1. Choose **Select Level → Pumpkin King → Start Selected Level**.
2. Press Space. The boss and weapons start updating after the ball is launched.
3. Hold **X** or hold **Fire Laser**.
4. Show the health bar and laser energy decreasing.
5. Release fire to show energy recharging. Include an incoming-attack warning if possible.

You do not need to defeat the boss during your segment.

## 2. Recording walkthrough and spoken script

### 0:00–0:25 — Introduce the feature

**Show:** The short seasonal-ability gameplay clip.

**Say:**

> “My section covers the power-up system, seasonal abilities, and the boss fight. Players can catch random pickups, such as multiball or extra life. They can also charge a separate seasonal ability by destroying bricks. These systems demonstrate interfaces, polymorphism, and encapsulation.”

### 0:25–1:10 — Interface and polymorphism

**Show:** `SeasonAbility.java`. Point to the interface, then compare `Sunburst.activate()` and `DeepFreeze.activate()`.

**Say:**

> “SeasonAbility is an interface. It defines a common contract: every seasonal ability must provide a name and an activate method. Sunburst and DeepFreeze both implement this interface, but they perform different actions. Sunburst enables piercing, while DeepFreeze freezes enemies and doubles brick points.”

**Switch to:** `GameModel.activateAbility()`, especially:

```java
SeasonAbility.forLevel(season()).activate(this, effects);
```

**Say:**

> “The game selects an ability for the current season and calls activate through the same interface. The actual object determines which implementation runs. This is polymorphism: one shared operation produces different behavior.”

### 1:10–1:40 — Encapsulation and effect duration

**Show:** `GameEffects.java`: private fields, `charge()`, `consumeCharge()`, and `update(dt)`.

**Say:**

> “GameEffects keeps its charge and timers private. Other classes request changes through methods instead of directly assigning these fields. Charge is capped at one hundred, and activating an ability consumes it. Timers decrease using elapsed time until the effect expires. This is encapsulation because the object manages its own state and rules.”

### 1:40–2:15 — Boss behavior

**Show:** Boss gameplay, then `Boss.java`: `health`, `damage()`, `isEnraged()`, and the attack interval in `update()`.

**Say:**

> “The boss starts with one hundred health. A ball hit deals five damage, while a laser hit deals one. Health is private and changes through the damage method, which prevents it from going below zero. At fifty health or less, the boss uses faster projectiles and a shorter attack interval. It alternates aimed shots and a fan pattern.”

### 2:15–2:50 — Laser limits and composition

**Show:** `LaserPaddle.java`, then the `effects`, `boss`, and `laser` fields in `GameModel.java`.

**Say:**

> “LaserPaddle manages private energy, cooldown, and recharge timing. Its update method returns true when the game is allowed to create a laser. This limits firing so players cannot continuously shoot without recharging.”
>
> “GameModel contains these objects and coordinates them. The boss manages its attacks, the laser controller manages firing permission, and the effects object manages powers. This is composition: the game is built from objects with separate responsibilities.”

**Handoff:**

> “That covers the power and boss systems. The next member will explain the face expressions, audio, and error handling.”

## 3. Understand the code you are showing

### A. Pickups and seasonal abilities are separate

| System | Trigger | Examples | Main code |
| --- | --- | --- | --- |
| Random pickup | A destroyed brick has a 25% chance to drop an item; the paddle catches it | Multiball, extra life, wide paddle | `GameModel.hit()`, `collect()`, `PowerUpType` |
| Seasonal ability | Destroy ten bricks, then activate with E or the button | Sunburst, Deep Freeze | `SeasonAbility`, `GameEffects`, `activateAbility()` |
| Boss laser | Hold X or Fire Laser after launching in the boss stage | Upward laser shots | `LaserPaddle.update()`, boss section of `GameModel.step()` |

Each of the six beneficial pickup types is equally likely after a successful drop roll. `NONE` is not a beneficial pickup.

`PowerUpType` is an **enum**: a named set of allowed types. It also stores each pickup's label and color. The enum/switch pickup logic is not the main example of polymorphism; use `SeasonAbility` for that explanation.

### B. Why use an interface?

```java
public interface SeasonAbility {
    String name();
    void activate(GameModel game, GameEffects effects);
}
```

The interface describes the operations an ability offers. Its implementations contain the specific behavior. The game can use a `SeasonAbility` reference without needing a different activation call for every implementation.

`forLevel()` contains the switch that chooses the implementation. The polymorphic part is calling `activate()` on the returned object, not the switch itself.

The concrete implementations are nested in the same file. They are still distinct classes. Their implementations of `activate()` **override** the interface method; they do not overload it.

### C. What happens when the player presses E?

1. The input handler calls `GameModel.activateAbility()`.
2. `canActivateAbility()` checks that the player is in an active seasonal stage, the preview has ended, a ball is launched, and charge is 100.
3. `consumeCharge()` spends the meter.
4. `forLevel(season())` chooses the ability.
5. `activate(this, effects)` applies its behavior.
6. Later simulation updates use the effect state to change gameplay.

`this` refers to the current GameModel. `effects` is the GameEffects object owned by that model.

For example, Sunburst sets the piercing timer to six seconds. The collision code checks this timer and skips the ordinary brick bounce while it is active.

### D. Why keep fields private?

Private fields cannot be assigned directly from another class. The object exposes operations that enforce its rules.

Examples:

```java
void charge() {
    charge = Math.min(100, charge + 10);
}

public void damage(int amount) {
    health = Math.max(0, health - amount);
}
```

The charge method limits the meter to 100. For the game's positive damage amounts, the damage method prevents health from becoming negative.

The effect-changing methods without an access modifier are **package-private**: other classes in `brickBreacker` can call them. They are not private methods. The fields themselves remain private.

### E. Boss and laser responsibilities

- `Boss`: health, horizontal movement, warning timing, attack selection, and projectile direction.
- `LaserPaddle`: energy, firing cooldown, recharge delay, and permission to fire.
- `GameModel`: creates and moves shots, checks collisions, applies damage, and decides victory.
- `BrickBreaker`: draws the boss/health bar and receives player input.

LaserPaddle does **not** extend Paddle in this project. It is a separate firing controller owned by GameModel. Use it as an example of composition and encapsulation, not class inheritance.

## 4. Numbers worth remembering

| Rule | Current value |
| --- | --- |
| Maximum ability charge | 100 |
| Charge per destroyed brick | 10 |
| Boss starting health | 100 |
| Ball damage to boss | 5 per contact |
| Laser damage to boss | 1 per hit |
| Boss stronger phase | 50 health or less |
| Boss attack interval | 3 seconds normally; 2 seconds in the stronger phase after the next attack schedules its cooldown |
| Attack warning | Last 0.5 seconds before an attack |
| Aimed volley / fan | 3 shots / 5 shots |
| Laser firing interval | 0.25 seconds |
| Laser energy capacity | 3 seconds of held fire |
| Recharge | Begins after 1 second of release; restores 0.5 energy seconds per second |

Only the last missed ball costs a life. Boss projectiles slow the paddle; they do not directly remove lives. Losing a life preserves boss health and stage speed, but clears projectiles and restores laser energy.

## 5. Likely questions from the instructor

**“Where is polymorphism?”**

In the call to `activate()` through the SeasonAbility interface. Different implementation objects execute different ability logic.

**“What abstraction does your section use?”**

SeasonAbility exposes a shared contract while leaving the effect details to its implementations.

**“Why not put everything in GameModel?”**

Separating effect state, boss behavior, and firing limits makes each responsibility easier to read, change, and test. GameModel coordinates their interactions.

**“What is dt?”**

Elapsed simulation time in seconds. Subtracting dt from timers makes durations depend on time instead of counting rendered frames.

**“What happens during pause?”**

GameModel stops updating the simulation, so effect timers, boss attacks, and laser energy stop advancing. Pause also clears held firing.

**“How do you prevent one overlap from repeatedly damaging the boss?”**

GameModel tracks boss contact separately for each ball. Another hit is allowed after that ball leaves the boss and contacts it again. A laser is removed after its hit.

**“How do you know the laser is limited?”**

Show the energy/cooldown checks and demonstrate energy exhaustion and recharge. Tests also cover firing duration, recharge timing, damage, and pause behavior.

## 6. Final rehearsal checklist

- Keep your spoken section under three minutes.
- Show two ability implementations, not all four in detail.
- Explain what a method accomplishes before discussing its code.
- Do not read the trigonometry line by line; explain that it aims/spreads projectiles.
- Use the gameplay clips to support the OOP explanation.
- Be accurate about your own contribution and understand any code you present.
- Rehearse once without reading the script word for word.
