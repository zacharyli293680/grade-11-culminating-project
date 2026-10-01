// Theme
// One place for every colour, font and drawing helper the screens share.

package poker;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.List;

public class Theme {

	// palette
	public static final Color BG = new Color(0x161a23);
	public static final Color BG_LIGHT = new Color(0x1f2430);
	public static final Color RAIL = new Color(0x3b2a1f);
	public static final Color RAIL_EDGE = new Color(0x5a4232);
	public static final Color FELT = new Color(0x1f6f48);
	public static final Color FELT_DARK = new Color(0x175537);
	public static final Color TEXT = new Color(0xf4efe6);
	public static final Color TEXT_DIM = new Color(0xa9a59c);
	public static final Color TEXT_DARK = new Color(0x1b1b22);
	public static final Color PLATE = new Color(0x252b3a);
	public static final Color PLATE_BORDER = new Color(0x3d4760);
	public static final Color ACCENT = new Color(0xf2c14e);
	public static final Color ACCENT_DARK = new Color(0xc79a2a);
	public static final Color DANGER = new Color(0xd9534f);
	public static final Color CALL = new Color(0x3b82c4);
	public static final Color RAISE = new Color(0x3fa66b);
	public static final Color OVERLAY = new Color(0, 0, 0, 160);
	public static final Color CARD_WHITE = new Color(0xfbfaf6);
	public static final Color CARD_BORDER = new Color(0xc9c6bd);
	public static final Color CARD_RED = new Color(0xc8323a);
	public static final Color CARD_BLACK = new Color(0x1d1d24);
	public static final Color CARD_BACK = new Color(0x2c4a8a);
	public static final Color CARD_BACK_LIGHT = new Color(0x4a6bb5);

	public static final int RADIUS = 12;
	public static final int PAD = 12;

	public static final String FAMILY = pickFamily();
	public static final Font SMALL = new Font(FAMILY, Font.PLAIN, 12);
	public static final Font BODY = new Font(FAMILY, Font.PLAIN, 14);
	public static final Font BOLD = new Font(FAMILY, Font.BOLD, 14);
	public static final Font LARGE = new Font(FAMILY, Font.BOLD, 18);
	public static final Font TITLE = new Font(FAMILY, Font.BOLD, 28);
	public static final Font HERO = new Font(FAMILY, Font.BOLD, 76);
	// logical fonts fall back to other fonts for symbols, which physical fonts do not
	public static final Font SYMBOL = new Font("SansSerif", Font.BOLD, 14);

	private static String pickFamily() {
		try {
			for (String family : GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()) {
				if (family.equals("Segoe UI")) {
					return family;
				}
			}
		} catch (Exception e) {
			// headless or no fonts: use the logical font
		}
		return "SansSerif";
	}

	public static void antialias(Graphics2D g) {
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
		g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
	}

	public static int textWidth(Graphics2D g, String text) {
		return g.getFontMetrics().stringWidth(text);
	}

	// Draws text horizontally and vertically centred on (cx, cy).
	public static void centerText(Graphics2D g, String text, int cx, int cy) {
		FontMetrics fm = g.getFontMetrics();
		int x = cx - fm.stringWidth(text) / 2;
		int y = cy + (fm.getAscent() - fm.getDescent()) / 2;
		g.drawString(text, x, y);
	}

	// Draws text at a baseline, shrinking the font if it would run past maxWidth.
	public static void fitText(Graphics2D g, String text, int x, int y, int maxWidth) {
		Font original = g.getFont();
		Font font = original;
		FontMetrics fm = g.getFontMetrics(font);
		while (fm.stringWidth(text) > maxWidth && font.getSize() > 9) {
			font = font.deriveFont((float) (font.getSize() - 1));
			fm = g.getFontMetrics(font);
		}
		g.setFont(font);
		g.drawString(text, x, y);
		g.setFont(original);
	}

	// Like fitText but centred on cx.
	public static void fitCenterText(Graphics2D g, String text, int cx, int cy, int maxWidth) {
		Font original = g.getFont();
		Font font = original;
		FontMetrics fm = g.getFontMetrics(font);
		while (fm.stringWidth(text) > maxWidth && font.getSize() > 9) {
			font = font.deriveFont((float) (font.getSize() - 1));
			fm = g.getFontMetrics(font);
		}
		g.setFont(font);
		centerText(g, text, cx, cy);
		g.setFont(original);
	}

	// Splits text into lines that fit maxWidth with the current font.
	public static List<String> wrap(Graphics2D g, String text, int maxWidth) {
		List<String> lines = new ArrayList<>();
		FontMetrics fm = g.getFontMetrics();
		for (String paragraph : text.split("\n")) {
			StringBuilder line = new StringBuilder();
			for (String word : paragraph.split(" ")) {
				String candidate = line.length() == 0 ? word : line + " " + word;
				if (fm.stringWidth(candidate) > maxWidth && line.length() > 0) {
					lines.add(line.toString());
					line = new StringBuilder(word);
				} else {
					line = new StringBuilder(candidate);
				}
			}
			lines.add(line.toString());
		}
		return lines;
	}

	public static void roundRect(Graphics2D g, int x, int y, int w, int h, int radius, Color fill, Color border) {
		if (fill != null) {
			g.setColor(fill);
			g.fillRoundRect(x, y, w, h, radius, radius);
		}
		if (border != null) {
			g.setColor(border);
			g.drawRoundRect(x, y, w, h, radius, radius);
		}
	}

	public static void roundRect(Graphics2D g, int x, int y, int w, int h, int radius, Color fill, Color border, float borderWidth) {
		Stroke old = g.getStroke();
		g.setStroke(new BasicStroke(borderWidth));
		roundRect(g, x, y, w, h, radius, fill, border);
		g.setStroke(old);
	}

	public static Color alpha(Color c, int alpha) {
		return new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
	}

	public static Color lighten(Color c, int amount) {
		return new Color(Math.min(255, c.getRed() + amount), Math.min(255, c.getGreen() + amount), Math.min(255, c.getBlue() + amount), c.getAlpha());
	}
}
