// MenuScreen
// The home screen: a drawn title with four fanned aces, and Play / How to Play / Quit.

package poker;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

public class MenuScreen implements Screen {

	private final Poker host;
	private final UiButton playButton;
	private final UiButton aboutButton;
	private final UiButton quitButton;
	private int width;
	private int height;

	public MenuScreen(Poker host) {
		this.host = host;
		playButton = new UiButton("Play", UiButton.Style.PRIMARY, () -> host.show(new SetupScreen(host)));
		aboutButton = new UiButton("How to Play", UiButton.Style.SECONDARY, () -> host.show(new AboutScreen(host)));
		quitButton = new UiButton("Quit", UiButton.Style.GHOST, host::quit);
	}

	public void layout(int w, int h) {
		width = w;
		height = h;
		int bw = 260;
		int bh = 48;
		int x = w / 2 - bw / 2;
		int y = (int) (h * 0.58);
		playButton.setBounds(x, y, bw, bh);
		aboutButton.setBounds(x, y + bh + 14, bw, bh);
		quitButton.setBounds(x, y + (bh + 14) * 2, bw, bh);
	}

	public void paint(Graphics2D g, int w, int h) {
		g.setColor(Theme.BG);
		g.fillRect(0, 0, w, h);

		// a soft felt disc behind the cards
		int discR = (int) (Math.min(w, h) * 0.23);
		int discY = (int) (h * 0.27);
		g.setColor(Theme.FELT_DARK);
		g.fillOval(w / 2 - discR, discY - discR, discR * 2, discR * 2);
		g.setColor(Theme.FELT);
		g.fillOval(w / 2 - discR + 10, discY - discR + 10, discR * 2 - 20, discR * 2 - 20);

		// four aces fanned around a pivot below them
		int cardW = (int) (discR * 0.55);
		int cardH = (int) (cardW * 1.4);
		int pivotX = w / 2;
		int pivotY = discY + (int) (discR * 0.55);
		int[] aces = {12, 25, 38, 51};
		double[] angles = {-27, -9, 9, 27};
		for (int i = 0; i < aces.length; i++) {
			AffineTransform saved = g.getTransform();
			g.rotate(Math.toRadians(angles[i]), pivotX, pivotY);
			CardPainter.paintFace(g, aces[i], pivotX - cardW / 2, pivotY - cardH - (int) (discR * 0.35), cardW, cardH);
			g.setTransform(saved);
		}

		g.setFont(Theme.HERO);
		g.setColor(Theme.ACCENT);
		Theme.centerText(g, "POKER", w / 2, (int) (h * 0.47));
		g.setFont(Theme.BODY);
		g.setColor(Theme.TEXT_DIM);
		Theme.centerText(g, "Texas Hold'em  ·  2 to 8 players on one screen", w / 2, (int) (h * 0.47) + 44);

		playButton.paint(g);
		aboutButton.paint(g);
		quitButton.paint(g);

		g.setFont(Theme.SMALL);
		g.setColor(Theme.TEXT_DIM);
		Theme.centerText(g, "Zachary Li & Sarah Zhou  ·  press M to mute", w / 2, h - 18);
	}

	public void mousePressed(int x, int y) {
		if (playButton.click(x, y) || aboutButton.click(x, y)) {
			return;
		}
		quitButton.click(x, y);
	}

	public void mouseMoved(int x, int y) {
		playButton.updateHover(x, y);
		aboutButton.updateHover(x, y);
		quitButton.updateHover(x, y);
	}
}
