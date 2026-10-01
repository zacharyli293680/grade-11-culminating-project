// UiSlider
// A horizontal slider drawn in code, used to pick a raise amount.

import java.awt.Graphics2D;

public class UiSlider {

	public int x, y, w, h;
	public int min = 0;
	public int max = 100;
	public int value = 0;
	public boolean dragging = false;
	public Runnable onChange;

	public void setBounds(int x, int y, int w, int h) {
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
	}

	public void setRange(int min, int max) {
		this.min = min;
		this.max = Math.max(min, max);
		setValue(value);
	}

	public void setValue(int v) {
		value = Math.max(min, Math.min(max, v));
	}

	public boolean contains(int px, int py) {
		return px >= x - 8 && px <= x + w + 8 && py >= y && py <= y + h;
	}

	// Returns true if the press landed on the slider.
	public boolean press(int px, int py) {
		if (!contains(px, py)) {
			return false;
		}
		dragging = true;
		moveTo(px);
		return true;
	}

	public void drag(int px) {
		if (dragging) {
			moveTo(px);
		}
	}

	public void release() {
		dragging = false;
	}

	private void moveTo(int px) {
		double fraction = (px - x) / (double) Math.max(1, w);
		fraction = Math.max(0, Math.min(1, fraction));
		int before = value;
		setValue((int) Math.round(min + fraction * (max - min)));
		if (value != before && onChange != null) {
			onChange.run();
		}
	}

	public void paint(Graphics2D g) {
		int trackY = y + h / 2;
		double fraction = max == min ? 1.0 : (value - min) / (double) (max - min);
		int knobX = x + (int) Math.round(fraction * w);
		g.setColor(Theme.PLATE_BORDER);
		g.fillRoundRect(x, trackY - 3, w, 6, 6, 6);
		g.setColor(Theme.ACCENT);
		g.fillRoundRect(x, trackY - 3, Math.max(0, knobX - x), 6, 6, 6);
		g.setColor(dragging ? Theme.lighten(Theme.ACCENT, 30) : Theme.ACCENT);
		g.fillOval(knobX - 8, trackY - 8, 16, 16);
		g.setColor(Theme.BG);
		g.drawOval(knobX - 8, trackY - 8, 16, 16);
	}
}
