// TableScreen
// The poker table: seats, cards, pot, the action panel for the current player, the
// pass-the-device overlay between turns, and the result card at the end of a hand.
// All rules come from Game; this class only draws state and sends actions.

package poker;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.KeyEvent;

public class TableScreen implements Screen {

	private final Poker host;
	private final Game game;
	private TableLayout layout = new TableLayout(1000, 760, 2);

	// turn state for the player currently holding the device
	private boolean turn = false;
	private int turnAction = 0;
	private boolean raiseOpen = false;
	private int customRaise = 0;
	private int secondsLeft = Poker.TURN_SECONDS;

	// top bar
	private final UiButton backButton;
	private final UiButton muteButton;
	private final UiButton exitButton;
	// overlays
	private final UiButton startButton;
	private final UiButton nextButton;
	// action panel
	private final UiButton foldButton;
	private final UiButton callButton;
	private final UiButton raiseButton;
	private final UiButton minButton;
	private final UiButton doubleButton;
	private final UiButton potButton;
	private final UiButton allInButton;
	private final UiButton confirmButton;
	private final UiSlider slider = new UiSlider();
	private final UiTextField amountField = new UiTextField("");

	public TableScreen(Poker host, String[] names) {
		this(host, new Game(names));
		game.startHand();
	}

	// Used by tests to supply a prepared game.
	public TableScreen(Poker host, Game game) {
		this.host = host;
		this.game = game;

		backButton = new UiButton("Back", UiButton.Style.SECONDARY, () -> {
			host.stopClock();
			host.show(new MenuScreen(host));
		});
		muteButton = new UiButton("Mute", UiButton.Style.GHOST, host::toggleMute);
		exitButton = new UiButton("Exit", UiButton.Style.GHOST, host::quit);

		startButton = new UiButton("Start my turn", UiButton.Style.PRIMARY, this::beginTurn);
		nextButton = new UiButton("Next Round", UiButton.Style.PRIMARY, this::nextRound);

		foldButton = new UiButton("Fold", UiButton.Style.DANGER, () -> select(Game.FOLD));
		callButton = new UiButton("Check", UiButton.Style.SECONDARY, () -> select(Game.CHECK_CALL));
		raiseButton = new UiButton("Raise", UiButton.Style.SECONDARY, this::toggleRaise);
		minButton = new UiButton("Min", UiButton.Style.SECONDARY, () -> selectCustom(Math.min(game.minRaiseTo(), game.maxTo(game.actingPlayer()))));
		doubleButton = new UiButton("2×", UiButton.Style.SECONDARY, () -> select(Game.DOUBLE));
		potButton = new UiButton("Pot", UiButton.Style.SECONDARY, () -> select(Game.POT));
		allInButton = new UiButton("All In", UiButton.Style.SECONDARY, () -> select(Game.ALL_IN));
		confirmButton = new UiButton("Confirm", UiButton.Style.PRIMARY, this::confirm);

		amountField.numeric = true;
		amountField.maxLength = 7;
		amountField.placeholder = "amount";
		amountField.onChange = () -> {
			String typed = amountField.value();
			if (!typed.isEmpty()) {
				customRaise = Integer.parseInt(typed);
				turnAction = Game.CUSTOM;
				slider.setValue(customRaise);
			}
		};
		slider.onChange = () -> selectCustom(slider.value);
	}

	// ---------------------------------------------------------------- turn flow

	public void beginTurn() {
		if (game.handOver || game.actingPlayer() == null || turn) {
			return;
		}
		turn = true;
		turnAction = 0;
		raiseOpen = false;
		customRaise = 0;
		amountField.setValue("");
		amountField.focused = false;
		secondsLeft = Poker.TURN_SECONDS;
		Player p = game.actingPlayer();
		slider.setRange(Math.min(game.minRaiseTo(), game.maxTo(p)), game.maxTo(p));
		slider.setValue(slider.min);
		host.startClock();
	}

	private void select(int action) {
		if (!turn) {
			return;
		}
		if (action == Game.FOLD && game.callAmount(game.actingPlayer()) == 0) {
			return; // nothing to fold to; the button is disabled anyway
		}
		turnAction = action;
		if (action == Game.FOLD || action == Game.CHECK_CALL) {
			raiseOpen = false;
			amountField.focused = false;
		} else {
			slider.setValue(game.targetBet(action, customRaise));
		}
	}

	private void selectCustom(int amount) {
		if (!turn) {
			return;
		}
		customRaise = amount;
		turnAction = Game.CUSTOM;
		slider.setValue(amount);
		amountField.setValue("" + amount);
	}

	private void toggleRaise() {
		if (!turn) {
			return;
		}
		raiseOpen = !raiseOpen;
		if (raiseOpen) {
			if (turnAction == Game.FOLD || turnAction == Game.CHECK_CALL) {
				turnAction = 0;
			}
		} else {
			if (turnAction >= Game.DOUBLE) {
				turnAction = 0;
			}
			amountField.focused = false;
		}
	}

	private boolean actionReady() {
		if (turnAction == 0) {
			return false;
		}
		if (turnAction == Game.CUSTOM) {
			return game.isValidCustom(customRaise);
		}
		return true;
	}

	private void confirm() {
		if (!turn || !actionReady()) {
			return;
		}
		submit(turnAction);
	}

	private void submit(int action) {
		host.stopClock();
		game.applyAction(action, customRaise);
		turn = false;
		turnAction = 0;
		raiseOpen = false;
		customRaise = 0;
		amountField.focused = false;
	}

	private void nextRound() {
		if (game.gameOver) {
			host.show(new MenuScreen(host));
			return;
		}
		game.startHand();
		turn = false;
		turnAction = 0;
		raiseOpen = false;
	}

	public void tick() {
		if (!turn || game.handOver) {
			host.stopClock();
			return;
		}
		secondsLeft--;
		if (secondsLeft <= 0) {
			Player p = game.actingPlayer();
			submit(game.callAmount(p) == 0 ? Game.CHECK_CALL : Game.FOLD);
		}
	}

	// test hooks
	public Game game() {
		return game;
	}

	public void debugPending(int action, int custom, boolean openRaise, int seconds) {
		turnAction = action;
		customRaise = custom;
		raiseOpen = openRaise;
		secondsLeft = seconds;
		if (action == Game.CUSTOM) {
			amountField.setValue("" + custom);
			slider.setValue(custom);
		}
	}

	// ---------------------------------------------------------------- layout

	public void layout(int w, int h) {
		layout = new TableLayout(w, h, game.players.length);
		Rectangle bar = layout.topBar;
		backButton.setBounds(16, bar.y + 8, 84, 32);
		exitButton.setBounds(w - 16 - 70, bar.y + 8, 70, 32);
		muteButton.setBounds(w - 16 - 70 - 8 - 76, bar.y + 8, 76, 32);
		muteButton.label = host.isMuted() ? "Unmute" : "Mute";

		Rectangle panel = layout.actionPanel;
		int rowY = panel.y + 46;
		int bh = 40;
		foldButton.setBounds(panel.x + 16, rowY, 100, bh);
		callButton.setBounds(panel.x + 16 + 100 + 10, rowY, 150, bh);
		raiseButton.setBounds(panel.x + 16 + 100 + 10 + 150 + 10, rowY, 120, bh);
		confirmButton.setBounds(panel.x + panel.width - 16 - 150, rowY, 150, bh);

		int row2Y = panel.y + 98;
		int qx = panel.x + 16;
		int qw = 64;
		minButton.setBounds(qx, row2Y, qw, 34);
		doubleButton.setBounds(qx + (qw + 8), row2Y, qw, 34);
		potButton.setBounds(qx + (qw + 8) * 2, row2Y, qw, 34);
		allInButton.setBounds(qx + (qw + 8) * 3, row2Y, qw, 34);
		int sliderX = qx + (qw + 8) * 4 + 12;
		int fieldW = 96;
		int fieldX = panel.x + panel.width - 16 - fieldW;
		slider.setBounds(sliderX, row2Y, Math.max(60, fieldX - 20 - sliderX), 34);
		amountField.setBounds(fieldX, row2Y, fieldW, 34);

		startButton.setBounds(panel.x + panel.width - 16 - 200, panel.y + panel.height / 2 - 23, 200, 46);
		nextButton.setBounds(panel.x + panel.width - 16 - 200, panel.y + panel.height / 2 - 23, 200, 46);
	}

	// ---------------------------------------------------------------- painting

	public void paint(Graphics2D g, int w, int h) {
		g.setColor(Theme.BG);
		g.fillRect(0, 0, w, h);
		paintTable(g);
		paintCenter(g);
		for (Player p : game.players) {
			paintSeat(g, p);
		}
		paintTopBar(g, w);
		if (game.handOver) {
			paintResult(g);
		} else if (turn) {
			paintActionPanel(g);
		} else {
			paintHandoff(g);
		}
	}

	private void paintTopBar(Graphics2D g, int w) {
		g.setColor(Theme.BG_LIGHT);
		g.fillRect(0, 0, w, layout.topBar.height);
		backButton.paint(g);
		muteButton.paint(g);
		exitButton.paint(g);
		g.setFont(Theme.BOLD);
		g.setColor(Theme.TEXT);
		Theme.centerText(g, "Hand " + game.handsPlayed + "   ·   Blinds " + Game.SMALL_BLIND + "/" + Game.BIG_BLIND, w / 2, layout.topBar.height / 2);
	}

	private void paintTable(Graphics2D g) {
		int cx = layout.centerX;
		int cy = layout.centerY;
		int rx = layout.tableRx;
		int ry = layout.tableRy;
		int rail = (int) (16 * layout.scale);
		g.setColor(Theme.RAIL_EDGE);
		g.fillOval(cx - rx - rail - 2, cy - ry - rail - 2, (rx + rail + 2) * 2, (ry + rail + 2) * 2);
		g.setColor(Theme.RAIL);
		g.fillOval(cx - rx - rail, cy - ry - rail, (rx + rail) * 2, (ry + rail) * 2);
		g.setColor(Theme.FELT_DARK);
		g.fillOval(cx - rx, cy - ry, rx * 2, ry * 2);
		g.setColor(Theme.FELT);
		g.fillOval(cx - rx + 6, cy - ry + 6, rx * 2 - 12, ry * 2 - 12);
		g.setColor(Theme.alpha(Color.WHITE, 28));
		Stroke old = g.getStroke();
		g.setStroke(new BasicStroke(2f));
		g.drawOval(cx - (int) (rx * 0.72), cy - (int) (ry * 0.62), (int) (rx * 1.44), (int) (ry * 1.24));
		g.setStroke(old);
	}

	private void paintCenter(Graphics2D g) {
		int shown = game.communityCardsShown();
		for (int i = 0; i < 5; i++) {
			Rectangle r = layout.communitySlot[i];
			if (i < shown) {
				CardPainter.paintFace(g, game.community[i], r.x, r.y, r.width, r.height);
			} else {
				CardPainter.paintSlot(g, r.x, r.y, r.width, r.height);
			}
		}
		Rectangle pill = layout.potPill;
		Theme.roundRect(g, pill.x, pill.y, pill.width, pill.height, pill.height, Theme.alpha(Color.BLACK, 110), Theme.alpha(Color.WHITE, 50));
		g.setFont(Theme.BOLD);
		g.setColor(Theme.ACCENT);
		int streetBets = 0;
		for (Player p : game.players) {
			streetBets += p.streetBet;
		}
		String potText = "Pot " + game.pot + (streetBets > 0 ? "  +" + streetBets : "");
		Theme.fitCenterText(g, potText, pill.x + pill.width / 2, pill.y + pill.height / 2, pill.width - 16);

		g.setFont(Theme.SMALL);
		g.setColor(Theme.alpha(Color.WHITE, 150));
		Theme.centerText(g, streetName(), layout.centerX, layout.communitySlot[0].y - (int) (12 * layout.scale));
	}

	private String streetName() {
		switch (game.street) {
			case Game.PREFLOP: return "Pre-flop";
			case Game.FLOP: return "Flop";
			case Game.TURN: return "Turn";
			case Game.RIVER: return "River";
			default: return "Showdown";
		}
	}

	private void paintSeat(Graphics2D g, Player p) {
		int seat = p.seat;
		Rectangle plate = layout.plate[seat];
		boolean active = !game.handOver && seat == game.acting;
		boolean reveal = (turn && active) || (game.handOver && game.showdownReached);

		// cards
		if (p.inHand()) {
			Point c1 = layout.card1[seat];
			Point c2 = layout.card2[seat];
			if (reveal) {
				CardPainter.paintFace(g, p.cards[0], c1.x, c1.y, layout.seatCardW, layout.seatCardH);
				CardPainter.paintFace(g, p.cards[1], c2.x, c2.y, layout.seatCardW, layout.seatCardH);
			} else {
				CardPainter.paintBack(g, c1.x, c1.y, layout.seatCardW, layout.seatCardH);
				CardPainter.paintBack(g, c2.x, c2.y, layout.seatCardW, layout.seatCardH);
			}
		}

		// bet marker
		if (p.streetBet > 0 && !game.handOver) {
			Point b = layout.betPoint[seat];
			g.setFont(Theme.SMALL);
			String bet = "" + p.streetBet;
			int tw = Theme.textWidth(g, bet);
			int pw = tw + 30;
			int ph = 20;
			Theme.roundRect(g, b.x - pw / 2, b.y - ph / 2, pw, ph, ph, Theme.alpha(Color.BLACK, 120), null);
			g.setColor(Theme.ACCENT);
			g.fillOval(b.x - pw / 2 + 5, b.y - 5, 10, 10);
			g.setColor(Theme.TEXT);
			g.drawString(bet, b.x - pw / 2 + 20, b.y + 4);
		}

		// plate
		Color fill = Theme.PLATE;
		Color border = Theme.PLATE_BORDER;
		Color nameColor = Theme.TEXT;
		Color chipColor = Theme.ACCENT;
		if (p.eliminated) {
			fill = Theme.alpha(Theme.PLATE, 120);
			border = Theme.alpha(Theme.PLATE_BORDER, 120);
			nameColor = Theme.TEXT_DIM;
			chipColor = Theme.TEXT_DIM;
		} else if (p.folded) {
			fill = Theme.alpha(Theme.PLATE, 140);
			nameColor = Theme.TEXT_DIM;
			chipColor = Theme.TEXT_DIM;
		}
		if (active) {
			border = Theme.ACCENT;
		}
		Theme.roundRect(g, plate.x, plate.y, plate.width, plate.height, Theme.RADIUS, fill, border, active ? 2.5f : 1f);

		int textLeft = plate.x + 12;
		int textRight = plate.x + plate.width - 12;
		g.setFont(Theme.BOLD);
		g.setColor(nameColor);
		Theme.fitText(g, p.name, textLeft, plate.y + plate.height / 2 - 3, textRight - textLeft - (active && turn ? 30 : 0));
		g.setFont(Theme.SMALL);
		g.setColor(chipColor);
		String status = p.eliminated ? "OUT" : (p.allIn && !game.handOver ? "ALL IN" : p.chips + " chips");
		Theme.fitText(g, status, textLeft, plate.y + plate.height - 8, textRight - textLeft - (active && turn ? 30 : 0));

		// dealer button and blind tags
		if (!p.eliminated) {
			int tagX = plate.x + plate.width - 10;
			int tagY = plate.y - 8;
			if (seat == game.dealer) {
				g.setColor(Theme.ACCENT);
				g.fillOval(tagX - 10, tagY - 10, 22, 22);
				g.setColor(Theme.TEXT_DARK);
				g.setFont(Theme.BOLD);
				Theme.centerText(g, "D", tagX + 1, tagY + 1);
			} else if (seat == game.smallBlindSeat || seat == game.bigBlindSeat) {
				String tag = seat == game.smallBlindSeat ? "SB" : "BB";
				g.setFont(Theme.SMALL);
				int tw = Theme.textWidth(g, tag) + 12;
				Theme.roundRect(g, tagX - tw + 6, tagY - 9, tw, 18, 18, Theme.CALL, null);
				g.setColor(Theme.TEXT);
				Theme.centerText(g, tag, tagX - tw / 2 + 6, tagY);
			}
		}
		if (p.folded && !p.eliminated) {
			g.setFont(Theme.SMALL);
			g.setColor(Theme.TEXT_DIM);
			String fold = "folded";
			g.drawString(fold, textRight - Theme.textWidth(g, fold), plate.y + plate.height - 8);
		}

		// turn clock on the active seat
		if (active && turn) {
			int r = 13;
			int ccx = plate.x + plate.width - 20;
			int ccy = plate.y + plate.height / 2;
			g.setColor(Theme.alpha(Color.BLACK, 100));
			g.fillOval(ccx - r, ccy - r, r * 2, r * 2);
			Stroke old = g.getStroke();
			g.setStroke(new BasicStroke(3f));
			g.setColor(secondsLeft <= 5 ? Theme.DANGER : Theme.ACCENT);
			int sweep = (int) Math.round(360.0 * secondsLeft / Poker.TURN_SECONDS);
			g.drawArc(ccx - r + 2, ccy - r + 2, r * 2 - 4, r * 2 - 4, 90, -sweep);
			g.setStroke(old);
			g.setFont(Theme.SMALL);
			g.setColor(Theme.TEXT);
			Theme.centerText(g, "" + secondsLeft, ccx, ccy);
		}
	}

	private void paintActionPanel(Graphics2D g) {
		Player p = game.actingPlayer();
		Rectangle panel = layout.actionPanel;
		Theme.roundRect(g, panel.x, panel.y, panel.width, panel.height, Theme.RADIUS, Theme.BG_LIGHT, Theme.PLATE_BORDER);

		// header
		g.setFont(Theme.LARGE);
		g.setColor(Theme.TEXT);
		Theme.fitText(g, p.name, panel.x + 16, panel.y + 28, 240);
		g.setFont(Theme.BODY);
		g.setColor(Theme.TEXT_DIM);
		String info = p.chips + " chips";
		if (game.callAmount(p) > 0) {
			info += "   ·   " + game.callAmount(p) + " to call";
		}
		if (game.street >= Game.FLOP) {
			info += "   ·   " + game.handDescription(p.seat);
		}
		Theme.fitText(g, info, panel.x + 16 + 250, panel.y + 27, panel.width - 16 - 250 - 16 - 110);
		g.setFont(Theme.LARGE);
		g.setColor(secondsLeft <= 5 ? Theme.DANGER : Theme.TEXT);
		String time = secondsLeft + "s";
		g.drawString(time, panel.x + panel.width - 16 - Theme.textWidth(g, time), panel.y + 28);

		// row 1
		int call = game.callAmount(p);
		foldButton.enabled = call > 0;
		callButton.label = call == 0 ? "Check" : "Call " + call;
		raiseButton.label = game.currentBet == 0 ? "Bet" : "Raise";
		raiseButton.enabled = game.maxTo(p) > game.currentBet;
		raiseButton.selected = raiseOpen;
		foldButton.selected = turnAction == Game.FOLD;
		callButton.selected = turnAction == Game.CHECK_CALL;
		confirmButton.enabled = actionReady();
		foldButton.paint(g);
		callButton.paint(g);
		raiseButton.paint(g);
		confirmButton.paint(g);

		// row 2: raise controls
		if (raiseOpen) {
			minButton.selected = turnAction == Game.CUSTOM && customRaise == Math.min(game.minRaiseTo(), game.maxTo(p));
			doubleButton.selected = turnAction == Game.DOUBLE;
			potButton.selected = turnAction == Game.POT;
			allInButton.selected = turnAction == Game.ALL_IN;
			minButton.paint(g);
			doubleButton.paint(g);
			potButton.paint(g);
			allInButton.paint(g);
			slider.paint(g);
			amountField.paint(g);
		}

		// preview line
		g.setFont(Theme.BODY);
		int previewY = panel.y + panel.height - 14;
		if (turnAction == 0) {
			g.setColor(Theme.TEXT_DIM);
			g.drawString(raiseOpen ? "Pick an amount, then Confirm." : "Choose an action, then Confirm.", panel.x + 16, previewY);
		} else if (turnAction == Game.FOLD) {
			g.setColor(Theme.DANGER);
			g.drawString("Fold this hand.", panel.x + 16, previewY);
		} else if (turnAction == Game.CUSTOM && !game.isValidCustom(customRaise)) {
			g.setColor(Theme.DANGER);
			g.drawString("Amount must be between " + Math.min(game.minRaiseTo(), game.maxTo(p)) + " and " + game.maxTo(p) + ".", panel.x + 16, previewY);
		} else {
			int to = game.targetBet(turnAction, customRaise);
			int left = game.chipsAfter(turnAction, customRaise);
			String verb = turnAction == Game.CHECK_CALL ? (call == 0 ? "Check" : "Call to") : (game.currentBet == 0 ? "Bet" : "Raise to");
			String text = verb + (turnAction == Game.CHECK_CALL && call == 0 ? "" : " " + to) + "   ·   " + left + " chips left" + (left == 0 ? "   (all in)" : "");
			g.setColor(Theme.TEXT);
			g.drawString(text, panel.x + 16, previewY);
		}
	}

	// Between turns: the bottom panel tells the next player to take the device.
	private void paintHandoff(Graphics2D g) {
		Player p = game.actingPlayer();
		if (p == null) {
			return;
		}
		Rectangle panel = layout.actionPanel;
		Theme.roundRect(g, panel.x, panel.y, panel.width, panel.height, Theme.RADIUS, Theme.BG_LIGHT, Theme.PLATE_BORDER);
		int textW = panel.width - 16 - 200 - 48;
		g.setFont(Theme.TITLE);
		g.setColor(Theme.ACCENT);
		Theme.fitText(g, p.name + "'s turn", panel.x + 24, panel.y + 52, textW);
		g.setFont(Theme.BODY);
		g.setColor(Theme.TEXT);
		Theme.fitText(g, "Pass the device to " + p.name + ", then press Start.", panel.x + 24, panel.y + 86, textW);
		g.setFont(Theme.SMALL);
		g.setColor(Theme.TEXT_DIM);
		Theme.fitText(g, "Cards stay hidden until the turn starts. Enter also starts the turn.", panel.x + 24, panel.y + 112, textW);
		startButton.paint(g);
	}

	// End of a hand: the bottom panel shows who won what; the table above stays visible.
	private void paintResult(Graphics2D g) {
		Rectangle panel = layout.actionPanel;
		Theme.roundRect(g, panel.x, panel.y, panel.width, panel.height, Theme.RADIUS, Theme.BG_LIGHT, Theme.ACCENT_DARK);
		int textW = panel.width - 16 - 200 - 48;
		int x = panel.x + 24;

		g.setFont(Theme.TITLE);
		g.setColor(Theme.ACCENT);
		String title = game.gameOver ? "Game Over" : (game.showdownReached ? "Showdown" : "Hand Over");
		g.drawString(title, x, panel.y + 40);

		g.setFont(Theme.LARGE);
		g.setColor(Theme.TEXT);
		int y = panel.y + 72;
		if (game.gameOver) {
			Player champion = game.players[game.gameWinner];
			Theme.fitText(g, champion.name + " wins the game with " + champion.chips + " chips", x, y, textW);
		} else {
			Theme.fitText(g, game.winnerText, x, y, textW);
			y += 26;
			g.setFont(Theme.BODY);
			g.setColor(Theme.TEXT_DIM);
			Theme.fitText(g, game.winningHandText, x, y, textW);
			y += 22;
			g.setFont(Theme.SMALL);
			for (String line : game.extraResultLines) {
				if (y > panel.y + panel.height - 10) {
					break;
				}
				Theme.fitText(g, line, x, y, textW);
				y += 18;
			}
		}
		nextButton.label = game.gameOver ? "Main Menu" : "Next Round";
		nextButton.paint(g);
	}

	// ---------------------------------------------------------------- input

	public void mousePressed(int x, int y) {
		if (backButton.click(x, y) || muteButton.click(x, y) || exitButton.click(x, y)) {
			return;
		}
		if (game.handOver) {
			nextButton.click(x, y);
			return;
		}
		if (!turn) {
			startButton.click(x, y);
			return;
		}
		if (foldButton.click(x, y) || callButton.click(x, y) || raiseButton.click(x, y) || confirmButton.click(x, y)) {
			return;
		}
		if (raiseOpen) {
			if (minButton.click(x, y) || doubleButton.click(x, y) || potButton.click(x, y) || allInButton.click(x, y)) {
				amountField.focused = false;
				return;
			}
			if (slider.press(x, y)) {
				amountField.focused = false;
				return;
			}
			amountField.click(x, y);
		}
	}

	public void mouseDragged(int x, int y) {
		if (turn && raiseOpen) {
			slider.drag(x);
		}
	}

	public void mouseReleased(int x, int y) {
		slider.release();
	}

	public void mouseMoved(int x, int y) {
		UiButton[] all = {backButton, muteButton, exitButton, startButton, nextButton, foldButton, callButton, raiseButton, minButton, doubleButton, potButton, allInButton, confirmButton};
		for (UiButton b : all) {
			b.updateHover(x, y);
		}
	}

	public boolean keyTyped(char c) {
		if (turn && raiseOpen && amountField.keyTyped(c)) {
			return true;
		}
		return false;
	}

	public boolean keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ENTER) {
			if (game.handOver) {
				nextRound();
			} else if (!turn) {
				beginTurn();
			} else {
				confirm();
			}
			return true;
		}
		return turn && raiseOpen && amountField.focused;
	}
}
