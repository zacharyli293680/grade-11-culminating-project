ISU Readme.txt file 
Game: Texas Hold'em Poker
Zachary Li & Sarah Zhou
ICS-3U
01-22-2024

Responsibilities:
Zach
- Hand Strength determination (i.e. straight, flush, two pair. ect)
- Betting rounds and chip amount tracking
- Determining winner & reseting variables for new rounds
- Creating the background images in photoshop
- Merging of text based game with graphics
- Initial proposal and initial design

Sarah
- Creating gamestates & setting up graphics (paint component, JFrames, JPanel, ect)
- Syncing in game variables with in game displays
- Comments
- Background music and sound effects
- Merging of text based game with graphics
- Readme.txt file

How to play
- Press Play, choose 2 to 8 players with the + and - buttons, click a box to type a name (blank names become Player 1, Player 2, ...), then press Start Game
- Everyone shares one screen. The bottom panel says whose turn it is: pass the device to that player and press Start my turn (or Enter) to reveal their cards
- Pick Fold, Check/Call or Bet/Raise. Bet/Raise opens Min, 2x, Pot, All In, a slider and an amount box. The line under the buttons previews what you will put in
- Press Confirm (or Enter) to submit. The cards hide again and the panel asks for the next player
- Each turn has a 30 second clock shown on the player's seat and in the panel; on zero the player checks if possible, otherwise folds
- At the end of a hand the panel lists the winner, the winning hand and any side pots. Press Next Round to deal again
- Back returns to the menu and discards the game. Exit and closing the window quit. Press M to mute the music and click sound
- How to Play on the main menu shows the instructions and credits
- The window can be resized; the table and seats rescale (minimum 800x600)

How to compile and run (from this folder):
- javac -d out *.java
- java -cp out Poker
- Tests: javac -d out *.java tests/*.java   then   java -cp out PokerTests
- Screen renders (no window needed): java -Djava.awt.headless=true -cp out RenderScreens render   writes PNGs of every screen into render/
- The game looks for its sounds in the working folder, then next to the compiled classes and one folder up, so it can also be started from elsewhere

Files:
- Poker.java          window and host panel: current screen, sound, turn clock, mouse and keyboard routing
- Screen.java         interface each screen implements
- MenuScreen.java, SetupScreen.java, AboutScreen.java, TableScreen.java   the four screens, drawn entirely in code
- TableLayout.java    seat, card, pot and panel geometry from the window size and player count
- Theme.java          colours, fonts and drawing helpers
- UiButton.java, UiTextField.java, UiSlider.java   code-drawn widgets (drawing and click areas share one rectangle)
- CardPainter.java    draws card faces, backs and empty slots at any size
- Game.java           all Texas Hold'em rules (blinds, action order, betting, all-ins, side pots, showdown, elimination)
- HandEvaluator.java  best five-card hand out of seven, with full kicker comparison
- Player.java         one seat's chips, bets, cards and status
- tests/PokerTests.java     rule checks that run without the graphics
- tests/RenderScreens.java  paints every screen to PNG files for checking the layout
- The old PNG and GIF images are no longer used by the game

Rules implemented:
- Blinds of 5/10 posted automatically; the dealer button (D), small blind (SB) and big blind (BB) are marked on the seats and rotate every hand
- Heads-up: the dealer posts the small blind and acts first preflop
- Preflop action starts left of the big blind; the big blind gets the option to check or raise; postflop action starts left of the dealer
- Minimum bet is the big blind; minimum raise is the size of the last raise; every bet is capped at the player's chips (an undersized all-in is allowed)
- All-in players stop acting; when nobody can bet any more the remaining board is dealt and the hand goes to showdown
- Side pots: each player can only win the part of the pot they matched; uncalled chips are returned
- Split pots divide evenly and the odd chip goes to the first winner left of the dealer
- Ties are broken by kickers (best five cards out of seven)
- A player who loses all their chips is eliminated (OUT); the game ends when one player remains
Testing specific hands:
- Game.setCommunity(...) and Game.setPlayerCards(...) force cards after startHand(); see tests/PokerTests.java for examples
- Card numbers: rank = card % 13 (0 = two ... 12 = ace), suit = card / 13
- Royal flush: 8,9,10,11,12
- Straight flush (nine high): 3,4,5,6,7
- Quads(Aces): 12,25,38,51,0
- Full house (Aces over Kings): 12,25,38,11,24
- Flush(Queen high): 2,4,6,8,10
- Straight(Queen high): 6,20,34,9,10
- Trips(Aces): 12,25,38,1,2
- Two pair(Aces & Kings): 12,25,11,24,1
- Pair(Aces): 38,25,1,2,5
- High Card(Ace high): 12, 14, 15, 17, 18

Missing Functionalities:
- None of the originally listed items (blinds, side pots, losing/winning, timer) remain missing

Extra Functionalities:
- Whole new selectplayers gamestate
- Ability to enter names of players
- Extra buttons (double, pot, exit, back)
- Dealer button and blind markers, all-in and fold markers, turn timer, mute key
- Code-drawn resizable interface: seats placed for the player count, pass-the-device panel between turns, raise slider and amount box, results panel with side pots

Known bugs/errors
- None known. The earlier straight flush and wheel straight errors are fixed by evaluating the best five cards together.
- Simplification: any raise, even an undersized all-in, reopens the action for players who already acted
