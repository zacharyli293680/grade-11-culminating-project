// Poker ISU
// Zachary Li & Sarah Zhou
// 01-22-2024
// This is a recreartion of the famous Texas Hold'em Poker
// The big blind is 10 chips and every player starts with 1000 chips


// imports
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;
import java.util.Arrays;
import java.util.Scanner;
import java.util.*;
import java.io.*;

// constructor
public class Poker extends JPanel implements MouseListener{
	
	// Graphics 
	JPanel myPanel;
	JFrame frame;
	Image pokerLogo;
	Toolkit t = Toolkit.getDefaultToolkit();
	static Clip backgroundMusic;
	static Clip soundEffect;
	
	// initializing global variables
	int numPlayer; 
	int screen = 0;
	int player = 0;
	boolean newRound = true;
	public static int[] globalPlayerCards;
	public static int globalFlopCard1;
	public static int globalFlopCard2;
	public static int globalFlopCard3;
	public static int globalTurnCard;
	public static int globalRiverCard;
	public static int [][] betting;
	public static String[] playerNames;
	public static String[] cardName = {"two", "three", "four", "five", "six", "seven", "eight", "nine", "ten", "jack", "queen", "king", "ace"};
	public static int[] drawnCards = {};
	public static int drawnCount = 0;
	public static int [] playerCardCoordinates = {195, 111, 245, 111, 355, 111, 400, 111, 515, 111, 560, 111, 620, 205, 665, 205, 560, 300, 515, 300, 400, 300, 355, 300, 245, 300, 195, 300, 90, 205, 135, 205};
	public static int[] communityCardsCoordinates = {290, 205, 335, 205, 380, 205, 425, 205, 470, 205};
	public static int [] playersEarnings = {194, 91, 354, 91, 514, 91, 620, 182, 512, 352, 354, 352, 194, 352, 89, 182};
	public static int [] playersBets = {215, 167, 375, 167, 535, 167, 639, 262, 530, 280, 375, 280, 215, 280, 108, 262};
	public static int pot = 0;
	public static int currentBet = 10;
	private final Font ARIAL_BIG = new Font("Ariel", Font.PLAIN, 25);
	private final Font ARIAL_SMALL = new Font("Ariel", Font.PLAIN, 15);
	public static boolean mouseInput = false;
	public static int mouseX;
	public static int mouseY;
	public static int bettingRound = 1;
	public static boolean turn;
	public static int turnAction = 0;
	public static boolean newGame = true;
	public static boolean raise;
	public static boolean resetBets;
	public static int customRaise;
	public static int raiseAmount;
	public static int playerTotalChip;
	public static int playerTotalBet;
	public static boolean winner = false;
	public static int actionShift = 0;

	// class
	public Poker() {
		
		// creating jpanel and jframe
		setPreferredSize(new Dimension(800, 750));
		setBackground(new Color(255, 255, 255));
		setFont(ARIAL_BIG);
		frame = new JFrame("Poker Home Screen");
		myPanel = new JPanel();
		myPanel.setPreferredSize(new Dimension(800, 750));
		myPanel.setLayout(null);
		myPanel.setBackground(Color.WHITE);
		myPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		addMouseListener(this);
		
		// background music
		try {
			AudioInputStream sound = AudioSystem.getAudioInputStream(new File ("gdmusic.wav"));
			backgroundMusic = AudioSystem.getClip();
			backgroundMusic.open(sound);
		}
		catch (Exception e) {
		}
	}
	
	// gets all the player names and stores them in an array
	public void playerNames () throws IOException {
		String name = "";
		playerNames = new String[numPlayer];
		int num = 1;
		for (int i = 0; i < numPlayer; i++) {
			name = JOptionPane.showInputDialog("Enter player " + num + "'s name: ");
			if (name == null) {
				name = JOptionPane.showInputDialog("Try again. Enter player " + num + "'s name: ");
			}
			else
				playerNames[i] = name;
			num++;
		} 
	}
	
	// paint component
	public void paintComponent(Graphics g) {
		//This method draws the graphics and calls the various screens. It returns nothing.
		super.paintComponent(g);
		// The screen variable is responsible for the gamestates
		if (screen == 0) { 
			homeScreen(g); // home menu 
		} else if (screen == 1) {
			playerScreen(g); // player select menu
		} else if (screen == 2) {
			if (newGame) {
				initializeBetting(); 
			}
			if (resetBets) { // resets key variables after each round
				currentBet = 0;
				resetBets = false;
			}
			if (newRound) { // resets key variables after each round
				resetRound();
			}
			gameScreen(g); // main game
			if (bettingRound  == 1) {
				preFlop(g); // fre-flop betting round
			} else if (bettingRound == 2) {
				flop(g); // flop betting round
			} else if (bettingRound == 3) {
				turn(g); // turn betting round
			} else if (bettingRound == 4) {
				river(g); // river betting round
			} else if (bettingRound == 5) {
				showdown(g); // showdown
			}
		} else if (screen == 3) {
			aboutScreen(g); // about/instructions page
		}
	}
	
	
	public int[] cardDisplay (int numPlayer) {
		//This method takes in the number of players that the user selected, and returns an array 
		//of x and y coordinates of where the face down cards should be put. For each player, 4 x and y 
		//coordinates are needed since there are 2 face down cards. 
		int [] result = new int [numPlayer*4];
		for (int i = 0; i < numPlayer*4; i++) {
			result [i] = playerCardCoordinates[i];
		}
		return result;	
	}
	
	// displays and runs the about/instructions page gamestate
	public void aboutScreen (Graphics g) {
		Image aboutScreen = t.getImage("About Page.png");
		try {
			BufferedImage bufferedImage = ImageIO.read(new File ("About Page.png"));
			aboutScreen = bufferedImage.getScaledInstance(800, 750, Image.SCALE_DEFAULT);
		} catch (IOException e) {
			e.printStackTrace();
		}

		g.drawImage(aboutScreen, 0, 0, this);
	}
	
	// displays and runs the player select gamestate
	public void playerScreen (Graphics g) {
		//This method designs the player screen by loading an image and rescaling it. It returns nothing. 
		Image playerMenu = t.getImage("Player Screen.png");
		try {
			BufferedImage bufferedImage = ImageIO.read(new File ("Player Screen.png"));
			playerMenu = bufferedImage.getScaledInstance(800, 750, Image.SCALE_DEFAULT);
		} catch (IOException e) {
			e.printStackTrace();
		}

		g.drawImage(playerMenu, 0, 0, this);
	}
	
	// draws one card face up
	public void drawCard(Graphics g) {
		//This method draws a single card for the 5 community cards (will be called 5 times). Returns nothing. 
		Toolkit t = Toolkit.getDefaultToolkit();
		int card = 13;
		Image pokerLogo = t.getImage(card + ".gif");
		try {
			BufferedImage bufferedImage = ImageIO.read(new File(card + ".gif"));
			pokerLogo = bufferedImage.getScaledInstance(40, 50, Image.SCALE_DEFAULT);
		} catch (IOException e) {
			e.printStackTrace();
		}
		g.drawImage(pokerLogo, mouseX, mouseY, this);
		mouseInput = false;
	}
	
	// draws and runs the home menu gamestate
	public void homeScreen(Graphics g) {
		//This method draws the first screen by loading and rescaling the poker logo. It returns nothing. 
		Toolkit t = Toolkit.getDefaultToolkit();
		Image pokerLogo = t.getImage("Poker Menu.png");
		try {
			BufferedImage bufferedImage = ImageIO.read(new File("Poker Menu.png"));
			pokerLogo = bufferedImage.getScaledInstance(800, 750, Image.SCALE_DEFAULT);
		} catch (IOException e) {
			e.printStackTrace();
		}

		g.drawImage(pokerLogo, 0, 0, this);
	}

	// draws and runs the main game gamestate
	public void gameScreen (Graphics g) {
		//This method draws the game screen. It returns nothing. 
		setFont(ARIAL_BIG);
		// draws and resizes the green game board
		Image gameBoard = t.getImage("Poker Game.png");
		try {
			BufferedImage bufferedImage = ImageIO.read(new File ("Poker Game.png"));
			gameBoard = bufferedImage.getScaledInstance(800, 750, Image.SCALE_DEFAULT);
		} catch (IOException e) {
			e.printStackTrace();
		}
		g.drawImage(gameBoard, 0, 0, this);

		// draws and resizes the face down cards, depending on the # of players. 
		Image faceDownCard = t.getImage("face down card.png");
		try { 
			BufferedImage bufferedImage = ImageIO.read(new File ("face down card.png"));
			faceDownCard = bufferedImage.getScaledInstance(40, 50, Image.SCALE_DEFAULT);
		} catch (IOException e) {
			e.printStackTrace();
		}
		int [] result = cardDisplay (numPlayer); //Determines the # of cards to draw
		for (int i = 0; i < result.length; i+=2) {
			g.drawImage(faceDownCard, result[i], result [i+1], this);
		}

		//Draws the various buttons. 
		g.setColor(Color.WHITE);
		g.drawString("Fold", 85, 597);
		if (!turn) {
			g.drawString("Check/Call", 85, 657);
		}
		if (currentBet == 0) {
			g.drawString("Bet", 85, 716);
		} else {
			g.drawString("Raise", 85, 716);
		}
		int [] earnings = playersEarnings (numPlayer);
		g.setColor(Color.WHITE);
		g.setFont(ARIAL_SMALL);
		for (int i = 0; i < earnings.length; i+=2) {
			g.setColor(Color.WHITE);
			g.fillRect(earnings[i], earnings [i+1], 88, 14);
			g.setColor(Color.BLACK);
			g.drawString("" + betting[i/2][0], earnings[i] + 20, earnings[i+1] + 12);
		}
		g.setFont(ARIAL_BIG);
		g.setColor(Color.CYAN);
		g.fillRect(0, 31, 148, 32);
		g.fillRect (662, 31, 148, 32);
		g.setColor(Color.BLACK);
		g.drawString("Back", 48, 55); //Back to main screen button
		g.drawString("Exit", 710, 55); //Exit button

		g.setColor(Color.WHITE);
		g.fillRect(357, 259, 85, 20);
		g.setColor(Color.BLACK);
		g.setFont(ARIAL_SMALL);
		g.drawString("" + pot, 380, 274); //Displays the current pot.

		//Displays each player's bets this round, depending on how many players there are. 
		int [] bets = playersBets (numPlayer);
		for (int i = 0; i < bets.length; i+=2) {
			g.setColor(Color.WHITE);
			g.fillRect(bets[i], bets [i+1], 50, 14);
			g.setColor(Color.BLACK);
			g.drawString("" + betting[i/2][1], bets[i] + 15, bets[i + 1] + 12);
		}
		g.setFont(ARIAL_BIG);

	}
	
	// runs the preflop betting round
	public void preFlop(Graphics g) {
		//This method runs the logistics of the first round of betting, depending on if the player
		//selects check, call, raise, or fold. It returns nothing. 
		g.setColor(Color.BLACK);
		g.drawString("Chips: " + betting[player][0], 325, 25); //Displays each player's available chips.
		g.drawString("Player: " + playerNames[player] , 50, 25);
		g.setColor(Color.WHITE);
		if (winner) {
			g.setColor(Color.CYAN);
			g.fillRect(0,400,150,36);
			g.setColor(Color.BLACK);
			g.drawString("Next Round", 9, 427);
			g.drawString("Winner: " + playerNames[player], 320, 427);
		}
		if (turn) {
			int totalChip = betting[player][0];
			int totalBet = betting[player][1];
			drawPlayerCards(g, player, globalPlayerCards);
			g.setColor(Color.WHITE);
			if (currentBet == 0) {
				g.drawString("Check", 85, 657);
			} else {
				g.drawString("Call " + (currentBet - totalBet), 85, 657);
			}
			if (raise) {
				g.setColor(Color.GREEN);
				g.fillRect(290,696,12,22);
			}
			if (turnAction == 1) {
				betting[player][3] = 0;
				playerTotalBet = totalBet;
				playerTotalChip = totalChip;
				g.setColor(Color.GREEN);
				g.fillRect(290,577,12,22);
				g.setColor(Color.BLACK);
				g.drawString("Current Bet: Fold", 450, 598);
			} else if (turnAction == 2) {
				int callAmount = currentBet - totalBet;
				playerTotalBet = totalBet + callAmount;
				playerTotalChip = totalChip - callAmount;
				g.setColor(Color.GREEN);
				g.fillRect(290,637,12,22);
				g.setColor(Color.BLACK);
				g.drawString("Current Bet: " + currentBet, 450, 598);
			} else if (turnAction == 3) {
				raiseAmount = currentBet;
				raiseAmount *= 2;
				playerTotalChip = totalChip - (raiseAmount - totalBet);
				playerTotalBet = raiseAmount;
				g.setColor(Color.GREEN);
				g.fillRect(533,638,12,22);
				g.setColor(Color.BLACK);
				g.drawString("Current Bet: " + raiseAmount, 450, 598);
			} else if (turnAction == 4) {
				int currentPot = 0;
				for (int i = 0; i < betting.length; i++) {
					currentPot += betting[i][1];
				}
				if (currentPot <= currentBet * 2) {
					currentPot = currentBet * 2;
				}
				raiseAmount = currentPot;
				playerTotalChip = totalChip - (raiseAmount - totalBet);
				playerTotalBet = raiseAmount;
				g.setColor(Color.GREEN);
				g.fillRect(735,638,12,22);
				g.setColor(Color.BLACK);
				g.drawString("Current Bet: " + raiseAmount, 450, 598);
			} else if (turnAction == 5) {
				raiseAmount = betting[player][0] + betting[player][1];
				playerTotalBet = totalChip + totalBet;
				playerTotalChip = 0;
				g.setColor(Color.GREEN);
				g.fillRect(533,698,12,22);
				g.setColor(Color.BLACK);
				g.drawString("Current Bet: " + raiseAmount, 450, 598);
			} else if (turnAction == 6) {
				raiseAmount = customRaise;
				playerTotalChip = totalChip - (raiseAmount - totalBet);
				playerTotalBet = raiseAmount;
				g.setColor(Color.GREEN);
				g.fillRect(735,698,12,22);
				g.setColor(Color.BLACK);
				g.drawString("Current Bet: " + raiseAmount, 450, 598);
			}
		}
	}
	
	// runs the flop betting round
	public void flop(Graphics g) {
		//This method deals the first 3 cards to the board and runs through the logistics of each possible move
		//for each player. It returns nothing. 
		g.setFont(ARIAL_BIG);
		g.setColor(Color.BLACK);
		g.drawString("Chips: " + betting[player][0], 325, 25);
		g.drawString("Player: " + playerNames[player] , 50, 25);
		drawCommunityCards(g, communityCardsCoordinates[0], communityCardsCoordinates[1], globalFlopCard1);
		drawCommunityCards(g, communityCardsCoordinates[2], communityCardsCoordinates[3], globalFlopCard2);
		drawCommunityCards(g, communityCardsCoordinates[4], communityCardsCoordinates[5], globalFlopCard3);
		if (winner) {
			g.setColor(Color.CYAN);
			g.fillRect(0,400,150,36);
			g.setColor(Color.BLACK);
			g.drawString("Next Round", 9, 427);
			g.drawString("Winner: " + playerNames[player], 320, 427);
		}
		if (turn) {
			int totalChip = betting[player][0];
			int totalBet = betting[player][1];
			drawPlayerCards(g, player, globalPlayerCards);
			g.setColor(Color.WHITE);
			if (currentBet == 0) {
				g.drawString("Check", 85, 657);
			} else {
				g.drawString("Call " + (currentBet - totalBet), 85, 657);
			}
			if (raise) {
				g.setColor(Color.GREEN);
				g.fillRect(290,696,12,22);
			}
			if (turnAction == 1) {
				betting[player][3] = 0;
				playerTotalBet = totalBet;
				playerTotalChip = totalChip;
				g.setColor(Color.GREEN);
				g.fillRect(290,577,12,22);
				g.setColor(Color.BLACK);
				g.drawString("Current Bet: Fold", 450, 598);
			} else if (turnAction == 2) {
				int callAmount = currentBet - totalBet;
				playerTotalBet = totalBet + callAmount;
				playerTotalChip = totalChip - callAmount;
				g.setColor(Color.GREEN);
				g.fillRect(290,637,12,22);
				g.setColor(Color.BLACK);
				g.drawString("Current Bet: " + currentBet, 450, 598);
			} else if (turnAction == 3) {
				raiseAmount = currentBet;
				raiseAmount *= 2;
				playerTotalChip = totalChip - (raiseAmount - totalBet);
				playerTotalBet = raiseAmount;
				g.setColor(Color.GREEN);
				g.fillRect(533,638,12,22);
				g.setColor(Color.BLACK);
				g.drawString("Current Bet: " + raiseAmount, 450, 598);
			} else if (turnAction == 4) {
				int currentPot = 0;
				for (int i = 0; i < betting.length; i++) {
					currentPot += betting[i][1];
				}
				currentPot += pot;
				if (currentPot <= currentBet * 2) {
					currentPot = currentBet * 2;
				}
				raiseAmount = currentPot;
				playerTotalChip = totalChip - (raiseAmount - totalBet);
				playerTotalBet = raiseAmount;
				g.setColor(Color.GREEN);
				g.fillRect(735,638,12,22);
				g.setColor(Color.BLACK);
				g.drawString("Current Bet: " + raiseAmount, 450, 598);
			} else if (turnAction == 5) {
				raiseAmount = betting[player][0] + betting[player][1];
				playerTotalBet = totalChip + totalBet;
				playerTotalChip = 0;
				g.setColor(Color.GREEN);
				g.fillRect(533,698,12,22);
				g.setColor(Color.BLACK);
				g.drawString("Current Bet: " + raiseAmount, 450, 598);
			} else if (turnAction == 6) {
				raiseAmount = customRaise;
				playerTotalChip = totalChip - (raiseAmount - totalBet);
				playerTotalBet = raiseAmount;
				g.setColor(Color.GREEN);
				g.fillRect(735,698,12,22);
				g.setColor(Color.BLACK);
				g.drawString("Current Bet: " + raiseAmount, 450, 598);
			}
		}
	}

	// runs the turn betting round
	public void turn(Graphics g) {
		//This method runs through the logistics of the third round of betting. The fourth community card
		//is being drawn. Once again, it sorts through the logistics of each possible move the player could make.
		//This method returns nothing. 
		g.setFont(ARIAL_BIG);
		g.setColor(Color.BLACK);
		g.drawString("Chips: " + betting[player][0], 325, 25);
		g.drawString("Player: " + playerNames[player] , 50, 25);
		drawCommunityCards(g, communityCardsCoordinates[0], communityCardsCoordinates[1], globalFlopCard1);
		drawCommunityCards(g, communityCardsCoordinates[2], communityCardsCoordinates[3], globalFlopCard2);
		drawCommunityCards(g, communityCardsCoordinates[4], communityCardsCoordinates[5], globalFlopCard3);
		drawCommunityCards(g, communityCardsCoordinates[6], communityCardsCoordinates[7], globalTurnCard);
		if (winner) {
			g.setColor(Color.CYAN);
			g.fillRect(0,400,150,36);
			g.setColor(Color.BLACK);
			g.drawString("Next Round", 9, 427);
			g.drawString("Winner: " + playerNames[player], 320, 427);
		}
		if (turn) {
			int totalChip = betting[player][0];
			int totalBet = betting[player][1];
			drawPlayerCards(g, player, globalPlayerCards);
			g.setColor(Color.WHITE);
			if (currentBet == 0) {
				g.drawString("Check", 85, 657);
			} else {
				g.drawString("Call " + (currentBet - totalBet), 85, 657);
			}
			if (raise) {
				g.setColor(Color.GREEN);
				g.fillRect(290,696,12,22);
			}
			if (turnAction == 1) {
				betting[player][3] = 0;
				playerTotalBet = totalBet;
				playerTotalChip = totalChip;
				g.setColor(Color.GREEN);
				g.fillRect(290,577,12,22);
				g.setColor(Color.BLACK);
				g.drawString("Current Bet: Fold", 450, 598);
			} else if (turnAction == 2) {
				int callAmount = currentBet - totalBet;
				playerTotalBet = totalBet + callAmount;
				playerTotalChip = totalChip - callAmount;
				g.setColor(Color.GREEN);
				g.fillRect(290,637,12,22);
				g.setColor(Color.BLACK);
				g.drawString("Current Bet: " + currentBet, 450, 598);
			} else if (turnAction == 3) {
				raiseAmount = currentBet;
				raiseAmount *= 2;
				playerTotalChip = totalChip - (raiseAmount - totalBet);
				playerTotalBet = raiseAmount;
				g.setColor(Color.GREEN);
				g.fillRect(533,638,12,22);
				g.setColor(Color.BLACK);
				g.drawString("Current Bet: " + raiseAmount, 450, 598);
			} else if (turnAction == 4) {
				int currentPot = 0;
				for (int i = 0; i < betting.length; i++) {
					currentPot += betting[i][1];
				}
				currentPot += pot;
				if (currentPot <= currentBet * 2) {
					currentPot = currentBet * 2;
				}
				raiseAmount = currentPot;
				playerTotalChip = totalChip - (raiseAmount - totalBet);
				playerTotalBet = raiseAmount;
				g.setColor(Color.GREEN);
				g.fillRect(735,638,12,22);
				g.setColor(Color.BLACK);
				g.drawString("Current Bet: " + raiseAmount, 450, 598);
			} else if (turnAction == 5) {
				raiseAmount = betting[player][0] + betting[player][1];
				playerTotalBet = totalChip + totalBet;
				playerTotalChip = 0;
				g.setColor(Color.GREEN);
				g.fillRect(533,698,12,22);
				g.setColor(Color.BLACK);
				g.drawString("Current Bet: " + raiseAmount, 450, 598);
			} else if (turnAction == 6) {
				raiseAmount = customRaise;
				playerTotalChip = totalChip - (raiseAmount - totalBet);
				playerTotalBet = raiseAmount;
				g.setColor(Color.GREEN);
				g.fillRect(735,698,12,22);
				g.setColor(Color.BLACK);
				g.drawString("Current Bet: " + raiseAmount, 450, 598);
			}
		}
	}
	
	// runs the river betting round
	public void river(Graphics g) {
		//This method runs the last round of betting, where the fifth community card is revealed. 
		//It runs through each possible move that the player can make. 
		//It returns nothing. 
		g.setFont(ARIAL_BIG);
		g.setColor(Color.BLACK);
		g.drawString("Chips: " + betting[player][0], 325, 25);
		g.drawString("Player: " + playerNames[player] , 50, 25);
		drawCommunityCards(g, communityCardsCoordinates[0], communityCardsCoordinates[1], globalFlopCard1);
		drawCommunityCards(g, communityCardsCoordinates[2], communityCardsCoordinates[3], globalFlopCard2);
		drawCommunityCards(g, communityCardsCoordinates[4], communityCardsCoordinates[5], globalFlopCard3);
		drawCommunityCards(g, communityCardsCoordinates[6], communityCardsCoordinates[7], globalTurnCard);
		drawCommunityCards(g, communityCardsCoordinates[8], communityCardsCoordinates[9], globalRiverCard);
		if (winner) {
			g.setColor(Color.CYAN);
			g.fillRect(0,400,150,36);
			g.setColor(Color.BLACK);
			g.drawString("Next Round", 9, 427);
			g.drawString("Winner: " + playerNames[player], 320, 427);
		}
		if (turn) {
			int totalChip = betting[player][0];
			int totalBet = betting[player][1];
			drawPlayerCards(g, player, globalPlayerCards);
			String handStrength = determineHandStrength(globalPlayerCards[player * 2], globalPlayerCards[player * 2 + 1], globalFlopCard1, globalFlopCard2, globalFlopCard3, globalTurnCard, globalRiverCard);
			g.drawString("Hand: " + handStrength, 500, 25);
			g.setColor(Color.WHITE);
			if (currentBet == 0) {
				g.drawString("Check", 85, 657);
			} else {
				g.drawString("Call " + (currentBet - totalBet), 85, 657);
			}
			if (raise) {
				g.setColor(Color.GREEN);
				g.fillRect(290,696,12,22);
			}
			if (turnAction == 1) {
				betting[player][3] = 0;
				playerTotalBet = totalBet;
				playerTotalChip = totalChip;
				g.setColor(Color.GREEN);
				g.fillRect(290,577,12,22);
				g.setColor(Color.BLACK);
				g.drawString("Current Bet: Fold", 450, 598);
			} else if (turnAction == 2) {
				int callAmount = currentBet - totalBet;
				playerTotalBet = totalBet + callAmount;
				playerTotalChip = totalChip - callAmount;
				g.setColor(Color.GREEN);
				g.fillRect(290,637,12,22);
				g.setColor(Color.BLACK);
				g.drawString("Current Bet: " + currentBet, 450, 598);
			} else if (turnAction == 3) {
				raiseAmount = currentBet;
				raiseAmount *= 2;
				playerTotalChip = totalChip - (raiseAmount - totalBet);
				playerTotalBet = raiseAmount;
				g.setColor(Color.GREEN);
				g.fillRect(533,638,12,22);
				g.setColor(Color.BLACK);
				g.drawString("Current Bet: " + raiseAmount, 450, 598);
			} else if (turnAction == 4) {
				int currentPot = 0;
				for (int i = 0; i < betting.length; i++) {
					currentPot += betting[i][1];
				}
				currentPot += pot;
				if (currentPot <= currentBet * 2) {
					currentPot = currentBet * 2;
				}
				raiseAmount = currentPot;
				playerTotalChip = totalChip - (raiseAmount - totalBet);
				playerTotalBet = raiseAmount;
				g.setColor(Color.GREEN);
				g.fillRect(735,638,12,22);
				g.setColor(Color.BLACK);
				g.drawString("Current Bet: " + raiseAmount, 450, 598);
			} else if (turnAction == 5) {
				raiseAmount = betting[player][0] + betting[player][1];
				playerTotalBet = totalChip + totalBet;
				playerTotalChip = 0;
				g.setColor(Color.GREEN);
				g.fillRect(533,698,12,22);
				g.setColor(Color.BLACK);
				g.drawString("Current Bet: " + raiseAmount, 450, 598);
			} else if (turnAction == 6) {
				raiseAmount = customRaise;
				playerTotalChip = totalChip - (raiseAmount - totalBet);
				playerTotalBet = raiseAmount;
				g.setColor(Color.GREEN);
				g.fillRect(735,698,12,22);
				g.setColor(Color.BLACK);
				g.drawString("Current Bet: " + raiseAmount, 450, 598);
			}
		}
	}
	
	// runs the showdown, determines winner, and awards the winner with the pot
	public void showdown(Graphics g) {
		drawCommunityCards(g, communityCardsCoordinates[0], communityCardsCoordinates[1], globalFlopCard1);
		drawCommunityCards(g, communityCardsCoordinates[2], communityCardsCoordinates[3], globalFlopCard2);
		drawCommunityCards(g, communityCardsCoordinates[4], communityCardsCoordinates[5], globalFlopCard3);
		drawCommunityCards(g, communityCardsCoordinates[6], communityCardsCoordinates[7], globalTurnCard);
		drawCommunityCards(g, communityCardsCoordinates[8], communityCardsCoordinates[9], globalRiverCard);
		int showdownAmount = 0;
		for (int i = 0; i < betting.length; i++) {
			if (betting[i][3] == 1) {
				showdownAmount++;
			}
		}
		int index = 0;
		int[] showdownPlayers = new int[showdownAmount];
		String[] showdownPlayerHands = new String[showdownAmount];
		for (int i = 0; i < betting.length; i++) {
			if (betting[i][3] == 1) {
				int playerCard1 = globalPlayerCards[i * 2];
				int playerCard2 = globalPlayerCards[i * 2 + 1];
				showdownPlayers[index] = i;
				showdownPlayerHands[index] = determineHandStrength(playerCard1, playerCard2, globalFlopCard1, globalFlopCard2, globalFlopCard3, globalTurnCard, globalRiverCard);
				index++;
			}
		}
		for (int i = 0; i < showdownPlayers.length; i++) {
			drawPlayerCards(g, showdownPlayers[i], globalPlayerCards);
		}

		String winningHand = determineWinner(showdownPlayerHands);
		int winnerAmount = 0;
		for (int i = 0; i < showdownPlayerHands.length; i++) {
			if(showdownPlayerHands[i].equals(winningHand)) {
				winnerAmount++;
			}
		}
		g.setColor(Color.BLACK);
		g.setFont(ARIAL_BIG);
		g.drawString("Winning Hand: " + winningHand, 210, 60);
		index = 0;
		int[] winningPlayers = new int[winnerAmount];
		for (int i = 0; i < showdownPlayerHands.length; i++) {
			if(showdownPlayerHands[i].equals(winningHand)) {
				winningPlayers[index] = showdownPlayers[i];
				index++;
			}
		}
		String winningPlayersDisplay = "Winner: ";
		for (int i = 0; i < winningPlayers.length; i++) {
			betting[winningPlayers[i]][0] += (pot / winningPlayers.length);
			winningPlayersDisplay += playerNames[winningPlayers[i]];
			winningPlayersDisplay += " ";
		}

		g.setColor(Color.CYAN);
		g.fillRect(0,400,150,36);
		g.setColor(Color.BLACK);
		g.drawString("Next Round", 9, 427);
		g.drawString(winningPlayersDisplay, 320, 427);
		pot = 0;
		winner = true;
	}
	
	// resets key variables for the next round
	public void resetRound() {
		int[] resetArray = {};
		drawnCards = resetArray;
		globalPlayerCards = new int[numPlayer * 2];
		for (int i = 0; i < globalPlayerCards.length; i++) {
			globalPlayerCards[i] = cardGenerator();
		}
		// if testing cases replace cardGenerator() with test values
		globalFlopCard1 = cardGenerator();
		globalFlopCard2 = cardGenerator();
		globalFlopCard3 = cardGenerator();
		globalTurnCard = cardGenerator();
		globalRiverCard = cardGenerator();
		for (int i = 0; i < betting.length; i++) {
			betting[i][3] = 1;
			betting[i][5] = 0;
			betting[i][1] = 0;
			if (betting[i][0] == 0) {
				betting[i][0] = 1000;
			}
		}
		player = 0;
		newRound = false;
		currentBet = 10;
	}
	
	// initializes the betting[][] array
	public void initializeBetting() {
		//This method sets up a 2D array to organize each player's chips and bets at the beginning. 
		//It returns nothing. 
		betting = new int[numPlayer][7];
		for (int i = 0; i < betting.length; i++) {
			betting[i][0] = 1000; //Total chips
			betting[i][1] = 0; //Total bet
			betting[i][3] = 1; //Fold
			betting[i][4] = 1; //Playing Status
			betting[i][5] = 0; //Action status
			betting[i][6] = i;
		}
		newGame = false;
	}
	
	public int [] playersEarnings (int numPlayer) {
		//This method reads in the # of players, and 
		//returns an array of the x and y coordinates of each player's earnings (graphics)
		int [] result = new int [numPlayer * 2];
		for (int i = 0; i < numPlayer*2; i++) {
			result [i] = playersEarnings [i];
		}
		return result;
	}

	public int [] playersBets (int numPlayer) {
		//This method reads in the # of players and returns an array of the x and y 
		//coordinates for each player's bets (graphics). 
		int [] bets = new int [numPlayer * 2];
		for (int i = 0; i < numPlayer*2; i++) {
			bets [i] = playersBets [i];
		}
		return bets;
	}
	public void drawPlayerCards(Graphics g, int i, int[] playerCards) {
		//This method draws the 2 cards for each player (graphics). It takes in the player and the array of their cards
		//and draws it on the game screen. This method is only called when it is that player's turn (their cards get
		//"flipped" over). This method returns nothing. 
		int playerCard1 = playerCards[i * 2];
		int playerCard2 = playerCards[i * 2 + 1];
		int x1 = playerCardCoordinates[i * 4];
		int y1 = playerCardCoordinates[i * 4 + 1];
		int x2 = playerCardCoordinates[i * 4 + 2];
		int y2 = playerCardCoordinates[i * 4 + 3];
		Image card1 = t.getImage(playerCard1 + ".gif");
		try { //Re-sizes the image
			BufferedImage bufferedImage = ImageIO.read(new File (playerCard1 + ".gif"));
			card1 = bufferedImage.getScaledInstance(40, 50, Image.SCALE_DEFAULT);
		} catch (IOException e) {
			e.printStackTrace();
		}
		Image card2 = t.getImage(playerCard2 + ".gif");
		try { //Re-sizes the image
			BufferedImage bufferedImage = ImageIO.read(new File (playerCard2 + ".gif"));
			card2 = bufferedImage.getScaledInstance(40, 50, Image.SCALE_DEFAULT);
		} catch (IOException e) {
			e.printStackTrace();
		}
		g.drawImage(card1, x1, y1, this);
		g.drawImage(card2, x2, y2, this);
	}

	public void drawCommunityCards(Graphics g, int x, int y, int card) {
		//This method draws the community cards onto the board. It takes in the x and y coordinates
		//of where to draw the card, as well as which card to draw. It returns nothing. 
		Image cardImage = t.getImage(card + ".gif");
		try { //Re-sizes the image
			BufferedImage bufferedImage = ImageIO.read(new File (card + ".gif"));
			cardImage = bufferedImage.getScaledInstance(40, 50, Image.SCALE_DEFAULT);
		} catch (IOException e) {
			e.printStackTrace();
		}
		g.drawImage(cardImage, x, y, this);
	}

	// set values for cardArray
	public int cardGenerator() {
		//This method generates a random card out of the deck of 52 possible cards. If the card has
		//already been drawn, it will draw another card. It returns the randomly generated card.
		int card;
		do {
			card = (int) (Math.random() * 52);
		} while (contains(drawnCards, card));
		int[] newDrawnCards = new int[drawnCards.length + 1];
		System.arraycopy(drawnCards, 0, newDrawnCards, 0, drawnCards.length);
		newDrawnCards[drawnCards.length] = card;
		drawnCards = newDrawnCards;
		return card;
	}

	// duplicate card check
	public static boolean contains(int[] array, int value) {
		for (int i = 0; i < array.length; i++) {
			if (array[i] == value) {
				return true;
			}
		}
		return false;
	}

	// overloaded duplicate card check for two arrays
	public static boolean contains(int[] array, int[] subArray) {
		for (int i = 0; i < subArray.length; i++) {
			boolean found = false;
			for (int j = 0; j < array.length; j++) {
				if (j == i) {
					found = true;
					break;
				}
			}
			if (!found) {
				return false;
			}
		}
		return true;
	}

	// removes a card value from the cards array
	public static int[] removeCard(int[] cards, int cardToRemove) {
		int count = 0;
		for (int i = 0; i < cards.length; i++) {
			if (cards[i] == cardToRemove) {
				count++;
			}
		}
		int[] updatedCards = new int[cards.length - (count)];
		int index = 0;
		for (int i = 0; i < cards.length; i++) {
			if (cards[i] != cardToRemove) {
				updatedCards[index++] = cards[i];
			}
		}
		return updatedCards;
	}

	public static int checkKicker1(int card1, int card2) {
		// This method takes in the two cards for each player and
		//checks the value of the higher kicker, which can be used to break ties. 
		int cardRank1 = card1 % 13;
		int cardRank2 = card2 % 13;
		return Math.max(cardRank1, cardRank2);
	}

	public static int checkKicker2(int card1, int card2) {
		// check value of lower kicker (same as above method)
		int cardRank1 = card1 % 13;
		int cardRank2 = card2 % 13;
		return Math.min(cardRank1, cardRank2);
	}

	public static String highCard (int card1, int card2, int flop1, int flop2, int flop3, int turn, int river) {
		// checks the value of the highest card out of the total cards in each game (5 community cards + 2 player cards)
		int[] cards = {card1 % 13, card2 % 13, flop1 % 13, flop2 % 13, flop3 % 13, turn % 13, river % 13};
		int max = -1;
		for (int i = 0; i < cards.length; i++) {
			if(cards[i] > max) {
				max = cards[i];
			}
		}
		return cardName[max] + " high";
	}

	public static int findPair(int[] cards) {
		//check for a pair given the array of 7 cards mentioned above. If there is a pair, 
		//the value of the pair will be returned. Otherwise, -1 is returned 
		for (int i = 0; i < cards.length - 1; i++) {
			for (int j = i + 1; j < cards.length; j++) {
				if (cards[i] == cards[j]) {
					return cards[i];
				}
			} 
		}
		return -1;
	}

	public static int checkTrips(int[]cards) {
		// check for trips (three of a kind). If it exists, the value of the triple will be returned. 
		//Otherwise, it returns -1. 
		for (int i = 0; i < cards.length; i++) {
			int count = 1;
			for (int j = i + 1; j < cards.length; j++) {
				if (cards[i] == cards[j]) {
					count ++;
					if (count == 3) {
						return cards[i];
					}
				}
			}
		}
		return -1;
	}

	public static int checkQuads(int[]cards) {
		// checks for quads (fours of a kind). Same as previous method. 
		for (int i = 0; i < cards.length; i++) {
			int count = 1;
			for (int j = i + 1; j < cards.length; j++) {
				if (cards[i] == cards[j]) {
					count ++;
					if (count == 4) {
						return cards[i];
					}
				}
			}
		}
		return -1;
	}

	public static String pair(int card1, int card2, int flop1, int flop2, int flop3, int turn, int river) {
		// Reads in the 7 cards and returns a String of either no pair, pair, or two pair
		int[] cards = {card1 % 13, card2 % 13, flop1 % 13, flop2 % 13, flop3 % 13, turn % 13, river % 13};
		int firstPair = findPair(cards);
		if (firstPair != -1) {
			int[] remainingCards = removeCard(cards, firstPair);
			int secondPair = findPair(remainingCards);
			if (secondPair != -1) {
				return "two pairs: " + cardName[firstPair] + " and " + cardName[secondPair];
			} else {
				return "pair of " + cardName[firstPair];
			}
		} else {
			return "no pair";
		}
	}

	public static String full(int card1, int card2, int flop1, int flop2, int flop3, int turn, int river) {
		// determines to display either no full house, trips, or full house
		int [] cards = {card1 % 13, card2 % 13, flop1 % 13, flop2 % 13, flop3 % 13, turn % 13, river % 13};
		int trips = checkTrips(cards);
		if (trips != -1) {
			int[] remainingCards = removeCard(cards, trips);
			int pair = findPair(remainingCards);
			if (pair != -1) {
				return cardName[trips] + " full of " + cardName[pair];
			} else {
				return "trip " + cardName[trips];
			}
		} else {
			return "no full house";
		}
	}

	public static String quads(int card1, int card2, int flop1, int flop2, int flop3, int turn, int river) {
		// determines to display either quads or no quads
		int[] cards = {card1 % 13, card2 % 13, flop1 % 13, flop2 % 13, flop3 % 13, turn % 13, river % 13};
		int quads = checkQuads(cards);
		if (quads != -1) {
			int[] remainingCards = removeCard(cards, quads);
			return "quad " + cardName[quads];
		}
		return "no quads";
	}

	public static int checkFlush(int[] cards) {
		// checks for flush (5 cards of the same suit). Returns -1 if there is no flush. 
		// 
		int[] originalCards = new int[cards.length];
		//Reorders the cards from largest to smallest. 
		for (int i = 0; i < cards.length - 1; i++) {
			for (int j = 0; j < cards.length - i - 1; j++) {
				if (cards[j] > cards[j + 1]) {
					int temp = cards[j];
					cards[j] = cards[j + 1];
					cards[j + 1] = temp;
					int tempOriginal = originalCards[j];
					originalCards[j] = originalCards[j + 1];
					originalCards[j + 1] = tempOriginal;
				}
			}
		}
		for (int i = 0; i < cards.length; i++) {
			originalCards[i] = cards[i] % 13; 
			cards[i] = cards[i] / 13;
		}
		int currentSuit = cards[0];
		int counter = 1;
		int flushRank = -1;

		for (int i = 1; i < cards.length; i++) {
			if (cards[i] == currentSuit) {
				counter++;
				if (counter == 5) {
					flushRank = originalCards[i];
				}
			} else {
				currentSuit = cards[i];
				counter = 1;
			}
		}
		return flushRank;
	}

	public static String flush(int card1, int card2, int flop1, int flop2, int flop3, int turn, int river) {
		// determines either to display flush or no flush (String)
		int[] cards = {card1, card2, flop1, flop2, flop3, turn, river};
		int flush = checkFlush(cards);
		if (flush != -1) {
			return cardName[flush] + " high flush";
		}
		return "no flush";
	}

	// checks for a straight or if there is a straight flush
	public static int[] checkStraight(int[] cards) {
		for (int i = 0; i < cards.length - 1; i++) {
			for (int j = 0; j < cards.length - i - 1; j++) {
				if (cards[j] % 13 > cards[j + 1] % 13) {
					int temp = cards[j];
					cards[j] = cards[j + 1];
					cards[j + 1] = temp;
				}
			}
		}
		int count = 1;
		int[] straightArray = new int[5];       
		for (int i = 0; i < cards.length - 1; i++) {
			if (cards[i] % 13 + 1 == cards[i + 1] % 13) {
				straightArray[count - 1] = cards[i] % 13;
				count++;
			} else if (cards[i] % 13 != cards[i + 1] % 13) {
				count = 1;
			}
			if (count == 5) {
				straightArray[count - 1] = cards[i + 1] % 13;
				return straightArray;
			}
		}
		int[] originalCards = cards;
		for (int i = 0; i < cards.length; i++) {
			cards[i] = cards[i]%13;
		}
		if (contains(cards, 0) && contains(cards, 1) && contains(cards, 2) && contains(cards, 3) && contains(cards, 12)) {
			for (int i = 0; i < cards.length; i++) {
				if(originalCards[i]%13 == 12) {
					straightArray[0] = originalCards[i];
				}
			}
			for (int i = 0; i < cards.length; i++) {
				if(originalCards[i]%13 == 0) {
					straightArray[1] = originalCards[i];
				}
			}
			for (int i = 0; i < cards.length; i++) {
				if(originalCards[i]%13 == 1) {
					straightArray[2] = originalCards[i];
				}
			}
			for (int i = 0; i < cards.length; i++) {
				if (originalCards[i]%13 == 2) {
					straightArray[3] = originalCards[i];
				}
			}
			for (int i = 0; i < cards.length; i++) {
				if (originalCards[i]%13 == 3) {
					straightArray[4] = originalCards[i];
				}
			}
			return straightArray;
		}
		int[] noStraight = {-1, -1, -1, -1, -1};
		return noStraight;
	}

	// determines either to display no straight, straight, or straight flush
	public static String straightFlush(int card1, int card2, int flop1, int flop2, int flop3, int turn, int river) {
		int[] cards = {card1, card2, flop1, flop2, flop3, turn, river};
		int[] straightArray = checkStraight(cards);
		if (straightArray[0] != -1) {
			int flush = checkFlush(cards);
			if (flush != -1) {
				return cardName[straightArray[4]] + " high straight flush";
			} else {
				return cardName[straightArray[4]] + " high straight";
			}
		} else {
			return "no straight";
		}
	}

	public static int determineRank(String playerHand) {
		// determine rank strength. Returns a number between 2-14, the higher the number, 
		//the stronger the strength. 
		int handRank;
		if (playerHand.indexOf("ace") != -1) {
			handRank = 14;
		} else if (playerHand.indexOf("king") != -1) {
			handRank = 13;
		} else if (playerHand.indexOf("queen") != -1) {
			handRank = 12;
		} else if (playerHand.indexOf("jack") != -1) {
			handRank = 11;
		} else if (playerHand.indexOf("ten") != -1) {
			handRank = 10;
		} else if (playerHand.indexOf("nine") != -1) {
			handRank = 9;
		} else if (playerHand.indexOf("eight") != -1) {
			handRank = 8;
		} else if (playerHand.indexOf("seven") != -1) {
			handRank = 7;
		} else if (playerHand.indexOf("six") != -1) {
			handRank = 6;
		} else if (playerHand.indexOf("five") != -1) {
			handRank = 5;
		} else if (playerHand.indexOf("four") != -1) {
			handRank = 4;
		} else if (playerHand.indexOf("three") != -1) {
			handRank = 3;
		} else {
			handRank = 2;
		}
		return handRank;
	}

	public static String determineHandStrength(int playerCard1, int playerCard2, int flopCard1, int flopCard2, int flopCard3, int turnCard, int riverCard) {

		// determine hand strength. Takes in the 7 cards and outputs a String of each player's hand strength. 
		String handStrength;
		String straightFlush = straightFlush(playerCard1, playerCard2, flopCard1, flopCard2, flopCard3, turnCard, riverCard);
		String quads = quads(playerCard1, playerCard2, flopCard1, flopCard2, flopCard3, turnCard, riverCard);
		String full = full(playerCard1, playerCard2, flopCard1, flopCard2, flopCard3, turnCard, riverCard);
		String flush = flush(playerCard1, playerCard2, flopCard1, flopCard2, flopCard3, turnCard, riverCard);
		String pair = pair(playerCard1, playerCard2, flopCard1, flopCard2, flopCard3, turnCard, riverCard);
		String highCard = highCard(playerCard1, playerCard2, flopCard1, flopCard2, flopCard3, turnCard, riverCard);
		if (straightFlush.equals("ace high straight flush")) {
			handStrength = "royal flush";
		} else if (straightFlush.indexOf("flush") != -1){
			handStrength = straightFlush;
		} else if (quads.indexOf("no") == -1) {
			handStrength = quads;
		} else if (full.indexOf("of") != -1) {
			handStrength = full;
		} else if (flush.indexOf("no") == -1) {
			handStrength = flush;
		} else if (straightFlush.indexOf("no") == -1) {
			handStrength = straightFlush;
		} else if (full.indexOf("trip") != -1) {
			handStrength = full;
		} else if (pair.indexOf("no") == -1) {
			handStrength = pair;
		} else {
			handStrength = highCard;
		}
		return handStrength;
	}

	// determine which player(s) has the winning hand
	public static String determineWinner(String[] playerHands) {
		int [][] playerHandValues = new int[playerHands.length][3];
		int highHand = -1;
		int highRank1 = -1;
		int highRank2 = -1;
		int winningHandIndex = 0;
		for (int i = 0; i < playerHands.length; i++) {
			String playerHand = playerHands[i];
			int playerHandStrength;
			int playerRank1 = 0;
			int playerRank2 = 0;
			if (playerHand.indexOf("royal flush") != -1){
				playerHandStrength = 10;
			} else if (playerHand.indexOf("straight flush") != -1) {
				playerHandStrength = 9;
				playerRank1 = determineRank(playerHand);
			} else if (playerHand.indexOf("quad") != -1){
				playerHandStrength = 8;
				playerRank1 = determineRank(playerHand);
			} else if (playerHand.indexOf("full") != -1) {
				playerHandStrength = 7;
				playerRank1 = determineRank(playerHand.substring(0,5));
				playerRank2 = determineRank(playerHand.substring(6));
			} else if (playerHand.indexOf("flush") != -1){
				playerHandStrength = 6;
				playerRank1 = determineRank(playerHand);
			} else if (playerHand.indexOf("straight") != -1) {
				playerHandStrength = 5;
				playerRank1 = determineRank(playerHand);
			} else if (playerHand.indexOf("trip") != -1) {
				playerHandStrength = 4;
				playerRank1 = determineRank(playerHand);
			} else if (playerHand.indexOf("two pair") != -1) {
				playerHandStrength = 3;
				playerRank1 = determineRank(playerHand.substring(10,15));
				playerRank2 = determineRank(playerHand.substring(16));
			} else if (playerHand.indexOf("pair") != -1) {
				playerHandStrength = 2;
				playerRank1 = determineRank(playerHand);
			} else {
				playerHandStrength = 1;
				playerRank1 = determineRank(playerHand);
			}
			playerHandValues[i][0] = playerHandStrength;
			playerHandValues[i][1] = playerRank1;
			playerHandValues[i][2] = playerRank2;
			if (playerHandStrength > highHand) {
				highHand = playerHandStrength;
				highRank1 = playerRank1;
				highRank2 = playerRank2;
				winningHandIndex = i;
			} else if (playerHandStrength == highHand) {
				if (playerRank1 > highRank1) {
					highRank1 = playerRank1;
					highRank2 = playerRank2;
					winningHandIndex = i;
				} else if (playerRank1 == highRank1) {
					if (playerRank2 > highRank2) {
						highRank2 = playerRank2;
						winningHandIndex = i;
					}
				}
			}

		}
		String winningHand = playerHands[winningHandIndex];
		return winningHand;
	}

	// checks if all bets are matched
	public static boolean betsMatched(int[][] betting, int bet) {
		boolean matched = true;
		for (int i = 0; i < betting.length; i++) {
			if(betting[i][3] == 1){
				if (betting[i][1] != bet) {
					matched = false;
				}
			}
		}
		return matched;
	}
	
	// checks if all bets have been matched
	public static boolean betsMatched(int[][] betting) {
		boolean matched = true;
		for (int i = 0; i < betting.length; i++) {
			if(betting[i][3] == 1){
				if (betting[i][1] != currentBet) {
					matched = false;
				}
			}
		}
		return matched;
	}

	// checks if there is a winner
	public static int checkWinner(int[][] betting) {
		int counter = 0;
		for (int i = 0; i < betting.length; i++) {
			if(betting[i][3] == 1) {
				counter++;
			}
		}
		if (counter == 1) {
			for (int i = 0; i < betting.length; i++) {
				if (betting[i][3] == 1) {
					return i;
				}
			}
		} 
		return -1;
	}

	// checks if all the players have had their action
	public static boolean checkActions (int[][] betting) {
		for (int i = 0; i < betting.length; i++) {
			if (betting[i][3] == 1) {
				if 	(betting[i][5] == 0) {
					return false;
				}
			}
		}
		return true;
	}


	// main
	public static void main(String[] args) {
		new Poker();
		JFrame frame = new JFrame("Poker Home Screen");
		backgroundMusic.setFramePosition(0);
		backgroundMusic.start();
		Poker myPanel = new Poker();
		frame.add(myPanel);
		frame.pack();
		frame.setVisible(true);

	}

	// if a mouse event (click) is detected, then this method is ran
	public void mouseClicked(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		mouseInput = true;
		mouseX = x;
		mouseY = y;	
		if (screen == 0) {
			if (x >= 190 && x <= 610 && y >= 505 && y <= 565) { // play button clicked
				screen++;
				repaint();
			} else if (x >= 190 && x <= 610 && y >= 595 && y <= 655) { // exit button clicked
				System.exit(0); 
			} else if (x >= 650 && x <= 800 && y >= 0 && y <= 50){ // about page button clicked
				screen = 3;
				repaint();
			} 
		}
		if (screen == 1) {
			if (x>=126 && x<=377 && y>=121 && y <=174) { //2 players button
				screen++;
				numPlayer = 2;
				try {
					playerNames ();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				repaint();

			}
			else if (x>=126 && x<=377 && y >=236 && y<=288) { //3 players button
				screen++;
				numPlayer = 3;
				try {
					playerNames ();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				repaint();
			}
			else if (x>=126 && x<=377 && y >=350 && y <=401) { //4 players
				screen++;
				numPlayer = 4;
				try {
					playerNames ();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				repaint();
			}
			else if (x>=126 && x<=377 && y >=464 && y <=517) { //5 players
				screen++;
				numPlayer = 5;
				try {
					playerNames ();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				repaint();
			}
			else if (x>=421 && x<=672 && y >=122 && y <=173) { //6 players
				screen++;
				numPlayer = 6;
				try {
					playerNames ();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				repaint();
			}
			else if (x>=421 && x<=672 && y >=236 && y <=288) { //7 players
				screen++;
				numPlayer = 7;
				try {
					playerNames ();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				repaint();
			}
			else if (x>=421 && x<=672 && y >=350 && y <=403) { //8 players
				screen++;
				numPlayer = 8;
				try {
					playerNames ();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				repaint();
			}
		}
		if (screen == 2) {
			if (x >= 0 && x <= 125 && y >= 35 && y <= 80) { //Back button. 
				screen = 0;
				repaint();
			} else if (x >= 655 && x <= 800 && y >= 25 && y <= 80) { //Exit button
				System.exit(0);
			} else if (x >= 68 && x <= 343 && y >= 443 && y <=474){ //Start turn button
				turn = true;
				repaint();
			} else if (x >= 458 && x <= 734 && y >= 443 && y <= 474 && turnAction != 0) { // end turn button and resets key variables
				turn = false;
				raise = false;
				customRaise = 0;
				betting[player][5] = 1;
				betting[player][0] = playerTotalChip;
				betting[player][1] = playerTotalBet;
				playerTotalChip = 0;
				playerTotalBet = 0;
				if (turnAction >= 3) {
					currentBet = raiseAmount;
				}
				turnAction = 0;
				do {
					player++;
					if (player >= numPlayer) {
						player = 0;
					}
				} while (betting[player][3] == 0);
				if(checkWinner(betting) != -1) {
					winner = true;
					System.out.println("Winner: Player " + (player + 1));
					for (int i = 0; i < betting.length; i++) {
						pot += betting[i][1];
						betting[i][1] = 0;
					}
					betting[player][0] += pot;
					System.out.println("Chips: " + betting[player][0]);
					pot = 0;
				} else if (betsMatched(betting) && checkActions(betting)) {
					for (int i = 0; i < betting.length; i++) {
						pot += betting[i][1];
						betting[i][1] = 0;
						betting[i][5] = 0;
					}
					bettingRound++;
					resetBets = true;
				}
				repaint();
			} else if (x >= 31 && x <= 305 && y >= 572 && y <= 602 && turn && !winner) { //Fold button
				if (currentBet == betting[player][1]) { //If the current bet is 0, the player can't fold.
					JOptionPane.showMessageDialog(myPanel, "Can not fold. Please check or raise");
					x = 30;
					y = 571;
				}
				turnAction = 1;
				raise = false;
				repaint();
			} else if (x >= 31 && x <= 305 && y >= 632 && y <= 662 && turn && !winner) { // check/call button
				turnAction = 2;
				raise = false;
				repaint();
			} else if (x >= 31 && x <= 305 && y >= 690 && y <= 720 && turn && !winner) { // raise/bet button
				raise = true;
				turnAction = 0;
				repaint();
			} else if (x >= 360 && x <= 550 && y >= 632 && y <= 664 && turn && raise && !winner) { // double the current bet button
				turnAction = 3;
				repaint();
			} else if (x >= 565 && x <= 755 && y >= 632 && y <= 664 && turn && raise && !winner) { // pot sized bet button
				turnAction = 4;
				repaint();
			} else if (x >= 360 && x <= 550 && y >= 690 && y <= 722 && turn && raise && !winner) { // all in button
				turnAction = 5;
				repaint();
			} else if (x >= 545 && x <= 755 && y >= 690 && y <= 722 && turn && raise && !winner) { // custom bet amount button
				turnAction = 6;
				customRaise = Integer.parseInt(JOptionPane.showInputDialog("Raise Amount: "));
				while (customRaise < 2*currentBet || customRaise <= 10) {
					JOptionPane.showMessageDialog (myPanel, "Invalid Input");
					customRaise = Integer.parseInt(JOptionPane.showInputDialog("Raise Amount: "));
				}
				repaint();
			} else if (x >= 0 && x <= 150 && y >= 400 && y <= 436 && winner) { // next round button
				bettingRound = 1;
				newRound = true;
				winner = false;
				repaint();
			}
		}
		// switches back to home screen
		if (screen == 3) {
			if (x >= 0 && x <= 150 && y >= 0 && y <= 50){
				screen = 0;
				repaint();
			}
		}
		
	}
	
	// other mouseEvent methods
	public void mousePressed(MouseEvent e) {
		try {
			AudioInputStream sound = AudioSystem.getAudioInputStream(new File ("end.wav"));
			soundEffect = AudioSystem.getClip();
			soundEffect.open(sound);
		}
		catch (Exception f) {
		}
		soundEffect.setFramePosition(0);
		soundEffect.start();
	}
	public void mouseReleased(MouseEvent e) {
	}
	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}
}