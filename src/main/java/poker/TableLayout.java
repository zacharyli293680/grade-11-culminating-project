// TableLayout
// Pure geometry for the table screen, computed from the panel size and the number of
// seats. Nothing is drawn here; TableScreen reads these rectangles and points.

package poker;

import java.awt.Point;
import java.awt.Rectangle;

public class TableLayout {

	public static final int TOP_BAR_HEIGHT = 48;
	public static final int ACTION_PANEL_HEIGHT = 176;

	public final int width;
	public final int height;
	public final double scale;

	public final Rectangle topBar;
	public final Rectangle tableArea;
	public final Rectangle actionPanel;

	// the felt oval
	public final int centerX;
	public final int centerY;
	public final int tableRx;
	public final int tableRy;

	public final int plateW;
	public final int plateH;
	public final int seatCardW;
	public final int seatCardH;
	public final int cardW;
	public final int cardH;

	public final Point[] seatCenter;
	public final Rectangle[] plate;
	public final Point[] card1; // top-left corners of a seat's two cards
	public final Point[] card2;
	public final Point[] betPoint;

	public final Rectangle[] communitySlot = new Rectangle[5];
	public final Rectangle potPill;

	public TableLayout(int width, int height, int seats) {
		this.width = width;
		this.height = height;
		scale = Math.max(0.75, Math.min(1.6, Math.min(width / 1000.0, height / 760.0)));

		topBar = new Rectangle(0, 0, width, TOP_BAR_HEIGHT);
		actionPanel = new Rectangle(16, height - ACTION_PANEL_HEIGHT - 12, width - 32, ACTION_PANEL_HEIGHT);
		tableArea = new Rectangle(16, TOP_BAR_HEIGHT + 8, width - 32, actionPanel.y - TOP_BAR_HEIGHT - 16);

		plateW = (int) (124 * scale);
		plateH = (int) (46 * scale);
		seatCardW = (int) (42 * scale);
		seatCardH = (int) (58 * scale);
		cardW = (int) (56 * scale);
		cardH = (int) (78 * scale);

		centerX = tableArea.x + tableArea.width / 2;
		centerY = tableArea.y + tableArea.height / 2 + (int) (6 * scale);
		int seatRx = tableArea.width / 2 - plateW / 2 - 6;
		int seatRy = tableArea.height / 2 - plateH / 2 - 6;
		tableRx = seatRx - (int) (46 * scale);
		tableRy = seatRy - (int) (40 * scale);

		seatCenter = new Point[seats];
		plate = new Rectangle[seats];
		card1 = new Point[seats];
		card2 = new Point[seats];
		betPoint = new Point[seats];
		for (int i = 0; i < seats; i++) {
			// seat 0 at the bottom centre, then clockwise (screen y grows downward)
			double angle = Math.PI / 2 + i * 2 * Math.PI / seats;
			int sx = centerX + (int) Math.round(seatRx * Math.cos(angle));
			int sy = centerY + (int) Math.round(seatRy * Math.sin(angle));
			seatCenter[i] = new Point(sx, sy);
			plate[i] = new Rectangle(sx - plateW / 2, sy - plateH / 2, plateW, plateH);

			// cards sit between the plate and the table centre, slightly overlapping the rail
			double dx = centerX - sx;
			double dy = centerY - sy;
			double len = Math.max(1, Math.sqrt(dx * dx + dy * dy));
			double ux = dx / len;
			double uy = dy / len;
			int gap = (int) (4 * scale);
			double pairHalfW = seatCardW + gap / 2.0;
			double plateReach = reach(plateW / 2.0, plateH / 2.0, ux, uy);
			double cardReach = reach(pairHalfW, seatCardH / 2.0, ux, uy);
			double cardDist = plateReach + cardReach + 6 * scale;
			int cx = sx + (int) Math.round(ux * cardDist);
			int cy = sy + (int) Math.round(uy * cardDist);
			card1[i] = new Point(cx - seatCardW - gap / 2, cy - seatCardH / 2);
			card2[i] = new Point(cx + gap / 2, cy - seatCardH / 2);

			double betDist = cardDist + cardReach + 16 * scale;
			betPoint[i] = new Point(sx + (int) Math.round(ux * betDist), sy + (int) Math.round(uy * betDist));
		}

		int gap = (int) (8 * scale);
		int totalW = cardW * 5 + gap * 4;
		int startX = centerX - totalW / 2;
		int cardsY = centerY - cardH / 2 - (int) (12 * scale);
		for (int i = 0; i < 5; i++) {
			communitySlot[i] = new Rectangle(startX + i * (cardW + gap), cardsY, cardW, cardH);
		}
		int pillW = (int) (150 * scale);
		int pillH = (int) (28 * scale);
		potPill = new Rectangle(centerX - pillW / 2, cardsY + cardH + (int) (12 * scale), pillW, pillH);
	}

	// How far a rectangle of the given half size extends from its centre along a direction.
	private static double reach(double halfW, double halfH, double ux, double uy) {
		double alongX = Math.abs(ux) < 1e-6 ? Double.MAX_VALUE : halfW / Math.abs(ux);
		double alongY = Math.abs(uy) < 1e-6 ? Double.MAX_VALUE : halfH / Math.abs(uy);
		return Math.min(alongX, alongY);
	}
}
