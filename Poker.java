// Poker ISU
// Zachary Li & Sarah Zhou
// 01-22-2024
// This is a recreation of the famous Texas Hold'em Poker.
// Blinds are 5/10 and every player starts with 1000 chips.
//
// This file is the screen and input side of the game. The rules live in Game.java,
// hand ranking lives in HandEvaluator.java, and each seat's state is a Player.

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;

public class Poker extends JPanel implements MouseListener, KeyListener {

	public static final int TURN_SECONDS = 30; // time each player has once they start their turn

	// screens (game states)
	private static final int HOME = 0;
	private static final int PLAYER_SELECT = 1;
	private static final int TABLE = 2;
	private static final int ABOUT = 3;

	private final Font ARIAL_BIG = new Font("Arial", Font.PLAIN, 25);
	private final Font ARIAL_SMALL = new Font("Arial", Font.PLAIN, 15);

	// where things are drawn on the table image, indexed by seat
	private static final int[] PLAYER_CARD_COORDS = {195, 111, 245, 111, 355, 111, 400, 111, 515, 111, 560, 111, 620, 205, 665, 205, 560, 300, 515, 300, 400, 300, 355, 300, 245, 300, 195, 300, 90, 205, 135, 205};
	private static final int[] COMMUNITY_CARD_COORDS = {290, 205, 335, 205, 380, 205, 425, 205, 470, 205};
	private static final int[] CHIP_BOX_COORDS = {194, 91, 354, 91, 514, 91, 620, 182, 512, 352, 354, 352, 194, 352, 89, 182};
	private static final int[] BET_BOX_COORDS = {215, 167, 375, 167, 535, 167, 639, 262, 530, 280, 375, 280, 215, 280, 108, 262};

	// sound
	private static Clip backgroundMusic;
	private static Clip soundEffect;
	private static boolean muted = false;

	private final Map<String, Image> imageCache = new HashMap<>();

	private int screen = HOME;
	private Game game;

	// the current player's turn on this shared screen
	private boolean turn = false;      // player has pressed Start Turn and can see their cards
	private int turnAction = 0;        // the action selected but not yet submitted (Game.FOLD etc.)
	private boolean raise = false;     // the Bet/Raise button has been pressed, showing raise options
	private int customRaise = 0;
	private final Timer turnTimer;
	private int secondsLeft;

	public Poker() {
		setPreferredSize(new Dimension(800, 750));
		setBackground(Color.WHITE);
		setFont(ARIAL_BIG);
		setFocusable(true);
		addMouseListener(this);
		addKeyListener(this);

		turnTimer = new Timer(1000, e -> tick());

		backgroundMusic = loadClip("gdmusic.wav");
		soundEffect = loadClip("end.wav");
	}

	private static Clip loadClip(String file) {
		try {
			AudioInputStream sound = AudioSystem.getAudioInputStream(new File(file));
			Clip clip = AudioSystem.getClip();
			clip.open(sound);
			return clip;
		} catch (Exception e) {
			return null;
		}
	}

	// Loads an image once and keeps the scaled copy, so repaints do not hit the disk.
	private Image image(String file, int width, int height) {
		String key = file + "@" + width + "x" + height;
		Image cached = imageCache.get(key);
		if (cached == null) {
			try {
				BufferedImage raw = ImageIO.read(new File(file));
				cached = raw.getScaledInstance(width, height, Image.SCALE_SMOOTH);
			} catch (IOException e) {
				e.printStackTrace();
				cached = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			}
			imageCache.put(key, cached);
		}
		return cached;
	}

	// ---------------------------------------------------------------- drawing

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (screen == HOME) {
			g.drawImage(image("Poker Menu.png", 800, 750), 0, 0, this);
		} else if (screen == PLAYER_SELECT) {
			g.drawImage(image("Player Screen.png", 800, 750), 0, 0, this);
		} else if (screen == TABLE && game != null) {
			gameScreen(g);
		} else if (screen == ABOUT) {
			g.drawImage(image("About Page.png", 800, 750), 0, 0, this);
		}
	}

	private void gameScreen(Graphics g) {
		g.drawImage(image("Poker Game.png", 800, 750), 0, 0, this);

		// cards: face down for everyone still in the hand, face up for the current player
		// during their turn and for everyone at a showdown
		Image faceDown = image("face down card.png", 40, 50);
		for (Player p : game.players) {
			if (!p.inHand()) {
				continue;
			}
			boolean reveal = (turn && p.seat == game.acting) || (game.handOver && game.showdownReached);
			int x1 = PLAYER_CARD_COORDS[p.seat * 4];
			int y1 = PLAYER_CARD_COORDS[p.seat * 4 + 1];
			int x2 = PLAYER_CARD_COORDS[p.seat * 4 + 2];
			int y2 = PLAYER_CARD_COORDS[p.seat * 4 + 3];
			if (reveal) {
				g.drawImage(image(p.cards[0] + ".gif", 40, 50), x1, y1, this);
				g.drawImage(image(p.cards[1] + ".gif", 40, 50), x2, y2, this);
			} else {
				g.drawImage(faceDown, x1, y1, this);
				g.drawImage(faceDown, x2, y2, this);
			}
		}

		// community cards
		int shown = game.communityCardsShown();
		for (int i = 0; i < shown; i++) {
			g.drawImage(image(game.community[i] + ".gif", 40, 50), COMMUNITY_CARD_COORDS[i * 2], COMMUNITY_CARD_COORDS[i * 2 + 1], this);
		}

		// each player's chips (with dealer / blind marker) and bet this round
		g.setFont(ARIAL_SMALL);
		for (Player p : game.players) {
			int cx = CHIP_BOX_COORDS[p.seat * 2];
			int cy = CHIP_BOX_COORDS[p.seat * 2 + 1];
			g.setColor(Color.WHITE);
			g.fillRect(cx, cy, 88, 14);
			g.setColor(Color.BLACK);
			if (p.eliminated) {
				g.drawString("OUT", cx + 20, cy + 12);
			} else {
				String marker = "";
				if (p.seat == game.dealer) {
					marker = "D";
				} else if (p.seat == game.smallBlindSeat) {
					marker = "SB";
				} else if (p.seat == game.bigBlindSeat) {
					marker = "BB";
				}
				g.drawString(marker, cx + 2, cy + 12);
				g.drawString("" + p.chips, cx + (marker.isEmpty() ? 20 : 28), cy + 12);
			}

			int bx = BET_BOX_COORDS[p.seat * 2];
			int by = BET_BOX_COORDS[p.seat * 2 + 1];
			g.setColor(Color.WHITE);
			g.fillRect(bx, by, 50, 14);
			g.setColor(Color.BLACK);
			String bet;
			if (p.eliminated) {
				bet = "-";
			} else if (p.folded) {
				bet = "fold";
			} else if (p.allIn && p.streetBet == 0) {
				bet = "all in";
			} else {
				bet = "" + p.streetBet;
			}
			g.drawString(bet, bx + 8, by + 12);
		}

		// pot
		g.setColor(Color.WHITE);
		g.fillRect(357, 259, 85, 20);
		g.setColor(Color.BLACK);
		g.drawString("" + game.pot, 380, 274);

		// back and exit buttons
		g.setFont(ARIAL_BIG);
		g.setColor(Color.CYAN);
		g.fillRect(0, 31, 148, 32);
		g.fillRect(662, 31, 148, 32);
		g.setColor(Color.BLACK);
		g.drawString("Back", 48, 55);
		g.drawString("Exit", 710, 55);

		// action button labels
		g.setColor(Color.WHITE);
		g.drawString("Fold", 85, 597);
		if (!turn) {
			g.drawString("Check/Call", 85, 657);
		}
		g.drawString(game.currentBet == 0 ? "Bet" : "Raise", 85, 716);

		if (game.handOver) {
			drawHandResult(g);
		} else {
			drawTurn(g);
		}
	}

	// Header, timer, and the preview of the action the current player has picked.
	private void drawTurn(Graphics g) {
		Player p = game.actingPlayer();
		if (p == null) {
			return;
		}
		g.setFont(ARIAL_BIG);
		g.setColor(Color.BLACK);
		g.drawString("Player: " + p.name, 50, 25);
		g.drawString("Chips: " + p.chips, 325, 25);
		if (!turn) {
			g.drawString("Press Start Turn", 31, 548);
			return;
		}

		if (game.street == Game.RIVER) {
			g.drawString("Hand: " + game.handDescription(p.seat), 500, 25);
		}
		g.setColor(secondsLeft <= 5 ? Color.RED : Color.BLACK);
		g.drawString("Time: " + secondsLeft, 31, 548);

		g.setColor(Color.WHITE);
		int call = game.callAmount(p);
		g.drawString(call == 0 ? "Check" : "Call " + call, 85, 657);

		g.setColor(Color.GREEN);
		if (raise) {
			g.fillRect(290, 696, 12, 22);
		}
		if (turnAction == Game.FOLD) {
			g.fillRect(290, 577, 12, 22);
		} else if (turnAction == Game.CHECK_CALL) {
			g.fillRect(290, 637, 12, 22);
		} else if (turnAction == Game.DOUBLE) {
			g.fillRect(533, 638, 12, 22);
		} else if (turnAction == Game.POT) {
			g.fillRect(735, 638, 12, 22);
		} else if (turnAction == Game.ALL_IN) {
			g.fillRect(533, 698, 12, 22);
		} else if (turnAction == Game.CUSTOM) {
			g.fillRect(735, 698, 12, 22);
		}

		if (turnAction != 0) {
			g.setColor(Color.BLACK);
			if (turnAction == Game.FOLD) {
				g.drawString("Current Bet: Fold", 450, 598);
			} else {
				int to = game.targetBet(turnAction, customRaise);
				String text = "Current Bet: " + to;
				if (game.chipsAfter(turnAction, customRaise) == 0) {
					text += " (all in)";
				}
				g.drawString(text, 450, 598);
			}
		}
	}

	// Winner announcement plus the Next Round / Main Menu button.
	private void drawHandResult(Graphics g) {
		g.setFont(ARIAL_BIG);
		g.setColor(Color.BLACK);
		g.drawString(game.winningHandText, 160, 60);

		g.setColor(Color.CYAN);
		g.fillRect(0, 400, 150, 36);
		g.setColor(Color.BLACK);
		if (game.gameOver) {
			Player champion = game.players[game.gameWinner];
			g.drawString("Main Menu", 9, 427);
			g.drawString("Game Over! " + champion.name + " wins with " + champion.chips + " chips", 160, 427);
		} else {
			g.drawString("Next Round", 9, 427);
			g.drawString(game.winnerText, 160, 427);
		}

		g.setFont(ARIAL_SMALL);
		int y = 530;
		for (String line : game.extraResultLines) {
			g.drawString(line, 31, y);
			y += 18;
		}
		g.setFont(ARIAL_BIG);
	}

	// ---------------------------------------------------------------- game flow

	private void startGame(int numPlayers) {
		String[] names = new String[numPlayers];
		for (int i = 0; i < numPlayers; i++) {
			String name = JOptionPane.showInputDialog(this, "Enter player " + (i + 1) + "'s name:");
			if (name == null || name.trim().isEmpty()) {
				name = "Player " + (i + 1);
			}
			names[i] = name.trim();
		}
		game = new Game(names);
		game.startHand();
		turn = false;
		turnAction = 0;
		raise = false;
		customRaise = 0;
		screen = TABLE;
	}

	private void leaveTable() {
		turnTimer.stop();
		game = null;
		turn = false;
		turnAction = 0;
		raise = false;
		screen = HOME;
	}

	private void startTurn() {
		turn = true;
		turnAction = 0;
		raise = false;
		customRaise = 0;
		secondsLeft = TURN_SECONDS;
		turnTimer.restart();
	}

	private void submitAction(int action) {
		turnTimer.stop();
		game.applyAction(action, customRaise);
		turn = false;
		turnAction = 0;
		raise = false;
		customRaise = 0;
		repaint();
	}

	// Called once a second while a turn is running. On zero the player checks if they
	// can, otherwise they fold.
	private void tick() {
		if (!turn || game == null || game.handOver) {
			turnTimer.stop();
			return;
		}
		secondsLeft--;
		if (secondsLeft <= 0) {
			Player p = game.actingPlayer();
			submitAction(game.callAmount(p) == 0 ? Game.CHECK_CALL : Game.FOLD);
			return;
		}
		repaint();
	}

	private void askCustomRaise() {
		Player p = game.actingPlayer();
		int min = Math.min(game.minRaiseTo(), game.maxTo(p));
		int max = game.maxTo(p);
		turnTimer.stop(); // do not let the clock run out while the dialog is open
		String input = JOptionPane.showInputDialog(this, "Raise to (min " + min + ", max " + max + "):");
		if (turn) {
			turnTimer.start();
		}
		if (input == null) {
			return;
		}
		try {
			int amount = Integer.parseInt(input.trim());
			if (!game.isValidCustom(amount)) {
				JOptionPane.showMessageDialog(this, "Invalid amount. Raise to between " + min + " and " + max + ".");
				return;
			}
			customRaise = amount;
			turnAction = Game.CUSTOM;
		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(this, "Please enter a whole number.");
		}
	}

	// ---------------------------------------------------------------- input

	private static boolean inside(int x, int y, int x1, int x2, int y1, int y2) {
		return x >= x1 && x <= x2 && y >= y1 && y <= y2;
	}

	public void mouseClicked(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		requestFocusInWindow();

		if (screen == HOME) {
			if (inside(x, y, 190, 610, 505, 565)) {          // play
				screen = PLAYER_SELECT;
			} else if (inside(x, y, 190, 610, 595, 655)) {   // exit
				System.exit(0);
			} else if (inside(x, y, 650, 800, 0, 50)) {      // hidden about button
				screen = ABOUT;
			}
		} else if (screen == PLAYER_SELECT) {
			int chosen = 0;
			if (inside(x, y, 126, 377, 121, 174)) {
				chosen = 2;
			} else if (inside(x, y, 126, 377, 236, 288)) {
				chosen = 3;
			} else if (inside(x, y, 126, 377, 350, 401)) {
				chosen = 4;
			} else if (inside(x, y, 126, 377, 464, 517)) {
				chosen = 5;
			} else if (inside(x, y, 421, 672, 122, 173)) {
				chosen = 6;
			} else if (inside(x, y, 421, 672, 236, 288)) {
				chosen = 7;
			} else if (inside(x, y, 421, 672, 350, 403)) {
				chosen = 8;
			}
			if (chosen > 0) {
				startGame(chosen);
			}
		} else if (screen == TABLE && game != null) {
			tableClick(x, y);
		} else if (screen == ABOUT) {
			if (inside(x, y, 0, 150, 0, 50)) {
				screen = HOME;
			}
		}
		repaint();
	}

	private void tableClick(int x, int y) {
		if (inside(x, y, 0, 125, 35, 80)) {                       // back
			leaveTable();
			return;
		}
		if (inside(x, y, 655, 800, 25, 80)) {                     // exit
			System.exit(0);
		}

		if (game.handOver) {
			if (inside(x, y, 0, 150, 400, 436)) {                 // next round / main menu
				if (game.gameOver) {
					leaveTable();
				} else {
					game.startHand();
					turn = false;
					turnAction = 0;
					raise = false;
				}
			}
			return;
		}

		Player p = game.actingPlayer();
		if (p == null) {
			return;
		}
		if (inside(x, y, 68, 343, 443, 474)) {                    // start turn
			if (!turn) {
				startTurn();
			}
			return;
		}
		if (!turn) {
			return;
		}
		if (inside(x, y, 458, 734, 443, 474)) {                   // end turn
			if (turnAction != 0) {
				submitAction(turnAction);
			}
		} else if (inside(x, y, 31, 305, 572, 602)) {             // fold
			if (game.callAmount(p) == 0) {
				JOptionPane.showMessageDialog(this, "Nothing to call. Check or bet instead.");
			} else {
				turnAction = Game.FOLD;
				raise = false;
			}
		} else if (inside(x, y, 31, 305, 632, 662)) {             // check / call
			turnAction = Game.CHECK_CALL;
			raise = false;
		} else if (inside(x, y, 31, 305, 690, 720)) {             // bet / raise
			raise = true;
			turnAction = 0;
		} else if (raise && inside(x, y, 360, 550, 632, 664)) {   // double
			turnAction = Game.DOUBLE;
		} else if (raise && inside(x, y, 565, 755, 632, 664)) {   // pot
			turnAction = Game.POT;
		} else if (raise && inside(x, y, 360, 550, 690, 722)) {   // all in
			turnAction = Game.ALL_IN;
		} else if (raise && inside(x, y, 555, 755, 690, 722)) {   // custom
			askCustomRaise();
		}
	}

	public void mousePressed(MouseEvent e) {
		if (soundEffect != null && !muted) {
			soundEffect.setFramePosition(0);
			soundEffect.start();
		}
	}

	public void mouseReleased(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	// M toggles the music and click sound on and off.
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_M) {
			muted = !muted;
			if (backgroundMusic != null) {
				if (muted) {
					backgroundMusic.stop();
				} else {
					backgroundMusic.loop(Clip.LOOP_CONTINUOUSLY);
				}
			}
		}
	}

	public void keyReleased(KeyEvent e) {
	}

	public void keyTyped(KeyEvent e) {
	}

	// ---------------------------------------------------------------- main

	public static void main(String[] args) {
		Poker panel = new Poker();
		JFrame frame = new JFrame("Poker Home Screen");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(panel);
		frame.pack();
		frame.setResizable(false);
		frame.setVisible(true);
		panel.requestFocusInWindow();
		if (backgroundMusic != null) {
			backgroundMusic.setFramePosition(0);
			backgroundMusic.loop(Clip.LOOP_CONTINUOUSLY);
		}
	}
}
