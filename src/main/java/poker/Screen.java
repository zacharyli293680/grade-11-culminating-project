// Screen
// One screen of the game (menu, setup, table, about). The host panel (Poker) calls
// layout() before every paint so the screen can place its widgets for the current size,
// then forwards mouse and keyboard events. Event methods return true when they used the
// event, so the host knows whether to apply its own shortcuts (such as M for mute).

package poker;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

public interface Screen {

	void layout(int width, int height);

	void paint(Graphics2D g, int width, int height);

	default void mousePressed(int x, int y) {
	}

	default void mouseReleased(int x, int y) {
	}

	default void mouseMoved(int x, int y) {
	}

	default void mouseDragged(int x, int y) {
	}

	default boolean keyPressed(KeyEvent e) {
		return false;
	}

	default boolean keyTyped(char c) {
		return false;
	}

	// Called once a second while the host clock is running.
	default void tick() {
	}
}
