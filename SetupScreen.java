// SetupScreen
// Pick how many players (2 to 8) and type their names, all on one screen.

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public class SetupScreen implements Screen {

	private static final int MIN_PLAYERS = 2;
	private static final int MAX_PLAYERS = 8;

	private final Poker host;
	private final UiButton backButton;
	private final UiButton fewerButton;
	private final UiButton moreButton;
	private final UiButton startButton;
	private final List<UiTextField> nameFields = new ArrayList<>();
	private int players = 4;
	private int stepperX;
	private int stepperY;

	public SetupScreen(Poker host) {
		this.host = host;
		backButton = new UiButton("Back", UiButton.Style.SECONDARY, () -> host.show(new MenuScreen(host)));
		fewerButton = new UiButton("−", UiButton.Style.SECONDARY, () -> setPlayers(players - 1));
		moreButton = new UiButton("+", UiButton.Style.SECONDARY, () -> setPlayers(players + 1));
		startButton = new UiButton("Start Game", UiButton.Style.PRIMARY, this::start);
		for (int i = 0; i < MAX_PLAYERS; i++) {
			UiTextField field = new UiTextField("");
			field.placeholder = "Player " + (i + 1);
			nameFields.add(field);
		}
	}

	private void setPlayers(int n) {
		players = Math.max(MIN_PLAYERS, Math.min(MAX_PLAYERS, n));
		fewerButton.enabled = players > MIN_PLAYERS;
		moreButton.enabled = players < MAX_PLAYERS;
		for (int i = players; i < MAX_PLAYERS; i++) {
			nameFields.get(i).focused = false;
		}
	}

	private void start() {
		String[] names = new String[players];
		for (int i = 0; i < players; i++) {
			String typed = nameFields.get(i).value().trim();
			names[i] = typed.isEmpty() ? "Player " + (i + 1) : typed;
		}
		host.show(new TableScreen(host, names));
	}

	public void layout(int w, int h) {
		backButton.setBounds(16, 10, 90, 32);
		stepperX = w / 2;
		stepperY = 118;
		fewerButton.setBounds(stepperX - 90, stepperY - 20, 40, 40);
		moreButton.setBounds(stepperX + 50, stepperY - 20, 40, 40);

		int columns = 2;
		int fieldW = 250;
		int fieldH = 38;
		int gapX = 24;
		int gapY = 14;
		int gridW = columns * fieldW + (columns - 1) * gapX;
		int gridX = w / 2 - gridW / 2;
		int gridY = stepperY + 60;
		for (int i = 0; i < MAX_PLAYERS; i++) {
			int col = i % columns;
			int row = i / columns;
			nameFields.get(i).setBounds(gridX + col * (fieldW + gapX) + 34, gridY + row * (fieldH + gapY), fieldW - 34, fieldH);
		}
		int rows = (players + columns - 1) / columns;
		startButton.setBounds(w / 2 - 130, gridY + rows * (fieldH + gapY) + 24, 260, 48);
		setPlayers(players);
	}

	public void paint(Graphics2D g, int w, int h) {
		g.setColor(Theme.BG);
		g.fillRect(0, 0, w, h);
		backButton.paint(g);

		g.setFont(Theme.TITLE);
		g.setColor(Theme.ACCENT);
		Theme.centerText(g, "New Game", w / 2, 60);

		g.setFont(Theme.BODY);
		g.setColor(Theme.TEXT_DIM);
		Theme.centerText(g, "Players", stepperX, stepperY - 34);
		fewerButton.paint(g);
		moreButton.paint(g);
		g.setFont(Theme.TITLE);
		g.setColor(Theme.TEXT);
		Theme.centerText(g, "" + players, stepperX, stepperY);

		g.setFont(Theme.BODY);
		for (int i = 0; i < players; i++) {
			UiTextField field = nameFields.get(i);
			g.setColor(Theme.TEXT_DIM);
			Theme.centerText(g, "" + (i + 1), field.x - 18, field.y + field.h / 2);
			field.paint(g);
		}
		startButton.paint(g);

		g.setFont(Theme.SMALL);
		g.setColor(Theme.TEXT_DIM);
		Theme.centerText(g, "Click a box to type a name. Blank names become Player 1, Player 2, ...", w / 2, startButton.y + startButton.h + 28);
	}

	public void mousePressed(int x, int y) {
		if (backButton.click(x, y) || fewerButton.click(x, y) || moreButton.click(x, y) || startButton.click(x, y)) {
			return;
		}
		for (int i = 0; i < MAX_PLAYERS; i++) {
			if (i < players) {
				nameFields.get(i).click(x, y);
			} else {
				nameFields.get(i).focused = false;
			}
		}
	}

	public void mouseMoved(int x, int y) {
		backButton.updateHover(x, y);
		fewerButton.updateHover(x, y);
		moreButton.updateHover(x, y);
		startButton.updateHover(x, y);
	}

	public boolean keyTyped(char c) {
		for (int i = 0; i < players; i++) {
			if (nameFields.get(i).keyTyped(c)) {
				return true;
			}
		}
		return false;
	}

	public boolean keyPressed(KeyEvent e) {
		int focused = -1;
		for (int i = 0; i < players; i++) {
			if (nameFields.get(i).focused) {
				focused = i;
			}
		}
		if (e.getKeyCode() == KeyEvent.VK_TAB && focused >= 0) {
			nameFields.get(focused).focused = false;
			nameFields.get((focused + 1) % players).focused = true;
			return true;
		}
		if (e.getKeyCode() == KeyEvent.VK_ENTER) {
			start();
			return true;
		}
		return focused >= 0;
	}
}
