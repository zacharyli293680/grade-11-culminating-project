// UiTextField
// A single-line text box drawn in code. Click it to focus, then type; backspace deletes.

import java.awt.Graphics2D;

public class UiTextField {

	public int x, y, w, h;
	public final StringBuilder text = new StringBuilder();
	public boolean focused = false;
	public boolean numeric = false;
	public int maxLength = 14;
	public String placeholder = "";
	public Runnable onChange;

	public UiTextField(String initial) {
		text.append(initial);
	}

	public void setBounds(int x, int y, int w, int h) {
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
	}

	public boolean contains(int px, int py) {
		return px >= x && px <= x + w && py >= y && py <= y + h;
	}

	// Focuses the field when clicked, unfocuses when clicked elsewhere.
	public void click(int px, int py) {
		focused = contains(px, py);
	}

	public String value() {
		return text.toString();
	}

	public void setValue(String value) {
		text.setLength(0);
		text.append(value);
	}

	// Returns true if the character was handled.
	public boolean keyTyped(char c) {
		if (!focused) {
			return false;
		}
		if (c == '\b') {
			if (text.length() > 0) {
				text.setLength(text.length() - 1);
				changed();
			}
			return true;
		}
		if (c < 32 || c == 127) {
			return false; // control characters such as Enter are left to the screen
		}
		if (numeric && !Character.isDigit(c)) {
			return true;
		}
		if (text.length() < maxLength) {
			text.append(c);
			changed();
		}
		return true;
	}

	private void changed() {
		if (onChange != null) {
			onChange.run();
		}
	}

	public void paint(Graphics2D g) {
		Theme.roundRect(g, x, y, w, h, 8, Theme.BG, focused ? Theme.ACCENT : Theme.PLATE_BORDER, focused ? 2f : 1f);
		g.setFont(Theme.BODY);
		String shown = text.length() == 0 ? placeholder : text.toString();
		g.setColor(text.length() == 0 ? Theme.TEXT_DIM : Theme.TEXT);
		int textY = y + h / 2 + (g.getFontMetrics().getAscent() - g.getFontMetrics().getDescent()) / 2;
		Theme.fitText(g, shown, x + 10, textY, w - 20);
		if (focused) {
			int caretX = x + 10 + Math.min(g.getFontMetrics().stringWidth(text.toString()), w - 20) + 1;
			g.setColor(Theme.ACCENT);
			g.drawLine(caretX, y + 7, caretX, y + h - 7);
		}
	}
}
