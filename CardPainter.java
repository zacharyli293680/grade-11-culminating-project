// CardPainter
// Draws playing cards in code at any size. Cards use the engine's 0..51 encoding:
// rank = card % 13 (0 = two ... 12 = ace), suit = card / 13.

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Stroke;

public class CardPainter {

	private static final String[] RANKS = {"2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A"};
	private static final String[] SUITS = {"♠", "♥", "♦", "♣"}; // spades, hearts, diamonds, clubs

	public static String rankLabel(int card) {
		return RANKS[HandEvaluator.rank(card)];
	}

	public static String suitGlyph(int card) {
		return SUITS[HandEvaluator.suit(card)];
	}

	public static boolean isRed(int card) {
		int suit = HandEvaluator.suit(card);
		return suit == 1 || suit == 2;
	}

	public static void paintFace(Graphics2D g, int card, int x, int y, int w, int h) {
		int radius = Math.max(6, h / 9);
		Theme.roundRect(g, x, y, w, h, radius, Theme.CARD_WHITE, Theme.CARD_BORDER);
		Color ink = isRed(card) ? Theme.CARD_RED : Theme.CARD_BLACK;
		g.setColor(ink);

		String rank = rankLabel(card);
		String suit = suitGlyph(card);
		int cornerSize = Math.max(9, (int) (h * 0.24));
		Font rankFont = new Font(Theme.FAMILY, Font.BOLD, cornerSize);
		Font smallSuit = Theme.SYMBOL.deriveFont((float) Math.max(8, h * 0.2));
		Font bigSuit = Theme.SYMBOL.deriveFont((float) Math.max(12, h * 0.46));

		// top-left corner: rank above a small suit
		g.setFont(rankFont);
		int pad = Math.max(3, w / 12);
		g.drawString(rank, x + pad, y + pad + g.getFontMetrics().getAscent() - 2);
		g.setFont(smallSuit);
		g.drawString(suit, x + pad, y + pad + cornerSize + g.getFontMetrics().getAscent() - 4);

		// large suit in the lower centre
		g.setFont(bigSuit);
		Theme.centerText(g, suit, x + w / 2, y + (int) (h * 0.66));
	}

	public static void paintBack(Graphics2D g, int x, int y, int w, int h) {
		int radius = Math.max(6, h / 9);
		Theme.roundRect(g, x, y, w, h, radius, Theme.CARD_BACK, Theme.CARD_BORDER);
		int inset = Math.max(3, w / 9);
		Theme.roundRect(g, x + inset, y + inset, w - inset * 2, h - inset * 2, Math.max(4, radius - 2), null, Theme.CARD_BACK_LIGHT, 1.5f);
		// a simple lattice inside the border
		Stroke old = g.getStroke();
		g.setStroke(new BasicStroke(1f));
		g.setColor(Theme.alpha(Theme.CARD_BACK_LIGHT, 120));
		int step = Math.max(5, w / 6);
		int left = x + inset + 2;
		int top = y + inset + 2;
		int right = x + w - inset - 2;
		int bottom = y + h - inset - 2;
		g.clipRect(left, top, right - left, bottom - top);
		for (int i = -h; i < w + h; i += step) {
			g.drawLine(left + i, top, left + i + (bottom - top), bottom);
			g.drawLine(left + i, bottom, left + i + (bottom - top), top);
		}
		g.setClip(null);
		g.setStroke(old);
	}

	// An empty outline where a card will be dealt later.
	public static void paintSlot(Graphics2D g, int x, int y, int w, int h) {
		int radius = Math.max(6, h / 9);
		Theme.roundRect(g, x, y, w, h, radius, Theme.alpha(Color.BLACK, 40), Theme.alpha(Color.WHITE, 60));
	}
}
