// UiButton
// A rounded button drawn in code. The same rectangle is used for drawing and for
// hit-testing, so there are no separate magic click coordinates.

package poker;

import java.awt.Color;
import java.awt.Graphics2D;

public class UiButton {

	public enum Style { PRIMARY, SECONDARY, DANGER, GHOST }

	public int x, y, w, h;
	public String label;
	public Style style;
	public boolean enabled = true;
	public boolean visible = true;
	public boolean hover = false;
	public boolean selected = false;
	public Runnable onClick;

	public UiButton(String label, Style style, Runnable onClick) {
		this.label = label;
		this.style = style;
		this.onClick = onClick;
	}

	public void setBounds(int x, int y, int w, int h) {
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
	}

	public boolean contains(int px, int py) {
		return visible && px >= x && px <= x + w && py >= y && py <= y + h;
	}

	// Runs the action if the point is on an enabled button. Returns true if it did.
	public boolean click(int px, int py) {
		if (enabled && contains(px, py)) {
			if (onClick != null) {
				onClick.run();
			}
			return true;
		}
		return false;
	}

	public void updateHover(int px, int py) {
		hover = enabled && contains(px, py);
	}

	public void paint(Graphics2D g) {
		if (!visible) {
			return;
		}
		Color fill;
		Color text;
		Color border = null;
		switch (style) {
			case PRIMARY:
				fill = Theme.ACCENT;
				text = Theme.TEXT_DARK;
				break;
			case DANGER:
				fill = Theme.DANGER;
				text = Theme.TEXT;
				break;
			case GHOST:
				fill = null;
				text = Theme.TEXT;
				border = Theme.PLATE_BORDER;
				break;
			default:
				fill = Theme.PLATE;
				text = Theme.TEXT;
				border = Theme.PLATE_BORDER;
				break;
		}
		if (hover && fill != null) {
			fill = Theme.lighten(fill, 18);
		}
		if (!enabled) {
			if (fill != null) {
				fill = Theme.alpha(fill, 70);
			}
			text = Theme.alpha(text, 90);
			if (border != null) {
				border = Theme.alpha(border, 90);
			}
		}
		if (selected) {
			border = Theme.ACCENT;
		}
		Theme.roundRect(g, x, y, w, h, Theme.RADIUS, fill, border, selected ? 2f : 1f);
		g.setColor(text);
		g.setFont(Theme.BOLD);
		Theme.fitCenterText(g, label, x + w / 2, y + h / 2, w - 12);
	}
}
