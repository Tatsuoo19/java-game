# BRICK BLOCK video walkthrough

Target: 12–14 minutes, staying within the guideline's 15-minute excellent band. Explain representative behavior instead of reading every file or method. Each member should present the parts they actually contributed to and can explain; this outline does not assign or invent contributions.

## Suggested recording order

| Time | Show | Explain |
| --- | --- | --- |
| 0:00–0:45 | Welcome and level selection | Game objective, Java Swing desktop platform, five stages |
| 0:45–3:30 | Play a seasonal stage, demonstrate pickups, then select the boss | Controls, lives, expressions, speed increase, boss health and lasers |
| 3:30–7:30 | GameBrick.java, then its update call in GameModel.java | Encapsulation, abstraction, inheritance and polymorphism |
| 7:30–9:30 | SeasonAbility.java | Interface and different implementations of the same operation |
| 9:30–11:00 | GameModel.java fields and relevant behavior | Composition, in-memory collections and object collaboration |
| 11:00–12:00 | GameAudio.java exception handling | Recovering from missing music or unavailable audio |
| 12:00–14:00 | Relevant member-owned code and final gameplay | Each member's contribution, test evidence and conclusion |

Use actual gameplay clips for pickups; their appearance is random. Level selection helps demonstrate all stages without completing the full game. Do not claim the custom music is finished until the supplied tracks are installed.

## The main example: GameBrick.java

The five concrete brick types are now public static nested classes in the same file as the abstract parent. They remain separate classes with their own instances and behavior. Static here means no outer GameBrick instance is needed to construct them; it does not mean their health or positions are shared.

### Encapsulation

Show private health and position fields, getHealth(), and hit().

Suggested explanation: “Each brick keeps its health private. The game requests a hit through a method, which applies the damage rules. Other classes can read health but cannot assign the field directly.”

### Abstraction

Show abstract class GameBrick and abstract update(dt, context).

Suggested explanation: “GameBrick defines what every brick must support and provides shared damage and collision-bound behavior. It leaves each brick's timed behavior to the concrete subclasses.”

An abstract class cannot be instantiated directly. A normal brick is a concrete NormalBrick object.

### Inheritance

Show SleepingBrick extends GameBrick and its super(...) constructor.

Suggested explanation: “SleepingBrick inherits the parent's health handling and bounds. Its constructor supplies two health points, so the shared damage method requires two hits.”

### Polymorphism

Compare ShyBrick.update() with FightingBrick.update(), then show the GameModel loop that calls brick.update(dt, this) through a GameBrick reference.

Suggested explanation: “The game calls the same update method for each brick. Java chooses the implementation belonging to the actual object: a shy brick dodges and a fighting brick requests a projectile.”

Also show MemoryBrick.onDestroyed(): it reports the sequence number using the common destruction hook.

## Interface example: SeasonAbility.java

Show the interface's activate() method and two implementations: Sunburst and DeepFreeze.

Suggested explanation: “The interface gives all seasonal abilities the same contract. The game activates the chosen ability through that contract; each implementation applies a different effect.”

BrickContext is another interface: bricks request game services without directly changing the model's private fields.

## Composition example: GameModel.java

Show the ball collection, Paddle, GameEffects, Boss and LaserPaddle fields.

Suggested explanation: “The game model has these objects and coordinates them. A game has a paddle and balls; it is not a kind of paddle or ball. This is composition.”

Use one interaction as the story: ball intersects a brick → hit() changes health → a destroyed brick adds score and may drop a pickup. Explain List and Map as in-memory storage, not a database.

## Exception handling: GameAudio.java

Show track(), its try/catch, and the fallback assignment.

Suggested explanation: “An external music file may be missing or unsupported. We catch those errors, report them and fall back to the generated melody. If audio cannot initialize, gameplay can continue silently.”

Do not call the intentional missing-image test diagnostic a game failure. Automated checks are supporting evidence; a successful test run is not proof that every possible bug is absent.

## What to prepare

- Write each member's actual contribution and choose their matching section.
- Bookmark the methods above before recording; enlarge the editor font.
- For every concept, explain what it means, point to the exact code, and show its effect in the game.
- Do not read long collision or rendering methods line by line.
- Use your own wording and be ready to explain why an abstract class or interface was useful.
- Keep the project's original attribution and accurately describe the group's additions and any assistance used, following your instructor's rules.
