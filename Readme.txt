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

Hints how to play / Skip levels
- To play, you must first pres play in the main menu
- To start your turn, you must press the start turn button which will reveal your cards, and allow you to start placing bets
- To use the betting options (options in blue square), you must first press the bet/raise button
- To submit the bets, you must press the finish end turn button which will submit the bets and hide your cards and wait for the next player to start their turn
- To see the about/instructions page, at the menu click the top right corner (secret button)
- To go back to game menu in the about/instructions page, click the top left corner

How to compile and run (from this folder):
- javac -d out *.java
- java -cp out Poker
- Tests: javac -d out *.java tests/PokerTests.java   then   java -cp out PokerTests

Files:
- Poker.java          screens, drawing, mouse and keyboard input, turn timer
- Game.java           all Texas Hold'em rules (blinds, action order, betting, all-ins, side pots, showdown, elimination)
- HandEvaluator.java  best five-card hand out of seven, with full kicker comparison
- Player.java         one seat's chips, bets, cards and status
- tests/PokerTests.java  rule checks that run without the graphics

Rules implemented:
- Blinds of 5/10 posted automatically; the dealer button (D), small blind (SB) and big blind (BB) are shown beside each player's chips and rotate every hand
- Heads-up: the dealer posts the small blind and acts first preflop
- Preflop action starts left of the big blind; the big blind gets the option to check or raise; postflop action starts left of the dealer
- Minimum bet is the big blind; minimum raise is the size of the last raise; every bet is capped at the player's chips (an undersized all-in is allowed)
- All-in players stop acting; when nobody can bet any more the remaining board is dealt and the hand goes to showdown
- Side pots: each player can only win the part of the pot they matched; uncalled chips are returned
- Split pots divide evenly and the odd chip goes to the first winner left of the dealer
- Ties are broken by kickers (best five cards out of seven)
- A player who loses all their chips is eliminated (OUT); the game ends when one player remains
- 30 second turn timer once Start Turn is pressed; on zero the player checks if they can, otherwise folds
- Back returns to the menu and discards the game; closing the window quits
- Press M to mute or unmute the music and click sound

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

Known bugs/errors
- None known. The earlier straight flush and wheel straight errors are fixed by evaluating the best five cards together.
- Simplification: any raise, even an undersized all-in, reopens the action for players who already acted
