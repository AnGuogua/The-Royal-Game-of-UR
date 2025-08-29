# The Royal Game of UR
Play a two-thousand-year-old board game on your phone!

## Rules

### Board

The board has a total of 20 squares, divided into three sections:

- **Starting Area**: The row of squares closest to the player.
- **Shared Area**: The 8 middle squares are shared by both players.
- **Safe Squares**: The dark gray squares on the board.

### Pieces and Dice

Each player has 7 pieces.

Tap the **"Roll"** button, and the number displayed above indicates the points for the current player's turn.

### Basic Rules

#### Entering the Board

- Players take turns rolling the dice.  
- If the result is **0**, the turn is skipped.  
- If the result is **1–4**, a player may move one piece:
  - From off the board onto the board (to the square corresponding to the dice roll), or  
  - Move an existing piece on the board forward by the number rolled.

If no legal move is possible, the turn is skipped.  
If a move is possible, the player must move one piece.

#### Movement Direction

Pieces move along the path from the player's starting area → shared area → goal.

#### Capturing Pieces

If a piece lands on a square occupied by an opponent's piece, and that square is **not** a safe square, the opponent's piece is captured and sent back off the board.

#### Safe Squares

Pieces on safe squares cannot be captured.

#### Bonus Roll

If a piece lands on a safe square, the player may roll the dice again.

#### Winning Condition

The first player to move all their pieces along the path and off the board wins.

## About This Game

This is a beta version mini-game created for practice.  
The interface is not very polished, and some features are incomplete.  
If you find any bugs, please report them in the issues section.

## Development

### Development Process

1. Clone this project to your local machine  
2. Open the project in Android Studio  

### Test Environment

- Windows 11 + Android Studio Koala 2024.1.1 Patch 2  
- Real Device: HarmonyOS 4.1 (Android 12)  
- Emulator: Android API 35  

## TODO

- Refactor code  
- Ensure compatibility with various device sizes (including small-window mode)  
- Improve UI design (make it more visually appealing)  
