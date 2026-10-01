// AboutScreen
// Instructions and credits, carried over from the original About / Instructions page.

import java.awt.Graphics2D;
import java.util.List;

public class AboutScreen implements Screen {

	private static final String INSTRUCTIONS =
		"Classic Texas Hold'em Poker. Each player gets two cards, five community cards are dealt "
		+ "in the middle, and the best five-card hand wins the pot.\n"
		+ "Everyone plays on this one screen. When it is your turn, take the device, press Start to see "
		+ "your cards, choose Fold, Check/Call or Bet/Raise, then press Confirm and pass the device on.\n"
		+ "Blinds are 5 and 10. The dealer button (D) moves each hand. You have 30 seconds per turn; "
		+ "if time runs out you check when you can, otherwise you fold.\n"
		+ "Run out of chips and you are out. The last player with chips wins the game.";

	private final Poker host;
	private final UiButton backButton;

	public AboutScreen(Poker host) {
		this.host = host;
		backButton = new UiButton("Back", UiButton.Style.SECONDARY, () -> host.show(new MenuScreen(host)));
	}

	public void layout(int w, int h) {
		backButton.setBounds(16, 10, 90, 32);
	}

	public void paint(Graphics2D g, int w, int h) {
		g.setColor(Theme.BG);
		g.fillRect(0, 0, w, h);
		backButton.paint(g);

		g.setFont(Theme.TITLE);
		g.setColor(Theme.ACCENT);
		Theme.centerText(g, "How to Play", w / 2, 60);

		int panelW = Math.min(620, w - 64);
		int panelX = w / 2 - panelW / 2;
		int y = 110;

		g.setFont(Theme.BODY);
		List<String> lines = Theme.wrap(g, INSTRUCTIONS, panelW - 48);
		int lineH = g.getFontMetrics().getHeight() + 3;
		int panelH = lines.size() * lineH + 40;
		Theme.roundRect(g, panelX, y, panelW, panelH, Theme.RADIUS, Theme.PLATE, Theme.PLATE_BORDER);
		g.setColor(Theme.TEXT);
		int textY = y + 30;
		for (String line : lines) {
			g.drawString(line, panelX + 24, textY);
			textY += lineH;
		}

		y += panelH + 24;
		String[] credits = {"Game: Texas Hold'em Poker", "Gamblers: Zachary Li & Sarah Zhou", "Date: January 22, 2024"};
		int creditsH = credits.length * lineH + 40;
		Theme.roundRect(g, panelX, y, panelW, creditsH, Theme.RADIUS, Theme.PLATE, Theme.PLATE_BORDER);
		g.setFont(Theme.LARGE);
		g.setColor(Theme.ACCENT);
		g.drawString("About", panelX + 24, y + 28);
		g.setFont(Theme.BODY);
		g.setColor(Theme.TEXT);
		textY = y + 28 + lineH;
		for (String line : credits) {
			g.drawString(line, panelX + 24, textY);
			textY += lineH;
		}
	}

	public void mousePressed(int x, int y) {
		backButton.click(x, y);
	}

	public void mouseMoved(int x, int y) {
		backButton.updateHover(x, y);
	}
}
