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
- To submit the bets, you must press the finish end turn button which will submit the bets and hide your cards and wait for the next 
- To see the about/instructions page, at the menu click the top right corner (secret button)
- To go back to game menu in the about/instructions page, click the top left corner 
player to start their turn
- To test the following hands, input these as the following variables where the comment in the code says to in the resetRound() method
- Format is: (globalFlopCard1, globalFlopCard2, globalFlopCard3, globalTurnCard, globalRiverCard)
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
- Blinds
- Side pots
- Losing/Winning
- Timer

Extra Functionalities:
- Whole new selectplayers gamestate
- Ability to enter names of players
- Extra buttons (double, pot, exit, back)
- Action is on preflop aggressor

Known bugs/errors
- When there are 5 out of 7 cards that are the same suit and also there is a 5 card combination that makes a straight, it will display straight flush regardless if the cards in the straight are suited or not
- Wheel straights are all displayed as straightflushes