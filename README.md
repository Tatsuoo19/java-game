🎮 Brick Breaker Game

A classic Brick Breaker game built in Java using Swing and AWT. The game features paddle-ball mechanics, brick collision detection, a scoring system, and win/lose states — all developed without external libraries.

📖 About

This project recreates the popular arcade-style Brick Breaker (similar to Atari Breakout). The player controls a paddle to bounce a ball and break all the bricks on the screen. The game ends when:

✅ All bricks are destroyed → You Win!

❌ The ball misses the paddle → Game Over

It’s a beginner-friendly project that demonstrates Java OOP concepts, GUI programming with Swing, event handling, and basic game logic.

✨ Features

Paddle and ball movement with keyboard controls

Brick collision detection and scoring system

Winning and Game Over states

Responsive paddle control with arrow keys

Modular code (Brick, BrickBreaker, MapGenerator classes)

No external libraries — built purely with Java, Swing, and AWT

🛠️ Technologies Used

Java (Core + OOP)

Java Swing – for GUI components (JFrame, JPanel, Timer)

Java AWT – for graphics, rendering, and event handling

🚀 How to Run

Clone this repository:

git clone https://github.com/SagarXdev23/Brick_Breaker-Java-Game

Open the project in your IDE (Eclipse, IntelliJ, VS Code with Java extension).

In VS Code, open the repository folder, then open `src/brickBreacker/Brick.java`
and click **Run** above `main()`, or select **Run Brick Breaker** in Run and Debug.
If VS Code still shows errors from the old layout, run **Java: Clean Java Language
Server Workspace** from the Command Palette and let it reload.

To run from either Git Bash or PowerShell in the repository folder:

```sh
javac -d out -sourcepath src src/brickBreacker/Brick.java
java -cp out brickBreacker.Brick
```

The compiler finds the other classes through `-sourcepath src`. Keep the spelling
`brickBreacker` as shown: it is the Java package name. The background loads from
the package resource when available, or from `image.jpg` in the working directory.
If neither image is available, the game uses a plain black background.

Use the Left ⬅️ and Right ➡️ arrow keys to move the paddle.

Try to break all the bricks and win! 🎉


📌 Future Improvements

Add multiple levels with increasing difficulty

Power-ups (e.g., wider paddle, faster ball, extra lives)

Background music & sound effects

Improved brick textures and animations

👨‍💻 Author

Sagar Mishra

💻 Aspiring Full-Stack Developer (Java + Web).

🚀 Passionate about building games and web applications.
